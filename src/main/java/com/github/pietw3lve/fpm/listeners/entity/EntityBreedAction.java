package com.github.pietw3lve.fpm.listeners.entity;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityBreedEvent;

import com.github.pietw3lve.fpm.FluxPerMillion;
import com.github.pietw3lve.fpm.events.FluxLevelChangeEvent;
import com.github.pietw3lve.fpm.listeners.EventAction;

public class EntityBreedAction implements EventAction<EntityBreedEvent> {
    
    private static final String ENTITY_OVERPOPULATE_LIMIT = "farming.overpopulate_threshold";
    private static final String ENTITY_PRESERVE_LIMIT = "farming.preserved_threshold";
    private static final String ENTITY_SEARCH_RADIUS = "farming.search_radius";
    private static final String FLUX_POINTS_ENTITY_OVERPOPULATE = "flux_points.entity_overpopulate";
    private static final String FLUX_POINTS_ENTITY_PRESERVE = "flux_points.entity_preserve";
    private static final int DEFAULT_ENTITY_OVERPOPULATE_LIMIT = 20;
    private static final int DEFAULT_ENTITY_PRESERVE_LIMIT = 7;
    private static final int DEFAULT_ENTITY_RADIUS = 10;
    private static final double DEFAULT_FLUX_POINTS_ENTITY_OVERPOPULATE = 0.25;
    private static final double DEFAULT_FLUX_POINTS_ENTITY_PRESERVE = -1.0;

    private final FluxPerMillion plugin;

    public EntityBreedAction(FluxPerMillion plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean matches(EntityBreedEvent event) {
        LivingEntity entity = event.getBreeder();
        return isPlayerInteracted(entity);
    }

    @Override
    public void execute(EntityBreedEvent event) {
        Player player = (Player) event.getBreeder();
        FluxLevelChangeEvent fluxEvent = new FluxLevelChangeEvent();
        int entityOverpopulateLimit = plugin.getConfig().getInt(ENTITY_OVERPOPULATE_LIMIT, DEFAULT_ENTITY_OVERPOPULATE_LIMIT);
        int entityPreserveLimit = plugin.getConfig().getInt(ENTITY_PRESERVE_LIMIT, DEFAULT_ENTITY_PRESERVE_LIMIT);
        int entitySearchRadius = plugin.getConfig().getInt(ENTITY_SEARCH_RADIUS, DEFAULT_ENTITY_RADIUS);

        Entity entity = event.getEntity();
        EntityType entityType = entity.getType();
        int nearbyEntitiesCount = 0;

        for (Entity nearbyEntity : event.getEntity().getNearbyEntities(entitySearchRadius, entitySearchRadius, entitySearchRadius)) {
            if (nearbyEntity.getType() == entityType) {
                nearbyEntitiesCount++;
            }
        }

        if (nearbyEntitiesCount > entityOverpopulateLimit) {
            double points = plugin.getConfig().getDouble(FLUX_POINTS_ENTITY_OVERPOPULATE, DEFAULT_FLUX_POINTS_ENTITY_OVERPOPULATE);
            fluxEvent = new FluxLevelChangeEvent(plugin.getFluxMeter(), entity.getLocation(), player, "overpopulated", entityType.toString().toLowerCase(), points);
        } else if (nearbyEntitiesCount <= entityPreserveLimit) {
            double points = plugin.getConfig().getDouble(FLUX_POINTS_ENTITY_PRESERVE, DEFAULT_FLUX_POINTS_ENTITY_PRESERVE);
            fluxEvent = new FluxLevelChangeEvent(plugin.getFluxMeter(), entity.getLocation(), player, "preserved", entityType.toString().toLowerCase(), points);
        }
        plugin.getServer().getPluginManager().callEvent(fluxEvent);
    }

    private boolean isPlayerInteracted(LivingEntity entity) {
        return entity instanceof Player;
    }
}
