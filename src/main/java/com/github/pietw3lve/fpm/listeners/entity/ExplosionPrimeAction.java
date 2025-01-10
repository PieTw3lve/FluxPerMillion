package com.github.pietw3lve.fpm.listeners.entity;

import java.util.Collection;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.ExplosionPrimeEvent;

import com.github.pietw3lve.fpm.FluxPerMillion;
import com.github.pietw3lve.fpm.events.FluxLevelChangeEvent;
import com.github.pietw3lve.fpm.utils.EventActionUtil;
import com.github.pietw3lve.fpm.utils.SQLiteUtil.ActionCategory;

public class ExplosionPrimeAction implements EventActionUtil<ExplosionPrimeEvent> {
    
    public final FluxPerMillion plugin;
    public static final String FLUX_POINTS_EXPLOSION = "flux_points.explosion";

    public ExplosionPrimeAction(FluxPerMillion plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean matches(ExplosionPrimeEvent event) {
        return true;
    }

    @Override
    public void execute(ExplosionPrimeEvent event) {
        FluxLevelChangeEvent fluxEvent = new FluxLevelChangeEvent();
        Entity explosion = event.getEntity();
        double points = plugin.getConfig().getDouble(FLUX_POINTS_EXPLOSION);

        Collection<Player> players = explosion.getLocation().getChunk().getPlayersSeeingChunk();
        if (!players.isEmpty()) {
            for (Player player : players) {
                double playerPoints = points / players.size();
                fluxEvent = new FluxLevelChangeEvent(plugin.getFluxMeter(), explosion.getLocation(), player, null, "detonated", explosion.getName().toLowerCase(), playerPoints, ActionCategory.ENERGY);
                plugin.getServer().getPluginManager().callEvent(fluxEvent);
            }
        } else {
            fluxEvent = new FluxLevelChangeEvent(plugin.getFluxMeter(), explosion.getLocation(), null, null, "detonated", explosion.getName().toLowerCase(), points, ActionCategory.ENERGY);
            plugin.getServer().getPluginManager().callEvent(fluxEvent);
        }        
    }
}
