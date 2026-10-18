package com.anticlean.listeners;

import com.anticlean.AntiCleanPlugin;
import com.anticlean.ProtectionEntry;
import com.anticlean.ProtectionManager;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;

import java.util.Iterator;
import java.util.List;

public final class ChestListener implements Listener {

    private final AntiCleanPlugin plugin;

    public ChestListener(AntiCleanPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onOpen(InventoryOpenEvent event) {
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }
        ProtectionManager manager = plugin.getProtectionManager();
        ProtectionEntry entry = manager.findByChest(event.getInventory());
        if (entry == null) {
            return;
        }
        long now = System.currentTimeMillis();
        if (entry.isExpired(now)) {
            return;
        }
        if (!entry.getKiller().equals(player.getUniqueId())) {
            event.setCancelled(true);
            player.sendMessage("§c[Anti-Clean] This chest is protected for another "
                    + entry.remainingSeconds(now) + "s.");
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onHopperMove(InventoryMoveItemEvent event) {
        ProtectionManager manager = plugin.getProtectionManager();
        ProtectionEntry entry = manager.findByChest(event.getSource());
        if (entry != null && !entry.isExpired(System.currentTimeMillis())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        ProtectionManager manager = plugin.getProtectionManager();
        ProtectionEntry entry = manager.findByChestBlock(event.getBlock());
        if (entry == null) {
            return;
        }
        long now = System.currentTimeMillis();
        if (entry.isExpired(now)) {
            manager.removeEntry(entry.getKiller());
            return;
        }
        Player player = event.getPlayer();
        if (!entry.getKiller().equals(player.getUniqueId())) {
            event.setCancelled(true);
            player.sendMessage("§c[Anti-Clean] This chest is protected for another "
                    + entry.remainingSeconds(now) + "s.");
        } else {
            manager.removeEntry(entry.getKiller());
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        stripProtectedChests(event.blockList());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent event) {
        stripProtectedChests(event.blockList());
    }

    private void stripProtectedChests(List<Block> blocks) {
        ProtectionManager manager = plugin.getProtectionManager();
        long now = System.currentTimeMillis();
        Iterator<Block> it = blocks.iterator();
        while (it.hasNext()) {
            ProtectionEntry entry = manager.findByChestBlock(it.next());
            if (entry != null && !entry.isExpired(now)) {
                it.remove();
            }
        }
    }
}
