package com.github.pietw3lve.fpm.commands;

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
        String[] statusLabels = {
            ChatColor.GREEN + ChatColor.BOLD.toString() + ChatColor.UNDERLINE + "STABLE" + ChatColor.GREEN + ChatColor.BOLD.toString() + ":" ,
            ChatColor.GOLD + ChatColor.BOLD.toString() + ChatColor.UNDERLINE + "WARNING" + ChatColor.GOLD + ChatColor.BOLD.toString() + ":",
            ChatColor.GOLD + ChatColor.BOLD.toString() + ChatColor.UNDERLINE + "WARNING" + ChatColor.GOLD + ChatColor.BOLD.toString() + ":" ,
            ChatColor.DARK_RED + ChatColor.BOLD.toString() + ChatColor.UNDERLINE + "CRITICAL" + ChatColor.DARK_RED + ChatColor.BOLD.toString() + ":"
        };
        String[] statusMessages = {
            ChatColor.GREEN + "Flux capacity is stable.",
            ChatColor.YELLOW + "Flux capacity is becoming a concern.",
            ChatColor.RED + "Flux capacity is increasing rapidly.",
            ChatColor.DARK_RED + "Flux capacity is at a critical level!"
        };
        int statusLevel = plugin.getFluxMeter().getStatusLevel();
        sender.sendMessage(statusLabels[statusLevel] + " " + statusMessages[statusLevel]);
        return true;
    }
}
