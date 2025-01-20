package com.github.pietw3lve.fpm.listeners.player;

import java.util.Set;

import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.metadata.FixedMetadataValue;

import com.github.pietw3lve.fpm.FluxPerMillion;
import com.github.pietw3lve.fpm.handlers.TreeHandler;
import com.github.pietw3lve.fpm.utils.EventActionUtil;

public class LogStripAction implements EventActionUtil<PlayerInteractEvent> {

    private final FluxPerMillion plugin;
    private final TreeHandler treeHandler;
    private final Set<Material> axes;

    public LogStripAction(FluxPerMillion plugin) {
        this.plugin = plugin;
        this.treeHandler = plugin.getTreeUtils();
        this.axes = Tag.ITEMS_AXES.getValues();
    }

    @Override
    public boolean matches(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Action action = event.getAction();
        Block block = event.getClickedBlock();
        Material mainHandItemType = player.getInventory().getItemInMainHand().getType();
        return action == Action.RIGHT_CLICK_BLOCK && !block.hasMetadata("fpm:placed") && treeHandler.isTreeBlock(block) && axes.contains(mainHandItemType);
    }

    @Override
    public void execute(PlayerInteractEvent event) {
        Block block = event.getClickedBlock();
        block.setMetadata("fpm:stripped", new FixedMetadataValue(plugin, true));
    }
}
