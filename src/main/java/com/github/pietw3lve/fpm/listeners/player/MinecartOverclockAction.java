package com.github.pietw3lve.fpm.listeners.player;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import com.github.pietw3lve.fpm.FluxPerMillion;
import com.github.pietw3lve.fpm.events.FluxLevelChangeEvent;
import com.github.pietw3lve.fpm.listeners.EventAction;

public class MinecartOverclockAction implements EventAction<PlayerInteractEvent> {
    
    private static final String FLUX_POINTS_MINECART_OVERCLOCK = "flux_points.minecart_overclock";
    private static final String OVERCLOCK_ENABLED = "custom_mechanics.minecart.overclock.enabled";
    private static final String OVERCLOCK_MULTIPLIER = "custom_mechanics.minecart.overclock.multiplier";
    private static final String OVERCLOCK_DURATION = "custom_mechanics.minecart.overclock.duration";
    private static final double DEFAULT_FLUX_POINTS_MINECART_OVERCLOCK = 3.0;
    private static final boolean DEFAULT_OVERCLOCK_ENABLED = true;
    private static final double DEFAULT_OVERCLOCK_MULTIPLIER = 2.0;
    private static final long DEFAULT_OVERCLOCK_DURATION = 600;

    private static final int PARTICLE_COUNT = 2;
    private static final double PARTICLE_OFFSET = 0.5;
    private static final double PARTICLE_SPEED = 0.1;
    private static final int TASK_DELAY = 0;
    private static final int TASK_PERIOD = 10;
    private static final float SOUND_VOLUME = 1;
    private static final float SOUND_PITCH = 1;

    private List<String> overclockedMinecarts = new ArrayList<String>();

    private final FluxPerMillion plugin;

    public MinecartOverclockAction(FluxPerMillion plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean matches(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Action action = event.getAction();
        Material mainHandItemType = player.getInventory().getItemInMainHand().getType();
        Material offHandItemType = player.getInventory().getItemInOffHand().getType();
        return  isEnabled(OVERCLOCK_ENABLED, DEFAULT_OVERCLOCK_ENABLED) && isPlayerInMinecart(player) && !isMinecartOnBoostCooldown((Minecart) player.getVehicle()) && isRightClick(action) && (isOverclockFuel(mainHandItemType) || isOverclockFuel(offHandItemType));
    }

    @Override
    public void execute(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack mainHandItem = player.getInventory().getItemInMainHand();
        ItemStack offHandItem = player.getInventory().getItemInOffHand();
        Minecart minecart = (Minecart) player.getVehicle();
        BukkitRunnable particleTask = new BukkitRunnable() {
            @Override
            public void run() {
                player.spawnParticle(Particle.END_ROD, minecart.getLocation(), PARTICLE_COUNT, PARTICLE_OFFSET, PARTICLE_OFFSET, PARTICLE_OFFSET, PARTICLE_SPEED);
            }
        };

        double multiplier = plugin.getConfig().getDouble(OVERCLOCK_MULTIPLIER, DEFAULT_OVERCLOCK_MULTIPLIER);
        minecart.setMaxSpeed(minecart.getMaxSpeed() * multiplier);
        particleTask.runTaskTimer(plugin, TASK_DELAY, TASK_PERIOD);
        player.playSound(player, Sound.ENTITY_ZOMBIE_VILLAGER_CURE, SOUND_VOLUME, SOUND_PITCH);

        if (player.getGameMode() != GameMode.CREATIVE) {
            reduceItemAmountIfFuel(mainHandItem);
            reduceItemAmountIfFuel(offHandItem);
        }

        long duration = plugin.getConfig().getLong(OVERCLOCK_DURATION, DEFAULT_OVERCLOCK_DURATION);
        overclockedMinecarts.add(minecart.getUniqueId().toString());
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            minecart.setMaxSpeed(minecart.getMaxSpeed() / multiplier);
            overclockedMinecarts.remove(minecart.getUniqueId().toString());
            particleTask.cancel();
        }, duration);

        double points = plugin.getConfig().getDouble(FLUX_POINTS_MINECART_OVERCLOCK, DEFAULT_FLUX_POINTS_MINECART_OVERCLOCK);
        FluxLevelChangeEvent fluxEvent = new FluxLevelChangeEvent(plugin.getFluxMeter(), player.getLocation(), player, "overclocked", "minecart", points);
        plugin.getServer().getPluginManager().callEvent(fluxEvent);
    }

    private void reduceItemAmountIfFuel(ItemStack item) {
        if (isOverclockFuel(item.getType())) {
            item.setAmount(item.getAmount() - 1);
        }
    }

    private boolean isEnabled(String key, boolean defaultValue) {
        return plugin.getConfig().getBoolean(key, defaultValue);
    }

    private boolean isMinecartOnBoostCooldown(Minecart minecart) {
        return overclockedMinecarts.contains(minecart.getUniqueId().toString());
    }

    private boolean isPlayerInMinecart(Player player) {
        return player.getVehicle() != null && player.getVehicle().getType() == EntityType.MINECART;
    }

    private boolean isRightClick(Action action) {
        return action == Action.RIGHT_CLICK_AIR;
    }

    private boolean isOverclockFuel(Material type) {
        return type == Material.COAL_BLOCK;
    }
}
