package com.github.pietw3lve.fpm.gui;

import java.util.function.Consumer;
import java.util.function.Function;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Represents a button in an inventory GUI.
 */
public class InventoryButton {

    private Function<Player, ItemStack> iconCreator;
    private Consumer<InventoryClickEvent> eventConsumer;
  
    /**
     * Sets the icon creator function for this button.
     *
     * @param iconCreator a function that takes a Player and returns an ItemStack
     * @return the current InventoryButton instance
     */
    public InventoryButton creator(Function<Player, ItemStack> iconCreator) {
      this.iconCreator = iconCreator;
      return this;
    }
  
    /**
     * Sets the event consumer for this button.
     *
     * @param eventConsumer a consumer that handles InventoryClickEvent
     * @return the current InventoryButton instance
     */
    public InventoryButton consumer(Consumer<InventoryClickEvent> eventConsumer) {
      this.eventConsumer = eventConsumer;
      return this;
    }
  
    /**
     * Gets the event consumer for this button.
     *
     * @return the event consumer
     */
    public Consumer<InventoryClickEvent> getEventConsumer() {
      return this.eventConsumer;
    }
  
    /**
     * Gets the icon creator function for this button.
     *
     * @return the icon creator function
     */
    public Function<Player, ItemStack> getIconCreator() {
      return this.iconCreator;
    }
}
