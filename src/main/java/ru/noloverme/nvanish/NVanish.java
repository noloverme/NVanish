package ru.noloverme.nvanish;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import ru.noloverme.nvanish.commands.VanishCommand;
import ru.noloverme.nvanish.config.Config;
import ru.noloverme.nvanish.integrations.IntegrationManager;
import ru.noloverme.nvanish.listeners.PlayerListener;
import ru.noloverme.nvanish.logging.ActionLogger;
import ru.noloverme.nvanish.manager.VanishManager;
import ru.noloverme.nvanish.placeholders.PlaceholderExpansion;
import ru.noloverme.nvanish.reports.ReportManager;
import ru.noloverme.nvanish.commands.ReportCommand;
import ru.noloverme.nvanish.storage.StorageManager;
import ru.noloverme.nvanish.utils.ActionBarManager;
import ru.noloverme.nvanish.utils.ColorUtils;

import java.util.logging.Level;

public class NVanish extends JavaPlugin {

    private static NVanish instance;
    private Config pluginConfig;
    private StorageManager storageManager;
    private VanishManager vanishManager;
    private ActionBarManager actionBarManager;
    private IntegrationManager integrationManager;
    private ActionLogger actionLogger;
    private ReportManager reportManager;

    @Override
    public void onEnable() {
        instance = this;
        
        // Загрузка конфигурации
        this.pluginConfig = new Config(this);
        this.pluginConfig.loadConfig();
        
        // Инициализация менеджера хранилища
        this.storageManager = new StorageManager(this);
        if (!this.storageManager.initialize()) {
            getLogger().severe(ColorUtils.colorize(pluginConfig.getMessage("storage_error")));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        
        // Инициализация менеджера ваниша
        this.vanishManager = new VanishManager(this);
        
        // Инициализация менеджера ActionBar
        this.actionBarManager = new ActionBarManager(this);
        
        // Инициализация логгера действий
        this.actionLogger = new ActionLogger(this);
        
        // Инициализация менеджера отчетов
        this.reportManager = new ReportManager(this);
        this.reportManager.loadReports();
        
        // Регистрация команд
        getCommand("vanish").setExecutor(new VanishCommand(this));
        getCommand("nreport").setExecutor(new ReportCommand(this));
        
        // Регистрация обработчиков событий
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        
        // Инициализация менеджера интеграций
        this.integrationManager = new IntegrationManager(this);
        this.integrationManager.initialize();
        
        // Регистрация расширения PlaceholderAPI, если оно доступно
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI") && 
            pluginConfig.isPlaceholdersEnabled()) {
            new PlaceholderExpansion(this).register();
        }
        
        // Проверка и обработка версии конфигурации согласно новым правилам
        // Делаем одну централизованную проверку, которая определит, устарела, новее или совпадает версия
        checkConfigVersion();
        
        getLogger().info(ColorUtils.colorize(pluginConfig.getMessage("plugin_enabled").replace("%version%", getDescription().getVersion())));
    }

    @Override
    public void onDisable() {
        // Сохранение данных при выключении плагина
        if (storageManager != null) {
            storageManager.shutdown();
        }
        
        // Остановка всех ActionBar задач
        if (actionBarManager != null) {
            actionBarManager.stopAll();
        }
        
        // Отключение интеграций
        if (integrationManager != null) {
            integrationManager.shutdown();
        }
        
        getLogger().info(ColorUtils.colorize(pluginConfig.getMessage("plugin_disabled")));
    }
    
    /**
     * Перезагрузка плагина
     */
    public void reload() {
        // Перезагрузка конфигурации
        pluginConfig.loadConfig();
        
        // Перезагрузка хранилища
        storageManager.shutdown();
        if (!storageManager.initialize()) {
            getLogger().log(Level.SEVERE, ColorUtils.colorize(pluginConfig.getMessage("storage_reload_error")));
        }
        
        // Перезагрузка ваниш-менеджера
        vanishManager.reload();
        
        // Перезапуск ActionBar для всех игроков в ванише
        if (actionBarManager != null) {
            actionBarManager.stopAll();
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (vanishManager.isVanished(player)) {
                    actionBarManager.startActionBar(player);
                }
            }
        }
        
        // Переинициализация интеграций
        if (integrationManager != null) {
            integrationManager.shutdown();
            integrationManager.initialize();
        }
        
