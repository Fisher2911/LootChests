package io.github.fisher2911.lootchests.lootchests;

import io.github.fisher2911.lootchests.number.Range;
import io.github.fisher2911.lootchests.util.ChestUtil;
import io.github.fisher2911.lootchests.util.RandomUtil;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class LootChest {

    private final String id;
    private final String displayName;
    private final Range range;
    private List<ChanceItem> itemStacks;

    public LootChest(final String id, final String displayName, final Range range, final List<ChanceItem> itemStacks) {
        this.id = id;
        this.displayName = displayName;
        this.range = range;
        this.itemStacks = itemStacks;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Range getRange() {
        return range;
    }

    public List<ChanceItem> getItemStacks() {
        return itemStacks;
    }

    public Map<Integer, ItemStack> getRandomItems() {
        final Map<Integer, ItemStack> itemStackSlots = new HashMap<>();

        final List<ItemStack> itemsToBeAdded = this.getItemsToBeAdded();

        for (ItemStack itemStack : itemsToBeAdded) {
            int randSlot = this.getRandomChestSlot();

            while (itemStackSlots.containsKey(randSlot)) {
                randSlot = this.getRandomChestSlot();
            }

            itemStackSlots.put(randSlot, itemStack);
        }

        return itemStackSlots;
    }

    private List<ItemStack> getItemsToBeAdded() {
        final List<ItemStack> itemStacks = new ArrayList<>();

        final int rand = Math.min(27, this.range.getRandom());

        final Set<Integer> addItemStacks = new HashSet<>();

        if (this.itemStacks.isEmpty()) {
            return itemStacks;
        }

        if (rand > this.getItemStacks().size()) {
            for (final ChanceItem chanceItem : this.getItemStacks()) {
                itemStacks.add(chanceItem.getRandomAmount());
            }
            return itemStacks;
        }

        for (int i = 0; i < rand; i++) {
            int randItem = RandomUtil.RANDOM.nextInt(0, this.itemStacks.size());

            while (addItemStacks.contains(randItem)) {
                randItem = RandomUtil.RANDOM.nextInt(0, this.itemStacks.size());
            }

            itemStacks.add(this.itemStacks.get(randItem).getRandomAmount());
            addItemStacks.add(randItem);
        }

        return itemStacks;
    }

    private int getRandomChestSlot() {
        return RandomUtil.RANDOM.nextInt(0, 27);
    }

    public ItemStack getLootChestItem() {
        final ItemStack itemStack = new ItemStack(Material.CHEST);

        final ItemMeta itemMeta = itemStack.getItemMeta();

        if (itemMeta == null) {
            return itemStack;
        }

        itemMeta.setDisplayName(displayName);

        final PersistentDataContainer container = itemMeta.getPersistentDataContainer();

        container.set(ChestUtil.CHEST_KEY, PersistentDataType.STRING, this.id);

        itemStack.setItemMeta(itemMeta);

        return itemStack;
    }

    public void setItemStacks(final List<ChanceItem> itemStacks) {
        this.itemStacks = itemStacks;
    }

    @Override
    public String toString() {
        return "LootChest{" +
                "id='" + id + '\'' +
                ", displayName='" + displayName + '\'' +
                ", range=" + range +
                ", itemStacks=" + itemStacks +
                '}';
    }
}
