package command;

import CombatLog.SmartCombat;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;

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
        if (args.length == 1
                && sender.hasPermission("smartcombat.reload")
                && "reload".startsWith(args[0].toLowerCase())) {
            return List.of("reload");
        }

        return List.of();
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