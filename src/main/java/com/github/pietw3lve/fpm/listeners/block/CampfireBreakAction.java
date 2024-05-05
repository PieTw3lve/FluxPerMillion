package com.github.pietw3lve.fpm.listeners.block;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;

import com.github.pietw3lve.fpm.FluxPerMillion;
import com.github.pietw3lve.fpm.events.FluxLevelChangeEvent;
import com.github.pietw3lve.fpm.listeners.EventAction;

public class CampfireBreakAction implements EventAction<BlockBreakEvent> {
    
    private static final String FLUX_POINTS_CAMPFIRE_BREAK = "flux_points.campfire_break";
    private static final double DEFAULT_FLUX_POINTS_CAMPFIRE_BREAK = -0.5;

    private final FluxPerMillion plugin;

    public CampfireBreakAction(FluxPerMillion plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean matches(BlockBreakEvent event) {
        Block block = event.getBlock();
        return isCampfire(block);
    }

    @Override
    public void execute(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        String blockName = block.getType().toString().replace("_", " ").toLowerCase();
        double points = plugin.getConfig().getDouble(FLUX_POINTS_CAMPFIRE_BREAK, DEFAULT_FLUX_POINTS_CAMPFIRE_BREAK);
        FluxLevelChangeEvent fluxEvent = new FluxLevelChangeEvent(plugin.getFluxMeter(), block.getLocation(), player, "removed", blockName, points);
        plugin.getServer().getPluginManager().callEvent(fluxEvent);
    }

    private boolean isCampfire(Block block) {
        Material blockType = block.getType();
        return blockType == Material.CAMPFIRE || blockType == Material.SOUL_CAMPFIRE;
    }
}
