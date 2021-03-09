package net.kunmc.lab.chatsizechangeplugin;

import net.kunmc.lab.chatsizechangeplugin.config.ConfigCommand;
import net.kunmc.lab.chatsizechangeplugin.config.ConfigManager;
import net.minecraft.server.v1_15_R1.ScoreboardObjective;
import net.minecraft.server.v1_15_R1.ScoreboardScore;
import net.minecraft.server.v1_15_R1.ScoreboardServer;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_15_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class ChatSizeChangePlugin extends JavaPlugin implements Listener {
    private static final String FOLLOWER_DATA_CHANNEL = "chatsizechange:follower";
    private static final String CONFIG_CHANGE_CHANNEL = "chatsizechange:config";
    private static ChatSizeChangePlugin instance;
    private ConfigManager configManager;
    private byte[] followerDataBytes;
    private byte[] configDataBytes;
    private boolean isEnabled;

    @Override
    public void onEnable() {
        instance = this;
        getServer().getPluginManager().registerEvents(new EventListener(), this);
        Bukkit.getMessenger().registerOutgoingPluginChannel(this, FOLLOWER_DATA_CHANNEL);
        Bukkit.getMessenger().registerOutgoingPluginChannel(this, CONFIG_CHANGE_CHANNEL);
        configManager = new ConfigManager();
        configManager.load();
        ConfigCommand configCommand = new ConfigCommand();
        Objects.requireNonNull(getCommand("cscconfig")).setExecutor(configCommand);
        Objects.requireNonNull(getCommand("cscconfig")).setTabCompleter(configCommand);
        isEnabled = true;
        saveConfig();
    }

    @Override
    public void onDisable() {
        isEnabled = false;
    }

    public static ChatSizeChangePlugin getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public void loadFollowerData(CraftWorld world) throws IOException {
        ScoreboardServer scoreboard = world.getHandle().getScoreboard();
        ScoreboardObjective objective = scoreboard.getObjective("twitter");
        Map<String, Integer> followerData = new HashMap<>();
        for (ScoreboardScore score : scoreboard.getScoresForObjective(objective)) {
            String name = score.getPlayerName();
            if (name.matches("\\w{3,16}")) {
                followerData.put(name, score.getScore());
            }
        }
        try (ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream(); DataOutputStream stream = new DataOutputStream(byteArrayStream)) {
            stream.writeByte(0);
            stream.writeInt(followerData.size());
            for (Map.Entry<String, Integer> entry : followerData.entrySet()) {
                stream.writeByte(entry.getKey().length());
                stream.writeBytes(entry.getKey());
                stream.writeInt(entry.getValue());
            }
            followerDataBytes = byteArrayStream.toByteArray();
        }
    }

    public void sendFollowerData(Player player) {
        if (followerDataBytes == null) {
            return;
        }
        if (!player.getListeningPluginChannels().contains(FOLLOWER_DATA_CHANNEL)) {
            ((CraftPlayer)player).addChannel(FOLLOWER_DATA_CHANNEL);
        }
        player.sendPluginMessage(this, FOLLOWER_DATA_CHANNEL, followerDataBytes);
    }

    public void loadConfigData() {
        ConfigManager manager = getConfigManager();
        try (ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream(); DataOutputStream stream = new DataOutputStream(byteArrayStream)) {
            stream.writeByte(1);
            stream.writeDouble(manager.getDefaultChatSize());
            stream.writeDouble(manager.getMinChatSize());
            stream.writeDouble(manager.getMaxChatSize());
            stream.writeDouble(manager.getChatSizeMultiply());
            stream.writeDouble(manager.getChatBaseSize());
            configDataBytes = byteArrayStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendConfigData(Player player) {
        if (configDataBytes == null) {
            return;
        }
        if (!player.getListeningPluginChannels().contains(CONFIG_CHANGE_CHANNEL)) {
            ((CraftPlayer)player).addChannel(CONFIG_CHANGE_CHANNEL);
        }
        System.out.println(Arrays.toString(configDataBytes));
        player.sendPluginMessage(this, CONFIG_CHANGE_CHANNEL, configDataBytes);
    }

    @Override
    public void reloadConfig() {
        super.reloadConfig();
        if (!isEnabled) {
            return;
        }
        loadConfigData();
        for (World world : Bukkit.getWorlds()) {
            for (Player player : world.getPlayers()) {
                sendConfigData(player);
            }
        }
    }

    @Override
    public void saveConfig() {
        super.saveConfig();
        if (!isEnabled) {
            return;
        }
        loadConfigData();
        for (World world : Bukkit.getWorlds()) {
            for (Player player : world.getPlayers()) {
                sendConfigData(player);
            }
        }
    }
}
