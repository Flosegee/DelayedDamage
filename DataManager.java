package de.delayeddamage;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class DataManager {
    private final DelayedDamagePlugin plugin;
    private final File dataFolder;

    public DataManager(DelayedDamagePlugin plugin) {
        this.plugin = plugin;
        this.dataFolder = new File(plugin.getDataFolder(), "playerdata");
        
        // Ordner erstellen falls nicht vorhanden
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
    }

    public void savePlayerData(UUID playerUUID, PlayerData data) {
        File playerFile = new File(dataFolder, playerUUID.toString() + ".yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(playerFile);
        
        config.set("timerSeconds", data.getTimerSeconds());
        config.set("storedDamage", data.getStoredDamage());
        
        try {
            config.save(playerFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Fehler beim Speichern der Spielerdaten für " + playerUUID + ": " + e.getMessage());
        }
    }

    public PlayerData loadPlayerData(UUID playerUUID) {
        File playerFile = new File(dataFolder, playerUUID.toString() + ".yml");
        
        if (!playerFile.exists()) {
            return new PlayerData(); // Neue Daten wenn Datei nicht existiert
        }
        
        FileConfiguration config = YamlConfiguration.loadConfiguration(playerFile);
        
        long timerSeconds = config.getLong("timerSeconds", 0);
        double storedDamage = config.getDouble("storedDamage", 0.0);
        
        return new PlayerData(timerSeconds, storedDamage);
    }

    public void deletePlayerData(UUID playerUUID) {
        File playerFile = new File(dataFolder, playerUUID.toString() + ".yml");
        if (playerFile.exists()) {
            playerFile.delete();
        }
    }
}