package com.github.pietw3lve.fpm.listeners.block;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;

import com.github.pietw3lve.fpm.FluxPerMillion;
import com.github.pietw3lve.fpm.events.FluxLevelChangeEvent;
import com.github.pietw3lve.fpm.listeners.EventAction;

public class FirePlaceAction implements EventAction<BlockPlaceEvent> {
    
    private static final String FLUX_POINTS_FIRE_PLACE = "flux_points.flint_and_steel_use";

    private final FluxPerMillion plugin;

    public FirePlaceAction(FluxPerMillion plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean matches(BlockPlaceEvent event) {
        Block block = event.getBlock();
        return isFire(block);
    }

    @Override
    public void execute(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        double points = plugin.getConfig().getDouble(FLUX_POINTS_FIRE_PLACE);
        FluxLevelChangeEvent fluxEvent = new FluxLevelChangeEvent(plugin.getFluxMeter(), block.getLocation(), player, "used", "flint and steel", points);
        plugin.getServer().getPluginManager().callEvent(fluxEvent);
    }

    private boolean isFire(Block block) {
        Material blockType = block.getType();
        return blockType == Material.FIRE || blockType == Material.SOUL_FIRE;
    }
}
