package CombatLog;

import AntiSameIp.Listener;
import AntiSpamKill.Manager;
import command.CommandListener;
import CombatLog.hud.Hud;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class SmartCombat extends JavaPlugin {

    private CombatManager combatManager;
    private Manager spamKillManager;
    private Hud hud;
    private FileConfiguration langConfig;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadLangConfig();

        combatManager = new CombatManager(this);
        spamKillManager = new Manager(this);
        hud = new Hud(this, combatManager);

        getServer().getPluginManager().registerEvents(
                new CombatListener(this, combatManager),
                this
        );

        getServer().getPluginManager().registerEvents(
                new Listener(this),
                this
        );

        getServer().getPluginManager().registerEvents(
                new AntiSpamKill.Listener(this, spamKillManager),
                this
        );

        getServer().getPluginManager().registerEvents(
                new CombatLog.Listener(this, combatManager),
                this
        );

        registerCommands();

        hud.start();

        getLogger().info("SmartCombat has been enabled.");
    }

    @Override
    public void onDisable() {
        if (hud != null) {
            hud.stop();
        }

        getLogger().info("SmartCombat has been disabled.");
    }

    private void registerCommands() {
        CommandListener commandListener = new CommandListener(this);

        PluginCommand command = getCommand("smartcombat");

        if (command == null) {
            getLogger().severe(
                    "Command 'smartcombat' is missing from plugin.yml."
            );
        } else {
            command.setExecutor(commandListener);
            command.setTabCompleter(commandListener);
        }

        PluginCommand spamKillCommand = getCommand("spamkill");

        if (spamKillCommand == null) {
            getLogger().severe(
                    "Command 'spamkill' is missing from plugin.yml."
            );
        } else {
            spamKillCommand.setExecutor(commandListener);
            spamKillCommand.setTabCompleter(commandListener);
        }
    }

    private void loadLangConfig() {
        File langFile = new File(getDataFolder(), "lang/lang.yml");

        if (!langFile.exists()) {
            saveResource("lang/lang.yml", false);
        }

        langConfig = YamlConfiguration.loadConfiguration(langFile);
    }

    public FileConfiguration getLangConfig() {
        return langConfig;
    }

    public CombatManager getCombatManager() {
        return combatManager;
    }

    public Manager getSpamKillManager() {
        return spamKillManager;
    }

    public void reloadPluginConfigs() {
        reloadConfig();
        loadLangConfig();
    }
}
