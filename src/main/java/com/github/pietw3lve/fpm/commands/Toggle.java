package com.github.pietw3lve.fpm.commands;

import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.github.pietw3lve.fpm.FluxPerMillion;
import com.github.pietw3lve.fpm.handlers.FluxHandler;

import net.md_5.bungee.api.ChatColor;

public class Toggle {
 
    private final FluxPerMillion plugin;

    /**
     * Toggle Constructor.
     * @param plugin
     */
    public Toggle(FluxPerMillion plugin) {
        this.plugin = plugin;
    }

    public boolean execute(CommandSender sender) {
        if (!(sender instanceof Player)) {
            String playerOnlyCommandMessage = plugin.getMessageHandler().getPlayerOnlyCommandMessage();
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', playerOnlyCommandMessage));
            return true;
        }

        Player player = (Player) sender;
        List<String> toggleMessages = plugin.getMessageHandler().getToggleMessages();
        FluxHandler fluxMeter = plugin.getFluxMeter();

        if (fluxMeter.toggle(player)) {
            try {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', toggleMessages.get(0)));
            } catch (IndexOutOfBoundsException e) {
                plugin.getLogger().warning("Toggle message not found.");    
            }
        } else {
            try {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', toggleMessages.get(1)));
            } catch (IndexOutOfBoundsException e) {
                plugin.getLogger().warning("Toggle message not found.");
            }
        }

        return true;
    }
}
