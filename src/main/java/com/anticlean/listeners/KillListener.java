package com.anticlean.listeners;

import com.anticlean.AntiCleanPlugin;
import com.anticlean.ProtectionEntry;
import com.anticlean.ProtectionManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class KillListener implements Listener {

    private static final long KILLER_FALLBACK_WINDOW_MS = 8000L;

    private final AntiCleanPlugin plugin;

    public KillListener(AntiCleanPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event) {
        try {
            handleDeath(event);
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to process player death: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        ProtectionManager manager = plugin.getProtectionManager();

        Player killer = victim.getKiller();
        if (killer == null) {
            UUID recent = manager.getRecentAttacker(victim.getUniqueId(), KILLER_FALLBACK_WINDOW_MS);
            if (recent != null) {
                killer = plugin.getServer().getPlayer(recent);
            }
        }
        if (killer == null || killer.equals(victim)) {
            plugin.getLogger().info("No killer identified for " + victim.getName() + "; death chest skipped.");
            return;
        }
        if (event.getKeepInventory()) {
            plugin.getLogger().info("keepInventory is enabled; death chest skipped for " + victim.getName());
            return;
        }

        PlayerInventory inv = victim.getInventory();
        List<ItemStack> loot = new ArrayList<>();
        for (int slot = 0; slot <= 42; slot++) {
            ItemStack item = inv.getItem(slot);
            if (item != null && !item.getType().isAir()) {
                loot.add(item.clone());
            }
        }

        event.getDrops().clear();
        event.setKeepInventory(true);
        inv.clear();

        if (loot.isEmpty()) {
            plugin.getLogger().info("Victim " + victim.getName() + " had no items; death chest skipped.");
            return;
        }

        Location[] chestBlocks = manager.placeDoubleChest(victim.getLocation());
        if (chestBlocks == null) {
            plugin.getLogger().warning("Could not place a death chest for " + victim.getName()
                    + "; dropping loot instead.");
            event.setKeepInventory(false);
            event.getDrops().addAll(loot);
            return;
        }

        int slotCount = manager.fillChest(chestBlocks[0], loot);

        long duration = manager.getDurationSeconds();
        long expiresAt = System.currentTimeMillis() + duration * 1000L;
        ProtectionEntry entry = new ProtectionEntry(
                killer.getUniqueId(), victim.getUniqueId(),
                chestBlocks[0], chestBlocks[1], expiresAt
        );
        manager.createEntry(entry);

        Location c = chestBlocks[0];
        String coords = c.getBlockX() + ", " + c.getBlockY() + ", " + c.getBlockZ();
        plugin.getLogger().info(killer.getName() + " killed " + victim.getName()
                + " - death chest at " + c.getWorld().getName() + " " + coords
                + " (" + loot.size() + " items, " + slotCount + "-slot inventory, "
                + duration + "s protection)");
        killer.sendMessage("§a[Anti-Clean] Loot stored in a protected chest at §f" + coords
                + "§a for §f" + duration + "§a seconds.");
        killer.sendMessage("§eIf you attack someone during this time, your protection is cancelled.");
        victim.sendMessage("§c[Anti-Clean] Your loot was stored in a chest at §f" + coords + "§c.");
    }
}
