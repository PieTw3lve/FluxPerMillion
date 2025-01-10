package com.github.pietw3lve.fpm.commands;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.github.pietw3lve.fpm.FluxPerMillion;
import com.github.pietw3lve.fpm.events.FluxLevelChangeEvent;
import com.github.pietw3lve.fpm.utils.SQLiteUtil.ActionCategory;

import net.md_5.bungee.api.ChatColor;

public class Add {
    
    private final FluxPerMillion plugin;

    /**
     * Inspect Constructor.
     * @param plugin
     */
    public Add(FluxPerMillion plugin) {
        this.plugin = plugin;
    }

    public boolean execute(CommandSender sender, OfflinePlayer player, double amount, int category) {
        String addMessage = plugin.getMessageHandler().getAddMessage();
        FluxLevelChangeEvent fluxEvent = new FluxLevelChangeEvent(plugin.getFluxMeter(), player.getLocation(), (Player) player, null, "received", "flux", amount, ActionCategory.fromValue(category));
        plugin.getServer().getPluginManager().callEvent(fluxEvent);
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', addMessage.replace("{player}", player.getName()).replace("{points}", String.valueOf(amount))));
        return true;
    }
}
