package com.github.pietw3lve.fpm.listeners.block;

import java.util.Random;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import com.github.pietw3lve.fpm.FluxPerMillion;
import com.github.pietw3lve.fpm.events.FluxLevelChangeEvent;
import com.github.pietw3lve.fpm.utils.EventActionUtil;
import com.github.pietw3lve.fpm.utils.SQLiteUtil.ActionCategory;

public class CoalBreakAction implements EventActionUtil<BlockBreakEvent> {
    
    private static final String FLUX_POINTS_COAL_BREAK = "flux_points.coal_break";

    private final FluxPerMillion plugin;
    private final Set<Material> coal;

    public CoalBreakAction(FluxPerMillion plugin) {
        this.plugin = plugin;
        this.coal = Tag.COAL_ORES.getValues();
    }

    @Override
    public boolean matches(BlockBreakEvent event) {
        Block block = event.getBlock();
        ItemStack item = event.getPlayer().getInventory().getItemInMainHand();
        return coal.contains(block.getType()) && (item == null || !item.getEnchantments().containsKey(Enchantment.SILK_TOUCH));
    }

    @Override
    public void execute(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        String blockName = block.getType().toString().replace("_", " ").toLowerCase();
        double points = plugin.getConfig().getDouble(FLUX_POINTS_COAL_BREAK);
        FluxLevelChangeEvent fluxEvent = new FluxLevelChangeEvent(plugin.getFluxMeter(), block.getLocation(), player, null, "removed", blockName, points, ActionCategory.ENERGY);
        plugin.getServer().getPluginManager().callEvent(fluxEvent);
        SpawnCoalParticles(block);
    }

    private void SpawnCoalParticles(Block block) {
        Random rand = new Random();
        int amount = 1 + rand.nextInt(2);
        for (int i = 0; i < amount; i++) {
            Location particle = block.getLocation().clone().add(rand.nextDouble() - 0.1, 0.5, rand.nextDouble() - 0.1);
            double height = rand.nextDouble() * 0.5;
            double speed = 0.15 + rand.nextDouble() * 0.05;
            block.getWorld().spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, particle, 0, 0, height, 0, speed);
        }
    }
}
