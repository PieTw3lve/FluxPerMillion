package com.github.pietw3lve.fpm.listeners.entity;

import java.util.Collection;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.ItemDespawnEvent;

import com.github.pietw3lve.fpm.FluxPerMillion;
import com.github.pietw3lve.fpm.events.FluxLevelChangeEvent;
import com.github.pietw3lve.fpm.listeners.EventAction;

public class ItemDespawnAction implements EventAction<ItemDespawnEvent> {
    
    private static final String FLUX_POINTS_POLLUTION = "flux_points.pollution";
    private static final double DEFAULT_FLUX_POINTS_POLLUTION = 0.25;

    private final FluxPerMillion plugin;

    public ItemDespawnAction(FluxPerMillion plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean matches(ItemDespawnEvent event) {
        return true;
    }

    @Override
    public void execute(ItemDespawnEvent event) {
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

        double points = plugin.getConfig().getDouble(FLUX_POINTS_POLLUTION, DEFAULT_FLUX_POINTS_POLLUTION);
        FluxLevelChangeEvent fluxEvent = new FluxLevelChangeEvent(plugin.getFluxMeter(), itemLocation, closestPlayer, "despawned", "item", points);
        plugin.getServer().getPluginManager().callEvent(fluxEvent);
    }
}
