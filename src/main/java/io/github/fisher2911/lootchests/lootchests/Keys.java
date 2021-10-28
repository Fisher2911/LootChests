package io.github.fisher2911.lootchests.lootchests;

import io.github.fisher2911.lootchests.LootChestsPlugin;
import org.bukkit.NamespacedKey;

public class Keys {

    public static final NamespacedKey LOOT_CHEST_KEY;

    static {
       LOOT_CHEST_KEY = new NamespacedKey(LootChestsPlugin.getPlugin(LootChestsPlugin.class), "loot-chest");
    }

}
