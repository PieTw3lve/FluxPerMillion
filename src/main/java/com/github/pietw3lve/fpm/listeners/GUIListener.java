package com.github.pietw3lve.fpm.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;

import com.github.pietw3lve.fpm.utils.GUIUtil;

public class GUIListener implements Listener {

    private final GUIUtil guiUtil;

    public GUIListener(GUIUtil guiUtil) {
        this.guiUtil = guiUtil;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        this.guiUtil.handleClick(event);
    }

    @EventHandler
    public void onOpen(InventoryOpenEvent event) {
        this.guiUtil.handleOpen(event);
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        this.guiUtil.handleClose(event);
    }

}