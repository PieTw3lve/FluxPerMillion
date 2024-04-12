package com.github.pietw3lve.fpm.listeners.fpm;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.github.pietw3lve.fpm.FluxPerMillion;
import com.github.pietw3lve.fpm.events.FluxLevelChangeEvent;

public class FluxLevelChangeListener implements Listener {
    
    private final FluxPerMillion plugin;

    /**
     * FluxLevelChangeListener Constructor.
     * @param plugin
     */
    public FluxLevelChangeListener(FluxPerMillion plugin) {
        this.plugin = plugin;
    }

    /**
     * Listens for flux meter level change events.
     * @param event FluxLevelChangeListener
     */
    @EventHandler
    public void onFluxLevelChange(FluxLevelChangeEvent event) {
        if (event.isCancelled()) return;

        if (event.isPlayerAction()) {
            plugin.getDbUtil().recordUserAction(event.getPlayer(), event.getActionType(), event.getType(), event.getPoints());
            plugin.sendDebugMessage(event.getPlayer().getName() + " " + event.getActionType() + " " + event.getType() + " - Added " + event.getPoints() + " point(s).");
        } else {
            plugin.getDbUtil().recordNaturalAction(event.getActionType(), event.getType(), event.getPoints());
            plugin.sendDebugMessage(event.getActionType().substring(0, 1).toUpperCase() + event.getActionType().substring(1) + " " + event.getType() + " - Added " + event.getPoints() + " point(s).");
        }
    }

}
