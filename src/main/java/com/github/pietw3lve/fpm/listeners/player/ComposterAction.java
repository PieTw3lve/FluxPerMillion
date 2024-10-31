package com.github.pietw3lve.fpm.listeners.player;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Levelled;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import com.github.pietw3lve.fpm.FluxPerMillion;
import com.github.pietw3lve.fpm.events.FluxLevelChangeEvent;
import com.github.pietw3lve.fpm.utils.EventActionUtil;

public class ComposterAction implements EventActionUtil<PlayerInteractEvent> {
    
    private static final String FLUX_POINTS_COMPOST_COMPLETE = "flux_points.compost_complete";

    private final FluxPerMillion plugin;
    
    public ComposterAction(FluxPerMillion plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean matches(PlayerInteractEvent event) {
        Action action = event.getAction();
        Block block = event.getClickedBlock();
        return isRightClick(action) && isComposter(block);
    }

    @Override
    public void execute(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();
        Levelled composter = (Levelled) block.getBlockData();
        if (composter.getLevel() == composter.getMaximumLevel()) {
            double points = plugin.getConfig().getDouble(FLUX_POINTS_COMPOST_COMPLETE);
            FluxLevelChangeEvent fluxEvent = new FluxLevelChangeEvent(plugin.getFluxMeter(), player.getLocation(), player, "filled", "composter", points);
            plugin.getServer().getPluginManager().callEvent(fluxEvent);
        }
    }

    private boolean isRightClick(Action action) {
        return action == Action.RIGHT_CLICK_BLOCK;
    }

    private boolean isComposter(Block block) {
        return block.getType() == Material.COMPOSTER;
    }
}
