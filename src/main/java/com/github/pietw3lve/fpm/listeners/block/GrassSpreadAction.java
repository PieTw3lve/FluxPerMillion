package com.github.pietw3lve.fpm.listeners.block;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockSpreadEvent;

import com.github.pietw3lve.fpm.FluxPerMillion;
import com.github.pietw3lve.fpm.events.FluxLevelChangeEvent;
import com.github.pietw3lve.fpm.listeners.EventAction;

public class GrassSpreadAction implements EventAction<BlockSpreadEvent> {
    
    private static final String FLUX_POINTS_GRASS_GROWTH = "flux_points.grass_growth";
    private static final double DEFAULT_FLUX_POINTS_GRASS_GROWTH = -0.05;

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
        Block source = event.getSource();
        double points = plugin.getConfig().getDouble(FLUX_POINTS_GRASS_GROWTH, DEFAULT_FLUX_POINTS_GRASS_GROWTH);
        FluxLevelChangeEvent fluxEvent = new FluxLevelChangeEvent(plugin.getFluxMeter(), source.getLocation(), null, "grown", "grass", points);
        plugin.getServer().getPluginManager().callEvent(fluxEvent);
    }

    private boolean isGrass(Block block) {
        return block.getType() == Material.GRASS_BLOCK;
    }
}
