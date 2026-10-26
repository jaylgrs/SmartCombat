package AntiSameIp;

import CombatLog.SmartCombat;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.net.InetAddress;
import java.net.InetSocketAddress;

public final class Listener implements org.bukkit.event.Listener {

    private final SmartCombat plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public Listener(SmartCombat plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerDamage(EntityDamageByEntityEvent event) {
        if (!plugin.getConfig().getBoolean("anti-same-ip.enabled", true)) {
            return;
        }

        if (!(event.getDamager() instanceof Player attacker)) {
            return;
        }

        if (!(event.getEntity() instanceof Player target)) {
            return;
        }

        if (attacker.equals(target)) {
            return;
        }

        InetAddress attackerAddress = getAddress(attacker);
        InetAddress targetAddress = getAddress(target);

        if (attackerAddress == null || targetAddress == null) {
            return;
        }

        if (!attackerAddress.equals(targetAddress)) {
            return;
        }

        event.setCancelled(true);

        String message = plugin.getLangConfig().getString(
                "anti-same-ip.pvp-blocked",
                "<#FF4D6D>⚠ PvP Denied: You cannot attack players connected from the same IP address."
        );

        attacker.sendMessage(miniMessage.deserialize(message));
    }

    private InetAddress getAddress(Player player) {
        InetSocketAddress socketAddress = player.getAddress();

        if (socketAddress == null) {
            return null;
        }

        return socketAddress.getAddress();
    }
}