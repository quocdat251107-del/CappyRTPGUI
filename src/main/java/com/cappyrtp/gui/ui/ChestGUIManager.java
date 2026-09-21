package com.cappyrtp.gui.ui;

import com.cappyrtp.gui.CappyRTPGUI;
import com.cappyrtp.gui.config.ConfigManager;
import com.cappyrtp.gui.config.Destination;
import com.cappyrtp.gui.hook.BetterRTPHook;
import com.cappyrtp.gui.util.MessageUtil;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds and displays a Bukkit Inventory UI fallback for older Java Edition players.
 */
public class ChestGUIManager implements Listener {

    private final CappyRTPGUI plugin;
    private final NamespacedKey destKey;

    public ChestGUIManager(CappyRTPGUI plugin) {
        this.plugin = plugin;
        this.destKey = new NamespacedKey(plugin, "destination_id");
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Shows the Chest GUI to the player.
     */
    public void showGUI(Player player) {
        ConfigManager config = plugin.getConfigManager();

        Component title = MessageUtil.parse(config.getChestGuiTitle());
        int size = config.getChestGuiSize();
        // Validate size (must be multiple of 9)
        if (size <= 0 || size > 54 || size % 9 != 0) {
            size = 27;
        }

        Inventory inventory = Bukkit.createInventory(null, size, title);

        // Collect accessible destinations and place in inventory
        for (Destination dest : config.getDestinations().values()) {
            if (player.hasPermission(dest.permission())) {
                ItemStack item = new ItemStack(dest.javaIcon());
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    meta.displayName(MessageUtil.parse(dest.displayName()));
                    // Store destination ID in NBT to retrieve it safely on click
                    meta.getPersistentDataContainer().set(destKey, PersistentDataType.STRING, dest.id());
                    item.setItemMeta(meta);
                }

                int slot = dest.guiSlot();
                if (slot >= 0 && slot < size) {
                    inventory.setItem(slot, item);
                } else {
                    plugin.getLogger().warning("Destination '" + dest.id() + "' has invalid slot " + slot);
                }
            }
        }

        player.openInventory(inventory);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        // Check if viewing our GUI
        Component expectedTitle = MessageUtil.parse(plugin.getConfigManager().getChestGuiTitle());
        if (!event.getView().title().equals(expectedTitle)) {
            return;
        }

        // Always cancel to prevent taking items
        event.setCancelled(true);

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;

        String destId = clicked.getItemMeta().getPersistentDataContainer().get(destKey, PersistentDataType.STRING);
        if (destId != null) {
            player.closeInventory();
            handleSelection(player, destId);
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        // Check if viewing our GUI
        Component expectedTitle = MessageUtil.parse(plugin.getConfigManager().getChestGuiTitle());
        if (event.getView().title().equals(expectedTitle)) {
            event.setCancelled(true);
        }
    }

    private void handleSelection(Player player, String selectedDestId) {
        ConfigManager config = plugin.getConfigManager();
        BetterRTPHook rtpHook = plugin.getBetterRTPHook();

        Destination dest = config.getDestination(selectedDestId);
        if (dest == null) {
            MessageUtil.send(player, config.getMsgNotFound());
            return;
        }

        // Permission check
        if (!player.hasPermission(dest.permission())) {
            MessageUtil.send(player, config.getMsgNoPermission());
            return;
        }

        // Execute the teleport via BetterRTP
        boolean success = rtpHook.teleport(player, dest.worldName());
        if (success) {
            MessageUtil.send(player, config.getMsgTeleporting());
        } else {
            MessageUtil.send(player, config.getMsgNotFound());
        }
    }
}

