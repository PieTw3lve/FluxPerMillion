package com.github.pietw3lve.fpm.handlers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Registry;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import com.github.pietw3lve.fpm.FluxPerMillion;

public class EffectsHandler {
    
    private final FluxPerMillion plugin;
    private Map<Integer, Map<Attribute, Double>> attributes;
    private Map<Integer, Map<PotionEffectType, PotionEffect>> potions;
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
        this.reload();
    }

    /**
     * Reload the effects.
     */
    public void reload() {
        this.interval = plugin.getConfig().getInt("effects.refresh_interval");
        this.enabled = plugin.getConfig().getBoolean("effects.enabled");
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
            List<String> worlds = plugin.getConfig().getStringList("worlds.whitelist");
            int statusLevel = plugin.getFluxMeter().getStatusLevel();
            Map<Attribute, Double> configAttributes = this.attributes.get(statusLevel);
            Map<PotionEffectType, PotionEffect> potions = this.potions.get(statusLevel);

            if (worlds.contains(player.getWorld().getName())) {
                applyAttributes(player, configAttributes);
                applyPotions(player, potions);
            } else {
                applyAttributes(player, new HashMap<>());
            }
        }
    }

    /**
     * Apply the attributes to the player.
     * @param player The player
     * @param attributes The attributes
     */
    private void applyAttributes(Player player, Map<Attribute, Double> attributes) {
        if (attributes == null) return;

        Map<Attribute, Double> defaultAttributes = getDefaultPlayerAttributes();

        for (Map.Entry<Attribute, Double> entry : attributes.entrySet()) {
            defaultAttributes.put(entry.getKey(), entry.getValue());
        }

        for (Map.Entry<Attribute, Double> entry : defaultAttributes.entrySet()) {
            AttributeInstance instance = player.getAttribute(entry.getKey());
            if (instance != null) {
                instance.setBaseValue(entry.getValue());
            }
        }
    }

    /**
     * Apply the potions to the player.
     * @param player The player
     * @param potions The potions
     */
    private void applyPotions(Player player, Map<PotionEffectType, PotionEffect> potions) {
        if (potions == null) return;

        for (PotionEffect effect : potions.values()) {
            player.addPotionEffect(effect);
        }
    }

    /**
     * Convert the configuration section to a map of attributes.
     * @param statusLevel The status level of the effect
     * @param config The configuration section
     * @return A map of attributes
     */
    private Map<Integer, Map<Attribute, Double>> configToAttributes() {
        Map<Integer, Map<Attribute, Double>> attributes = new HashMap<>();

        for (int i = 0; i <= 3; i++) {
            String path = "effects.tier_" + i + ".attributes";
            ConfigurationSection config = plugin.getConfig().getConfigurationSection(path);
            Map<Attribute, Double> innerMap = new HashMap<>();

            if (config != null) {
                for (String key : config.getKeys(false)) {
                    String identifier = config.getString(key + ".identifier");
                    double value = config.getDouble(key + ".value");
                    Attribute attribute = Registry.ATTRIBUTE.match(identifier);
                    if (attribute != null) {
                        innerMap.put(attribute, value);
                    }
                }
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
    private Map<Integer, Map<PotionEffectType, PotionEffect>> configToPotions() {
        Map<Integer, Map<PotionEffectType, PotionEffect>> potions = new HashMap<>();

        for (int i = 0; i <= 3; i++) {
            String path = "effects.tier_" + i + ".potion_effects";
            ConfigurationSection config = plugin.getConfig().getConfigurationSection(path);
            Map<PotionEffectType, PotionEffect> innerMap = new HashMap<>();

            for (String key : config.getKeys(false)) {
                String identifier = config.getString(key + ".identifier");
                int amplifier = config.getInt(key + ".amplifier");
                PotionEffectType type = Registry.EFFECT.match(identifier);
                if (type != null) {
                    innerMap.put(type, new PotionEffect(type, interval + 100, amplifier));
                }
            }

            potions.put(i, innerMap);
        }

        return potions;
    }

    /**
     * Get the default player attributes.
     * <p>
     * PLEASE ADD A RESET TO DEFAULTS ATTRIBUTE METHOD
     * @return A map of default player attributes
     */
    private Map<Attribute, Double> getDefaultPlayerAttributes() {
        Map<Attribute, Double> attributes = new HashMap<>();
        attributes.put(Attribute.GENERIC_MAX_HEALTH, 20.0);
        attributes.put(Attribute.GENERIC_KNOCKBACK_RESISTANCE, 0.0);
        attributes.put(Attribute.GENERIC_MOVEMENT_SPEED, 0.1);
        attributes.put(Attribute.GENERIC_ATTACK_DAMAGE, 1.0);
        attributes.put(Attribute.GENERIC_ATTACK_KNOCKBACK, 0.0);
        attributes.put(Attribute.GENERIC_ATTACK_SPEED, 4.0);
        attributes.put(Attribute.GENERIC_ARMOR, 0.0);
        attributes.put(Attribute.GENERIC_ARMOR_TOUGHNESS, 0.0);
        attributes.put(Attribute.GENERIC_FALL_DAMAGE_MULTIPLIER, 1.0);
        attributes.put(Attribute.GENERIC_LUCK, 0.0);
        attributes.put(Attribute.GENERIC_MAX_ABSORPTION, 0.0);
        attributes.put(Attribute.GENERIC_SAFE_FALL_DISTANCE, 3.0);
        attributes.put(Attribute.GENERIC_SCALE, 1.0);
        attributes.put(Attribute.GENERIC_STEP_HEIGHT, 0.6);
        attributes.put(Attribute.GENERIC_GRAVITY, 0.08);
        attributes.put(Attribute.GENERIC_JUMP_STRENGTH, 0.41999998688697815);
        attributes.put(Attribute.GENERIC_BURNING_TIME, 1.0);
        attributes.put(Attribute.GENERIC_EXPLOSION_KNOCKBACK_RESISTANCE, 0.0);
        attributes.put(Attribute.GENERIC_MOVEMENT_EFFICIENCY, 0.0);
        attributes.put(Attribute.GENERIC_OXYGEN_BONUS, 0.0);
        attributes.put(Attribute.GENERIC_WATER_MOVEMENT_EFFICIENCY, 0.0);
        attributes.put(Attribute.PLAYER_BLOCK_INTERACTION_RANGE, 4.5);
        attributes.put(Attribute.PLAYER_ENTITY_INTERACTION_RANGE, 3.0);
        attributes.put(Attribute.PLAYER_BLOCK_BREAK_SPEED, 1.0);
        attributes.put(Attribute.PLAYER_MINING_EFFICIENCY, 0.0);
        attributes.put(Attribute.PLAYER_SNEAKING_SPEED, 0.3);
        attributes.put(Attribute.PLAYER_SUBMERGED_MINING_SPEED, 0.2);
        attributes.put(Attribute.PLAYER_SWEEPING_DAMAGE_RATIO, 0.0);
        return attributes;
    }
}
