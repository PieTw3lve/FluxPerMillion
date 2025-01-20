package com.github.pietw3lve.fpm.listeners.block;

import java.util.Collection;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBurnEvent;

import com.github.pietw3lve.fpm.FluxPerMillion;
import com.github.pietw3lve.fpm.events.FluxLevelChangeEvent;
import com.github.pietw3lve.fpm.utils.EventActionUtil;
import com.github.pietw3lve.fpm.utils.SQLiteUtil.ActionCategory;

public class BlockBurnAction implements EventActionUtil<BlockBurnEvent> {
    
    private static final String FLUX_POINTS_BLOCK_BURN = "flux_points.block_burn";

    private final FluxPerMillion plugin;

    public BlockBurnAction(FluxPerMillion plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean matches(BlockBurnEvent event) {
        return true;
    }

    @Override
    public void execute(BlockBurnEvent event) {
        FluxLevelChangeEvent fluxEvent = new FluxLevelChangeEvent();
        Block block = event.getBlock();
        double points = plugin.getConfig().getDouble(FLUX_POINTS_BLOCK_BURN);

        Collection<Player> players = block.getChunk().getPlayersSeeingChunk();
        if (!players.isEmpty()) {
            for (Player player : players) {
                double playerPoints = points / players.size();
                fluxEvent = new FluxLevelChangeEvent(plugin.getFluxMeter(), block.getLocation(), player, null, "burned", "block", playerPoints, ActionCategory.ENERGY);
                plugin.getServer().getPluginManager().callEvent(fluxEvent);
            }
        } else {
            fluxEvent = new FluxLevelChangeEvent(plugin.getFluxMeter(), block.getLocation(), null, null, "burned", "block", points, ActionCategory.ENERGY);
            plugin.getServer().getPluginManager().callEvent(fluxEvent);
        }
        SpawnBurnedParticles(block);
    }

    private void SpawnBurnedParticles(Block block) {
        Random rand = new Random();
        int amount = 10 + rand.nextInt(5);
        for (int i = 0; i < amount; i++) {
            Location particle = block.getLocation().clone().add(rand.nextDouble() - 0.1, 0.5, rand.nextDouble() - 0.1);
            double height = rand.nextDouble() * 0.5;
            double speed = 0.15 + rand.nextDouble() * 0.05;
            block.getWorld().spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, particle, 0, 0, height, 0, speed);
        }
    }
}
