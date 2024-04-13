package com.github.pietw3lve.fpm.commands;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.github.pietw3lve.fpm.FluxPerMillion;

import net.md_5.bungee.api.ChatColor;

public class Status implements CommandExecutor {
    
    private final FluxPerMillion plugin;

    /**
     * Status Constructor.
     * @param plugin
     */
    public Status(FluxPerMillion plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        List<String> defaultStatusMessages = plugin.getMessageHandler().getDefaultStatusMessages();
        List<String> statusMessages = plugin.getMessageHandler().getStatusMessages();
        int statusLevel = plugin.getFluxMeter().getStatusLevel();

        try {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', statusMessages.get(statusLevel)));
        } catch (IndexOutOfBoundsException e) {
            sender.sendMessage(defaultStatusMessages.get(statusLevel));
        }
        
        return true;
    }
}
