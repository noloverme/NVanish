package com.noloverme.nvanish.database;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.noloverme.nvanish.NVanish;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

public class JsonManager {

    private final NVanish plugin;
    private final File dataFile;
    private final Gson gson;

    public JsonManager(NVanish plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "vanished_players.json");
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    public void savePlayerStates(Set<VanishPlayerState> playerStates) {
        try (FileWriter writer = new FileWriter(dataFile)) {
            gson.toJson(playerStates, writer);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Не удалось сохранить данные в JSON-файл.", e);
        }
    }

    public Set<VanishPlayerState> loadPlayerStates() {
        if (!dataFile.exists()) {
            return new HashSet<>();
        }
        try (FileReader reader = new FileReader(dataFile)) {
            Type type = new TypeToken<Set<VanishPlayerState>>() {}.getType();
            return gson.fromJson(reader, type);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Не удалось загрузить данные из JSON-файла.", e);
            return new HashSet<>();
        }
    }
}