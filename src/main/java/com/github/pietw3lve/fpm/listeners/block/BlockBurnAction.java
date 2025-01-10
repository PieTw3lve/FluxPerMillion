package com.github.pietw3lve.fpm.listeners.block;

import java.util.Collection;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBurnEvent;

import com.github.pietw3lve.fpm.FluxPerMillion;
import com.github.pietw3lve.fpm.events.FluxLevelChangeEvent;
import com.github.pietw3lve.fpm.utils.EventActionUtil;
import com.github.pietw3lve.fpm.utils.SQLiteUtil.ActionCategory;

public class BlockBurnAction implements EventActionUtil<BlockBurnEvent> {
    
    private static final String FLUX_POINTS_BLOCK_BURN = "flux_points.block_burn";

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
        FluxLevelChangeEvent fluxEvent = new FluxLevelChangeEvent();
        Block block = event.getBlock();
        double points = plugin.getConfig().getDouble(FLUX_POINTS_BLOCK_BURN);

        Collection<Player> players = block.getChunk().getPlayersSeeingChunk();
        if (!players.isEmpty()) {
            for (Player player : players) {
                double playerPoints = points / players.size();
                fluxEvent = new FluxLevelChangeEvent(plugin.getFluxMeter(), block.getLocation(), player, null, "burned", "block", playerPoints, ActionCategory.ENERGY);
                plugin.getServer().getPluginManager().callEvent(fluxEvent);
            }
        } else {
            fluxEvent = new FluxLevelChangeEvent(plugin.getFluxMeter(), block.getLocation(), null, null, "burned", "block", points, ActionCategory.ENERGY);
            plugin.getServer().getPluginManager().callEvent(fluxEvent);
        }
    }
}
