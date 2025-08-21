package com.noloverme.nvanish;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.wrappers.PlayerInfoData;
import com.comphenix.protocol.wrappers.EnumWrappers.PlayerInfoAction;
import com.noloverme.nvanish.commands.VanishCommand;
import com.noloverme.nvanish.database.JsonManager;
import com.noloverme.nvanish.database.VanishPlayerState;
import com.noloverme.nvanish.expansion.NVanishPlaceholder;
import com.noloverme.nvanish.listeners.VanishListener;
import com.noloverme.nvanish.settings.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import su.plo.voice.api.addon.AddonInitializer;
import su.plo.voice.api.addon.InjectPlasmoVoice;
import su.plo.voice.api.addon.annotation.Addon;
import su.plo.voice.api.server.PlasmoVoiceServer;
import su.plo.voice.api.server.mute.MuteDurationUnit;
import su.plo.voice.api.server.mute.MuteManager;
import su.plo.voice.api.server.player.VoiceServerPlayer;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.stream.Collectors;

@Addon(
        id = "pv-addon-nvanish",
        name = "NVanish Addon",
        version = "2.1",
        authors = {"noloverme"}
)
public final class NVanish extends JavaPlugin implements AddonInitializer {

    private final Set<UUID> vanishedPlayers = new HashSet<>();
    private final Map<UUID, VanishPlayerState> playerStates = new ConcurrentHashMap<>();
    private ProtocolManager protocolManager;
    @InjectPlasmoVoice
    private PlasmoVoiceServer voiceApi;
    private ConfigManager configManager;
    private JsonManager jsonManager;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    @Override
    public void onAddonInitialize() {
        getLogger().info("Аддон PlasmoVoice для NVanish инициализирован.");
    }

    @Override
    public void onAddonShutdown() {
        getLogger().info("Аддон PlasmoVoice для NVanish остановлен.");
    }

