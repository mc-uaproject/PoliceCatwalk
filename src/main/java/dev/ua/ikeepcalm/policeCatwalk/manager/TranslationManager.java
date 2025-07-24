package dev.ua.ikeepcalm.policeCatwalk.manager;

import dev.ua.ikeepcalm.policeCatwalk.PoliceCatwalk;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

public class TranslationManager {
    
    private final PoliceCatwalk plugin;
    private final Map<String, YamlConfiguration> translations = new HashMap<>();
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final String defaultLanguage = "uk";

    public TranslationManager(PoliceCatwalk plugin) {
        this.plugin = plugin;
        loadTranslations();
    }

    private void loadTranslations() {
        loadLanguageFile("uk");
        loadLanguageFile("en");
    }

    private void loadLanguageFile(String language) {
        File langDir = new File(plugin.getDataFolder(), "lang");
        File langFile = new File(langDir, language + ".yml");

        if (!langDir.exists()) {
            langDir.mkdirs();
        }

        if (!langFile.exists()) {
            try (InputStream resource = plugin.getResource("lang/" + language + ".yml")) {
                if (resource != null) {
                    Files.copy(resource, langFile.toPath());
                } else {
                    createDefaultTranslation(langFile, language);
                }
            } catch (IOException e) {
                plugin.getLogger().warning("Could not create language file for " + language);
                createDefaultTranslation(langFile, language);
            }
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(langFile);
        translations.put(language, config);
        plugin.getLogger().info("Loaded translations for language: " + language);
    }

    private void createDefaultTranslation(File file, String language) {
        YamlConfiguration config = new YamlConfiguration();
        
        if (language.equals("uk")) {
            config.set("teleport.request_received", "<blue><bold>ПОЛІЦІЯ</bold></blue> <white>Ви отримали запит на телепортацію.</white>");
            config.set("teleport.reason", "<yellow>Причина</yellow>");
            config.set("teleport.countdown", "<red><bold>Телепортація через</bold></red>");
            config.set("teleport.preparing", "<gold>Підготовка до телепортації...</gold>");
            config.set("teleport.completed", "<green><bold>Телепортацію завершено!</bold></green>");
            config.set("teleport.returned", "<blue>Ви повернулися на початкову позицію.</blue>");
        } else {
            config.set("teleport.request_received", "<blue><bold>POLICE</bold></blue> <white>You have received a teleport request.</white>");
            config.set("teleport.reason", "<yellow>Reason</yellow>");
            config.set("teleport.countdown", "<red><bold>Teleporting in</bold></red>");
            config.set("teleport.preparing", "<gold>Preparing for teleportation...</gold>");
            config.set("teleport.completed", "<green><bold>Teleportation completed!</bold></green>");
            config.set("teleport.returned", "<blue>You have been returned to your original position.</blue>");
        }

        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save default translation file for " + language);
        }
    }

    public Component getMessage(String key, Player player) {
        return getMessage(key, getPlayerLanguage(player));
    }

    public Component getMessage(String key, String language) {
        YamlConfiguration config = translations.get(language);
        if (config == null) {
            config = translations.get(defaultLanguage);
        }

        String message = config.getString(key);
        if (message == null) {
            YamlConfiguration defaultConfig = translations.get(defaultLanguage);
            message = defaultConfig != null ? defaultConfig.getString(key) : key;
        }

        if (message == null) {
            return Component.text(key).color(NamedTextColor.RED);
        }

        try {
            return miniMessage.deserialize(message);
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to parse message: " + key + " - " + message);
            return Component.text(message).color(NamedTextColor.WHITE);
        }
    }

    private String getPlayerLanguage(Player player) {
        String locale = player.locale().getLanguage();
        return translations.containsKey(locale) ? locale : defaultLanguage;
    }
}