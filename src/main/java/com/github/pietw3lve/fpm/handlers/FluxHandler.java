package com.github.pietw3lve.fpm.handlers;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.text.DecimalFormat;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Arrays;

import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import com.github.pietw3lve.fpm.FluxPerMillion;
import com.github.pietw3lve.fpm.events.StatusLevelChangeEvent;
import com.github.pietw3lve.fpm.utils.SQLiteUtil.ActionCategory;
import com.mitchtalmadge.asciidata.graph.ASCIIGraph;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.OfflinePlayer;

/**
 * Handles the flux meter functionality, including updating the flux meter,
 * toggling it for players, and managing the flux meter task.
 */
public class FluxHandler {
    
    private final FluxPerMillion plugin;
    private final Set<String> playersWithBossBar = new HashSet<>();
    private final Map<UUID, Double> playerFluxMap = new HashMap<>();
    private final Deque<Double> energyPercentages = new ArrayDeque<>();
    private final Deque<Double> agriculturePercentages = new ArrayDeque<>();
    private final Deque<Double> pollutionPercentages = new ArrayDeque<>();
    private final Deque<Double> wildlifePercentages = new ArrayDeque<>();
    private final int historySize = 28;
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

    /**
     * FluxHandler Constructor.
     * @param plugin The main plugin instance.
     */
    public FluxHandler(FluxPerMillion plugin) {
        this.plugin = plugin;
        this.fluxMeterTask = null;
        this.fluxMeter = plugin.getServer().createBossBar("Flux Meter", BarColor.RED, BarStyle.SEGMENTED_12);
        this.lastRunTime = System.currentTimeMillis();
        for (int i = 0; i < historySize; i++) {
            this.energyPercentages.addLast(0.0);
            this.agriculturePercentages.addLast(0.0);
            this.pollutionPercentages.addLast(0.0);
            this.wildlifePercentages.addLast(0.0);
        }
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

        updatePercentages();
        updatePlayerFlux();

        int newStatusLevel = calculateStatusLevel();

        if (newStatusLevel != statusLevel) {
            StatusLevelChangeEvent event = new StatusLevelChangeEvent(this, newStatusLevel, statusLevel);
            plugin.getServer().getPluginManager().callEvent(event);
            statusLevel = newStatusLevel;
        }

        lastRunTime = System.currentTimeMillis();
    }

    /**
     * Updates the percentages for energy, agriculture, pollution, and wildlife.
     */
    private void updatePercentages() {
        double energyPoints = plugin.getDbUtil().calculateTotalPointsForCategory(ActionCategory.ENERGY);
        double agriculturePoints = plugin.getDbUtil().calculateTotalPointsForCategory(ActionCategory.AGRICULTURE);
        double pollutionPoints = plugin.getDbUtil().calculateTotalPointsForCategory(ActionCategory.POLLUTION);
        double wildlifePoints = plugin.getDbUtil().calculateTotalPointsForCategory(ActionCategory.WILDLIFE);
        double totalPoints = energyPoints + agriculturePoints + pollutionPoints + wildlifePoints;

        if (totalPoints == 0) {
            addPercentage(energyPercentages, 0);
            addPercentage(agriculturePercentages, 0);
            addPercentage(pollutionPercentages, 0);
            addPercentage(wildlifePercentages, 0);
        } else {
            addPercentage(energyPercentages, energyPoints / totalPoints * 100);
            addPercentage(agriculturePercentages, agriculturePoints / totalPoints * 100);
            addPercentage(pollutionPercentages, pollutionPoints / totalPoints * 100);
            addPercentage(wildlifePercentages, wildlifePoints / totalPoints * 100);
        }
    }

    private void addPercentage(Deque<Double> deque, double percentage) {
        if (deque.size() >= historySize) {
            deque.removeFirst();
        }
        deque.addLast(percentage);
    }

    /**
     * Updates the player flux data asynchronously.
     */
    private void updatePlayerFlux() {
        CompletableFuture.runAsync(() -> {
            List<UUID> playerUUIDs = plugin.getServer().getOnlinePlayers().stream()
                .map(Player::getUniqueId)
                .collect(Collectors.toList());

            for (OfflinePlayer offlinePlayer : plugin.getServer().getOfflinePlayers()) {
                if (!offlinePlayer.isOnline()) {
                    playerUUIDs.add(offlinePlayer.getUniqueId());
                }
            }

            Map<UUID, Double> newPlayerFluxMap = plugin.getDbUtil().getPlayerFluxBatch(playerUUIDs);
            synchronized (playerFluxMap) {
                playerFluxMap.clear();
                playerFluxMap.putAll(newPlayerFluxMap);
            }
        });
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
     * Generates a graph for the flux meter.
     * @param data The data to generate the graph from.
     * @return The generated graph as a list of strings.
     */
    public List<String> generateGraph(Deque<Double> data) {
        String graph = ASCIIGraph
            .fromSeries(data.stream().mapToDouble(Double::doubleValue).toArray())
            .withNumRows(7)
            .withTickFormat(new DecimalFormat("###0.0"))
            .withTickWidth(5)
            .plot();
        String line = ChatColor.YELLOW + "x";
        String filler = ChatColor.DARK_GRAY + "∙" + ChatColor.translateAlternateColorCodes('&', plugin.getMessageHandler().getStatusMessages().menu.graph);
        graph = graph.replace("╯", line).replace("╭", line).replace("╰", line).replace("╮", line).replace("│", line).replace("─", line).replace("┼", "┤").replace(" ", filler);
        return Arrays.asList(graph.split("\n"));
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
     * Returns the flux value for a specific player.
     * @param playerUUID The UUID of the player.
     * @return The flux value for the player.
     */
    public double getPlayerFlux(UUID playerUUID) {
        synchronized (playerFluxMap) {
            return playerFluxMap.getOrDefault(playerUUID, Double.NaN);
        }
    }

    /**
     * Returns the flux percentage for a specific player.
     * @param playerFlux The flux value for the player.
     * @return The flux percentage for the player.
     */
    public double getPlayerPercent(double playerFlux) {
        return Math.max(0, playerFlux / totalPoints) * 100;
    }

    /**
     * Returns the energy percentage.
     * @return The energy percentage.
     */
    public Deque<Double> getEnergyPercentages() {
        return energyPercentages;
    }

    /**
     * Returns the agriculture percentage.
     * @return The agriculture percentage.
     */
    public Deque<Double> getAgriculturePercentages() {
        return agriculturePercentages;
    }

    /**
     * Returns the pollution percentage.
     * @return The pollution percentage.
     */
    public Deque<Double> getPollutionPercentages() {
        return pollutionPercentages;
    }

    /**
     * Returns the wildlife percentage.
     * @return The wildlife percentage.
     */
    public Deque<Double> getWildlifePercentages() {
        return wildlifePercentages;
    }
}