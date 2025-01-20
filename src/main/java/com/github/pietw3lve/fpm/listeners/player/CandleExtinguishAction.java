package com.github.pietw3lve.fpm.listeners.player;

import java.util.Set;

import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Candle;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import com.github.pietw3lve.fpm.FluxPerMillion;
import com.github.pietw3lve.fpm.events.FluxLevelChangeEvent;
import com.github.pietw3lve.fpm.utils.EventActionUtil;
import com.github.pietw3lve.fpm.utils.SQLiteUtil.ActionCategory;

public class CandleExtinguishAction implements EventActionUtil<PlayerInteractEvent> {
    
    private static final String FLUX_POINTS_CANDLE_UNLIT = "flux_points.candle_unlit";

    private final FluxPerMillion plugin;
    private final Set<Material> candles;

    public CandleExtinguishAction(FluxPerMillion plugin) {
        this.plugin = plugin;
        this.candles = Tag.CANDLES.getValues();
    }

    @Override
    public boolean matches(PlayerInteractEvent event) {
        Block block = event.getClickedBlock();
        Action action = event.getAction();
        return action == Action.RIGHT_CLICK_BLOCK && candles.contains(block.getType()) && ((Candle) block.getBlockData()).isLit() && event.getItem() == null;
    }

    @Override
    public void execute(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();
        Candle candle = (Candle) block.getBlockData();
        String blockName = block.getType().toString().replace("_", " ").toLowerCase();
        double points = plugin.getConfig().getDouble(FLUX_POINTS_CANDLE_UNLIT) * candle.getCandles();
        FluxLevelChangeEvent fluxEvent = new FluxLevelChangeEvent(plugin.getFluxMeter(), block.getLocation(), player, null, "removed", blockName, points, ActionCategory.ENERGY);
        plugin.getServer().getPluginManager().callEvent(fluxEvent);
    }
}
