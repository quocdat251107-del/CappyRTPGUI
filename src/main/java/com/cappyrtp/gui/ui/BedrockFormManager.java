package com.cappyrtp.gui.ui;

import com.cappyrtp.gui.CappyRTPGUI;
import com.cappyrtp.gui.config.BedrockButtonConfig;
import com.cappyrtp.gui.config.ConfigManager;
import com.cappyrtp.gui.config.Destination;
import com.cappyrtp.gui.hook.BetterRTPHook;
import com.cappyrtp.gui.util.MessageUtil;

import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * Builds and sends Floodgate SimpleForm dialogs for Bedrock/PE players.
 */
public class BedrockFormManager {

    private final CappyRTPGUI plugin;

    public BedrockFormManager(CappyRTPGUI plugin) {
        this.plugin = plugin;
    }

    /**
     * Shows the RTP world selection SimpleForm to a Bedrock player.
     */
    public void showForm(Player player) {
        ConfigManager config = plugin.getConfigManager();
        BetterRTPHook rtpHook = plugin.getBetterRTPHook();

        List<Destination> accessibleDests = new ArrayList<>();
        for (Destination dest : config.getDestinations().values()) {
            if (player.hasPermission(dest.permission())) {
                accessibleDests.add(dest);
            }
        }

        if (accessibleDests.isEmpty()) {
            MessageUtil.send(player, config.getMsgNoPermission());
            return;
        }

        try {
            org.geysermc.cumulus.form.SimpleForm.Builder formBuilder =
                    org.geysermc.cumulus.form.SimpleForm.builder()
                            .title(config.getBedrockTitle())
                            .content(config.getBedrockDescription());

            for (Destination dest : accessibleDests) {
                BedrockButtonConfig btnConfig = config.getBedrockButton(dest.id());

                String buttonText;
                if (btnConfig != null) {
                    buttonText = btnConfig.line1();
                } else {
                    buttonText = MessageUtil.toPlainText(dest.displayName());
                }

                if (btnConfig != null && !btnConfig.iconValue().isEmpty()) {
                    org.geysermc.cumulus.util.FormImage.Type iconType = switch (btnConfig.iconType().toUpperCase()) {
                        case "URL" -> org.geysermc.cumulus.util.FormImage.Type.URL;
                        default -> org.geysermc.cumulus.util.FormImage.Type.PATH;
                    };
                    formBuilder.button(buttonText, iconType, btnConfig.iconValue());
                } else {
                    formBuilder.button(buttonText);
                }
            }

            formBuilder.validResultHandler(response -> {
                int buttonId = response.clickedButtonId();
                if (buttonId >= 0 && buttonId < accessibleDests.size()) {
                    Destination dest = accessibleDests.get(buttonId);

                    boolean success = rtpHook.teleport(player, dest.worldName());
                    if (success) {
                        MessageUtil.send(player, config.getMsgTeleporting());
                    } else {
                        MessageUtil.send(player, config.getMsgNotFound());
                    }
                }
            });

            formBuilder.closedResultHandler(response -> { });

            org.geysermc.cumulus.form.SimpleForm form = formBuilder.build();
            org.geysermc.floodgate.api.FloodgateApi.getInstance()
                    .sendForm(player.getUniqueId(), form);

        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to show Bedrock form to " + player.getName(), e);
            MessageUtil.send(player, "<red>An error occurred while opening the RTP menu.</red>");
        }
    }
}
