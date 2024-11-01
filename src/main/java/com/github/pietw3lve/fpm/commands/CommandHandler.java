package com.github.pietw3lve.fpm.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import com.github.pietw3lve.fpm.FluxPerMillion;
import com.github.pietw3lve.fpm.utils.GUIUtil;

import net.md_5.bungee.api.ChatColor;

public class CommandHandler implements CommandExecutor, TabCompleter {
    
    private final FluxPerMillion plugin;
    private final GUIUtil guiUtil;
    private final char[] timeUnits = { 'w', 'd', 'h', 'm', 's' };

    /**
     * CommandHandler Constructor.
     * @param plugin
     */
    public CommandHandler(FluxPerMillion plugin, GUIUtil guiUtil) {
        this.plugin = plugin;
        this.guiUtil = guiUtil;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Invalid argument! Usage: /fpm <subcommand>");
            return true;
        }

        String noPermissionMessage = plugin.getMessageHandler().getNoPermissionMessage();
        String playerOnlyCommandMessage = plugin.getMessageHandler().getPlayerOnlyCommandMessage();
        String invalidArgsMessage = plugin.getMessageHandler().getInvalidArgsMessage();
        String usage = "/fpm <subcommand>";
        String subCommand = args[0].toLowerCase();
        switch (subCommand) {
            case "inspect":
                if (args.length == 2) {
                    return new Inspect(plugin).onCommand(sender, command, label, args);
                } else {
                    usage = "/fpm inspect <player>";
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', invalidArgsMessage).replace("%usage%", usage));
                    return true;
                }
            case "toggle":
                if (!(sender instanceof Player)) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', playerOnlyCommandMessage));
                    return true;
                } else {
                    return new Toggle(plugin).onCommand(sender, command, label, args);
                }
            case "status":
                if (!(sender instanceof Player)) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', playerOnlyCommandMessage));
                    return true;
                } else {
                    return new Status(plugin, guiUtil).onCommand(sender, command, label, args);
                }
            case "lookup":
                if (!sender.hasPermission("fpm.lookup")) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', noPermissionMessage));
                    return true;
                } else if (args.length == 3 || args.length == 4) {
                    return new Lookup(plugin).onCommand(sender, command, label, args);
                } else {
                    usage = "/fpm lookup <player> <duration> <page>";
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', invalidArgsMessage).replace("%usage%", usage));
                    return true;
                }
            case "reload":
                if (!sender.hasPermission("fpm.reload")) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', noPermissionMessage));
                    return true;
                }
                return new Reload(plugin).onCommand(sender, command, label, args);
        }
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', invalidArgsMessage).replace("%usage%", usage));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> suggestions = new ArrayList<>();
        
        if (args.length == 1) {
            suggestions.addAll(Arrays.asList("inspect", "toggle", "status"));
            if (sender.hasPermission("fpm.reload")) suggestions.add("reload");
            if (sender.hasPermission("fpm.lookup")) suggestions.add("lookup");
        } else if (args.length >= 2) {
            String subCommand = args[0].toLowerCase();
            if ("lookup".equals(subCommand) && sender.hasPermission("fpm.lookup")) {
                if (args.length == 3) {
                    String duration = args[2];
                    if (duration.length() < 1) {
                        suggestions.add("<duration>");
                    } else if (duration.length() >= 1 && !containsTimeUnits(duration)) {
                        for (char unit : timeUnits) {
                            suggestions.add(duration + unit);
                        }
                    }
                } else if (args.length == 4) {
                    suggestions.add("<page>");
                }
            }
        }
        
        return suggestions.isEmpty() ? null : suggestions;
    }

    /**
     * Checks if the given text contains time units.
     * @param text The text to check.
     * @return True if the text contains time units, false otherwise.
     */
    private boolean containsTimeUnits(String text) {
        return text.matches(".*\\D.*");
    }
}
