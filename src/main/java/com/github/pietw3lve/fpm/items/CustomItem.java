package com.github.pietw3lve.fpm.items;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.meta.ItemMeta;

import com.github.pietw3lve.fpm.utils.CustomItemUtil;

/**
 * Represents a custom item in the game.
 */
public class CustomItem {

    private NamespacedKey key;
    private Material material;
    private ItemMeta meta;

    /**
     * Constructs a new CustomItem.
     *
     * @param key the unique key for the custom item
     * @param material the material of the custom item
     * @param meta the meta information for the custom item
     */
    public CustomItem(NamespacedKey key, Material material, ItemMeta meta) {
        this.key = key;
        this.material = material;
        this.meta = meta;
    }

    /**
     * Registers the custom item using the CustomItemUtil.
     */
    public void register() {
        CustomItemUtil.registerCustomItem(key, this);
    }

    /**
     * Gets the unique key for the custom item.
     *
     * @return the unique key
     */
    public NamespacedKey getKey() {
        return key;
    }

    /**
     * Gets the material of the custom item.
     *
     * @return the material
     */
    public Material getMaterial() {
        return material;
    }

    /**
     * Gets the meta information for the custom item.
     *
     * @return the meta information
     */
    public ItemMeta getMeta() {
        return meta;
    }
}
