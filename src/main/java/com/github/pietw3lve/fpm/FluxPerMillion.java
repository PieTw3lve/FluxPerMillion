package com.github.pietw3lve.fpm;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.github.pietw3lve.fpm.commands.FPMCommands;
import com.github.pietw3lve.fpm.handlers.DeadlyDisastersHandler;
import com.github.pietw3lve.fpm.handlers.EffectsHandler;
import com.github.pietw3lve.fpm.handlers.FishTrackerHandler;
import com.github.pietw3lve.fpm.handlers.FluxHandler;
import com.github.pietw3lve.fpm.handlers.MessageHandler;
import com.github.pietw3lve.fpm.handlers.PlaceholderHandler;
import com.github.pietw3lve.fpm.handlers.TreeHandler;
import com.github.pietw3lve.fpm.items.*;
import com.github.pietw3lve.fpm.listeners.EventListener;
import com.github.pietw3lve.fpm.listeners.GUIListener;
import com.github.pietw3lve.fpm.listeners.fpm.FluxLevelChangeListener;
import com.github.pietw3lve.fpm.listeners.fpm.StatusLevelChangeListener;
import com.github.pietw3lve.fpm.utils.SQLiteUtil;
import com.google.common.collect.ImmutableList;

import co.aikar.commands.ConditionFailedException;
import co.aikar.commands.PaperCommandManager;

import com.github.pietw3lve.fpm.utils.ConfigUpdaterUtil;
import com.github.pietw3lve.fpm.utils.GUIUtil;


public class FluxPerMillion extends JavaPlugin {

	private PaperCommandManager commandManager;
	private SQLiteUtil dbHandler;
	private GUIUtil guiUtil;
	private MessageHandler messageHandler;
	private FluxHandler fluxMeter;
	private DeadlyDisastersHandler deadlyDisastersHandler;
	private EffectsHandler effectsHandler;
	private PlaceholderHandler placeholderHandler;
	private FishTrackerHandler fishTracker;
	private TreeHandler treeUtils;
	private RespirationMask respirationMask;
	private RespirationMaskEX respirationMaskEX;

	public void onEnable() {
		loadPersonalData();
		initializeHandlers();
		loadDependencies();
		registerCommands();
		registerEventListeners();
		registerCustomItems();
	}

	/**
	 * Initialize all handlers.
	 */
	private void initializeHandlers() {
		this.commandManager = new PaperCommandManager(this);
		this.guiUtil = new GUIUtil();
		this.messageHandler = new MessageHandler(this);
		this.fluxMeter = new FluxHandler(this);
		this.deadlyDisastersHandler = new DeadlyDisastersHandler(this);
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
			deadlyDisastersHandler.registerDeadlyDisasters();
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
		saveDefaultLanguageFile();
		
		try {
			ConfigUpdaterUtil.update(this, "config.yml", new File(getDataFolder(), "config.yml"), Arrays.asList("effects", "deadly_disasters"));
		} catch (IOException e) {
			e.printStackTrace();
		}

		reloadConfig();
	}

	/**
	 * Copy the default language file from the resources to the data folder.
	 */
	private void saveDefaultLanguageFile() {
		File languagesFolder = new File(getDataFolder(), "languages");
		if (!languagesFolder.exists()) {
			languagesFolder.mkdirs();
		}
		for (String language : new String[] { "en" }) {
			File file = new File(languagesFolder, "lang_" + language + ".yml");
			if (!file.exists()) {
				saveResource("languages/lang_" + language + ".yml", false);
			}
		}
	}

	/**
	 * Register command executors.
	 */
	private void registerCommands() {
		this.commandManager.enableUnstableAPI("help");
		this.commandManager.registerCommand(new FPMCommands(this));

		// Register auto-completions
		this.commandManager.getCommandCompletions().registerAsyncCompletion("duration", c -> {
			String input = c.getInput();
			if (input.matches("\\d+")) {
				return ImmutableList.of(input + "w", input + "d", input + "h", input + "m", input + "s");
			}
			return ImmutableList.of();
		});

		// Register conditions
		this.commandManager.getCommandConditions().addCondition(Double.class, "positive", (context, executionContext, value) -> {
			if (value <= 0) {
				throw new ConditionFailedException("");
			}
        });
		this.commandManager.getCommandConditions().addCondition(Integer.class, "category", (context, executionContext, value) -> {
			if (value < 1 || value > 4) {
				throw new ConditionFailedException("");
			}
        });
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
	 * Register custom items.
	 */
	private void registerCustomItems() {
		respirationMask = new RespirationMask(this);
		respirationMask.reload();
		respirationMaskEX = new RespirationMaskEX(this);
		respirationMaskEX.reload();
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
	 * Returns the PaperCommandManager instance.
	 * @return PaperCommandManager
	 */
	public PaperCommandManager getCommandManager() {
		return this.commandManager;
	}

	/**
	 * Returns the SQLiteUtil instance.
	 * @return SQLiteUtil
	 */
	public SQLiteUtil getDbUtil() {
		return this.dbHandler;
	}

	/**
	 * Returns the GUIUtil instance.
	 * @return GUIUtil
	 */
	public GUIUtil getGUIUtil() {
		return this.guiUtil;
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
	public FluxHandler getFluxMeter() {
		return this.fluxMeter;
	}

	/**
	 * Returns the DeadlyDisastersHandler instance.
	 * @return DeadlyDisastersHandler
	 */
	public DeadlyDisastersHandler getDeadlyDisasters() {
		return this.deadlyDisastersHandler;
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

	/**
	 * Returns the RespirationMask instance.
	 * @return RespirationMask
	 */
	public RespirationMask getRespirationMask() {
		return this.respirationMask;
	}

	/**
	 * Returns the RespirationMaskEX instance.
	 * @return RespirationMaskEX
	 */
	public RespirationMaskEX getRespirationMaskEX() {
		return this.respirationMaskEX;
	}
}
