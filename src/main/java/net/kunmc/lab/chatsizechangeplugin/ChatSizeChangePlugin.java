package net.kunmc.lab.chatsizechangeplugin;

import net.minecraft.server.v1_15_R1.ScoreboardObjective;
import net.minecraft.server.v1_15_R1.ScoreboardScore;
import net.minecraft.server.v1_15_R1.ScoreboardServer;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_15_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public final class ChatSizeChangePlugin extends JavaPlugin implements Listener {
    private static final String CHANNEL = "chatsizechange:followers";
    private byte[] followerDataBytes;

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        Bukkit.getMessenger().registerOutgoingPluginChannel(this, CHANNEL);
    }

    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        try {
            loadFollowerData((CraftWorld)event.getWorld());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Player sender = event.getPlayer();
        if (event.getMessage().equalsIgnoreCase("/scoresheet fetch")) {
            if (!(sender instanceof CraftPlayer)) {
                return;
            }
            World world = sender.getWorld();
            try {
                loadFollowerData((CraftWorld)world);
                for (Player player : world.getPlayers()) {
                    sendFollowerData(player);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        sendFollowerData(player);
    }

    private void loadFollowerData(CraftWorld world) throws IOException {
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
            stream.writeInt(followerData.size());
            for (Map.Entry<String, Integer> entry : followerData.entrySet()) {
                stream.writeByte(entry.getKey().length());
                stream.writeBytes(entry.getKey());
                stream.writeInt(entry.getValue());
            }
            followerDataBytes = byteArrayStream.toByteArray();
        }
    }

    private void sendFollowerData(Player player) {
        if (followerDataBytes == null) {
            return;
        }
        if (!player.getListeningPluginChannels().contains(CHANNEL)) {
            ((CraftPlayer)player).addChannel(CHANNEL);
        }
        player.sendPluginMessage(this, CHANNEL, followerDataBytes);
    }
}
