package io.github.fisher2911.lootchests.message;

import io.github.fisher2911.lootchests.LootChestsPlugin;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Messages {

    public static final String PREFIX = "prefix";
    public static final String RELOADED = "reloaded";
    public static final String COMMAND_USAGE = "command-usage";
    public static final String COMMAND_CREATE = "command-create-format";
    public static final String COMMAND_DELETE = "command-delete-format";
    public static final String COMMAND_GET = "command-get-format";
    public static final String COMMAND_GIVE = "command-give";
    public static final String COMMAND_EDIT = "command-edit-format";
    public static final String COMMAND_LIST = "command-list-format";
    public static final String COMMAND_EDIT_ITEM = "command-edit-item";
    public static final String COMMAND_FIX = "command-fix";
    public static final String NO_PERMISSION = "no-permission";
    public static final String MUST_BE_PLAYER = "must-be-player";
    public static final String LOOT_CHEST_SAVED = "loot-chest-saved";
    public static final String LOOT_CHEST_DELETED = "loot-chest-deleted";
    public static final String NO_LOOT_CHEST_FOUND = "no-loot-chest-found";
    public static final String MIN_ITEMS_SET = "min-items-set";
    public static final String MAX_ITEMS_SET = "max-items-set";
    public static final String ITEM_CHANCES_SET = "item-chances-set";
    public static final String HOLOGRAM_DISPLAY_LOOTED = "hologram-display-looted";
    public static final String HOLOGRAM_DISPLAY_NOT_LOOTED = "hologram-display-not-looted";
    public static final String PLAYER_NOT_ONLINE = "player-not-online";
    public static final String FIX_TASK_COMPLETE = "fix-task-complete";
    public static final String FIXED_LOOT_CHEST_AT = "fixed-loot-chest-at";
    public static final String CHECKED_CHUNK = "checked-chunk";
    public static final String STARTING_FIX = "starting-fix";


    final Map<String, String> messageMap = new HashMap<>();

    private final LootChestsPlugin plugin;

    public Messages(final LootChestsPlugin plugin) {
        this.plugin = plugin;
    }

    public String getMessage(final String message) {
        return this.messageMap.get(message);
    }

    public String getMessage(final String message, final String id) {
        final String string = this.messageMap.get(message);

        if (string == null) {
            return string;
        }

        return string.replace("%lootchest%", id);
    }

    private void addNewMessages() {
        this.plugin.saveDefaultConfig();
        final FileConfiguration config = this.plugin.getConfig();
        final Map<String, String> addMessages = Map.of(
                "command-fix", "%prefix% /lootchest fix <x> <y> <z> <x> <y> <z>",
                "fix-task-complete", "%prefix% Loot chests have been fixed",
                "fixed-loot-chest-at", "%prefix% Fixed loot chests at %x%, %y%, %z%",
                "checked-chunk", "%prefix% Checked chunk %x%, %z% for loot chests",
                "starting-fix", "%prefix% Starting fix");

        final ConfigurationSection messagesSection = config.getConfigurationSection("messages");

        if (messagesSection == null) {
            this.plugin.getLogger().info("No messages section");
            return;
        }

        final Set<String> keys = messagesSection.getKeys(false);

        for (final var entry : addMessages.entrySet()) {
            if (!keys.contains(entry.getKey())) {
                config.set("messages." + entry.getKey(), entry.getValue());
            }
        }

        try {
            config.save(new File(this.plugin.getDataFolder(), "config.yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void load() {
        this.messageMap.clear();
        this.addNewMessages();
        this.plugin.saveDefaultConfig();
        this.plugin.reloadConfig();
        final FileConfiguration config = this.plugin.getConfig();

        final ConfigurationSection section = config.getConfigurationSection("messages");

        if (section == null) {
            this.plugin.getLogger().warning("No messages found in config.yml");
            return;
        }

        String prefix = section.getString(PREFIX);

        if (prefix == null) {
            prefix = "";
        } else {
            prefix = this.color(prefix);
        }

        this.messageMap.put(PREFIX, prefix);

        for (final String key : section.getKeys(false)) {
            String message = section.getString(key);

            if (message == null) {
                message = "";
            } else {
                message = this.color(message);
            }

            this.messageMap.put(key, message.replace("%prefix%", prefix));
        }

    }

    public String color(final String string) {
        return ChatColor.translateAlternateColorCodes('&', string);
    }
}
