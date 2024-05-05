package com.github.pietw3lve.fpm.listeners.player;

import java.util.List;
import java.util.Random;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerFishEvent;

import com.github.pietw3lve.fpm.FluxPerMillion;
import com.github.pietw3lve.fpm.events.FluxLevelChangeEvent;
import com.github.pietw3lve.fpm.handlers.FishTrackerHandler;
import com.github.pietw3lve.fpm.listeners.EventAction;

public class FishCaughtAction implements EventAction<PlayerFishEvent> {
    
    private static final String FLUX_POINTS_OVER_FISH = "flux_points.over_fish";
    private static final double DEFAULT_FLUX_POINTS_OVER_FISH = 0.25;

    private final FluxPerMillion plugin;
    private final FishTrackerHandler fishTracker;

    public FishCaughtAction(FluxPerMillion plugin) {
        this.plugin = plugin;
        this.fishTracker = new FishTrackerHandler(plugin);
    }

    @Override
    public boolean matches(PlayerFishEvent event) {
        return event.getState().equals(PlayerFishEvent.State.CAUGHT_FISH);
    }

    @Override
    public void execute(PlayerFishEvent event) {
        Player player = event.getPlayer();
        if (fishTracker.hasReachedFishThreshold(player)) {
            List<String> overFishingLines = plugin.getMessageHandler().getOverFishingLines();
            double points = plugin.getConfig().getDouble(FLUX_POINTS_OVER_FISH, DEFAULT_FLUX_POINTS_OVER_FISH);
            Random rand = new Random();
            
            if (!overFishingLines.isEmpty()) {
                player.sendMessage(overFishingLines.get(rand.nextInt(overFishingLines.size())));
            }

            FluxLevelChangeEvent fluxEvent = new FluxLevelChangeEvent(plugin.getFluxMeter(), player.getLocation(), player, "over", "fishing", points);
            plugin.getServer().getPluginManager().callEvent(fluxEvent);
        }
        fishTracker.addFish(event.getPlayer());
        fishTracker.startFishCountResetTask(event.getPlayer());
    }
}
