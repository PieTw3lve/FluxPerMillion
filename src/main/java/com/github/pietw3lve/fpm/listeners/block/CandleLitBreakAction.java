package com.github.pietw3lve.fpm.listeners.block;

import java.util.Set;

import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Candle;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;

import com.github.pietw3lve.fpm.FluxPerMillion;
import com.github.pietw3lve.fpm.events.FluxLevelChangeEvent;
import com.github.pietw3lve.fpm.utils.EventActionUtil;
import com.github.pietw3lve.fpm.utils.SQLiteUtil.ActionCategory;

public class CandleLitBreakAction implements EventActionUtil<BlockBreakEvent> {
    
    private final String FLUX_POINTS_CANDLE_EXTINGUISH = "flux_points.candle_unlit";

    private final FluxPerMillion plugin;
    private final Set<Material> candles;

    public CandleLitBreakAction(FluxPerMillion plugin) {
        this.plugin = plugin;
        this.candles = Tag.CANDLES.getValues();
    }

    @Override
    public boolean matches(BlockBreakEvent event) {
        Block block = event.getBlock();
        return candles.contains(block.getType()) && ((Candle) block.getBlockData()).isLit();
    }

    @Override
    public void execute(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        Candle candle = (Candle) block.getBlockData();
        String blockName = block.getType().toString().replace("_", " ").toLowerCase();
        double points = plugin.getConfig().getDouble(FLUX_POINTS_CANDLE_EXTINGUISH) * candle.getCandles();
        FluxLevelChangeEvent fluxEvent = new FluxLevelChangeEvent(plugin.getFluxMeter(), block.getLocation(), player, null, "removed", blockName, points, ActionCategory.ENERGY);
        plugin.getServer().getPluginManager().callEvent(fluxEvent);
    }
}
