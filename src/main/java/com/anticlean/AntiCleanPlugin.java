package com.anticlean;

import com.anticlean.listeners.ChestListener;
import com.anticlean.listeners.CombatListener;
import com.anticlean.listeners.KillListener;
import org.bukkit.plugin.java.JavaPlugin;

public final class AntiCleanPlugin extends JavaPlugin {

    private ProtectionManager protectionManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        protectionManager = new ProtectionManager(this);
        getServer().getPluginManager().registerEvents(new KillListener(this), this);
        getServer().getPluginManager().registerEvents(new CombatListener(this), this);
        getServer().getPluginManager().registerEvents(new ChestListener(this), this);
        getServer().getScheduler().runTaskTimer(this, protectionManager::checkExpiry, 20L, 20L);
        getLogger().info("Anti-Clean enabled. Protection duration: "
                + getConfig().getLong("protection-duration", 45L) + "s");
    }

    @Override
    public void onDisable() {
        if (protectionManager != null) {
            protectionManager.shutdown();
        }
    }

    public ProtectionManager getProtectionManager() {
        return protectionManager;
    }
}
