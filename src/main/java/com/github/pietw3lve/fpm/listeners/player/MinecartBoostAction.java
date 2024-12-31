package com.github.pietw3lve.fpm.listeners.player;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import com.github.pietw3lve.fpm.FluxPerMillion;
import com.github.pietw3lve.fpm.events.FluxLevelChangeEvent;
import com.github.pietw3lve.fpm.utils.EventActionUtil;
import com.github.pietw3lve.fpm.utils.SQLiteUtil.ActionCategory;

public class MinecartBoostAction implements EventActionUtil<PlayerInteractEvent> {

    private static final String FLUX_POINTS_MINECART_BOOST = "flux_points.minecart_boost";
    private static final String BOOST_ENABLED = "custom_mechanics.minecart.surge_boost.enabled";
    private static final String BOOST_AMOUNT = "custom_mechanics.minecart.surge_boost.amount";
    private static final String BOOST_COOLDOWN = "custom_mechanics.minecart.surge_boost.cooldown";

    private static final int PARTICLE_COUNT = 10;
    private static final double PARTICLE_OFFSET = 0.5;
    private static final double PARTICLE_SPEED = 0.1;
    private static final float SOUND_VOLUME = 1;
    private static final float SOUND_PITCH = 1;

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
        return isEnabled(BOOST_ENABLED) && !isPlayerOnBoostCooldown(player) && isPlayerInMinecart(player) && isRightClick(action) && (isBoostFuel(mainHandItemType) || isBoostFuel(offHandItemType));
    }

    @Override
    public void execute(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack mainHandItem = player.getInventory().getItemInMainHand();
        ItemStack offHandItem = player.getInventory().getItemInOffHand();
        
        Minecart minecart = (Minecart) player.getVehicle();
        Vector currentVelocity = minecart.getVelocity();
        Vector boostVelocity = currentVelocity.add(calculateBoostVelocity(player));
        double maxMinecartVelocity = minecart.getMaxSpeed();
        Vector finalVelocity = (boostVelocity.length() > maxMinecartVelocity) ? boostVelocity.normalize().multiply(maxMinecartVelocity) : currentVelocity.add(boostVelocity);

        minecart.setVelocity(finalVelocity);
        player.spawnParticle(Particle.FLAME, player.getLocation(), PARTICLE_COUNT, PARTICLE_OFFSET, PARTICLE_OFFSET, PARTICLE_OFFSET, PARTICLE_SPEED);
        player.playSound(player, Sound.ENTITY_BLAZE_SHOOT, SOUND_VOLUME, SOUND_PITCH);

        if (player.getGameMode() != GameMode.CREATIVE) {
            reduceItemAmountIfFuel(mainHandItem);
            reduceItemAmountIfFuel(offHandItem);
        }
        
        minecartBoostCooldown.add(player.getUniqueId().toString());
        
        long cooldown = plugin.getConfig().getInt(BOOST_COOLDOWN);
        player.setCooldown(event.getMaterial(), (int) cooldown);
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            minecartBoostCooldown.remove(player.getUniqueId().toString());
        }, cooldown);

        double points = plugin.getConfig().getDouble(FLUX_POINTS_MINECART_BOOST);
        FluxLevelChangeEvent fluxEvent = new FluxLevelChangeEvent(plugin.getFluxMeter(), player.getLocation(), player, null, "boosted", "minecart", points, ActionCategory.ENERGY);
        plugin.getServer().getPluginManager().callEvent(fluxEvent);
    }

    private Vector calculateBoostVelocity(Player player) {
        Location eyeLocation = player.getEyeLocation();
        Vector direction = eyeLocation.getDirection().normalize();
        double speedBoost = plugin.getConfig().getDouble(BOOST_AMOUNT);
        return direction.multiply(speedBoost);
    }

    private void reduceItemAmountIfFuel(ItemStack item) {
        if (isBoostFuel(item.getType())) {
            item.setAmount(item.getAmount() - 1);
        }
    }

    private boolean isEnabled(String key) {
        return plugin.getConfig().getBoolean(key);
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
