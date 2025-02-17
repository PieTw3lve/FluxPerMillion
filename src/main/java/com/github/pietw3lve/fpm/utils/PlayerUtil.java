package com.github.pietw3lve.fpm.utils;

import java.util.UUID;
import java.net.MalformedURLException;
import java.net.URI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;

public class PlayerUtil {

    /**
     * Creates a player skull with a custom texture from a URL.
     *
     * @param url The URL of the custom texture.
     * @return The ItemStack representing the player skull with the custom texture.
     */
    public static ItemStack getPlayerSkull(String url) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        PlayerProfile profile = Bukkit.createPlayerProfile(UUID.randomUUID());
        PlayerTextures playerTextures = profile.getTextures();

        try {
            playerTextures.setSkin(URI.create(url).toURL());
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return new ItemStack(Material.PLAYER_HEAD);
        }

        meta.setOwnerProfile(profile);
        item.setItemMeta(meta);
        return item;
    }

    /**
     * Creates a player skull based on a player's UUID.
     *
     * @param uuid The UUID of the player.
     * @return The ItemStack representing the player skull.
     */
    public static ItemStack getPlayerSkull(UUID uuid) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        PlayerProfile profile = Bukkit.createPlayerProfile(uuid);
        meta.setOwnerProfile(profile);
        item.setItemMeta(meta);
        return item;
    }

    /**
     * Creates a player skull based on a player.
     *
     * @param player The player to create the skull for.
     * @return The ItemStack representing the player skull.
     */
    public static ItemStack getPlayerSkull(OfflinePlayer player) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        meta.setOwningPlayer(player);
        item.setItemMeta(meta);
        return item;
    }
}
