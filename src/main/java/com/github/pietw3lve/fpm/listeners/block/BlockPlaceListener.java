package com.github.pietw3lve.fpm.listeners.block;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.metadata.FixedMetadataValue;

import com.github.pietw3lve.fpm.FluxPerMillion;
import com.github.pietw3lve.fpm.events.FluxLevelChangeEvent;

public class BlockPlaceListener implements Listener{
    
    private final FluxPerMillion plugin;

    /**
     * BlockPlaceListener Constructor.
     * @param plugin
     */
    public BlockPlaceListener(FluxPerMillion plugin) {
        this.plugin = plugin;
    }

    /**
     * Listens for block place events.
     * @param event BlockPlaceEvent
     */
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.isCancelled()) return;
        
        FluxLevelChangeEvent fluxEvent = new FluxLevelChangeEvent();
        Block block = event.getBlock();
        block.setMetadata("fpm:placed", new FixedMetadataValue(plugin, true));

        Player player = event.getPlayer();
        double points;

        switch (block.getType()) {
            case COAL_BLOCK:
                points = plugin.getConfig().getDouble("flux_points.coal_place", 0.25) * 9;
                fluxEvent = new FluxLevelChangeEvent(plugin.getFluxMeter(), player, "placed", "coal block", points);
                break;
            case COAL_ORE:
                points = plugin.getConfig().getDouble("flux_points.coal_place", 0.25);
                fluxEvent = new FluxLevelChangeEvent(plugin.getFluxMeter(), player, "placed", "coal ore", points);
                break;
            case DEEPSLATE_COAL_ORE:
                points = plugin.getConfig().getDouble("flux_points.coal_place", 0.25);
                fluxEvent = new FluxLevelChangeEvent(plugin.getFluxMeter(), player, "placed", "deepslate coal ore", points);
                break;
            case CAMPFIRE:
                points = plugin.getConfig().getDouble("flux_points.campfire_place", 0.5);
                fluxEvent = new FluxLevelChangeEvent(plugin.getFluxMeter(), player, "placed", "campfire", points);
                break;
            case SOUL_CAMPFIRE:
                points = plugin.getConfig().getDouble("flux_points.campfire_place", 0.5);
                fluxEvent = new FluxLevelChangeEvent(plugin.getFluxMeter(), player, "placed", "soul campfire", points);
                break;
            case TORCH:
                points = plugin.getConfig().getDouble("flux_points.torch_place", 0.25);
                fluxEvent = new FluxLevelChangeEvent(plugin.getFluxMeter(), player, "placed", "torch", points);
                break;
            case SOUL_TORCH:
                points = plugin.getConfig().getDouble("flux_points.torch_place", 0.25);
                fluxEvent = new FluxLevelChangeEvent(plugin.getFluxMeter(), player, "placed", "soul torch", points);
                break;
            case FIRE:
                points = plugin.getConfig().getDouble("flux_points.flint_and_steel_use", 0.25);
                fluxEvent = new FluxLevelChangeEvent(plugin.getFluxMeter(), player, "used", "flint and steel", points);
                break;
            default:
                break;
        }
        plugin.getServer().getPluginManager().callEvent(fluxEvent);
    }
}
