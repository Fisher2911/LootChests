package io.github.fisher2911.lootchests.listener;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import io.github.fisher2911.lootchests.LootChestsPlugin;
import io.github.fisher2911.lootchests.lootchests.LootChest;
import io.github.fisher2911.lootchests.lootchests.LootChestManager;
import io.github.fisher2911.lootchests.message.Messages;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.BlockInventoryHolder;
import org.bukkit.inventory.Inventory;

import java.util.Optional;

public class LootChestLootEvent implements Listener {

    private final LootChestsPlugin plugin;
    private final LootChestManager lootChestManager;

    public LootChestLootEvent(final LootChestsPlugin plugin) {
        this.plugin = plugin;
        this.lootChestManager = this.plugin.getLootChestManager();
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryCloseEvent(final InventoryCloseEvent event) {

        final Inventory inventory = event.getInventory();

        if (!(inventory.getHolder() instanceof final BlockInventoryHolder blockInventoryHolder)) {
            return;
        }

        final Block block = blockInventoryHolder.getBlock();

        final Optional<LootChest> optionalLootChest = this.lootChestManager.getLootChestAtBlock(block);

        if (optionalLootChest.isEmpty()) {
            return;
        }

        if (!inventory.isEmpty()) {
            return;
        }

        final Location above = block.getLocation().clone().add(0.5, 1.5, 0.5);

        for (final Hologram hologram : HologramsAPI.getHolograms(this.plugin)) {
            if (above.equals(hologram.getLocation())) {
                hologram.clearLines();
                hologram.appendTextLine(
                        this.plugin.getMessages().
                        getMessage(Messages.HOLOGRAM_DISPLAY_LOOTED, optionalLootChest.
                                get().
                                getDisplayName()));
                break;
            }
        }

    }

}
