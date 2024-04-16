package com.github.pietw3lve.fpm.listeners.block;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;

import com.github.pietw3lve.fpm.FluxPerMillion;
import com.github.pietw3lve.fpm.events.FluxLevelChangeEvent;

public class BlockGrowListener implements Listener {
    
    private final FluxPerMillion plugin;

    /**
     * BlockGrowthListener Constructor.
     * @param plugin
     */
    public BlockGrowListener(FluxPerMillion plugin) {
        this.plugin = plugin;
    }

    /**
     * Listens for block growth events.
     * @param event StructureGrowEvent
     */
    @EventHandler
    public void onBlockGrow(BlockGrowEvent event) {
        if (event.isCancelled()) return;

        Block block = event.getBlock();
        BlockData blockData = event.getNewState().getBlockData();
        if (blockData instanceof Ageable) {
            Ageable ageable = (Ageable) blockData;
            if (ageable.getAge() == ageable.getMaximumAge()) {
                if (block.hasMetadata("fpm:fertilized")) {
                    Player player = (Player) block.getMetadata("fpm:fertilized").get(0).value();
                    double points = plugin.getConfig().getDouble("flux_points.crop_growth", -0.25);
                    FluxLevelChangeEvent fluxEvent = new FluxLevelChangeEvent(plugin.getFluxMeter(), block.getLocation(), player, "grown", "crop", points);
                    plugin.getServer().getPluginManager().callEvent(fluxEvent);
                    return;
                }
                double points = plugin.getConfig().getDouble("flux_points.crop_growth", -0.25);
                FluxLevelChangeEvent fluxEvent = new FluxLevelChangeEvent(plugin.getFluxMeter(), block.getLocation(), null, "grown", "crop", points);
                plugin.getServer().getPluginManager().callEvent(fluxEvent);
            }
        }
    }
}
