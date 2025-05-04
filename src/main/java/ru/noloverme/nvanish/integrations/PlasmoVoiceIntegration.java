package ru.noloverme.nvanish.integrations;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import ru.noloverme.nvanish.NVanish;

import java.util.logging.Level;

/**
 * Интеграция с плагином PlasmoVoice
 */
public class PlasmoVoiceIntegration implements VoiceChatIntegration {
    
    private final NVanish plugin;
    private boolean available = false;
    
    public PlasmoVoiceIntegration(NVanish plugin) {
        this.plugin = plugin;
        this.available = Bukkit.getPluginManager().isPluginEnabled("PlasmoVoice");
    }
    
    @Override
    public boolean isAvailable() {
        return available && plugin.getPluginConfig().isPlasmoVoiceEnabled();
    }
    
    @Override
    public boolean initialize() {
        if (!available) {
            plugin.getLogger().log(Level.INFO, "PlasmoVoice не найден, интеграция отключена.");
            return false;
        }
        
        if (!plugin.getPluginConfig().isPlasmoVoiceEnabled()) {
            plugin.getLogger().log(Level.INFO, "Интеграция с PlasmoVoice отключена в конфигурации.");
            return false;
        }
        
        plugin.getLogger().log(Level.INFO, "Интеграция с PlasmoVoice успешно активирована.");
        return true;
    }
    
    @Override
    public void shutdown() {
        // Нет необходимости в дополнительных действиях при отключении
    }
    
    @Override
    public void hideVoice(Player player) {
        if (!isAvailable() || !plugin.getPluginConfig().isPlasmoVoiceHideVoice()) {
            return;
        }
        
        try {
            // Тут должен быть код для вызова API PlasmoVoice для скрытия голоса
            // Например: PlasmoVoiceAPI.mutePlayer(player.getUniqueId());
            
            // Поскольку у нас нет прямого доступа к API PlasmoVoice,
            // мы будем использовать серверные команды
            String command = "voicemute " + player.getName();
            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), command);
            
            plugin.getLogger().log(Level.FINE, "Голос игрока " + player.getName() + " скрыт в PlasmoVoice.");
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Ошибка при скрытии голоса в PlasmoVoice: " + e.getMessage());
        }
    }
    
    @Override
    public void showVoice(Player player) {
        if (!isAvailable() || !plugin.getPluginConfig().isPlasmoVoiceHideVoice()) {
            return;
        }
        
        try {
            // Тут должен быть код для вызова API PlasmoVoice для показа голоса
            // Например: PlasmoVoiceAPI.unmutePlayer(player.getUniqueId());
            
            // Поскольку у нас нет прямого доступа к API PlasmoVoice,
            // мы будем использовать серверные команды
            String command = "voiceunmute " + player.getName();
            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), command);
            
            plugin.getLogger().log(Level.FINE, "Голос игрока " + player.getName() + " показан в PlasmoVoice.");
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Ошибка при показе голоса в PlasmoVoice: " + e.getMessage());
        }
    }
    
    @Override
    public void blockHearing(Player player, boolean block) {
        if (!isAvailable() || !plugin.getPluginConfig().isPlasmoVoiceBlockHearing()) {
            return;
        }
        
        try {
            // Тут должен быть код для вызова API PlasmoVoice для блокировки слышимости
            // Например: PlasmoVoiceAPI.setHearing(player.getUniqueId(), !block);
            
            // Поскольку у нас нет прямого доступа к API PlasmoVoice,
            // мы будем использовать серверные команды
            String command = block ? "voicedeafen " + player.getName() : "voiceundeafen " + player.getName();
            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), command);
            
            plugin.getLogger().log(Level.FINE, 
                "Слышимость " + (block ? "заблокирована" : "разблокирована") + 
                " для игрока " + player.getName() + " в PlasmoVoice.");
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Ошибка при изменении слышимости в PlasmoVoice: " + e.getMessage());
        }
    }
}