package com.github.pietw3lve.fpm.listeners.player;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import com.github.pietw3lve.fpm.FluxPerMillion;
import com.github.pietw3lve.fpm.events.FluxLevelChangeEvent;
import com.github.pietw3lve.fpm.listeners.EventAction;

public class FireworkLaunchAction implements EventAction<PlayerInteractEvent> {
    
    private static final String FLUX_POINTS_FIREWORK_LAUNCH = "flux_points.firework_launch";
    private static final double DEFAULT_FLUX_POINTS_FIREWORK_LAUNCH = 0.25;

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
        double points = plugin.getConfig().getDouble(FLUX_POINTS_FIREWORK_LAUNCH, DEFAULT_FLUX_POINTS_FIREWORK_LAUNCH);
        FluxLevelChangeEvent fluxEvent = new FluxLevelChangeEvent(plugin.getFluxMeter(), player.getLocation(), player, "used", "firework", points);
        plugin.getServer().getPluginManager().callEvent(fluxEvent);
    }

    private boolean isRightClick(Action action) {
        return action == Action.RIGHT_CLICK_BLOCK;
    }

    private boolean isFirework(Material type) {
        return type == Material.FIREWORK_ROCKET;
    }
}
