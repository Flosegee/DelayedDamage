package de.delayeddamage;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DelayedDamageCommand implements CommandExecutor {
    private final DelayedDamagePlugin plugin;

    public DelayedDamageCommand(DelayedDamagePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("delayeddamage.admin")) {
            sender.sendMessage("§cDu hast keine Berechtigung für diesen Befehl!");
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "trigger":
                if (args.length < 2) {
                    sender.sendMessage("§cVerwendung: /delayeddamage trigger <Spieler>");
                    return true;
                }
                
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    sender.sendMessage("§cSpieler nicht gefunden!");
                    return true;
                }
                
                plugin.triggerDamageForPlayer(target);
                sender.sendMessage("§aDelayed Damage für " + target.getName() + " ausgelöst!");
                break;

            case "reset":
                if (args.length < 2) {
                    sender.sendMessage("§cVerwendung: /delayeddamage reset <Spieler>");
                    return true;
                }
                
                Player resetTarget = Bukkit.getPlayer(args[1]);
                if (resetTarget == null) {
                    sender.sendMessage("§cSpieler nicht gefunden!");
                    return true;
                }
                
                plugin.resetTimerForPlayer(resetTarget);
                sender.sendMessage("§aTimer und Schaden für " + resetTarget.getName() + " zurückgesetzt!");
                break;

            case "info":
                if (args.length < 2) {
                    sender.sendMessage("§cVerwendung: /delayeddamage info <Spieler>");
                    return true;
                }
                
                Player infoTarget = Bukkit.getPlayer(args[1]);
                if (infoTarget == null) {
                    sender.sendMessage("§cSpieler nicht gefunden!");
                    return true;
                }
                
                PlayerData data = plugin.getPlayerDataMap().get(infoTarget.getUniqueId());
                if (data == null) {
                    sender.sendMessage("§cKeine Daten für " + infoTarget.getName() + " gefunden!");
                    return true;
                }
                
                long totalSeconds = data.getTimerSeconds();
                long hours = totalSeconds / 3600;
                long minutes = (totalSeconds % 3600) / 60;
                long seconds = totalSeconds % 60;
                
                String timeStr = String.format("%dh %02dm %02ds", hours, minutes, seconds);
                
                sender.sendMessage("§7=== Info für " + infoTarget.getName() + " ===");
                sender.sendMessage("§6Timer: §f" + timeStr);
                sender.sendMessage("§cGespeicherter Schaden: §f" + String.format("%.1f", data.getStoredDamage()));
                break;

            default:
                sendHelp(sender);
                break;
        }

        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§7=== DelayedDamage Commands ===");
        sender.sendMessage("§6/delayeddamage trigger <Spieler> §7- Löst den gespeicherten Schaden aus");
        sender.sendMessage("§6/delayeddamage reset <Spieler> §7- Setzt Timer und Schaden zurück");
        sender.sendMessage("§6/delayeddamage info <Spieler> §7- Zeigt Timer und Schaden Info");
    }
}