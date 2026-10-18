package com.anticlean;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class ProtectionManager {

    private final AntiCleanPlugin plugin;
    private final Map<UUID, ProtectionEntry> byKiller = new HashMap<>();
    private final Map<String, UUID> chestIndex = new HashMap<>();
    private final Map<UUID, AttackerRecord> lastAttackers = new HashMap<>();

    private static final class AttackerRecord {
        final UUID attacker;
        final long time;

        AttackerRecord(UUID attacker, long time) {
            this.attacker = attacker;
            this.time = time;
        }
    }

    public void recordAttack(UUID attacker, UUID victim) {
        lastAttackers.put(victim, new AttackerRecord(attacker, System.currentTimeMillis()));
    }

    public UUID getRecentAttacker(UUID victim, long maxAgeMillis) {
        AttackerRecord record = lastAttackers.get(victim);
        if (record == null || System.currentTimeMillis() - record.time > maxAgeMillis) {
            return null;
        }
        return record.attacker;
    }

    public ProtectionManager(AntiCleanPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean isProtected(UUID playerId) {
        return byKiller.containsKey(playerId);
    }

    public ProtectionEntry getEntry(UUID killerId) {
        return byKiller.get(killerId);
    }

    public void createEntry(ProtectionEntry entry) {
        removeEntry(entry.getKiller());
        byKiller.put(entry.getKiller(), entry);
        chestIndex.put(blockKey(entry.getChestA()), entry.getKiller());
        chestIndex.put(blockKey(entry.getChestB()), entry.getKiller());
    }

    public ProtectionEntry removeEntry(UUID killerId) {
        ProtectionEntry entry = byKiller.remove(killerId);
        if (entry != null) {
            chestIndex.remove(blockKey(entry.getChestA()));
            chestIndex.remove(blockKey(entry.getChestB()));
        }
        return entry;
    }

    public ProtectionEntry findByChest(Inventory inventory) {
        InventoryHolder holder = inventory.getHolder();
        if (holder instanceof DoubleChest dc) {
            UUID killer = lookupSide(dc.getLeftSide());
            if (killer == null) {
                killer = lookupSide(dc.getRightSide());
            }
            return killer != null ? byKiller.get(killer) : null;
        }
        if (holder instanceof Chest chest) {
            UUID killer = chestIndex.get(blockKey(chest.getBlock().getLocation()));
            return killer != null ? byKiller.get(killer) : null;
        }
        return null;
    }

    private UUID lookupSide(InventoryHolder side) {
        if (side instanceof Chest chest) {
            return chestIndex.get(blockKey(chest.getBlock().getLocation()));
        }
        return null;
    }

    public ProtectionEntry findByChestBlock(Block block) {
        UUID killer = chestIndex.get(blockKey(block.getLocation()));
        return killer != null ? byKiller.get(killer) : null;
    }

    public void checkExpiry() {
        long now = System.currentTimeMillis();
        lastAttackers.entrySet().removeIf(e -> now - e.getValue().time > 30_000L);
        List<UUID> expired = new ArrayList<>();
        for (Map.Entry<UUID, ProtectionEntry> e : byKiller.entrySet()) {
            if (e.getValue().isExpired(now)) {
                expired.add(e.getKey());
            }
        }
        for (UUID id : expired) {
            ProtectionEntry entry = removeEntry(id);
            if (entry != null) {
                org.bukkit.entity.Player killer = plugin.getServer().getPlayer(id);
                if (killer != null && killer.isOnline()) {
                    killer.sendMessage("§e[Anti-Clean] Your protection has expired. The chest is now public.");
                }
            }
        }
    }

    public void shutdown() {
        byKiller.clear();
        chestIndex.clear();
        lastAttackers.clear();
    }

    public long getDurationSeconds() {
        return plugin.getConfig().getLong("protection-duration", 45L);
    }

    public static String blockKey(Location loc) {
        World world = loc.getWorld();
        if (world == null) {
            return "null";
        }
        return world.getName() + ":" + loc.getBlockX() + ":" + loc.getBlockY() + ":" + loc.getBlockZ();
    }

    public Location[] placeDoubleChest(Location near) {
        World world = near.getWorld();
        if (world == null) {
            return null;
        }
        int bx = near.getBlockX();
        int by = near.getBlockY();
        int bz = near.getBlockZ();

        for (int r = 0; r <= 6; r++) {
            for (int dx = -r; dx <= r; dx++) {
                for (int dy = -r; dy <= r; dy++) {
                    for (int dz = -r; dz <= r; dz++) {
                        Location a = new Location(world, bx + dx, by + dy, bz + dz);
                        if (tryPlacePair(a, 1, 0)) {
                            return new Location[]{a, a.clone().add(1, 0, 0)};
                        }
                        if (tryPlacePair(a, 0, 1)) {
                            return new Location[]{a, a.clone().add(0, 0, 1)};
                        }
                    }
                }
            }
        }
        return forcePlace(near);
    }

    public int fillChest(Location chestLoc, List<ItemStack> loot) {
        Block block = chestLoc.getBlock();
        if (!(block.getState() instanceof Chest chest)) {
            return -1;
        }
        Inventory inv = chest.getInventory();
        HashMap<Integer, ItemStack> leftover = inv.addItem(loot.toArray(new ItemStack[0]));
        if (!leftover.isEmpty()) {
            for (ItemStack item : leftover.values()) {
                block.getWorld().dropItemNaturally(block.getLocation().add(0.5, 0.5, 0.5), item);
            }
        }
        return inv.getSize();
    }

    private boolean tryPlacePair(Location a, int dx, int dz) {
        Location b = a.clone().add(dx, 0, dz);
        Block blockA = a.getBlock();
        Block blockB = b.getBlock();
        if (!isReplaceable(blockA) || !isReplaceable(blockB)) {
            return false;
        }
        if (!blockA.getRelative(0, -1, 0).getType().isSolid()
                || !blockB.getRelative(0, -1, 0).getType().isSolid()) {
            return false;
        }
        placeChests(blockA, blockB, dx, dz);
        return true;
    }

    private Location[] forcePlace(Location near) {
        Block a = near.getBlock();
        Block b = a.getRelative(1, 0, 0);
        if (!isReplaceable(a)) {
            a.setType(Material.AIR, false);
        }
        if (!isReplaceable(b)) {
            b.setType(Material.AIR, false);
        }
        if (!a.getRelative(0, -1, 0).getType().isSolid()) {
            a.getRelative(0, -1, 0).setType(Material.STONE, false);
        }
        if (!b.getRelative(0, -1, 0).getType().isSolid()) {
            b.getRelative(0, -1, 0).setType(Material.STONE, false);
        }
        placeChests(a, b, 1, 0);
        return new Location[]{a.getLocation(), b.getLocation()};
    }

    private void placeChests(Block blockA, Block blockB, int dx, int dz) {
        blockA.setType(Material.CHEST, false);
        blockB.setType(Material.CHEST, false);

        BlockFace pairDirection = dx != 0 ? BlockFace.EAST : BlockFace.SOUTH;
        BlockFace facing = counterClockWise(pairDirection);

        org.bukkit.block.data.type.Chest dataA =
                (org.bukkit.block.data.type.Chest) blockA.getBlockData();
        dataA.setFacing(facing);
        dataA.setType(org.bukkit.block.data.type.Chest.Type.LEFT);
        blockA.setBlockData(dataA, false);

        org.bukkit.block.data.type.Chest dataB =
                (org.bukkit.block.data.type.Chest) blockB.getBlockData();
        dataB.setFacing(facing);
        dataB.setType(org.bukkit.block.data.type.Chest.Type.RIGHT);
        blockB.setBlockData(dataB, false);
    }

    private static BlockFace counterClockWise(BlockFace face) {
        return switch (face) {
            case EAST -> BlockFace.NORTH;
            case SOUTH -> BlockFace.EAST;
            case WEST -> BlockFace.SOUTH;
            default -> BlockFace.WEST;
        };
    }

    private boolean isReplaceable(Block block) {
        Material type = block.getType();
        return type.isAir()
                || type == Material.SHORT_GRASS
                || type == Material.FERN
                || type == Material.LARGE_FERN
                || type == Material.TALL_GRASS
                || type == Material.DEAD_BUSH
                || type == Material.SNOW
                || type == Material.VINE;
    }
}
