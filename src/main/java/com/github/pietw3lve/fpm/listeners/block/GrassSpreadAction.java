package com.github.pietw3lve.fpm.listeners.block;

import java.util.Collection;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockSpreadEvent;

import com.github.pietw3lve.fpm.FluxPerMillion;
import com.github.pietw3lve.fpm.events.FluxLevelChangeEvent;
import com.github.pietw3lve.fpm.utils.EventActionUtil;
import com.github.pietw3lve.fpm.utils.PlayerUtil;
import com.github.pietw3lve.fpm.utils.SQLiteUtil.ActionCategory;

public class GrassSpreadAction implements EventActionUtil<BlockSpreadEvent> {
    
    private static final String FLUX_POINTS_GRASS_GROWTH = "flux_points.grass_growth";
    private static final int SEARCH_RADIUS = 96;

    private final FluxPerMillion plugin;

    public GrassSpreadAction(FluxPerMillion plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean matches(BlockSpreadEvent event) {
        Block source = event.getSource();
        return isGrass(source);
    }

    @Override
    public void execute(BlockSpreadEvent event) {
        Location blockLocation = event.getSource().getLocation();
        Collection<Entity> nearbyEntities = event.getBlock().getWorld().getNearbyEntities(blockLocation, SEARCH_RADIUS, SEARCH_RADIUS, SEARCH_RADIUS);

        Player closestPlayer = PlayerUtil.findClosestPlayer(blockLocation, nearbyEntities);

        double points = plugin.getConfig().getDouble(FLUX_POINTS_GRASS_GROWTH);
        FluxLevelChangeEvent fluxEvent = new FluxLevelChangeEvent(plugin.getFluxMeter(), blockLocation, closestPlayer, null, "grown", "grass", points, ActionCategory.AGRICULTURE);
        plugin.getServer().getPluginManager().callEvent(fluxEvent);
    }

    private boolean isGrass(Block block) {
        return block.getType() == Material.GRASS_BLOCK;
    }
}
