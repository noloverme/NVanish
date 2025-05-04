package ru.noloverme.nvanish.reports;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import ru.noloverme.nvanish.NVanish;
import ru.noloverme.nvanish.reports.Report.ReportCategory;
import ru.noloverme.nvanish.utils.ColorUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Менеджер для управления отчетами игроков
 */
public class ReportManager {

    private final NVanish plugin;
    private final Map<UUID, Report> reports;
    private final Map<UUID, Long> cooldowns;
    private final File reportsDir;

    /**
     * Конструктор менеджера отчетов
     * @param plugin Экземпляр плагина NVanish
     */
    public ReportManager(NVanish plugin) {
        this.plugin = plugin;
        this.reports = new HashMap<>();
        this.cooldowns = new HashMap<>();
        this.reportsDir = new File(plugin.getDataFolder(), "reports");
        
        // Создаем директорию для отчетов, если она не существует
        if (!reportsDir.exists()) {
            reportsDir.mkdirs();
        }
    }

    /**
     * Создает новый отчет от игрока
     * @param player Игрок, отправивший отчет
     * @param message Сообщение отчета
     * @param category Категория отчета
     * @return true, если отчет успешно создан, иначе false
     */
    public boolean createReport(Player player, String message, ReportCategory category) {
        // Проверка на кулдаун
        if (isOnCooldown(player.getUniqueId())) {
            return false;
        }
        
        // Создание отчета
        Report report = new Report(player.getName(), player.getUniqueId(), message, category);
        reports.put(report.getId(), report);
        
        // Сохранение отчета в файл
        if (saveReport(report)) {
            // Уведомляем администраторов о новом отчете
            notifyAdmins(report);
            
            // Устанавливаем кулдаун для игрока
            setCooldown(player.getUniqueId());
            return true;
        }
        
        return false;
    }

    /**
     * Проверяет, находится ли игрок в периоде ожидания между отправкой отчетов
     * @param playerUUID UUID игрока
     * @return true, если игрок должен подождать перед отправкой нового отчета
     */
    public boolean isOnCooldown(UUID playerUUID) {
        if (!cooldowns.containsKey(playerUUID)) {
            return false;
        }
        
        long lastReportTime = cooldowns.get(playerUUID);
        long currentTime = System.currentTimeMillis();
        long cooldownTime = plugin.getPluginConfig().getReportCooldown() * 1000L; // конвертируем секунды в миллисекунды
        
        return (currentTime - lastReportTime) < cooldownTime;
    }

    /**
     * Получает оставшееся время кулдауна для игрока в секундах
     * @param playerUUID UUID игрока
     * @return Оставшееся время в секундах или 0, если кулдаун истек
     */
    public int getRemainingCooldown(UUID playerUUID) {
        if (!cooldowns.containsKey(playerUUID)) {
            return 0;
        }
        
        long lastReportTime = cooldowns.get(playerUUID);
        long currentTime = System.currentTimeMillis();
        long cooldownTime = plugin.getPluginConfig().getReportCooldown() * 1000L;
        long remainingTime = cooldownTime - (currentTime - lastReportTime);
        
        return remainingTime > 0 ? (int) (remainingTime / 1000) : 0;
    }

    /**
     * Устанавливает кулдаун для игрока
     * @param playerUUID UUID игрока
     */
    private void setCooldown(UUID playerUUID) {
        cooldowns.put(playerUUID, System.currentTimeMillis());
    }

    /**
     * Сохраняет отчет в файл
     * @param report Отчет для сохранения
     * @return true, если отчет успешно сохранен
     */
    private boolean saveReport(Report report) {
        String fileName = "report-" + report.getId().toString().substring(0, 8) + ".txt";
        File reportFile = new File(reportsDir, fileName);
        
        try (FileWriter writer = new FileWriter(reportFile)) {
            writer.write(report.toString());
            return true;
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Не удалось сохранить отчет: " + e.getMessage());
            return false;
        }
    }

    /**
     * Уведомляет всех администраторов о новом отчете
     * @param report Новый отчет
     */
    private void notifyAdmins(Report report) {
        if (!plugin.getPluginConfig().isReportNotificationsEnabled()) {
            return;
        }
        
        String message = plugin.getPluginConfig().getMessage("new_report")
                .replace("%player%", report.getPlayerName())
                .replace("%category%", report.getCategory().getDescription())
                .replace("%id%", report.getId().toString().substring(0, 8));
        
        // Уведомляем всех игроков с правами администратора
        for (Player admin : Bukkit.getOnlinePlayers()) {
            if (admin.hasPermission("nvanish.admin")) {
                admin.sendMessage(ColorUtils.colorize(message));
            }
        }
    }

    /**
     * Получает список всех отчетов
     * @return Список отчетов
     */
    public List<Report> getReports() {
        return new ArrayList<>(reports.values());
    }

    /**
     * Получает отчет по его ID
     * @param reportId ID отчета
     * @return Отчет или null, если не найден
     */
    public Report getReport(UUID reportId) {
        return reports.get(reportId);
    }

    /**
     * Загружает отчеты из файлов при запуске сервера
     * Примечание: в текущей реализации не поддерживается загрузка из файлов,
     * так как это требует более сложного механизма десериализации
     */
    public void loadReports() {
        plugin.getLogger().info("Система отчетов инициализирована. Директория: " + reportsDir.getAbsolutePath());
    }
}