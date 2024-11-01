package com.github.pietw3lve.fpm.listeners.player;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import com.github.pietw3lve.fpm.FluxPerMillion;
import com.github.pietw3lve.fpm.events.FluxLevelChangeEvent;
import com.github.pietw3lve.fpm.utils.EventActionUtil;
import com.github.pietw3lve.fpm.utils.SQLiteUtil.ActionCategory;

public class ElytraBoostAction implements EventActionUtil<PlayerInteractEvent> {
    
    private static final String FLUX_POINTS_ELYTRA_BOOST = "flux_points.elytra_boost";

    private final FluxPerMillion plugin;

    public ElytraBoostAction(FluxPerMillion plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean matches(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        return usingElytra(player) && isRightClick(event.getAction()) && isBoostFuel(event.getMaterial());
    }

    @Override
    public void execute(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        double points = plugin.getConfig().getDouble(FLUX_POINTS_ELYTRA_BOOST);
        FluxLevelChangeEvent fluxEvent = new FluxLevelChangeEvent(plugin.getFluxMeter(), player.getLocation(), player, "boosted", "elytra", points, ActionCategory.POLLUTION);
        plugin.getServer().getPluginManager().callEvent(fluxEvent);
    }

    private boolean usingElytra(Player player) {
        return player.isGliding();
    }

    private boolean isRightClick(Action action) {
        return action == Action.RIGHT_CLICK_AIR;
    }

    private boolean isBoostFuel(Material type) {
        return type == Material.FIREWORK_ROCKET;
    }
}
