package com.cappyrtp.gui.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;

/**
 * Utility class for MiniMessage parsing and message sending.
 */
public final class MessageUtil {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    private MessageUtil() {
    }

    /**
     * Parses a MiniMessage string into an Adventure Component.
     */
    public static Component parse(String miniMessageString) {
        if (miniMessageString == null || miniMessageString.isEmpty()) {
            return Component.empty();
        }
        return MINI_MESSAGE.deserialize(miniMessageString);
    }

    /**
     * Sends a parsed MiniMessage to a player.
     */
    public static void send(Player player, String miniMessageString) {
        if (miniMessageString == null || miniMessageString.isEmpty()) return;
        player.sendMessage(parse(miniMessageString));
    }

    /**
     * Converts a Component to plain text (for Bedrock forms which don't support MiniMessage).
     */
    public static String toPlainText(String miniMessageString) {
        if (miniMessageString == null || miniMessageString.isEmpty()) return "";
        Component component = MINI_MESSAGE.deserialize(miniMessageString);
        return PlainTextComponentSerializer.plainText().serialize(component);
    }

    /**
     * Formats a price string using the configured format.
     *
     * @param price       the numeric price from BetterRTP
     * @param freeFormat  the MiniMessage format for free (price == 0)
     * @param priceFormat the MiniMessage format with %price% placeholder
     * @return the formatted MiniMessage string (not yet parsed)
     */
    public static String formatPrice(double price, String freeFormat, String priceFormat) {
        if (price <= 0) {
            return freeFormat;
        }
        // Format to remove trailing zeroes (500.0 -> 500)
        String priceStr = (price == Math.floor(price)) ? String.valueOf((long) price) : String.valueOf(price);
        return priceFormat.replace("%price%", priceStr);
    }

    /**
     * Formats a cooldown string using the configured format.
     *
     * @param cooldownSeconds remaining seconds (0 or negative = ready)
     * @param readyFormat     the MiniMessage format for ready
     * @param waitFormat      the MiniMessage format with %time% placeholder
     * @return the formatted MiniMessage string (not yet parsed)
     */
    public static String formatCooldown(long cooldownSeconds, String readyFormat, String waitFormat) {
        if (cooldownSeconds <= 0) {
            return readyFormat;
        }
        return waitFormat.replace("%time%", String.valueOf(cooldownSeconds));
    }
}
