package CombatLog.hud;

import CombatLog.CombatManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public final class Hud {

    private final JavaPlugin plugin;
    private final CombatManager combatManager;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    private BukkitTask hudTask;

    public Hud(JavaPlugin plugin, CombatManager combatManager) {
        this.plugin = plugin;
        this.combatManager = combatManager;
    }

    public void start() {
        if (hudTask != null) {
            return;
        }

        hudTask = Bukkit.getScheduler().runTaskTimer(
                plugin,
                this::updateHud,
                1L,
                20L
        );
    }

    public void stop() {
        if (hudTask != null) {
            hudTask.cancel();
            hudTask = null;
        }
    }

    private void updateHud() {
        if (!plugin.getConfig().getBoolean("hud.enabled", true)) {
            return;
        }

        String format = plugin.getConfig().getString(
                "hud.format",
                "<#A8A8A8>Combat: <#FF003C>{time}"
        );

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!combatManager.isInCombat(player)) {
                continue;
            }

            int seconds = combatManager.getRemainingSeconds(player);

            if (seconds <= 0) {
                continue;
            }

            String message = format.replace("{time}", String.valueOf(seconds));
            Component actionBar = miniMessage.deserialize(message);

            player.sendActionBar(actionBar);
        }
    }
}