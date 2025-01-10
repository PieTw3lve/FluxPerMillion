package com.github.pietw3lve.fpm.events;

import java.math.BigDecimal;
import java.math.RoundingMode;

import javax.annotation.Nullable;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.github.pietw3lve.fpm.handlers.FluxHandler;
import com.github.pietw3lve.fpm.utils.SQLiteUtil.ActionCategory;

public class FluxLevelChangeEvent extends Event implements Cancellable {
    
    private final String ACTIVE_WORLDS = "worlds.whitelist";

    private static final HandlerList handlers = new HandlerList();
    private FluxHandler fluxMeter;
    private Location location;
    private Player player;
    private String message;
    private String actionType;
    private String type;
    private double points;
    private ActionCategory category;
    private boolean isCancelled;

    /**
     * FluxLevelChangeEvent Constructor.
     * @param plugin The FluxPerMillion plugin.
     */
    public FluxLevelChangeEvent(FluxHandler fluxMeter, Location location, @Nullable Player player, @Nullable String message, String actionType, String type, double points, ActionCategory category) {
        this.fluxMeter = fluxMeter;
        this.location = location;
        this.player = player;
        this.message = message;
        this.actionType = actionType;
        this.type = type;
        this.points = BigDecimal.valueOf(points).setScale(5, RoundingMode.HALF_UP).stripTrailingZeros().doubleValue();
        this.category = category;
        this.isCancelled = points != 0 && fluxMeter.getFluxMeterTask().getOwner().getConfig().getStringList(ACTIVE_WORLDS).contains(location.getWorld().getName()) ? false : true;
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
        this.category = ActionCategory.DEFAULT;
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
    public FluxHandler getFluxMeter() {
        return fluxMeter;
    }

    /**
     * Returns the location.
     * @return The location.
     */
    public Location getLocation() {
        return location;
    }

    /**
     * Returns the player.
     * @return The player.
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Returns the message.
     * @return The message.
     */
    public String getMessage() {
        return message;
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
     * Returns the category.
     * @return The category.
     */
    public ActionCategory getCategory() {
        return category;
    }

    /**
     * Returns whether the event is player related.
     * @return Whether the event is player related.
     */
    public boolean isPlayerAction() {
        return player != null;
    }
}
