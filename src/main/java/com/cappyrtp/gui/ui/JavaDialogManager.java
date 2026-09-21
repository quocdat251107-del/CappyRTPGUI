package com.cappyrtp.gui.ui;

import com.cappyrtp.gui.CappyRTPGUI;
import com.cappyrtp.gui.config.ConfigManager;
import com.cappyrtp.gui.config.Destination;
import com.cappyrtp.gui.hook.BetterRTPHook;
import com.cappyrtp.gui.util.MessageUtil;

import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.body.ItemDialogBody;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.input.SingleOptionDialogInput;
import io.papermc.paper.registry.data.dialog.type.DialogType;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds and displays Paper Dialog API dialogs for Java Edition players.
 * Renders a centered 3D rotating item, world selector (multiple-choice), and information card.
 */
public class JavaDialogManager {

    private static final String WORLD_SELECTION_KEY = "rtp_world_selection";

    private final CappyRTPGUI plugin;

    public JavaDialogManager(CappyRTPGUI plugin) {
        this.plugin = plugin;
    }

    /**
     * Shows the RTP world selection dialog to a Java Edition player.
     */
    public void showDialog(Player player, String selectedDestId) {
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

        if (selectedDestId == null || selectedDestId.isEmpty()) {
            selectedDestId = config.getDefaultDestination();
        }

        Destination defaultDest = config.getDestination(selectedDestId);

        // Build the 3D item body (Use Ender Pearl as the global teleport icon)
        ItemDialogBody itemBody = DialogBody.item(new ItemStack(Material.ENDER_PEARL))
                .showDecorations(false)
                .showTooltip(false)
                .build();

        Component descComponent = MessageUtil.parse(config.getDialogDescription());

        List<SingleOptionDialogInput.OptionEntry> optionEntries = new ArrayList<>();

        for (Destination dest : accessibleDests) {
            Component optionLabel = MessageUtil.parse(dest.displayName());
            boolean isDefault = dest.id().equals(selectedDestId);
            optionEntries.add(SingleOptionDialogInput.OptionEntry.create(
                    dest.id(), optionLabel, isDefault
            ));
        }

        // The multiple-choice world selection bar
        SingleOptionDialogInput worldSelector = DialogInput.singleOption(
                WORLD_SELECTION_KEY,
                Component.empty(),
                optionEntries
        ).labelVisible(false).build();

        // Information Card (Lower Dialog Body)
        // Uses the currently 'selected' (default) destination's specs.
        Component infoBuilder = Component.empty();
        if (defaultDest != null) {
            infoBuilder = infoBuilder
                    .append(MessageUtil.parse(defaultDest.lore())).append(Component.newline())
                    .append(Component.newline())
                    .append(MessageUtil.parse(defaultDest.specs())).append(Component.newline())
                    .append(Component.newline())
                    .append(MessageUtil.parse(defaultDest.tip()));
        }
        final Component infoCardComponent = infoBuilder;

        ActionButton confirmBtn = ActionButton.create(
                MessageUtil.parse(config.getConfirmButton()),
                null,
                120,
                DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) {
                        String finalSelectedId = view.getText(WORLD_SELECTION_KEY);
                        handleConfirm(p, finalSelectedId);
                    }
                }, net.kyori.adventure.text.event.ClickCallback.Options.builder().build())
        );

        ActionButton cancelBtn = ActionButton.create(
                MessageUtil.parse(config.getCancelButton()),
                null,
                120,
                null
        );

        Component titleComponent = MessageUtil.parse(config.getDialogTitle());

        Dialog dialog = Dialog.create(factory -> factory.empty()
                .base(DialogBase.builder(titleComponent)
                        // Structure: Centered 3D Icon -> Description -> Info Card
                        .body(List.of(
                                itemBody, 
                                DialogBody.plainMessage(descComponent),
                                DialogBody.plainMessage(infoCardComponent)
                        ))
                        .inputs(List.of(worldSelector))
                        .afterAction(DialogBase.DialogAfterAction.CLOSE)
                        .canCloseWithEscape(true)
                        .build())
                .type(DialogType.confirmation(confirmBtn, cancelBtn))
        );

        player.showDialog(dialog);
    }

    private void handleConfirm(Player player, String selectedDestId) {
        ConfigManager config = plugin.getConfigManager();
        BetterRTPHook rtpHook = plugin.getBetterRTPHook();

        if (selectedDestId == null || selectedDestId.isEmpty()) {
            selectedDestId = config.getDefaultDestination();
        }

        Destination dest = config.getDestination(selectedDestId);
        if (dest == null) {
            MessageUtil.send(player, config.getMsgNotFound());
            return;
        }

        if (!player.hasPermission(dest.permission())) {
            MessageUtil.send(player, config.getMsgNoPermission());
            return;
        }

        boolean success = rtpHook.teleport(player, dest.worldName());
        if (success) {
            MessageUtil.send(player, config.getMsgTeleporting());
        } else {
            MessageUtil.send(player, config.getMsgNotFound());
        }
    }
}
