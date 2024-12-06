package com.github.pietw3lve.fpm.listeners.block;

import java.util.Collection;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BrewingStartEvent;

import com.github.pietw3lve.fpm.FluxPerMillion;
import com.github.pietw3lve.fpm.events.FluxLevelChangeEvent;
import com.github.pietw3lve.fpm.utils.EventActionUtil;
import com.github.pietw3lve.fpm.utils.PlayerUtil;
import com.github.pietw3lve.fpm.utils.SQLiteUtil.ActionCategory;

public class BrewingStartAction implements EventActionUtil<BrewingStartEvent> {
    
    private static final String FLUX_POINTS_BREWING_START = "flux_points.brew_potion";
    private static final int SEARCH_RADIUS = 96;

    private final FluxPerMillion plugin;

    public BrewingStartAction(FluxPerMillion plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean matches(BrewingStartEvent event) {
        return true;
    }

    @Override
    public void execute(BrewingStartEvent event) {
        FluxLevelChangeEvent fluxEvent = new FluxLevelChangeEvent();
        Location brewingLocation = event.getBlock().getLocation();
        Collection<Entity> nearbyEntities = event.getBlock().getWorld().getNearbyEntities(brewingLocation, SEARCH_RADIUS, SEARCH_RADIUS, SEARCH_RADIUS);

        Player closestPlayer = PlayerUtil.findClosestPlayer(brewingLocation, nearbyEntities);

        double points = plugin.getConfig().getDouble(FLUX_POINTS_BREWING_START) * (event.getTotalBrewTime() / 200.0);
        fluxEvent = new FluxLevelChangeEvent(plugin.getFluxMeter(), brewingLocation, closestPlayer, null, "brewed", "potion", points, ActionCategory.ENERGY);
        plugin.getServer().getPluginManager().callEvent(fluxEvent);
    }
}
