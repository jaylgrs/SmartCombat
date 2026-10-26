package CombatLog;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class CombatListener implements Listener {

    private final SmartCombat plugin;
    private final CombatManager combatManager;
    private final Set<UUID> loggedOut = new HashSet<>();
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public CombatListener(
            SmartCombat plugin,
            CombatManager combatManager
    ) {
        this.plugin = plugin;
        this.combatManager = combatManager;
    }

    @EventHandler(
            priority = EventPriority.MONITOR,
            ignoreCancelled = true
    )
    public void onPlayerDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player attacker)) {
            return;
        }

        if (!(event.getEntity() instanceof Player victim)) {
            return;
        }

        if (attacker.equals(victim)) {
            return;
        }

        combatManager.tagPlayers(attacker, victim);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        combatManager.clearCombat(player);
        loggedOut.remove(player.getUniqueId());
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();

        combatManager.clearCombat(player);
        loggedOut.remove(player.getUniqueId());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        if (!combatManager.isInCombat(player)) {
            combatManager.clearCombat(player);
            return;
        }

        if (!plugin.getConfig().getBoolean("combat-log.enabled", true)) {
            combatManager.clearCombat(player);
            return;
        }

        UUID playerId = player.getUniqueId();
        Player killer = combatManager.getLastHitPlayer(player);

        loggedOut.add(playerId);

        if (killer != null && !killer.equals(player)) {
            combatManager.incrementKills(killer);
            broadcastSlain(player, killer);
            combatManager.clearCombat(killer);
        }

        combatManager.clearCombat(player);

        if (player.getHealth() > 0) {
            player.setHealth(0);
        }
    }

    @EventHandler
    public void onPlayerResurrect(EntityResurrectEvent event) {
        Entity entity = event.getEntity();

        if (entity instanceof Player player
                && loggedOut.contains(player.getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();

        event.deathMessage(null);

        if (loggedOut.remove(victim.getUniqueId())) {
            combatManager.clearCombat(victim);
            return;
        }

        if (killer != null && !killer.equals(victim)) {
            combatManager.incrementKills(killer);
            broadcastSlain(victim, killer);
            combatManager.clearCombat(killer);
        }

        combatManager.clearCombat(victim);
    }

    private void broadcastSlain(Player victim, Player killer) {
        String message = plugin.getLangConfig().getString(
                "combat-slain",
                "<#FF003C>☠ <victim> was slain by <killer>"
        );

        message = message
                .replace("<victim>", victim.getName())
                .replace("<killer>", killer.getName());

        Bukkit.broadcast(miniMessage.deserialize(message));
    }
}