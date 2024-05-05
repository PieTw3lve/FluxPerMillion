package com.github.pietw3lve.fpm.listeners.player;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import com.github.pietw3lve.fpm.FluxPerMillion;
import com.github.pietw3lve.fpm.events.FluxLevelChangeEvent;
import com.github.pietw3lve.fpm.listeners.EventAction;

public class MinecartBoostAction implements EventAction<PlayerInteractEvent> {

    private static final String BOOST_ENABLED = "minecart.boost_enabled";
    private static final String FLUX_POINTS_MINECART_BOOST = "flux_points.minecart_boost";
    private static final String MINECART_BOOST_COOLDOWN = "minecart.boost_cooldown";
    private static final String MINECART_BOOST_AMOUNT = "minecart.boost_amount";
    private static final boolean DEFAULT_BOOST_ENABLED = true;
    private static final double DEFAULT_FLUX_POINTS_MINECART_BOOST = 1.0;
    private static final int DEFAULT_MINECART_BOOST_COOLDOWN = 5;
    private static final double DEFAULT_MINECART_BOOST_AMOUNT = 0.15;

    private final FluxPerMillion plugin;
    private List<String> minecartBoostCooldown = new ArrayList<String>();

    public MinecartBoostAction(FluxPerMillion plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean matches(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Action action = event.getAction();
        Material mainHandItemType = player.getInventory().getItemInMainHand().getType();
        Material offHandItemType = player.getInventory().getItemInOffHand().getType();
        return isEnabled(BOOST_ENABLED, DEFAULT_BOOST_ENABLED) && !isPlayerOnBoostCooldown(player) && isPlayerInMinecart(player) && isRightClick(action) && (isBoostFuel(mainHandItemType) || isBoostFuel(offHandItemType));
    }

    @Override
    public void execute(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack mainHandItem = player.getInventory().getItemInMainHand();
        ItemStack offHandItem = player.getInventory().getItemInOffHand();
        
        Minecart minecart = (Minecart) player.getVehicle();
        Vector currentVelocity = minecart.getVelocity();
        Vector boostVelocity = calculateBoostVelocity(player);
        Vector finalVelocity = currentVelocity.add(boostVelocity);
        double maxMinecartVelocity = minecart.getMaxSpeed();
        Vector velocity = (finalVelocity.length() > maxMinecartVelocity) ? finalVelocity.normalize().multiply(maxMinecartVelocity) : currentVelocity.add(boostVelocity);

        minecart.setVelocity(velocity);
        player.spawnParticle(org.bukkit.Particle.FLAME, player.getLocation(), 10, 0.5, 0.5, 0.5, 0.1);
        player.playSound(player, org.bukkit.Sound.ENTITY_BLAZE_SHOOT, 1, 1);

        if (player.getGameMode() != GameMode.CREATIVE) {
            if (isBoostFuel(mainHandItem.getType())) {
                mainHandItem.setAmount(mainHandItem.getAmount() - 1);
            } else if (isBoostFuel(offHandItem.getType())) {
                offHandItem.setAmount(offHandItem.getAmount() - 1);
            }
        }
        
        minecartBoostCooldown.add(player.getUniqueId().toString());
        
        long cooldown = plugin.getConfig().getInt(MINECART_BOOST_COOLDOWN, DEFAULT_MINECART_BOOST_COOLDOWN);
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            minecartBoostCooldown.remove(player.getUniqueId().toString());
        }, cooldown);

        double points = plugin.getConfig().getDouble(FLUX_POINTS_MINECART_BOOST, DEFAULT_FLUX_POINTS_MINECART_BOOST);
        FluxLevelChangeEvent fluxEvent = new FluxLevelChangeEvent(plugin.getFluxMeter(), player.getLocation(), player, "boosted", "minecart", points);
        plugin.getServer().getPluginManager().callEvent(fluxEvent);
    }

    private Vector calculateBoostVelocity(Player player) {
        Location eyeLocation = player.getEyeLocation();
        Vector direction = eyeLocation.getDirection().normalize();
        double speedBoost = plugin.getConfig().getDouble(MINECART_BOOST_AMOUNT, DEFAULT_MINECART_BOOST_AMOUNT);
        return direction.multiply(speedBoost);
    }

    private boolean isEnabled(String key, boolean defaultValue) {
        return plugin.getConfig().getBoolean(key, defaultValue);
    }

    private boolean isPlayerOnBoostCooldown(Player player) {
        return minecartBoostCooldown.contains(player.getUniqueId().toString());
    }

    private boolean isPlayerInMinecart(Player player) {
        return player.getVehicle() != null && player.getVehicle().getType() == EntityType.MINECART;
    }

    private boolean isRightClick(Action action) {
        return action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK;
    }

    private boolean isBoostFuel(Material type) {
        return type == Material.COAL || type == Material.CHARCOAL;
    }
}
