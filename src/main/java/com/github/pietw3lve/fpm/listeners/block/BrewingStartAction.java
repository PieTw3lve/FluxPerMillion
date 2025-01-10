package com.github.pietw3lve.fpm.listeners.block;

import java.util.Collection;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BrewingStartEvent;

import com.github.pietw3lve.fpm.FluxPerMillion;
import com.github.pietw3lve.fpm.events.FluxLevelChangeEvent;
import com.github.pietw3lve.fpm.utils.EventActionUtil;
import com.github.pietw3lve.fpm.utils.SQLiteUtil.ActionCategory;

public class BrewingStartAction implements EventActionUtil<BrewingStartEvent> {
    
    private static final String FLUX_POINTS_BREWING_START = "flux_points.brew_potion";

    private final FluxPerMillion plugin;

    public BrewingStartAction(FluxPerMillion plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean matches(BrewingStartEvent event) {
        return true;
    }

    @Override
    public void execute(BrewingStartEvent event) {
        FluxLevelChangeEvent fluxEvent = new FluxLevelChangeEvent();
        Block brewingStand = event.getBlock();
        double points = plugin.getConfig().getDouble(FLUX_POINTS_BREWING_START) * (event.getTotalBrewTime() / 200.0);

        Collection<Player> players = brewingStand.getChunk().getPlayersSeeingChunk();
        if (!players.isEmpty()) {
            for (Player player : players) {
                double playerPoints = points / players.size();
                fluxEvent = new FluxLevelChangeEvent(plugin.getFluxMeter(), brewingStand.getLocation(), player, null, "brewed", "potion", playerPoints, ActionCategory.ENERGY);
                plugin.getServer().getPluginManager().callEvent(fluxEvent);
            }
        } else {
            fluxEvent = new FluxLevelChangeEvent(plugin.getFluxMeter(), brewingStand.getLocation(), null, null, "brewed", "potion", points, ActionCategory.ENERGY);
            plugin.getServer().getPluginManager().callEvent(fluxEvent);
        }
    }
}
