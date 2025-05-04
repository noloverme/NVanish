package ru.noloverme.nvanish.integrations;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import ru.noloverme.nvanish.NVanish;

import java.util.logging.Level;

/**
 * Интеграция с плагином SimpleVoice
 */
public class SimpleVoiceIntegration implements VoiceChatIntegration {
    
    private final NVanish plugin;
    private boolean available = false;
    
    public SimpleVoiceIntegration(NVanish plugin) {
        this.plugin = plugin;
        this.available = Bukkit.getPluginManager().isPluginEnabled("SimpleVoice");
    }
    
    @Override
    public boolean isAvailable() {
        return available && plugin.getPluginConfig().isSimpleVoiceEnabled();
    }
    
    @Override
    public boolean initialize() {
        if (!available) {
            plugin.getLogger().log(Level.INFO, "SimpleVoice не найден, интеграция отключена.");
            return false;
        }
        
        if (!plugin.getPluginConfig().isSimpleVoiceEnabled()) {
            plugin.getLogger().log(Level.INFO, "Интеграция с SimpleVoice отключена в конфигурации.");
            return false;
        }
        
        plugin.getLogger().log(Level.INFO, "Интеграция с SimpleVoice успешно активирована.");
        return true;
    }
    
    @Override
    public void shutdown() {
        // Нет необходимости в дополнительных действиях при отключении
    }
    
    @Override
    public void hideVoice(Player player) {
        if (!isAvailable() || !plugin.getPluginConfig().isSimpleVoiceHideVoice()) {
            return;
        }
        
        try {
            // Тут должен быть код для вызова API SimpleVoice для скрытия голоса
            // Например: SimpleVoiceAPI.mutePlayer(player.getUniqueId());
            
            // Поскольку у нас нет прямого доступа к API SimpleVoice,
            // мы будем использовать серверные команды или свою логику
            String command = "sv mute " + player.getName();
            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), command);
            
            plugin.getLogger().log(Level.FINE, "Голос игрока " + player.getName() + " скрыт в SimpleVoice.");
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Ошибка при скрытии голоса в SimpleVoice: " + e.getMessage());
        }
    }
    
    @Override
    public void showVoice(Player player) {
        if (!isAvailable() || !plugin.getPluginConfig().isSimpleVoiceHideVoice()) {
            return;
        }
        
        try {
            // Тут должен быть код для вызова API SimpleVoice для показа голоса
            // Например: SimpleVoiceAPI.unmutePlayer(player.getUniqueId());
            
            // Поскольку у нас нет прямого доступа к API SimpleVoice,
            // мы будем использовать серверные команды или свою логику
            String command = "sv unmute " + player.getName();
            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), command);
            
            plugin.getLogger().log(Level.FINE, "Голос игрока " + player.getName() + " показан в SimpleVoice.");
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Ошибка при показе голоса в SimpleVoice: " + e.getMessage());
        }
    }
    
    @Override
    public void blockHearing(Player player, boolean block) {
        // SimpleVoice не имеет функции блокировки слышимости для игрока
        // Этот метод оставлен пустым, так как данная функциональность не поддерживается SimpleVoice
    }
}