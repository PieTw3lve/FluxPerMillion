package com.github.pietw3lve.fpm.listeners.player;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;

import com.github.pietw3lve.fpm.FluxPerMillion;
import com.github.pietw3lve.fpm.handlers.FluxMeterHandler;
import com.github.pietw3lve.fpm.listeners.EventAction;

public class UpdateFPMBarAction implements EventAction<PlayerJoinEvent> {
    
    private final FluxMeterHandler fluxMeterHandler;

    public UpdateFPMBarAction(FluxPerMillion plugin) {
        this.fluxMeterHandler = plugin.getFluxMeter();
    }

    @Override
    public boolean matches(PlayerJoinEvent event) {
        return true;
    }

    @Override
    public void execute(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (fluxMeterHandler.isPlayerWithBossBar(player)) {
            fluxMeterHandler.getFluxMeter().addPlayer(player);
        }
    }
}
