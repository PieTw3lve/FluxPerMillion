package com.github.pietw3lve.fpm.listeners.inventory;

import java.util.Collection;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.FurnaceBurnEvent;

import com.github.pietw3lve.fpm.FluxPerMillion;
import com.github.pietw3lve.fpm.events.FluxLevelChangeEvent;

public class FurnaceBurnListener implements Listener {
    
    private final FluxPerMillion plugin;

    /**
     * FurnaceBurnListener Constructor.
     * @param plugin
     */
    public FurnaceBurnListener(FluxPerMillion plugin) {
        this.plugin = plugin;
    }

    /**
     * Listens for furnace burn events.
     * @param event FurnaceBurnEvent
     */
    @EventHandler
    public void onFurnaceSmelt(FurnaceBurnEvent event) {
        if (event.isCancelled()) return;

        FluxLevelChangeEvent fluxEvent = new FluxLevelChangeEvent();
        Location furnaceLocation = event.getBlock().getLocation();
        Collection<Entity> nearbyEntities = event.getBlock().getWorld().getNearbyEntities(furnaceLocation, 20, 20, 20);

        Player closestPlayer = null;
        double closestDistance = Double.MAX_VALUE;
        for (Entity entity : nearbyEntities) {
            if (entity instanceof Player) {
                double distance = entity.getLocation().distance(furnaceLocation);
                if (distance < closestDistance) {
                    closestPlayer = (Player) entity;
                    closestDistance = distance;
                }
            }
        }

        double points = plugin.getConfig().getDouble("flux_points.fuel_burn", 0.25) * (event.getBurnTime() / 200.0);
        fluxEvent = new FluxLevelChangeEvent(plugin.getFluxMeter(), furnaceLocation, closestPlayer, "burned", "fuel", points);
        plugin.getServer().getPluginManager().callEvent(fluxEvent);
    }
}
