package com.github.pietw3lve.fpm.events;

import javax.annotation.Nullable;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.github.pietw3lve.fpm.handlers.FluxMeterHandler;

public class FluxLevelChangeEvent extends Event implements Cancellable {
    
    private static final HandlerList handlers = new HandlerList();
    private FluxMeterHandler fluxMeter;
    private Player player;
    private String actionType;
    private String type;
    private double points;
    private boolean isCancelled;

    /**
     * FluxLevelChangeEvent Constructor.
     * @param plugin The FluxPerMillion plugin.
     */
    public FluxLevelChangeEvent(FluxMeterHandler fluxMeter, @Nullable Player player, String actionType, String type, double points) {
        this.fluxMeter = fluxMeter;
        this.player = player;
        this.actionType = actionType;
        this.type = type;
        this.points = points;
        this.isCancelled = false;
    }

    /**
     * FluxLevelChangeEvent Default Constructor.
     */
    public FluxLevelChangeEvent() {
        this.fluxMeter = null;
        this.player = null;
        this.actionType = null;
        this.type = null;
        this.points = 0;
        this.isCancelled = true;
    }

    @Override
    public boolean isCancelled() {
        return this.isCancelled;
    }
    
    @Override
    public void setCancelled(boolean arg0) {
        this.isCancelled = arg0;
    }
    
    /**
     * Returns the handlers for this event.
     * @return The handlers for this event.
     */
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
    
    /**
     * Returns the handler list for this event.
     * @return The handler list for this event.
     */
    public static HandlerList getHandlerList() {
        return handlers;
    }

    /**
     * Returns the flux meter handler.
     * @return The flux meter handler.
     */
    public FluxMeterHandler getFluxMeter() {
        return fluxMeter;
    }

    /**
     * Returns the player.
     * @return The player.
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Returns the action type.
     * @return The action type.
     */
    public String getActionType() {
        return actionType;
    }

    /**
     * Returns the type.
     * @return The type.
     */
    public String getType() {
        return type;
    }

    /**
     * Returns the points.
     * @return The points.
     */
    public double getPoints() {
        return points;
    }

    /**
     * Returns whether the event is player related.
     * @return Whether the event is player related.
     */
    public boolean isPlayerAction() {
        return player != null;
    }
}
