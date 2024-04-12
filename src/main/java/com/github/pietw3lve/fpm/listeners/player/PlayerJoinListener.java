package com.github.pietw3lve.fpm.listeners.player;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import com.github.pietw3lve.fpm.FluxPerMillion;
import com.github.pietw3lve.fpm.handlers.FluxMeterHandler;

public class PlayerJoinListener implements Listener {
    
    private final FluxPerMillion plugin;

    /**
     * PlayerJoinListener Constructor.
     * @param plugin
     */
    public PlayerJoinListener(FluxPerMillion plugin) {
        this.plugin = plugin;
    }

    /**
     * Listens for player joins.
     * @param event PlayerJoinEvent
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        FluxMeterHandler fluxMeterHandler = plugin.getFluxMeter();
        if (fluxMeterHandler.isPlayerWithBossBar(player)) {
            fluxMeterHandler.getFluxMeter().addPlayer(player);
        }
    }
}
