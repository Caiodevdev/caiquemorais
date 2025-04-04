package org.caique.caiquemorais.commands;

import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.caique.caiquemorais.Caiquemorais;
import org.caique.caiquemorais.chat.ChatManager;
import org.caique.caiquemorais.database.PlayerData;
import org.caique.caiquemorais.utils.MessageUtils;

import java.sql.SQLException;

public class GlobalChatCommand implements CommandExecutor {
    private final Caiquemorais plugin;
    private final ChatManager chatManager;

    public GlobalChatCommand(Caiquemorais plugin, ChatManager chatManager) {
        this.plugin = plugin;
        this.chatManager = chatManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Apenas jogadores podem usar este comando!");
            return true;
        }

        Player player = (Player) sender;
        PlayerData playerData = new PlayerData(plugin.getDatabaseManager());

        try {
            if (!playerData.isLoggedIn(player)) {
                MessageUtils.sendMessage(player, "&cVocê precisa estar logado para falar no chat global!");
                MessageUtils.playSound(player, Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                return true;
            }

            if (args.length == 0) {
                MessageUtils.sendMessage(player, "&cUso: /g <mensagem>");
                MessageUtils.playSound(player, Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                return true;
            }

            String message = String.join(" ", args);
            chatManager.sendGlobalMessage(player, message);
        } catch (SQLException e) {
            MessageUtils.sendMessage(player, "&cErro ao enviar mensagem: " + e.getMessage());
            plugin.getLogger().severe("Erro no comando /g: " + e.getMessage());
        }
        return true;
    }
}