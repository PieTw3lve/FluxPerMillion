package com.github.pietw3lve.fpm.handlers;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import com.github.pietw3lve.fpm.FluxPerMillion;

public class EffectsHandler {
    
    private final FluxPerMillion plugin;
    private Map<Integer, Map<String, Double>> attributes;
    private Map<Integer, Map<String, Integer>> potions;
    private int interval;
    private BukkitTask effectTask;
    private boolean enabled;

    /**
     * EffectsHandler constructor.
     * @param plugin The plugin
     */
    public EffectsHandler(FluxPerMillion plugin) {
        this.plugin = plugin;
        this.effectTask = null;
        reload();
    }

    /**
     * Reload the effects.
     */
    public void reload() {
        this.interval = plugin.getConfig().getInt("effects.interval", 1200);
        this.enabled = plugin.getConfig().getBoolean("effects.enabled", true);
        this.attributes = configToAttributes();
        this.potions = configToPotions();

        if (!enabled) {
            if (effectTask != null) plugin.getServer().getScheduler().cancelTask(effectTask.getTaskId());
            return;
        } else {
            if (effectTask != null) plugin.getServer().getScheduler().cancelTask(effectTask.getTaskId());
        }

        effectTask = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> applyEffects(), 0, interval);
    }

    /**
     * Apply the effects to the players.
     */
    private void applyEffects() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            int statusLevel = plugin.getFluxMeter().getStatusLevel();
            Map<String, Double> attributes = this.attributes.get(statusLevel);
            Map<String, Integer> potions = this.potions.get(statusLevel);

            if (attributes != null) {
                for (Map.Entry<String, Double> entry : attributes.entrySet()) {
                    String attribute = entry.getKey();
                    double value = entry.getValue();
                    player.getAttribute(convertToAttribute(attribute)).setBaseValue(value);
                }
            }

            if (potions != null) {
                for (Map.Entry<String, Integer> entry : potions.entrySet()) {
                    String potion = entry.getKey();
                    int amplifier = entry.getValue();
                    player.addPotionEffect(new PotionEffect(PotionEffectType.getByName(potion), interval, amplifier));
                }
            }
        }
    }

    /**
     * Convert the string to an attribute.
     * @param attribute The attribute to convert
     * @return The attribute
     */
    private Attribute convertToAttribute(String attribute) {
        switch (attribute) {
            case "max_health":
                return Attribute.GENERIC_MAX_HEALTH;
            case "follow_range":
                return Attribute.GENERIC_FOLLOW_RANGE;
            case "knockback_resistance":
                return Attribute.GENERIC_KNOCKBACK_RESISTANCE;
            case "movement_speed":
                return Attribute.GENERIC_MOVEMENT_SPEED;
            case "attack_damage":
                return Attribute.GENERIC_ATTACK_DAMAGE;
            case "attack_speed":
                return Attribute.GENERIC_ATTACK_SPEED;
            case "armor":
                return Attribute.GENERIC_ARMOR;
            case "armor_toughness":
                return Attribute.GENERIC_ARMOR_TOUGHNESS;
            case "luck":
                return Attribute.GENERIC_LUCK;
            default:
                return null;
        }
    }

    /**
     * Convert the configuration section to a map of attributes.
     * @param statusLevel The status level of the effect
     * @param config The configuration section
     * @return A map of attributes
     */
    private Map<Integer, Map<String, Double>> configToAttributes() {
        Map<Integer, Map<String, Double>> attributes = new HashMap<>();

        for (int i = 0; i <= 3; i++) {
            String path = "effects."+ "tier_" + i +".player.attributes";
            ConfigurationSection config = plugin.getConfig().getConfigurationSection(path);
            Map<String, Double> innerMap = new HashMap<>();

            for (String key : config.getKeys(false)) {
                double value = config.getDouble(key);
                innerMap.put(key, value);
            }

            attributes.put(i, innerMap);
        }

        return attributes;
    }

    /**
     * Convert the configuration section to a map of potions.
     * @param statusLevel The status level of the effect
     * @param config The configuration section
     * @return A map of potions
     */
    private Map<Integer, Map<String, Integer>> configToPotions() {
        Map<Integer, Map<String, Integer>> potions = new HashMap<>();

        for (int i = 0; i <= 3; i++) {
            String path = "effects."+ "tier_" + i +".player.potion_effects";
            ConfigurationSection config = plugin.getConfig().getConfigurationSection(path);
            Map<String, Integer> innerMap = new HashMap<>();

            for (String key : config.getKeys(false)) {
                int value = config.getInt(key + ".amplifier");
                innerMap.put(key, value);
            }

            potions.put(i, innerMap);
        }

        return potions;
    }
}
