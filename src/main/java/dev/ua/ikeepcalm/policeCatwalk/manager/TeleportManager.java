package dev.ua.ikeepcalm.policeCatwalk.manager;

import dev.ua.ikeepcalm.policeCatwalk.PoliceCatwalk;
import dev.ua.ikeepcalm.policeCatwalk.dto.TeleportRequest;
import dev.ua.ikeepcalm.policeCatwalk.dto.TeleportResponse;
import dev.ua.ikeepcalm.policeCatwalk.utils.ParticleEffects;
import lombok.Data;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TeleportManager {

    private final PoliceCatwalk plugin;
    private final TranslationManager translationManager;
    private final Path logFile;
    private final DateTimeFormatter logFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final Map<String, Long> requestCooldowns = new ConcurrentHashMap<>();
    private final Map<String, TeleportSession> activeSessions = new ConcurrentHashMap<>();
    private final Map<String, BukkitTask> preparationTasks = new ConcurrentHashMap<>();
    private final Map<String, Integer> failedAttempts = new ConcurrentHashMap<>();

    public TeleportManager(PoliceCatwalk plugin, TranslationManager translationManager) {
        this.plugin = plugin;
        this.translationManager = translationManager;
        this.logFile = plugin.getDataFolder().toPath().resolve("teleport_log.txt");

        try {
            Files.createDirectories(logFile.getParent());
            if (!Files.exists(logFile)) {
                Files.createFile(logFile);
            }
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to create log file: " + e.getMessage());
        }
    }

    public TeleportResponse processTeleportRequest(TeleportRequest request) {
        String playerName = request.getPlayerName();
        long currentTime = System.currentTimeMillis();

        int attempts = failedAttempts.getOrDefault(playerName, 0);
        long cooldownTime = attempts > 3 ? 30000 : 10000;

        if (requestCooldowns.containsKey(playerName) &&
            currentTime - requestCooldowns.get(playerName) < cooldownTime) {
            return TeleportResponse.builder()
                    .success(false)
                    .message("Request cooldown active. Please wait " + (cooldownTime / 1000) + " seconds.")
                    .playerName(playerName)
                    .timestamp(currentTime)
                    .build();
        }

        Player player = Bukkit.getPlayer(playerName);
        if (player == null || !player.isOnline()) {
            return TeleportResponse.builder()
                    .success(false)
                    .message("Player not found or offline")
                    .playerName(playerName)
                    .timestamp(currentTime)
                    .build();
        }

        if (activeSessions.containsKey(playerName)) {
            return TeleportResponse.builder()
                    .success(false)
                    .message("Player already has an active teleport session")
                    .playerName(playerName)
                    .timestamp(currentTime)
                    .build();
        }

        Location targetLocation = new Location(
                Bukkit.getWorld(request.getTargetWorld()),
                request.getTargetX(),
                request.getTargetY(),
                request.getTargetZ()
        );

        if (targetLocation.getWorld() == null) {
            return TeleportResponse.builder()
                    .success(false)
                    .message("Target world not found")
                    .playerName(playerName)
                    .timestamp(currentTime)
                    .build();
        }

        if (!isSafeLocation(targetLocation)) {
            failedAttempts.put(playerName, attempts + 1);
            return TeleportResponse.builder()
                    .success(false)
                    .message("Target location is unsafe")
                    .playerName(playerName)
                    .timestamp(currentTime)
                    .build();
        }

        requestCooldowns.put(playerName, currentTime);
        String teleportId = "tp_" + playerName + "_" + currentTime;

        TeleportSession session = new TeleportSession(
                teleportId,
                player.getLocation().clone(),
                targetLocation,
                request.getReason(),
                request.getRequesterDiscordId(),
                request.getRequesterDiscordName(),
                currentTime
        );

        activeSessions.put(playerName, session);
        failedAttempts.remove(playerName);
        startPreparationSequence(player, session);
        logTeleportOperation("REQUEST", session, playerName);

        return TeleportResponse.builder()
                .success(true)
                .message("Teleport request initiated successfully")
                .playerName(playerName)
                .timestamp(currentTime)
                .teleportId(teleportId)
                .build();
    }

    public TeleportResponse returnPlayer(String playerName) {
        long currentTime = System.currentTimeMillis();

        TeleportSession session = activeSessions.get(playerName);
        if (session == null) {
            return TeleportResponse.builder()
                    .success(false)
                    .message("No active teleport session found for player")
                    .playerName(playerName)
                    .timestamp(currentTime)
                    .build();
        }

        Player player = Bukkit.getPlayer(playerName);
        if (player == null || !player.isOnline()) {
            activeSessions.remove(playerName);
            return TeleportResponse.builder()
                    .success(false)
                    .message("Player not found or offline")
                    .playerName(playerName)
                    .timestamp(currentTime)
                    .build();
        }

        BukkitTask task = preparationTasks.remove(playerName);
        if (task != null) {
            task.cancel();
        }

        ParticleEffects.playReturnEffect(player);
        player.teleport(session.getOriginalLocation());

        Component returnMessage = translationManager.getMessage("teleport.returned", player);
        player.sendMessage(returnMessage);

        activeSessions.remove(playerName);
        logTeleportOperation("RETURN", session, playerName);

        return TeleportResponse.builder()
                .success(true)
                .message("Player returned successfully")
                .playerName(playerName)
                .timestamp(currentTime)
                .teleportId(session.getTeleportId())
                .build();
    }

    private void startPreparationSequence(Player player, TeleportSession session) {
        Component initialMessage = translationManager.getMessage("teleport.request_received", player)
                .append(Component.text(" "))
                .append(translationManager.getMessage("teleport.reason", player))
                .append(Component.text(": " + (session.getReason() != null ? session.getReason() : "N/A")));

        player.sendMessage(initialMessage);

        BukkitTask countdownTask = Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
            int countdown = 30;

            @Override
            public void run() {
                if (countdown <= 0) {
                    executeTeleport(player, session);
                    preparationTasks.remove(player.getName());
                    return;
                }

                if (countdown <= 10 || countdown % 5 == 0) {
                    Component countdownMessage = translationManager.getMessage("teleport.countdown", player)
                            .append(Component.text(": " + countdown));

                    player.sendActionBar(countdownMessage);

                    if (countdown <= 5) {
                        Title title = Title.title(
                                translationManager.getMessage("teleport.preparing", player),
                                Component.text(String.valueOf(countdown))
                        );
                        player.showTitle(title);
                    }
                }

                countdown--;
            }
        }, 0L, 20L);

        preparationTasks.put(player.getName(), countdownTask);
    }

    private void executeTeleport(Player player, TeleportSession session) {
        ParticleEffects.playTeleportEffect(player);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            player.teleport(session.getTargetLocation());

            Component teleportedMessage = translationManager.getMessage("teleport.completed", player);
            player.sendMessage(teleportedMessage);

            Title completedTitle = Title.title(
                    translationManager.getMessage("teleport.completed", player),
                    Component.empty()
            );
            player.showTitle(completedTitle);

            logTeleportOperation("EXECUTE", session, player.getName());
        }, 40L);
    }

    private void logTeleportOperation(String operation, TeleportSession session, String playerName) {
        try {
            String logEntry = String.format("[%s] %s - Player: %s, ID: %s, Reason: %s, Requester: %s%n",
                    LocalDateTime.now().format(logFormatter),
                    operation,
                    playerName,
                    session.getTeleportId(),
                    session.getReason() != null ? session.getReason() : "N/A",
                    session.getRequesterDiscordName() != null ? session.getRequesterDiscordName() : "N/A"
            );

            Files.write(logFile, logEntry.getBytes(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to write to log file: " + e.getMessage());
        }
    }

    private boolean isSafeLocation(Location location) {
        if (location.getWorld() == null) {
            return false;
        }

        double x = location.getX();
        double y = location.getY();
        double z = location.getZ();

        if (Math.abs(x) > 30000000 || Math.abs(z) > 30000000) {
            return false;
        }

        if (y < -64 || y > 320) {
            return false;
        }

        try {
            Location checkLocation = location.clone();
            if (!location.getWorld().isChunkLoaded(checkLocation.getBlockX() >> 4, checkLocation.getBlockZ() >> 4)) {
                location.getWorld().loadChunk(checkLocation.getBlockX() >> 4, checkLocation.getBlockZ() >> 4);
            }

            if (checkLocation.getBlock().getType().isSolid() ||
                checkLocation.clone().add(0, 1, 0).getBlock().getType().isSolid()) {
                return false;
            }

            if (checkLocation.clone().subtract(0, 1, 0).getBlock().getType().isAir()) {
                return false;
            }

        } catch (Exception e) {
            plugin.getLogger().warning("Error checking location safety: " + e.getMessage());
            return false;
        }

        return true;
    }

    public void stop() {
        for (BukkitTask task : preparationTasks.values()) {
            if (task != null) {
                task.cancel();
            }
        }
        preparationTasks.clear();
        activeSessions.clear();
        failedAttempts.clear();
    }

    @Data
    private static class TeleportSession {
        private final String teleportId;
        private final Location originalLocation;
        private final Location targetLocation;
        private final String reason;
        private final String requesterDiscordId;
        private final String requesterDiscordName;
        private final long timestamp;
    }
}