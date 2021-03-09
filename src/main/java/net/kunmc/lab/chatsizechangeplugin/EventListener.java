package net.kunmc.lab.chatsizechangeplugin;

import org.bukkit.World;
import org.bukkit.craftbukkit.v1_15_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.world.WorldLoadEvent;

import java.io.IOException;

public class EventListener implements Listener {
    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        try {
            ChatSizeChangePlugin plugin = ChatSizeChangePlugin.getInstance();
            plugin.loadFollowerData((CraftWorld)event.getWorld());
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
            ChatSizeChangePlugin plugin = ChatSizeChangePlugin.getInstance();
            World world = sender.getWorld();
            try {
                plugin.loadFollowerData((CraftWorld)world);
                for (Player player : world.getPlayers()) {
                    plugin.sendFollowerData(player);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        ChatSizeChangePlugin plugin = ChatSizeChangePlugin.getInstance();
        Player player = event.getPlayer();
        plugin.sendConfigData(player);
        plugin.sendFollowerData(player);
    }
}
