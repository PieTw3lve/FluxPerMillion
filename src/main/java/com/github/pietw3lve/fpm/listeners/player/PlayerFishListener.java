package com.github.pietw3lve.fpm.listeners.player;

import java.util.List;
import java.util.Random;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;

import com.github.pietw3lve.fpm.FluxPerMillion;
import com.github.pietw3lve.fpm.events.FluxLevelChangeEvent;
import com.github.pietw3lve.fpm.handlers.FishTrackerHandler;

public class PlayerFishListener implements Listener {
    
    private final FluxPerMillion plugin;
    private final FishTrackerHandler fishTracker;

    /**
     * PlayerFishListener Constructor.
     * @param plugin
     */
    public PlayerFishListener(FluxPerMillion plugin) {
        this.plugin = plugin;
        this.fishTracker = new FishTrackerHandler(plugin);
    }

    /**
     * Listens for player fish caught events.
     * @param event PlayerFishEvent
     */
    @EventHandler
    public void onPlayerFishCaught(PlayerFishEvent event) {
        FluxLevelChangeEvent fluxEvent = new FluxLevelChangeEvent();;
        Player player = event.getPlayer();
        switch (event.getState()) {
            case CAUGHT_FISH:
                if (fishTracker.hasReachedFishThreshold(player)) {
                    List<String> overFishingLines = plugin.getMessageHandler().getOverFishingLines();
                    double points = plugin.getConfig().getDouble("flux_points.over_fish", 0.25);
                    Random rand = new Random();
                    
                    if (!overFishingLines.isEmpty()) {
                        player.sendMessage(overFishingLines.get(rand.nextInt(overFishingLines.size())));
                    }

                    fluxEvent = new FluxLevelChangeEvent(plugin.getFluxMeter(), player, "over", "fishing", points);
                }
                fishTracker.addFish(event.getPlayer());
                fishTracker.startFishCountResetTask(event.getPlayer());
            default:
                break;
        }
        plugin.getServer().getPluginManager().callEvent(fluxEvent);
    }
}
