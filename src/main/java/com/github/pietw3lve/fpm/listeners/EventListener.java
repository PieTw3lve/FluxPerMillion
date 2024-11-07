package com.github.pietw3lve.fpm.listeners;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockFertilizeEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.block.BrewingStartEvent;
import org.bukkit.event.entity.EntityBreedEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.world.StructureGrowEvent;

import com.github.pietw3lve.fpm.FluxPerMillion;
import com.github.pietw3lve.fpm.listeners.block.BlockBurnAction;
import com.github.pietw3lve.fpm.listeners.block.BrewingStartAction;
import com.github.pietw3lve.fpm.listeners.block.CampfireBreakAction;
import com.github.pietw3lve.fpm.listeners.block.CampfirePlaceAction;
import com.github.pietw3lve.fpm.listeners.block.CoalBreakAction;
import com.github.pietw3lve.fpm.listeners.block.CropFertilizeAction;
import com.github.pietw3lve.fpm.listeners.block.CropMaxAgeAction;
import com.github.pietw3lve.fpm.listeners.block.FirePlaceAction;
import com.github.pietw3lve.fpm.listeners.block.GrassSpreadAction;
import com.github.pietw3lve.fpm.listeners.block.PlaceMetadataSetAction;
import com.github.pietw3lve.fpm.listeners.block.TorchBreakAction;
import com.github.pietw3lve.fpm.listeners.block.TorchPlaceAction;
import com.github.pietw3lve.fpm.listeners.block.TreeBreakAction;
import com.github.pietw3lve.fpm.listeners.entity.EntityBreedAction;
import com.github.pietw3lve.fpm.listeners.entity.ExplosionPrimeAction;
import com.github.pietw3lve.fpm.listeners.entity.ItemDespawnAction;
import com.github.pietw3lve.fpm.listeners.entity.PotionThrownAction;
import com.github.pietw3lve.fpm.listeners.inventory.FuelSmeltAction;
import com.github.pietw3lve.fpm.listeners.player.ComposterAction;
import com.github.pietw3lve.fpm.listeners.player.ElytraBoostAction;
import com.github.pietw3lve.fpm.listeners.player.FireworkLaunchAction;
import com.github.pietw3lve.fpm.listeners.player.FishCaughtAction;
import com.github.pietw3lve.fpm.listeners.player.LogStripAction;
import com.github.pietw3lve.fpm.listeners.player.MinecartBoostAction;
import com.github.pietw3lve.fpm.listeners.player.MinecartOverclockAction;
import com.github.pietw3lve.fpm.listeners.player.UpdateFPMBarAction;
import com.github.pietw3lve.fpm.listeners.world.TreeGrowAction;
import com.github.pietw3lve.fpm.utils.EventActionUtil;

public class EventListener implements Listener {
    
    private Map<Class<? extends Event>, List<EventActionUtil<? extends Event>>> actions;
    
    public EventListener(FluxPerMillion plugin) {
        actions = new HashMap<>();
        actions.put(BlockBurnEvent.class, new ArrayList<>(Arrays.asList(new BlockBurnAction(plugin))));
        actions.put(BlockFertilizeEvent.class, new ArrayList<>(Arrays.asList(new CropFertilizeAction(plugin))));
        actions.put(BlockGrowEvent.class, new ArrayList<>(Arrays.asList(new CropMaxAgeAction(plugin))));
        actions.put(BlockSpreadEvent.class, new ArrayList<>(Arrays.asList(new GrassSpreadAction(plugin))));
        actions.put(BlockBreakEvent.class, new ArrayList<>(Arrays.asList(new CampfireBreakAction(plugin), new CoalBreakAction(plugin), new TorchBreakAction(plugin), new TreeBreakAction(plugin))));
        actions.put(BlockPlaceEvent.class, new ArrayList<>(Arrays.asList(new PlaceMetadataSetAction(plugin), new CampfirePlaceAction(plugin), new TorchPlaceAction(plugin), new FirePlaceAction(plugin))));
        actions.put(EntityBreedEvent.class, new ArrayList<>(Arrays.asList(new EntityBreedAction(plugin))));
        actions.put(ItemDespawnEvent.class, new ArrayList<>(Arrays.asList(new ItemDespawnAction(plugin))));
        actions.put(FurnaceBurnEvent.class, new ArrayList<>(Arrays.asList(new FuelSmeltAction(plugin))));
        actions.put(PlayerInteractEvent.class, new ArrayList<>(Arrays.asList(new ComposterAction(plugin), new ElytraBoostAction(plugin), new FireworkLaunchAction(plugin), new LogStripAction(plugin), new MinecartBoostAction(plugin), new MinecartOverclockAction(plugin))));
        actions.put(PlayerFishEvent.class, new ArrayList<>(Arrays.asList(new FishCaughtAction(plugin))));
        actions.put(PlayerJoinEvent.class, new ArrayList<>(Arrays.asList(new UpdateFPMBarAction(plugin))));
        actions.put(StructureGrowEvent.class, new ArrayList<>(Arrays.asList(new TreeGrowAction(plugin))));
        actions.put(ExplosionPrimeEvent.class, new ArrayList<>(Arrays.asList(new ExplosionPrimeAction(plugin))));
        actions.put(ProjectileHitEvent.class, new ArrayList<>(Arrays.asList(new PotionThrownAction(plugin))));
        actions.put(BrewingStartEvent.class, new ArrayList<>(Arrays.asList(new BrewingStartAction(plugin))));
    }

    @EventHandler
    public void onBlockBurn(BlockBurnEvent event) {
        handleEvent(event);
    }

    @EventHandler
    public void onBlockFertilize(BlockFertilizeEvent event) {
        handleEvent(event);
    }

    @EventHandler
    public void onBlockGrow(BlockGrowEvent event) {
        handleEvent(event);
    }

    @EventHandler
    public void onBlockSpread(BlockSpreadEvent event) {
        handleEvent(event);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        handleEvent(event);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        handleEvent(event);
    }

    @EventHandler
    public void onEntityBreed(EntityBreedEvent event) {
        handleEvent(event);
    }

    @EventHandler
    public void onItemDespawn(ItemDespawnEvent event) {
        handleEvent(event);
    }

    @EventHandler
    public void onFurnaceBurn(FurnaceBurnEvent event) {
        handleEvent(event);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        handleEvent(event);
    }

    @EventHandler
    public void onPlayerFish(PlayerFishEvent event) {
        handleEvent(event);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        handleEvent(event);
    }

    @EventHandler
    public void onStructureGrow(StructureGrowEvent event) {
        handleEvent(event);
    }

    @EventHandler
    public void onExplosionPrime(ExplosionPrimeEvent event) {
        handleEvent(event);
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        handleEvent(event);
    }

    @EventHandler
    public void onBrewingStart(BrewingStartEvent event) {
        handleEvent(event);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private <T extends Event> void handleEvent(T event) {
        List<EventActionUtil<? extends Event>> eventActions = actions.get(event.getClass());
        if (eventActions != null) {
            for (EventActionUtil action : eventActions) {
                if (action.matches(event)) {
                    action.execute(event);
                }
            }
        }
    }
}
