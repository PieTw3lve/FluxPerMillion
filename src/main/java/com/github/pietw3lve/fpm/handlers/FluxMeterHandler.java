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
import com.github.pietw3lve.fpm.utils.SQLiteUtil.ActionCategory;

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
    private double offset;
    private int decay;
    private long lastRunTime;
    private double energyPoints;
    private double agriculturePoints;
    private double pollutionPoints;
    private double wildlifePoints;

    /**
     * FluxMeterHandler Constructor.
     * @param plugin
     */
    public FluxMeterHandler(FluxPerMillion plugin) {
        this.plugin = plugin;
        this.fluxMeterTask = null;
        this.fluxMeter = plugin.getServer().createBossBar("Flux Meter", BarColor.RED, BarStyle.SEGMENTED_12);
        this.lastRunTime = System.currentTimeMillis();
        this.reload();
    }

    /**
     * Updates the flux meter percent and status level.
     */
    public void update() {
        plugin.getDbUtil().deleteOldActions(decay);
        totalPoints = plugin.getDbUtil().calculateTotalPoints() + offset;
        energyPoints = Math.max(0, plugin.getDbUtil().calculateTotalPointsForCategory(ActionCategory.ENERGY));
        agriculturePoints = Math.max(0, plugin.getDbUtil().calculateTotalPointsForCategory(ActionCategory.AGRICULTURE));
        pollutionPoints = Math.max(0, plugin.getDbUtil().calculateTotalPointsForCategory(ActionCategory.POLLUTION));
        wildlifePoints = Math.max(0, plugin.getDbUtil().calculateTotalPointsForCategory(ActionCategory.WILDLIFE));
        percent = Math.max(Math.min(totalPoints, max), 0) / max;
        fluxMeter.setProgress(percent);

        int newStatusLevel;
        if (percent >= tier3Threshold) {
            newStatusLevel = 3;
        } else if (percent >= tier2Threshold) {
            newStatusLevel = 2;
        } else if (percent >= tier1Threshold) {
            newStatusLevel = 1;
        } else {
            newStatusLevel = 0;
        }

        if (newStatusLevel != statusLevel) {
            StatusLevelChangeEvent event = new StatusLevelChangeEvent(this, newStatusLevel, statusLevel);
            plugin.getServer().getPluginManager().callEvent(event);
            statusLevel = newStatusLevel;
        }

        lastRunTime = System.currentTimeMillis();
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
        offset = plugin.getConfig().getDouble("flux_meter.flux_capacity_offset");
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
     * Returns the offset flux.
     * @param task
     */
    public double getOffset() {
        return offset;
    }

    /**
     * Sets the current flux meter task.
     */
    public void setFluxMeterTask(BukkitTask task) {
        fluxMeterTask = task;
    }

    /**
     * Returns the time until the next run.
     * @return long
     */
    public long getTimeUntilNextRun() {
        long currentTime = System.currentTimeMillis();
        long elapsedTime = currentTime - lastRunTime;
        return refreshInterval * 50 - elapsedTime; // refreshInterval is in ticks (20 ticks = 1 second)
    }

    /**
     * Returns the energy points.
     * @return double
     */
    public double getEnergyPoints() {
        return energyPoints;
    }

    /**
     * Returns the agriculture points.
     * @return double
     */
    public double getAgriculturePoints() {
        return agriculturePoints;
    }

    /**
     * Returns the pollution points.
     * @return double
     */
    public double getPollutionPoints() {
        return pollutionPoints;
    }

    /**
     * Returns the wildlife points.
     * @return double
     */
    public double getWildlifePoints() {
        return wildlifePoints;
    }
}
