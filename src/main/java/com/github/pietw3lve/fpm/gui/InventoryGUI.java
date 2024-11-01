package com.github.pietw3lve.fpm.gui;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.github.pietw3lve.fpm.handlers.InventoryHandler;

/**
 * Abstract class representing a GUI inventory.
 */
public abstract class InventoryGUI implements InventoryHandler {

    private final Inventory inventory;
    private final Map<Integer, InventoryButton> buttonMap = new HashMap<>();
  
    /**
     * Constructor to initialize the inventory.
     */
    public InventoryGUI() {
        this.inventory = this.createInventory();
    }
  
    /**
     * Gets the inventory.
     * 
     * @return the inventory
     */
    public Inventory getInventory() {
        return this.inventory;
    }
  
    /**
     * Adds a button to the inventory at the specified slot.
     * 
     * @param slot the slot to add the button to
     * @param button the button to add
     */
    public void addButton(int slot, InventoryButton button) {
        this.buttonMap.put(slot, button);
    }
  
    /**
     * Decorates the inventory with buttons for the specified player.
     * 
     * @param player the player for whom the inventory is being decorated
     */
    public void decorate(Player player) {
        this.buttonMap.forEach((slot, button) -> {
            ItemStack icon = button.getIconCreator().apply(player);
            this.inventory.setItem(slot, icon);
        });
    }
  
    /**
     * Handles inventory click events.
     * 
     * @param event the inventory click event
     */
    @Override
    public void onClick(InventoryClickEvent event) {
        event.setCancelled(true);
        int slot = event.getSlot();
        InventoryButton button = this.buttonMap.get(slot);
        if (button != null) {
            button.getEventConsumer().accept(event);
        }
    }
  
    /**
     * Handles inventory open events.
     * 
     * @param event the inventory open event
     */
    @Override
    public void onOpen(InventoryOpenEvent event) {
        this.decorate((Player) event.getPlayer());
    }
  
    /**
     * Handles inventory close events.
     * 
     * @param event the inventory close event
     */
    @Override
    public void onClose(InventoryCloseEvent event) {}
  
    /**
     * Creates the inventory.
     * 
     * @return the created inventory
     */
    protected abstract Inventory createInventory();
  
}
