package de.delayeddamage;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DelayedDamagePlugin extends JavaPlugin implements Listener {

    private Map<UUID, PlayerData> playerDataMap = new HashMap<>();
    private BukkitTask timerTask;
    private DataManager dataManager;

    @Override
    public void onEnable() {
        // Plugin startup logic
        getLogger().info("DelayedDamage Plugin wurde aktiviert!");
        
        // Data Manager initialisieren
        dataManager = new DataManager(this);
        
        // Events registrieren
        Bukkit.getPluginManager().registerEvents(this, this);
        
        // Commands registrieren
        getCommand("delayeddamage").setExecutor(new DelayedDamageCommand(this));
        
        // Timer starten
        startTimer();
        
        // Daten laden
        loadAllPlayerData();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getLogger().info("DelayedDamage Plugin wurde deaktiviert!");
        
        // Timer stoppen
        if (timerTask != null) {
            timerTask.cancel();
        }
        
        // Alle Daten speichern
        saveAllPlayerData();
    }

    private void startTimer() {
        timerTask = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    PlayerData data = getPlayerData(player);
                    
                    // Timer erhöhen
                    data.incrementTimer();
                    
                    // ActionBar aktualisieren
                    updateActionBar(player, data);
                    
                    // Prüfen ob 5 Minuten erreicht
                    if (data.getTimerSeconds() == 300 && data.getStoredDamage() > 0) { // 5 Minuten = 300 Sekunden
                        applyStoredDamage(player, data);
                    }
                }
            }
        }.runTaskTimer(this, 0L, 20L); // Jede Sekunde (20 Ticks)
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        
        Player player = (Player) event.getEntity();
        PlayerData data = getPlayerData(player);
        
        // Schaden sammeln statt direkt anwenden
        double damage = event.getFinalDamage();
        data.addDamage(damage);
        
        // Event canceln um direkten Schaden zu verhindern
        event.setCancelled(true);
        
        getLogger().info("Schaden gesammelt für " + player.getName() + ": " + damage + " (Gesamt: " + data.getStoredDamage() + ")");
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        PlayerData data = getPlayerData(player);
        
        // Spieler in Spectator Modus setzen
        player.setGameMode(GameMode.SPECTATOR);
        
        // Alle anderen Spieler auch in Spectator setzen
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!p.equals(player)) {
                p.setGameMode(GameMode.SPECTATOR);
            }
        }
        
        getLogger().info("Spieler " + player.getName() + " ist gestorben. Alle Spieler sind jetzt Spectator.");
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        PlayerData data = getPlayerData(player);
        
        // Timer pausieren (wird bei Rejoin fortgesetzt)
        savePlayerData(player, data);
        
        getLogger().info("Timer für " + player.getName() + " pausiert bei: " + formatTime(data.getTimerSeconds()));
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        loadPlayerData(player);
        
        getLogger().info("Timer für " + player.getName() + " fortgesetzt.");
    }

    private void updateActionBar(Player player, PlayerData data) {
        String timeStr = formatTime(data.getTimerSeconds());
        String message = "§c⏰ Timer: " + timeStr;
        
        if (data.getStoredDamage() > 0) {
            message += " §7| §4❤ Schaden: " + String.format("%.1f", data.getStoredDamage());
        }
        
        player.sendActionBar(message);
    }

    private String formatTime(long totalSeconds) {
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        
        if (hours > 0) {
            return String.format("%dh %02dm %02ds", hours, minutes, seconds);
        } else {
            return String.format("%02dm %02ds", minutes, seconds);
        }
    }

    private void applyStoredDamage(Player player, PlayerData data) {
        double damage = data.getStoredDamage();
        
        if (damage > 0) {
            // Schaden anwenden
            double newHealth = Math.max(0, player.getHealth() - damage);
            player.setHealth(newHealth);
            
            // Nachricht senden
            player.sendMessage("§c§l💀 DELAYED DAMAGE! §r§cDu erhältst " + String.format("%.1f", damage) + " Schaden!");
            
            // Schaden zurücksetzen
            data.resetDamage();
            
            getLogger().info("Delayed Damage angewendet auf " + player.getName() + ": " + damage);
            
            // Prüfen ob Spieler stirbt
            if (newHealth <= 0) {
                player.damage(0.1); // Trigger death
            }
        }
    }

    public void triggerDamageForPlayer(Player player) {
        PlayerData data = getPlayerData(player);
        applyStoredDamage(player, data);
    }

    public void resetTimerForPlayer(Player player) {
        PlayerData data = getPlayerData(player);
        data.resetTimer();
        data.resetDamage();
        player.sendMessage("§a✓ Timer und Schaden wurden zurückgesetzt!");
    }

    private PlayerData getPlayerData(Player player) {
        return playerDataMap.computeIfAbsent(player.getUniqueId(), k -> new PlayerData());
    }

    private void saveAllPlayerData() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            savePlayerData(player, getPlayerData(player));
        }
    }

    private void loadAllPlayerData() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            loadPlayerData(player);
        }
    }

    private void savePlayerData(Player player, PlayerData data) {
        dataManager.savePlayerData(player.getUniqueId(), data);
    }

    private void loadPlayerData(Player player) {
        PlayerData data = dataManager.loadPlayerData(player.getUniqueId());
        if (data != null) {
            playerDataMap.put(player.getUniqueId(), data);
        }
    }

    // Getter für andere Klassen
    public Map<UUID, PlayerData> getPlayerDataMap() {
        return playerDataMap;
    }
}