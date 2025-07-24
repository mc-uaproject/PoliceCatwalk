package dev.ua.ikeepcalm.policeCatwalk.utils;

import dev.ua.ikeepcalm.policeCatwalk.PoliceCatwalk;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

public class ParticleEffects {

    public static void playTeleportEffect(Player player) {
        Location location = player.getLocation();
        
        BukkitTask spiralTask = Bukkit.getScheduler().runTaskTimer(PoliceCatwalk.getInstance(), new Runnable() {
            int ticks = 0;
            double radius = 3.0;
            double height = 4.0;
            
            @Override
            public void run() {
                if (ticks >= 40) {
                    this.cancel();
                    return;
                }
                
                double currentRadius = radius * (1.0 - (ticks / 40.0));
                double currentHeight = height * (ticks / 40.0);
                
                for (int i = 0; i < 8; i++) {
                    double angle = (ticks * 0.5 + i * 45) * Math.PI / 180;
                    double x = location.getX() + currentRadius * Math.cos(angle);
                    double z = location.getZ() + currentRadius * Math.sin(angle);
                    double y = location.getY() + currentHeight;
                    
                    Location particleLocation = new Location(location.getWorld(), x, y, z);
                    
                    location.getWorld().spawnParticle(
                            Particle.PORTAL,
                            particleLocation,
                            3,
                            0.1, 0.1, 0.1,
                            0.02
                    );
                    
                    if (ticks % 5 == 0) {
                        location.getWorld().spawnParticle(
                                Particle.ENCHANT,
                                particleLocation,
                                2,
                                0.05, 0.05, 0.05,
                                0.01
                        );
                    }
                }
                
                if (ticks == 0) {
                    player.playSound(location, Sound.ENTITY_ENDERMAN_TELEPORT, 0.8f, 0.8f);
                } else if (ticks == 20) {
                    player.playSound(location, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.2f);
                } else if (ticks == 35) {
                    player.playSound(location, Sound.ENTITY_ENDERMAN_TELEPORT, 1.2f, 1.5f);
                }
                
                ticks++;
            }
            
            private void cancel() {
                Bukkit.getScheduler().cancelTask(this.hashCode());
            }
        }, 0L, 1L);
    }

    public static void playReturnEffect(Player player) {
        Location location = player.getLocation();
        
        location.getWorld().spawnParticle(
                Particle.REVERSE_PORTAL,
                location.clone().add(0, 1, 0),
                50,
                1.0, 1.0, 1.0,
                0.1
        );
        
        location.getWorld().spawnParticle(
                Particle.ENCHANT,
                location.clone().add(0, 1, 0),
                30,
                0.8, 0.8, 0.8,
                0.05
        );
        
        player.playSound(location, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 0.6f);
        
        Bukkit.getScheduler().runTaskLater(PoliceCatwalk.getInstance(), () -> {
            location.getWorld().spawnParticle(
                    Particle.TOTEM_OF_UNDYING,
                    location.clone().add(0, 1, 0),
                    20,
                    0.5, 0.5, 0.5,
                    0.02
            );
            player.playSound(location, Sound.ITEM_TOTEM_USE, 0.5f, 1.2f);
        }, 10L);
    }
}