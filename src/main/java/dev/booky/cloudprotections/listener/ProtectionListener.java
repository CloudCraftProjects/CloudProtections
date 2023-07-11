package dev.booky.cloudprotections.listener;
// Created by booky10 in CraftAttack (17:38 30.10.21)

import com.destroystokyo.paper.event.entity.EntityPathfindEvent;
import dev.booky.cloudprotections.ProtectionsManager;
import dev.booky.cloudprotections.region.ProtectionFlag;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public final class ProtectionListener implements Listener {

    private final ProtectionsManager manager;

    public ProtectionListener(ProtectionsManager manager) {
        this.manager = manager;
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        if (this.manager.isProtected(event.getBlock(), ProtectionFlag.BUILDING, event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        if (this.manager.isProtected(event.getBlock(), ProtectionFlag.BUILDING, event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBucketFill(PlayerBucketFillEvent event) {
        if (this.manager.isProtected(event.getBlock(), ProtectionFlag.BUILDING, event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBucketEmpty(PlayerBucketEmptyEvent event) {
        if (this.manager.isProtected(event.getBlock(), ProtectionFlag.BUILDING, event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (this.manager.isProtected(event.getEntity().getLocation(), ProtectionFlag.HEALTH, null)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (this.manager.isProtected(event.getEntity().getLocation(), ProtectionFlag.MOB_SPAWNING, null)) {
            switch (event.getSpawnReason()) {
                case SPAWNER_EGG, BUILD_SNOWMAN, BUILD_IRONGOLEM, BUILD_WITHER, BREEDING,
                        DISPENSE_EGG, INFECTION, CURED, SHOULDER_ENTITY, DROWNED, SHEARED,
                        PIGLIN_ZOMBIFIED, FROZEN, METAMORPHOSIS, DUPLICATION, COMMAND, CUSTOM, DEFAULT -> { /**/ }
                default -> event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onFoodChange(FoodLevelChangeEvent event) {
        // don't cancel food level regeneration
        if (event.getFoodLevel() >= event.getEntity().getFoodLevel()) {
            return;
        }
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        if (this.manager.isProtected(event.getEntity().getLocation(), ProtectionFlag.HUNGER, player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) {
            return;
        }

        switch (event.getAction()) {
            case LEFT_CLICK_BLOCK, RIGHT_CLICK_BLOCK -> {
                if (!this.manager.isProtected(event.getClickedBlock(), ProtectionFlag.INTERACT, event.getPlayer())) {
                    return;
                }

                Material blockType = event.getClickedBlock().getType();
                if (blockType == Material.ENDER_CHEST) {
                    return;
                }

                // try to not handle block places, because handling them here seems more "buggy"
                if (!event.getMaterial().isBlock()) {
                    event.setUseInteractedBlock(Event.Result.DENY);
                } else if (blockType.isInteractable() && !event.getPlayer().isSneaking()) {
                    event.setUseInteractedBlock(Event.Result.DENY);
                }
            }
            case PHYSICAL -> {
                // null is used for the entity, because creative players should still not trample farmland
                if (this.manager.isProtected(event.getClickedBlock(), ProtectionFlag.INTERACT, null)) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEntityEvent event) {
        if (this.manager.isProtected(event.getRightClicked().getLocation(), ProtectionFlag.INTERACT, event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onArmorStandManipulate(PlayerArmorStandManipulateEvent event) {
        if (this.manager.isProtected(event.getRightClicked().getLocation(), ProtectionFlag.INTERACT, event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onHangingBreak(HangingBreakByEntityEvent event) {
        // always cancel break events if it wasn't caused by a player, otherwise let the method check for creative
        Player remover = event.getRemover() instanceof Player ? (Player) event.getRemover() : null;

        if (this.manager.isProtected(event.getEntity().getLocation(), ProtectionFlag.INTERACT, remover)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityExplosion(EntityExplodeEvent event) {
        event.blockList().removeIf(block -> this.manager.isProtected(block, ProtectionFlag.EXPLOSION, null));
    }

    @EventHandler
    public void onBlockExplosion(BlockExplodeEvent event) {
        event.blockList().removeIf(block -> this.manager.isProtected(block, ProtectionFlag.EXPLOSION, null));
    }

    @EventHandler
    public void onEntityRegainNegativeHealth(EntityRegainHealthEvent event) {
        // Paper doesn't want to fix it :(
        if (event.getAmount() < 0d) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onRedstone(BlockRedstoneEvent event) {
        if (this.manager.isProtected(event.getBlock(), ProtectionFlag.REDSTONE, null)) {
            event.setNewCurrent(0);
        }
    }

    private boolean onPiston(Block piston, Iterable<Block> moved) {
        if (this.manager.isProtected(piston, ProtectionFlag.REDSTONE, null)) {
            return true;
        }

        for (Block block : moved) {
            if (this.manager.isProtected(block, ProtectionFlag.REDSTONE, null)) {
                return false;
            }
        }
        return true;
    }

    @EventHandler
    public void onPistonExtend(BlockPistonExtendEvent event) {
        this.onPiston(event.getBlock(), event.getBlocks());
    }

    @EventHandler
    public void onPistonRetract(BlockPistonRetractEvent event) {
        this.onPiston(event.getBlock(), event.getBlocks());
    }

    @EventHandler
    public void onPathfind(EntityPathfindEvent event) {
        if (this.manager.isProtected(event.getLoc(), ProtectionFlag.MOB_AI, null)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onFireSpread(BlockSpreadEvent event) {
        if (event.getSource().getType() != Material.FIRE) {
            return;
        }
        if (event.isCancelled()) {
            return;
        }

        if (this.manager.isProtected(event.getBlock(), ProtectionFlag.FIRE, null)) {
            event.setCancelled(true);
        } else if (this.manager.isProtected(event.getSource(), ProtectionFlag.FIRE, null)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockBurn(BlockBurnEvent event) {
        if (this.manager.isProtected(event.getBlock(), ProtectionFlag.FIRE, null)) {
            event.setCancelled(true);
        }
    }
}
