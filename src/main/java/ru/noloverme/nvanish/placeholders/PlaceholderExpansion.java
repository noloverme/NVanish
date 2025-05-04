package ru.noloverme.nvanish.placeholders;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import ru.noloverme.nvanish.NVanish;
import ru.noloverme.nvanish.manager.VanishManager;

import java.util.logging.Level;

/**
 * Класс для интеграции с PlaceholderAPI
 */
public class PlaceholderExpansion extends me.clip.placeholderapi.expansion.PlaceholderExpansion {
    
    private final NVanish plugin;
    
    public PlaceholderExpansion(NVanish plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public String getIdentifier() {
        return "nvanish";
    }
    
    @Override
    public String getAuthor() {
        return plugin.getDescription().getAuthors().get(0);
    }
    
    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }
    
    @Override
    public boolean persist() {
        return true;
    }
    
    @Override
    public boolean canRegister() {
        return true;
    }
    
    @Override
    public String onPlaceholderRequest(Player player, String identifier) {
        if (player == null) {
            return "";
        }
        
        VanishManager vanishManager = plugin.getVanishManager();
        
        // %nvanish_status% - возвращает статус ваниша игрока (true/false)
        if (identifier.equals("status")) {
            return String.valueOf(vanishManager.isVanished(player));
        }
        
        // %nvanish_status_text% - возвращает статус ваниша игрока (Скрыт/Виден)
        if (identifier.equals("status_text")) {
            String vanishedText = plugin.getPluginConfig().getPlaceholderMessage("vanished");
            String visibleText = plugin.getPluginConfig().getPlaceholderMessage("visible");
            return vanishManager.isVanished(player) ? vanishedText : visibleText;
        }
        
        // %nvanish_count% - возвращает количество скрытых игроков
        if (identifier.equals("count")) {
            return String.valueOf(vanishManager.getVanishedPlayers().size());
        }
        
        // %nvanish_can_see_<player>% - может ли текущий игрок видеть указанного
        if (identifier.startsWith("can_see_")) {
            String targetName = identifier.substring(8);
            Player target = Bukkit.getPlayerExact(targetName);
            
            if (target == null) {
                return "false";
            }
            
            boolean canSee = !vanishManager.isVanished(target) || 
                             player.hasPermission("nvanish.see") || 
                             player.equals(target);
            
            return String.valueOf(canSee);
        }
        
        // %nvanish_list% - список скрытых игроков через запятую
        if (identifier.equals("list")) {
            StringBuilder list = new StringBuilder();
            boolean first = true;
            
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                if (vanishManager.isVanished(onlinePlayer)) {
                    if (!first) {
                        list.append(", ");
                    }
                    list.append(onlinePlayer.getName());
                    first = false;
                }
            }
            
            return list.toString();
        }
        
        // %nvanish_visible_count% - количество видимых игроков (не в ванише)
        if (identifier.equals("visible_count")) {
            int total = Bukkit.getOnlinePlayers().size();
            int vanished = vanishManager.getVanishedPlayers().size();
            return String.valueOf(total - vanished);
        }
        
        return null;
    }
    
    /**
     * Регистрирует расширение в PlaceholderAPI
     * @return true если регистрация успешна, иначе false
     */
    public boolean register() {
        if (!plugin.getPluginConfig().isPlaceholdersEnabled()) {
            plugin.getLogger().log(Level.INFO, "Интеграция с PlaceholderAPI отключена в конфигурации.");
            return false;
        }
        
        if (!Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            plugin.getLogger().log(Level.INFO, "PlaceholderAPI не найден, интеграция отключена.");
            return false;
        }
        
        if (super.register()) {
            plugin.getLogger().log(Level.INFO, "Интеграция с PlaceholderAPI успешно активирована.");
            return true;
        }
        
        return false;
    }
}