package ru.noloverme.nvanish.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import ru.noloverme.nvanish.NVanish;
import ru.noloverme.nvanish.reports.Report.ReportCategory;
import ru.noloverme.nvanish.utils.ColorUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Команда для отправки отчетов
 */
public class ReportCommand implements CommandExecutor, TabCompleter {

    private final NVanish plugin;

    public ReportCommand(NVanish plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ColorUtils.colorize(plugin.getPluginConfig().getMessage("player_only")));
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("nvanish.report")) {
            player.sendMessage(ColorUtils.colorize(plugin.getPluginConfig().getMessage("no_permission")));
            return true;
        }

        // Проверка на наличие аргументов
        if (args.length < 2) {
            player.sendMessage(ColorUtils.colorize(plugin.getPluginConfig().getMessage("report_usage")
                    .replace("%cmd%", label)));
            return true;
        }

        // Определение категории отчета
        ReportCategory category;
        try {
            category = ReportCategory.valueOf(args[0].toUpperCase());
        } catch (IllegalArgumentException e) {
            player.sendMessage(ColorUtils.colorize(plugin.getPluginConfig().getMessage("invalid_report_category")));
            return true;
        }

        // Получение текста отчета (объединяем все аргументы, кроме первого)
        String message = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

        // Проверка на кулдаун
        if (plugin.getReportManager().isOnCooldown(player.getUniqueId())) {
            int remainingTime = plugin.getReportManager().getRemainingCooldown(player.getUniqueId());
            player.sendMessage(ColorUtils.colorize(plugin.getPluginConfig().getMessage("report_cooldown")
                    .replace("%time%", String.valueOf(remainingTime))));
            return true;
        }

        // Отправка отчета
        if (plugin.getReportManager().createReport(player, message, category)) {
            player.sendMessage(ColorUtils.colorize(plugin.getPluginConfig().getMessage("report_sent")));
        } else {
            player.sendMessage(ColorUtils.colorize(plugin.getPluginConfig().getMessage("report_failed")));
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.stream(ReportCategory.values())
                    .map(ReportCategory::name)
                    .map(String::toLowerCase)
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}