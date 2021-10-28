package io.github.fisher2911.lootchests.command;

import io.github.fisher2911.lootchests.LootChestsPlugin;
import io.github.fisher2911.lootchests.gui.LootChestEditor;
import io.github.fisher2911.lootchests.lootchests.ChanceItem;
import io.github.fisher2911.lootchests.lootchests.LootChest;
import io.github.fisher2911.lootchests.lootchests.LootChestManager;
import io.github.fisher2911.lootchests.message.Messages;
import io.github.fisher2911.lootchests.number.Range;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LootChestCommand implements CommandExecutor, TabExecutor {

    private final String permission = "lootchests.command";

    final String notNumberMessage = ChatColor.RED + "%number% is not a valid number!";

    private final LootChestsPlugin plugin;
    private final LootChestManager lootChestManager;
    private final Messages messages;

    public LootChestCommand(final LootChestsPlugin plugin) {
        this.plugin = plugin;
        this.lootChestManager = this.plugin.getLootChestManager();
        this.messages = plugin.getMessages();
    }

    @Override
    public boolean onCommand(@NotNull final CommandSender sender,
                             @NotNull final Command command,
                             @NotNull final String label,
                             @NotNull final String[] args) {

        if (!sender.hasPermission(permission)) {
            sender.sendMessage(this.messages.getMessage(Messages.NO_PERMISSION));
            return true;
        }

        final int argsLength = args.length;

        if (argsLength == 0) {
            sender.sendMessage(this.messages.getMessage(Messages.COMMAND_USAGE));
            return true;
        }

        final String arg = args[0];

        switch (arg.toLowerCase()) {
            case "list" -> {
                this.listLootChest(sender);
                return true;
            }

            case "delete" -> {
                this.deleteLootChest(sender, args);
                return true;
            }

            case "reload" -> {
                this.plugin.reload();
                sender.sendMessage(this.messages.getMessage(Messages.RELOADED));
                return true;
            }
        }

        if (!(sender instanceof final Player player)) {
            sender.sendMessage(this.messages.getMessage(Messages.MUST_BE_PLAYER));
            return true;
        }

        switch (arg.toLowerCase()) {
            case "create" -> {
                this.createLootChest(player, args);
                return true;
            }

            case "edit" -> {
                this.editLootChest(player, args);
                return true;
            }

            case "get" -> {
                this.getLootChest(player, args);
                return true;
            }

            case "edititem" -> {
                this.setItemChance(player, args);
                return true;
            }
        }

        player.sendMessage(this.messages.getMessage(Messages.COMMAND_USAGE));
        return true;
    }

    private void createLootChest(final Player player, final String[] args) {

        final int argsLength = args.length;

        if (argsLength < 5) {
            player.sendMessage(this.messages.getMessage(Messages.COMMAND_CREATE));
            return;
        }

        final String id = args[1];
        final String minString = args[2];
        final String maxString = args[3];

        if (!NumberUtils.isNumber(minString)) {
            player.sendMessage(notNumberMessage.replace("%number%", minString));
            return;
        }

        if (!NumberUtils.isNumber(maxString)) {
            player.sendMessage(notNumberMessage.replace("%number%", maxString));
            return;
        }

        final StringBuilder displayName = new StringBuilder();

        for (int i = 4; i < args.length; i++) {
            displayName.append(args[i]);

            if (i != argsLength - 1) {
                displayName.append(" ");
            }
        }

        final int min = Integer.parseInt(minString);
        final int max = Integer.parseInt(maxString);

        if (min >= max) {
            player.sendMessage(ChatColor.RED + "The minimum amount of items must be less than the maximum!");
            return;
        }

        final LootChest lootChest = new LootChest(id, this.messages.color(displayName.toString()),
                new Range(min, max),
                new ArrayList<>());
        this.lootChestManager.addLootChest(lootChest);

        new LootChestEditor(this.plugin, lootChest).open(player);
    }

    private void editLootChest(final Player player, final String[] args) {
        if (args.length < 2) {
            player.sendMessage(this.messages.getMessage(Messages.COMMAND_EDIT));
            return;
        }

        final String id = args[1];

        final Optional<LootChest> optional = this.lootChestManager.getLootChest(id);

        if (optional.isEmpty()) {
            player.sendMessage(this.messages.getMessage(Messages.COMMAND_EDIT));
            return;
        }

        final LootChest lootChest = optional.get();

        if (args.length == 2) {
            new LootChestEditor(this.plugin, lootChest).open(player);
            return;
        }

        int min;
        int max = lootChest.getRange().getMax();

        final String minString = args[2];
        if (!NumberUtils.isNumber(minString)) {
            player.sendMessage(notNumberMessage.replace("%number%", minString));
            return;
        }

        min = Integer.parseInt(minString);
        player.sendMessage(this.messages.getMessage(Messages.MIN_ITEMS_SET, id).
                replace("%amount%", minString));

        if (args.length == 4) {
            final String maxString = args[3];
            if (!NumberUtils.isNumber(maxString)) {
                player.sendMessage(notNumberMessage.replace("%number%", maxString));
                return;
            }

            max = Integer.parseInt(maxString) + 1;
            player.sendMessage(this.messages.getMessage(Messages.MAX_ITEMS_SET, id).
                    replace("%amount%", maxString));
        }

        final Range range = lootChest.getRange();
        range.setMin(min);
        range.setMax(max);
        this.plugin.runAsync(() -> this.lootChestManager.saveLootChest(lootChest));

    }

    private void getLootChest(final Player player, final String[] args) {
        if (args.length != 2) {
            player.sendMessage(this.messages.getMessage(Messages.COMMAND_GET));
            return;
        }

        final String id = args[1];

        final Optional<LootChest> optional = this.lootChestManager.getLootChest(id);

        optional.ifPresentOrElse(lootChest -> player.getInventory().addItem(lootChest.getLootChestItem()),
                () -> player.sendMessage(this.messages.getMessage(Messages.NO_LOOT_CHEST_FOUND, id)));
    }

    private void deleteLootChest(final CommandSender sender, final String[] args) {
        if (args.length != 2) {
            sender.sendMessage(this.messages.getMessage(Messages.COMMAND_DELETE));
            return;
        }

        final String id = args[1];

        final Optional<LootChest> optionalLootChest = this.lootChestManager.getLootChest(id);

        if (optionalLootChest.isEmpty()) {
            sender.sendMessage(this.messages.getMessage(Messages.NO_LOOT_CHEST_FOUND, id));
            return;
        }

        this.lootChestManager.removeLootChest(id);
        sender.sendMessage(this.messages.getMessage(Messages.LOOT_CHEST_DELETED, id));

    }

    private void listLootChest(final CommandSender sender) {
        final List<String> allLootChests = new ArrayList<>(this.lootChestManager.getAll());
        allLootChests.sort(String::compareTo);

        final StringBuilder builder = new StringBuilder(ChatColor.GREEN + "Loot Chests: ");

        final int total = allLootChests.size();
        int index = 0;

        for (final String id : allLootChests) {
            builder.append(id);

            if (index != total - 1) {
                builder.append(", ");
            }
            index++;
        }

        sender.sendMessage(builder.toString());
    }

    private void setItemChance(final Player player, final String[] args) {
        if (args.length < 4) {
            player.sendMessage(this.messages.getMessage(Messages.COMMAND_EDIT_ITEM));
            return;
        }

        final Optional<LootChest> optional = this.lootChestManager.getLootChest(args[1]);

        if (optional.isEmpty()) {
            return;
        }

        final LootChest lootChest = optional.get();

        Material material;

        try {
            material = Material.valueOf(args[2]);
        } catch (final IllegalArgumentException exception) {
            player.sendMessage(ChatColor.RED + args[2] + " is not a valid material.");
            return;
        }

        int min = -1;
        int max = -1;

        final String minString = args[3];
        if (!NumberUtils.isNumber(minString)) {
            if (!minString.equalsIgnoreCase("same")) {
                player.sendMessage(notNumberMessage.replace("%number%", minString));
                return;
            }
        } else {
            min = Integer.parseInt(minString);
        }


        if (args.length == 5) {
            final String maxString = args[4];

            if (!NumberUtils.isNumber(maxString)) {
                player.sendMessage(notNumberMessage.replace("%number%", minString));
                return;
            }

            max = Integer.parseInt(maxString);
        }

        for (final ChanceItem chanceItem : lootChest.getItemStacks()) {

            if (chanceItem.getItemStack().getType() != material) {
                continue;
            }

            final Range range = chanceItem.getRange();

            if (min != -1) {
                range.setMin(min);
            }

            if (max != -1) {
                range.setMax(max + 1);
            }
        }

        String message = this.messages.getMessage(Messages.ITEM_CHANCES_SET).
                replace("%material%", args[2]).
                replace("%min%", minString);

        if (max != -1) {
            message = message.replace("%max%", String.valueOf(max));
        } else {
            message = message.replace("%max%", "same");
        }

        player.sendMessage(message);
        this.plugin.runAsync(() -> this.lootChestManager.saveLootChest(lootChest));
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull final CommandSender sender,
                                      @NotNull final Command command,
                                      @NotNull final String alias,
                                      @NotNull final String[] args) {


        final List<String> tabs = new ArrayList<>();

        if (args.length == 1) {
            final List<String> possibleTabs = List.of("create", "delete", "get", "edit", "list", "edititem");
            final String arg = args[0];
            for (final String possible : possibleTabs) {
                if (possible.toLowerCase().startsWith(arg.toLowerCase())) {
                    tabs.add(possible);
                }
            }
            return tabs;
        }

        if (args.length == 2) {
            final List<String> possibleTabs = List.of("delete", "get", "edit", "edititem");
            final String arg = args[0];
            final String argTwo = args[1];

            if (!possibleTabs.contains(arg.toLowerCase())) {
                return null;
            }

            for (final String possible : this.lootChestManager.getAll()) {
                if (possible.toLowerCase().startsWith(argTwo.toLowerCase())) {
                    tabs.add(possible);
                }
            }

            return tabs;
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("edititem")) {
            final String arg = args[2];
            for (final Material material : Material.values()) {
                final String string = material.toString();
                if (string.toLowerCase().startsWith(arg.toLowerCase())) {
                    tabs.add(string);
                }
            }
            return tabs;
        }

        return null;
    }
}
