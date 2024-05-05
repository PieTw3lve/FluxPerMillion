package com.github.pietw3lve.fpm.listeners.block;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;

import com.github.pietw3lve.fpm.FluxPerMillion;
import com.github.pietw3lve.fpm.events.FluxLevelChangeEvent;
import com.github.pietw3lve.fpm.listeners.EventAction;

public class CoalBreakAction implements EventAction<BlockBreakEvent> {
    
    private static final String FLUX_POINTS_COAL_BREAK = "flux_points.coal_break";
    private static final double DEFAULT_FLUX_POINTS_COAL_BREAK = -0.25;

    private final FluxPerMillion plugin;

    public CoalBreakAction(FluxPerMillion plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean matches(BlockBreakEvent event) {
        Block block = event.getBlock();
        return isCoalOre(block) || isCoalBlock(block);
    }

    @Override
    public void execute(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        String blockName = block.getType().toString().replace("_", " ").toLowerCase();
        double points;
        
        if (isCoalOre(block)) {
            points = plugin.getConfig().getDouble(FLUX_POINTS_COAL_BREAK, DEFAULT_FLUX_POINTS_COAL_BREAK);
        } else {
            points = plugin.getConfig().getDouble(FLUX_POINTS_COAL_BREAK, DEFAULT_FLUX_POINTS_COAL_BREAK) * 9;
        }

        FluxLevelChangeEvent fluxEvent = new FluxLevelChangeEvent(plugin.getFluxMeter(), block.getLocation(), player, "removed", blockName, points);
        plugin.getServer().getPluginManager().callEvent(fluxEvent);
    }

    private boolean isCoalOre(Block block) {
        return block.getType() == Material.COAL_ORE || block.getType() == Material.DEEPSLATE_COAL_ORE;
    }

    private boolean isCoalBlock(Block block) {
        return block.getType() == Material.COAL_BLOCK;
    }   
}
