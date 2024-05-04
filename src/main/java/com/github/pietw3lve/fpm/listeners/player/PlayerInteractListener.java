package com.github.pietw3lve.fpm.listeners.player;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Levelled;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

import com.github.pietw3lve.fpm.FluxPerMillion;
import com.github.pietw3lve.fpm.events.FluxLevelChangeEvent;

public class PlayerInteractListener implements Listener {
    
    private final FluxPerMillion plugin;
    private List<String> minecartBoostCooldown = new ArrayList<String>();

    /**
     * PlayerInteractListener Constructor.
     * @param plugin
     */
    public PlayerInteractListener(FluxPerMillion plugin) {
        this.plugin = plugin;
    }

    /**
     * Listens for player interactions.
     * @param event PlayerInteractEvent
     */
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        FluxLevelChangeEvent fluxEvent = new FluxLevelChangeEvent();
        Player player = event.getPlayer();
        Action action = event.getAction();
        Block block = event.getClickedBlock();

        if (action == Action.RIGHT_CLICK_BLOCK && block != null && block.getType() == Material.COMPOSTER) {
            Levelled composter = (Levelled) block.getBlockData();
            if (composter.getLevel() == composter.getMaximumLevel()) {
                double points = plugin.getConfig().getDouble("flux_points.compost_complete", -2.0);
                fluxEvent = new FluxLevelChangeEvent(plugin.getFluxMeter(), player.getLocation(), player, "filled", "composter", points);
            }
        }
        else if (action == Action.RIGHT_CLICK_BLOCK && block != null && !block.hasMetadata("fpm:placed") && plugin.getTreeUtils().isTreeLog(block) && player.getInventory().getItemInMainHand().getType().toString().contains("_AXE")) {
            block.setMetadata("fpm:stripped", new FixedMetadataValue(plugin, true));
        }
        else if (!minecartBoostCooldown.contains(player.getUniqueId().toString()) && player.getVehicle() != null && player.getVehicle().getType() == EntityType.MINECART && (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) && player.getInventory().getItemInMainHand().getType() == Material.COAL) {
            long cooldown = plugin.getConfig().getInt("minecart.boost_cooldown", 5);
            Minecart minecart = (Minecart) player.getVehicle();
            Vector currentVelocity = minecart.getVelocity();
            Vector boostVelocity = calculateBoostVelocity(player);
            Vector finalVelocity = currentVelocity.add(boostVelocity);
            double maxMinecartVelocity = minecart.getMaxSpeed();
            Vector velocity = (finalVelocity.length() > maxMinecartVelocity) ? finalVelocity.normalize().multiply(maxMinecartVelocity) : currentVelocity.add(boostVelocity);

            minecart.setVelocity(velocity);
            player.spawnParticle(org.bukkit.Particle.FLAME, player.getLocation(), 10, 0.5, 0.5, 0.5, 0.1);
            player.playSound(player, org.bukkit.Sound.ENTITY_BLAZE_SHOOT, 1, 1);
            
            if (player.getGameMode() != GameMode.CREATIVE) player.getInventory().getItemInMainHand().setAmount(player.getInventory().getItemInMainHand().getAmount() - 1);
            
            minecartBoostCooldown.add(player.getUniqueId().toString());
            
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                minecartBoostCooldown.remove(player.getUniqueId().toString());
            }, cooldown);
        }
        plugin.getServer().getPluginManager().callEvent(fluxEvent);
    }

    private Vector calculateBoostVelocity(Player player) {
        Location eyeLocation = player.getEyeLocation();
        Vector direction = eyeLocation.getDirection().normalize();
        double speedBoost = plugin.getConfig().getDouble("minecart.boost_amount", 0.1);
        return direction.multiply(speedBoost);
    }
}
