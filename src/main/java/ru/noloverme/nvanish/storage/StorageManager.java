package ru.noloverme.nvanish.storage;

import ru.noloverme.nvanish.NVanish;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

public class StorageManager {

    private final NVanish plugin;
    private MySQLStorage mysqlStorage;
    private JSONStorage jsonStorage;
    private boolean usingMySQL;

    public StorageManager(NVanish plugin) {
        this.plugin = plugin;
    }

    /**
     * Инициализирует систему хранения
     * @return true если инициализация успешна, иначе false
     */
    public boolean initialize() {
        String storageType = plugin.getPluginConfig().getStorageType();
        
        // Инициализация JSON хранилища в любом случае (как резервное)
        jsonStorage = new JSONStorage(plugin);
        boolean jsonInitialized = jsonStorage.initialize();
        
        if (!jsonInitialized) {
            plugin.getLogger().severe("Не удалось инициализировать JSON хранилище!");
            return false;
        }
        
        // Если выбран MySQL, пробуем его инициализировать
        if (storageType.equalsIgnoreCase("MySQL")) {
            mysqlStorage = new MySQLStorage(plugin);
            boolean mysqlInitialized = mysqlStorage.initialize();
            
            if (mysqlInitialized) {
                usingMySQL = true;
                plugin.getLogger().info("Используется MySQL хранилище");
                
                // Переносим данные из JSON в MySQL при первом запуске
                try {
                    Set<UUID> jsonVanishedPlayers = jsonStorage.getAllVanishedPlayers();
                    for (UUID uuid : jsonVanishedPlayers) {
                        mysqlStorage.setVanished(uuid, true);
                    }
                } catch (Exception e) {
                    plugin.getLogger().log(Level.WARNING, "Ошибка при переносе данных из JSON в MySQL", e);
                }
            } else {
                plugin.getLogger().warning("Не удалось подключиться к MySQL, используется JSON хранилище");
                usingMySQL = false;
            }
        } else {
            usingMySQL = false;
            plugin.getLogger().info("Используется JSON хранилище");
        }
        
        return true;
    }

    /**
     * Выключает хранилище данных
     */
    public void shutdown() {
        if (usingMySQL && mysqlStorage != null) {
            mysqlStorage.shutdown();
        }
        
        if (jsonStorage != null) {
            jsonStorage.shutdown();
        }
    }

    /**
     * Проверяет, находится ли игрок в ванише
     * @param uuid UUID игрока
     * @return true если игрок в ванише, иначе false
     */
    public boolean isVanished(UUID uuid) {
        if (usingMySQL) {
            return mysqlStorage.isVanished(uuid);
        } else {
            return jsonStorage.isVanished(uuid);
        }
    }

    /**
     * Устанавливает состояние ваниша для игрока
     * @param uuid UUID игрока
     * @param vanished true если скрыт, false если виден
     */
    public void setVanished(UUID uuid, boolean vanished) {
        if (usingMySQL) {
            mysqlStorage.setVanished(uuid, vanished);
        }
        
        // В любом случае сохраняем в JSON как резервное хранилище
        jsonStorage.setVanished(uuid, vanished);
    }

    /**
     * Получает список всех UUID игроков в ванише
     * @return Set с UUID скрытых игроков
     */
    public Set<UUID> getAllVanishedPlayers() {
        if (usingMySQL) {
            try {
                return mysqlStorage.getAllVanishedPlayers();
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Ошибка при получении данных из MySQL, использую JSON", e);
                usingMySQL = false;
                return jsonStorage.getAllVanishedPlayers();
            }
        } else {
            return jsonStorage.getAllVanishedPlayers();
        }
    }

    /**
     * Возвращает используемый тип хранилища
     * @return "MySQL" или "JSON"
     */
    public String getCurrentStorageType() {
        return usingMySQL ? "MySQL" : "JSON";
    }
}
