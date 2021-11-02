package io.github.fisher2911.lootchests.world;

import io.github.fisher2911.lootchests.LootChestsPlugin;
import io.github.fisher2911.lootchests.lootchests.LootChest;
import io.github.fisher2911.lootchests.lootchests.LootChestManager;
import io.github.fisher2911.lootchests.message.Messages;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.LinkedList;
import java.util.Objects;
import java.util.Queue;

public class LootChestFixer {

    private final LootChestsPlugin plugin;
    private final Messages messages;
    private final Player taskStarter;
    private final LootChestManager lootChestManager;
    private final World world;
    private final int startX;
    private final int startY;
    private final int startZ;
    private final int endX;
    private final int endY;
    private final int endZ;

    private BukkitTask fixTask;

    private final Queue<Pair<Integer, Integer>> chunkCoordinates = new LinkedList<>();

    private LootChestFixer(
            final LootChestsPlugin plugin,
            final Player taskStarter,
            final Location start,
            final Location end) {
        this.plugin = plugin;
        this.messages = this.plugin.getMessages();
        this.taskStarter = taskStarter;
        this.lootChestManager = this.plugin.getLootChestManager();
        this.world = start.getWorld();
        this.startX = Math.min(start.getBlockX(),  end.getBlockX());
        this.startY = Math.min(start.getBlockY(),  end.getBlockY());
        this.startZ = Math.min(start.getBlockZ(),  end.getBlockZ());
        this.endX = Math.max(start.getBlockX(),  end.getBlockX());
        this.endY = Math.max(start.getBlockY(),  end.getBlockY());
        this.endZ = Math.max(start.getBlockZ(),  end.getBlockZ());
    }

    public static void startFix(
            final LootChestsPlugin plugin,
            final Player taskStarter,
            final Location start,
            final Location end) {

        final LootChestFixer fixer = new LootChestFixer(plugin, taskStarter, start, end);
        fixer.populateQueue();
        fixer.fixTask = Bukkit.getScheduler().runTaskTimer(
                fixer.plugin,
                () -> {
                    final var key = fixer.chunkCoordinates.poll();
                    if (key == null) {
                        fixer.cancel();
                        fixer.taskStarter.sendMessage(fixer.messages.getMessage(Messages.FIX_TASK_COMPLETE));
                        return;
                    }

                    fixer.fixChunkLootChests(key);
                },
                1, 1
        );

    }

    private void fixChunkLootChests(final Pair<Integer, Integer> key) {
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = 0; y < world.getMaxHeight(); y++) {
                    final Location location = new Location(this.world,
                            x + (key.key * 16), y, z + (key.value * 16));

                    if (this.lootChestManager.fixLootChest(location)) {
                        this.taskStarter.sendMessage(
                                this.messages.getMessage(Messages.FIXED_LOOT_CHEST_AT).
                                        replace("%x%", String.valueOf(x + (key.key * 16))).
                                        replace("%y%", String.valueOf(y)).
                                        replace("%z%", String.valueOf(z + (key.value * 16)))
                        );
                    }
                }
            }
        }

        this.taskStarter.sendMessage(
                this.messages.getMessage(Messages.CHECKED_CHUNK).
                        replace("%x%", String.valueOf(key.key)).
                        replace("%z%", String.valueOf(key.value))
                );
    }

    private void populateQueue() {
        for (int x = startX; x <= endX; x += 16) {
            for (int z = startZ; z <= endZ; z += 16) {
                this.addChunkKey(new Pair<>(x / 16, z / 16));
            }
        }
    }

    private void addChunkKey(final Pair<Integer, Integer> pair) {
        this.chunkCoordinates.add(pair);
    }

    private void cancel() {
        this.fixTask.cancel();
    }

    private static final class Pair<K, V> {

        private final K key;
        private final V value;

        public Pair(final K key, final V value) {
            this.key = key;
            this.value = value;
        }

        public K getKey() {
            return key;
        }

        public V getValue() {
            return value;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            final Pair<?, ?> pair = (Pair<?, ?>) o;
            return Objects.equals(key, pair.key) && Objects.equals(value, pair.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(key, value);
        }
    }

}
