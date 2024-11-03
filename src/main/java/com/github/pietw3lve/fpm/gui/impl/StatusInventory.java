package com.github.pietw3lve.fpm.gui.impl;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import com.github.pietw3lve.fpm.FluxPerMillion;
import com.github.pietw3lve.fpm.gui.InventoryButton;
import com.github.pietw3lve.fpm.gui.InventoryGUI;
import com.github.pietw3lve.fpm.handlers.FluxMeterHandler;
import com.github.pietw3lve.fpm.handlers.MessageHandler;
import com.github.pietw3lve.fpm.utils.PlayerSkullUtil;
import com.github.pietw3lve.fpm.utils.SQLiteUtil;

/**
 * Represents the status inventory GUI for displaying world and player status.
 */
public class StatusInventory extends InventoryGUI {

    private final SQLiteUtil dbUtil;
    private final FluxMeterHandler fluxMeter;
    private final MessageHandler messageHandler;

    /**
     * Constructs a new StatusInventory.
     *
     * @param plugin the FluxPerMillion plugin instance
     */
    public StatusInventory(FluxPerMillion plugin) {
        this.dbUtil = plugin.getDbUtil();
        this.fluxMeter = plugin.getFluxMeter();
        this.messageHandler = plugin.getMessageHandler();
    }

    /**
     * Creates the inventory for the status GUI.
     *
     * @return the created inventory
     */
    @Override
    protected Inventory createInventory() {
        return Bukkit.createInventory(null, 45, ChatColor.DARK_GREEN + "World Status");
    }
    
    /**
     * Decorates the inventory with status information.
     *
     * @param player the player for whom the inventory is being decorated
     */
    @Override
    public void decorate(Player player) {
        int inventorySize = this.getInventory().getSize();
        double playerFlux = dbUtil.getPlayerFlux(player);
        double playerPercent = Math.max(0, playerFlux / dbUtil.calculateTotalPoints()) * 100;

        double[] newFlux = {
            fluxMeter.getNewEnergyPoints(),
            fluxMeter.getNewAgriculturePoints(),
            fluxMeter.getNewPollutionPoints(),
            fluxMeter.getNewWildlifePoints()
        };

        double[] oldFlux = {
            fluxMeter.getOldEnergyPoints(),
            fluxMeter.getOldAgriculturePoints(),
            fluxMeter.getOldPollutionPoints(),
            fluxMeter.getOldWildlifePoints()
        };

        double newTotalFlux = Arrays.stream(newFlux).sum();
        double oldTotalFlux = Arrays.stream(oldFlux).sum();

        double[] newPercents = Arrays.stream(newFlux).map(p -> p / newTotalFlux * 100).toArray();
        double[] oldPercents = Arrays.stream(oldFlux).map(p -> p / oldTotalFlux * 100).toArray();

        for (int i = 0; i < inventorySize; i++) {
            this.addButton(i, createFillerIcon(Material.GRAY_STAINED_GLASS_PANE));
        }

        this.addButton(12, createIcon(
            PlayerSkullUtil.getPlayerSkull("http://textures.minecraft.net/texture/2e2cc42015e6678f8fd49ccc01fbf787f1ba2c32bcf559a015332fc5db50"),
            ChatColor.YELLOW + "World",
            Arrays.asList(
                ChatColor.GRAY + "Overall Condition: " + messageHandler.getStatusMessages().get(fluxMeter.getStatusLevel()),
                ChatColor.GRAY + "Next Check: " + ChatColor.YELLOW + String.format("%dh %dm %ds", 
                    TimeUnit.MILLISECONDS.toHours(fluxMeter.getTimeUntilNextRun()), 
                    TimeUnit.MILLISECONDS.toMinutes(fluxMeter.getTimeUntilNextRun()) % 60, 
                    TimeUnit.MILLISECONDS.toSeconds(fluxMeter.getTimeUntilNextRun()) % 60),
                "",
                ChatColor.DARK_GRAY + "This world changes based on its inhabitants and thrives or suffers from their choices."
            )
        ));
        this.addButton(14, createIcon(
            PlayerSkullUtil.getPlayerSkull(player),
            ChatColor.YELLOW + "You",
            Arrays.asList(
                ChatColor.GRAY + "Flux Contribution: " + getColorForPlayerContribution(playerPercent) + String.format("%.1f%%", playerPercent),
                "",
                ChatColor.DARK_GRAY + "Your actions affect the world around you and determine its future for better or worse."
            )
        ));
        this.addButton(28, createIcon(
            new ItemStack(Material.FIRE_CHARGE),
            ChatColor.GOLD + "Energy and Combustion " + formatChange(newPercents[0] - oldPercents[0]),
            Arrays.asList(
                ChatColor.GRAY + "Flux Contribution: " + ChatColor.YELLOW + String.format("%.1f%%", newPercents[0]),
                "",
                ChatColor.DARK_GRAY + "The use of energy sources, including fossil fuels and biomass, contribute significantly to greenhouse gas emissions."
            )
        ));
        this.addButton(30, createIcon(
            new ItemStack(Material.SPRUCE_SAPLING),
            ChatColor.DARK_GREEN + "Agriculture " + formatChange(newPercents[1] - oldPercents[1]),
            Arrays.asList(
                ChatColor.GRAY + "Flux Contribution: " + ChatColor.YELLOW + String.format("%.1f%%", newPercents[1]), 
                "",
                ChatColor.DARK_GRAY + "Agricultural practices, including crop production and tree management, exert a critical role in carbon storage and ecosystem health."
            )
        ));
        this.addButton(32, createIcon(
            new ItemStack(Material.MINECART),
            ChatColor.DARK_GRAY + "Pollution " + formatChange(newPercents[2] - oldPercents[2]),
            Arrays.asList(
                ChatColor.GRAY + "Flux Contribution: " + ChatColor.YELLOW + String.format("%.1f%%", newPercents[2]), 
                "",
                ChatColor.DARK_GRAY + "The generation and mismanagement of waste lead to environmental degradation, releasing pollutants that threaten air and water quality."
            )
        ));
        this.addButton(34, createIcon(
            new ItemStack(Material.TURTLE_EGG),
            ChatColor.DARK_AQUA + "Wildlife " + formatChange(newPercents[3] - oldPercents[3]),
            Arrays.asList(
                ChatColor.GRAY + "Flux Contribution: " + ChatColor.YELLOW + String.format("%.1f%%", newPercents[3]), 
                "",
                ChatColor.DARK_GRAY + "Human activities like fishing and other practices that disrupt ecosystems, resulting in biodiversity loss and altering the delicate balance of the environment."
            )
        ));

        super.decorate(player);
    }

