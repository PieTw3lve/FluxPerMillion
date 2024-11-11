package com.github.pietw3lve.fpm.handlers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import com.github.pietw3lve.fpm.FluxPerMillion;
import com.github.pietw3lve.fpm.utils.RandomCollectionUtil;

import com.github.jewishbanana.deadlydisasters.events.disasters.*;

public class DeadlyDisastersHandler {
    
    private final FluxPerMillion plugin;
    private final FluxHandler fluxMeter;
    private BukkitTask disastersTask;
    private long minInterval;
    private long maxInterval;
    private List<Double> probabilities;
    private List<Map<String, Double>> disasters;
    private List<Map<String, Double>> difficulties;
    private boolean enabled;
    private boolean preventDisastersWhenIdle;
    
    /**
     * DeadlyDisastersHandler Constructor.
     * @param plugin
     */
    public DeadlyDisastersHandler(FluxPerMillion plugin) {
        this.plugin = plugin;
        this.fluxMeter = plugin.getFluxMeter();
        this.disastersTask = null;
        this.enabled = false;
        this.preventDisastersWhenIdle = false;
    }
    
    /**
     * Checks if a disaster should occur.
     */
    private void checkDisaster() {
        Random rand = new Random();
        int statusLevel = fluxMeter.getStatusLevel();
        double value = rand.nextDouble();
        for (int i = 0; i <= 3; i++) {
            if (value <= probabilities.get(i) && statusLevel == i) {
                String disaster = getDisaster(statusLevel);
                startDisaster(disaster, statusLevel, 0);
                break;
            }
        }
        disastersTask = startScheduledDisasters(minInterval, maxInterval);
    }

    /**
     * Returns a disaster based on the status level of the flux meter.
     * @param statusLevel The status level of the flux meter.
     * @return The disaster to start.
     */
    private String getDisaster(int statusLevel) {
        Map<String, Double> disasters = this.disasters.get(statusLevel);

        if (disasters.isEmpty()) {
            return null;
        }

        RandomCollectionUtil<String> randomCollection = new RandomCollectionUtil<>();
        for (Map.Entry<String, Double> entry : disasters.entrySet()) {
            String disaster = entry.getKey();
            double weight = entry.getValue();
            randomCollection.add(weight, disaster);
        }
        
        String selectedDisaster = randomCollection.next();
        return selectedDisaster;
    }

    /**
     * Starts a disaster based on the status level of the flux meter.
     * @param disaster The disaster to start.
     * @param statusLevel The status level of the flux meter.
     */
    private void startDisaster(String disaster, int statusLevel, int attempts) {
        if (disaster == null) {
            plugin.sendDebugMessage("No disasters found for status level: " + statusLevel);
            retryDisasterStartEvent(statusLevel, attempts);
            return;
        }

        World overworld = getWorld(World.Environment.NORMAL);
        World nether = getWorld(World.Environment.NETHER);
        World end = getWorld(World.Environment.THE_END);
        Collection<? extends Player> players = plugin.getServer().getOnlinePlayers();
        Random rand = new Random();

        if (players.isEmpty() && preventDisastersWhenIdle) {
            plugin.sendDebugMessage("No players found, skipping disaster start event.");
            return;
        }

        Player player = selectPlayer(players, disaster, overworld, nether, end, rand);
        if (player == null) {
            retryDisasterStartEvent(statusLevel, attempts);
            return;
        }

        int disasterLevel = getDisasterLevel(statusLevel, 1, getMaxDisasterLevel(disaster));
        startSpecificDisaster(disaster, disasterLevel, player, overworld, nether, end);
        plugin.sendDebugMessage("Starting " + disaster + " with danger level: " + disasterLevel);
    }

    /**
     * Returns the world based on the environment.
     * @param environment The environment of the world.
     * @return The world with the specified environment.
     */
    private World getWorld(World.Environment environment) {
        return plugin.getServer().getWorlds().stream().filter(w -> w.getEnvironment() == environment).findAny().orElse(null);
    }

