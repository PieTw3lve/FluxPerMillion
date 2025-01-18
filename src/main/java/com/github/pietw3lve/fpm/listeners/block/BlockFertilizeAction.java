package com.github.pietw3lve.fpm.listeners.block;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockFertilizeEvent;

import com.github.pietw3lve.fpm.FluxPerMillion;
import com.github.pietw3lve.fpm.events.FluxLevelChangeEvent;
import com.github.pietw3lve.fpm.utils.EventActionUtil;
import com.github.pietw3lve.fpm.utils.SQLiteUtil.ActionCategory;

public class BlockFertilizeAction implements EventActionUtil<BlockFertilizeEvent> {
    
    private static final String FLUX_POINTS_BLOCK_FERTILIZE = "flux_points.block_fertilize";

    private final FluxPerMillion plugin;

    public BlockFertilizeAction(FluxPerMillion plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean matches(BlockFertilizeEvent event) {
        return true;
    }

    @Override
    public void execute(BlockFertilizeEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        double points = plugin.getConfig().getDouble(FLUX_POINTS_BLOCK_FERTILIZE);
        FluxLevelChangeEvent fluxEvent = new FluxLevelChangeEvent(plugin.getFluxMeter(), block.getLocation(), player, null, "used", "bonemeal", points, ActionCategory.AGRICULTURE);
        plugin.getServer().getPluginManager().callEvent(fluxEvent);
    }
}
