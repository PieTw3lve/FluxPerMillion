package com.github.pietw3lve.fpm.listeners.world;

import java.util.Collection;
import java.util.List;

import org.bukkit.TreeType;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.world.StructureGrowEvent;

import com.github.pietw3lve.fpm.FluxPerMillion;
import com.github.pietw3lve.fpm.events.FluxLevelChangeEvent;
import com.github.pietw3lve.fpm.handlers.TreeHandler;
import com.github.pietw3lve.fpm.utils.EventActionUtil;
import com.github.pietw3lve.fpm.utils.SQLiteUtil.ActionCategory;

public class TreeGrowAction implements EventActionUtil<StructureGrowEvent> {
    
    private static final String FLUX_POINTS_TREE_GROWTH = "flux_points.tree_growth";

    private final FluxPerMillion plugin;
    private final TreeHandler treeUtils;


    public TreeGrowAction(FluxPerMillion plugin) {
        this.plugin = plugin;
        this.treeUtils = plugin.getTreeUtils();
    }

    @Override
    public boolean matches(StructureGrowEvent event) {
        TreeType species = event.getSpecies();
        return isNotTree(species);
    }

    @Override
    public void execute(StructureGrowEvent event) {
        FluxLevelChangeEvent fluxEvent = new FluxLevelChangeEvent();
        List<BlockState> tree = event.getBlocks();      

        int logCount = 0;
        for (BlockState block : tree) {
            if (treeUtils.getTreeLogs().contains(block.getType())) {
                block.removeMetadata("fpm:placed", plugin);
                logCount++;
            }
        }
        double points = logCount * plugin.getConfig().getDouble(FLUX_POINTS_TREE_GROWTH);

        if (event.getPlayer() != null) {
            fluxEvent = new FluxLevelChangeEvent(plugin.getFluxMeter(), event.getLocation(), event.getPlayer(), null, "grown", "tree", points, ActionCategory.AGRICULTURE);
            plugin.getServer().getPluginManager().callEvent(fluxEvent);
            plugin.sendDebugMessage("Player " + event.getPlayer().getName() + " grew a tree");
        } else {
            Collection<Player> players = event.getLocation().getChunk().getPlayersSeeingChunk();
            if (!players.isEmpty()) {
                for (Player player : players) {
                    double playerPoints = points / players.size();
                    fluxEvent = new FluxLevelChangeEvent(plugin.getFluxMeter(), event.getLocation(), player, null, "grown", "tree", playerPoints, ActionCategory.AGRICULTURE);
                    plugin.getServer().getPluginManager().callEvent(fluxEvent);
                }
            } else {
                fluxEvent = new FluxLevelChangeEvent(plugin.getFluxMeter(), event.getLocation(), null, null, "grown", "tree", points, ActionCategory.AGRICULTURE);
                plugin.getServer().getPluginManager().callEvent(fluxEvent);
            }  
        }
    }

    private boolean isNotTree(TreeType species) {
        return species != TreeType.BROWN_MUSHROOM && species != TreeType.RED_MUSHROOM && species != TreeType.CHORUS_PLANT && species != TreeType.WARPED_FUNGUS && species != TreeType.CRIMSON_FUNGUS;
    }
}