    /**
     * Selects a player based on the disaster type.
     * @param players The collection of players to select from.
     * @param disaster The disaster to start.
     * @param overworld The overworld world.
     * @param nether The nether world.
     * @param end The end world.
     * @param rand The random object.
     * @return The selected player.
     */
    private Player selectPlayer(Collection<? extends Player> players, String disaster, World overworld, World nether, World end, Random rand) {
        List<Player> selectedPlayers = null;
        switch (disaster) {
            case "cavein":
            case "geyser":
                selectedPlayers = players.stream().filter(p -> p.getWorld() == overworld || p.getWorld() == nether).collect(Collectors.toList());
                break;
            case "hurricane":
            case "tsunami":
                selectedPlayers = players.stream().filter(p -> p.getWorld() == overworld).collect(Collectors.toList());
                break;
            case "supernova":
                selectedPlayers = players.stream().filter(p -> p.getWorld() == overworld || p.getWorld() == end).collect(Collectors.toList());
                break;
            default:
                selectedPlayers = new ArrayList<>(players);
                break;
        }
        return selectedPlayers.isEmpty() ? null : selectedPlayers.get(rand.nextInt(selectedPlayers.size()));
    }

    private int getMaxDisasterLevel(String disaster) {
        switch (disaster) {
            case "cavein":
            case "earthquake":
            case "extremewinds":
            case "hurricane":
            case "geyser":
            case "plague":
            case "purge":
            case "sinkhole":
            case "supernova":
            case "tsunami":
            case "tornado":
                return 6;
            default:
                return 5;
        }
    }

    private void startSpecificDisaster(String disaster, int disasterLevel, Player player, World overworld, World nether, World end) {
        switch (disaster) {
            case "acidstorm":
                new AcidStorm(disasterLevel, overworld).start(overworld, null, true);
                break;
            case "blizzard":
                new Blizzard(disasterLevel, overworld).start(overworld, null, true);
                break;
            case "cavein":
                new CaveIn(disasterLevel, player.getWorld()).start(player.getLocation(), player);
                break;
            case "earthquake":
                new Earthquake(disasterLevel, player.getWorld()).start(player.getLocation(), player);
                break;
            case "endstorm":
                new EndStorm(disasterLevel, end).start(end, null, true);
                break;
            case "extremewinds":
                new ExtremeWinds(disasterLevel, overworld).start(overworld, null, true);
                break;
            case "hurricane":
                new Hurricane(disasterLevel, player.getWorld()).start(player.getLocation(), player);
                break;
            case "geyser":
                new Geyser(disasterLevel, player.getWorld()).start(player.getLocation(), player);
                break;
            case "meteorshowers":
                new MeteorShower(disasterLevel, overworld).start(overworld, null, true);
                break;
            case "plague":
                List<World> worlds = Arrays.asList(overworld, nether, end);
                World world = worlds.get(new Random().nextInt(worlds.size()));
                new BlackPlague(disasterLevel, world).start(world, player, enabled);
                break;
            case "purge":
                new Purge(disasterLevel, player.getWorld()).start(player.getLocation(), player);
                break;
            case "sandstorm":
                new SandStorm(disasterLevel, overworld).start(overworld, null, true);
                break;
            case "sinkhole":
                new Sinkhole(disasterLevel, player.getWorld()).start(player.getLocation(), player);
                break;
            case "soulstorm":
                new SoulStorm(disasterLevel, nether).start(nether, null, true);
                break;
            case "supernova":
                new Supernova(disasterLevel, player.getWorld()).start(player.getLocation(), player);
                break;
            case "tsunami":
                Tsunami tsunami = new Tsunami(disasterLevel, player.getWorld());
                Location pool = tsunami.findAvailabePool(player.getLocation());
                if (pool != null) {
                    tsunami.start(pool, player);
                }
                break;
            case "tornado":
                new Tornado(disasterLevel, player.getWorld()).start(player.getLocation(), player);
                break;
            case "solarstorm":
                new SolarStorm(disasterLevel, overworld).start(overworld, null, true);
                break;
            default:
                plugin.sendDebugMessage("Disaster not found: " + disaster);
                break;
        }
    }

    /**
     * Retries the disaster start event if no players are found.
     * @param statusLevel The status level of the flux meter.
     * @param attempts The number of attempts to start the disaster.
     */
    private void retryDisasterStartEvent(int statusLevel, int attempts) {
        if (attempts < 3) {
            plugin.sendDebugMessage("No players were found, retrying disaster start event...");
            String disaster = getDisaster(statusLevel);
            startDisaster(disaster, statusLevel, attempts + 1);
        } else {
            plugin.sendDebugMessage("Too many attempts, skipping disaster start event.");
        }
    }

