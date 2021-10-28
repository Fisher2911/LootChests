package io.github.fisher2911.lootchests.util;

import io.github.fisher2911.lootchests.LootChestsPlugin;
import io.github.fisher2911.lootchests.lootchests.LootChest;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

public class ChestUtil {

    private final static LootChestsPlugin plugin;

    static {
        plugin = LootChestsPlugin.getPlugin(LootChestsPlugin.class);
    }

    public static final NamespacedKey CHEST_KEY = new NamespacedKey(plugin, "loot-chest");

    private static final String EMPTY = "";

    public static String getLootChestId(final @NotNull ItemStack itemStack) {
        final ItemMeta itemMeta = itemStack.getItemMeta();

        if (itemMeta == null) {
            return EMPTY;
        }

        final PersistentDataContainer container = itemMeta.getPersistentDataContainer();

        final String id = container.get(CHEST_KEY, PersistentDataType.STRING);

        if (id == null) {
            return EMPTY;
        }

        return id;
    }
}
