package com.github.pietw3lve.fpm.handlers;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import com.github.pietw3lve.fpm.FluxPerMillion;

public class FishTrackerHandler {
    
    private final FluxPerMillion plugin;
    private final Map<UUID, Integer> playerCountDowns = new HashMap<>();
    private final Map<UUID, Integer> fishCounts = new HashMap<>();
    private int overFishThreshold;
    private long resetIntervalTicks;

    /**
     * FishTrackerUtil Constructor.
     * @param plugin
     */
    public FishTrackerHandler(FluxPerMillion plugin) {
        this.plugin = plugin;
        this.reload();
    }

    /**
     * Starts a task to reset the fish count for a player after a set interval.
     * @param player The player to reset the fish count for.
     */
    public void startFishCountResetTask(Player player) {
        if (playerCountDowns.containsKey(player.getUniqueId())) {
            plugin.getServer().getScheduler().cancelTask(playerCountDowns.get(player.getUniqueId()));
        }
        BukkitTask task = plugin.getServer().getScheduler().runTaskLater(plugin, () -> resetFishCount(player), resetIntervalTicks);
        playerCountDowns.put(player.getUniqueId(), task.getTaskId());
    }

    /**
     * Resets the fish count for a player.
     * @param player The player to reset the fish count for.
     */
    public void resetFishCount(OfflinePlayer player) {
        fishCounts.remove(player.getUniqueId());
    }

    /**
     * Adds a fish to the fish count for a player.
     * @param player The player to add a fish to.
     */
    public void addFish(Player player) {
        int fishCount = fishCounts.getOrDefault(player.getUniqueId(), 0);
        fishCounts.put(player.getUniqueId(), fishCount + 1);
    }

    /**
     * Removes a fish from the fish count for a player.
     * @param player The player to remove a fish from.
     */
    public void removeFish(Player player) {
        int fishCount = fishCounts.getOrDefault(player.getUniqueId(), 0);
        fishCounts.put(player.getUniqueId(), Math.max(0, fishCount - 1));
    }

    /**
     * Returns true if the player has reached the fish threshold.
     * @param player The player to check the fish count for.
     * @return True if the player has reached the fish threshold.
     */
    public boolean hasReachedFishThreshold(OfflinePlayer player) {
        return fishCounts.getOrDefault(player.getUniqueId(), 0) >= overFishThreshold;
    }

    /**
     * Reloads the fish tracker configuration.
     */
    public void reload() {
        overFishThreshold = plugin.getConfig().getInt("fishing.threshold", 50);
        resetIntervalTicks = plugin.getConfig().getLong("fishing.timer", 36000);
    }
}
