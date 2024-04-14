package com.github.pietw3lve.fpm.listeners.entity;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityBreedEvent;

import com.github.pietw3lve.fpm.FluxPerMillion;
import com.github.pietw3lve.fpm.events.FluxLevelChangeEvent;

public class EntityBreedListener implements Listener {
    
    private final FluxPerMillion plugin;

    /**
     * EntityBreedListener Constructor.
     * @param plugin
     */
    public EntityBreedListener(FluxPerMillion plugin) {
        this.plugin = plugin;
    }

    /**
     * Listens for entity breed events.
     * @param event EntityBreedEvent
     */
    @EventHandler
    public void onEntityBreed(EntityBreedEvent event) {
        if (event.getBreeder() instanceof Player) {
            FluxLevelChangeEvent fluxEvent = new FluxLevelChangeEvent();
            Player player = (Player) event.getBreeder();
            int entityOverpopulateLimit = plugin.getConfig().getInt("farming.overpopulate_threshold");
            int entityPreserveLimit = plugin.getConfig().getInt("farming.preserved_threshold");
            int entityRadius = plugin.getConfig().getInt("farming.check_radius");
            
            EntityType entityType = event.getEntity().getType();
            int nearbyEntitiesCount = 0;
            
            for (Entity nearbyEntity : event.getEntity().getNearbyEntities(entityRadius, entityRadius, entityRadius)) {
                if (nearbyEntity.getType() == entityType) {
                    nearbyEntitiesCount++;
                }
            }
            
            if (nearbyEntitiesCount > entityOverpopulateLimit) {
                double points = plugin.getConfig().getDouble("flux_points.entity_overpopulate", 0.25);
                fluxEvent = new FluxLevelChangeEvent(plugin.getFluxMeter(), player, "overpopulated", entityType.toString().toLowerCase(), points);
            } else if (nearbyEntitiesCount <= entityPreserveLimit) {
                double points = plugin.getConfig().getDouble("flux_points.entity_preserve", -1.0);
                fluxEvent = new FluxLevelChangeEvent(plugin.getFluxMeter(), player, "preserved", entityType.toString().toLowerCase(), points);
            }
            plugin.getServer().getPluginManager().callEvent(fluxEvent);
        }
    }
}