        // Повторная регистрация расширения PlaceholderAPI, если оно доступно
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI") && 
            pluginConfig.isPlaceholdersEnabled()) {
            new PlaceholderExpansion(this).register();
        }
        
        // Обновление настроек логгера действий
        if (actionLogger != null) {
            actionLogger.reload();
        }
        
        // Проверка и обработка версии конфигурации согласно новым правилам
        // Делаем одну централизованную проверку, которая определит, устарела, новее или совпадает версия
        checkConfigVersion();
        
        getLogger().info(ColorUtils.colorize(pluginConfig.getMessage("plugin_reloaded_log")));
    }
    
    /**
     * Получение экземпляра плагина
     * @return экземпляр плагина
     */
    public static NVanish getInstance() {
        return instance;
    }
    
    /**
     * Получение конфигурации плагина
     * @return конфигурация плагина
     */
    public Config getPluginConfig() {
        return pluginConfig;
    }
    
    /**
     * Получение менеджера хранилища
     * @return менеджер хранилища
     */
    public StorageManager getStorageManager() {
        return storageManager;
    }
    
    /**
     * Получение менеджера ваниша
     * @return менеджер ваниша
     */
    public VanishManager getVanishManager() {
        return vanishManager;
    }
    
    /**
     * Получение менеджера ActionBar
     * @return менеджер ActionBar
     */
    public ActionBarManager getActionBarManager() {
        return actionBarManager;
    }
    
    /**
     * Получение менеджера интеграций
     * @return менеджер интеграций
     */
    public IntegrationManager getIntegrationManager() {
        return integrationManager;
    }
    
    /**
     * Получение логгера действий
     * @return логгер действий
     */
    public ActionLogger getActionLogger() {
        return actionLogger;
    }
    
    /**
     * Получение менеджера отчетов
     * @return менеджер отчетов
     */
    public ReportManager getReportManager() {
        return reportManager;
    }
    
    /**
     * Проверяет версию конфигурации и выполняет соответствующие действия:
     * - Если версия конфигурации ниже версии плагина: выводит предупреждение
     * - Если версия конфигурации выше версии плагина: тихо заменяет на текущую версию
     */
    private void checkConfigVersion() {
        // Если версия конфигурации устарела (ниже версии плагина)
        if (pluginConfig.isConfigOutdated()) {
            String configVersion = pluginConfig.getConfigVersion();
            String pluginVersion = getDescription().getVersion();
            
            // Выводим предупреждение в консоль
            String message = ColorUtils.colorize(
                    pluginConfig.getMessage("config_outdated")
                    .replace("%current%", configVersion != null ? configVersion : "неизвестно")
                    .replace("%latest%", pluginVersion)
            );
            
            getLogger().warning(message);
            
            // Уведомляем администраторов при входе
            Bukkit.getScheduler().runTaskLater(this, () -> {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.hasPermission("nvanish.admin")) {
                        player.sendMessage(message); // Префикс уже содержится в сообщении через %prefix%
                    }
                }
            }, 40L); // Задержка в 2 секунды после загрузки плагина
        } 
        // Если версия конфигурации новее версии плагина (выше)
        else if (pluginConfig.isConfigNewer()) {
            // Тихо обновляем конфигурацию без уведомления пользователей
            updateConfigOnReload(true);
        }
    }
    
    /**
     * Обновляет конфигурацию и создает резервную копию (используется при ручном обновлении)
     * @param silent если true, то не выводит сообщения об обновлении
     * @return true, если обновление прошло успешно
     */
    private boolean updateConfigOnReload(boolean silent) {
        String backupPath = pluginConfig.updateConfig();
        
        if (backupPath != null) {
            if (!silent) {
                // Выводим сообщение об успешном обновлении
                String message = ColorUtils.colorize(
                        pluginConfig.getMessage("config_updated")
                        .replace("%version%", getDescription().getVersion())
                        .replace("%backup%", backupPath)
                );
                
                getLogger().info(message);
                
                // Уведомляем администраторов
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.hasPermission("nvanish.admin")) {
                        player.sendMessage(message); // Префикс уже содержится в сообщении через %prefix%
                    }
                }
            }
            return true;
        } else {
            if (!silent) {
                // Выводим сообщение об ошибке
                String message = ColorUtils.colorize(pluginConfig.getMessage("config_backup_failed"));
                getLogger().warning(message);
                
                // Уведомляем администраторов
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.hasPermission("nvanish.admin")) {
                        player.sendMessage(message); // Префикс уже содержится в сообщении через %prefix%
                    }
                }
            }
            return false;
        }
    }
    
    /**
     * Обновляет конфигурацию с выводом сообщений (для ручного обновления)
     */
    private void updateConfigOnReload() {
        updateConfigOnReload(false);
    }
}
