package ru.noloverme.nvanish.manager;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import ru.noloverme.nvanish.NVanish;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class VanishManager {

    private final NVanish plugin;
    private final Set<UUID> vanishedPlayers;

    public VanishManager(NVanish plugin) {
        this.plugin = plugin;
        this.vanishedPlayers = new HashSet<>();
        
        // Загрузка игроков в ванише при старте
        loadVanishedPlayers();
    }

    /**
     * Загрузка всех игроков, находящихся в ванише
     */
    private void loadVanishedPlayers() {
        vanishedPlayers.clear();
        vanishedPlayers.addAll(plugin.getStorageManager().getAllVanishedPlayers());
    }

    /**
     * Перезагрузка менеджера
     */
    public void reload() {
        // Сохраняем текущее состояние
        Set<UUID> oldVanished = new HashSet<>(vanishedPlayers);
        
        // Загружаем новое состояние
        loadVanishedPlayers();
        
        // Обновляем состояние для онлайн игроков
        for (Player player : Bukkit.getOnlinePlayers()) {
            // Если игрок был в ванише, но теперь нет - показываем его
            if (oldVanished.contains(player.getUniqueId()) && !vanishedPlayers.contains(player.getUniqueId())) {
                showPlayer(player);
            }
            // Если игрок не был в ванише, но теперь должен быть - скрываем его
            else if (!oldVanished.contains(player.getUniqueId()) && vanishedPlayers.contains(player.getUniqueId())) {
                hidePlayer(player);
            }
            
            // Обновляем видимость всех игроков
            updateVisibility(player);
        }
    }

    /**
     * Скрывает игрока от других игроков
     * @param player Игрок, которого нужно скрыть
     */
    public void hidePlayer(Player player) {
        if (vanishedPlayers.contains(player.getUniqueId())) {
            return; // Игрок уже скрыт
        }
        
        vanishedPlayers.add(player.getUniqueId());
        plugin.getStorageManager().setVanished(player.getUniqueId(), true);
        
        // Скрываем игрока от всех, кто его не должен видеть
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (!onlinePlayer.hasPermission("nvanish.see")) {
                onlinePlayer.hidePlayer(plugin, player);
            }
        }
        
        // Применяем эффекты, если они включены в конфиге
        applyVanishEffects(player);
        
        // Применяем интеграции с голосовыми чатами
        plugin.getIntegrationManager().hideVoice(player);
        plugin.getIntegrationManager().blockHearing(player, plugin.getPluginConfig().isPlasmoVoiceBlockHearing());
        
        // Запускаем ActionBar уведомление
        plugin.getActionBarManager().startActionBar(player);
    }

    /**
     * Применяет эффекты ваниша к игроку
     * @param player Игрок для применения эффектов
     */
    private void applyVanishEffects(Player player) {
        // Применяем полет, если это настроено в конфиге
        if (plugin.getPluginConfig().isVanishEffect("flight")) {
            player.setAllowFlight(true);
        }
        
        // Применяем ночное зрение, если это настроено в конфиге
        if (plugin.getPluginConfig().isVanishEffect("night_vision")) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 0, false, false));
        }
        
        // Устанавливаем режим игры, если это настроено в конфиге
        String gamemode = plugin.getPluginConfig().getVanishGamemode();
        if (!gamemode.isEmpty()) {
            switch (gamemode.toLowerCase()) {
                case "survival":
                case "0":
                    player.setGameMode(GameMode.SURVIVAL);
                    break;
                case "creative":
                case "1":
                    player.setGameMode(GameMode.CREATIVE);
                    break;
                case "adventure":
                case "2":
                    player.setGameMode(GameMode.ADVENTURE);
                    break;
                case "spectator":
                case "3":
                    player.setGameMode(GameMode.SPECTATOR);
                    break;
            }
        }
    }

    /**
     * Делает игрока видимым для всех
     * @param player Игрок, которого нужно показать
     */
    public void showPlayer(Player player) {
        if (!vanishedPlayers.contains(player.getUniqueId())) {
            return; // Игрок уже видим
        }
        
        vanishedPlayers.remove(player.getUniqueId());
        plugin.getStorageManager().setVanished(player.getUniqueId(), false);
        
        // Показываем игрока всем
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            onlinePlayer.showPlayer(plugin, player);
        }
        
        // Удаляем эффекты ваниша
        removeVanishEffects(player);
        
        // Выключаем интеграции с голосовыми чатами
        plugin.getIntegrationManager().showVoice(player);
        plugin.getIntegrationManager().blockHearing(player, false);
        
        // Останавливаем ActionBar уведомление
        plugin.getActionBarManager().stopActionBar(player);
    }

    /**
     * Удаляет эффекты ваниша у игрока
     * @param player Игрок для удаления эффектов
     */
    private void removeVanishEffects(Player player) {
        // Удаляем полет, если нужно
        if (plugin.getPluginConfig().isVanishEffect("flight") && !player.hasPermission("nvanish.flight")) {
            if (player.getGameMode() != GameMode.CREATIVE && player.getGameMode() != GameMode.SPECTATOR) {
                player.setAllowFlight(false);
                player.setFlying(false);
            }
        }
        
        // Удаляем ночное зрение
        if (plugin.getPluginConfig().isVanishEffect("night_vision")) {
            player.removePotionEffect(PotionEffectType.NIGHT_VISION);
        }
    }

    /**
     * Обновляет видимость игроков для указанного игрока
     * @param player Игрок, для которого нужно обновить видимость
     */
    public void updateVisibility(Player player) {
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (vanishedPlayers.contains(onlinePlayer.getUniqueId())) {
                if (player.hasPermission("nvanish.see")) {
                    player.showPlayer(plugin, onlinePlayer);
                } else {
                    player.hidePlayer(plugin, onlinePlayer);
                }
            } else {
                player.showPlayer(plugin, onlinePlayer);
            }
        }
    }

    /**
     * Проверяет, находится ли игрок в ванише
     * @param player Игрок для проверки
     * @return true, если игрок в ванише, иначе false
     */
    public boolean isVanished(Player player) {
        return vanishedPlayers.contains(player.getUniqueId());
    }

    /**
     * Получает список всех UUID игроков в ванише
     * @return Set с UUID скрытых игроков
     */
    public Set<UUID> getVanishedPlayers() {
        return new HashSet<>(vanishedPlayers);
    }
}
