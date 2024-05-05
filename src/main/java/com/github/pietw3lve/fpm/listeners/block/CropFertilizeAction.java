package com.github.pietw3lve.fpm.listeners.block;

import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockFertilizeEvent;
import org.bukkit.metadata.FixedMetadataValue;

import com.github.pietw3lve.fpm.FluxPerMillion;
import com.github.pietw3lve.fpm.listeners.EventAction;

public class CropFertilizeAction implements EventAction<BlockFertilizeEvent> {
    
    private final FluxPerMillion plugin;

    public CropFertilizeAction(FluxPerMillion plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean matches(BlockFertilizeEvent event) {
        Block block = event.getBlock();
        BlockData blockData = event.getBlock().getBlockData();
        return blockData instanceof Ageable && !block.hasMetadata("fpm:fertilized");
    }

    @Override
    public void execute(BlockFertilizeEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        block.setMetadata("fpm:fertilized", new FixedMetadataValue(plugin, player));
    }
}
