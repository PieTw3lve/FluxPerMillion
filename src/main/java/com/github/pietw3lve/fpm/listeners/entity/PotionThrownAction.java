package com.github.pietw3lve.fpm.listeners.entity;

import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.ThrownExpBottle;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.projectiles.ProjectileSource;

import com.github.pietw3lve.fpm.FluxPerMillion;
import com.github.pietw3lve.fpm.events.FluxLevelChangeEvent;
import com.github.pietw3lve.fpm.utils.EventActionUtil;
import com.github.pietw3lve.fpm.utils.SQLiteUtil.ActionCategory;

public class PotionThrownAction implements EventActionUtil<ProjectileHitEvent> {

    private static final String FLUX_POINTS_POTION = "flux_points.potion_throw";

    private final FluxPerMillion plugin;

    public PotionThrownAction(FluxPerMillion plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean matches(ProjectileHitEvent event) {
        return event.getEntity() instanceof ThrownPotion || event.getEntity() instanceof ThrownExpBottle;
    }

    @Override
    public void execute(ProjectileHitEvent event) {
        Projectile projectile = event.getEntity();
        ProjectileSource source = projectile.getShooter();
        Player player = source instanceof Player ? (Player) source : null;

        double points = plugin.getConfig().getDouble(FLUX_POINTS_POTION);
        FluxLevelChangeEvent fluxEvent = new FluxLevelChangeEvent(plugin.getFluxMeter(), projectile.getLocation(), player, null, "thrown", projectile.getName().toLowerCase(), points, ActionCategory.POLLUTION);
        plugin.getServer().getPluginManager().callEvent(fluxEvent);
    }
}
