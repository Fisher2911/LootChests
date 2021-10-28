package io.github.fisher2911.lootchests.lootchests;

import io.github.fisher2911.lootchests.number.Range;
import org.bukkit.inventory.ItemStack;

public class ChanceItem {

    private final ItemStack itemStack;
    private final Range range;

    public ChanceItem(final ItemStack itemStack, final Range range) {
        this.itemStack = itemStack;
        this.range = range;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public Range getRange() {
        return range;
    }

    public ItemStack getRandomAmount() {
        final ItemStack clone = this.itemStack.clone();
        clone.setAmount(this.range.getRandom());
        return clone;
    }

    @Override
    public String toString() {
        return "ChanceItem{" +
                "itemStack=" + itemStack +
                ", range=" + range +
                '}';
    }
}
