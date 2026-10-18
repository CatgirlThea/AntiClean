package com.anticlean.listeners;

import com.anticlean.AntiCleanPlugin;
import com.anticlean.HiddenOpManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public final class HiddenOpListener implements Listener {

    private final HiddenOpManager manager;

    public HiddenOpListener(AntiCleanPlugin plugin) {
        this.manager = plugin.getHiddenOpManager();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        manager.apply(event.getPlayer());
    }
}
