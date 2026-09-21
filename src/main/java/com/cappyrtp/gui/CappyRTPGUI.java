package com.cappyrtp.gui;

import com.cappyrtp.gui.command.RTPCommand;
import com.cappyrtp.gui.config.ConfigManager;
import com.cappyrtp.gui.hook.BetterRTPHook;
import com.cappyrtp.gui.hook.FloodgateHook;
import com.cappyrtp.gui.hook.ViaVersionHook;
import com.cappyrtp.gui.ui.BedrockFormManager;
import com.cappyrtp.gui.ui.ChestGUIManager;
import com.cappyrtp.gui.ui.JavaDialogManager;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class CappyRTPGUI extends JavaPlugin {

    private static CappyRTPGUI instance;

    private ConfigManager configManager;
    private BetterRTPHook betterRTPHook;
    private FloodgateHook floodgateHook;
    private ViaVersionHook viaVersionHook;
    
    private JavaDialogManager javaDialogManager;
    private BedrockFormManager bedrockFormManager;
    private ChestGUIManager chestGUIManager;

    @Override
    public void onEnable() {
        instance = this;

        // Initialize configuration
        configManager = new ConfigManager(this);
        configManager.loadAll();

        // Initialize hooks
        betterRTPHook = new BetterRTPHook(getLogger());
        betterRTPHook.init();

        // Initialize bStats Metrics
        int pluginId = 34192;
        new org.bstats.bukkit.Metrics(this, pluginId);

        floodgateHook = new FloodgateHook(getLogger());
        floodgateHook.init();

        viaVersionHook = new ViaVersionHook(getLogger());
        viaVersionHook.init();

        // Initialize UI managers
        javaDialogManager = new JavaDialogManager(this);
        bedrockFormManager = new BedrockFormManager(this);
        chestGUIManager = new ChestGUIManager(this); // Registers listeners

        // Register command
        RTPCommand rtpCommand = new RTPCommand(this);
        PluginCommand cmd = getCommand("rtp");
        if (cmd != null) {
            cmd.setExecutor(rtpCommand);
            cmd.setTabCompleter(rtpCommand);
        } else {
            getLogger().severe("Failed to register /rtp command! Is it defined in plugin.yml?");
        }

        getLogger().info("CappyRTPGUI v" + getPluginMeta().getVersion() + " enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("CappyRTPGUI disabled.");
        instance = null;
    }

    public static CappyRTPGUI getInstance() { return instance; }
    public ConfigManager getConfigManager() { return configManager; }
    public BetterRTPHook getBetterRTPHook() { return betterRTPHook; }
    public FloodgateHook getFloodgateHook() { return floodgateHook; }
    public ViaVersionHook getViaVersionHook() { return viaVersionHook; }
    public JavaDialogManager getJavaDialogManager() { return javaDialogManager; }
    public BedrockFormManager getBedrockFormManager() { return bedrockFormManager; }
    public ChestGUIManager getChestGUIManager() { return chestGUIManager; }
}