    @Override
    public void onEnable() {
        configManager = new ConfigManager(this);
        jsonManager = new JsonManager(this);
        saveDefaultConfig();

        Set<VanishPlayerState> loadedStates = jsonManager.loadPlayerStates();
        for (VanishPlayerState state : loadedStates) {
            vanishedPlayers.add(state.playerUuid());
            playerStates.put(state.playerUuid(), state);
        }

        getCommand("vanish").setExecutor(new VanishCommand(this, configManager));
        getServer().getPluginManager().registerEvents(new VanishListener(this, configManager), this);

        if (getServer().getPluginManager().getPlugin("ProtocolLib") == null) {
            getLogger().severe("ProtocolLib не найден! Отключаю плагин.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        protocolManager = ProtocolLibrary.getProtocolManager();
        setupPacketListeners();

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new NVanishPlaceholder(this).register();
        }

        scheduler.scheduleAtFixedRate(() -> jsonManager.savePlayerStates(new HashSet<>(playerStates.values())), 5, 5, TimeUnit.MINUTES);

        if (configManager.getVanishActionbarEnabled()) {
            String actionBarMessage = configManager.getVanishActionbarMessage();
            if (actionBarMessage != null && !actionBarMessage.isEmpty()) {
                Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
                    for (UUID uuid : vanishedPlayers) {
                        Player player = Bukkit.getPlayer(uuid);
                        if (player != null && player.isOnline()) {
                            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(actionBarMessage));
                        }
                    }
                }, 0L, 5L);
            }
        }
    }

    @Override
    public void onDisable() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
        }

        for (UUID uuid : new HashSet<>(vanishedPlayers)) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                unvanishPlayer(player);
            }
        }
        jsonManager.savePlayerStates(new HashSet<>(playerStates.values()));
        if (protocolManager != null) {
            protocolManager.removePacketListeners(this);
        }
    }

    public void reloadPlugin() {
        this.reloadConfig();
        configManager = new ConfigManager(this);
    }

    public void toggleVanish(Player player) {
        if (isVanished(player)) {
            unvanishPlayer(player);
            player.sendMessage(configManager.getMessage("vanish_off"));
        } else {
            vanishPlayer(player);
            player.sendMessage(configManager.getMessage("vanish_on"));
        }
    }

    public void vanishPlayer(Player player) {
        if (vanishedPlayers.add(player.getUniqueId())) {
            VanishPlayerState state = new VanishPlayerState(
                    player.getUniqueId(),
                    player.getGameMode()
            );
            playerStates.put(player.getUniqueId(), state);

            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                if (!onlinePlayer.hasPermission("nvanish.see_vanished")) {
                    onlinePlayer.hidePlayer(this, player);
                }
            }

            player.setPlayerListName(ChatColor.GRAY + "[Невидимка] " + ChatColor.RESET + player.getDisplayName());
            player.setCollidable(false);

            if (configManager.getSetting("set_gamemode") && !player.hasPermission("nvanish.bypass.gamemode")) {
                player.setGameMode(GameMode.SPECTATOR);
                player.setFlying(true);
            }

            if (voiceApi != null) {
                try {
                    MuteManager muteManager = voiceApi.getMuteManager();
                    Optional<VoiceServerPlayer> voicePlayerOpt = voiceApi.getPlayerManager().getPlayerById(player.getUniqueId());
                    voicePlayerOpt.ifPresent(vp -> {
                        muteManager.mute(
                                player.getUniqueId(),
                                player.getUniqueId(),
                                -1,
                                MuteDurationUnit.SECOND,
                                "Vanish mute",
                                true
                        );
                    });
                } catch (Exception e) {
                    getLogger().severe("Не удалось заглушить игрока " + player.getName() + " с помощью PlasmoVoice API.");
                }
            }
            getLogger().log(Level.INFO, "Игрок " + player.getName() + " стал невидимым.");
        }
    }

    public void unvanishPlayer(Player player) {
        if (vanishedPlayers.remove(player.getUniqueId())) {
            VanishPlayerState state = playerStates.remove(player.getUniqueId());

            if (state != null) {
                player.setGameMode(state.previousGameMode());
                player.setAllowFlight(true);
                player.setAllowFlight(false);
            }

            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                onlinePlayer.showPlayer(this, player);
            }
            player.setPlayerListName(player.getDisplayName());
            player.setCollidable(true);

            if (voiceApi != null) {
                try {
                    MuteManager muteManager = voiceApi.getMuteManager();
                    Optional<VoiceServerPlayer> voicePlayerOpt = voiceApi.getPlayerManager().getPlayerById(player.getUniqueId());
                    voicePlayerOpt.ifPresent(vp -> {
                        muteManager.unmute(player.getUniqueId(), true);
                    });
                } catch (Exception e) {
                    getLogger().severe("Не удалось снять заглушение с игрока " + player.getName() + " с помощью PlasmoVoice API.");
                }
            }
            getLogger().log(Level.INFO, "Игрок " + player.getName() + " больше не невидим.");
        }
    }

    public boolean isVanished(Player player) {
        return vanishedPlayers.contains(player.getUniqueId());
    }

    public Set<Player> getVanishedPlayers() {
        return vanishedPlayers.stream()
                .map(Bukkit::getPlayer)
                .filter(p -> p != null)
                .collect(Collectors.toSet());
    }

    private void setupPacketListeners() {
        protocolManager.addPacketListener(new PacketAdapter(this, ListenerPriority.NORMAL, PacketType.Play.Server.PLAYER_INFO) {
            @Override
            public void onPacketSending(PacketEvent event) {
                Player receiver = event.getPlayer();
                if (receiver.hasPermission("nvanish.see_vanished")) {
                    return;
                }

                try {
                    List<PlayerInfoData> infoDataList = event.getPacket().getPlayerInfoDataLists().read(0);

                    List<PlayerInfoData> filteredList = infoDataList.stream()
                            .filter(data -> !vanishedPlayers.contains(data.getProfile().getUUID()))
                            .collect(Collectors.toList());

                    if (event.getPacket().getPlayerInfoActions().read(0).contains(PlayerInfoAction.ADD_PLAYER)) {
                        event.getPacket().getPlayerInfoDataLists().write(0, filteredList);
                    }
                } catch (Exception e) {
                    getLogger().log(Level.WARNING, "Не удалось обработать пакет PlayerInfo для " + receiver.getName(), e);
                }
            }
        });
    }
}
