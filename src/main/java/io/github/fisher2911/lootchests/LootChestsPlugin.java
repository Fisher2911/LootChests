package io.github.fisher2911.lootchests;

import io.github.fisher2911.lootchests.command.LootChestCommand;
import io.github.fisher2911.lootchests.listener.BlockBreakListener;
import io.github.fisher2911.lootchests.listener.BlockPlaceListener;
import io.github.fisher2911.lootchests.listener.LootChestLootEvent;
import io.github.fisher2911.lootchests.listener.WorldLoadListener;
import io.github.fisher2911.lootchests.lootchests.LootChestManager;
import io.github.fisher2911.lootchests.message.Messages;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class LootChestsPlugin extends JavaPlugin {

    private LootChestManager lootChestManager;
    private Messages messages;

    @Override
    public void onEnable() {
        final int pluginId = 13181;
        Metrics metrics = new Metrics(this, pluginId);

        this.lootChestManager = new LootChestManager(this);
        this.messages = new Messages(this);
        List.of(
                        new BlockPlaceListener(this),
                        new BlockBreakListener(this),
                        new LootChestLootEvent(this),
                        new WorldLoadListener(this)).
                forEach(listener ->
                        this.getServer().
                                getPluginManager().
                                registerEvents(listener, this));

        this.getCommand("lootchest").setExecutor(new LootChestCommand(this));
        this.load();
    }

    public void load() {
        this.lootChestManager.load();
        this.messages.load();
        Bukkit.getScheduler().runTaskLater(this,
                () -> {
                    for (final World world : Bukkit.getWorlds()) {
                        this.lootChestManager.spawnHolograms(world.getUID());
                    }
                }, 1);
    }
    
    public void reload() {
       this.runAsync(() -> {
            this.lootChestManager.saveAll();
            Bukkit.getScheduler().runTask(this, this::load);
        });
    }
    
    @Override
    public void onDisable() {
        this.lootChestManager.saveAll();
    }

    public LootChestManager getLootChestManager() {
        return lootChestManager;
    }

    public Messages getMessages() {
        return messages;
    }
    
    public void runAsync(final Runnable runnable) {
        Bukkit.getScheduler().runTaskAsynchronously(this, runnable);
    }
}
