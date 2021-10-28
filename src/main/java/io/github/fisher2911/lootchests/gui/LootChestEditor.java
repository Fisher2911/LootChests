package io.github.fisher2911.lootchests.gui;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.GuiItem;
import dev.triumphteam.gui.guis.StorageGui;
import io.github.fisher2911.lootchests.LootChestsPlugin;
import io.github.fisher2911.lootchests.lootchests.ChanceItem;
import io.github.fisher2911.lootchests.lootchests.LootChest;
import io.github.fisher2911.lootchests.lootchests.LootChestManager;
import io.github.fisher2911.lootchests.message.Messages;
import io.github.fisher2911.lootchests.number.Range;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class LootChestEditor {

    private static final GuiItem SAVE_ITEM = ItemBuilder.
            from(Material.LIME_CONCRETE).
            setName(ChatColor.GREEN + "Save").
            asGuiItem(event -> event.setCancelled(true));

    private final LootChestsPlugin plugin;
    private final LootChestManager lootChestManager;
    private final LootChest lootChest;
    private final StorageGui gui;

    public LootChestEditor(final LootChestsPlugin plugin, final LootChest lootChest) {
        this.plugin = plugin;
        this.lootChestManager = this.plugin.getLootChestManager();
        this.lootChest = lootChest;
        this.gui = new StorageGui(6, this.lootChest.getDisplayName() + " Editor", new HashSet<>());

        for (int i = 45; i < 54; i++) {

            if (i == 49) {
                this.gui.setItem(i, SAVE_ITEM);
                continue;
            }

            this.gui.setItem(i,
                    ItemBuilder.
                    from(Material.BLACK_STAINED_GLASS_PANE).
                    setName(" ").
                    asGuiItem(event -> event.setCancelled(true)));
        }

        int index = 0;
        for (final ChanceItem chanceItem : this.lootChest.getItemStacks()) {
            this.gui.getInventory().setItem(index, chanceItem.getItemStack());
            index++;
        }

        this.gui.setDefaultClickAction(event -> {
            final ItemStack clicked = event.getCurrentItem();

            if (clicked == null || !clicked.equals(SAVE_ITEM.getItemStack())) {
                return;
            }

            final List<ChanceItem> itemStackList = new ArrayList<>();

            final Inventory inventory = event.getView().getTopInventory();

            for (int i = 0; i < 45; i++) {
                final ItemStack itemStack = inventory.getItem(i);

                if (itemStack == null || itemStack.getType() == Material.AIR) {
                    continue;
                }

                final int itemAmount = itemStack.getAmount();

                itemStackList.add(new ChanceItem(itemStack, new Range(itemAmount, itemAmount + 1)));
            }

            this.lootChest.setItemStacks(itemStackList);

            this.plugin.runAsync(() -> this.lootChestManager.saveLootChest(this.lootChest));
            event.getWhoClicked().sendMessage(this.plugin.getMessages().getMessage(Messages.LOOT_CHEST_SAVED,
                    this.lootChest.getId()));
        });
    }

    public void open(final Player player) {
        gui.open(player);
    }
}
