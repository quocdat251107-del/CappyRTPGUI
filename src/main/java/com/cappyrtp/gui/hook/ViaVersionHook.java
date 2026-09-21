package com.cappyrtp.gui.hook;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.logging.Logger;

/**
 * Handles ViaVersion integration to determine client protocol versions.
 */
public class ViaVersionHook {

    private final Logger logger;
    private boolean available;

    public ViaVersionHook(Logger logger) {
        this.logger = logger;
        this.available = false;
    }

    public void init() {
        if (Bukkit.getPluginManager().isPluginEnabled("ViaVersion")) {
            available = true;
            logger.info("ViaVersion detected — Protocol version fallback enabled.");
        } else {
            available = false;
            logger.info("ViaVersion not detected — Fallback checking disabled.");
        }
    }

    public boolean isAvailable() {
        return available;
    }

    /**
     * Retrieves the protocol version of the player using ViaVersion.
     * If ViaVersion is unavailable, returns -1.
     */
    public int getPlayerProtocolVersion(UUID uuid) {
        if (!available) return -1;
        try {
            return com.viaversion.viaversion.api.Via.getAPI().getPlayerVersion(uuid);
        } catch (Exception e) {
            logger.warning("Error checking ViaVersion protocol: " + e.getMessage());
            return -1;
        }
    }

    /**
     * Checks if the player is running a client version that supports Paper Dialog APIs (1.21.4+).
     * Paper added native Dialog API support for 1.21.3+ clients (Protocol 768 / 769).
     * If ViaVersion is missing, assumes true (assuming native server version support).
     */
    public boolean supportsDialogAPI(Player player) {
        if (!available) return true; // Assume native support
        int version = getPlayerProtocolVersion(player.getUniqueId());
        // Protocol 768 corresponds to 1.21.2/1.21.3
        // Protocol 769 corresponds to 1.21.4
        return version >= 768 || version == -1;
    }
}

