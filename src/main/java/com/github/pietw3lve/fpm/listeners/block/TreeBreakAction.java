package com.github.pietw3lve.fpm.listeners.block;

import java.util.Set;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;

import com.github.pietw3lve.fpm.FluxPerMillion;
import com.github.pietw3lve.fpm.events.FluxLevelChangeEvent;
import com.github.pietw3lve.fpm.handlers.TreeHandler;
import com.github.pietw3lve.fpm.listeners.EventAction;

public class TreeBreakAction implements EventAction<BlockBreakEvent> {
    
    private static final String FLUX_POINTS_TREE_BREAK = "flux_points.tree_cut";
    private static final String DEBUG_TREE_FELLER = "debug.tree_feller";
    private static final double DEFAULT_FLUX_POINTS_TREE_BREAK = 0.25;
    private static final boolean DEFAULT_DEBUG_TREE_FELLER = false;

    private final FluxPerMillion plugin;
    private final TreeHandler treeUtils;

    public TreeBreakAction(FluxPerMillion plugin) {
        this.plugin = plugin;
        this.treeUtils = plugin.getTreeUtils();
    }
    
    @Override
    public boolean matches(BlockBreakEvent event) {
        Block block = event.getBlock();
        return isTreeLog(block) && isTreeAlive(block);
    }

    @Override
    public void execute(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        Set<Block> tree = treeUtils.getLiveTree(block);
        if (!tree.isEmpty()) {
            double points = plugin.getConfig().getDouble(FLUX_POINTS_TREE_BREAK, DEFAULT_FLUX_POINTS_TREE_BREAK) * treeUtils.getTreeLogsCount(tree);
            FluxLevelChangeEvent fluxEvent = new FluxLevelChangeEvent(plugin.getFluxMeter(), block.getLocation(), player, "cut", "tree", points);
            plugin.getServer().getPluginManager().callEvent(fluxEvent);
            if (plugin.getConfig().getBoolean(DEBUG_TREE_FELLER, DEFAULT_DEBUG_TREE_FELLER)) treeUtils.breakTree(tree);
        }
    }

    private boolean isTreeLog(Block block) {
        return treeUtils.isTreeLog(block) || treeUtils.isStrippedTreeLog(block);
    }

    private boolean isTreeAlive(Block block) {
        return (!block.hasMetadata("fpm:placed") || block.hasMetadata("fpm:stripped")) && !block.hasMetadata("fpm:tree_dead");
    }
}
