package com.github.pietw3lve.fpm.handlers;

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
    private BukkitTask disastersTask;
    private long minInterval;
    private long maxInterval;
    private double tier0Chance;
    private Map<String, Double> tier0Disaster;
    private double tier1Chance;
    private Map<String, Double> tier1Disaster;
    private double tier2Chance;
    private Map<String, Double> tier2Disaster;
    private double tier3Chance;
    private Map<String, Double> tier3Disaster;
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
        if (value <= tier0Chance && statusLevel == 0) {
            String disaster = getDisaster(statusLevel);
            startDisaster(disaster, statusLevel, 0);
        } else if (value <= tier1Chance && statusLevel == 1) {
            String disaster = getDisaster(statusLevel);
            startDisaster(disaster, statusLevel, 0);
        } else if (value <= tier2Chance && statusLevel == 2) {
            String disaster = getDisaster(statusLevel);
            startDisaster(disaster, statusLevel, 0);
        } else if (value <= tier3Chance && statusLevel == 3) {
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
        List<Map<String, Double>> disasterList = Arrays.asList(tier0Disaster, tier1Disaster, tier2Disaster, tier3Disaster);
        Map<String, Double> disasters = disasterList.get(statusLevel);

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
     * @param disasterList The configuration section to convert.
     * @return A map of disasters.
     */
    private Map<String, Double> configToDisasterList(ConfigurationSection disasterList) {
        Map<String, Double> disasters = new HashMap<>();

        if (disasterList == null) {
            return disasters;
        }

        for (String disaster : disasterList.getKeys(false)) {
            double weight = disasterList.getDouble(disaster + ".weight");
            disasters.put(disaster, weight);
        }

        return disasters;
    }

    /**
     * Returns a random disaster level between the min and max values.
     * @param statusLevel The status level of the flux meter.
     * @param minDisasterLevel The minimum disaster level.
     * @param maxDisasterLevel The maximum disaster level.
     * @return A random disaster level.
     */
    private int getDisasterLevel(int statusLevel, int minDisasterLevel, int maxDisasterLevel) {
        if (statusLevel == 3) {
            return (int) (Math.random() * (maxDisasterLevel - minDisasterLevel + 1)) + minDisasterLevel;
        } else {
            int adjustedMax = minDisasterLevel + (statusLevel * (maxDisasterLevel - minDisasterLevel)) / 3;
            return (int) (Math.random() * (adjustedMax - minDisasterLevel + 1)) + minDisasterLevel;
        }
    }

    /**
     * Starts the a scheduled disaster check task with a random delay.
     * @return The scheduled disasters task.
     */
    private BukkitTask startScheduledDisasters(long minInterval, long maxInterval) {
        long randomDelay = minInterval + (long) (Math.random() * (maxInterval - minInterval));
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
        tier0Chance = plugin.getConfig().getDouble("deadly_disasters.tier_0.chance", 0.1);
        tier0Disaster = configToDisasterList(plugin.getConfig().getConfigurationSection("deadly_disasters.tier_0.disasters"));
        tier1Chance = plugin.getConfig().getDouble("deadly_disasters.tier_1.chance", 0.2);
        tier1Disaster = configToDisasterList(plugin.getConfig().getConfigurationSection("deadly_disasters.tier_1.disasters"));
        tier2Chance = plugin.getConfig().getDouble("deadly_disasters.tier_2.chance", 0.3);
        tier2Disaster = configToDisasterList(plugin.getConfig().getConfigurationSection("deadly_disasters.tier_2.disasters"));
        tier3Chance = plugin.getConfig().getDouble("deadly_disasters.tier_3.chance", 0.3);
        tier3Disaster = configToDisasterList(plugin.getConfig().getConfigurationSection("deadly_disasters.tier_3.disasters"));
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

    @Override
    public String toString() {
        String tier0String = "\n" + "Chance: " + tier0Chance + "\n" + "Disasters: " + tier0Disaster.toString();
        String tier1String = "\n" + "Chance: " + tier1Chance + "\n" + "Disasters: " + tier1Disaster.toString();
        String tier2String = "\n" + "Chance: " + tier2Chance + "\n" + "Disasters: " + tier2Disaster.toString();
        String tier3String = "\n" + "Chance: " + tier3Chance + "\n" + "Disasters: " + tier3Disaster.toString();
        return "min: " + minInterval + "\n" +
               "max: " + maxInterval + "\n" +
               "-----tier 0-----" + tier0String + "\n" +
               "-----tier 1-----" + tier1String + "\n" +
               "-----tier 2-----" + tier2String + "\n" +
               "-----tier 3-----" + tier3String;
    }
}
