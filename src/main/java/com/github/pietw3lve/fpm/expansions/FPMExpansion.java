package com.github.pietw3lve.fpm.expansions;

import java.util.List;

import org.bukkit.OfflinePlayer;

import com.github.pietw3lve.fpm.FluxPerMillion;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;

public class FPMExpansion extends PlaceholderExpansion {
    
    private final FluxPerMillion plugin;
    
    /**
     * FPMExpansion Constructor.
     * @param plugin The instance of the FluxPerMillion plugin.
     */
    public FPMExpansion(FluxPerMillion plugin) {
        this.plugin = plugin;
    }

    /**
     * Returns the author of the expansion.
     * @return The author of the expansion.
     */
    @Override
    public String getAuthor() {
        return "PieTw3lve";
    }
    
    /**
     * Returns the identifier of the expansion.
     * @return The identifier of the expansion.
     */
    @Override
    public String getIdentifier() {
        return "fluxpermillion";
    }

    /**
     * Returns the version of the expansion.
     * @return The version of the expansion.
     */
    @Override
    public String getVersion() {
        return "1.0.0";
    }
    /**
     * Indicates whether the expansion should persist or not.
     * @return {@code true} if the expansion should persist, {@code false} otherwise.
     */
    @Override
    public boolean persist() {
        return true;
    }

    /**
     * This method is called when a placeholder with the prefix "fluxpermillion_" is used in a plugin.
     * It returns the value corresponding to the given identifier.
     * @param player     The player for whom the placeholder is being requested.
     * @param identifier The identifier of the placeholder.
     * @return The value corresponding to the given identifier.
     */
    @Override
    public String onRequest(OfflinePlayer player, String identifier) {
        // %fluxpermillion_<identifier>%
        if (identifier.equals("points")) {
            return String.format("%.0f", plugin.getFluxMeter().getTotalPoints());
        }
        else if (identifier.equals("max_points")) {
            return String.format("%.0f", plugin.getFluxMeter().getMax());
        }
        else if (identifier.equals("min_points")) {
            return String.format("%.0f", plugin.getFluxMeter().getMin());
        }
        else if (identifier.equals("status_level")) {
            return String.format("%s", plugin.getFluxMeter().getStatusLevel());
        }
        else if (identifier.equals("status_color")) {
            List<String> statusColors = plugin.getPlaceholderHandler().getStatusColors();
            int statusLevel = plugin.getFluxMeter().getStatusLevel();
            if (statusLevel == 0) {
                return statusColors.get(0);
            } else if (statusLevel == 1) {
                return statusColors.get(1);
            } else if (statusLevel == 2) {
                return statusColors.get(2);
            } else if (statusLevel == 3) {
                return statusColors.get(3);
            }
        }
        else if (identifier.equals("percentage")) {
            return String.format("%.0f", plugin.getFluxMeter().getProgress() * 100);
        } 
        else if (identifier.equals("percentage_color")) {
            List<String> percentColors = plugin.getPlaceholderHandler().getPercentColors();
            int statusLevel = plugin.getFluxMeter().getStatusLevel();
            if (statusLevel == 0) {
                return percentColors.get(0);
            } else if (statusLevel == 1) {
                return percentColors.get(1);
            } else if (statusLevel == 2) {
                return percentColors.get(2);
            } else if (statusLevel == 3) {
                return percentColors.get(3);
            }
        }
        return null;
    }
}
