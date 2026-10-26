package AntiSpamKill;

import CombatLog.SmartCombat;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.Locale;

public final class Listener implements org.bukkit.event.Listener {

    private final SmartCombat plugin;
    private final Manager manager;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public Listener(SmartCombat plugin, Manager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerDamage(EntityDamageByEntityEvent event) {
        if (!manager.isEnabled()) {
            return;
        }

        if (!(event.getDamager() instanceof Player attacker)) {
            return;
        }

        if (!(event.getEntity() instanceof Player victim)) {
            return;
        }

        if (attacker.equals(victim)) {
            return;
        }

        if (!manager.hasActiveCooldown(attacker, victim)) {
            return;
        }

        Player protectedPlayer = manager.getProtectedPlayer(attacker, victim);

        if (protectedPlayer == null
                || !manager.isProtectionEnabled(protectedPlayer)) {
            return;
        }

        event.setCancelled(true);

        String duration = formatDuration(
                manager.getRemainingSeconds(attacker, victim)
        );

        if (attacker.equals(protectedPlayer)) {
            String message = plugin.getLangConfig().getString(
                    "spam-kill.victim-protected",
                    "<#FFB347>You are protected from spam killing. "
                            + "Disable your protection with "
                            + "/spamkill protect disable to fight back. "
                            + "Remaining protection: {time}."
            );

            sendMessage(attacker, message, duration);
            return;
        }

        String message = plugin.getLangConfig().getString(
                "spam-kill.attacker-blocked",
                "<#FF4D6D>You cannot attack this player while "
                        + "spam-kill protection is active. "
                        + "Remaining protection: {time}."
        );

        sendMessage(attacker, message, duration);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (!manager.isEnabled()) {
            return;
        }

        Player victim = event.getEntity();
        Player killer = victim.getKiller();

        if (killer == null || killer.equals(victim)) {
            return;
        }

        manager.startCooldown(killer, victim);
    }

    private void sendMessage(
            Player player,
            String message,
            String duration
    ) {
        message = message.replace("{time}", duration);
        player.sendMessage(miniMessage.deserialize(message));
    }

    private String formatDuration(long seconds) {
        long minutes = seconds / 60;
        long remainingSeconds = seconds % 60;

        if (minutes > 0) {
            return String.format(
                    Locale.ROOT,
                    "%dm %02ds",
                    minutes,
                    remainingSeconds
            );
        }

        return remainingSeconds + "s";
    }
}