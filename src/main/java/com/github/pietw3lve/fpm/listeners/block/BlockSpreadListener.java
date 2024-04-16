package com.github.pietw3lve.fpm.listeners.block;

import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockSpreadEvent;

import com.github.pietw3lve.fpm.FluxPerMillion;
import com.github.pietw3lve.fpm.events.FluxLevelChangeEvent;

public class BlockSpreadListener implements Listener {
    
    private final FluxPerMillion plugin;

    /**
     * BlockSpreadListener Constructor.
     * @param plugin
     */
    public BlockSpreadListener(FluxPerMillion plugin) {
        this.plugin = plugin;
    }

    /**
     * Listens for block spread events.
     * @param event BlockSpreadEvent
     */
    @EventHandler
    public void onBlockSpread(BlockSpreadEvent event) {
        if (event.isCancelled()) return;

        Block source = event.getSource();
        switch (source.getType()) {
            case GRASS_BLOCK:
                double points = plugin.getConfig().getDouble("flux_points.grass_growth", -0.05);
                FluxLevelChangeEvent fluxEvent = new FluxLevelChangeEvent(plugin.getFluxMeter(), source.getLocation(), null, "grown", "grass", points);
                plugin.getServer().getPluginManager().callEvent(fluxEvent);
                break;
            default:
                break;
        }
    }
}
