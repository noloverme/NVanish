package com.noloverme.nvanish.expansion;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import com.noloverme.nvanish.NVanish;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class NVanishPlaceholder extends PlaceholderExpansion {

    private final NVanish plugin;

    public NVanishPlaceholder(NVanish plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "nvanish";
    }

    @Override
    public @NotNull String getAuthor() {
        return "noloverme";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String identifier) {
        if (player == null) {
            return "";
        }
        if ("status".equals(identifier)) {
            return plugin.isVanished(player) ? "Vanished" : "Visible";
        }
        return null;
    }
}
