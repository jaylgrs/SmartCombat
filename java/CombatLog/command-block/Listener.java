package CombatLog;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.List;
import java.util.Locale;

public final class Listener implements org.bukkit.event.Listener {

    private final SmartCombat plugin;
    private final CombatManager combatManager;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public Listener(
            SmartCombat plugin,
            CombatManager combatManager
    ) {
        this.plugin = plugin;
        this.combatManager = combatManager;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        if (!plugin.getConfig().getBoolean("blocked-commands.enabled", true)) {
            return;
        }

        Player player = event.getPlayer();

        if (!combatManager.isInCombat(player)) {
            return;
        }

        String commandLine = event.getMessage().substring(1).trim();

        if (commandLine.isEmpty()) {
            return;
        }

        List<String> blockedCommands = plugin.getConfig().getStringList(
                "blocked-commands.commands"
        );

        String normalizedCommand = normalizeCommand(commandLine);

        for (String blockedCommand : blockedCommands) {
            if (!matchesCommand(normalizedCommand, blockedCommand)) {
                continue;
            }

            event.setCancelled(true);

            String message = plugin.getConfig().getString(
                    "blocked-commands.message",
                    "<#FF003C>You cannot do this in combat."
            );

            player.sendMessage(miniMessage.deserialize(message));
            return;
        }
    }

    private String normalizeCommand(String commandLine) {
        String[] parts = commandLine.trim().split("\\s+", 2);
        String command = parts[0].toLowerCase(Locale.ROOT);

        if (command.startsWith("minecraft:")) {
            command = command.substring("minecraft:".length());
        } else if (command.startsWith("bukkit:")) {
            command = command.substring("bukkit:".length());
        }

        if (parts.length == 1) {
            return command;
        }

        return command + " " + parts[1].trim().toLowerCase(Locale.ROOT);
    }

    private boolean matchesCommand(
            String commandLine,
            String blockedCommand
    ) {
        String normalizedBlocked = normalizeCommand(blockedCommand);

        return commandLine.equals(normalizedBlocked)
                || commandLine.startsWith(normalizedBlocked + " ");
    }
}