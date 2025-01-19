package com.github.pietw3lve.fpm.listeners.block;

import java.util.Set;

import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Leaves;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;

import com.github.pietw3lve.fpm.FluxPerMillion;
import com.github.pietw3lve.fpm.events.FluxLevelChangeEvent;
import com.github.pietw3lve.fpm.utils.EventActionUtil;
import com.github.pietw3lve.fpm.utils.SQLiteUtil.ActionCategory;

public class LeavesBreakAction implements EventActionUtil<BlockBreakEvent> {
    
    private static final String FLUX_POINTS_LEAVES_BREAK = "flux_points.leaves_break";

    private final FluxPerMillion plugin;
    private final Set<Material> leaves;


    public LeavesBreakAction(FluxPerMillion plugin) {
        this.plugin = plugin;
        this.leaves = Tag.LEAVES.getValues();
    }
    
    @Override
    public boolean matches(BlockBreakEvent event) {
        Block block = event.getBlock();
        return leaves.contains(block.getType()) && ((Leaves) block.getBlockData()).isPersistent();
    }

    @Override
    public void execute(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        String blockName = block.getType().toString().replace("_", " ").toLowerCase();
        double points = plugin.getConfig().getDouble(FLUX_POINTS_LEAVES_BREAK);
        FluxLevelChangeEvent fluxEvent = new FluxLevelChangeEvent(plugin.getFluxMeter(), block.getLocation(), player, null, "removed", blockName, points, ActionCategory.AGRICULTURE);
        plugin.getServer().getPluginManager().callEvent(fluxEvent);
    }
}
