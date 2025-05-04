package ru.noloverme.nvanish.storage;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import ru.noloverme.nvanish.NVanish;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

public class MySQLStorage {

    private final NVanish plugin;
    private HikariDataSource dataSource;

    public MySQLStorage(NVanish plugin) {
        this.plugin = plugin;
    }

    /**
     * Инициализирует MySQL хранилище
     * @return true если инициализация успешна, иначе false
     */
    public boolean initialize() {
        String host = plugin.getPluginConfig().getMySQLHost();
        int port = plugin.getPluginConfig().getMySQLPort();
        String database = plugin.getPluginConfig().getMySQLDatabase();
        String user = plugin.getPluginConfig().getMySQLUser();
        String password = plugin.getPluginConfig().getMySQLPassword();
        
        try {
            // Настройка HikariCP
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database);
            config.setUsername(user);
            config.setPassword(password);
            config.setMaximumPoolSize(10);
            config.setMinimumIdle(2);
            config.setConnectionTimeout(30000);
            config.setIdleTimeout(600000);
            config.setMaxLifetime(1800000);
            
            // Дополнительные параметры
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            config.addDataSourceProperty("useServerPrepStmts", "true");
            config.addDataSourceProperty("useLocalSessionState", "true");
            config.addDataSourceProperty("rewriteBatchedStatements", "true");
            config.addDataSourceProperty("cacheResultSetMetadata", "true");
            config.addDataSourceProperty("cacheServerConfiguration", "true");
            config.addDataSourceProperty("elideSetAutoCommits", "true");
            config.addDataSourceProperty("maintainTimeStats", "false");
            
            dataSource = new HikariDataSource(config);
            
            // Создание таблицы, если она не существует
            createTables();
            
            return true;
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Ошибка подключения к MySQL: " + e.getMessage(), e);
            if (dataSource != null) {
                dataSource.close();
            }
            return false;
        }
    }

    /**
     * Создает необходимые таблицы в БД
     */
    private void createTables() {
        String createTableQuery = "CREATE TABLE IF NOT EXISTS nvanish_players (" +
                "uuid VARCHAR(36) NOT NULL PRIMARY KEY, " +
                "vanished BOOLEAN NOT NULL DEFAULT FALSE" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";
        
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(createTableQuery)) {
            statement.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Ошибка при создании таблиц: " + e.getMessage(), e);
        }
    }

    /**
     * Выключает MySQL хранилище
     */
    public void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }

    /**
     * Проверяет, находится ли игрок в ванише
     * @param uuid UUID игрока
     * @return true если игрок в ванише, иначе false
     */
    public boolean isVanished(UUID uuid) {
        String query = "SELECT vanished FROM nvanish_players WHERE uuid = ?";
        
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, uuid.toString());
            
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getBoolean("vanished");
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Ошибка при проверке состояния ваниша: " + e.getMessage(), e);
        }
        
        return false;
    }

    /**
     * Устанавливает состояние ваниша для игрока
     * @param uuid UUID игрока
     * @param vanished true если скрыт, false если виден
     */
    public void setVanished(UUID uuid, boolean vanished) {
        String query = "INSERT INTO nvanish_players (uuid, vanished) VALUES (?, ?) " +
                "ON DUPLICATE KEY UPDATE vanished = ?";
        
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, uuid.toString());
            statement.setBoolean(2, vanished);
            statement.setBoolean(3, vanished);
            
            statement.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Ошибка при установке состояния ваниша: " + e.getMessage(), e);
        }
    }

    /**
     * Получает список всех UUID игроков в ванише
     * @return Set с UUID скрытых игроков
     */
    public Set<UUID> getAllVanishedPlayers() {
        Set<UUID> vanishedPlayers = new HashSet<>();
        String query = "SELECT uuid FROM nvanish_players WHERE vanished = true";
        
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {
            
            while (resultSet.next()) {
                try {
                    UUID uuid = UUID.fromString(resultSet.getString("uuid"));
                    vanishedPlayers.add(uuid);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Некорректный UUID в базе данных: " + resultSet.getString("uuid"));
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Ошибка при получении списка скрытых игроков: " + e.getMessage(), e);
        }
        
        return vanishedPlayers;
    }
}
