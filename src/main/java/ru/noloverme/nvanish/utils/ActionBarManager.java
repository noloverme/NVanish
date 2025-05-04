package ru.noloverme.nvanish.utils;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import ru.noloverme.nvanish.NVanish;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Менеджер для отправки ActionBar сообщений игрокам в ванише
 */
public class ActionBarManager {
    
    private final NVanish plugin;
    private final Map<UUID, BukkitTask> activeTasks = new HashMap<>();
    private final PlaceholderUtils placeholderUtils;
    
    public ActionBarManager(NVanish plugin) {
        this.plugin = plugin;
        this.placeholderUtils = new PlaceholderUtils(plugin);
    }
    
    /**
     * Начинает отправку повторяющихся ActionBar сообщений игроку
     * @param player Игрок
     */
    public void startActionBar(Player player) {
        // Если отправка ActionBar отключена в настройках, ничего не делаем
        if (!plugin.getPluginConfig().isActionbarEnabled()) {
            return;
        }
        
        // Если уже есть активная задача для этого игрока, отменяем её
        stopActionBar(player);
        
        // Создаём новую задачу для отправки ActionBar
        int interval = plugin.getPluginConfig().getActionbarUpdateInterval();
        BukkitTask task = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            if (player.isOnline()) {
                sendActionBar(player);
            } else {
                // Если игрок вышел с сервера, отменяем задачу
                stopActionBar(player);
            }
        }, 0, interval);
        
        // Сохраняем задачу
        activeTasks.put(player.getUniqueId(), task);
    }
    
    /**
     * Останавливает отправку ActionBar сообщений игроку
     * @param player Игрок
     */
    public void stopActionBar(Player player) {
        BukkitTask task = activeTasks.remove(player.getUniqueId());
        if (task != null) {
            task.cancel();
        }
    }
    
    /**
     * Отправляет единичное ActionBar сообщение игроку
     * @param player Игрок
     */
    private void sendActionBar(Player player) {
        if (!player.isOnline()) {
            return;
        }
        
        String message = plugin.getPluginConfig().getActionbarMessage();
        
        // Обработка системного плейсхолдера %prefix%
        if (message.contains("%prefix%")) {
            String prefix = plugin.getPluginConfig().getMessage("prefix");
            message = message.replace("%prefix%", prefix);
        }
        
        // Обработка PlaceholderAPI
        if (plugin.getPluginConfig().isPlaceholdersEnabled()) {
            message = placeholderUtils.setPlaceholders(player, message);
        }
        
        // Обработка цветов
        Component component = ColorUtils.colorizeComponent(message);
        
        // Отправка ActionBar
        player.sendActionBar(component);
    }
    
    /**
     * Останавливает все активные задачи
     */
    public void stopAll() {
        for (BukkitTask task : activeTasks.values()) {
            task.cancel();
        }
        activeTasks.clear();
    }
}