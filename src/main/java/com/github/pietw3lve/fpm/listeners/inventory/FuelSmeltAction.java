package com.github.pietw3lve.fpm.listeners.inventory;

import java.util.Collection;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.FurnaceBurnEvent;

import com.github.pietw3lve.fpm.FluxPerMillion;
import com.github.pietw3lve.fpm.events.FluxLevelChangeEvent;
import com.github.pietw3lve.fpm.utils.EventActionUtil;
import com.github.pietw3lve.fpm.utils.SQLiteUtil.ActionCategory;
import com.github.pietw3lve.fpm.utils.PlayerUtil;

public class FuelSmeltAction implements EventActionUtil<FurnaceBurnEvent> {
    
    private static final String FLUX_POINTS_FUEL_BURN = "flux_points.fuel_burn";
    private static final int SEARCH_RADIUS = 96;

    private final FluxPerMillion plugin;

    public FuelSmeltAction(FluxPerMillion plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean matches(FurnaceBurnEvent event) {
        return true;
    }

    @Override
    public void execute(FurnaceBurnEvent event) {
        FluxLevelChangeEvent fluxEvent = new FluxLevelChangeEvent();
        Location furnaceLocation = event.getBlock().getLocation();
        Collection<Entity> nearbyEntities = event.getBlock().getWorld().getNearbyEntities(furnaceLocation, SEARCH_RADIUS, SEARCH_RADIUS, SEARCH_RADIUS);

        Player closestPlayer = PlayerUtil.findClosestPlayer(furnaceLocation, nearbyEntities);

        double points = plugin.getConfig().getDouble(FLUX_POINTS_FUEL_BURN) * (event.getBurnTime() / 200.0);
        fluxEvent = new FluxLevelChangeEvent(plugin.getFluxMeter(), furnaceLocation, closestPlayer, null, "burned", "fuel", points, ActionCategory.ENERGY);
        plugin.getServer().getPluginManager().callEvent(fluxEvent);
    }
}
