package ru.noloverme.nvanish.reports;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

/**
 * Класс, представляющий отчет игрока
 */
public class Report {

    /**
     * Категории отчетов
     */
    public enum ReportCategory {
        BUG("Ошибка"),
        SUGGESTION("Предложение"),
        OTHER("Другое");

        private final String description;

        ReportCategory(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
    
    private final UUID id;
    private final String playerName;
    private final UUID playerUUID;
    private final String message;
    private final ReportCategory category;
    private final long timestamp;
    
    /**
     * Создает новый отчет
     * @param playerName Имя игрока
     * @param playerUUID UUID игрока
     * @param message Сообщение отчета
     * @param category Категория отчета
     */
    public Report(String playerName, UUID playerUUID, String message, ReportCategory category) {
        this.id = UUID.randomUUID();
        this.playerName = playerName;
        this.playerUUID = playerUUID;
        this.message = message;
        this.category = category;
        this.timestamp = System.currentTimeMillis();
    }
    
    /**
     * Получает уникальный идентификатор отчета
     * @return UUID отчета
     */
    public UUID getId() {
        return id;
    }
    
    /**
     * Получает имя игрока, отправившего отчет
     * @return Имя игрока
     */
    public String getPlayerName() {
        return playerName;
    }
    
    /**
     * Получает UUID игрока, отправившего отчет
     * @return UUID игрока
     */
    public UUID getPlayerUUID() {
        return playerUUID;
    }
    
    /**
     * Получает сообщение отчета
     * @return Сообщение отчета
     */
    public String getMessage() {
        return message;
    }
    
    /**
     * Получает категорию отчета
     * @return Категория отчета
     */
    public ReportCategory getCategory() {
        return category;
    }
    
    /**
     * Получает время создания отчета в миллисекундах
     * @return Время создания
     */
    public long getTimestamp() {
        return timestamp;
    }
    
    /**
     * Получает форматированную дату и время создания отчета
     * @return Строка с датой и временем
     */
    public String getFormattedTimestamp() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        return dateFormat.format(new Date(timestamp));
    }
    
    @Override
    public String toString() {
        return "Отчет ID: " + id.toString() + "\n" +
               "Дата и время: " + getFormattedTimestamp() + "\n" +
               "Игрок: " + playerName + " (" + playerUUID + ")\n" +
               "Категория: " + category.getDescription() + "\n" +
               "Сообщение: " + message;
    }
}