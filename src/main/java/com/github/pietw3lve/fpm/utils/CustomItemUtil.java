package com.github.pietw3lve.fpm.utils;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

import com.github.pietw3lve.fpm.items.CustomItem;

/**
 * Utility class for managing custom items.
 */
public class CustomItemUtil {
    
    private static Map<NamespacedKey, CustomItem> customItems = new HashMap<>();

    /**
     * Registers a custom item.
     *
     * @param namespace the unique key for the custom item
     * @param customItem the custom item to register
     */
    public static void registerCustomItem(NamespacedKey namespace, CustomItem customItem) {
        customItems.put(namespace, customItem);
    }

    /**
     * Unregisters a custom item.
     *
     * @param namespace the unique key for the custom item
     */
    public static void unregisterCustomItem(NamespacedKey namespace) {
        customItems.remove(namespace);
    }

    /**
     * Retrieves a custom item by its unique key.
     *
     * @param namespace the unique key for the custom item
     * @return the custom item, or null if not found
     */
    public static CustomItem getCustomItem(NamespacedKey namespace) {
        return customItems.get(namespace);
    }

    /**
     * Creates an ItemStack from a custom item.
     *
     * @param namespace the unique key for the custom item
     * @return the ItemStack representing the custom item
     */
    public static ItemStack getItemStack(NamespacedKey namespace) {
        CustomItem customItem = getCustomItem(namespace);

        if (customItem == null) {
            return null;
        }

        ItemStack item = new ItemStack(customItem.getMaterial());
        item.setItemMeta(customItem.getMeta());
        return item;
    }

    /**
     * Checks if a custom item is registered.
     *
     * @param namespace the unique key for the custom item
     * @return true if the custom item is registered, false otherwise
     */
    public static boolean isCustomItem(NamespacedKey namespace) {
        return customItems.containsKey(namespace);
    }
}
