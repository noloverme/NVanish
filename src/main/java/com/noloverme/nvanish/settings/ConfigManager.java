package com.noloverme.nvanish.settings;

import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConfigManager {

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");

    private final JavaPlugin plugin;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        plugin.saveDefaultConfig();
    }

    public String getMessage(String path) {
        String message = plugin.getConfig().getString("messages." + path, "");
        return colorize(message);
    }

    public boolean getSetting(String path) {
        return plugin.getConfig().getBoolean("settings." + path, false);
    }

    public String getVanishActionbarMessage() {
        return colorize(plugin.getConfig().getString("messages.actionbar_message", ""));
    }

    public boolean getVanishActionbarEnabled() {
        return plugin.getConfig().getBoolean("settings.actionbar_enabled", true);
    }

    private String colorize(String message) {
        if (message == null) {
            return "";
        }
        Matcher matcher = HEX_PATTERN.matcher(message);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            String hexColor = matcher.group(1);
            String replacement = net.md_5.bungee.api.ChatColor.of("#" + hexColor).toString();
            matcher.appendReplacement(sb, replacement);
        }
        matcher.appendTail(sb);
        return ChatColor.translateAlternateColorCodes('&', sb.toString());
    }
}
