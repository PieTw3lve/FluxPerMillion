package com.github.pietw3lve.fpm.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Description;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import com.github.pietw3lve.fpm.FluxPerMillion;
@CommandAlias("fpm")
public class FPMCommands extends BaseCommand {

    private final FluxPerMillion plugin;

    public FPMCommands(FluxPerMillion plugin) {
        this.plugin = plugin;
    }

    @Subcommand("inspect")
    @CommandCompletion("@players")
    @Syntax("<player>")
    @Description("Inspect a player's Flux.")
    public void onInspect(CommandSender sender, OfflinePlayer player) {
        new Inspect(plugin).execute(sender, player);
    }

    @Subcommand("toggle")
    @Description("Toggle Flux meter visibility.")
    public void onToggle(CommandSender sender) {
        new Toggle(plugin).execute(sender);
    }

    @Subcommand("status")
    @Description("Check world status.")
    public void onStatus(CommandSender sender) {
        new Status(plugin).execute(sender);
    }

    @Subcommand("lookup")
    @CommandPermission("fpm.lookup")
    @CommandCompletion("@players @duration <page>")
    @Syntax("<player> <duration> [page]")
    @Description("Lookup a player's Flux activity.")
    public void onLookup(CommandSender sender, OfflinePlayer player, String duration, @Default("1") int page) {
        new Lookup(plugin).execute(sender, player, duration, page);
    }

    @Subcommand("reload")
    @CommandPermission("fpm.reload")
    @Description("Reload the FluxPerMillion plugin.")
    public void onReload(CommandSender sender) {
        new Reload(plugin).execute(sender);
    }

}
