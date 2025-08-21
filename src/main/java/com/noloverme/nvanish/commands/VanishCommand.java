package com.noloverme.nvanish.commands;

import com.noloverme.nvanish.settings.ConfigManager;
import com.noloverme.nvanish.NVanish;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.Bukkit;
import java.util.Set;

public class VanishCommand implements CommandExecutor {

    private final NVanish plugin;
    private final ConfigManager configManager;

    public VanishCommand(NVanish plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(configManager.getMessage("only_players"));
                return true;
            }
            Player player = (Player) sender;
            if (!player.hasPermission("nvanish.use")) {
                player.sendMessage(configManager.getMessage("no_permission"));
                return true;
            }
            plugin.toggleVanish(player);
        } else {
            String subCommand = args[0].toLowerCase();
            if (subCommand.equals("list")) {
                if (!sender.hasPermission("nvanish.list")) {
                    sender.sendMessage(configManager.getMessage("no_permission"));
                    return true;
                }
                Set<Player> vanishedPlayers = plugin.getVanishedPlayers();
                sender.sendMessage(configManager.getMessage("vanished_list_header").replace("%count%", String.valueOf(vanishedPlayers.size())));
                if (!vanishedPlayers.isEmpty()) {
                    for (Player vanishedPlayer : vanishedPlayers) {
                        sender.sendMessage(configManager.getMessage("vanished_list_entry").replace("%player%", vanishedPlayer.getName()));
                    }
                }
            } else if (subCommand.equals("reload")) {
                if (!sender.hasPermission("nvanish.reload")) {
                    sender.sendMessage(configManager.getMessage("no_permission"));
                    return true;
                }
                plugin.reloadPlugin();
                sender.sendMessage(configManager.getMessage("config_reloaded"));
            } else {
                if (!sender.hasPermission("nvanish.use_others")) {
                    sender.sendMessage(configManager.getMessage("no_permission"));
                    return true;
                }
                Player target = Bukkit.getPlayer(args[0]);
                if (target != null) {
                    plugin.toggleVanish(target);
                    sender.sendMessage(configManager.getMessage(plugin.isVanished(target) ? "vanish_other_on" : "vanish_other_off").replace("%player%", target.getName()));
                } else {
                    sender.sendMessage(configManager.getMessage("player_not_found").replace("%player%", args[0]));
                }
            }
        }
        return true;
    }
}
