package com.github.pietw3lve.fpm;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import com.github.pietw3lve.fpm.commands.CommandHandler;
import com.github.pietw3lve.fpm.handlers.DeadlyDisastersHandler;
import com.github.pietw3lve.fpm.handlers.EffectsHandler;
import com.github.pietw3lve.fpm.handlers.FishTrackerHandler;
import com.github.pietw3lve.fpm.handlers.FluxMeterHandler;
import com.github.pietw3lve.fpm.handlers.MessageHandler;
import com.github.pietw3lve.fpm.handlers.PlaceholderHandler;
import com.github.pietw3lve.fpm.handlers.TreeHandler;
import com.github.pietw3lve.fpm.listeners.EventListener;
import com.github.pietw3lve.fpm.listeners.GUIListener;
import com.github.pietw3lve.fpm.listeners.fpm.FluxLevelChangeListener;
import com.github.pietw3lve.fpm.listeners.fpm.StatusLevelChangeListener;
import com.github.pietw3lve.fpm.utils.SQLiteUtil;
import com.github.pietw3lve.fpm.utils.ConfigUpdaterUtil;
import com.github.pietw3lve.fpm.utils.GUIUtil;


public class FluxPerMillion extends JavaPlugin {

	private SQLiteUtil dbHandler;
	private GUIUtil guiUtil;
	private MessageHandler messageHandler;
	private FluxMeterHandler fluxMeter;
	private DeadlyDisastersHandler deadlyDisasters;
	private EffectsHandler effectsHandler;
	private PlaceholderHandler placeholderHandler;
	private FishTrackerHandler fishTracker;
	private TreeHandler treeUtils;

	public void onEnable() {
		loadPersonalData();
		initializeHandlers();
		loadDependencies();
		registerCommands();
		registerEventListeners();
	}

	/**
	 * Initialize all handlers.
	 */
	private void initializeHandlers() {
		this.guiUtil = new GUIUtil();
		this.messageHandler = new MessageHandler(this);
		this.fluxMeter = new FluxMeterHandler(this);
		this.deadlyDisasters = new DeadlyDisastersHandler(this);
		this.effectsHandler = new EffectsHandler(this);
		this.placeholderHandler = new PlaceholderHandler(this);
		this.fishTracker = new FishTrackerHandler(this);
		this.treeUtils = new TreeHandler(this);
	}

	/**
	 * Load dependencies.
	 */
	private void loadDependencies() {
		if (Bukkit.getPluginManager().isPluginEnabled("DeadlyDisasters")) {
			deadlyDisasters.registerDeadlyDisasters();
		}
		if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
			placeholderHandler.registerPlaceholders();
		}
	}

	/**
	 * Save the default configuration file.
	 */
	private void loadPersonalData() {
		this.dbHandler = new SQLiteUtil(this);
		dbHandler.initializeDatabase();
		saveDefaultConfig();
		
		try {
			ConfigUpdaterUtil.update((Plugin) this, "config.yml", new File(getDataFolder(), "config.yml"), Arrays.asList("effects", "deadly_disasters"));
		} catch (IOException e) {
			e.printStackTrace();
		}

		reloadConfig();
	}

	/**
	 * Register command executors.
	 */
	private void registerCommands() {
		getCommand("fpm").setExecutor(new CommandHandler(this, this.guiUtil));
	}

	/**
	 * Register event listeners.
	 */
	private void registerEventListeners() {
		// Bukkit Listeners
		getServer().getPluginManager().registerEvents(new EventListener(this), this);
		// GUI Listener
		getServer().getPluginManager().registerEvents(new GUIListener(this.guiUtil), this);
		// Custom Listeners
		getServer().getPluginManager().registerEvents(new FluxLevelChangeListener(this), this);
		getServer().getPluginManager().registerEvents(new StatusLevelChangeListener(this), this);
	}

	/**
	 * Send a debug message to the console.
	 * @param message The string message (or a key in the message catalog)
	 */
	public void sendDebugMessage(String message) {
		if (getConfig().getBoolean("debug.messages", false)) {
			getLogger().info(message);
		}
	}

	/**
	 * Gets a player object by the given username.
	 * @param name The name to look up
	 * @return A player if one was found, null otherwise
	 */
	public OfflinePlayer getPlayer(String name) {
		Player player = this.getServer().getPlayer(name);
		if (player == null) {
			OfflinePlayer[] offlinePlayers = this.getServer().getOfflinePlayers();
			for (OfflinePlayer offlinePlayer : offlinePlayers) {
				if (offlinePlayer.getName().equalsIgnoreCase(name)) {
					return offlinePlayer;
				}
			}
			return null;
		}
		return player;
	}

	/**
	 * Returns the SQLiteUtil instance.
	 * @return SQLiteUtil
	 */
	public SQLiteUtil getDbUtil() {
		return this.dbHandler;
	}

	/**
	 * Returns the MessageHandler instance.
	 * @return MessageHandler
	 */
	public MessageHandler getMessageHandler() {
		return this.messageHandler;
	}

	/**
	 * Returns the FluxMeterHandler instance.
	 * @return FluxMeterHandler
	 */
	public FluxMeterHandler getFluxMeter() {
		return this.fluxMeter;
	}

	/**
	 * Returns the DeadlyDisastersHandler instance.
	 * @return DeadlyDisastersHandler
	 */
	public DeadlyDisastersHandler getDeadlyDisasters() {
		return this.deadlyDisasters;
	}

	/**
	 * Returns the EffectsHandler instance.
	 * @return EffectsHandler
	 */
	public EffectsHandler getEffectsHandler() {
		return this.effectsHandler;
	}

	/**
	 * Returns the PlaceholderHandler instance.
	 * @return PlaceholderHandler
	 */
	public PlaceholderHandler getPlaceholderHandler() {
		return this.placeholderHandler;
	}

	/**
	 * Returns the FishTrackerUtil instance.
	 * @return FishTrackerUtil
	 */
	public FishTrackerHandler getFishTracker() {
		return this.fishTracker;
	}

	/**
	 * Returns the TreeUtil instance.
	 * @return TreeUtil
	 */
	public TreeHandler getTreeUtils() {
		return this.treeUtils;
	}
}
