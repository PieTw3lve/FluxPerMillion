package com.github.pietw3lve.fpm.listeners.block;

import org.bukkit.block.Block;
import org.bukkit.event.block.BlockBreakEvent;

import com.github.pietw3lve.fpm.FluxPerMillion;
import com.github.pietw3lve.fpm.listeners.EventAction;

public class BreakMetadataSetAction implements EventAction<BlockBreakEvent> {
    
    private final FluxPerMillion plugin;

    public BreakMetadataSetAction(FluxPerMillion plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean matches(BlockBreakEvent event) {
        return true;
    }

    @Override
    public void execute(BlockBreakEvent event) {
        Block block = event.getBlock();
        block.removeMetadata("fpm:placed", plugin);
    }
}
