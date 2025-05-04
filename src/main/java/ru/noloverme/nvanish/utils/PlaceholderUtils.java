package ru.noloverme.nvanish.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import ru.noloverme.nvanish.NVanish;

import java.util.logging.Level;

/**
 * Утилиты для работы с PlaceholderAPI
 */
public class PlaceholderUtils {

    private final NVanish plugin;
    private static boolean placeholderAPILoaded = false;

    public PlaceholderUtils(NVanish plugin) {
        this.plugin = plugin;
        checkPlaceholderAPI();
    }

    /**
     * Проверяет наличие PlaceholderAPI на сервере
     */
    private void checkPlaceholderAPI() {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            placeholderAPILoaded = true;
            plugin.getLogger().log(Level.INFO, ColorUtils.colorize(plugin.getPluginConfig().getMessage("placeholder_api_found")));
        } else {
            plugin.getLogger().log(Level.INFO, ColorUtils.colorize(plugin.getPluginConfig().getMessage("placeholder_api_not_found")));
        }
    }

    /**
     * Заменяет placeholders в строке для конкретного игрока
     * @param player Игрок
     * @param text Исходная строка с placeholders
     * @return Строка с замененными placeholders
     */
    public String setPlaceholders(Player player, String text) {
        if (player == null || text == null || text.isEmpty()) {
            return text;
        }

        // Если PlaceholderAPI не загружен или отключен в конфигурации
        if (!placeholderAPILoaded || !plugin.getPluginConfig().isPlaceholdersEnabled()) {
            return text;
        }

        try {
            // Используем PlaceholderAPI для замены placeholders
            return me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, text);
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, ColorUtils.colorize(
                    plugin.getPluginConfig().getMessage("placeholder_error").replace("%error%", e.getMessage())));
            return text;
        }
    }

    /**
     * Проверяет, загружен ли PlaceholderAPI
     * @return true если PlaceholderAPI загружен, иначе false
     */
    public static boolean isPlaceholderAPILoaded() {
        return placeholderAPILoaded;
    }
}