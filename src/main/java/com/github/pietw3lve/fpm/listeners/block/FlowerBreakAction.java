package com.github.pietw3lve.fpm.listeners.block;

import java.util.Set;

import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;

import com.github.pietw3lve.fpm.FluxPerMillion;
import com.github.pietw3lve.fpm.events.FluxLevelChangeEvent;
import com.github.pietw3lve.fpm.utils.EventActionUtil;
import com.github.pietw3lve.fpm.utils.SQLiteUtil.ActionCategory;

public class FlowerBreakAction implements EventActionUtil<BlockBreakEvent> {
    
    private static final String FLUX_POINTS_FLOWER_BREAK = "flux_points.flower_break";

    private final FluxPerMillion plugin;
    private final Set<Material> flowers;
    private final Set<Material> tallFlowers;

    public FlowerBreakAction(FluxPerMillion plugin) {
        this.plugin = plugin;
        this.flowers = Tag.FLOWERS.getValues();
        this.tallFlowers = Tag.TALL_FLOWERS.getValues();
    }
    
    @Override
    public boolean matches(BlockBreakEvent event) {
        Block block = event.getBlock();
        return flowers.contains(block.getType());
    }

    @Override
    public void execute(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        String blockName = block.getType().toString().replace("_", " ").toLowerCase();
        double points = tallFlowers.contains(block.getType()) 
            ? plugin.getConfig().getDouble(FLUX_POINTS_FLOWER_BREAK)
            : plugin.getConfig().getDouble(FLUX_POINTS_FLOWER_BREAK) * 2;
        FluxLevelChangeEvent fluxEvent = new FluxLevelChangeEvent(plugin.getFluxMeter(), block.getLocation(), player, null, "removed", blockName, points, ActionCategory.AGRICULTURE);
        plugin.getServer().getPluginManager().callEvent(fluxEvent);
    }
}
