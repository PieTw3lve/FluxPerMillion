package com.github.pietw3lve.fpm.listeners.inventory;

import java.util.Collection;
import java.math.BigDecimal;
import java.math.RoundingMode;

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

        double points = plugin.getConfig().getDouble(FLUX_POINTS_FUEL_BURN) * (event.getBurnTime() / 200.0);
        
        Collection<Player> players = furnace.getChunk().getPlayersSeeingChunk();
        if (!players.isEmpty()) {
            for (Player player : players) {
                double playerPoints = points / players.size();
                playerPoints = BigDecimal.valueOf(playerPoints).setScale(2, RoundingMode.HALF_UP).doubleValue();
                fluxEvent = new FluxLevelChangeEvent(plugin.getFluxMeter(), furnace.getLocation(), player, null, "burned", "fuel", playerPoints, ActionCategory.ENERGY);
                plugin.getServer().getPluginManager().callEvent(fluxEvent);
            }
        } else {
            fluxEvent = new FluxLevelChangeEvent(plugin.getFluxMeter(), furnace.getLocation(), null, null, "burned", "fuel", points, ActionCategory.ENERGY);
            plugin.getServer().getPluginManager().callEvent(fluxEvent);
        }
    }
}
