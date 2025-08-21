package com.noloverme.nvanish.listeners;

import com.noloverme.nvanish.NVanish;
import com.noloverme.nvanish.settings.ConfigManager;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class VanishListener implements Listener {

    private final NVanish plugin;
    private final ConfigManager configManager;

    public VanishListener(NVanish plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player newPlayer = event.getPlayer();
        if (plugin.isVanished(newPlayer) && configManager.getSetting("silent_join_quit")) {
            event.setJoinMessage(null);
        }
        if (!newPlayer.hasPermission("nvanish.see_vanished")) {
            for (Player vanishedPlayer : plugin.getVanishedPlayers()) {
                newPlayer.hidePlayer(plugin, vanishedPlayer);
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (plugin.isVanished(player) && configManager.getSetting("silent_join_quit")) {
            event.setQuitMessage(null);
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!configManager.getSetting("disable_on_hit")) {
            return;
        }
        if (event.getDamager().getType() == EntityType.PLAYER) {
            Player damager = (Player) event.getDamager();
            if (plugin.isVanished(damager) && !damager.hasPermission("nvanish.bypass.hit")) {
                event.setCancelled(true);
                damager.sendMessage(configManager.getMessage("action_blocked"));
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!configManager.getSetting("disable_on_interact")) {
            return;
        }
        Player player = event.getPlayer();
        if (plugin.isVanished(player) && !player.hasPermission("nvanish.bypass.interact")) {
            event.setCancelled(true);
            player.sendMessage(configManager.getMessage("action_blocked"));
        }
    }

    @EventHandler
    public void onEntityTarget(EntityTargetLivingEntityEvent event) {
        if (!configManager.getSetting("mob_invisible")) {
            return;
        }
        if (event.getTarget() instanceof Player) {
            Player target = (Player) event.getTarget();
            if (plugin.isVanished(target)) {
                event.setCancelled(true);
            }
        }
    }
}