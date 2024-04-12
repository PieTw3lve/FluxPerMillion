package com.github.pietw3lve.fpm.listeners.fpm;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.github.pietw3lve.fpm.FluxPerMillion;
import com.github.pietw3lve.fpm.events.StatusLevelChangeEvent;

public class StatusLevelChangeListener implements Listener {
    
    private final FluxPerMillion plugin;

    /**
     * StatusChangeListener Constructor.
     * @param plugin
     */
    public StatusLevelChangeListener(FluxPerMillion plugin) {
        this.plugin = plugin;
    }

    /**
     * Listens for flux meter status change events.
     * @param event StatusChangeEvent
     */
    @EventHandler
    public void onStatusChange(StatusLevelChangeEvent event) {
        if (event.isCancelled()) return;
        plugin.sendDebugMessage("Flux meter status changed from " + event.getPrevStatusLevel() + " to " + event.getNewStatusLevel());
    }
}
