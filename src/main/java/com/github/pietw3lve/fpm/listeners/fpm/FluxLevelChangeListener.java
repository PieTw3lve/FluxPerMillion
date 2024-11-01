package com.github.pietw3lve.fpm.listeners.fpm;

import java.math.BigDecimal;
import java.math.RoundingMode;

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
        String actionType = event.getActionType();
        String type = event.getType();
        BigDecimal bd = new BigDecimal(event.getPoints()).setScale(5, RoundingMode.HALF_UP);
        double points = bd.doubleValue();
        ActionCategory category = event.getCategory();

        if (points == 0) return;

        plugin.getDbUtil().recordAction(player, actionType, type, points, location, category);

        if (event.isPlayerAction()) {
            plugin.sendDebugMessage(String.format("%s %s %s - Added %.2f point(s). (x%d/y%d/z%d/%s)", 
            player.getName(), actionType, type, points, location.getBlockX(), location.getBlockY(), location.getBlockZ(), location.getWorld().getName()));
        } else {
            plugin.sendDebugMessage(String.format("%s %s - Added %.2f point(s). (x%d/y%d/z%d/%s)", 
            actionType.substring(0, 1).toUpperCase() + actionType.substring(1), type, points, location.getBlockX(), location.getBlockY(), location.getBlockZ(), location.getWorld().getName()));
        }
    }

}
