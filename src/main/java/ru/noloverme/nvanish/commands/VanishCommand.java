package ru.noloverme.nvanish.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import ru.noloverme.nvanish.NVanish;
import ru.noloverme.nvanish.logging.ActionLogger.ActionType;
import ru.noloverme.nvanish.utils.ColorUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class VanishCommand implements CommandExecutor, TabCompleter {

    private final NVanish plugin;

    public VanishCommand(NVanish plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            // Если команда без аргументов, то показываем помощь
            if (sender.hasPermission("nvanish.use")) {
                String helpMessage = plugin.getPluginConfig().getMessage("help")
                        .replace("%cmd%", label)
                        .replace("%desc%", "управление видимостью игроков");
                sender.sendMessage(ColorUtils.colorize(helpMessage));
            } else {
                sender.sendMessage(ColorUtils.colorize(plugin.getPluginConfig().getMessage("no_permission")));
            }
            return true;
        }

        // Проверка на reload
        if (args[0].equalsIgnoreCase("reload")) {
            if (sender.hasPermission("nvanish.admin")) {
                plugin.reload();
                sender.sendMessage(ColorUtils.colorize(plugin.getPluginConfig().getMessage("plugin_reloaded")));
                
                // Логирование перезагрузки плагина
                if (sender instanceof Player) {
                    plugin.getActionLogger().log(ActionType.PLUGIN_RELOADED, (Player) sender);
                }
            } else {
                sender.sendMessage(ColorUtils.colorize(plugin.getPluginConfig().getMessage("no_permission")));
            }
            return true;
        }

        // Проверка на выключение (off) или включение (on) ваниша
        if (args[0].equalsIgnoreCase("on") || args[0].equalsIgnoreCase("off")) {
            boolean vanishState = args[0].equalsIgnoreCase("on");
            
            // Если указан ник игрока, то меняем состояние для этого игрока
            if (args.length > 1) {
                if (!sender.hasPermission("nvanish.other")) {
                    sender.sendMessage(ColorUtils.colorize(plugin.getPluginConfig().getMessage("no_permission")));
                    return true;
                }
                
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    sender.sendMessage(ColorUtils.colorize(plugin.getPluginConfig().getMessage("no_player")));
                    return true;
                }
                
                if (vanishState) {
                    plugin.getVanishManager().hidePlayer(target);
                    sender.sendMessage(ColorUtils.colorize(plugin.getPluginConfig().getMessage("vanish_enabled_other").replace("%player%", target.getName())));
                    target.sendMessage(ColorUtils.colorize(plugin.getPluginConfig().getMessage("vanish_enabled")));
                    
                    // Логирование включения ваниша для другого игрока
                    if (sender instanceof Player) {
                        plugin.getActionLogger().log(ActionType.VANISH_OTHER_ENABLED, (Player) sender, target);
                    }
                } else {
                    plugin.getVanishManager().showPlayer(target);
                    sender.sendMessage(ColorUtils.colorize(plugin.getPluginConfig().getMessage("vanish_disabled_other").replace("%player%", target.getName())));
                    target.sendMessage(ColorUtils.colorize(plugin.getPluginConfig().getMessage("vanish_disabled")));
                    
                    // Логирование выключения ваниша для другого игрока
                    if (sender instanceof Player) {
                        plugin.getActionLogger().log(ActionType.VANISH_OTHER_DISABLED, (Player) sender, target);
                    }
                }
            } else {
                // Если ник не указан, то меняем состояние для отправителя команды
                if (!(sender instanceof Player)) {
                    sender.sendMessage(ColorUtils.colorize(plugin.getPluginConfig().getMessage("player_only")));
                    return true;
                }
                
                if (!sender.hasPermission("nvanish.use")) {
                    sender.sendMessage(ColorUtils.colorize(plugin.getPluginConfig().getMessage("no_permission")));
                    return true;
                }
                
                Player player = (Player) sender;
                if (vanishState) {
                    plugin.getVanishManager().hidePlayer(player);
                    player.sendMessage(ColorUtils.colorize(plugin.getPluginConfig().getMessage("vanish_enabled")));
                    
                    // Логирование включения ваниша для себя
                    plugin.getActionLogger().log(ActionType.VANISH_ENABLED, player);
                } else {
                    plugin.getVanishManager().showPlayer(player);
                    player.sendMessage(ColorUtils.colorize(plugin.getPluginConfig().getMessage("vanish_disabled")));
                    
                    // Логирование выключения ваниша для себя
                    plugin.getActionLogger().log(ActionType.VANISH_DISABLED, player);
                }
            }
            return true;
        }

        // Если команда без аргументов для игрока, переключаем состояние
        if (sender instanceof Player) {
            Player player = (Player) sender;
            
            if (!player.hasPermission("nvanish.use")) {
                player.sendMessage(ColorUtils.colorize(plugin.getPluginConfig().getMessage("no_permission")));
                return true;
            }
            
            // Переключаем режим ваниша
            if (plugin.getVanishManager().isVanished(player)) {
                plugin.getVanishManager().showPlayer(player);
                player.sendMessage(ColorUtils.colorize(plugin.getPluginConfig().getMessage("vanish_disabled")));
                
                // Логирование выключения ваниша для себя
                plugin.getActionLogger().log(ActionType.VANISH_DISABLED, player);
            } else {
                plugin.getVanishManager().hidePlayer(player);
                player.sendMessage(ColorUtils.colorize(plugin.getPluginConfig().getMessage("vanish_enabled")));
                
                // Логирование включения ваниша для себя
                plugin.getActionLogger().log(ActionType.VANISH_ENABLED, player);
            }
            return true;
        } else {
            sender.sendMessage(ColorUtils.colorize(plugin.getPluginConfig().getMessage("player_only")));
        }
        
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            if (sender.hasPermission("nvanish.use")) {
                completions.add("on");
                completions.add("off");
            }
            if (sender.hasPermission("nvanish.admin")) {
                completions.add("reload");
            }
            return completions.stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        } else if (args.length == 2) {
            if ((args[0].equalsIgnoreCase("on") || args[0].equalsIgnoreCase("off")) 
                    && sender.hasPermission("nvanish.other")) {
                return Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }
        }
        return new ArrayList<>();
    }
}
