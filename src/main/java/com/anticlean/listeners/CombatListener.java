package com.anticlean.listeners;

import com.anticlean.AntiCleanPlugin;
import com.anticlean.ProtectionEntry;
import com.anticlean.ProtectionManager;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.projectiles.ProjectileSource;

public final class CombatListener implements Listener {

    private final AntiCleanPlugin plugin;

    public CombatListener(AntiCleanPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) {
            return;
        }

        Player attacker = resolveAttacker(event.getDamager());
        if (attacker == null || attacker.equals(victim)) {
            return;
        }

        ProtectionManager manager = plugin.getProtectionManager();
        long now = System.currentTimeMillis();

        ProtectionEntry attackerEntry = manager.getEntry(attacker.getUniqueId());
        if (attackerEntry != null && !attackerEntry.isExpired(now)) {
            manager.removeEntry(attacker.getUniqueId());
            attacker.sendMessage("§c[Anti-Clean] Your protection was cancelled because you attacked someone!");
            victim.sendMessage("§c[Anti-Clean] The attacker's protection has been cancelled!");
        }

        ProtectionEntry victimEntry = manager.getEntry(victim.getUniqueId());
        if (victimEntry != null && !victimEntry.isExpired(now)) {
            event.setCancelled(true);
            attacker.sendMessage("§c[Anti-Clean] " + victim.getName() + " is protected for another "
                    + victimEntry.remainingSeconds(now) + "s.");
        }

        if (!event.isCancelled()) {
            manager.recordAttack(attacker.getUniqueId(), victim.getUniqueId());
        }
    }

    private Player resolveAttacker(Entity damager) {
        if (damager instanceof Player player) {
            return player;
        }
        if (damager instanceof Projectile projectile) {
            ProjectileSource shooter = projectile.getShooter();
            if (shooter instanceof Player player) {
                return player;
            }
        }
        if (damager instanceof TNTPrimed tnt && tnt.getSource() instanceof Player player) {
            return player;
        }
        return null;
    }
}
