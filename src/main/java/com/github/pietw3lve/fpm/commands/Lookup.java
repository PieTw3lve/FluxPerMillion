package com.github.pietw3lve.fpm.commands;

import java.util.List;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.github.pietw3lve.fpm.FluxPerMillion;
import com.github.pietw3lve.fpm.utils.ListPaginatorUtil;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class Lookup implements CommandExecutor {
    
    private final FluxPerMillion plugin;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Lookup Constructor.
     * @param plugin
     */
    public Lookup(FluxPerMillion plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        OfflinePlayer player = plugin.getPlayer(args[1]);
        String timeString = parseTimeString(args[2]);
        int page; 

        if (timeString == null) {
            sender.sendMessage(ChatColor.RED + "Invalid time duration! Usage: /fpm lookup <player> <duration> <page>");
            return true;
        }

        try {
            page = (args.length == 4) ? Integer.parseInt(args[3]) : 1;
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Invalid page number! Usage: /fpm lookup <player> <duration> <page>");
            return true;
        }

        List<String> playerActions = plugin.getDbUtil().getPlayerActions(player, timeString);
        if (playerActions.isEmpty() ) {
            sender.sendMessage(ChatColor.RED + "No actions found for " + args[1] + ".");
            return true;
        }

        try {
            ListPaginatorUtil<String, String> paginator = new ListPaginatorUtil<>(10, ListPaginatorUtil.MessagePlatform.NORMAL, String::toString);
            TextComponent previousArrow = getTextCommand("/fpm lookup " + args[1] + " " + args[2] + " " + (page - 1), "◀", "Previous page");
            TextComponent nextArrow = getTextCommand("/fpm lookup " + args[1] + " " + args[2] + " " + (page + 1), "▶", "Next page");
            paginator.setHeader((target, pageIndex, pageCount) -> target.sendMessage(ChatColor.RESET + "-----" + ChatColor.GOLD + " FluxPerMillion | Lookup Results " + ChatColor.RESET + "-----"));
            paginator.setFooter((target, pageIndex, pageCount) -> target.spigot().sendMessage(previousArrow, new TextComponent(String.format(" %s %s/%s ", ChatColor.GOLD + "Page", ChatColor.RESET.toString() + pageIndex, pageCount)), nextArrow, new TextComponent(String.format(" %s%s %s%s ", ChatColor.GRAY + "(", ChatColor.RESET.toString() + playerActions.size(), ChatColor.GOLD + "entries", ChatColor.GRAY + ")"))));
            paginator.sendPage(playerActions, sender, page);
        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + "Page does not exist!");
        }

        return true;
    }

    /**
     * Parses a time string into a LocalDateTime object.
     * @param timeString The time string to parse.
     * @return The parsed LocalDateTime object.
     */
    public String parseTimeString(String timeString) {
        LocalDateTime startDate = null;

        long amount = Long.parseLong(timeString.substring(0, timeString.length() - 1));
        String unit = timeString.substring(timeString.length() - 1).toLowerCase();
        switch (unit) {
            case "s":
                startDate = LocalDateTime.now(ZoneId.of("UTC")).minusSeconds(amount);
                break;
            case "m":
                startDate = LocalDateTime.now(ZoneId.of("UTC")).minusMinutes(amount);
                break;
            case "h":
                startDate = LocalDateTime.now(ZoneId.of("UTC")).minusHours(amount);
                break;
            case "w":
                startDate = LocalDateTime.now(ZoneId.of("UTC")).minusWeeks(amount);
                break;
            case "d":
                startDate = LocalDateTime.now(ZoneId.of("UTC")).minusDays(amount);
                break;
            default:
                return null;
        }

        return startDate.format(formatter);
    }

    /**
     * Creates a TextComponent with a command, text, and hover text.
     * @param command The command to run.
     * @param text The text to display.
     * @param hoverText The hover text to display.
     * @return The created TextComponent.
     */
    public TextComponent getTextCommand(String command, String text, String hoverText) {
        TextComponent component = new TextComponent(text);
        component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(hoverText)));
        component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command));
        return component;
    }
}