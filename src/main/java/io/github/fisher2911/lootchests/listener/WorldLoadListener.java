package io.github.fisher2911.lootchests.listener;

import io.github.fisher2911.lootchests.LootChestsPlugin;
import io.github.fisher2911.lootchests.lootchests.LootChestManager;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;

import java.util.UUID;

public class WorldLoadListener implements Listener {

    private final LootChestsPlugin plugin;
    private final LootChestManager lootChestManager;

    public WorldLoadListener(final LootChestsPlugin plugin) {
        this.plugin = plugin;
        this.lootChestManager = this.plugin.getLootChestManager();
    }

    @EventHandler
    public void onWorldLoad(final WorldLoadEvent event) {
        final World world = event.getWorld();

        final UUID uuid = world.getUID();

        this.lootChestManager.spawnHolograms(uuid);
    }
}
