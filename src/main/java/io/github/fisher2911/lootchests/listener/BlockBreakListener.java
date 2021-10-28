package io.github.fisher2911.lootchests.listener;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import io.github.fisher2911.lootchests.LootChestsPlugin;
import io.github.fisher2911.lootchests.lootchests.LootChest;
import io.github.fisher2911.lootchests.lootchests.LootChestManager;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

import java.util.Optional;

public class BlockBreakListener implements Listener {

    private final LootChestsPlugin plugin;
    private final LootChestManager lootChestManager;

    public BlockBreakListener(final LootChestsPlugin plugin) {
        this.plugin = plugin;
        this.lootChestManager = this.plugin.getLootChestManager();
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(final BlockBreakEvent event) {
        this.removeLootChest(event.getBlock());
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockExplode(final BlockExplodeEvent event) {
        for (final Block block : event.blockList()) {
            this.removeLootChest(block);
        }
        this.removeLootChest(event.getBlock());
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityExplode(final EntityExplodeEvent event) {
        for (final Block block : event.blockList()) {
            this.removeLootChest(block);
        }
    }

    private void removeLootChest(final Block block) {
        final Optional<LootChest> lootChestOptional = this.lootChestManager.getLootChestAtBlock(block);

        if (lootChestOptional.isEmpty()) {
            return;
        }

        final Location above = block.getLocation().clone().add(0.5, 1.5, 0.5);

        for (final Hologram hologram : HologramsAPI.getHolograms(this.plugin)) {
            if  (above.equals(hologram.getLocation())) {
                hologram.delete();
                break;
            }
        }
    }
}
