
package dev.ua.ikeepcalm.policeCatwalk.config;

import dev.ua.ikeepcalm.policeCatwalk.PoliceCatwalk;
import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;

@Getter
public class PoliceConfig {
    
    private final int preparationTimeSeconds;
    private final int requestCooldownSeconds;
    private final boolean enableParticleEffects;
    private final boolean enableSoundEffects;
    private final String defaultLanguage;
    private final boolean logTeleportOperations;
    private final int maxLogFileSizeMB;
    
    public PoliceConfig(PoliceCatwalk plugin) {
        FileConfiguration config = plugin.getConfig();
        
        this.preparationTimeSeconds = config.getInt("teleport.preparationTimeSeconds", 30);
        this.requestCooldownSeconds = config.getInt("teleport.requestCooldownSeconds", 10);
        this.enableParticleEffects = config.getBoolean("effects.enableParticleEffects", true);
        this.enableSoundEffects = config.getBoolean("effects.enableSoundEffects", true);
        this.defaultLanguage = config.getString("language.default", "uk");
        this.logTeleportOperations = config.getBoolean("logging.enableTeleportLog", true);
        this.maxLogFileSizeMB = config.getInt("logging.maxLogFileSizeMB", 10);
    }
}