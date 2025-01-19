package com.github.pietw3lve.fpm.listeners.block;

import java.util.Set;

import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockMultiPlaceEvent;

import com.github.pietw3lve.fpm.FluxPerMillion;
import com.github.pietw3lve.fpm.events.FluxLevelChangeEvent;
import com.github.pietw3lve.fpm.utils.EventActionUtil;
import com.github.pietw3lve.fpm.utils.SQLiteUtil.ActionCategory;

public class TallFlowerPlaceAction implements EventActionUtil<BlockMultiPlaceEvent> {
    
    private static final String FLUX_POINTS_FLOWER_PLACE = "flux_points.flower_place";

    private final FluxPerMillion plugin;
    private final Set<Material> flowers;

    public TallFlowerPlaceAction(FluxPerMillion plugin) {
        this.plugin = plugin;
        this.flowers = Tag.TALL_FLOWERS.getValues();
    }
    
    @Override
    public boolean matches(BlockMultiPlaceEvent event) {
        Block block = event.getBlock();
        return flowers.contains(block.getType());
    }

    @Override
    public void execute(BlockMultiPlaceEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        String blockName = block.getType().toString().replace("_", " ").toLowerCase();
        double points = plugin.getConfig().getDouble(FLUX_POINTS_FLOWER_PLACE) / 2;
        FluxLevelChangeEvent fluxEvent = new FluxLevelChangeEvent(plugin.getFluxMeter(), block.getLocation(), player, null, "placed", blockName, points, ActionCategory.AGRICULTURE);
        plugin.getServer().getPluginManager().callEvent(fluxEvent);
    }
}
