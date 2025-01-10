package com.github.pietw3lve.fpm.listeners.block;

import java.util.Collection;

import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockGrowEvent;

import com.github.pietw3lve.fpm.FluxPerMillion;
import com.github.pietw3lve.fpm.events.FluxLevelChangeEvent;
import com.github.pietw3lve.fpm.utils.EventActionUtil;
import com.github.pietw3lve.fpm.utils.SQLiteUtil.ActionCategory;

public class CropMaxAgeAction implements EventActionUtil<BlockGrowEvent> {
    
    private static final String FLUX_POINTS_CROP_GROWTH = "flux_points.crop_growth";

    private final FluxPerMillion plugin;

    public CropMaxAgeAction(FluxPerMillion plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean matches(BlockGrowEvent event) {
        BlockData blockData = event.getNewState().getBlockData();
        return isCrop(blockData) && isMaxAge((Ageable) blockData);
    }

    @Override
    public void execute(BlockGrowEvent event) {
        FluxLevelChangeEvent fluxEvent = new FluxLevelChangeEvent();
        Block crop = event.getBlock();
        double points = plugin.getConfig().getDouble(FLUX_POINTS_CROP_GROWTH);

        if (crop.hasMetadata("fpm:fertilized")) {
            Player player = (Player) crop.getMetadata("fpm:fertilized").get(0).value();
            fluxEvent = new FluxLevelChangeEvent(plugin.getFluxMeter(), crop.getLocation(), player, null, "grown", "crop", points, ActionCategory.AGRICULTURE);
            plugin.getServer().getPluginManager().callEvent(fluxEvent);
        } else {
            Collection<Player> players = crop.getChunk().getPlayersSeeingChunk();
            if (!players.isEmpty()) {
                for (Player player : players) {
                    double playerPoints = points / players.size();
                    fluxEvent = new FluxLevelChangeEvent(plugin.getFluxMeter(), crop.getLocation(), player, null, "grown", "crop", playerPoints, ActionCategory.AGRICULTURE);
                    plugin.getServer().getPluginManager().callEvent(fluxEvent);
                }
            } else {
                fluxEvent = new FluxLevelChangeEvent(plugin.getFluxMeter(), crop.getLocation(), null, null, "grown", "crop", points, ActionCategory.AGRICULTURE);
                plugin.getServer().getPluginManager().callEvent(fluxEvent);
            }
        }
    }

    private boolean isCrop(BlockData blockData) {
        return blockData instanceof Ageable;
    }

    private boolean isMaxAge(Ageable ageable) {
        return ageable.getAge() == ageable.getMaximumAge();
    }
}
