package com.github.pietw3lve.fpm.handlers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import com.github.pietw3lve.fpm.FluxPerMillion;
import com.github.pietw3lve.fpm.utils.RandomCollectionUtil;

import deadlydisasters.events.disasters.AcidStorm;
import deadlydisasters.events.disasters.BlackPlague;
import deadlydisasters.events.disasters.Blizzard;
import deadlydisasters.events.disasters.CaveIn;
import deadlydisasters.events.disasters.Earthquake;
import deadlydisasters.events.disasters.EndStorm;
import deadlydisasters.events.disasters.ExtremeWinds;
import deadlydisasters.events.disasters.Geyser;
import deadlydisasters.events.disasters.Hurricane;
import deadlydisasters.events.disasters.MeteorShower;
import deadlydisasters.events.disasters.Purge;
import deadlydisasters.events.disasters.SandStorm;
import deadlydisasters.events.disasters.Sinkhole;
import deadlydisasters.events.disasters.SoulStorm;
import deadlydisasters.events.disasters.Supernova;
import deadlydisasters.events.disasters.Tornado;
import deadlydisasters.events.disasters.Tsunami;

public class DeadlyDisastersHandler {
    
    private final FluxPerMillion plugin;
    private final FluxMeterHandler fluxMeter;
    private final List<Double> DEFAULT_PROBABILITIES;
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
        this.DEFAULT_PROBABILITIES = Arrays.asList(0.1, 0.2, 0.3, 0.3);
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
        if (value <= probabilities.get(0) && statusLevel == 0) {
            String disaster = getDisaster(statusLevel);
            startDisaster(disaster, statusLevel, 0);
        } else if (value <= probabilities.get(1) && statusLevel == 1) {
            String disaster = getDisaster(statusLevel);
            startDisaster(disaster, statusLevel, 0);
        } else if (value <= probabilities.get(2) && statusLevel == 2) {
            String disaster = getDisaster(statusLevel);
            startDisaster(disaster, statusLevel, 0);
        } else if (value <= probabilities.get(3) && statusLevel == 3) {
            String disaster = getDisaster(statusLevel);
            startDisaster(disaster, statusLevel, 0);
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
        World overworld = plugin.getServer().getWorlds().stream().filter(w -> w.getEnvironment() == World.Environment.NORMAL).findAny().get();
        World nether = plugin.getServer().getWorlds().stream().filter(w -> w.getEnvironment() == World.Environment.NETHER).findAny().get();
        World end = plugin.getServer().getWorlds().stream().filter(w -> w.getEnvironment() == World.Environment.THE_END).findAny().get();
        Collection<? extends Player> players = plugin.getServer().getOnlinePlayers();
        List<Player> selectedPlayers = null;
        Player player = null;
        int disasterLevel = 1;
        Random rand = new Random();

        if (players.isEmpty() && preventDisastersWhenIdle) {
            plugin.sendDebugMessage("No players found, skipping disaster start event.");
            return;
        } else if (disaster == null) {
            plugin.sendDebugMessage("No disasters found for status level: " + statusLevel);
            retryDisasterStartEvent(statusLevel, attempts);
            return;
        }

        switch (disaster) {
            case "acidstorm":
                disasterLevel = getDisasterLevel(statusLevel, 1, 5);
                AcidStorm acidStorm = new AcidStorm(disasterLevel);
                acidStorm.start(overworld, null, true);
                break;
            case "blizzard":
                disasterLevel = getDisasterLevel(statusLevel, 1, 5);
                Blizzard blizzard = new Blizzard(disasterLevel);
                blizzard.start(overworld, null, true);
                break;
            case "cavein":
                selectedPlayers = players.stream().filter(p -> p.getWorld() == overworld || p.getWorld() == nether).collect(Collectors.toList());
                if (selectedPlayers.isEmpty()) {
                    retryDisasterStartEvent(statusLevel, attempts); 
                    return;
                }
                player = selectedPlayers.get(rand.nextInt(selectedPlayers.size()));
                disasterLevel = getDisasterLevel(statusLevel, 1, 6);
                CaveIn cavein = new CaveIn(disasterLevel);
                cavein.start(player.getLocation(), player);
                break;
            case "earthquake":
                selectedPlayers = players.stream().collect(Collectors.toList());
                if (selectedPlayers.isEmpty()) {
                    retryDisasterStartEvent(statusLevel, attempts); 
                    return;
                }
                player = selectedPlayers.get(rand.nextInt(selectedPlayers.size()));
                disasterLevel = getDisasterLevel(statusLevel, 1, 6);
                Earthquake earthquake = new Earthquake(disasterLevel);
                earthquake.start(player.getLocation(), player);
                break;
            case "endstorm":
                disasterLevel = getDisasterLevel(statusLevel, 1, 5);
                EndStorm endStorm = new EndStorm(disasterLevel);
                endStorm.start(end, null, true);
                break;
            case "extremewinds":
                disasterLevel = getDisasterLevel(statusLevel, 1, 5);
                ExtremeWinds extremeWinds = new ExtremeWinds(disasterLevel);
                extremeWinds.start(overworld, null, true);
                break;
            case "hurricane":
                selectedPlayers = players.stream().filter(p -> p.getWorld() == overworld).collect(Collectors.toList());
                if (selectedPlayers.isEmpty()) {
                    retryDisasterStartEvent(statusLevel, attempts); 
                    return;
                }
                player = selectedPlayers.get(rand.nextInt(selectedPlayers.size()));
                disasterLevel = getDisasterLevel(statusLevel, 1, 6);
                Hurricane hurricane = new Hurricane(disasterLevel);
                hurricane.start(player.getLocation(), player);
                break;
            case "geyser":
                selectedPlayers = players.stream().filter(p -> p.getWorld() == overworld || p.getWorld() == nether).collect(Collectors.toList());
                if (selectedPlayers.isEmpty()) {
                    retryDisasterStartEvent(statusLevel, attempts); 
                    return;
                }
                player = selectedPlayers.get(rand.nextInt(selectedPlayers.size()));
                disasterLevel = getDisasterLevel(statusLevel, 1, 6);
                Geyser geyser = new Geyser(disasterLevel);
                geyser.start(player.getLocation(), player);
                break;
            case "meteorshowers":
                disasterLevel = getDisasterLevel(statusLevel, 1, 5);
                MeteorShower meteorShowers = new MeteorShower(disasterLevel);
                meteorShowers.start(overworld, null, true);
                break;
            case "plague":
                List<World> worlds = Arrays.asList(overworld, nether, end);
                disasterLevel = getDisasterLevel(statusLevel, 1, 5);
                BlackPlague plague = new BlackPlague(disasterLevel);
                plague.start(worlds.get(rand.nextInt(worlds.size())), null, true);
                break;
            case "purge":
                selectedPlayers = players.stream().collect(Collectors.toList());
                if (selectedPlayers.isEmpty()) {
                    retryDisasterStartEvent(statusLevel, attempts); 
                    return;
                }
                player = selectedPlayers.get(rand.nextInt(selectedPlayers.size()));
                disasterLevel = getDisasterLevel(statusLevel, 1, 6);
                Purge purge = new Purge(disasterLevel);
                purge.start(player.getLocation(), player);
            case "sandstorm":
                disasterLevel = getDisasterLevel(statusLevel, 1, 5);
                SandStorm sandstorm = new SandStorm(disasterLevel);
                sandstorm.start(overworld, null, true);
                break;
            case "sinkhole":
                selectedPlayers = players.stream().collect(Collectors.toList());
                if (selectedPlayers.isEmpty()) {
                    retryDisasterStartEvent(statusLevel, attempts); 
                    return;
                }
                player = selectedPlayers.get(rand.nextInt(selectedPlayers.size()));
                disasterLevel = getDisasterLevel(statusLevel, 1, 6);
                Sinkhole sinkhole = new Sinkhole(disasterLevel);
                sinkhole.start(player.getLocation(), player);
                break;
            case "soulstorm":
                disasterLevel = getDisasterLevel(statusLevel, 1, 5);
                SoulStorm soulstorm = new SoulStorm(disasterLevel);
                soulstorm.start(nether, null, true);
                break;
            case "supernova":
                selectedPlayers = players.stream().filter(p -> p.getWorld() == overworld || p.getWorld() == end).collect(Collectors.toList());
                if (selectedPlayers.isEmpty()) {
                    retryDisasterStartEvent(statusLevel, attempts); 
                    return;
                }
                player = selectedPlayers.get(rand.nextInt(selectedPlayers.size()));
                disasterLevel = getDisasterLevel(statusLevel, 1, 6);
                Supernova supernova = new Supernova(disasterLevel);
                supernova.start(player.getLocation(), player);
                break;
            case "tsunami":
                selectedPlayers = players.stream().filter(p -> p.getWorld() == overworld).collect(Collectors.toList());
                if (selectedPlayers.isEmpty()) {
                    retryDisasterStartEvent(statusLevel, attempts); 
                    return;
                }
                player = selectedPlayers.get(rand.nextInt(selectedPlayers.size()));
                disasterLevel = getDisasterLevel(statusLevel, 1, 6);
                Tsunami tsunami = new Tsunami(disasterLevel);
                Location pool = tsunami.findAvailabePool(player.getLocation());
                tsunami.start(pool, player);
                break;
            case "tornado":
                selectedPlayers = players.stream().collect(Collectors.toList());
                if (selectedPlayers.isEmpty()) {
                    retryDisasterStartEvent(statusLevel, attempts); 
                    return;
                }
                player = selectedPlayers.get(rand.nextInt(selectedPlayers.size()));
                disasterLevel = getDisasterLevel(statusLevel, 1, 6);
                Tornado tornado = new Tornado(disasterLevel);
                tornado.start(player.getLocation(), player);
                break;
            default:
                plugin.sendDebugMessage("Disaster not found: " + disaster);
                return;
        }

        plugin.sendDebugMessage("Starting " + disaster + " with danger level: " + disasterLevel);
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
            probabilities.add(plugin.getConfig().getDouble("deadly_disasters.tier_" + i + ".chance", DEFAULT_PROBABILITIES.get(i)));
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
        this.reload();
		plugin.getLogger().info("DeadlyDisasters has been found, enabling features...");
	}
}
