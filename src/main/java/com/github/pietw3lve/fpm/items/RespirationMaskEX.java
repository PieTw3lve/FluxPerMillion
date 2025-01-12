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

public class RespirationMaskEX implements Listener {
    
    private static final String RESPIRATION_MASK_IDENTIFIER_RECIPE = "respiration_mask_ex_recipe";
    private static final String RESPIRATION_MASK_IDENTIFIER = "respiration_mask_ex";
    private final String RESPIRATION_MASK_ENABLED = "custom_mechanics.item.respiration_mask_ex.enabled";
    private final String RESPIRATION_MASK_MATERIAL = "custom_mechanics.item.respiration_mask_ex.material";
    private final String RESPIRATION_MASK_DURABILITY = "custom_mechanics.item.respiration_mask_ex.durability";
    private final String RESPIRATION_MASK_CONSUME = "custom_mechanics.item.respiration_mask_ex.consume";
    private final String RESPIRATION_MASK_CHARGE = "custom_mechanics.item.respiration_mask_ex.durability_per_charge";
    private final int RESPIRATION_MASK_IDENTIFIER_NUMBER = 10001;

    private final FluxPerMillion plugin;
    private boolean enabled;
    private String name;
    private List<String> lore;
    private String material;
    private int durability;
    private BukkitTask durabilityTask;
    private String consume;
    private int charge;

    public RespirationMaskEX(FluxPerMillion plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        if (event.getSlotType() == InventoryType.SlotType.ARMOR && event.getSlot() == 39) {
            ItemStack current = event.getCurrentItem();
            ItemStack cursor = event.getCursor();
            if (RespirationMaskEX.isItem(this.plugin, cursor)) {
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
        if ((event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) && RespirationMaskEX.isItem(this.plugin, main)) {
            if (!player.isSneaking()) {
                ItemStack helmet = player.getInventory().getHelmet();
                player.getInventory().setHelmet(main);
                player.getInventory().setItemInMainHand(helmet);
                player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_GENERIC, 1, 1);
            } else {
                if (player.getInventory().containsAtLeast(new ItemStack(Material.getMaterial(this.consume)), 1)) {
                    int currentDurability = main.getItemMeta().getPersistentDataContainer().get(new NamespacedKey(plugin, "durability"), PersistentDataType.INTEGER);
                    int maxDurabilityToAdd = this.durability - currentDurability;
                    int itemsToConsume = Math.min(maxDurabilityToAdd / this.charge, player.getInventory().all(Material.getMaterial(this.consume)).values().stream().mapToInt(ItemStack::getAmount).sum());
                    
                    if (itemsToConsume > 0) {
                        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1, 1);
                        player.getInventory().removeItem(new ItemStack(Material.getMaterial(this.consume), itemsToConsume));
                        int newDurability = currentDurability + itemsToConsume * this.charge;
                        
                        ItemMeta meta = main.getItemMeta();
                        meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "durability"), PersistentDataType.INTEGER, newDurability);
                        meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "active"), PersistentDataType.BOOLEAN, true);
                        meta.setDisplayName(name);
                        List<String> updatedLore = lore.stream()
                            .map(line -> ChatColor.translateAlternateColorCodes('&', line.replace("{item}", this.consume.toLowerCase()).replace("{num}", String.valueOf(newDurability)).replace("{max}", String.valueOf(durability))))
                            .collect(ImmutableList.toImmutableList());
                        meta.setLore(updatedLore);
                        main.setItemMeta(meta);
                    }
                }
            }
            event.setCancelled(true);
        }
    }

    public void reload() {
        boolean prevConfig = this.enabled;
        
        this.enabled = plugin.getConfig().getBoolean(RESPIRATION_MASK_ENABLED);
        this.material = plugin.getConfig().getString(RESPIRATION_MASK_MATERIAL);
        this.durability = plugin.getConfig().getInt(RESPIRATION_MASK_DURABILITY);
        this.consume = plugin.getConfig().getString(RESPIRATION_MASK_CONSUME);
        this.charge = plugin.getConfig().getInt(RESPIRATION_MASK_CHARGE);
        this.name = ChatColor.translateAlternateColorCodes('&', plugin.getMessageHandler().getRespirationMaskEXName());
        this.lore = plugin.getMessageHandler().getRespirationMaskEXLore();

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
        recipe.setIngredient('D', Material.NETHERITE_INGOT);
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
        meta.setLore(
            this.lore.stream()
            .map(line -> ChatColor.translateAlternateColorCodes('&', line.replace("{item}", this.consume.toLowerCase()).replace("{num}", String.valueOf(durability)).replace("{max}", String.valueOf(durability))))
            .collect(ImmutableList.toImmutableList())
        );
        meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "durability"), PersistentDataType.INTEGER, durability);
        meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "active"), PersistentDataType.BOOLEAN, true);
        meta.setCustomModelData(RESPIRATION_MASK_IDENTIFIER_NUMBER);
        meta.setMaxStackSize(1);
        item.setItemMeta(meta);
        return item;
    }
    
    private void damageItem() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            ItemStack helmet = player.getInventory().getHelmet();
            if (helmet != null && RespirationMaskEX.isItem(this.plugin, helmet)) {
                int currentDurability = helmet.getItemMeta().getPersistentDataContainer().get(new NamespacedKey(plugin, "durability"), PersistentDataType.INTEGER);
                if (currentDurability > 0) {
                    int newDurability = currentDurability - 1;
                    ItemMeta meta = helmet.getItemMeta();
                    meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "durability"), PersistentDataType.INTEGER, newDurability);
                    meta.setDisplayName(name);
                    meta.setLore(
                        this.lore.stream()
                        .map(line -> ChatColor.translateAlternateColorCodes('&', line.replace("{item}", this.consume.toLowerCase()).replace("{num}", String.valueOf(newDurability)).replace("{max}", String.valueOf(durability))))
                        .collect(ImmutableList.toImmutableList())
                    );

                    if (newDurability == 0) {
                        meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "active"), PersistentDataType.BOOLEAN, false);
                        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_DEACTIVATE, 1, 1);
                    }

                    helmet.setItemMeta(meta);
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

    public static boolean isActivated(FluxPerMillion plugin, ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }
        return item.getItemMeta().getPersistentDataContainer().get(new NamespacedKey(plugin, "active"), PersistentDataType.BOOLEAN);
    }
}
