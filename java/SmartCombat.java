package CombatLog;

import CombatLog.hud.Hud;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class SmartCombat extends JavaPlugin {

    private CombatManager combatManager;
    private Hud hud;
    private FileConfiguration langConfig;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadLangConfig();

        combatManager = new CombatManager(this);
        hud = new Hud(this, combatManager);

        getServer().getPluginManager().registerEvents(
                new CombatListener(this, combatManager),
                this
        );

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

    public void reloadPluginConfigs() {
        reloadConfig();
        loadLangConfig();
    }
}