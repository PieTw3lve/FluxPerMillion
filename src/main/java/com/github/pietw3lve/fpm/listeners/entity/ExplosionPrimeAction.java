package com.github.pietw3lve.fpm.listeners.entity;

import java.util.Collection;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Particle;
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
        SpawnExplosionParticles(explosion);        
    }

    private void SpawnExplosionParticles(Entity explosion) {
        Random rand = new Random();
        int amount = 10 + rand.nextInt(5);
        for (int i = 0; i < amount; i++) {
            Location particle = explosion.getLocation().clone().add(rand.nextDouble() - 0.1, 0.5, rand.nextDouble() - 0.1);
            double height = rand.nextDouble() * 0.5;
            double speed = 0.15 + rand.nextDouble() * 0.05;
            explosion.getWorld().spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, particle, 0, 0, height, 0, speed);
        }
    }
}
