package com.github.pietw3lve.fpm.listeners.block;

import java.util.Collection;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBurnEvent;

import com.github.pietw3lve.fpm.FluxPerMillion;
import com.github.pietw3lve.fpm.events.FluxLevelChangeEvent;
import com.github.pietw3lve.fpm.utils.EventActionUtil;
import com.github.pietw3lve.fpm.utils.PlayerUtil;
import com.github.pietw3lve.fpm.utils.SQLiteUtil.ActionCategory;

public class BlockBurnAction implements EventActionUtil<BlockBurnEvent> {
    
    private static final String FLUX_POINTS_BLOCK_BURN = "flux_points.block_burn";
    private static final int SEARCH_RADIUS = 96;

    private final FluxPerMillion plugin;

    public BlockBurnAction(FluxPerMillion plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean matches(BlockBurnEvent event) {
        return true;
    }

    @Override
    public void execute(BlockBurnEvent event) {
        Location blockLocation = event.getBlock().getLocation();
        Collection<Entity> nearbyEntities = event.getBlock().getWorld().getNearbyEntities(blockLocation, SEARCH_RADIUS, SEARCH_RADIUS, SEARCH_RADIUS);

        Player closestPlayer = PlayerUtil.findClosestPlayer(blockLocation, nearbyEntities);

        double points = plugin.getConfig().getDouble(FLUX_POINTS_BLOCK_BURN);
        FluxLevelChangeEvent fluxEvent = new FluxLevelChangeEvent(plugin.getFluxMeter(), blockLocation, closestPlayer, null, "burned", "block", points, ActionCategory.ENERGY);
        plugin.getServer().getPluginManager().callEvent(fluxEvent);
    }
}
