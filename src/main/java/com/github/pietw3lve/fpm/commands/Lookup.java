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
        String timeString = null;
        
        try {
            timeString = parseTimeString(args[2]);
        } catch (Exception e) {
            String invalidTimeDurationMessage = plugin.getMessageHandler().getInvalidTimeDurationMessage();
            String usage = "/fpm lookup <player> <duration> <page>";
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', invalidTimeDurationMessage).replace("%usage%", usage));
            return true;
        }
        
        int page; 

        if (timeString == null) {
            String invalidTimeDurationMessage = plugin.getMessageHandler().getInvalidTimeDurationMessage();
            String usage = "/fpm lookup <player> <duration> <page>";
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', invalidTimeDurationMessage).replace("%usage%", usage));
            return true;
        }

        try {
            page = (args.length == 4) ? Integer.parseInt(args[3]) : 1;
        } catch (NumberFormatException e) {
            String invalidPageNumberMessage = plugin.getMessageHandler().getInvalidPageNumberMessage();
            String usage = "/fpm lookup <player> <duration> <page>";
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', invalidPageNumberMessage).replace("%usage%", usage));
            return true;
        }

        List<String> playerActions = plugin.getDbUtil().getPlayerActions(player, timeString);
        if (playerActions.isEmpty() ) {
            String noActionsFoundMessage = plugin.getMessageHandler().getNoActionsFoundMessage();
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', noActionsFoundMessage).replace("%player%", player.getName()));
            return true;
        }

        List<String> defaultLookupMessages = plugin.getMessageHandler().getDefaultLookupMessages();
        List<String> lookupMessages = plugin.getMessageHandler().getLookupMessages();
        if (lookupMessages.size() < 3) {
            lookupMessages.clear();
            lookupMessages.addAll(defaultLookupMessages);
        }

        try {
            ListPaginatorUtil<String, String> paginator = new ListPaginatorUtil<>(10, ListPaginatorUtil.MessagePlatform.NORMAL, String::toString);
            TextComponent previousArrow = getTextCommand("/fpm lookup " + args[1] + " " + args[2] + " " + (page - 1), "◀", "Previous page");
            TextComponent nextArrow = getTextCommand("/fpm lookup " + args[1] + " " + args[2] + " " + (page + 1), "▶", "Next page");
            paginator.setHeader((target, pageIndex, pageCount) -> target.sendMessage(ChatColor.translateAlternateColorCodes('&', lookupMessages.get(0).replace("%index%", String.valueOf(pageIndex)).replace("%total%", String.valueOf(pageCount)).replace("%action%", String.valueOf(playerActions.size())))));
            paginator.setFooter((target, pageIndex, pageCount) -> target.spigot().sendMessage(previousArrow, new TextComponent(ChatColor.translateAlternateColorCodes('&', lookupMessages.get(1)).replace("%index%", String.valueOf(pageIndex)).replace("%total%", String.valueOf(pageCount)).replace("%action%", String.valueOf(playerActions.size()))), nextArrow, new TextComponent(ChatColor.translateAlternateColorCodes('&', lookupMessages.get(2).replace("%index%", String.valueOf(pageIndex)).replace("%total%", String.valueOf(pageCount)).replace("%action%", String.valueOf(playerActions.size()))))));
            paginator.sendPage(playerActions, sender, page);
        } catch (Exception e) {
            String pageNotFoundMessage = plugin.getMessageHandler().getPageNotFoundMessage();
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', pageNotFoundMessage));
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