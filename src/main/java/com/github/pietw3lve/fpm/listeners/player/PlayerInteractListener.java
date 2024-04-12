package com.github.pietw3lve.fpm.listeners.player;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Levelled;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.metadata.FixedMetadataValue;

import com.github.pietw3lve.fpm.FluxPerMillion;
import com.github.pietw3lve.fpm.events.FluxLevelChangeEvent;

public class PlayerInteractListener implements Listener {
    
    private final FluxPerMillion plugin;

    /**
     * PlayerInteractListener Constructor.
     * @param plugin
     */
    public PlayerInteractListener(FluxPerMillion plugin) {
        this.plugin = plugin;
    }

    /**
     * Listens for player interactions.
     * @param event PlayerInteractEvent
     */
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        FluxLevelChangeEvent fluxEvent = new FluxLevelChangeEvent();
        Player player = event.getPlayer();
        Action action = event.getAction();
        Block block = event.getClickedBlock();

        if (action == Action.RIGHT_CLICK_BLOCK && block != null && block.getType() == Material.COMPOSTER) {
            Levelled composter = (Levelled) block.getBlockData();
            if (composter.getLevel() == composter.getMaximumLevel()) {
                double points = plugin.getConfig().getDouble("flux_points.compost_complete", -5.0);
                fluxEvent = new FluxLevelChangeEvent(plugin.getFluxMeter(), player, "filled", "composter", points);
            }
        }
        if (action == Action.RIGHT_CLICK_BLOCK && block != null && !block.hasMetadata("fpm:placed") && plugin.getTreeUtils().isTreeLog(block) && player.getInventory().getItemInMainHand().getType().toString().contains("_AXE")) {
            block.setMetadata("fpm:stripped", new FixedMetadataValue(plugin, true));
        }
        plugin.getServer().getPluginManager().callEvent(fluxEvent);
    }
}
