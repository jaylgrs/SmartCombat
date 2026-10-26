package CombatLog;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public final class CombatManager {

    private static final int DEFAULT_COMBAT_DURATION = 21;

    private final JavaPlugin plugin;

    private final Map<UUID, Long> combatUntil = new HashMap<>();
    private final Map<UUID, UUID> lastHit = new HashMap<>();

    public CombatManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void tagPlayers(Player attacker, Player victim) {
        long duration = plugin.getConfig().getLong(
                "combat.duration-seconds",
                DEFAULT_COMBAT_DURATION
        );

        long expiry = System.currentTimeMillis()
                + Math.max(1, duration) * 1000L;

        UUID attackerId = attacker.getUniqueId();
        UUID victimId = victim.getUniqueId();

        combatUntil.put(attackerId, expiry);
        combatUntil.put(victimId, expiry);

        lastHit.put(attackerId, victimId);
        lastHit.put(victimId, attackerId);
    }

    public boolean isInCombat(Player player) {
        UUID uuid = player.getUniqueId();
        Long expiry = combatUntil.get(uuid);

        if (expiry == null) {
            return false;
        }

        if (expiry <= System.currentTimeMillis()) {
            clearCombat(uuid);
            return false;
        }

        return true;
    }

    public int getRemainingSeconds(Player player) {
        Long expiry = combatUntil.get(player.getUniqueId());

        if (expiry == null) {
            return 0;
        }

        long remainingMillis = expiry - System.currentTimeMillis();

        if (remainingMillis <= 0) {
            clearCombat(player);
            return 0;
        }

        return (int) Math.ceil(remainingMillis / 1000.0);
    }

    public Player getLastHitPlayer(Player player) {
        UUID hitterId = lastHit.get(player.getUniqueId());

        if (hitterId == null) {
            return null;
        }

        return Bukkit.getPlayer(hitterId);
    }

    public void clearCombat(UUID uuid) {
        combatUntil.remove(uuid);
        lastHit.remove(uuid);

        Iterator<Map.Entry<UUID, UUID>> iterator =
                lastHit.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<UUID, UUID> entry = iterator.next();

            if (entry.getValue().equals(uuid)) {
                iterator.remove();
            }
        }
    }

    public void clearCombat(Player player) {
        clearCombat(player.getUniqueId());
    }

    public void incrementKills(Player player) {
        String path = "kills." + player.getUniqueId();

        int kills = plugin.getConfig().getInt(path);
        plugin.getConfig().set(path, kills + 1);
        plugin.saveConfig();
    }

    public int getKills(Player player) {
        String path = "kills." + player.getUniqueId();

        return plugin.getConfig().getInt(path);
    }
}