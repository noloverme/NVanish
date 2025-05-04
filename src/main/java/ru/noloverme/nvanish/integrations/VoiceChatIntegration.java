package ru.noloverme.nvanish.integrations;

import org.bukkit.entity.Player;

/**
 * Интерфейс для интеграции с голосовыми чатами
 */
public interface VoiceChatIntegration {
    
    /**
     * Проверяет, доступна ли интеграция
     * @return true если интеграция доступна, иначе false
     */
    boolean isAvailable();
    
    /**
     * Инициализирует интеграцию
     * @return true если инициализация успешна, иначе false
     */
    boolean initialize();
    
    /**
     * Выключает интеграцию
     */
    void shutdown();
    
    /**
     * Скрывает голос игрока для других игроков
     * @param player Игрок, чей голос нужно скрыть
     */
    void hideVoice(Player player);
    
    /**
     * Показывает голос игрока другим игрокам
     * @param player Игрок, чей голос нужно показать
     */
    void showVoice(Player player);
    
    /**
     * Блокирует слышимость других игроков для указанного игрока
     * @param player Игрок, для которого нужно заблокировать слышимость
     * @param block true для блокировки, false для разблокировки
     */
    void blockHearing(Player player, boolean block);
}