package com.github.pietw3lve.fpm.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Conditions;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.HelpCommand;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import com.github.pietw3lve.fpm.FluxPerMillion;

@CommandAlias("fpm")
public class FPMCommands extends BaseCommand {

    private final FluxPerMillion plugin;

    public FPMCommands(FluxPerMillion plugin) {
        this.plugin = plugin;
    }

    @HelpCommand
    @Description("Displays a list of available commands and their descriptions.")
    public void onHelp(CommandSender sender, CommandHelp help) {
        help.showHelp();
    }

    @Subcommand("inspect")
    @CommandCompletion("@players")
    @Syntax("<player>")
    @Description("View detailed Flux information for a specific player.")
    public void onInspect(CommandSender sender, OfflinePlayer player) {
        new Inspect(plugin).execute(sender, player);
    }

    @Subcommand("toggle")
    @Description("Toggle the visibility of the Flux meter on or off.")
    public void onToggle(CommandSender sender) {
        new Toggle(plugin).execute(sender);
    }

    @Subcommand("status")
    @Description("Check the current status and statistics of the world.")
    public void onStatus(CommandSender sender) {
        new Status(plugin).execute(sender);
    }

    @Subcommand("add")
    @CommandPermission("fpm.add")
    @CommandCompletion("@players <amount> @range:1-4")
    @Syntax("<player> <amount> <category>")   
    @Description("Add Flux to a player.")
    public void onAdd(CommandSender sender, OfflinePlayer player, @Conditions("positive") Double amount, @Conditions("category") Integer category) {
        new Add(plugin).execute(sender, player, amount, category);
    }

    @Subcommand("remove")
    @CommandPermission("fpm.remove")
    @CommandCompletion("@players <amount> @range:1-4")
    @Syntax("<player> <amount> <category>")   
    @Description("Remove Flux from a player.")
    public void onRemove(CommandSender sender, OfflinePlayer player, @Conditions("positive") Double amount, @Conditions("category") Integer category) {
        new Remove(plugin).execute(sender, player, amount, category);
    }

    @Subcommand("lookup")
    @CommandPermission("fpm.lookup")
    @CommandCompletion("@players @duration <page>")
    @Syntax("<player> <duration> [page]")
    @Description("Retrieve a player's Flux activity.")
    public void onLookup(CommandSender sender, OfflinePlayer player, String duration, @Default("1") @Conditions("positive") Double page) {
        new Lookup(plugin).execute(sender, player, duration, page.intValue());
    }

    @Subcommand("reload")
    @CommandPermission("fpm.reload")
    @Description("Reloads the FluxPerMillion plugin configuration and settings.")
    public void onReload(CommandSender sender) {
        new Reload(plugin).execute(sender);
    }
}
