package com.github.pietw3lve.fpm.listeners.block;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;

import com.github.pietw3lve.fpm.FluxPerMillion;
import com.github.pietw3lve.fpm.events.FluxLevelChangeEvent;
import com.github.pietw3lve.fpm.utils.EventActionUtil;
import com.github.pietw3lve.fpm.utils.SQLiteUtil.ActionCategory;

public class CampfirePlaceAction implements EventActionUtil<BlockPlaceEvent> {
    
    private static final String FLUX_POINTS_CAMPFIRE_PLACE = "flux_points.campfire_place";

    private final FluxPerMillion plugin;

    public CampfirePlaceAction(FluxPerMillion plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean matches(BlockPlaceEvent event) {
        Block block = event.getBlock();
        return isCampfire(block);
    }

    @Override
    public void execute(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        String blockName = block.getType().toString().replace("_", " ").toLowerCase();
        double points = plugin.getConfig().getDouble(FLUX_POINTS_CAMPFIRE_PLACE);
        FluxLevelChangeEvent fluxEvent = new FluxLevelChangeEvent(plugin.getFluxMeter(), block.getLocation(), player, "placed", blockName, points, ActionCategory.ENERGY);
        plugin.getServer().getPluginManager().callEvent(fluxEvent);
    }

    private boolean isCampfire(Block block) {
        Material blockType = block.getType();
        return blockType == Material.CAMPFIRE || blockType == Material.SOUL_CAMPFIRE;
    }
}
