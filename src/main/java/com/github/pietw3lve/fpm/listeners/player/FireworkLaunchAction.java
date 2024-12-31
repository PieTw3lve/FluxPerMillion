package com.github.pietw3lve.fpm.listeners.player;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import com.github.pietw3lve.fpm.FluxPerMillion;
import com.github.pietw3lve.fpm.events.FluxLevelChangeEvent;
import com.github.pietw3lve.fpm.utils.EventActionUtil;
import com.github.pietw3lve.fpm.utils.SQLiteUtil.ActionCategory;

public class FireworkLaunchAction implements EventActionUtil<PlayerInteractEvent> {
    
    private static final String FLUX_POINTS_FIREWORK_LAUNCH = "flux_points.firework_launch";

    private final FluxPerMillion plugin;

    public FireworkLaunchAction(FluxPerMillion plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean matches(PlayerInteractEvent event) {
        Action action = event.getAction();
        Material item = event.getMaterial();
        return isRightClick(action) && isFirework(item);
    }

    @Override
    public void execute(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        double points = plugin.getConfig().getDouble(FLUX_POINTS_FIREWORK_LAUNCH);
        FluxLevelChangeEvent fluxEvent = new FluxLevelChangeEvent(plugin.getFluxMeter(), player.getLocation(), player, null, "used", "firework", points, ActionCategory.ENERGY);
        plugin.getServer().getPluginManager().callEvent(fluxEvent);
    }

    private boolean isRightClick(Action action) {
        return action == Action.RIGHT_CLICK_BLOCK;
    }

    private boolean isFirework(Material type) {
        return type == Material.FIREWORK_ROCKET;
    }
}
