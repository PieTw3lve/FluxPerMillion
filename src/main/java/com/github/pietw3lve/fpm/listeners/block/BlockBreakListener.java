package com.github.pietw3lve.fpm.listeners.block;

import java.util.Set;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import com.github.pietw3lve.fpm.FluxPerMillion;
import com.github.pietw3lve.fpm.events.FluxLevelChangeEvent;
import com.github.pietw3lve.fpm.handlers.TreeHandler;

public class BlockBreakListener implements Listener{
    
    private final FluxPerMillion plugin;
    private TreeHandler treeUtils;

    /**
     * BlockBreakListener Constructor.
     * @param plugin
     */
    public BlockBreakListener(FluxPerMillion plugin) {
        this.plugin = plugin;
        this.treeUtils = plugin.getTreeUtils();
    }

    /**
     * Listens for block break events.
     * @param event BlockBreakEvent
     */
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.isCancelled()) return;

        FluxLevelChangeEvent fluxEvent = new FluxLevelChangeEvent();
        Player player = event.getPlayer();
        Block block = event.getBlock();
        double points;

        switch (block.getType()) {
            case COAL_BLOCK:
                points = plugin.getConfig().getDouble("flux_points.coal_break", -0.25) * 9;
                fluxEvent = new FluxLevelChangeEvent(plugin.getFluxMeter(), player, "removed", "coal block", points);
                break;
            case COAL_ORE:
                points = plugin.getConfig().getDouble("flux_points.coal_break", -0.25);
                fluxEvent = new FluxLevelChangeEvent(plugin.getFluxMeter(), player, "removed", "coal ore", points);
                break;
            case DEEPSLATE_COAL_ORE:
                points = plugin.getConfig().getDouble("flux_points.coal_break", -0.25);
                fluxEvent = new FluxLevelChangeEvent(plugin.getFluxMeter(), player, "removed", "deepslate coal ore", points);
                break;
            case CAMPFIRE:
                points = plugin.getConfig().getDouble("flux_points.campfire_break", -0.5);
                fluxEvent = new FluxLevelChangeEvent(plugin.getFluxMeter(), player, "removed", "campfire", points);
                break;
            case SOUL_CAMPFIRE:
                points = plugin.getConfig().getDouble("flux_points.campfire_break", -0.5);
                fluxEvent = new FluxLevelChangeEvent(plugin.getFluxMeter(), player, "removed", "soul campfire", points);
                break;
            case TORCH:
                points = plugin.getConfig().getDouble("flux_points.torch_break", -0.25);
                fluxEvent = new FluxLevelChangeEvent(plugin.getFluxMeter(), player, "removed", "torch", points);
                break;
            case SOUL_TORCH:
                points = plugin.getConfig().getDouble("flux_points.torch_break", -0.25);
                fluxEvent = new FluxLevelChangeEvent(plugin.getFluxMeter(), player, "removed", "soul torch", points);
                break;
            default:
                // Check if player cut a tree
                if ((!block.hasMetadata("fpm:placed") || block.hasMetadata("fpm:stripped")) && !block.hasMetadata("fpm:tree_dead") && (treeUtils.isTreeLog(block) || treeUtils.isStrippedTreeLog(block))) {
                    Set<Block> tree = treeUtils.getLiveTree(block);
                    if (!tree.isEmpty()) {
                        points = plugin.getConfig().getDouble("flux_points.tree_cut", 0.25) * treeUtils.getTreeLogsCount(tree);
                        fluxEvent = new FluxLevelChangeEvent(plugin.getFluxMeter(), player, "cut", "tree", points);
                        if (plugin.getConfig().getBoolean("debug.tree_feller", false)) treeUtils.breakTree(tree);
                    }
                } 
                break;
        }
        plugin.getServer().getPluginManager().callEvent(fluxEvent);
        block.removeMetadata("fpm:placed", plugin);
    }
}
