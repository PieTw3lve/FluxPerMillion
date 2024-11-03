package com.github.pietw3lve.fpm.events;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.github.pietw3lve.fpm.handlers.FluxHandler;

public class StatusLevelChangeEvent extends Event implements Cancellable {
    
    private static final HandlerList handlers = new HandlerList();
    private FluxHandler fluxMeter;
    private int newStatusLevel;
    private int prevStatusLevel;
    private boolean isCancelled;

    /**
     * FluxStatusChange Constructor.
     * @param plugin The FluxPerMillion plugin.
     */
    public StatusLevelChangeEvent(FluxHandler fluxMeter, int newStatusLevel, int prevStatusLevel) {
        this.fluxMeter = fluxMeter;
        this.newStatusLevel = newStatusLevel;
        this.prevStatusLevel = prevStatusLevel;
        this.isCancelled = false;
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
     * Returns the new status level.
     * @return The new status level.
     */
    public int getNewStatusLevel() {
        return newStatusLevel;
    }

    /**
     * Returns the previous status level.
     * @return The previous status level.
     */
    public int getPrevStatusLevel() {
        return prevStatusLevel;
    }
}
