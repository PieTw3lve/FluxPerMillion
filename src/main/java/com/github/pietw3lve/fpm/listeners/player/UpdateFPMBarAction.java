package com.github.pietw3lve.fpm.listeners.player;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;

import com.github.pietw3lve.fpm.FluxPerMillion;
import com.github.pietw3lve.fpm.handlers.FluxHandler;
import com.github.pietw3lve.fpm.utils.EventActionUtil;

public class UpdateFPMBarAction implements EventActionUtil<PlayerJoinEvent> {
    
    private final FluxHandler fluxMeterHandler;

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
