package com.github.pietw3lve.fpm.handlers;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.configuration.ConfigurationSection;

import com.github.pietw3lve.fpm.FluxPerMillion;
import com.github.pietw3lve.fpm.expansions.FPMExpansion;

public class PlaceholderHandler {
    
    private final FluxPerMillion plugin;
    private FPMExpansion expansion;
    private List<String> percentColors;
    private boolean enabled;

    /**
     * PlaceholderHandler constructor.
     * @param plugin FluxPerMillion instance.
     */
    public PlaceholderHandler(FluxPerMillion plugin) {
        this.plugin = plugin;
        this.expansion = new FPMExpansion(plugin);
        this.percentColors = new ArrayList<>();
        this.enabled = false;
    }

    /**
     * Register placeholders.
     */
    public void registerPlaceholders() {
        this.reload();
        plugin.getLogger().info("PlaceholderAPI has been found, enabling placeholders...");
    }

    /**
     * Reload placeholders.
     */
    public void reload() {
        expansion = new FPMExpansion(plugin);
        enabled = plugin.getConfig().getBoolean("placeholderapi.enabled", false);
        
        if (!enabled) {
            expansion.unregister();
            return;
        }
        
        percentColors = configToList(plugin.getConfig().getConfigurationSection("placeholderapi.percentage_colors"));
        expansion.register();
    }

    /**
     * Get percent colors.
     * @return List of percent colors.
     */
    private List<String> configToList(ConfigurationSection configurationSection) {
        List<String> list = new ArrayList<>();
        for (String key : configurationSection.getKeys(false)) {
            list.add(configurationSection.getString(key));
        }
        return list;
    }

    /**
     * Get percent colors.
     * @return List of percent colors.
     */
    public List<String> getPercentColors() {
        return percentColors;
    }
}
