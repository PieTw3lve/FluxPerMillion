package com.github.pietw3lve.fpm.listeners.player;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.metadata.FixedMetadataValue;

import com.github.pietw3lve.fpm.FluxPerMillion;
import com.github.pietw3lve.fpm.handlers.TreeHandler;
import com.github.pietw3lve.fpm.listeners.EventAction;

public class TreeStripAction implements EventAction<PlayerInteractEvent> {

    private final FluxPerMillion plugin;
    private final TreeHandler treeHandler;

    public TreeStripAction(FluxPerMillion plugin) {
        this.plugin = plugin;
        this.treeHandler = plugin.getTreeUtils();
    }

    @Override
    public boolean matches(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Action action = event.getAction();
        Block block = event.getClickedBlock();
        Material mainHandItemType = player.getInventory().getItemInMainHand().getType();
        return action == Action.RIGHT_CLICK_BLOCK && block != null && !block.hasMetadata("fpm:placed") && treeHandler.isTreeLog(block) && isAxe(mainHandItemType);
    }

    @Override
    public void execute(PlayerInteractEvent event) {
        Block block = event.getClickedBlock();
        block.setMetadata("fpm:stripped", new FixedMetadataValue(plugin, true));
    }

    private boolean isAxe(Material material) {
        return material == Material.WOODEN_AXE || material == Material.STONE_AXE || material == Material.IRON_AXE || material == Material.GOLDEN_AXE || material == Material.DIAMOND_AXE || material == Material.NETHERITE_AXE;
    }
}
