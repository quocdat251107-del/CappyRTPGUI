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
import io.papermc.paper.registry.data.dialog.type.DialogType;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class JavaDialogManager {

    private final CappyRTPGUI plugin;

    public JavaDialogManager(CappyRTPGUI plugin) {
        this.plugin = plugin;
    }

    /**
     * Step 1: Shows the world selection menu as a MultiAction dialog.
     */
    public void showDialog(Player player, String ignored) {
        ConfigManager config = plugin.getConfigManager();

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

        Component titleComponent = MessageUtil.parse(config.getDialogTitle());
        Component descComponent = MessageUtil.parse(config.getDialogDescription());

        List<ActionButton> worldButtons = new ArrayList<>();
        for (Destination dest : accessibleDests) {
            String destId = dest.id();
            worldButtons.add(ActionButton.create(
                    MessageUtil.parse(dest.displayName()),
                    null,
                    120,
                    DialogAction.customClick((view, audience) -> {
                        if (audience instanceof Player p) {
                            showConfirmationDialog(p, destId);
                        }
                    }, net.kyori.adventure.text.event.ClickCallback.Options.builder().build())
            ));
        }

        ActionButton cancelBtn = ActionButton.create(
                MessageUtil.parse(config.getCancelButton()),
                null,
                120,
                null
        );

        Dialog dialog = Dialog.create(factory -> factory.empty()
                .base(DialogBase.builder(titleComponent)
                        .body(List.of(DialogBody.plainMessage(descComponent)))
                        .afterAction(DialogBase.DialogAfterAction.CLOSE)
                        .canCloseWithEscape(true)
                        .build())
                .type(DialogType.multiAction(worldButtons, cancelBtn, 1)) // 1 column to stack them vertically
        );

        player.showDialog(dialog);
    }

    /**
     * Step 2: Shows the 3D Item and Information card for the selected world.
     */
    private void showConfirmationDialog(Player player, String destId) {
        ConfigManager config = plugin.getConfigManager();
        Destination dest = config.getDestination(destId);
        
        if (dest == null) return;

        Material iconMaterial = dest.javaIcon();
        if (iconMaterial == null) iconMaterial = Material.GRASS_BLOCK;

        ItemDialogBody itemBody = DialogBody.item(new ItemStack(iconMaterial))
                .showDecorations(false)
                .showTooltip(false)
                .build();

        Component infoBuilder = Component.empty();
        infoBuilder = infoBuilder
                .append(MessageUtil.parse(dest.lore())).append(Component.newline())
                .append(Component.newline())
                .append(MessageUtil.parse(dest.specs())).append(Component.newline())
                .append(Component.newline())
                .append(MessageUtil.parse(dest.tip()));
        
        final Component infoCardComponent = infoBuilder;

        ActionButton confirmBtn = ActionButton.create(
                MessageUtil.parse(config.getConfirmButton()),
                null,
                120,
                DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) {
                        handleConfirm(p, destId);
                    }
                }, net.kyori.adventure.text.event.ClickCallback.Options.builder().build())
        );

        ActionButton backBtn = ActionButton.create(
                MessageUtil.parse(config.getCancelButton()), // Reusing cancel label for 'Back/Cancel'
                null,
                120,
                DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) {
                        showDialog(p, null); // Go back to selection
                    }
                }, net.kyori.adventure.text.event.ClickCallback.Options.builder().build())
        );

        Component titleComponent = MessageUtil.parse(config.getDialogTitle() + " - " + dest.displayName());

        Dialog dialog = Dialog.create(factory -> factory.empty()
                .base(DialogBase.builder(titleComponent)
                        .body(List.of(
                                itemBody,
                                DialogBody.plainMessage(infoCardComponent)
                        ))
                        .afterAction(DialogBase.DialogAfterAction.CLOSE)
                        .canCloseWithEscape(true)
                        .build())
                .type(DialogType.confirmation(confirmBtn, backBtn))
        );

        player.showDialog(dialog);
    }

    private void handleConfirm(Player player, String selectedDestId) {
        ConfigManager config = plugin.getConfigManager();
        BetterRTPHook rtpHook = plugin.getBetterRTPHook();

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
