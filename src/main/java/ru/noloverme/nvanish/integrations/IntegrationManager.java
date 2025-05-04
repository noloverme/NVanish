package ru.noloverme.nvanish.integrations;

import org.bukkit.entity.Player;
import ru.noloverme.nvanish.NVanish;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * Менеджер для управления интеграциями с другими плагинами
 */
public class IntegrationManager {
    
    private final NVanish plugin;
    private final List<VoiceChatIntegration> voiceChatIntegrations;
    
    public IntegrationManager(NVanish plugin) {
        this.plugin = plugin;
        this.voiceChatIntegrations = new ArrayList<>();
    }
    
    /**
     * Инициализирует все доступные интеграции
     */
    public void initialize() {
        // Регистрируем интеграции с голосовыми чатами
        registerVoiceChatIntegrations();
        
        // Инициализируем зарегистрированные интеграции
        initializeVoiceChatIntegrations();
    }
    
    /**
     * Регистрирует все поддерживаемые интеграции с голосовыми чатами
     */
    private void registerVoiceChatIntegrations() {
        // Добавляем PlasmoVoice
        voiceChatIntegrations.add(new PlasmoVoiceIntegration(plugin));
        
        // Добавляем SimpleVoice
        voiceChatIntegrations.add(new SimpleVoiceIntegration(plugin));
        
        // В будущем здесь можно добавить другие интеграции
    }
    
    /**
     * Инициализирует зарегистрированные интеграции с голосовыми чатами
     */
    private void initializeVoiceChatIntegrations() {
        int initializedCount = 0;
        
        for (VoiceChatIntegration integration : voiceChatIntegrations) {
            try {
                if (integration.initialize()) {
                    initializedCount++;
                }
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, 
                    "Ошибка при инициализации интеграции с голосовым чатом: " + e.getMessage());
            }
        }
        
        if (initializedCount > 0) {
            plugin.getLogger().log(Level.INFO, 
                "Успешно инициализировано интеграций с голосовыми чатами: " + initializedCount);
        }
    }
    
    /**
     * Отключает все интеграции
     */
    public void shutdown() {
        // Отключаем интеграции с голосовыми чатами
        for (VoiceChatIntegration integration : voiceChatIntegrations) {
            try {
                integration.shutdown();
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, 
                    "Ошибка при отключении интеграции с голосовым чатом: " + e.getMessage());
            }
        }
    }
    
    /**
     * Скрывает голос игрока во всех активных интеграциях голосовых чатов
     * @param player Игрок, чей голос нужно скрыть
     */
    public void hideVoice(Player player) {
        for (VoiceChatIntegration integration : voiceChatIntegrations) {
            if (integration.isAvailable()) {
                integration.hideVoice(player);
            }
        }
    }
    
    /**
     * Показывает голос игрока во всех активных интеграциях голосовых чатов
     * @param player Игрок, чей голос нужно показать
     */
    public void showVoice(Player player) {
        for (VoiceChatIntegration integration : voiceChatIntegrations) {
            if (integration.isAvailable()) {
                integration.showVoice(player);
            }
        }
    }
    
    /**
     * Блокирует или разблокирует слышимость других игроков для указанного игрока
     * @param player Игрок, для которого нужно изменить слышимость
     * @param block true для блокировки, false для разблокировки
     */
    public void blockHearing(Player player, boolean block) {
        for (VoiceChatIntegration integration : voiceChatIntegrations) {
            if (integration.isAvailable()) {
                integration.blockHearing(player, block);
            }
        }
    }
}