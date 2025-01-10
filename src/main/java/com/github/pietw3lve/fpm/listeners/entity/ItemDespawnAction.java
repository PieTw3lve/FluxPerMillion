package com.github.pietw3lve.fpm.listeners.entity;

import java.util.Collection;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.ItemDespawnEvent;

import com.github.pietw3lve.fpm.FluxPerMillion;
import com.github.pietw3lve.fpm.events.FluxLevelChangeEvent;
import com.github.pietw3lve.fpm.utils.EventActionUtil;
import com.github.pietw3lve.fpm.utils.SQLiteUtil.ActionCategory;

public class ItemDespawnAction implements EventActionUtil<ItemDespawnEvent> {
    
    private static final String FLUX_POINTS_POLLUTION = "flux_points.pollution";

    private final FluxPerMillion plugin;

    public ItemDespawnAction(FluxPerMillion plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean matches(ItemDespawnEvent event) {
        return true;
    }

    @Override
    public void execute(ItemDespawnEvent event) {
        FluxLevelChangeEvent fluxEvent = new FluxLevelChangeEvent();
        Entity item = event.getEntity();
        double points = plugin.getConfig().getDouble(FLUX_POINTS_POLLUTION);

        Collection<Player> players = item.getLocation().getChunk().getPlayersSeeingChunk();
        if (!players.isEmpty()) {
            for (Player player : players) {
                double playerPoints = points / players.size();
                fluxEvent = new FluxLevelChangeEvent(plugin.getFluxMeter(), item.getLocation(), player, null, "despawned", "item", playerPoints, ActionCategory.WASTE);
                plugin.getServer().getPluginManager().callEvent(fluxEvent);
            }
        } else {
            fluxEvent = new FluxLevelChangeEvent(plugin.getFluxMeter(), item.getLocation(), null, null, "despawned", "item", points, ActionCategory.WASTE);
            plugin.getServer().getPluginManager().callEvent(fluxEvent);
        }  
    }
}
