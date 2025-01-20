package com.github.pietw3lve.fpm.listeners.block;

import java.util.Set;

import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;

import com.github.pietw3lve.fpm.FluxPerMillion;
import com.github.pietw3lve.fpm.events.FluxLevelChangeEvent;
import com.github.pietw3lve.fpm.utils.EventActionUtil;
import com.github.pietw3lve.fpm.utils.SQLiteUtil.ActionCategory;

public class FirePlaceAction implements EventActionUtil<BlockPlaceEvent> {
    
    private static final String FLUX_POINTS_FIRE_PLACE = "flux_points.flint_and_steel";

    private final FluxPerMillion plugin;
    private final Set<Material> fire;

    public FirePlaceAction(FluxPerMillion plugin) {
        this.plugin = plugin;
        this.fire = Tag.FIRE.getValues();
    }

    @Override
    public boolean matches(BlockPlaceEvent event) {
        Block block = event.getBlock();
        return fire.contains(block.getType());
    }

    @Override
    public void execute(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        double points = plugin.getConfig().getDouble(FLUX_POINTS_FIRE_PLACE);
        FluxLevelChangeEvent fluxEvent = new FluxLevelChangeEvent(plugin.getFluxMeter(), block.getLocation(), player, null, "used", "flint and steel", points, ActionCategory.ENERGY);
        plugin.getServer().getPluginManager().callEvent(fluxEvent);
    }
}
