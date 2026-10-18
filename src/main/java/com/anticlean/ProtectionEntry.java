package com.anticlean;

import org.bukkit.Location;

import java.util.UUID;

public final class ProtectionEntry {

    private final UUID killer;
    private final UUID victim;
    private final Location chestA;
    private final Location chestB;
    private final long expiresAtMillis;

    public ProtectionEntry(UUID killer, UUID victim, Location chestA, Location chestB, long expiresAtMillis) {
        this.killer = killer;
        this.victim = victim;
        this.chestA = chestA;
        this.chestB = chestB;
        this.expiresAtMillis = expiresAtMillis;
    }

    public UUID getKiller() {
        return killer;
    }

    public UUID getVictim() {
        return victim;
    }

    public Location getChestA() {
        return chestA;
    }

    public Location getChestB() {
        return chestB;
    }

    public long getExpiresAtMillis() {
        return expiresAtMillis;
    }

    public boolean isExpired(long now) {
        return now >= expiresAtMillis;
    }

    public long remainingSeconds(long now) {
        return Math.max(0, (expiresAtMillis - now) / 1000L);
    }
}
