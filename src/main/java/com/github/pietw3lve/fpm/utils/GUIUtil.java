package com.github.pietw3lve.fpm.utils;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;

import com.github.pietw3lve.fpm.gui.InventoryGUI;
import com.github.pietw3lve.fpm.handlers.InventoryHandler;

/**
 * Utility class for handling GUI-related events in Minecraft.
 */
public class GUIUtil {
    
    private final Map<Inventory, InventoryHandler> activeInventories = new HashMap<>();

    /**
     * Register an inventory with its corresponding handler.
     *
     * @param inventory the inventory to be handled
     * @param handler the handler for the inventory
     */
    public void registerHandledInventory(Inventory inventory, InventoryHandler handler) {
        this.activeInventories.put(inventory, handler);
    }

    /**
     * Unregister an inventory, removing its handler.
     *
     * @param inventory the inventory to be unregistered
     */
    public void unregisterInventory(Inventory inventory) {
        this.activeInventories.remove(inventory);
    }

    /**
     * Handles an inventory click event.
     *
     * @param event the inventory click event
     */
    public void handleClick(InventoryClickEvent event) {
        InventoryHandler handler = this.activeInventories.get(event.getInventory());
        if (handler != null) {
            handler.onClick(event);
        }
    }

    /**
     * Handles an inventory open event.
     *
     * @param event the inventory open event
     */
    public void handleOpen(InventoryOpenEvent event) {
        InventoryHandler handler = this.activeInventories.get(event.getInventory());
        if (handler != null) {
            handler.onOpen(event);
        }
    }

    /**
     * Handles an inventory close event.
     *
     * @param event the inventory close event
     */
    public void handleClose(InventoryCloseEvent event) {
        Inventory inventory = event.getInventory();
        InventoryHandler handler = this.activeInventories.get(inventory);
        if (handler != null) {
          handler.onClose(event);
          this.unregisterInventory(inventory);
        }
    }

    /**
     * Opens a GUI for a player.
     *
     * @param gui the GUI to be opened
     * @param player the player to open the GUI for
     */
    public void openGUI(InventoryGUI gui, Player player) {
        this.registerHandledInventory(gui.getInventory(), gui);
        player.openInventory(gui.getInventory());
    }
}
