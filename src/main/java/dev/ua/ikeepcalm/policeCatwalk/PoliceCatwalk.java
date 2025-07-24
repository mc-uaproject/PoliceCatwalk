package dev.ua.ikeepcalm.policeCatwalk;

import dev.ua.ikeepcalm.policeCatwalk.api.PoliceTeleportEndpoint;
import dev.ua.ikeepcalm.policeCatwalk.config.PoliceConfig;
import dev.ua.ikeepcalm.policeCatwalk.manager.TeleportManager;
import dev.ua.ikeepcalm.policeCatwalk.manager.TranslationManager;
import dev.ua.uaproject.catwalk.hub.webserver.services.CatWalkWebserverService;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public class PoliceCatwalk extends JavaPlugin {

    @Getter
    @Setter
    private static PoliceCatwalk instance;

    private PoliceConfig policeConfig;
    private TranslationManager translationManager;
    private TeleportManager teleportManager;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();
        policeConfig = new PoliceConfig(this);
        translationManager = new TranslationManager(this);
        teleportManager = new TeleportManager(this, translationManager);

        CatWalkWebserverService webserverService = Bukkit.getServicesManager().load(CatWalkWebserverService.class);

        if (webserverService == null) {
            getLogger().severe("Failed to load CatWalkWebserverService from Bukkit ServicesManager.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        webserverService.registerHandlers(new PoliceTeleportEndpoint(teleportManager));

        log("PoliceCatwalk has been enabled!");
    }

    @Override
    public void onDisable() {
        if (teleportManager != null) {
            teleportManager.stop();
        }
        log("PoliceCatwalk has been disabled!");
    }

    public static void log(String message) {
        Component logMessage = Component.text("[PoliceCatwalk] ")
                .color(NamedTextColor.BLUE)
                .append(Component.text(message).color(NamedTextColor.WHITE));
        Bukkit.getConsoleSender().sendMessage(logMessage);
    }

    public static void error(String message) {
        Component errorMessage = Component.text("[PoliceCatwalk] ")
                .color(NamedTextColor.RED)
                .append(Component.text(message).color(NamedTextColor.WHITE));
        Bukkit.getConsoleSender().sendMessage(errorMessage);
    }
}