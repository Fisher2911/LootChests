package io.github.fisher2911.lootchests.listener;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import io.github.fisher2911.lootchests.LootChestsPlugin;
import io.github.fisher2911.lootchests.lootchests.Keys;
import io.github.fisher2911.lootchests.lootchests.LootChest;
import io.github.fisher2911.lootchests.lootchests.LootChestManager;
import io.github.fisher2911.lootchests.message.Messages;
import io.github.fisher2911.lootchests.util.ChestUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.block.data.type.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.Map;
import java.util.Optional;

public class BlockPlaceListener implements Listener {

    private final LootChestsPlugin plugin;
    private final LootChestManager lootChestManager;

    public BlockPlaceListener(final LootChestsPlugin plugin) {
        this.plugin = plugin;
        this.lootChestManager = this.plugin.getLootChestManager();
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockPlace(final BlockPlaceEvent event) {
        final ItemStack placedItemStack = event.getItemInHand();

        final String id = ChestUtil.getLootChestId(placedItemStack);

        final Optional<LootChest> lootChestOptional = this.lootChestManager.getLootChest(id);

        if (lootChestOptional.isEmpty()) {
            return;
        }

        final LootChest lootChest = lootChestOptional.get();

        this.placeLootChest(event, lootChest);
    }

    private void placeLootChest(final BlockPlaceEvent event, final LootChest lootChest) {
        final Block placed = event.getBlockPlaced();

        if (placed.getType() != Material.CHEST) {
            placed.setType(Material.CHEST);
        }

        final Container chest = (Container) placed.getState();

        final Location above = chest.getLocation().getBlock().getLocation().clone().add(0.5, 1.5, 0.5);

        this.lootChestManager.spawnHologram(lootChest, above);

        chest.getPersistentDataContainer().set(Keys.LOOT_CHEST_KEY, PersistentDataType.STRING, lootChest.getId());
        chest.update(true);

        final Map<Integer, ItemStack> lootItemStacks = lootChest.getRandomItems();

        for (final Map.Entry<Integer, ItemStack> entry : lootItemStacks.entrySet()) {
            chest.getInventory().setItem(entry.getKey(), entry.getValue());
        }

        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () ->
                this.lootChestManager.saveLootChestLocation(placed.getLocation()));

    }
}
