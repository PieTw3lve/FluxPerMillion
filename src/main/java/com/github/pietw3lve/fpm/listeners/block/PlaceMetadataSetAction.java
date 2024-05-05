package com.github.pietw3lve.fpm.listeners.block;

import org.bukkit.block.Block;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.metadata.FixedMetadataValue;

import com.github.pietw3lve.fpm.FluxPerMillion;
import com.github.pietw3lve.fpm.listeners.EventAction;

public class PlaceMetadataSetAction implements EventAction<BlockPlaceEvent> {
    
    private final FluxPerMillion plugin;

    public PlaceMetadataSetAction(FluxPerMillion plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean matches(BlockPlaceEvent event) {
        return true;
    }

    @Override
    public void execute(BlockPlaceEvent event) {
        Block block = event.getBlock();
        block.setMetadata("fpm:placed", new FixedMetadataValue(plugin, true));
    }
}
