package com.github.pietw3lve.fpm.handlers;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.configuration.ConfigurationSection;

import com.github.pietw3lve.fpm.FluxPerMillion;

public class MessageHandler {
    
    private final FluxPerMillion plugin;
    private String inspectMessage;
    private List<String> lookupMessages;
    private String reloadMessage;
    private List<String> statusMessages;
    private List<String> toggleMessages;
    private List<String> overFishingLines;
    private String noPermissionMessage;
    private String playerNotFoundMessage;
    private String playerOnlyCommandMessage;
    private String noActionsFoundMessage;
    private String pageNotFoundMessage;
    private String invalidArgsMessage;
    private String invalidTimeDurationMessage;
    private String invalidPageNumberMessage;

    /**
     * MessageHandler constructor.
     * @param plugin The plugin
     */
    public MessageHandler(FluxPerMillion plugin) {
        this.plugin = plugin;
        this.reload();
    }

    /**
     * Reload the messages.
     */
    public void reload() {
        inspectMessage = plugin.getConfig().getString("messages.inspect");
        lookupMessages = configToList(plugin.getConfig().getConfigurationSection("messages.lookup"));
        reloadMessage = plugin.getConfig().getString("messages.reload");
        statusMessages = configToList(plugin.getConfig().getConfigurationSection("messages.status.conditions"));
        toggleMessages = configToList(plugin.getConfig().getConfigurationSection("messages.toggle"));
        overFishingLines = plugin.getConfig().getStringList("messages.fishing.over_fishing");
        noPermissionMessage = plugin.getConfig().getString("messages.errors.no_permission");
        playerNotFoundMessage = plugin.getConfig().getString("messages.errors.player_not_found");
        playerOnlyCommandMessage = plugin.getConfig().getString("messages.errors.player_only_command");
        noActionsFoundMessage = plugin.getConfig().getString("messages.errors.no_actions_found");
        pageNotFoundMessage = plugin.getConfig().getString("messages.errors.page_not_found");
        invalidArgsMessage = plugin.getConfig().getString("messages.errors.invalid_arguments");
        invalidTimeDurationMessage = plugin.getConfig().getString("messages.errors.invalid_time_duration");
        invalidPageNumberMessage = plugin.getConfig().getString("messages.errors.invalid_page_number");
    }

    /**
     * Get percent colors.
     * @return List of percent colors.
     */
    private List<String> configToList(ConfigurationSection configurationSection) {
        List<String> list = new ArrayList<>();
        for (String key : configurationSection.getKeys(false)) {
            String value = configurationSection.getString(key);

            if (value == null) {
                return new ArrayList<>();
            }

            list.add(value);
        }
        return list;
    }

    /**
     * Get inspect message.
     * @return Inspect message.
     */
    public String getInspectMessage() {
        return inspectMessage;
    }


    /**
     * Get lookup messages.
     * @return List of lookup messages.
     */
    public List<String> getLookupMessages() {
        return lookupMessages;
    }

    /**
     * Get reload message.
     * @return Reload message.
     */
    public String getReloadMessage() {
        return reloadMessage;
    }

    /**
     * Get status messages.
     * @return List of status messages.
     */
    public List<String> getStatusMessages() {
        return statusMessages;
    }

    /**
     * Get toggle messages.
     * @return List of toggle messages.
     */
    public List<String> getToggleMessages() {
        return toggleMessages;
    }

    /**
     * Get over fishing lines.
     * @return List of over fishing lines.
     */
    public List<String> getOverFishingLines() {
        return overFishingLines;
    }

    /**
     * Get no permission message.
     * @return No permission message.
     */
    public String getNoPermissionMessage() {
        return noPermissionMessage;
    }

    /**
     * Get player not found message.
     * @return Player not found message.
     */
    public String getPlayerNotFoundMessage() {
        return playerNotFoundMessage;
    }

    /**
     * Get player only command message.
     * @return Player only command message.
     */
    public String getPlayerOnlyCommandMessage() {
        return playerOnlyCommandMessage;
    }

    /**
     * Get no actions found message.
     * @return No actions found message.
     */
    public String getNoActionsFoundMessage() {
        return noActionsFoundMessage;
    }

    /**
     * Get page not found message.
     * @return Page not found message.
     */
    public String getPageNotFoundMessage() {
        return pageNotFoundMessage;
    }

    /**
     * Get invalid arguments message.
     * @return Invalid arguments message.
     */
    public String getInvalidArgsMessage() {
        return invalidArgsMessage;
    }

    /**
     * Get invalid time duration message.
     * @return Invalid time duration message.
     */
    public String getInvalidTimeDurationMessage() {
        return invalidTimeDurationMessage;
    }

    /**
     * Get invalid page number message.
     * @return Invalid page number message.
     */
    public String getInvalidPageNumberMessage() {
        return invalidPageNumberMessage;
    }
}
