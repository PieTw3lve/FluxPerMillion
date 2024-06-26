package com.github.pietw3lve.fpm.listeners.world;

import org.bukkit.TreeType;
import org.bukkit.entity.Player;
import org.bukkit.event.world.StructureGrowEvent;

import com.github.pietw3lve.fpm.FluxPerMillion;
import com.github.pietw3lve.fpm.events.FluxLevelChangeEvent;
import com.github.pietw3lve.fpm.handlers.TreeHandler;
import com.github.pietw3lve.fpm.listeners.EventAction;

public class TreeGrowAction implements EventAction<StructureGrowEvent> {
    
    private static final String FLUX_POINTS_TREE_GROWTH = "flux_points.tree_growth";
    private static final double DEFAULT_FLUX_POINTS_TREE_GROWTH = -0.25;

    private final FluxPerMillion plugin;
    private final TreeHandler treeUtils;

    public TreeGrowAction(FluxPerMillion plugin) {
        this.plugin = plugin;
        this.treeUtils = plugin.getTreeUtils();
    }

    @Override
    public boolean matches(StructureGrowEvent event) {
        TreeType species = event.getSpecies();
        return species != TreeType.BROWN_MUSHROOM && species != TreeType.RED_MUSHROOM && species != TreeType.CHORUS_PLANT;
    }

    @Override
    public void execute(StructureGrowEvent event) {
        FluxLevelChangeEvent fluxEvent = new FluxLevelChangeEvent();
        long logCount = event.getBlocks().stream().filter(block -> treeUtils.getTreeLogs().contains(block.getType())).count();
        double points = logCount * plugin.getConfig().getDouble(FLUX_POINTS_TREE_GROWTH, DEFAULT_FLUX_POINTS_TREE_GROWTH);

        if (event.getPlayer() != null) {
            Player player = event.getPlayer();
            fluxEvent = new FluxLevelChangeEvent(plugin.getFluxMeter(), event.getLocation(), player, "grown", "tree", points);
        } else {
            fluxEvent = new FluxLevelChangeEvent(plugin.getFluxMeter(), event.getLocation(), null, "grown", "tree", points);
        }

        plugin.getServer().getPluginManager().callEvent(fluxEvent);
    }
}
