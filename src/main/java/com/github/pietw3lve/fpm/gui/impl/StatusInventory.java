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
import java.util.Deque;
import java.util.stream.Collectors;

import com.github.pietw3lve.fpm.FluxPerMillion;
import com.github.pietw3lve.fpm.gui.InventoryButton;
import com.github.pietw3lve.fpm.gui.InventoryGUI;
import com.github.pietw3lve.fpm.handlers.FluxHandler;
import com.github.pietw3lve.fpm.handlers.MessageHandler;
import com.github.pietw3lve.fpm.utils.PlayerUtil;

/**
 * Represents the status inventory GUI for displaying world and player status.
 */
public class StatusInventory extends InventoryGUI {

    private final FluxHandler fluxMeter;
    private final MessageHandler.StatusMessages statusMessages;
    private final int lineLength = 45;

    /**
     * Constructs a new StatusInventory.
     *
     * @param plugin the FluxPerMillion plugin instance
     */
    public StatusInventory(FluxPerMillion plugin) {
        this.fluxMeter = plugin.getFluxMeter();
        this.statusMessages = plugin.getMessageHandler().getStatusMessages();
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
        
        double playerFlux = fluxMeter.getPlayerFlux(player.getUniqueId());
        double playerPercent = fluxMeter.getPlayerPercent(playerFlux);
        
        double[] newPercents = getLatestPercentages();
        double[] oldPercents = getPreviousPercentages();

        for (int i = 0; i < inventorySize; i++) {
            this.addButton(i, createFillerIcon(Material.GRAY_STAINED_GLASS_PANE));
        }
        
        this.addButton(12, createIcon(
            PlayerUtil.getPlayerSkull("http://textures.minecraft.net/texture/2e2cc42015e6678f8fd49ccc01fbf787f1ba2c32bcf559a015332fc5db50"),
            ChatColor.translateAlternateColorCodes('&', statusMessages.world.name),
            new ArrayList<String>() {{
                add(ChatColor.translateAlternateColorCodes('&', statusMessages.world.health.replace("{health}", statusMessages.world.conditions.get(fluxMeter.getStatusLevel()))));
                add(ChatColor.translateAlternateColorCodes('&', statusMessages.world.check.replace("{time}", String.format("%dh %dm %ds", 
                    TimeUnit.MILLISECONDS.toHours(fluxMeter.getTimeUntilNextRun()), 
                    TimeUnit.MILLISECONDS.toMinutes(fluxMeter.getTimeUntilNextRun()) % 60, 
                    TimeUnit.MILLISECONDS.toSeconds(fluxMeter.getTimeUntilNextRun()) % 60))));
                add("");
                addAll(wrapLine(ChatColor.translateAlternateColorCodes('&', statusMessages.world.lore), lineLength));
            }}
        ));

        this.addButton(14, createIcon(
            PlayerUtil.getPlayerSkull(player),
            ChatColor.translateAlternateColorCodes('&', statusMessages.player.name),
            new ArrayList<String>() {{
                add(ChatColor.translateAlternateColorCodes('&', statusMessages.player.contribution.replace("{contribution}", String.format("%s%.1f%%", getColorForPlayerContribution(playerPercent), playerPercent))));
                add("");
                addAll(wrapLine(ChatColor.translateAlternateColorCodes('&', statusMessages.player.lore), lineLength));
            }}
        ));

        this.addButton(28, createIcon(
            new ItemStack(Material.FIRE_CHARGE),
            ChatColor.translateAlternateColorCodes('&', statusMessages.energy.name.replace("{history}", formatChange(newPercents[0] - oldPercents[0]))),
            createIconLore(
                statusMessages.energy.contribution.replace("{contribution}", String.format("%.1f%%", newPercents[0])),
                fluxMeter.generateGraph(fluxMeter.getEnergyPercentages()).stream().map(line -> ChatColor.translateAlternateColorCodes('&', statusMessages.menu.graph) + line).collect(Collectors.toList()),
                statusMessages.energy.lore
            )
        ));

        this.addButton(30, createIcon(
            new ItemStack(Material.SPRUCE_SAPLING),
            ChatColor.translateAlternateColorCodes('&', statusMessages.agriculture.name.replace("{history}", formatChange(newPercents[1] - oldPercents[1]))),
            createIconLore(
                statusMessages.agriculture.contribution.replace("{contribution}", String.format("%.1f%%", newPercents[1])),
                fluxMeter.generateGraph(fluxMeter.getAgriculturePercentages()).stream().map(line -> ChatColor.translateAlternateColorCodes('&', statusMessages.menu.graph) + line).collect(Collectors.toList()),
                statusMessages.agriculture.lore
            )
        ));

        this.addButton(32, createIcon(
            new ItemStack(Material.MINECART),
            ChatColor.translateAlternateColorCodes('&', statusMessages.pollution.name.replace("{history}", formatChange(newPercents[2] - oldPercents[2]))),
            createIconLore(
                statusMessages.pollution.contribution.replace("{contribution}", String.format("%.1f%%", newPercents[2])),
                fluxMeter.generateGraph(fluxMeter.getPollutionPercentages()).stream().map(line -> ChatColor.translateAlternateColorCodes('&', statusMessages.menu.graph) + line).collect(Collectors.toList()),
                statusMessages.pollution.lore
            )
        ));

        this.addButton(34, createIcon(
            new ItemStack(Material.TURTLE_EGG),
            ChatColor.translateAlternateColorCodes('&', statusMessages.wildlife.name.replace("{history}", formatChange(newPercents[3] - oldPercents[3]))),
            createIconLore(
                statusMessages.wildlife.contribution.replace("{contribution}", String.format("%.1f%%", newPercents[3])),
                fluxMeter.generateGraph(fluxMeter.getWildlifePercentages()).stream().map(line -> ChatColor.translateAlternateColorCodes('&', statusMessages.menu.graph) + line).collect(Collectors.toList()),
                statusMessages.wildlife.lore
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
            return ChatColor.GOLD.toString();
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
                    meta.setLore(lore);
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
        return ChatColor.translateAlternateColorCodes('&', statusMessages.menu.history.replace("{sign}", sign).replace("{change}", String.format("%.1f%%", Math.abs(change))));
    }

    /**
     * Gets the latest percentages for energy, agriculture, pollution, and wildlife.
     *
     * @return an array of the latest percentages
     */
    private double[] getLatestPercentages() {
        return new double[] {
            getLast(fluxMeter.getEnergyPercentages()),
            getLast(fluxMeter.getAgriculturePercentages()),
            getLast(fluxMeter.getPollutionPercentages()),
            getLast(fluxMeter.getWildlifePercentages())
        };
    }

    /**
     * Gets the previous percentages for energy, agriculture, pollution, and wildlife.
     *
     * @return an array of the previous percentages
     */
    private double[] getPreviousPercentages() {
        return new double[] {
            getSecondLast(fluxMeter.getEnergyPercentages()),
            getSecondLast(fluxMeter.getAgriculturePercentages()),
            getSecondLast(fluxMeter.getPollutionPercentages()),
            getSecondLast(fluxMeter.getWildlifePercentages())
        };
    }

    /**
     * Gets the last element from a deque.
     *
     * @param deque the deque
     * @return the last element
     */
    private double getLast(Deque<Double> deque) {
        return deque.isEmpty() ? 0 : deque.getLast();
    }

    /**
     * Gets the second last element from a deque.
     *
     * @param deque the deque
     * @return the second last element
     */
    private double getSecondLast(Deque<Double> deque) {
        if (deque.size() < 2) return 0;
        return deque.stream().skip(deque.size() - 2).findFirst().orElse(0.0);
    }

    /**
     * Creates the lore for an icon with a contribution, graph, and lore.
     *
     * @param contribution the contribution text
     * @param graph the graph lines
     * @param lore the lore text
     * @return the combined lore as a list of strings
     */
    private List<String> createIconLore(String contribution, List<String> graph, String lore) {
        List<String> iconLore = new ArrayList<>();
        iconLore.add(ChatColor.translateAlternateColorCodes('&', contribution));
        iconLore.add("");
        iconLore.addAll(graph);
        iconLore.add("");
        iconLore.addAll(wrapLore(Arrays.asList(ChatColor.translateAlternateColorCodes('&', lore)), lineLength));
        return iconLore;
    }
}
