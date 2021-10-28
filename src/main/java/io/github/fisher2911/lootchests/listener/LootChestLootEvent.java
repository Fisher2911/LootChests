package io.github.fisher2911.lootchests.listener;

import io.github.fisher2911.lootchests.LootChestsPlugin;
import io.github.fisher2911.lootchests.lootchests.LootChestManager;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.BlockInventoryHolder;
import org.bukkit.inventory.Inventory;

public class LootChestLootEvent implements Listener {

    private final LootChestsPlugin plugin;
    private final LootChestManager lootChestManager;

    public LootChestLootEvent(final LootChestsPlugin plugin) {
        this.plugin = plugin;
        this.lootChestManager = this.plugin.getLootChestManager();
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInventoryCloseEvent(final InventoryCloseEvent event) {

        final Inventory inventory = event.getInventory();

        if (!(inventory.getHolder() instanceof final BlockInventoryHolder blockInventoryHolder)) {
            return;
        }

        final Block block = blockInventoryHolder.getBlock();

        if (!inventory.isEmpty()) {
            return;
        }

        this.lootChestManager.removeLootChest(block);
    }

}
