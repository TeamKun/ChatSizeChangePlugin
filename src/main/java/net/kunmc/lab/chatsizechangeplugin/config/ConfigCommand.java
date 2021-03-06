package net.kunmc.lab.chatsizechangeplugin.config;

import net.kunmc.lab.chatsizechangeplugin.ChatSizeChangePlugin;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;

public class ConfigCommand implements CommandExecutor, TabCompleter {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        ConfigManager configManager = ChatSizeChangePlugin.getInstance().getConfigManager();
        if (args.length == 1 && args[0].equals("reload")) {
            configManager.load();
            sender.sendMessage(ChatColor.GREEN + "[ChatSizeChange] " + ChatColor.RESET + "コンフィグをリロードしました");
            return true;
        }
        if (args.length == 3 && args[0].equals("set")) {
            String path = args[1];
            String value = args[2];
            boolean result = configManager.setConfig(path, value);
            if (result) {
                sender.sendMessage(ChatColor.GREEN + "[ChatSizeChange] " + ChatColor.RESET + path + "を" + value + "にセットしました");
            } else {
                sender.sendMessage(ChatColor.GREEN + "[ChatSizeChange] " + ChatColor.RED + "コンフィグの設定に失敗しました");
            }
            return true;
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return suggest(args[0], "reload", "set");
        } else if (args.length == 2 && args[0].equals("set")) {
            return suggest(args[1], ConfigManager.getConfigPaths());
        } else {
            return null;
        }
    }

    private List<String> suggest(String arg, String... candidates) {
        List<String> result = new ArrayList<>();
        for (String candidate : candidates) {
            if (candidate.startsWith(arg)) {
                result.add(candidate);
            }
        }
        return result;
    }
}
