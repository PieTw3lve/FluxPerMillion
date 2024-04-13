package com.github.pietw3lve.fpm.listeners.block;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBurnEvent;

import com.github.pietw3lve.fpm.FluxPerMillion;
import com.github.pietw3lve.fpm.events.FluxLevelChangeEvent;

public class BlockBurnListener implements Listener {

    private final FluxPerMillion plugin;

    /**
     * BlockBurnListener Constructor.
     * @param plugin
     */
    public BlockBurnListener(FluxPerMillion plugin) {
        this.plugin = plugin;
    }

    /**
     * Listens for block burn events.
     * @param event BlockBurnEvent
     */
    @EventHandler
    public void onBlockBurn(BlockBurnEvent event) {
        if (event.isCancelled()) return;

        double points = plugin.getConfig().getDouble("flux_points.block_burn", 2.0);
        FluxLevelChangeEvent fluxEvent = new FluxLevelChangeEvent(plugin.getFluxMeter(), null, "burned", "block", points);
        plugin.getServer().getPluginManager().callEvent(fluxEvent);
    }
}
