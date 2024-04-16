package com.github.pietw3lve.fpm.listeners.entity;

import java.util.Collection;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemDespawnEvent;

import com.github.pietw3lve.fpm.FluxPerMillion;
import com.github.pietw3lve.fpm.events.FluxLevelChangeEvent;

public class ItemDespawnListener implements Listener {
    
    private final FluxPerMillion plugin;

    /**
     * EntityRemoveListener Constructor.
     * @param plugin
     */
    public ItemDespawnListener(FluxPerMillion plugin) {
        this.plugin = plugin;
    }

    /**
     * Listens for item despawn events.
     * @param event EntityRemoveEvent
     */
    @EventHandler
    public void onItemDespawn(ItemDespawnEvent event) {
        if (event.isCancelled()) return;

        Entity item = event.getEntity();
        Location itemLocation = event.getEntity().getLocation();
        Collection<Entity> nearbyEntities = item.getNearbyEntities(40, 40, 40);

        Player closestPlayer = null;
        double closestDistance = Double.MAX_VALUE;
        for (Entity entity : nearbyEntities) {
            if (entity instanceof Player) {
                double distance = entity.getLocation().distance(itemLocation);
                if (distance < closestDistance) {
                    closestPlayer = (Player) entity;
                    closestDistance = distance;
                }
            }
        }

        double points = plugin.getConfig().getDouble("flux_points.pollution", 0.25);
        FluxLevelChangeEvent fluxEvent = new FluxLevelChangeEvent(plugin.getFluxMeter(), itemLocation, closestPlayer, "despawned", "item", points);
        plugin.getServer().getPluginManager().callEvent(fluxEvent);
    }
}
