package org.caique.caiquemorais.chat;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.caique.caiquemorais.Caiquemorais;
import org.caique.caiquemorais.database.PlayerData;
import org.caique.caiquemorais.utils.MessageUtils;

import java.sql.SQLException;

public class ChatListener implements Listener {
    private final Caiquemorais plugin;
    private final ChatManager chatManager;

    public ChatListener(Caiquemorais plugin, ChatManager chatManager) {
        this.plugin = plugin;
        this.chatManager = chatManager;
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        event.setCancelled(true);

        Player player = event.getPlayer();
        String message = event.getMessage();

        try {
            PlayerData playerData = new PlayerData(plugin.getDatabaseManager());
            if (!playerData.isLoggedIn(player)) {
                MessageUtils.sendMessage(player, "&cVocê precisa estar logado para falar no chat!");
                MessageUtils.playSound(player, Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                return;
            }

            chatManager.sendLocalMessage(player, message);
        } catch (SQLException e) {
            MessageUtils.sendMessage(player, "&cErro ao processar o chat: " + e.getMessage());
            plugin.getLogger().severe("Erro no chat: " + e.getMessage());
        }
    }
}