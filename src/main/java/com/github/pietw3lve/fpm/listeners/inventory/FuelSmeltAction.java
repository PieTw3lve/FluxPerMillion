package com.github.pietw3lve.fpm.listeners.inventory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.FurnaceBurnEvent;

import com.github.pietw3lve.fpm.FluxPerMillion;
import com.github.pietw3lve.fpm.events.FluxLevelChangeEvent;
import com.github.pietw3lve.fpm.utils.EventActionUtil;
import com.github.pietw3lve.fpm.utils.SQLiteUtil.ActionCategory;

public class FuelSmeltAction implements EventActionUtil<FurnaceBurnEvent> {
    
    private static final String FLUX_POINTS_FUEL_BURN = "flux_points.fuel_burn";

    private final FluxPerMillion plugin;
    
    private final Map<Set<Material>, Double> energy;
    private final Map<Set<Material>, Double> geothermal;
    private final Map<Set<Material>, Double> plants;
    private final Map<Set<Material>, Double> woods;
    private final Map<Set<Material>, Double> coals;

    public FuelSmeltAction(FluxPerMillion plugin) {
        this.plugin = plugin;
        this.energy = getEnergyMaterials();
        this.geothermal = getGeothermalMaterials();
        this.plants = getPlantMaterials();
        this.woods = getWoodMaterials();
        this.coals = getCoalMaterials();
    }

    @Override
    public boolean matches(FurnaceBurnEvent event) {
        return true;
    }

    @Override
    public void execute(FurnaceBurnEvent event) {
        FluxLevelChangeEvent fluxEvent = new FluxLevelChangeEvent();
        Block furnace = event.getBlock();

        double multiplier = getMultiplier(event.getFuel().getType());
        double points = plugin.getConfig().getDouble(FLUX_POINTS_FUEL_BURN) * (event.getBurnTime() / 200.0) * multiplier;
        
        Collection<Player> players = furnace.getChunk().getPlayersSeeingChunk();
        if (!players.isEmpty()) {
            for (Player player : players) {
                double playerPoints = points / players.size();
                fluxEvent = new FluxLevelChangeEvent(plugin.getFluxMeter(), furnace.getLocation(), player, null, "burned", "fuel", playerPoints, ActionCategory.ENERGY);
                plugin.getServer().getPluginManager().callEvent(fluxEvent);
            }
        } else {
            fluxEvent = new FluxLevelChangeEvent(plugin.getFluxMeter(), furnace.getLocation(), null, null, "burned", "fuel", points, ActionCategory.ENERGY);
            plugin.getServer().getPluginManager().callEvent(fluxEvent);
        }
    }

    private Map<Set<Material>, Double> getEnergyMaterials() {
        Map<Set<Material>, Double> materials = new HashMap<>();
        Set<Material> energy = new HashSet<>();
        energy.add(Material.BLAZE_ROD);
        materials.put(energy, 0.1);
        return materials;
    }

    private Map<Set<Material>, Double> getGeothermalMaterials() {
        Map<Set<Material>, Double> materials = new HashMap<>();
        Set<Material> geothermal = new HashSet<>();
        geothermal.add(Material.LAVA_BUCKET);
        materials.put(geothermal, 0.4);
        return materials;
    }

    private Map<Set<Material>, Double> getPlantMaterials() {
        Map<Set<Material>, Double> materials = new HashMap<>();
        Set<Material> plants = new HashSet<>();
        plants.add(Material.BAMBOO);
        plants.add(Material.DRIED_KELP_BLOCK);
        materials.put(plants, 0.8);
        return materials;
    }

    private Map<Set<Material>, Double> getWoodMaterials() {
        Map<Set<Material>, Double> materials = new HashMap<>();
        Set<Material> woods = new HashSet<>();
        woods.addAll(Tag.SAPLINGS.getValues());
        woods.addAll(Tag.LOGS.getValues());
        woods.addAll(Tag.PLANKS.getValues());
        woods.addAll(Tag.WOODEN_SLABS.getValues());
        woods.addAll(Tag.WOODEN_STAIRS.getValues());
        woods.addAll(Tag.WOODEN_TRAPDOORS.getValues());
        woods.addAll(Tag.WOODEN_DOORS.getValues());
        woods.addAll(Tag.WOODEN_FENCES.getValues());
        woods.addAll(Tag.WOODEN_PRESSURE_PLATES.getValues());
        woods.addAll(Tag.WOODEN_BUTTONS.getValues());
        woods.addAll(Tag.ITEMS_BOATS.getValues());
        woods.addAll(Tag.ITEMS_CHEST_BOATS.getValues());
        woods.addAll(Tag.SIGNS.getValues());
        woods.add(Material.WOODEN_SWORD);
        woods.add(Material.WOODEN_SHOVEL);
        woods.add(Material.WOODEN_PICKAXE);
        woods.add(Material.WOODEN_AXE);
        woods.add(Material.WOODEN_HOE);
        woods.add(Material.BOWL);
        woods.removeAll(Tag.ITEMS_NON_FLAMMABLE_WOOD.getValues());
        materials.put(woods, 0.8);
        return materials;
    }

    private Map<Set<Material>, Double> getCoalMaterials() {
        Map<Set<Material>, Double> materials = new HashMap<>();
        Set<Material> coals = new HashSet<>();
        coals.addAll(Tag.ITEMS_COALS.getValues());
        coals.add(Material.COAL_BLOCK);
        materials.put(coals, 2.5);
        return materials;
    }

    private double getMultiplier(Material material) {
        List<Map<Set<Material>, Double>> materials = new ArrayList<>();
        materials.add(energy);
        materials.add(geothermal);
        materials.add(plants);
        materials.add(woods);
        materials.add(coals);

        for (Map<Set<Material>, Double> map : materials) {
            for (Set<Material> set : map.keySet()) {
                if (set.contains(material)) {
                    return map.get(set);
                }
            }
        }
        return 1.0;
    }
}
