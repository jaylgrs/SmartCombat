package command;

import CombatLog.SmartCombat;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public final class CommandListener implements CommandExecutor, TabCompleter {

    private final SmartCombat plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public CommandListener(SmartCombat plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String label,
            @NotNull String[] args
    ) {
        if (command.getName().equalsIgnoreCase("spamkill")) {
            handleSpamKill(sender, args);
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender, label);
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            reload(sender);
            return true;
        }

        sendMessage(
                sender,
                "commands.reload.unknown",
                "<#FF003C>Unknown subcommand."
        );

        sendHelp(sender, label);
        return true;
    }

    @Override
    public List<String> onTabComplete(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String alias,
            @NotNull String[] args
    ) {
        if (command.getName().equalsIgnoreCase("spamkill")) {
            if (!(sender instanceof Player)
                    || !sender.hasPermission("smartcombat.spamkill")) {
                return List.of();
            }

            if (args.length == 1) {
                return filterSuggestions(List.of("protect"), args[0]);
            }

            if (args.length == 2
                    && args[0].equalsIgnoreCase("protect")) {
                return filterSuggestions(
                        List.of("disable", "status"),
                        args[1]
                );
            }

            return List.of();
        }

        if (args.length == 1) {
            List<String> suggestions = new ArrayList<>();

            if (sender.hasPermission("smartcombat.reload")) {
                suggestions.add("reload");
            }

            return filterSuggestions(suggestions, args[0]);
        }

        return List.of();
    }

    private void handleSpamKill(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sendMessage(
                    sender,
                    "spam-kill.player-only",
                    "<#FF003C>This command can only be used by players."
            );
            return;
        }

        if (!player.hasPermission("smartcombat.spamkill")) {
            sendMessage(
                    player,
                    "spam-kill.no-permission",
                    "<#FF003C>You don't have permission to use this command."
            );
            return;
        }

        if (args.length == 0
                || !args[0].equalsIgnoreCase("protect")) {
            sendMessage(
                    player,
                    "spam-kill.usage",
                    "<#A8A8A8>Usage: /spamkill protect [disable|status]"
            );
            return;
        }

        if (args.length == 1
                || args[1].equalsIgnoreCase("status")) {
            showProtectionStatus(player);
            return;
        }

        if (args.length == 2
                && args[1].equalsIgnoreCase("disable")) {

            AntiSpamKill.Manager manager = plugin.getSpamKillManager();

            if (!manager.isProtectionEnabled(player)) {
                sendMessage(
                        player,
                        "spam-kill.already-disabled",
                        "<#FFB347>Your spam-kill protection is already disabled "
                                + "and cannot be enabled again this session."
                );
                return;
            }

            manager.disableProtection(player);

            sendMessage(
                    player,
                    "spam-kill.protection-disabled",
                    "<#FFB347>Your spam-kill protection has been disabled. "
                            + "You cannot enable it again this session."
            );
            return;
        }

        sendMessage(
                player,
                "spam-kill.usage",
                "<#A8A8A8>Usage: /spamkill protect [disable|status]"
        );
    }

    private void showProtectionStatus(Player player) {
        boolean enabled = plugin.getSpamKillManager()
                .isProtectionEnabled(player);

        if (enabled) {
            sendMessage(
                    player,
                    "spam-kill.protection-status-enabled",
                    "<#00FF8C>Your spam-kill protection is enabled."
            );
        } else {
            sendMessage(
                    player,
                    "spam-kill.protection-status-disabled",
                    "<#FFB347>Your spam-kill protection is disabled "
                            + "and cannot be enabled again this session."
            );
        }
    }

    private void reload(CommandSender sender) {
        if (!sender.hasPermission("smartcombat.reload")) {
            sendMessage(
                    sender,
                    "commands.reload.no-permission",
                    "<#FF003C>You don't have permission to use this command."
            );
            return;
        }

        plugin.reloadPluginConfigs();

        sendMessage(
                sender,
                "commands.reload.success",
                "<#00FF8C>SmartCombat configurations reloaded successfully."
        );
    }

    private void sendHelp(CommandSender sender, String label) {
        sendMessage(
                sender,
                "commands.help.header",
                "<#00FF8C>SmartCombat commands:"
        );

        if (sender.hasPermission("smartcombat.reload")) {
            sendMessage(
                    sender,
                    "commands.help.reload",
                    "<#A8A8A8>/" + label
                            + " reload <#666666>- Reload configuration files"
            );

            sendMessage(
                    sender,
                    "commands.help.alias",
                    "<#A8A8A8>Alias: /sc reload"
            );
        }

        if (sender instanceof Player
                && sender.hasPermission("smartcombat.spamkill")) {
            sendMessage(
                    sender,
                    "spam-kill.help",
                    "<#A8A8A8>/spamkill protect [disable|status]"
            );
        }
    }

    private List<String> filterSuggestions(
            List<String> suggestions,
            String input
    ) {
        String lowerInput = input.toLowerCase();

        return suggestions.stream()
                .filter(suggestion -> suggestion.startsWith(lowerInput))
                .toList();
    }

    private void sendMessage(
            CommandSender sender,
            String path,
            String defaultMessage
    ) {
        String message = plugin.getLangConfig().getString(path, defaultMessage);

        sender.sendMessage(miniMessage.deserialize(message));
    }
}