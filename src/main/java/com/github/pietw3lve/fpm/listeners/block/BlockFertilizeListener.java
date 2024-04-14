package com.github.pietw3lve.fpm.listeners.block;

import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFertilizeEvent;
import org.bukkit.metadata.FixedMetadataValue;

import com.github.pietw3lve.fpm.FluxPerMillion;

public class BlockFertilizeListener implements Listener {
    
    private final FluxPerMillion plugin;

    /**
     * BlockFertilizeListener Constructor.
     * @param plugin
     */
    public BlockFertilizeListener(FluxPerMillion plugin) {
        this.plugin = plugin;
    }

    /**
     * Listens for block fertilize events.
     * @param event BlockFertilizeEvent
     */
    @EventHandler
    public void onBlockFertilize(BlockFertilizeEvent event) {
        if (event.isCancelled()) return;

        Block block = event.getBlock();
        BlockData blockData = event.getBlock().getBlockData();
        if (blockData instanceof Ageable && !block.hasMetadata("fpm:fertilized")) {
            block.setMetadata("fpm:fertilized", new FixedMetadataValue(plugin, event.getPlayer()));
        }
    }
}
