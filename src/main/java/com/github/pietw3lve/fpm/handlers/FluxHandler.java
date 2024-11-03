package com.github.pietw3lve.fpm.handlers;

import java.util.Arrays;
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

/**
 * Handles the flux meter functionality, including updating the flux meter,
 * toggling it for players, and managing the flux meter task.
 */
public class FluxHandler {
    
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
    private double oldEnergyPoints;
    private double newEnergyPoints;
    private double oldAgriculturePoints;
    private double newAgriculturePoints;
    private double oldPollutionPoints;
    private double newPollutionPoints;
    private double oldWildlifePoints;
    private double newWildlifePoints;

    /**
     * FluxHandler Constructor.
     * @param plugin The main plugin instance.
     */
    public FluxHandler(FluxPerMillion plugin) {
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
        percent = Math.max(Math.min(totalPoints, max), 0) / max;
        fluxMeter.setProgress(percent);

        updateOldPoints();
        updateNewPoints();

        int newStatusLevel = calculateStatusLevel();
        if (newStatusLevel != statusLevel) {
            StatusLevelChangeEvent event = new StatusLevelChangeEvent(this, newStatusLevel, statusLevel);
            plugin.getServer().getPluginManager().callEvent(event);
            statusLevel = newStatusLevel;
        }

        lastRunTime = System.currentTimeMillis();
    }

    /**
     * Updates the old points with the current new points.
     */
    private void updateOldPoints() {
        oldEnergyPoints = newEnergyPoints;
        oldAgriculturePoints = newAgriculturePoints;
        oldPollutionPoints = newPollutionPoints;
        oldWildlifePoints = newWildlifePoints;
    }

    /**
     * Updates the new points by calculating the total points for each category.
     */
    private void updateNewPoints() {
        newEnergyPoints = Math.max(0, plugin.getDbUtil().calculateTotalPointsForCategory(ActionCategory.ENERGY));
        newAgriculturePoints = Math.max(0, plugin.getDbUtil().calculateTotalPointsForCategory(ActionCategory.AGRICULTURE));
        newPollutionPoints = Math.max(0, plugin.getDbUtil().calculateTotalPointsForCategory(ActionCategory.POLLUTION));
        newWildlifePoints = Math.max(0, plugin.getDbUtil().calculateTotalPointsForCategory(ActionCategory.WILDLIFE));
    }

    /**
     * Calculates the status level based on the current percent.
     * @return The new status level.
     */
    private int calculateStatusLevel() {
        if (percent >= tier3Threshold) {
            return 3;
        } else if (percent >= tier2Threshold) {
            return 2;
        } else if (percent >= tier1Threshold) {
            return 1;
        } else {
            return 0;
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
        offset = plugin.getConfig().getDouble("flux_meter.flux_capacity_offset");
        decay = plugin.getConfig().getInt("flux_meter.decay");
        
        if (fluxMeterTask != null) {
            plugin.getServer().getScheduler().cancelTask(fluxMeterTask.getTaskId());
        }

        fluxMeterTask = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> update(), 0, refreshInterval);
    }

    /**
     * Checks if a player has a boss bar.
     * @param player The player to check.
     * @return true if the player has a boss bar, false otherwise.
     */
    public boolean isPlayerWithBossBar(Player player) {
        return playersWithBossBar.contains(player.getUniqueId().toString());
    }

    /**
     * Returns the current flux meter task.
     * @return The current BukkitTask for the flux meter.
     */
    public BukkitTask getFluxMeterTask() {
        return fluxMeterTask;
    }

    /**
     * Returns the flux meter.
     * @return The BossBar instance representing the flux meter.
     */
    public BossBar getFluxMeter() {
        return fluxMeter;
    }

    /**
     * Returns the total flux points.
     * @return The total flux points.
     */
    public double getTotalPoints() {
        return totalPoints;
    }

    /**
     * Returns the flux meter percent.
     * @return The flux meter percent.
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
     * @return The flux meter status level.
     */
    public int getStatusLevel() {
        return statusLevel;
    }

    /**
     * Returns the maximum flux capacity.
     * @return The maximum flux capacity.
     */
    public double getMax() {
        return max;
    }

    /**
     * Returns the offset flux.
     * @return The offset flux.
     */
    public double getOffset() {
        return offset;
    }

    /**
     * Sets the current flux meter task.
     * @param task The new BukkitTask for the flux meter.
     */
    public void setFluxMeterTask(BukkitTask task) {
        fluxMeterTask = task;
    }

    /**
     * Returns the time until the next run.
     * @return The time in milliseconds until the next run.
     */
    public long getTimeUntilNextRun() {
        long currentTime = System.currentTimeMillis();
        long elapsedTime = currentTime - lastRunTime;
        return refreshInterval * 50 - elapsedTime; // refreshInterval is in ticks (20 ticks = 1 second)
    }

    /**
     * Returns the new flux percentages.
     * @return The new flux percentages.
     */
    public double[] getNewFluxPercentages() {
        double[] newFlux = {
            newEnergyPoints,
            newAgriculturePoints,
            newPollutionPoints,
            newWildlifePoints
        };
        double newTotalFlux = Arrays.stream(newFlux).sum();
        return Arrays.stream(newFlux).map(p -> p / newTotalFlux * 100).toArray();
    }

    /**
     * Returns the old flux percentages.
     * @return The old flux percentages.
     */
    public double[] getOldFluxPercentages() {
        double[] oldFlux = {
            oldEnergyPoints,
            oldAgriculturePoints,
            oldPollutionPoints,
            oldWildlifePoints
        };
        double oldTotalFlux = Arrays.stream(oldFlux).sum();
        return Arrays.stream(oldFlux).map(p -> p / oldTotalFlux * 100).toArray();
    }

    public double getPlayerPercent(double playerFlux) {
        return Math.max(0, playerFlux / totalPoints) * 100;
    }
}
