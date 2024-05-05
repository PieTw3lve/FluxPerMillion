package com.github.pietw3lve.fpm.listeners;

import org.bukkit.event.Event;

/**
 * The PlayerAction interface represents an action that can be performed by a player in Minecraft.
 * Implementations of this interface should provide methods to check if the action matches a specific event
 * and to execute the action when the event occurs.
 * @author PieTw3lve
 */
public interface EventAction<T extends Event> {
    /**
     * Checks if the action matches the given PlayerInteractEvent.
     *
     * @param event The PlayerInteractEvent to check against.
     * @return true if the action matches the event, false otherwise.
     */
    boolean matches(T event);

    /**
     * Executes the action when the given PlayerInteractEvent occurs.
     *
     * @param event The PlayerInteractEvent to execute the action for.
     */
    void execute(T event);
}
