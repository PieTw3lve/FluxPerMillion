package com.github.pietw3lve.fpm.commands;

import org.bukkit.command.CommandSender;

import com.github.pietw3lve.fpm.FluxPerMillion;

import net.md_5.bungee.api.ChatColor;

public class Reload {
    
    private final FluxPerMillion plugin;

    /**
     * Reload Constructor.
     * @param plugin
     */
    public Reload(FluxPerMillion plugin) {
        this.plugin = plugin;
    }

    public boolean execute(CommandSender sender) {
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
