package com.github.pietw3lve.fpm.listeners.block;

import java.util.Collection;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockSpreadEvent;

import com.github.pietw3lve.fpm.FluxPerMillion;
import com.github.pietw3lve.fpm.events.FluxLevelChangeEvent;
import com.github.pietw3lve.fpm.utils.EventActionUtil;
import com.github.pietw3lve.fpm.utils.SQLiteUtil.ActionCategory;

public class GrassSpreadAction implements EventActionUtil<BlockSpreadEvent> {
    
    private static final String FLUX_POINTS_GRASS_GROWTH = "flux_points.grass_growth";

    private final FluxPerMillion plugin;

    public GrassSpreadAction(FluxPerMillion plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean matches(BlockSpreadEvent event) {
        Block source = event.getSource();
        return isGrass(source);
    }

    @Override
    public void execute(BlockSpreadEvent event) {
        FluxLevelChangeEvent fluxEvent = new FluxLevelChangeEvent();
        Block grass = event.getBlock();
        double points = plugin.getConfig().getDouble(FLUX_POINTS_GRASS_GROWTH);

        Collection<Player> players = grass.getChunk().getPlayersSeeingChunk();
        if (!players.isEmpty()) {
            for (Player player : players) {
                double playerPoints = points / players.size();
                fluxEvent = new FluxLevelChangeEvent(plugin.getFluxMeter(), grass.getLocation(), player, null, "grown", "grass", playerPoints, ActionCategory.AGRICULTURE);
                plugin.getServer().getPluginManager().callEvent(fluxEvent);
            }
        } else {
            fluxEvent = new FluxLevelChangeEvent(plugin.getFluxMeter(), grass.getLocation(), null, null, "grown", "grass", points, ActionCategory.AGRICULTURE);
            plugin.getServer().getPluginManager().callEvent(fluxEvent);
        }
    }

    private boolean isGrass(Block block) {
        return block.getType() == Material.GRASS_BLOCK;
    }
}
