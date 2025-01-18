package com.github.pietw3lve.fpm.listeners.fpm;

import java.math.BigDecimal;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.github.pietw3lve.fpm.FluxPerMillion;
import com.github.pietw3lve.fpm.events.FluxLevelChangeEvent;
import com.github.pietw3lve.fpm.utils.SQLiteUtil.ActionCategory;

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

        Location location = event.getLocation();
        Player player = event.getPlayer();
        String message = event.getMessage();
        String actionType = event.getActionType();
        String type = event.getType();
        double points = event.getPoints();
        ActionCategory category = event.getCategory();

        // Asynchronously record the action to avoid blocking the main thread
        plugin.getDbUtil().recordAction(player, actionType, type, points, location, category);

        if (event.isPlayerAction()) {
            plugin.sendDebugMessage(String.format("%s %s %s - Added %s point(s). (x%d/y%d/z%d/%s)", 
            player.getName(), actionType, type, BigDecimal.valueOf(points).stripTrailingZeros().toString(), location.getBlockX(), location.getBlockY(), location.getBlockZ(), location.getWorld().getName()));
            if (message != null) player.sendMessage(message);
        } else {
            plugin.sendDebugMessage(String.format("%s %s - Added %s point(s). (x%d/y%d/z%d/%s)", 
            actionType.substring(0, 1).toUpperCase() + actionType.substring(1), type, BigDecimal.valueOf(points).stripTrailingZeros().toString(), location.getBlockX(), location.getBlockY(), location.getBlockZ(), location.getWorld().getName()));
        }
    }

}
