package AntiSpamKill;

import CombatLog.SmartCombat;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public final class Manager {

    private final SmartCombat plugin;

    private final Map<PlayerPair, CooldownData> killCooldowns = new HashMap<>();
    private final Map<UUID, Boolean> protectionStates = new HashMap<>();

    public Manager(SmartCombat plugin) {
        this.plugin = plugin;
    }

    public boolean isEnabled() {
        return plugin.getConfig().getBoolean("spam-kill.enabled", true);
    }

    public long getCooldownMillis() {
        long minutes = plugin.getConfig().getLong(
                "spam-kill.kill-cooldown-minutes",
                15
        );

        return Math.max(0, minutes) * 60_000L;
    }

    public boolean isProtectionEnabled(Player player) {
        return protectionStates.getOrDefault(
                player.getUniqueId(),
                plugin.getConfig().getBoolean(
                        "spam-kill.protection-enabled",
                        true
                )
        );
    }

    public void disableProtection(Player player) {
        protectionStates.put(player.getUniqueId(), false);
    }

    public void startCooldown(Player killer, Player victim) {
        long cooldown = getCooldownMillis();

        if (cooldown <= 0) {
            return;
        }

        PlayerPair pair = new PlayerPair(
                killer.getUniqueId(),
                victim.getUniqueId()
        );

        killCooldowns.put(
                pair,
                new CooldownData(
                        victim.getUniqueId(),
                        System.currentTimeMillis() + cooldown
                )
        );
    }

    public boolean hasActiveCooldown(Player first, Player second) {
        return getCooldown(first, second) != null;
    }

    public Player getProtectedPlayer(Player first, Player second) {
        CooldownData cooldown = getCooldown(first, second);

        if (cooldown == null) {
            return null;
        }

        UUID protectedId = cooldown.protectedPlayer();

        if (first.getUniqueId().equals(protectedId)) {
            return first;
        }

        if (second.getUniqueId().equals(protectedId)) {
            return second;
        }

        return null;
    }

    public long getRemainingMillis(Player first, Player second) {
        CooldownData cooldown = getCooldown(first, second);

        if (cooldown == null) {
            return 0;
        }

        return Math.max(
                0,
                cooldown.expiryTime() - System.currentTimeMillis()
        );
    }

    public long getRemainingSeconds(Player first, Player second) {
        return (long) Math.ceil(
                getRemainingMillis(first, second) / 1000.0
        );
    }

    public void clearExpiredCooldowns() {
        long now = System.currentTimeMillis();

        Iterator<Map.Entry<PlayerPair, CooldownData>> iterator =
                killCooldowns.entrySet().iterator();

        while (iterator.hasNext()) {
            if (iterator.next().getValue().expiryTime() <= now) {
                iterator.remove();
            }
        }
    }

    private CooldownData getCooldown(Player first, Player second) {
        PlayerPair pair = new PlayerPair(
                first.getUniqueId(),
                second.getUniqueId()
        );

        CooldownData cooldown = killCooldowns.get(pair);

        if (cooldown == null) {
            return null;
        }

        if (cooldown.expiryTime() <= System.currentTimeMillis()) {
            killCooldowns.remove(pair);
            return null;
        }

        return cooldown;
    }

    private record CooldownData(UUID protectedPlayer, long expiryTime) {
    }

    private record PlayerPair(UUID first, UUID second) {

        private PlayerPair {
            if (first.compareTo(second) > 0) {
                UUID temporary = first;
                first = second;
                second = temporary;
            }
        }
    }
}