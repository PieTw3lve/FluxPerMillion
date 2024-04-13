package com.github.pietw3lve.fpm.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.github.pietw3lve.fpm.FluxPerMillion;

import net.md_5.bungee.api.ChatColor;

public class Reload implements CommandExecutor {
    
    private final FluxPerMillion plugin;

    /**
     * Reload Constructor.
     * @param plugin
     */
    public Reload(FluxPerMillion plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        plugin.reloadConfig();
        plugin.getMessageHandler().reload();
        plugin.getFluxMeter().reload();
        plugin.getDeadlyDisasters().reload();
        plugin.getEffectsHandler().reload();
        plugin.getPlaceholderHandler().reload();
        plugin.getFishTracker().reload();
        String reloadMessage = plugin.getMessageHandler().getReloadMessage();
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', reloadMessage));
        return true;
    }
}
