package ru.noloverme.nvanish.storage;

import org.json.JSONArray;
import org.json.JSONObject;
import ru.noloverme.nvanish.NVanish;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

public class JSONStorage {

    private final NVanish plugin;
    private final File dataFile;
    private final Set<UUID> vanishedPlayers;

    public JSONStorage(NVanish plugin) {
        this.plugin = plugin;
        this.vanishedPlayers = new HashSet<>();
        
        // Создаем путь для JSON файла
        String jsonPath = plugin.getPluginConfig().getJSONLocation();
        if (!jsonPath.endsWith("/")) {
            jsonPath += "/";
        }
        
        this.dataFile = new File(jsonPath + "players.json");
    }

    /**
     * Инициализирует JSON хранилище
     * @return true если инициализация успешна, иначе false
     */
    public boolean initialize() {
        try {
            // Создаем директорию, если она не существует
            Path directoryPath = Paths.get(dataFile.getParent());
            if (!Files.exists(directoryPath)) {
                Files.createDirectories(directoryPath);
            }
            
            // Создаем файл, если он не существует
            if (!dataFile.exists()) {
                dataFile.createNewFile();
                // Записываем пустой JSON объект
                JSONObject emptyJson = new JSONObject();
                emptyJson.put("vanished_players", new JSONArray());
                Files.write(dataFile.toPath(), emptyJson.toString(2).getBytes());
            }
            
            // Загружаем данные
            loadData();
            
            return true;
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Ошибка при инициализации JSON хранилища: " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * Загружает данные из JSON файла
     */
    private void loadData() {
        vanishedPlayers.clear();
        
        try {
            if (dataFile.length() == 0) {
                // Если файл пустой, создаем пустую структуру
                JSONObject emptyJson = new JSONObject();
                emptyJson.put("vanished_players", new JSONArray());
                Files.write(dataFile.toPath(), emptyJson.toString(2).getBytes());
                return;
            }
            
            String content = new String(Files.readAllBytes(dataFile.toPath()));
            if (content.trim().isEmpty()) {
                // Если файл пустой (но не нулевой длины), создаем пустую структуру
                JSONObject emptyJson = new JSONObject();
                emptyJson.put("vanished_players", new JSONArray());
                Files.write(dataFile.toPath(), emptyJson.toString(2).getBytes());
                return;
            }
            
            JSONObject jsonData = new JSONObject(content);
            JSONArray vanishedArray = jsonData.optJSONArray("vanished_players");
            
            if (vanishedArray != null) {
                for (int i = 0; i < vanishedArray.length(); i++) {
                    try {
                        UUID uuid = UUID.fromString(vanishedArray.getString(i));
                        vanishedPlayers.add(uuid);
                    } catch (IllegalArgumentException e) {
                        plugin.getLogger().warning("Некорректный UUID в JSON: " + vanishedArray.getString(i));
                    }
                }
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Ошибка при загрузке данных из JSON: " + e.getMessage(), e);
            // Создаем резервную копию проблемного файла
            try {
                File backupFile = new File(dataFile.getAbsolutePath() + ".bak");
                Files.copy(dataFile.toPath(), backupFile.toPath());
                
                // Создаем новый пустой файл
                JSONObject emptyJson = new JSONObject();
                emptyJson.put("vanished_players", new JSONArray());
                Files.write(dataFile.toPath(), emptyJson.toString(2).getBytes());
            } catch (IOException ex) {
                plugin.getLogger().log(Level.SEVERE, "Не удалось создать резервную копию проблемного JSON файла", ex);
            }
        }
    }

    /**
     * Сохраняет данные в JSON файл
     */
    private void saveData() {
        try {
            JSONObject jsonData = new JSONObject();
            JSONArray vanishedArray = new JSONArray();
            
            for (UUID uuid : vanishedPlayers) {
                vanishedArray.put(uuid.toString());
            }
            
            jsonData.put("vanished_players", vanishedArray);
            
            Files.write(dataFile.toPath(), jsonData.toString(2).getBytes());
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Ошибка при сохранении данных в JSON: " + e.getMessage(), e);
        }
    }

    /**
     * Выключает JSON хранилище
     */
    public void shutdown() {
        saveData();
    }

    /**
     * Проверяет, находится ли игрок в ванише
     * @param uuid UUID игрока
     * @return true если игрок в ванише, иначе false
     */
    public boolean isVanished(UUID uuid) {
        return vanishedPlayers.contains(uuid);
    }

    /**
     * Устанавливает состояние ваниша для игрока
     * @param uuid UUID игрока
     * @param vanished true если скрыт, false если виден
     */
    public void setVanished(UUID uuid, boolean vanished) {
        if (vanished) {
            vanishedPlayers.add(uuid);
        } else {
            vanishedPlayers.remove(uuid);
        }
        
        saveData();
    }

    /**
     * Получает список всех UUID игроков в ванише
     * @return Set с UUID скрытых игроков
     */
    public Set<UUID> getAllVanishedPlayers() {
        return new HashSet<>(vanishedPlayers);
    }
}