    /**
     * Converts a configuration section to a map of disasters.
     * @param config The configuration section to convert.
     * @return A map of disasters.
     */
    private Map<String, Double> configToList(ConfigurationSection config) {
        Map<String, Double> map = new HashMap<>();

        if (config == null) {
            return map;
        }

        for (String key : config.getKeys(false)) {
            double weight = config.getDouble(key + ".weight");
            map.put(key, weight);
        }

        return map;
    }

    /**
     * Returns a random disaster level between the min and max values.
     * @param statusLevel The status level of the flux meter.
     * @param minDisasterLevel The minimum disaster level.
     * @param maxDisasterLevel The maximum disaster level.
     * @return A random disaster level.
     */
    private int getDisasterLevel(int statusLevel, int minDisasterLevel, int maxDisasterLevel) {
        Map<String, Double> difficulties = this.difficulties.get(statusLevel);

        if (difficulties.isEmpty()) {
            return 1;
        }

        RandomCollectionUtil<Integer> randomCollection = new RandomCollectionUtil<>();
        for (Map.Entry<String, Double> entry : difficulties.entrySet()) {
            int difficulty = Integer.parseInt(entry.getKey().split("_")[1]);
            double weight = entry.getValue();
            randomCollection.add(weight, difficulty);
        }

        int selectedDifficulty = randomCollection.next();
        return Math.min(Math.max(selectedDifficulty, minDisasterLevel), maxDisasterLevel);
    }

    /**
     * Starts the a scheduled disaster check task with a random delay.
     * @return The scheduled disasters task.
     */
    private BukkitTask startScheduledDisasters(long minInterval, long maxInterval) {
        Random rand = new Random();
        long randomDelay = (long) (rand.nextDouble() * (maxInterval - minInterval + 1)) + minInterval;
        BukkitTask disastersTask = plugin.getServer().getScheduler().runTaskLater(plugin, () -> checkDisaster(), randomDelay);
        plugin.sendDebugMessage("Next disaster check in " + randomDelay + " ticks.");
        return disastersTask;
    }

    /**
     * Reloads the deadly disasters handler.
     */
    public void reload() {
        enabled = plugin.getConfig().getBoolean("deadly_disasters.enabled", false);

        if (!enabled) {
            if (disastersTask != null) plugin.getServer().getScheduler().cancelTask(disastersTask.getTaskId());
            return;
        } else {
            if (disastersTask != null) plugin.getServer().getScheduler().cancelTask(disastersTask.getTaskId());
        }
        
        minInterval = plugin.getConfig().getLong("deadly_disasters.min_interval", 54000);
        maxInterval = plugin.getConfig().getLong("deadly_disasters.max_interval", 216000);
        probabilities = new ArrayList<>();
        disasters = new ArrayList<>();
        difficulties = new ArrayList<>();
        
        for (int i = 0; i <= 3; i++) {
            probabilities.add(plugin.getConfig().getDouble("deadly_disasters.tier_" + i + ".frequency"));
            disasters.add(configToList(plugin.getConfig().getConfigurationSection("deadly_disasters.tier_" + i + ".disasters")));
            difficulties.add(configToList(plugin.getConfig().getConfigurationSection("deadly_disasters.tier_" + i + ".difficulties")));
        }

        preventDisastersWhenIdle = plugin.getConfig().getBoolean("deadly_disasters.prevent_disasters_when_idle", true);
        disastersTask = startScheduledDisasters(minInterval, maxInterval);
    }

    /**
     * Registers Deadly Disasters plugin.
     * <p>
     * Updates the configuration and starts the scheduled disasters task.
     */
    public void registerDeadlyDisasters() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("DeadlyDisasters");
        Double version = Double.valueOf(plugin.getDescription().getVersion());
        if (version >= 12.0) {
            this.plugin.getLogger().info("DeadlyDisasters has been found, enabling settings...");
            this.reload();
        } else {
            plugin.getLogger().warning("This version is not compatible with FluxPerMillion. Please update DeadlyDisasters to version 12.0 or higher.");
        }
	}
}
