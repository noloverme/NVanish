package ru.noloverme.nvanish.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import ru.noloverme.nvanish.NVanish;
import ru.noloverme.nvanish.utils.ColorUtils;

public class PlayerListener implements Listener {

    private final NVanish plugin;

    public PlayerListener(NVanish plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // Скрыть игроков в ванише для этого игрока
        plugin.getVanishManager().updateVisibility(player);
        
        // Отключить сообщение о входе для скрытых игроков, если это настроено в конфиге
        if (plugin.getVanishManager().isVanished(player) && plugin.getPluginConfig().isHideJoinMessages()) {
            event.setJoinMessage(null);
        }
        
        // Проверка прав администратора и оповещение об устаревшей конфигурации
        if (player.hasPermission("nvanish.admin") && !plugin.getPluginConfig().isConfigUpToDate()) {
            String configVersion = plugin.getPluginConfig().getConfigVersion();
            String pluginVersion = plugin.getDescription().getVersion();
            
            // Задержка, чтобы сообщение не затерялось среди других сообщений при входе
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                String message = ColorUtils.colorize(
                    plugin.getPluginConfig().getMessage("config_outdated")
                    .replace("%current%", configVersion != null ? configVersion : "неизвестно")
                    .replace("%latest%", pluginVersion)
                );
                player.sendMessage(ColorUtils.colorize(plugin.getPluginConfig().getMessage("prefix") + message));
            }, 20L); // Задержка в 1 секунду
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        
        // Отключить сообщение о выходе для скрытых игроков, если это настроено в конфиге
        if (plugin.getVanishManager().isVanished(player) && plugin.getPluginConfig().isHideQuitMessages()) {
            event.setQuitMessage(null);
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        
        // Отключить сообщение о смерти для скрытых игроков, если это настроено в конфиге
        if (plugin.getVanishManager().isVanished(player) && plugin.getPluginConfig().isHideDeathMessages()) {
            event.setDeathMessage(null);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        
        // Отключить чат для скрытых игроков, если это настроено в конфиге
        if (plugin.getVanishManager().isVanished(player) && 
                plugin.getPluginConfig().isVanishRestriction("disable_chat")) {
            event.setCancelled(true);
            player.sendMessage(ColorUtils.colorize(plugin.getPluginConfig().getMessage("chat_blocked")));
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        
        // Отключить взаимодействие с блоками для скрытых игроков, если это настроено в конфиге
        if (plugin.getVanishManager().isVanished(player) && 
                plugin.getPluginConfig().isVanishRestriction("disable_block_interaction")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            Player damager = (Player) event.getDamager();
            
            // Отключить нанесение урона для скрытых игроков, если это настроено в конфиге
            if (plugin.getVanishManager().isVanished(damager) && 
                    plugin.getPluginConfig().isVanishRestriction("disable_damage")) {
                event.setCancelled(true);
            }
        }
        
        if (event.getEntity() instanceof Player) {
            Player victim = (Player) event.getEntity();
            
            // Отключить получение урона для скрытых игроков, если это настроено в конфиге
            if (plugin.getVanishManager().isVanished(victim) && 
                    plugin.getPluginConfig().isVanishRestriction("invulnerable")) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            
            // Отключить изменение голода для скрытых игроков, если это настроено в конфиге
            if (plugin.getVanishManager().isVanished(player) && 
                    plugin.getPluginConfig().isVanishRestriction("disable_hunger")) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerPickupItem(EntityPickupItemEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            
            // Отключить подбор предметов для скрытых игроков, если это настроено в конфиге
            if (plugin.getVanishManager().isVanished(player) && 
                    plugin.getPluginConfig().isVanishRestriction("disable_item_pickup")) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onEntityTarget(EntityTargetEvent event) {
        if (event.getTarget() instanceof Player) {
            Player player = (Player) event.getTarget();
            
            // Отключить атаку мобами скрытых игроков
            if (plugin.getVanishManager().isVanished(player)) {
                event.setCancelled(true);
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        
        // Отключить выбрасывание предметов для скрытых игроков, если это настроено в конфиге
        if (plugin.getVanishManager().isVanished(player) && 
                plugin.getPluginConfig().isVanishRestriction("disable_item_drop")) {
            event.setCancelled(true);
            player.sendMessage(ColorUtils.colorize(plugin.getPluginConfig().getMessage("item_drop_blocked")));
        }
    }
}
