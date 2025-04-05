package org.caique.caiquemorais.chat;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.caique.caiquemorais.tags.TagManager;

public class ChatManager {
    private final TagManager tagManager;
    private static final int LOCAL_CHAT_RANGE = 50;

    public ChatManager(TagManager tagManager) {
        this.tagManager = tagManager;
    }

    public void sendLocalMessage(Player sender, String message) {
        String tag = tagManager.getPlayerTag(sender);
        String formattedMessage = ChatColor.GRAY + "[L] " + tag + " " + sender.getName() + ": " + ChatColor.WHITE + message;

        for (Player nearby : sender.getWorld().getPlayers()) {
            if (nearby.getLocation().distance(sender.getLocation()) <= LOCAL_CHAT_RANGE) {
                nearby.sendMessage(formattedMessage);
            }
        }
    }

    public void sendGlobalMessage(Player sender, String message) {
        String tag = tagManager.getPlayerTag(sender);
        String formattedMessage = ChatColor.YELLOW + "[G] " + tag + " " + sender.getName() + ": " + ChatColor.WHITE + message;

        for (Player online : sender.getServer().getOnlinePlayers()) {
            online.sendMessage(formattedMessage);
        }
    }
}