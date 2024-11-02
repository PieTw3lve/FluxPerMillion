package com.github.pietw3lve.fpm.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.github.pietw3lve.fpm.FluxPerMillion;
import com.github.pietw3lve.fpm.gui.impl.StatusInventory;
import com.github.pietw3lve.fpm.utils.GUIUtil;

public class Status {
    
    private final FluxPerMillion plugin;
    private final GUIUtil guiUtil;

    /**
     * Status Constructor.
     * @param plugin
     */
    public Status(FluxPerMillion plugin) {
        this.plugin = plugin;
        this.guiUtil = plugin.getGUIUtil();
    }

    public boolean execute(CommandSender sender) {
        if (!(sender instanceof Player)) {
            String playerOnlyCommandMessage = plugin.getMessageHandler().getPlayerOnlyCommandMessage();
            sender.sendMessage(playerOnlyCommandMessage);
            return true;
        }

        Player player = (Player) sender;
        this.guiUtil.openGUI(new StatusInventory(plugin), player);
        return true;
    }
}
