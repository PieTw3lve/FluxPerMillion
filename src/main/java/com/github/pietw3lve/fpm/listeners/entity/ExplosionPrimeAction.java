package com.github.pietw3lve.fpm.listeners.entity;

import java.util.Collection;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.ExplosionPrimeEvent;

import com.github.pietw3lve.fpm.FluxPerMillion;
import com.github.pietw3lve.fpm.events.FluxLevelChangeEvent;
import com.github.pietw3lve.fpm.utils.EventActionUtil;
import com.github.pietw3lve.fpm.utils.PlayerUtil;
import com.github.pietw3lve.fpm.utils.SQLiteUtil.ActionCategory;

public class ExplosionPrimeAction implements EventActionUtil<ExplosionPrimeEvent> {
    
    public final FluxPerMillion plugin;
    public static final String FLUX_POINTS_EXPLOSION = "flux_points.explosion";
    private static final int SEARCH_RADIUS = 96;

    public ExplosionPrimeAction(FluxPerMillion plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean matches(ExplosionPrimeEvent event) {
        return true;
    }

    @Override
    public void execute(ExplosionPrimeEvent event) {
        Entity explosion = event.getEntity();
        Location explosionLocation = event.getEntity().getLocation();
        Collection<Entity> nearbyEntities = explosion.getNearbyEntities(SEARCH_RADIUS, SEARCH_RADIUS, SEARCH_RADIUS);

        Player closestPlayer = PlayerUtil.findClosestPlayer(explosionLocation, nearbyEntities);

        double points = plugin.getConfig().getDouble(FLUX_POINTS_EXPLOSION);
        FluxLevelChangeEvent fluxEvent = new FluxLevelChangeEvent(plugin.getFluxMeter(), explosionLocation, closestPlayer, "detonated", explosion.getName().toLowerCase(), points, ActionCategory.POLLUTION);
        plugin.getServer().getPluginManager().callEvent(fluxEvent);
    }
}
