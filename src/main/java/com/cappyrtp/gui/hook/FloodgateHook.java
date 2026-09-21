package com.cappyrtp.gui.hook;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.logging.Logger;

/**
 * Handles Floodgate integration for detecting Bedrock/PE players.
 * Floodgate is a soft dependency — all calls are guarded against ClassNotFoundError.
 */
public class FloodgateHook {

    private final Logger logger;
    private boolean available;

    public FloodgateHook(Logger logger) {
        this.logger = logger;
        this.available = false;
    }

    /**
     * Initializes the hook. Call during onEnable after soft dependencies have loaded.
     */
    public void init() {
        if (Bukkit.getPluginManager().isPluginEnabled("floodgate")) {
            try {
                Class.forName("org.geysermc.floodgate.api.FloodgateApi");
                available = true;
                logger.info("Floodgate detected — Bedrock form support enabled.");
            } catch (ClassNotFoundException e) {
                available = false;
                logger.warning("Floodgate plugin found but API class not available.");
            }
        } else {
            available = false;
            logger.info("Floodgate not detected — Bedrock form support disabled.");
        }
    }

    /**
     * @return true if Floodgate is loaded and available
     */
    public boolean isAvailable() {
        return available;
    }

    /**
     * Checks if the given player is connecting via Bedrock/PE through Floodgate.
     */
    public boolean isBedrockPlayer(Player player) {
        return isBedrockPlayer(player.getUniqueId());
    }

    /**
     * Checks if the given UUID belongs to a Bedrock/PE player.
     */
    public boolean isBedrockPlayer(UUID uuid) {
        if (!available) return false;
        try {
            return org.geysermc.floodgate.api.FloodgateApi.getInstance().isFloodgatePlayer(uuid);
        } catch (Exception e) {
            logger.warning("Error checking Floodgate player status: " + e.getMessage());
            return false;
        }
    }
}
