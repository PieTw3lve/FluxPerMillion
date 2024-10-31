package com.github.pietw3lve.fpm.handlers;

import java.util.HashSet;
import java.util.Set;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import com.github.pietw3lve.fpm.FluxPerMillion;
import com.github.pietw3lve.fpm.events.StatusLevelChangeEvent;

public class FluxMeterHandler {
    
    private final FluxPerMillion plugin;
    private final Set<String> playersWithBossBar = new HashSet<>();
    private BukkitTask fluxMeterTask;
    private BossBar fluxMeter;
    private int refreshInterval;
    private double totalPoints;
    private int statusLevel;
    private double tier1Threshold;
    private double tier2Threshold;
    private double tier3Threshold;
    private double percent;
    private double max;
    private double min;
    private int decay;

    /**
     * FluxMeterHandler Constructor.
     * @param plugin
     */
    public FluxMeterHandler(FluxPerMillion plugin) {
        this.plugin = plugin;
        this.fluxMeterTask = null;
        this.fluxMeter = plugin.getServer().createBossBar("Flux Meter", BarColor.RED, BarStyle.SEGMENTED_12);
        this.reload();
    }

    /**
     * Updates the flux meter percent and status level.
     */
    public void update() {
        plugin.getDbUtil().deleteOldActions(decay);
        totalPoints = plugin.getDbUtil().calculateTotalPoints();
        percent = Math.max(Math.min(totalPoints, max), min) / max;
        fluxMeter.setProgress(percent);

        if (percent >= tier3Threshold) {
            if (statusLevel == 3) return;
            StatusLevelChangeEvent event = new StatusLevelChangeEvent(this, 3, statusLevel);
            plugin.getServer().getPluginManager().callEvent(event);
            statusLevel = 3;
        } else if (percent >= tier2Threshold) {
            if (statusLevel == 2) return;
            StatusLevelChangeEvent event = new StatusLevelChangeEvent(this, 2, statusLevel);
            plugin.getServer().getPluginManager().callEvent(event);
            statusLevel = 2;
        } else if (percent >= tier1Threshold) {
            if (statusLevel == 1) return;
            StatusLevelChangeEvent event = new StatusLevelChangeEvent(this, 1, statusLevel);
            plugin.getServer().getPluginManager().callEvent(event);
            statusLevel = 1;
        } else {
            if (statusLevel == 0) return;
            StatusLevelChangeEvent event = new StatusLevelChangeEvent(this, 0, statusLevel);
            plugin.getServer().getPluginManager().callEvent(event);
            statusLevel = 0;
        }
    }

    /**
     * Toggles the flux meter for a player.
     * @param player The player to toggle the flux meter for.
     * @return True if the flux meter is toggled on, false if toggled off.
     */
    public boolean toggle(Player player) {
        if (playersWithBossBar.contains(player.getUniqueId().toString())) {
            playersWithBossBar.remove(player.getUniqueId().toString());
            fluxMeter.removePlayer(player);
            return false;
        } else {
            playersWithBossBar.add(player.getUniqueId().toString());
            fluxMeter.addPlayer(player);
            return true;
        }
    }

    /**
     * Reloads the flux meter.
     */
    public void reload() {
        refreshInterval = plugin.getConfig().getInt("flux_meter.refresh_interval");
        tier1Threshold = plugin.getConfig().getDouble("flux_meter.tier_1_threshold");
        tier2Threshold = plugin.getConfig().getDouble("flux_meter.tier_2_threshold");
        tier3Threshold = plugin.getConfig().getDouble("flux_meter.tier_3_threshold");
        max = plugin.getConfig().getDouble("flux_meter.maximum_flux_capacity");
        min = plugin.getConfig().getDouble("flux_meter.minimum_flux_capacity");
        decay = plugin.getConfig().getInt("flux_meter.decay");
        
        if (fluxMeterTask != null) {
            plugin.getServer().getScheduler().cancelTask(fluxMeterTask.getTaskId());
        }

        fluxMeterTask = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> update(), 0, refreshInterval);
    }

    
    /**
     * Checks if a player has a boss bar.
     *
     * @param player The player to check.
     * @return true if the player has a boss bar, false otherwise.
     */
    public boolean isPlayerWithBossBar(Player player) {
        return playersWithBossBar.contains(player.getUniqueId().toString());
    }

    /**
     * Returns the current flux meter task.
     */
    public BukkitTask getFluxMeterTask() {
        return fluxMeterTask;
    }

    /**
     * Returns the flux meter.
     * @return BossBar
     */
    public BossBar getFluxMeter() {
        return fluxMeter;
    }

    /**
     * Returns the total flux points.
     * @return double
     */
    public double getTotalPoints() {
        return totalPoints;
    }

    /**
     * Returns the flux meter percent.
     * @return double
     */
    public double getProgress() {
        return percent;
    }

    /**
     * Returns the flux meter status level.
     * <p>
     * 0 = No concern (0-25%)
     * <p>
     * 1 = Minor concern (25-50%)
     * <p>
     * 2 = Moderate concern (50-75%)
     * <p>
     * 3 = Major concern (75-100%)
     * @return int
     */
    public int getStatusLevel() {
        return statusLevel;
    }

    /**
     * Returns the maximum flux capacity.
     * @param task
     */
    public double getMax() {
        return max;
    }

    /**
     * Returns the minimum flux capacity.
     * @param task
     */
    public double getMin() {
        return min;
    }

    /**
     * Sets the current flux meter task.
     */
    public void setFluxMeterTask(BukkitTask task) {
        fluxMeterTask = task;
    }
}