    /**
     * Gets the color code for the player's contribution percentage.
     *
     * @param contribution the player's contribution percentage
     * @return the color code as a string
     */
    private String getColorForPlayerContribution(double contribution) {
        if (contribution < 25) {
            return ChatColor.GREEN.toString();
        } else if (contribution < 50) {
            return ChatColor.YELLOW.toString();
        } else if (contribution < 75) {
            return ChatColor.GOLD.toString(); // Orange color
        } else {
            return ChatColor.RED.toString();
        }
    }

    /**
     * Creates a filler icon for the inventory.
     *
     * @param material the material of the filler icon
     * @return the created InventoryButton
     */
    private InventoryButton createFillerIcon(Material material) {
        return new InventoryButton()
            .creator(player -> {
                ItemStack itemStack = new ItemStack(material);
                ItemMeta meta = itemStack.getItemMeta();
                meta.setDisplayName(" ");
                itemStack.setItemMeta(meta);
                return itemStack;
            })
            .consumer(event -> {});
    }

    /**
     * Creates an icon for the inventory.
     *
     * @param item the item stack for the icon
     * @param name the display name of the icon
     * @param lore the lore of the icon
     * @return the created InventoryButton
     */
    private InventoryButton createIcon(ItemStack item, String name, List<String> lore) {
        return new InventoryButton()
            .creator(player -> {
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(name);
                    meta.setLore(wrapLore(lore, 45));
                    item.setItemMeta(meta);
                }
                return item;
            })
            .consumer(event -> {});
    }

    /**
     * Wraps the lore text to fit within a specified line length.
     *
     * @param lore the list of lore strings
     * @param lineLength the maximum line length
     * @return the wrapped lore as a list of strings
     */
    private List<String> wrapLore(List<String> lore, int lineLength) {
        List<String> wrappedLore = new ArrayList<>();
        for (String line : lore) {
            wrappedLore.addAll(wrapLine(line, lineLength));
        }
        return wrappedLore;
    }

    /**
     * Wraps a single line of text to fit within a specified line length.
     *
     * @param line the line of text
     * @param lineLength the maximum line length
     * @return the wrapped line as a list of strings
     */
    private List<String> wrapLine(String line, int lineLength) {
        List<String> wrappedLines = new ArrayList<>();
        String colorCode = ChatColor.getLastColors(line);
        while (line.length() > lineLength) {
            int spaceIndex = line.lastIndexOf(' ', lineLength);
            if (spaceIndex == -1) {
                spaceIndex = lineLength;
            }
            wrappedLines.add(line.substring(0, spaceIndex));
            line = colorCode + line.substring(spaceIndex).trim();
        }
        wrappedLines.add(line);
        return wrappedLines;
    }

    /**
     * Formats the change in percentage with appropriate color coding.
     *
     * @param change the change in percentage
     * @return the formatted change string
     */
    private String formatChange(double change) {
        if (change == 0 || Double.isNaN(change)) return "";
        String sign = change > 0 ? ChatColor.RED + "+" : ChatColor.GREEN + "-";
        return String.format(ChatColor.GRAY + "(%s%.0f%%" + ChatColor.GRAY + ")", sign, Math.abs(change));
    }
}
