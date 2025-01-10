package com.github.pietw3lve.fpm.listeners.inventory;

import java.util.Collection;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.FurnaceBurnEvent;

import com.github.pietw3lve.fpm.FluxPerMillion;
import com.github.pietw3lve.fpm.events.FluxLevelChangeEvent;
import com.github.pietw3lve.fpm.utils.EventActionUtil;
import com.github.pietw3lve.fpm.utils.SQLiteUtil.ActionCategory;

public class FuelSmeltAction implements EventActionUtil<FurnaceBurnEvent> {
    
    private static final String FLUX_POINTS_FUEL_BURN = "flux_points.fuel_burn";

    private final FluxPerMillion plugin;

    public FuelSmeltAction(FluxPerMillion plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean matches(FurnaceBurnEvent event) {
        return true;
    }

    @Override
    public void execute(FurnaceBurnEvent event) {
        FluxLevelChangeEvent fluxEvent = new FluxLevelChangeEvent();
        Block furnace = event.getBlock();

        double multiplier = getMultiplier(event.getFuel().getType());
        double points = plugin.getConfig().getDouble(FLUX_POINTS_FUEL_BURN) * (event.getBurnTime() / 200.0) * multiplier;
        
        Collection<Player> players = furnace.getChunk().getPlayersSeeingChunk();
        if (!players.isEmpty()) {
            for (Player player : players) {
                double playerPoints = points / players.size();
                fluxEvent = new FluxLevelChangeEvent(plugin.getFluxMeter(), furnace.getLocation(), player, null, "burned", "fuel", playerPoints, ActionCategory.ENERGY);
                plugin.getServer().getPluginManager().callEvent(fluxEvent);
            }
        } else {
            fluxEvent = new FluxLevelChangeEvent(plugin.getFluxMeter(), furnace.getLocation(), null, null, "burned", "fuel", points, ActionCategory.ENERGY);
            plugin.getServer().getPluginManager().callEvent(fluxEvent);
        }
    }

    private double getMultiplier(Material material) {
        switch (material) {
            case BLAZE_ROD:
                return 0.1;
            case LAVA_BUCKET:
                return 0.4;
            case BAMBOO:
                return 0.8;
            case DRIED_KELP_BLOCK:
                return 0.8;
            case OAK_LOG:
                return 0.8;
            case STRIPPED_OAK_LOG:
                return 0.8;
            case OAK_WOOD:
                return 0.8;
            case OAK_SAPLING:
                return 0.8;
            case SPRUCE_LOG:
                return 0.8;
            case STRIPPED_SPRUCE_LOG:
                return 0.8;
            case SPRUCE_WOOD:
                return 0.8;
            case SPRUCE_SAPLING:
                return 0.8;
            case BIRCH_LOG:
                return 0.8;
            case STRIPPED_BIRCH_LOG:
                return 0.8;
            case BIRCH_WOOD:
                return 0.8;
            case BIRCH_SAPLING:
                return 0.8;
            case JUNGLE_LOG:
                return 0.8;
            case STRIPPED_JUNGLE_LOG:
                return 0.8;
            case JUNGLE_WOOD:
                return 0.8;
            case JUNGLE_SAPLING:
                return 0.8;
            case ACACIA_LOG:
                return 0.8;
            case STRIPPED_ACACIA_LOG:
                return 0.8;
            case ACACIA_WOOD:
                return 0.8;
            case ACACIA_SAPLING:
                return 0.8;
            case DARK_OAK_LOG:
                return 0.8;
            case STRIPPED_DARK_OAK_LOG:
                return 0.8;
            case DARK_OAK_WOOD:
                return 0.8;
            case DARK_OAK_SAPLING:
                return 0.8;
            case CHERRY_LOG:
                return 0.8;
            case STRIPPED_CHERRY_LOG:
                return 0.8;
            case CHERRY_WOOD:
                return 0.8;
            case CHERRY_SAPLING:
                return 0.8;
            case MANGROVE_LOG:
                return 0.8;
            case STRIPPED_MANGROVE_LOG:
                return 0.8;
            case MANGROVE_WOOD:
                return 0.8;
            case MANGROVE_ROOTS:
                return 0.8;
            case MANGROVE_PROPAGULE:
                return 0.8;
            case CHARCOAL:
                return 0.8;
            case COAL:
                return 2.5;
            case COAL_BLOCK:
                return 2.5;
            default:
                return 1.0;
        }
    }
}
