package ru.noloverme.nvanish.config;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import ru.noloverme.nvanish.NVanish;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class Config {

    private final NVanish plugin;
    private FileConfiguration config;
    private File configFile;
    
    // Кэш для настроек
    private String storageType;
    private String mysqlHost;
    private int mysqlPort;
    private String mysqlDatabase;
    private String mysqlUser;
    private String mysqlPassword;
    private String jsonLocation;
    private Map<String, Boolean> vanishRestrictions = new HashMap<>();
    private Map<String, Boolean> vanishEffects = new HashMap<>();
    private String vanishGamemode;
    private boolean actionbarEnabled;
    private String actionbarMessage;
    private int actionbarUpdateInterval;
    private boolean placeholdersEnabled;
    
    // Настройки скрытия сообщений
    private boolean hideMessagesEnabled;
    private boolean hideJoinMessages;
    private boolean hideQuitMessages;
    private boolean hideDeathMessages;
    
    // Настройки интеграции с голосовыми чатами
    private boolean plasmoVoiceEnabled;
    private boolean plasmoVoiceHideVoice;
    private boolean plasmoVoiceBlockHearing;
    private boolean simpleVoiceEnabled;
    private boolean simpleVoiceHideVoice;
    
    // Настройки журнала действий
    private boolean loggingEnabled;
    private boolean loggingToConsole;
    private boolean loggingToFile;
    
    // Настройки системы отчетов
    private boolean reportsEnabled;
    private boolean reportsNotifyAdmins;
    private int reportCooldown;

    public Config(NVanish plugin) {
        this.plugin = plugin;
    }

    /**
     * Загружает конфигурацию плагина
     */
    public void loadConfig() {
        if (configFile == null) {
            configFile = new File(plugin.getDataFolder(), "config.yml");
        }
        
        // Создаем файл, если он не существует
        if (!configFile.exists()) {
            plugin.saveResource("config.yml", false);
        }
        
        config = YamlConfiguration.loadConfiguration(configFile);
        
        // Сравниваем с конфигурацией по умолчанию
        InputStream defaultConfigStream = plugin.getResource("config.yml");
        if (defaultConfigStream != null) {
            YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(
                    new InputStreamReader(defaultConfigStream, StandardCharsets.UTF_8));
            
            config.setDefaults(defaultConfig);
        }
        
        // Загружаем настройки в кэш
        loadSettings();
    }

    /**
     * Загружает настройки из конфигурации в кэш
     */
    private void loadSettings() {
        // Настройки хранилища
        ConfigurationSection dbSection = config.getConfigurationSection("database");
        if (dbSection != null) {
            storageType = dbSection.getString("type", "JSON");
            
            ConfigurationSection mysqlSection = dbSection.getConfigurationSection("MYSQL");
            if (mysqlSection != null) {
                mysqlHost = mysqlSection.getString("host", "localhost");
                mysqlPort = mysqlSection.getInt("port", 3306);
                mysqlDatabase = mysqlSection.getString("database", "nvanish");
                mysqlUser = mysqlSection.getString("user", "root");
                mysqlPassword = mysqlSection.getString("password", "");
            }
            
            ConfigurationSection jsonSection = dbSection.getConfigurationSection("JSON");
            if (jsonSection != null) {
                jsonLocation = jsonSection.getString("location", plugin.getDataFolder().getAbsolutePath());
            } else {
                jsonLocation = plugin.getDataFolder().getAbsolutePath();
            }
        }
        
        // Загрузка ограничений в ванише
        vanishRestrictions.clear();
        ConfigurationSection restrictionsSection = config.getConfigurationSection("restrictions");
        if (restrictionsSection != null) {
            for (String key : restrictionsSection.getKeys(false)) {
                vanishRestrictions.put(key, restrictionsSection.getBoolean(key));
            }
        } else {
            // Значения по умолчанию
            vanishRestrictions.put("disable_chat", true);
            vanishRestrictions.put("disable_block_interaction", false);
            vanishRestrictions.put("disable_damage", true);
            vanishRestrictions.put("invulnerable", true);
            vanishRestrictions.put("disable_hunger", true);
            vanishRestrictions.put("disable_item_pickup", true);
            vanishRestrictions.put("disable_item_drop", true);
        }
        
        // Загрузка эффектов в ванише
        vanishEffects.clear();
        ConfigurationSection effectsSection = config.getConfigurationSection("effects");
        if (effectsSection != null) {
            for (String key : effectsSection.getKeys(false)) {
                vanishEffects.put(key, effectsSection.getBoolean(key));
            }
        } else {
            // Значения по умолчанию
            vanishEffects.put("flight", true);
            vanishEffects.put("night_vision", true);
        }
        
        // Загрузка игрового режима
        vanishGamemode = config.getString("vanish_gamemode", "");
        
        // Загрузка настроек ActionBar
        ConfigurationSection actionbarSection = config.getConfigurationSection("actionbar");
        if (actionbarSection != null) {
            actionbarEnabled = actionbarSection.getBoolean("enabled", true);
            actionbarMessage = actionbarSection.getString("message", "&c&lВы находитесь в режиме ваниша!");
            actionbarUpdateInterval = actionbarSection.getInt("update_interval", 20);
        } else {
            // Значения по умолчанию
            actionbarEnabled = true;
            actionbarMessage = "&c&lВы находитесь в режиме ваниша!";
            actionbarUpdateInterval = 20;
        }
        
        // Загрузка настроек интеграции с PlaceholderAPI
        ConfigurationSection placeholdersSection = config.getConfigurationSection("placeholders");
        if (placeholdersSection != null) {
            placeholdersEnabled = placeholdersSection.getBoolean("enabled", true);
        } else {
            placeholdersEnabled = true;
        }
        
        // Загрузка настроек скрытия сообщений
        ConfigurationSection hideMessagesSection = config.getConfigurationSection("hide_messages");
        if (hideMessagesSection != null) {
            hideMessagesEnabled = hideMessagesSection.getBoolean("enabled", true);
            hideJoinMessages = hideMessagesSection.getBoolean("join_messages", true);
            hideQuitMessages = hideMessagesSection.getBoolean("quit_messages", true);
            hideDeathMessages = hideMessagesSection.getBoolean("death_messages", true);
        } else {
            // Значения по умолчанию
            hideMessagesEnabled = true;
            hideJoinMessages = true;
            hideQuitMessages = true;
            hideDeathMessages = true;
        }
        
        // Загрузка настроек интеграции с голосовыми чатами
        ConfigurationSection voiceChatSection = config.getConfigurationSection("voice_chat");
        if (voiceChatSection != null) {
            ConfigurationSection plasmoVoiceSection = voiceChatSection.getConfigurationSection("plasmo_voice");
            if (plasmoVoiceSection != null) {
                plasmoVoiceEnabled = plasmoVoiceSection.getBoolean("enabled", false);
                plasmoVoiceHideVoice = plasmoVoiceSection.getBoolean("hide_voice", true);
                plasmoVoiceBlockHearing = plasmoVoiceSection.getBoolean("block_hearing", false);
            } else {
                plasmoVoiceEnabled = false;
                plasmoVoiceHideVoice = true;
                plasmoVoiceBlockHearing = false;
            }
            
            ConfigurationSection simpleVoiceSection = voiceChatSection.getConfigurationSection("simple_voice");
            if (simpleVoiceSection != null) {
                simpleVoiceEnabled = simpleVoiceSection.getBoolean("enabled", false);
                simpleVoiceHideVoice = simpleVoiceSection.getBoolean("hide_voice", true);
            } else {
                simpleVoiceEnabled = false;
                simpleVoiceHideVoice = true;
            }
        } else {
            // Значения по умолчанию
            plasmoVoiceEnabled = false;
            plasmoVoiceHideVoice = true;
            plasmoVoiceBlockHearing = false;
            simpleVoiceEnabled = false;
            simpleVoiceHideVoice = true;
        }
        
        // Загрузка настроек журнала действий
        ConfigurationSection loggingSection = config.getConfigurationSection("logging");
        if (loggingSection != null) {
            loggingEnabled = loggingSection.getBoolean("enabled", true);
            loggingToConsole = loggingSection.getBoolean("to_console", true);
            loggingToFile = loggingSection.getBoolean("to_file", true);
        } else {
            // Значения по умолчанию
            loggingEnabled = true;
            loggingToConsole = true;
            loggingToFile = true;
        }
        
        // Загрузка настроек системы отчетов
        ConfigurationSection reportsSection = config.getConfigurationSection("reports");
        if (reportsSection != null) {
            reportsEnabled = reportsSection.getBoolean("enabled", true);
            reportsNotifyAdmins = reportsSection.getBoolean("notify_admins", true);
            reportCooldown = reportsSection.getInt("cooldown", 300);
        } else {
            // Значения по умолчанию
            reportsEnabled = true;
            reportsNotifyAdmins = true;
            reportCooldown = 300;
        }
    }

    /**
     * Сохраняет конфигурацию плагина
     */
    public void saveConfig() {
        if (config == null || configFile == null) {
            return;
        }
        
        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Не удалось сохранить конфигурацию в " + configFile, e);
        }
    }

    /**
     * Получает сообщение из конфигурации и обрабатывает системные плейсхолдеры
     * @param path Путь к сообщению
     * @return Сообщение с обработанными системными плейсхолдерами
     */
    public String getMessage(String path) {
        ConfigurationSection messagesSection = config.getConfigurationSection("messages");
        if (messagesSection != null) {
            String message = messagesSection.getString(path, "&cСообщение не найдено: " + path);
            return processSystemPlaceholders(message);
        }
        return "&cСекция сообщений не найдена";
    }
    
    /**
     * Обрабатывает системные плейсхолдеры в сообщении
     * @param message Исходное сообщение
     * @return Сообщение с обработанными системными плейсхолдерами
     */
    private String processSystemPlaceholders(String message) {
        if (message == null || message.isEmpty()) {
            return message;
        }
        
        ConfigurationSection messagesSection = config.getConfigurationSection("messages");
        if (messagesSection != null) {
            // Обработка %prefix%
            if (message.contains("%prefix%")) {
                String prefix = messagesSection.getString("prefix", "&#00FFFF&l[NVanish] &#FFFFFF");
                message = message.replace("%prefix%", prefix);
            }
        }
        
        return message;
    }

    /**
     * Получает тип хранилища данных
     * @return Тип хранилища (MySQL или JSON)
     */
    public String getStorageType() {
        return storageType;
    }

    /**
     * Получает хост MySQL
     * @return Хост MySQL
     */
    public String getMySQLHost() {
        return mysqlHost;
    }

    /**
     * Получает порт MySQL
     * @return Порт MySQL
     */
    public int getMySQLPort() {
        return mysqlPort;
    }

    /**
     * Получает имя базы данных MySQL
     * @return Имя базы данных MySQL
     */
    public String getMySQLDatabase() {
        return mysqlDatabase;
    }

    /**
     * Получает имя пользователя MySQL
     * @return Имя пользователя MySQL
     */
    public String getMySQLUser() {
        return mysqlUser;
    }

    /**
     * Получает пароль MySQL
     * @return Пароль MySQL
     */
    public String getMySQLPassword() {
        return mysqlPassword;
    }

    /**
     * Получает путь к JSON хранилищу
     * @return Путь к JSON хранилищу
     */
    public String getJSONLocation() {
        return jsonLocation;
    }

    /**
     * Проверяет, активно ли указанное ограничение в ванише
     * @param restriction Название ограничения
     * @return true если ограничение активно, иначе false
     */
    public boolean isVanishRestriction(String restriction) {
        return vanishRestrictions.getOrDefault(restriction, false);
    }

    /**
     * Проверяет, активен ли указанный эффект в ванише
     * @param effect Название эффекта
     * @return true если эффект активен, иначе false
     */
    public boolean isVanishEffect(String effect) {
        return vanishEffects.getOrDefault(effect, false);
    }

    /**
     * Получает игровой режим для ваниша
     * @return Название игрового режима или пустая строка, если не настроен
     */
    public String getVanishGamemode() {
        return vanishGamemode;
    }
    
    /**
     * Проверяет, включено ли ActionBar уведомление
     * @return true если ActionBar уведомление включено, иначе false
     */
    public boolean isActionbarEnabled() {
        return actionbarEnabled;
    }
    
    /**
     * Получает сообщение для ActionBar
     * @return Сообщение для ActionBar
     */
    public String getActionbarMessage() {
        return actionbarMessage;
    }
    
    /**
     * Получает интервал обновления ActionBar в тиках
     * @return Интервал обновления в тиках
     */
    public int getActionbarUpdateInterval() {
        return actionbarUpdateInterval;
    }
    
    /**
     * Проверяет, включена ли поддержка PlaceholderAPI
     * @return true если поддержка PlaceholderAPI включена, иначе false
     */
    public boolean isPlaceholdersEnabled() {
        return placeholdersEnabled;
    }
    
    /**
     * Проверяет, включено ли скрытие сообщений
     * @return true если скрытие сообщений включено, иначе false
     */
    public boolean isHideMessagesEnabled() {
        return hideMessagesEnabled;
    }
    
    /**
     * Проверяет, должны ли скрываться сообщения о входе на сервер
     * @return true если сообщения скрываются, иначе false
     */
    public boolean isHideJoinMessages() {
        return hideMessagesEnabled && hideJoinMessages;
    }
    
    /**
     * Проверяет, должны ли скрываться сообщения о выходе с сервера
     * @return true если сообщения скрываются, иначе false
     */
    public boolean isHideQuitMessages() {
        return hideMessagesEnabled && hideQuitMessages;
    }
    
    /**
     * Проверяет, должны ли скрываться сообщения о смерти
     * @return true если сообщения скрываются, иначе false
     */
    public boolean isHideDeathMessages() {
        return hideMessagesEnabled && hideDeathMessages;
    }
    
    /**
     * Проверяет, включена ли интеграция с PlasmoVoice
     * @return true если интеграция включена, иначе false
     */
    public boolean isPlasmoVoiceEnabled() {
        return plasmoVoiceEnabled;
    }
    
    /**
     * Проверяет, должен ли скрываться голос игрока в PlasmoVoice
     * @return true если голос должен скрываться, иначе false
     */
    public boolean isPlasmoVoiceHideVoice() {
        return plasmoVoiceHideVoice;
    }
    
    /**
     * Проверяет, должен ли игрок в ванише не слышать других игроков в PlasmoVoice
     * @return true если игрок не должен слышать других, иначе false
     */
    public boolean isPlasmoVoiceBlockHearing() {
        return plasmoVoiceBlockHearing;
    }
    
    /**
     * Проверяет, включена ли интеграция с SimpleVoice
     * @return true если интеграция включена, иначе false
     */
    public boolean isSimpleVoiceEnabled() {
        return simpleVoiceEnabled;
    }
    
    /**
     * Проверяет, должен ли скрываться голос игрока в SimpleVoice
     * @return true если голос должен скрываться, иначе false
     */
    public boolean isSimpleVoiceHideVoice() {
        return simpleVoiceHideVoice;
    }
    
    /**
     * Проверяет, включено ли логирование действий
     * @return true если логирование действий включено, иначе false
     */
    public boolean isLoggingEnabled() {
        return loggingEnabled;
    }
    
    /**
     * Проверяет, нужно ли логировать действия в консоль
     * @return true если логирование в консоль включено, иначе false
     */
    public boolean isLoggingToConsole() {
        return loggingEnabled && loggingToConsole;
    }
    
    /**
     * Проверяет, нужно ли логировать действия в файл
     * @return true если логирование в файл включено, иначе false
     */
    public boolean isLoggingToFile() {
        return loggingEnabled && loggingToFile;
    }
    
    /**
     * Проверяет, включена ли система отчетов
     * @return true если система отчетов включена, иначе false
     */
    public boolean isReportsEnabled() {
        return reportsEnabled;
    }
    
    /**
     * Проверяет, нужно ли уведомлять администраторов о новых отчетах
     * @return true если нужно уведомлять, иначе false
     */
    public boolean isReportNotificationsEnabled() {
        return reportsEnabled && reportsNotifyAdmins;
    }
    
    /**
     * Получает время ожидания между отправкой отчетов в секундах
     * @return Время ожидания в секундах
     */
    public int getReportCooldown() {
        return reportCooldown;
    }
    
    /**
     * Получает сообщение для плейсхолдера и обрабатывает системные плейсхолдеры
     * @param placeholder Название плейсхолдера
     * @return Сообщение для плейсхолдера с обработанными системными плейсхолдерами
     */
    public String getPlaceholderMessage(String placeholder) {
        ConfigurationSection messagesSection = config.getConfigurationSection("messages");
        if (messagesSection != null) {
            ConfigurationSection placeholderSection = messagesSection.getConfigurationSection("placeholder");
            if (placeholderSection != null && placeholderSection.contains(placeholder)) {
                String message = placeholderSection.getString(placeholder, "");
                return processSystemPlaceholders(message);
            }
        }
        return "";
    }
    
    /**
     * Получает версию конфигурации
     * @return Версия конфигурации или null, если версия не указана
     */
    public String getConfigVersion() {
        return config.getString("config_version");
    }
    
    /**
     * Проверяет соотношение версии конфигурации и версии плагина
     * @return 0 если версии совпадают, -1 если версия конфигурации ниже, 1 если версия конфигурации выше
     */
    public int compareConfigVersion() {
        String configVersion = getConfigVersion();
        String pluginVersion = plugin.getDescription().getVersion();
        
        if (configVersion == null) {
            return -1; // Считаем, что версия конфигурации ниже, если она не указана
        }
        
        // Проверяем простое равенство строк (точное совпадение)
        if (configVersion.equals(pluginVersion)) {
            return 0; // Версии совпадают
        }
        
        try {
            // Разбиваем версии на компоненты (например, "1.2.3" -> [1, 2, 3])
            String[] configParts = configVersion.split("\\.");
            String[] pluginParts = pluginVersion.split("\\.");
            
            // Сравниваем каждый компонент версии
            int length = Math.min(configParts.length, pluginParts.length);
            for (int i = 0; i < length; i++) {
                int configPart = Integer.parseInt(configParts[i]);
                int pluginPart = Integer.parseInt(pluginParts[i]);
                
                if (configPart < pluginPart) {
                    return -1; // Версия конфигурации ниже
                } else if (configPart > pluginPart) {
                    return 1; // Версия конфигурации выше
                }
            }
            
            // Если все совпадающие компоненты равны, проверяем длину
            if (configParts.length < pluginParts.length) {
                return -1; // Версия конфигурации ниже (например, "1.2" < "1.2.1")
            } else if (configParts.length > pluginParts.length) {
                return 1; // Версия конфигурации выше (например, "1.2.1" > "1.2")
            }
            
            return 0; // Версии совпадают
        } catch (Exception e) {
            // Если произошла ошибка при парсинге версий (например, нечисловые компоненты)
            plugin.getLogger().warning("Ошибка при сравнении версий: " + e.getMessage());
            return 0; // Считаем версии совпадающими, чтобы избежать проблем
        }
    }
    
    /**
     * Проверяет, соответствует ли версия конфигурации текущей версии плагина
     * @return true, если версия конфигурации соответствует текущей версии плагина, иначе false
     */
    public boolean isConfigUpToDate() {
        return compareConfigVersion() == 0;
    }
    
    /**
     * Проверяет, является ли версия конфигурации устаревшей (ниже версии плагина)
     * @return true, если версия конфигурации ниже версии плагина, иначе false
     */
    public boolean isConfigOutdated() {
        return compareConfigVersion() < 0;
    }
    
    /**
     * Проверяет, является ли версия конфигурации более новой, чем версия плагина
     * @return true, если версия конфигурации выше версии плагина, иначе false
     */
    public boolean isConfigNewer() {
        return compareConfigVersion() > 0;
    }
    
    /**
     * Создает резервную копию конфигурации и обновляет ее до текущей версии
     * @return путь к резервной копии или null, если произошла ошибка
     */
    public String updateConfig() {
        // Создаем директорию для резервных копий
        File backupDir = new File(plugin.getDataFolder(), "backups");
        if (!backupDir.exists() && !backupDir.mkdirs()) {
            plugin.getLogger().log(Level.SEVERE, "Не удалось создать директорию для резервных копий");
            return null;
        }
        
        // Генерируем имя резервной копии
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        String backupFileName = "config_" + dateFormat.format(new Date()) + ".yml";
        File backupFile = new File(backupDir, backupFileName);
        
        try {
            // Создаем резервную копию
            Files.copy(configFile.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            
            // Сохраняем текущие настройки
            Map<String, Object> existingValues = new HashMap<>();
            for (String key : config.getKeys(true)) {
                if (!config.isConfigurationSection(key)) {
                    existingValues.put(key, config.get(key));
                }
            }
            
            // Заменяем файл конфигурации на файл по умолчанию
            plugin.saveResource("config.yml", true);
            
            // Загружаем новую конфигурацию
            config = YamlConfiguration.loadConfiguration(configFile);
            
            // Восстанавливаем пользовательские настройки
            for (Map.Entry<String, Object> entry : existingValues.entrySet()) {
                String key = entry.getKey();
                // Не восстанавливаем версию конфигурации
                if (!key.equals("config_version")) {
                    config.set(key, entry.getValue());
                }
            }
            
            // Сохраняем обновленную конфигурацию
            config.save(configFile);
            
            // Перезагружаем настройки в кэш
            loadSettings();
            
            return backupFile.getPath();
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Ошибка при обновлении конфигурации", e);
            return null;
        }
    }
}
