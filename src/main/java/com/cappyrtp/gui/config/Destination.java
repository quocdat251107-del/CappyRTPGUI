package com.cappyrtp.gui.config;

import org.bukkit.Material;

/**
 * Represents a configured RTP destination world.
 */
public record Destination(
        String id,
        String displayName,
        String worldName,
        Material javaIcon,
        int guiSlot,
        String permission
) {
}
