package com.github.pietw3lve.fpm.listeners.block;

import org.bukkit.event.block.BlockBurnEvent;

import com.github.pietw3lve.fpm.FluxPerMillion;
import com.github.pietw3lve.fpm.events.FluxLevelChangeEvent;
import com.github.pietw3lve.fpm.listeners.EventAction;

public class BlockBurnAction implements EventAction<BlockBurnEvent> {
    
    private static final String FLUX_POINTS_BLOCK_BURN = "flux_points.block_burn";
    private static final double DEFAULT_FLUX_POINTS_BLOCK_BURN = 2.0;

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
        double points = plugin.getConfig().getDouble(FLUX_POINTS_BLOCK_BURN, DEFAULT_FLUX_POINTS_BLOCK_BURN);
        FluxLevelChangeEvent fluxEvent = new FluxLevelChangeEvent(plugin.getFluxMeter(), event.getBlock().getLocation(), null, "burned", "block", points);
        plugin.getServer().getPluginManager().callEvent(fluxEvent);
    }
}
