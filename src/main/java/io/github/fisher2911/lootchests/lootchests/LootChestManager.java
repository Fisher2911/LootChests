package io.github.fisher2911.lootchests.lootchests;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import io.github.fisher2911.lootchests.LootChestsPlugin;
import io.github.fisher2911.lootchests.message.Messages;
import io.github.fisher2911.lootchests.number.Range;
import io.github.fisher2911.lootchests.util.ChestUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class LootChestManager {

    private static final String DISPLAY_NAME = "display-name";
    private static final String MIN_ITEMS = "min-items";
    private static final String MAX_ITEMS = "max-items";
    private static final String ITEMS = "items";

    private final LootChestsPlugin plugin;

    private final Path path;

    private final Map<String, LootChest> lootChestMap = new HashMap<>();

    public LootChestManager(final LootChestsPlugin plugin) {
        this.plugin = plugin;
        path = Path.of(this.plugin.getDataFolder().getPath(),
                "loot-chests");
    }

    public Optional<LootChest> getLootChest(final String id) {
        return Optional.ofNullable(this.lootChestMap.get(id));
    }

    public void addLootChest(final LootChest lootChest) {
        this.lootChestMap.put(lootChest.getId(), lootChest);
    }

    public void removeLootChest(final String id) {
        final LootChest lootChest = this.lootChestMap.remove(id);

        if (lootChest == null) {
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
            final File file = this.getLootChestFile(id);

            if (!file.exists()) {
                return;
            }

            file.delete();
        });
    }

    public void load() {
        final File file = path.toFile();

        if (!file.exists()) {
            file.mkdirs();
        }

        loadAllLootChests(file);
    }

    public File getLootChestFile(final String id) {
        return Path.of(this.path.toString(), id + ".yml").toFile();
    }

    public void saveAll() {
        for (final LootChest lootChest : this.lootChestMap.values()) {
            saveLootChest(lootChest);
        }
    }

    public void loadAllLootChests(final @NotNull File file) {
        this.lootChestMap.clear();
        final File[] files = file.listFiles();

        if (files == null) {
            return;
        }

        for (final File loadFile : files) {
            loadLootChest(loadFile);
        }
    }

    public void loadLootChest(final File file) {
        final YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

        final String id = file.getName().replace(".yml", "");
        final String displayName = config.getString(DISPLAY_NAME);
        final int minItems = config.getInt(MIN_ITEMS);
        final int maxItems = config.getInt(MAX_ITEMS) + 1;

        final ConfigurationSection itemSection = config.getConfigurationSection(ITEMS);

        final ArrayList<ChanceItem> itemStacks = new ArrayList<>();

        final LootChest lootChest = new LootChest(id, displayName, new Range(minItems, maxItems), itemStacks);

        if (itemSection == null) {
            this.plugin.getLogger().warning("Could not find items in file: " + file.getName());
            this.lootChestMap.put(id, lootChest);
            return;
        }

        for (final String key : itemSection.getKeys(false)) {
            final ItemStack itemStack = itemSection.getItemStack(key + ".item");
            final int min = itemSection.getInt(key + ".min");
            final int max = itemSection.getInt(key + ".max");
            itemStacks.add(new ChanceItem(itemStack, new Range(min, max)));
        }

        this.lootChestMap.put(id, lootChest);
    }

    public void saveLootChest(final LootChest lootChest) {
        final File file = Path.of(path.toString(), lootChest.getId() + ".yml").toFile();

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (final IOException exception) {
                exception.printStackTrace();
            }
        }

        final FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        final Range range = lootChest.getRange();

        config.set(DISPLAY_NAME, lootChest.getDisplayName());
        config.set(MIN_ITEMS, range.getMin());
        config.set(MAX_ITEMS, range.getMax());

        config.set(ITEMS, null);

        int index = 0;

        for (final ChanceItem itemStack : lootChest.getItemStacks()) {
            final Range itemRange = itemStack.getRange();
            config.set(ITEMS + "." + index + ".item", itemStack.getItemStack());
            config.set(ITEMS + "." + index + ".min", itemRange.getMin());
            config.set(ITEMS + "." + index + ".max", itemRange.getMax());
            index++;
        }

        try {
            config.save(file);
        } catch (final IOException exception) {
            exception.printStackTrace();
        }
    }

    public Set<String> getAll() {
        return new HashSet<>(this.lootChestMap.keySet());
    }

    public void saveLootChestLocation(final Location location) {
        final File file = Path.of(this.plugin.getDataFolder().getPath(), "locations.yml").toFile();

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (final IOException exception) {
                this.plugin.getLogger().severe("Could not create file: locations.yml");
            }
        }

        final YamlConfiguration configuration = YamlConfiguration.loadConfiguration(file);

        final String locationString = locationToString(location);

        final String worldUUID = location.getWorld().getUID().toString();

        final List<String> locations = configuration.getStringList(worldUUID);

        locations.add(locationString);

        configuration.set(worldUUID, locations);

        try {
            configuration.save(file);
        } catch (final IOException e) {
            this.plugin.getLogger().severe("Could not save loot chest locations");
        }
    }

    public void spawnHolograms(final UUID world) {
        final File file = Path.of(this.plugin.getDataFolder().getPath(), "locations.yml").toFile();

        if (!file.exists()) {
            return;
        }

        final YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

        final List<String> locations = config.getStringList(world.toString());

        for (final String locationString : locations) {
            final Optional<Location> optionalLocation = this.locationFromString(locationString);

            if (optionalLocation.isEmpty()) {
                continue;
            }

            final Location location = optionalLocation.get();

            final Block block = location.getBlock();

            if (!(block.getState() instanceof final Container containerBlock)) {
                continue;
            }

            final PersistentDataContainer container = containerBlock.getPersistentDataContainer();

            final String id = container.get(ChestUtil.CHEST_KEY, PersistentDataType.STRING);

            if (id == null) {
                return;
            }

            this.getLootChest(id).ifPresent(lootChest -> this.spawnHologram(lootChest,
                    location.clone().add(0.5, 1.5, 0.5)));
        }

        locations.removeIf(string -> {
            final Optional<Location> locationOptional = this.locationFromString(string);

            if (locationOptional.isEmpty()) {
                return true;
            }

            final Location location = locationOptional.get();

            if (!(location instanceof final Container blockContainer)) {
                return true;
            }

            final String id = blockContainer.getPersistentDataContainer().get(ChestUtil.CHEST_KEY, PersistentDataType.STRING);

            if (id == null) {
                return true;
            }

            return this.getLootChest(id).isEmpty();
        });

        config.set(world.toString(), locations);

        try {
            config.save(file);
        } catch (final IOException exception) {
            this.plugin.getLogger().info("Error saving loot chest locations.");
        }
    }

    // uuid:x:y:z
    private Optional<Location> locationFromString(final String string) {
        final String[] parts = string.split(":");
        if (parts.length != 4) {
            return Optional.empty();
        }

        final String uuidString = parts[0];
        final String xString = parts[1];
        final String yString = parts[2];
        final String zString = parts[3];

        try {
            return Optional.of(new Location(Bukkit.getWorld(UUID.fromString(uuidString)),
                    Integer.parseInt(xString),
                    Integer.parseInt(yString),
                    Integer.parseInt(zString)));
        } catch (final NumberFormatException exception) {
            return Optional.empty();
        }
    }

    // uuid:x:y:z
    public String locationToString(final Location location) {
        return location.getWorld().getUID() + ":" + location.getBlockX() + ":" +
                location.getBlockY() + ":" + location.getBlockZ();

    }

    public void spawnHologram(final LootChest lootChest, final Location location) {
        final Hologram hologram = HologramsAPI.createHologram(this.plugin, location);
        hologram.appendTextLine(this.plugin.getMessages().
                getMessage(
                        Messages.HOLOGRAM_DISPLAY_NOT_LOOTED, lootChest.getDisplayName()));
    }

    public Optional<LootChest> getLootChestAtBlock(final Block block) {
        if (block.getType() != Material.CHEST) {
            return Optional.empty();
        }

        final Container chest = (Container) block.getState();

        final String id = chest.getPersistentDataContainer().get(Keys.LOOT_CHEST_KEY, PersistentDataType.STRING);

        if (id == null) {
            return Optional.empty();
        }

        return this.getLootChest(id);
    }
}
