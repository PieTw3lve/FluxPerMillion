package com.github.pietw3lve.fpm.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.github.pietw3lve.fpm.FluxPerMillion;
import com.github.pietw3lve.fpm.gui.impl.StatusInventory;
import com.github.pietw3lve.fpm.utils.GUIUtil;

import net.md_5.bungee.api.ChatColor;

public class Status implements CommandExecutor {
    
    private final FluxPerMillion plugin;
    private final GUIUtil guiUtil;

    /**
     * Status Constructor.
     * @param plugin
     */
    public Status(FluxPerMillion plugin, GUIUtil guiUtil) {
        this.plugin = plugin;
        this.guiUtil = guiUtil;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "You must be a player to use this command.");
            return false;
        }

        Player player = (Player) sender;
        this.guiUtil.openGUI(new StatusInventory(plugin), player);
        return true;
    }
}
