package com.cappyrtp.gui.hook;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import me.SuperRonanCraft.BetterRTP.BetterRTP;
import me.SuperRonanCraft.BetterRTP.player.rtp.RTPSetupInformation;

import java.util.logging.Logger;

/**
 * Isolates all BetterRTP API access into a single class.
 * Uses official API methods, no reflection.
 */
public class BetterRTPHook {

    private final Logger logger;
    private BetterRTP pluginInstance;
    private boolean available;

    public BetterRTPHook(Logger logger) {
        this.logger = logger;
        this.available = false;
    }

    /**
     * Initializes the hook. Call during onEnable.
     */
    public void init() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("BetterRTP");
        if (plugin instanceof BetterRTP rtpPlugin && plugin.isEnabled()) {
            this.pluginInstance = rtpPlugin;
            this.available = true;
            logger.info("BetterRTP detected — API hook established via native classes.");
        } else {
            this.available = false;
            logger.warning("BetterRTP not detected or not enabled — RTP functionality will be unavailable.");
        }
    }

    /**
     * @return true if BetterRTP is loaded and available
     */
    public boolean isAvailable() {
        return available && pluginInstance != null && ((Plugin) pluginInstance).isEnabled();
    }

    /**
     * Initiates an RTP teleportation for the player to the specified world.
     * Uses BetterRTP's official Java API methods.
     * Returns true ONLY if the teleport was successfully started.
     *
     * @param player    the player to teleport
     * @param worldName the target Bukkit world name
     * @return true if the teleport was initiated successfully, false if world invalid or API failed
     */
    public boolean teleport(Player player, String worldName) {
        if (!isAvailable()) return false;
        
        try {
            World world = Bukkit.getWorld(worldName);
            if (world == null) {
                logger.warning("World '" + worldName + "' does not exist on this server.");
                return false;
            }

            // Execute teleport via official BetterRTP API directly
            CommandSender sender = Bukkit.getConsoleSender();
            RTPSetupInformation setupInfo = new RTPSetupInformation(world, sender, player, true);
            
            // Do NOT use BetterRTP.getInstance(). Use the cached real plugin instance.
            pluginInstance.getRTP().start(setupInfo);

            return true;
        } catch (Exception e) {
            logger.severe("Failed to initiate BetterRTP teleport for " + player.getName() + ": " + e.getMessage());
            return false;
        }
    }
}
