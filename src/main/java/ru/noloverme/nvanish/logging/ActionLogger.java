package ru.noloverme.nvanish.logging;

import org.bukkit.entity.Player;
import ru.noloverme.nvanish.NVanish;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;

/**
 * Класс для ведения журнала действий, связанных с ванишем
 */
public class ActionLogger {

    private final NVanish plugin;
    private final File logFile;
    private final SimpleDateFormat dateFormat;
    private boolean enabled;

    /**
     * Конструктор логгера действий
     * @param plugin Экземпляр плагина NVanish
     */
    public ActionLogger(NVanish plugin) {
        this.plugin = plugin;
        this.dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        
        // Создаем директорию логов, если она не существует
        File logsDir = new File(plugin.getDataFolder(), "logs");
        if (!logsDir.exists()) {
            logsDir.mkdirs();
        }
        
        // Создаем файл лога с текущей датой
        SimpleDateFormat fileDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String fileName = "vanish-log-" + fileDateFormat.format(new Date()) + ".log";
        this.logFile = new File(logsDir, fileName);
        
        // Проверяем, включено ли логирование в конфигурации
        this.enabled = plugin.getPluginConfig().isLoggingEnabled();
        
        // Создаем файл, если он не существует
        if (enabled && !logFile.exists()) {
            try {
                logFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().log(Level.WARNING, "Не удалось создать файл журнала действий: " + e.getMessage());
            }
        }
    }
    
    /**
     * Записывает действие в журнал
     * @param action Тип действия
     * @param player Игрок, выполнивший действие
     * @param targetPlayer Целевой игрок (может быть null)
     */
    public void log(ActionType action, Player player, Player targetPlayer) {
        if (!enabled) return;
        
        String timestamp = dateFormat.format(new Date());
        String playerName = player.getName();
        String targetName = targetPlayer != null ? targetPlayer.getName() : "-";
        String logMessage = String.format("[%s] %s: %s -> %s", timestamp, action.toString(), playerName, targetName);
        
        // Записываем в консоль сервера, если это настроено
        if (plugin.getPluginConfig().isLoggingToConsole()) {
            plugin.getLogger().info(logMessage);
        }
        
        // Записываем в файл
        if (plugin.getPluginConfig().isLoggingToFile()) {
            try (PrintWriter writer = new PrintWriter(new FileWriter(logFile, true))) {
                writer.println(logMessage);
            } catch (IOException e) {
                plugin.getLogger().log(Level.WARNING, "Не удалось записать в файл журнала действий: " + e.getMessage());
            }
        }
    }
    
    /**
     * Логирует действие без целевого игрока
     * @param action Тип действия
     * @param player Игрок, выполнивший действие
     */
    public void log(ActionType action, Player player) {
        log(action, player, null);
    }
    
    /**
     * Обновляет статус логгера на основе конфигурации
     */
    public void reload() {
        this.enabled = plugin.getPluginConfig().isLoggingEnabled();
    }
    
    /**
     * Перечисление типов действий для журнала
     */
    public enum ActionType {
        VANISH_ENABLED("Ваниш включен"),
        VANISH_DISABLED("Ваниш выключен"),
        VANISH_OTHER_ENABLED("Ваниш включен для другого игрока"),
        VANISH_OTHER_DISABLED("Ваниш выключен для другого игрока"),
        PLUGIN_RELOADED("Плагин перезагружен");
        
        private final String description;
        
        ActionType(String description) {
            this.description = description;
        }
        
        @Override
        public String toString() {
            return description;
        }
    }
}