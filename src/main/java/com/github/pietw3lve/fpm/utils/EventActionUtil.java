package com.github.pietw3lve.fpm.utils;

import org.bukkit.event.Event;

/**
 * The EventAction interface represents an action that can be performed on an event.
 * It provides methods to check if the action matches a specific event and to execute the action on the event.
 *
 * @param <T> the type of event that this action can handle
 * @author PieTw3lve
 */
public interface EventActionUtil<T extends Event> {
    /**
     * Checks if the action matches the given event.
     *
     * @param event the event to check against
     * @return true if the action matches the event, false otherwise
     */
    boolean matches(T event);

    /**
     * Executes the action on the given event.
     *
     * @param event the event to execute the action on
     */
    void execute(T event);
}
