package com.github.pietw3lve.fpm.handlers;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import com.github.pietw3lve.fpm.FluxPerMillion;

import co.aikar.commands.MessageType;

/**
 * Handles loading and managing messages for the FluxPerMillion plugin.
 */
public class MessageHandler {
    
    private final FluxPerMillion plugin;
    private String inspectMessage;
    private List<String> lookupMessages;
    private String reloadMessage;
    private StatusMessages statusMessages;
    private List<String> toggleMessages;
    private List<String> overFishingLines;
    private String noPermissionMessage;
    private String playerNotFoundMessage;
    private String playerOnlyCommandMessage;
    private String noActionsFoundMessage;
    private String pageNotFoundMessage;
    private String invalidTimeDurationMessage;
    private String invalidPageNumberMessage;
    private List<String> overpopulatedMessages;
    private List<String> preservedMessages;

    /**
     * MessageHandler constructor.
     * @param plugin The plugin
     */
    public MessageHandler(FluxPerMillion plugin) {
        this.plugin = plugin;
        this.reload();
    }

    /**
     * Reload the messages from the configuration file.
     */
    public void reload() {
        String language = plugin.getConfig().getString("debug.language");
        String langFile = "languages/lang_" + language + ".yml";
        YamlConfiguration config = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), langFile));
        ConfigurationSection messagesSection = config.getConfigurationSection("fpm-messages");

        loadMessages(messagesSection);
        loadErrors(messagesSection);
        loadLocales(langFile, language);
        setColorMessages();
    }

    /**
     * Load general messages from the configuration section.
     * @param messagesSection The configuration section containing the messages.
     */
    private void loadMessages(ConfigurationSection messagesSection) {
        inspectMessage = messagesSection.getString("inspect");
        lookupMessages = configToList(messagesSection.getConfigurationSection("lookup"));
        reloadMessage = messagesSection.getString("reload");
        statusMessages = new StatusMessages(messagesSection.getConfigurationSection("status"));
        toggleMessages = configToList(messagesSection.getConfigurationSection("toggle"));
        overFishingLines = messagesSection.getStringList("fishing.over_fishing");
        overpopulatedMessages = messagesSection.getStringList("farming.overpopulated");
        preservedMessages = messagesSection.getStringList("farming.preserved");
    }

    /**
     * Load error messages from the configuration section.
     * @param messagesSection The configuration section containing the error messages.
     */
    private void loadErrors(ConfigurationSection messagesSection) {
        noActionsFoundMessage = messagesSection.getString("errors.no_actions_found");
        pageNotFoundMessage = messagesSection.getString("errors.page_not_found");
        invalidTimeDurationMessage = messagesSection.getString("errors.invalid_time_duration");
        invalidPageNumberMessage = messagesSection.getString("errors.invalid_page_number");
    }

    /**
     * Set color formatting for different message types.
     */
    private void setColorMessages() {
        plugin.getCommandManager().setFormat(MessageType.SYNTAX, 1, ChatColor.RED);
        plugin.getCommandManager().setFormat(MessageType.HELP, 1, ChatColor.GOLD);
        plugin.getCommandManager().setFormat(MessageType.HELP, 2, ChatColor.WHITE);
        plugin.getCommandManager().setFormat(MessageType.HELP, 3, ChatColor.GRAY);
    }

    /**
     * Load locale files for the specified language.
     * @param langFile The language file name.
     * @param language The language code.
     */
    private void loadLocales(String langFile, String language) {
        try {
            plugin.getCommandManager().getLocales().loadYamlLanguageFile(langFile, new Locale(language));
        } catch (Exception e) {
            plugin.getLogger().log(java.util.logging.Level.SEVERE, "Failed to load language: " + language, e);
        }
    }

    /**
     * Convert a configuration section to a list of strings.
     * @param configurationSection The configuration section to convert.
     * @return List of strings from the configuration section.
     */
    private static List<String> configToList(ConfigurationSection configurationSection) {
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
    public StatusMessages getStatusMessages() {
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

    public String getRandomOverpopulatedMessage(String entity) {
        return ChatColor.translateAlternateColorCodes('&', overpopulatedMessages.get(new Random().nextInt(overpopulatedMessages.size())).replace("{entity}", entity));
    }

    public String getRandomPreservedMessage(String entity) {
        return ChatColor.translateAlternateColorCodes('&', preservedMessages.get(new Random().nextInt(preservedMessages.size())).replace("{entity}", entity));
    }

    /**
     * Class representing status messages.
     */
    public static class StatusMessages {

        public final MenuMessages menu;
        public final WorldMessages world;
        public final PlayerMessages player;
        public final EnergyMessages energy;
        public final AgricultureMessages agriculture;
        public final WasteMessages waste;
        public final WildlifeMessages wildlife;

        /**
         * StatusMessages constructor.
         * @param statusSection The configuration section containing the status messages.
         */
        public StatusMessages(ConfigurationSection statusSection) {
            menu = new MenuMessages(statusSection.getConfigurationSection("menu"));
            world = new WorldMessages(statusSection.getConfigurationSection("world"));
            player = new PlayerMessages(statusSection.getConfigurationSection("player"));
            energy = new EnergyMessages(statusSection.getConfigurationSection("energy"));
            agriculture = new AgricultureMessages(statusSection.getConfigurationSection("agriculture"));
            waste = new WasteMessages(statusSection.getConfigurationSection("waste"));
            wildlife = new WildlifeMessages(statusSection.getConfigurationSection("wildlife"));
        }

        /**
         * Class representing menu messages.
         */
        public static class MenuMessages {

            public final String name;
            public final String history;
            public final String graph;

            /**
             * MenuMessages constructor.
             * @param menuSection The configuration section containing the menu messages.
             */
            public MenuMessages(ConfigurationSection menuSection) {
                name = menuSection.getString("name");
                history = menuSection.getString("history");
                graph = menuSection.getString("graph");
            }
        }

        /**
         * Class representing world messages.
         */
        public static class WorldMessages {

            public final String name;
            public final String health;
            public final List<String> conditions;
            public final String check;
            public final String lore;

            /**
             * WorldMessages constructor.
             * @param worldSection The configuration section containing the world messages.
             */
            public WorldMessages(ConfigurationSection worldSection) {
                name = worldSection.getString("name");
                health = worldSection.getString("health");
                conditions = configToList(worldSection.getConfigurationSection("conditions"));
                check = worldSection.getString("check");
                lore = worldSection.getString("lore");
            }
        }

        /**
         * Class representing player messages.
         */
        public static class PlayerMessages {

            public final String name;
            public final String contribution;
            public final String lore;

            /**
             * PlayerMessages constructor.
             * @param playerSection The configuration section containing the player messages.
             */
            public PlayerMessages(ConfigurationSection playerSection) {
                name = playerSection.getString("name");
                contribution = playerSection.getString("contribution");
                lore = playerSection.getString("lore");
            }
        }

        /**
         * Class representing energy messages.
         */
        public static class EnergyMessages {

            public final String name;
            public final String contribution;
            public final String lore;

            /**
             * EnergyMessages constructor.
             * @param energySection The configuration section containing the energy messages.
             */
            public EnergyMessages(ConfigurationSection energySection) {
                name = energySection.getString("name");
                contribution = energySection.getString("contribution");
                lore = energySection.getString("lore");
            }
        }

        /**
         * Class representing agriculture messages.
         */
        public static class AgricultureMessages {

            public final String name;
            public final String contribution;
            public final String lore;

            /**
             * AgricultureMessages constructor.
             * @param agricultureSection The configuration section containing the agriculture messages.
             */
            public AgricultureMessages(ConfigurationSection agricultureSection) {
                name = agricultureSection.getString("name");
                contribution = agricultureSection.getString("contribution");
                lore = agricultureSection.getString("lore");
            }
        }

        /**
         * Class representing waste messages.
         */
        public static class WasteMessages {

            public final String name;
            public final String contribution;
            public final String lore;

            /**
             * WasteMessages constructor.
             * @param wasteSection The configuration section containing the waste messages.
             */
            public WasteMessages(ConfigurationSection wasteSection) {
                name = wasteSection.getString("name");
                contribution = wasteSection.getString("contribution");
                lore = wasteSection.getString("lore");
            }
        }

        /**
         * Class representing wildlife messages.
         */
        public static class WildlifeMessages {

            public final String name;
            public final String contribution;
            public final String lore;

            /**
             * WildlifeMessages constructor.
             * @param wildlifeSection The configuration section containing the wildlife messages.
             */
            public WildlifeMessages(ConfigurationSection wildlifeSection) {
                name = wildlifeSection.getString("name");
                contribution = wildlifeSection.getString("contribution");
                lore = wildlifeSection.getString("lore");
            }
        }
    }
}
