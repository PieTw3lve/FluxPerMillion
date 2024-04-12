package com.github.pietw3lve.fpm.listeners.player;

import java.util.Random;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;

import com.github.pietw3lve.fpm.FluxPerMillion;
import com.github.pietw3lve.fpm.events.FluxLevelChangeEvent;
import com.github.pietw3lve.fpm.handlers.FishTrackerHandler;

import net.md_5.bungee.api.ChatColor;

public class PlayerFishListener implements Listener {
    
    private final FluxPerMillion plugin;
    private final FishTrackerHandler fishTracker;
    private final String[] overFishingLines = new String[] {
        "You might want to reel in a bit and explore other waters.",
        "Perhaps it's time to cast your line towards new horizons.",
        "Let's reel it in a bit and savor the anticipation of the next big catch.",
        "Sometimes, it's good to let a few fish swim by.",
        "Maybe it's time to let your thoughts swim freely outside this pond for a bit.",
        "Even the most skilled angler needs a break to sharpen their hooks.",
        "Your fishing skills are impressive, but don't forget to give your mind a breather.",
        "You've reeled in quite a catch; now it's time to let the waters settle and see what else swims by.",
        "Even the most captivating fishing spots can benefit from a change of scenery.",
        "Sometimes, a pause in fishing leads to the biggest catches.",
    };

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
                    Random rand = new Random();
                    double points = plugin.getConfig().getDouble("flux_points.over_fish", 0.25);
                    player.sendMessage(ChatColor.RED + ChatColor.ITALIC.toString() + overFishingLines[rand.nextInt(overFishingLines.length)]);
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
