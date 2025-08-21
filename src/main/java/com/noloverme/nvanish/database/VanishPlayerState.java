package com.noloverme.nvanish.database;

import org.bukkit.GameMode;
import java.util.UUID;

public record VanishPlayerState(UUID playerUuid, GameMode previousGameMode) {}
