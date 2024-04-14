package com.github.pietw3lve.fpm.listeners.world;

import org.bukkit.TreeType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.StructureGrowEvent;

import com.github.pietw3lve.fpm.FluxPerMillion;
import com.github.pietw3lve.fpm.events.FluxLevelChangeEvent;
import com.github.pietw3lve.fpm.handlers.TreeHandler;

public class StructureGrowListener implements Listener {
    
    private final FluxPerMillion plugin;
    private final TreeHandler treeUtils;

    /**
     * BlockGrowthListener Constructor.
     * @param plugin
     */
    public StructureGrowListener(FluxPerMillion plugin) {
        this.plugin = plugin;
        this.treeUtils = plugin.getTreeUtils();
    }

    /**
     * Listens for block growth events.
     * @param event StructureGrowEvent
     */
    @EventHandler
    public void onStructureGrow(StructureGrowEvent event) {
        if (event.isCancelled()) return;

        event.getBlocks().get(0).removeMetadata("fpm:placed", plugin);
        // Check if the grown structure is a tree
        FluxLevelChangeEvent fluxEvent = new FluxLevelChangeEvent();
        TreeType species = event.getSpecies();
        if (species != TreeType.BROWN_MUSHROOM && species != TreeType.RED_MUSHROOM && species != TreeType.CHORUS_PLANT) {
            long logCount = event.getBlocks().stream().filter(block -> treeUtils.getTreeLogs().contains(block.getType())).count();
            double points = logCount * plugin.getConfig().getDouble("flux_points.tree_growth", -0.25);

            if (event.getPlayer() != null) {
                Player player = event.getPlayer();
                fluxEvent = new FluxLevelChangeEvent(plugin.getFluxMeter(), player, "grown", "tree", points);
                plugin.getServer().getPluginManager().callEvent(fluxEvent);
            } else {
                fluxEvent = new FluxLevelChangeEvent(plugin.getFluxMeter(), null, "grown", "tree", points);
                plugin.getServer().getPluginManager().callEvent(fluxEvent);
            }
        }
    }
}