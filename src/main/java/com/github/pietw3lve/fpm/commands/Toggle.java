package com.github.pietw3lve.fpm.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.github.pietw3lve.fpm.FluxPerMillion;
import com.github.pietw3lve.fpm.handlers.FluxMeterHandler;

import net.md_5.bungee.api.ChatColor;

public class Toggle implements CommandExecutor {
 
    private final FluxPerMillion plugin;

    /**
     * Toggle Constructor.
     * @param plugin
     */
    public Toggle(FluxPerMillion plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;
        FluxMeterHandler fluxMeter = plugin.getFluxMeter();

        if (fluxMeter.toggle(player)) {
            player.sendMessage(ChatColor.GREEN + "Flux meter is now visible.");
        } else {
            player.sendMessage(ChatColor.RED + "Flux meter is now hidden.");
        }

        return true;
    }
}
