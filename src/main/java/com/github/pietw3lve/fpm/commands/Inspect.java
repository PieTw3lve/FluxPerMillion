package com.github.pietw3lve.fpm.commands;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import com.github.pietw3lve.fpm.FluxPerMillion;

import net.md_5.bungee.api.ChatColor;

public class Inspect {

    private final FluxPerMillion plugin;

    /**
     * Inspect Constructor.
     * @param plugin
     */
    public Inspect(FluxPerMillion plugin) {
        this.plugin = plugin;
    }

    public boolean execute(CommandSender sender, OfflinePlayer player) {
        if (player == null) {
            String playerNotFoundMessage = plugin.getMessageHandler().getPlayerNotFoundMessage();
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', playerNotFoundMessage));
            return true;
        }
        
        String inspectMessage = plugin.getMessageHandler().getInspectMessage();
        double playerFlux = plugin.getDbUtil().getPlayerFlux(player);
        String points = String.format((playerFlux >= 0 ? ChatColor.RED : ChatColor.GREEN) + "%.2f", playerFlux);
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', inspectMessage.replace("{player}", player.getName()).replace("{points}", points)));

        return true;
    }
}
