package net.kunmc.lab.chatsizechangeplugin.config;

import net.kunmc.lab.chatsizechangeplugin.ChatSizeChangePlugin;
import net.kunmc.lab.chatsizechangeplugin.config.parser.DoubleParser;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class ConfigManager {
    private FileConfiguration config;
    private static final Map<String, Function<String, Object>> CONFIGS = new HashMap<String, Function<String, Object>>() {{
        put("defaultChatSize", new DoubleParser(0, Double.MAX_VALUE));
        put("minChatSize", new DoubleParser(0, Double.MAX_VALUE));
        put("maxChatSize", new DoubleParser(0, Double.MAX_VALUE));
        put("chatSizeMultiply", new DoubleParser(0, Double.MAX_VALUE));
        put("chatBaseSize", new DoubleParser(0, Double.MAX_VALUE));
    }};

    public static String[] getConfigPaths() {
        return CONFIGS.keySet().toArray(new String[0]);
    }

    public void load() {
        ChatSizeChangePlugin plugin = ChatSizeChangePlugin.getInstance();
        plugin.saveDefaultConfig();
        if (config != null) {
            plugin.reloadConfig();
        }
        config = plugin.getConfig();
    }

    public boolean setConfig(String path, String valueString) {
        if (!CONFIGS.containsKey(path)) {
            return false;
        }
        Object value = CONFIGS.get(path).apply(valueString);
        if (value == null) {
            return false;
        }
        ChatSizeChangePlugin plugin = ChatSizeChangePlugin.getInstance();
        config.set(path, value);
        plugin.saveConfig();
        return true;
    }

    public double getDefaultChatSize() {
        return config.getDouble("defaultChatSize");
    }

    public double getMinChatSize() {
        return config.getDouble("minChatSize");
    }

    public double getMaxChatSize() {
        return config.getDouble("maxChatSize");
    }

    public double getChatSizeMultiply() {
        return config.getDouble("chatSizeMultiply");
    }

    public double getChatBaseSize() {
        return config.getDouble("chatBaseSize");
    }
}
