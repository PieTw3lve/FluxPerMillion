package com.github.pietw3lve.fpm.items;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitTask;

import com.github.pietw3lve.fpm.FluxPerMillion;
import com.github.pietw3lve.fpm.utils.CustomItemUtil;
import com.google.common.collect.ImmutableList;

import net.md_5.bungee.api.ChatColor;

public class RespirationMask implements Listener {
    
    private static final String RESPIRATION_MASK_IDENTIFIER_RECIPE = "respiration_mask_recipe";
    private static final String RESPIRATION_MASK_IDENTIFIER = "respiration_mask";
    private final String RESPIRATION_MASK_ENABLED = "custom_mechanics.item.respiration_mask.enabled";
    private final String RESPIRATION_MASK_MATERIAL = "custom_mechanics.item.respiration_mask.material";
    private final String RESPIRATION_MASK_DURABILITY = "custom_mechanics.item.respiration_mask.durability";
    private final int RESPIRATION_MASK_IDENTIFIER_NUMBER = 10000;

    private final FluxPerMillion plugin;
    private boolean enabled;
    private String name;
    private List<String> lore;
    private String material;
    private int durability;
    private BukkitTask durabilityTask;

    public RespirationMask(FluxPerMillion plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        if (event.getSlotType() == InventoryType.SlotType.ARMOR && event.getSlot() == 39) {
            ItemStack current = event.getCurrentItem();
            ItemStack cursor = event.getCursor();
            if (RespirationMask.isItem(this.plugin, cursor)) {
                event.setCurrentItem(cursor);
                player.setItemOnCursor(current);
                player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_GENERIC, 1, 1);
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack main = player.getInventory().getItemInMainHand();
        if ((event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) && RespirationMask.isItem(this.plugin, main)) {
            ItemStack helmet = player.getInventory().getHelmet();
            player.getInventory().setHelmet(main);
            player.getInventory().setItemInMainHand(helmet);
            player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_GENERIC, 1, 1);
            event.setCancelled(true);
        }
    }

    public void reload() {
        boolean prevConfig = this.enabled;
        
        this.enabled = plugin.getConfig().getBoolean(RESPIRATION_MASK_ENABLED);
        this.material = plugin.getConfig().getString(RESPIRATION_MASK_MATERIAL);
        this.durability = plugin.getConfig().getInt(RESPIRATION_MASK_DURABILITY);
        this.name = ChatColor.translateAlternateColorCodes('&', plugin.getMessageHandler().getRespirationMaskName());
        this.lore = plugin.getMessageHandler().getRespirationMaskLore();

        if (!this.enabled) {
            unregister();
        } else {
            register(prevConfig);
        }
    }

    private void register(Boolean prevConfig) {
        ItemStack item = getItemStack();
        CustomItem respirationMask = new CustomItem (new NamespacedKey(plugin, RESPIRATION_MASK_IDENTIFIER), item.getType(), item.getItemMeta());
        CustomItemUtil.unregisterCustomItem(new NamespacedKey(plugin, RESPIRATION_MASK_IDENTIFIER));
        respirationMask.register();
        
        ShapedRecipe recipe = new ShapedRecipe(new NamespacedKey(plugin, RESPIRATION_MASK_IDENTIFIER_RECIPE), CustomItemUtil.getItemStack(new NamespacedKey(plugin, RESPIRATION_MASK_IDENTIFIER)));
        
        recipe.shape(" A ", "BCB", "DED");
        recipe.setIngredient('A', Material.STRING);
        recipe.setIngredient('B', Material.LEATHER);
        recipe.setIngredient('C', Material.GLASS_PANE);
        recipe.setIngredient('D', Material.IRON_INGOT);
        recipe.setIngredient('E', Material.WHITE_WOOL);

        if (prevConfig == this.enabled) {
            this.plugin.getServer().removeRecipe(new NamespacedKey(plugin, RESPIRATION_MASK_IDENTIFIER_RECIPE));
            this.plugin.getServer().addRecipe(recipe);
        } else {
            this.plugin.getServer().addRecipe(recipe);
            this.plugin.getServer().getPluginManager().registerEvents(this, plugin);
            this.durabilityTask = this.plugin.getServer().getScheduler().runTaskTimer(plugin, this::damageItem, 0, 20);
        }
    }

    private void unregister() {
        CustomItemUtil.unregisterCustomItem(new NamespacedKey(plugin, RESPIRATION_MASK_IDENTIFIER));
        this.plugin.getServer().removeRecipe(new NamespacedKey(plugin, RESPIRATION_MASK_IDENTIFIER_RECIPE));
        if (durabilityTask != null) {
            durabilityTask.cancel();
        }
        HandlerList.unregisterAll(this);
    }

    private ItemStack getItemStack() {
        ItemStack item = new ItemStack(Material.getMaterial(this.material));
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(this.name);
        meta.setLore(this.lore.stream().map(line -> ChatColor.translateAlternateColorCodes('&', line.replace("{num}", String.valueOf(this.durability)).replace("{max}", String.valueOf(this.durability)))).collect(ImmutableList.toImmutableList()));
        meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "durability"), PersistentDataType.INTEGER, durability);
        meta.setCustomModelData(RESPIRATION_MASK_IDENTIFIER_NUMBER);
        meta.setMaxStackSize(1);
        item.setItemMeta(meta);
        return item;
    }

    private void damageItem() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            ItemStack helmet = player.getInventory().getHelmet();
            if (helmet != null && RespirationMask.isItem(this.plugin, helmet)) {
                int currentDurability = helmet.getItemMeta().getPersistentDataContainer().get(new NamespacedKey(plugin, "durability"), PersistentDataType.INTEGER);
                if (currentDurability > 1) {
                    int newDurability = currentDurability - 1;
                    ItemMeta meta = helmet.getItemMeta();
                    meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "durability"), PersistentDataType.INTEGER, newDurability);
                    meta.setDisplayName(name);
                    meta.setLore(lore.stream().map(line -> ChatColor.translateAlternateColorCodes('&', line.replace("{num}", String.valueOf(newDurability)).replace("{max}", String.valueOf(durability)))).collect(ImmutableList.toImmutableList()));
                    helmet.setItemMeta(meta);
                } else {
                    player.getInventory().setHelmet(null);
                    player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1, 1);
                }
            }
        }
    }

    public static boolean isItem(FluxPerMillion plugin, ItemStack item) {
        ItemStack respirationMask = CustomItemUtil.getItemStack(new NamespacedKey(plugin, RESPIRATION_MASK_IDENTIFIER));
        if (item == null || respirationMask == null || !item.hasItemMeta()) {
            return false;
        }
        return item.getItemMeta().getCustomModelData() == respirationMask.getItemMeta().getCustomModelData();
    }
}