package com.cappyrtp.gui.command;

import com.cappyrtp.gui.CappyRTPGUI;
import com.cappyrtp.gui.util.MessageUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles the /rtp command with client-adaptive routing.
 * Routes Bedrock players to SimpleForm, Java players to Paper Dialog,
 * and older Java clients to a Chest GUI fallback via ViaVersion.
 */
public class RTPCommand implements CommandExecutor, TabCompleter {

    private final CappyRTPGUI plugin;

    public RTPCommand(CappyRTPGUI plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {

        // Handle reload subcommand
        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("cappyrtp.reload")) {
                if (sender instanceof Player player) {
                    MessageUtil.send(player, plugin.getConfigManager().getMsgNoPermission());
                } else {
                    sender.sendMessage("No permission!");
                }
                return true;
            }
            plugin.getConfigManager().loadAll();
            if (sender instanceof Player player) {
                MessageUtil.send(player, "<green>CappyRTPGUI config reloaded!</green>");
            } else {
                sender.sendMessage("CappyRTPGUI config reloaded!");
            }
            return true;
        }

        // Player-only command
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be used by players!");
            return true;
        }

        // Check base permission
        if (!player.hasPermission("cappyrtp.use")) {
            MessageUtil.send(player, plugin.getConfigManager().getMsgNoPermission());
            return true;
        }

        // Check BetterRTP availability
        if (!plugin.getBetterRTPHook().isAvailable()) {
            MessageUtil.send(player, plugin.getConfigManager().getMsgNotFound());
            return true;
        }

        // Route based on client type
        if (plugin.getFloodgateHook().isAvailable() && plugin.getFloodgateHook().isBedrockPlayer(player)) {
            // Bedrock player -> Floodgate SimpleForm
            plugin.getBedrockFormManager().showForm(player);
        } else if (plugin.getJavaDialogManager() == null || !plugin.getViaVersionHook().supportsDialogAPI(player)) {
            // Older Java Server OR Older Java Client -> Chest GUI Fallback
            plugin.getChestGUIManager().showGUI(player);
        } else {
            // Native Java client (1.21.3+) on modern server -> Paper Dialog
            plugin.getJavaDialogManager().showDialog(player, null);
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                      @NotNull String label, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1 && sender.hasPermission("cappyrtp.reload")) {
            if ("reload".startsWith(args[0].toLowerCase())) {
                completions.add("reload");
            }
        }
        return completions;
    }
}
