package com.cappyrtp.gui.config;

import com.cappyrtp.gui.CappyRTPGUI;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.logging.Level;

/**
 * Manages loading and access for config.yml and bedrock_gui.yml.
 */
public class ConfigManager {

    private final CappyRTPGUI plugin;

    // Dialog settings
    private String dialogTitle;
    private String dialogDescription;
    private String defaultDestination;
    private String dialogLore;
    private String dialogSpecs;
    private String dialogTip;

    // Chest GUI settings
    private String chestGuiTitle;
    private int chestGuiSize;

    // Button labels
    private String confirmButton;
    private String cancelButton;

    // Messages
    private String msgNoPermission;
    private String msgTeleporting;
    private String msgNotFound;

    // Destinations
    private final LinkedHashMap<String, Destination> destinations = new LinkedHashMap<>();

    // Bedrock config
    private String bedrockTitle;
    private String bedrockDescription;
    private final LinkedHashMap<String, BedrockButtonConfig> bedrockButtons = new LinkedHashMap<>();

    public ConfigManager(CappyRTPGUI plugin) {
        this.plugin = plugin;
    }

    public void loadAll() {
        loadMainConfig();
        loadBedrockConfig();
    }

    private void loadMainConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        FileConfiguration config = plugin.getConfig();

        // Dialog section
        dialogTitle = config.getString("dialog.title", "<gradient:#55ff55:#00aa00>❖ RANDOM TELEPORT ❖</gradient>");
        dialogDescription = config.getString("dialog.description", "<gray>Select the world you wish to teleport to:</gray>");
        defaultDestination = config.getString("dialog.default-destination", "overworld");
        
        dialogLore = config.getString("dialog.card.lore", "<gray>Choose a dimension below to embark on your journey.</gray>");
        dialogSpecs = config.getString("dialog.card.specs", "<gray>Safe Landing: <green>Active</green>\n<gray>Random Radius: <yellow>World Specific</yellow></gray>");
        dialogTip = config.getString("dialog.card.tip", "<italic><dark_gray>Tip: Make sure you are well equipped before teleporting!</dark_gray></italic>");

        // Chest GUI section
        chestGuiTitle = config.getString("chest-gui.title", "<dark_gray>» <green><bold>Random Teleport</bold>");
        chestGuiSize = config.getInt("chest-gui.size", 27);

        // Buttons
        confirmButton = config.getString("buttons.confirm", "<green>✔ Confirm</green>");
        cancelButton = config.getString("buttons.cancel", "<red>✖ Cancel</red>");

        // Messages
        msgNoPermission = config.getString("messages.no-permission", "<red>You do not have permission to RTP to this world!</red>");
        msgTeleporting = config.getString("messages.teleporting", "<green>Finding a safe location and teleporting...</green>");
        msgNotFound = config.getString("messages.not-found", "<red>The specified world was not found in BetterRTP!</red>");

        // Destinations
        destinations.clear();
        ConfigurationSection destSection = config.getConfigurationSection("destinations");
        if (destSection != null) {
            for (String key : destSection.getKeys(false)) {
                ConfigurationSection entry = destSection.getConfigurationSection(key);
                if (entry == null) continue;

                String displayName = entry.getString("display-name", key);
                String worldName = entry.getString("world", entry.getString("world-name", key));
                String iconStr = entry.getString("java-icon", "GRASS_BLOCK");
                int guiSlot = entry.getInt("chest-gui.gui-slot", entry.getInt("gui-slot", 0));
                String permission = entry.getString("permission", "cappyrtp.world." + key);

                Material icon;
                try {
                    icon = Material.valueOf(iconStr.toUpperCase());
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().log(Level.WARNING, "Invalid material '" + iconStr + "' for destination '" + key + "', defaulting to GRASS_BLOCK");
                    icon = Material.GRASS_BLOCK;
                }

                destinations.put(key, new Destination(key, displayName, worldName, icon, guiSlot, permission));
            }
        }

        plugin.getLogger().info("Loaded " + destinations.size() + " destination(s) from config.yml");
    }

    private void loadBedrockConfig() {
        File bedrockFile = new File(plugin.getDataFolder(), "bedrock_gui.yml");
        if (!bedrockFile.exists()) {
            plugin.saveResource("bedrock_gui.yml", false);
        }

        FileConfiguration bedrockConfig = YamlConfiguration.loadConfiguration(bedrockFile);

        bedrockTitle = bedrockConfig.getString("title", "❖ RANDOM TELEPORT ❖");
        bedrockDescription = bedrockConfig.getString("description", "Select the world you wish to teleport to:");

        bedrockButtons.clear();
        ConfigurationSection btnSection = bedrockConfig.getConfigurationSection("buttons");
        if (btnSection != null) {
            for (String key : btnSection.getKeys(false)) {
                ConfigurationSection entry = btnSection.getConfigurationSection(key);
                if (entry == null) continue;

                bedrockButtons.put(key, new BedrockButtonConfig(
                        key,
                        entry.getString("line1", key),
                        entry.getString("icon-type", "PATH"),
                        entry.getString("icon-value", "")
                ));
            }
        }

        plugin.getLogger().info("Loaded " + bedrockButtons.size() + " Bedrock button(s) from bedrock_gui.yml");
    }

    // ---- Getters ----

    public String getDialogTitle() { return dialogTitle; }
    public String getDialogDescription() { return dialogDescription; }
    public String getDefaultDestination() { return defaultDestination; }
    public String getDialogLore() { return dialogLore; }
    public String getDialogSpecs() { return dialogSpecs; }
    public String getDialogTip() { return dialogTip; }

    public String getChestGuiTitle() { return chestGuiTitle; }
    public int getChestGuiSize() { return chestGuiSize; }

    public String getConfirmButton() { return confirmButton; }
    public String getCancelButton() { return cancelButton; }

    public String getMsgNoPermission() { return msgNoPermission; }
    public String getMsgTeleporting() { return msgTeleporting; }
    public String getMsgNotFound() { return msgNotFound; }

    public LinkedHashMap<String, Destination> getDestinations() { return destinations; }
    public Destination getDestination(String id) { return destinations.get(id); }

    public String getBedrockTitle() { return bedrockTitle; }
    public String getBedrockDescription() { return bedrockDescription; }
    public LinkedHashMap<String, BedrockButtonConfig> getBedrockButtons() { return bedrockButtons; }
    public BedrockButtonConfig getBedrockButton(String id) { return bedrockButtons.get(id); }
}
