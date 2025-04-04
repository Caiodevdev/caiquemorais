package org.caique.caiquemorais.commands;

import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.caique.caiquemorais.Caiquemorais;
import org.caique.caiquemorais.database.PlayerData;
import org.caique.caiquemorais.utils.MessageUtils;

import java.sql.SQLException;

public class LoginCommand implements CommandExecutor {
    private final Caiquemorais plugin;

    public LoginCommand(Caiquemorais plugin) {
        this.plugin = plugin;
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
            if (!playerData.isRegistered(player)) {
                MessageUtils.sendTitle(player, "&cErro", "&fRegistre-se primeiro!", 10, 40, 10);
                MessageUtils.sendMessage(player, "&cUse /register <senha> <confirmação>");
                MessageUtils.playSound(player, Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                return true;
            }

            if (playerData.isLoggedIn(player)) {
                MessageUtils.sendTitle(player, "&cErro", "&fVocê já está logado!", 10, 40, 10);
                MessageUtils.playSound(player, Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                return true;
            }

            if (args.length != 1) {
                MessageUtils.sendMessage(player, "&cUso: /login <senha>");
                MessageUtils.playSound(player, Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                return true;
            }

            if (playerData.checkPassword(player, args[0])) {
                playerData.setLoggedIn(player, true);
                MessageUtils.sendTitle(player, "&aLogado!", "&fBem-vindo, " + player.getName() + "!", 10, 60, 10);
                MessageUtils.sendMessage(player, "&aVocê foi logado com sucesso!");
                MessageUtils.playSound(player, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
            } else {
                MessageUtils.sendTitle(player, "&cErro", "&fSenha incorreta!", 10, 40, 10);
                MessageUtils.playSound(player, Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            }
        } catch (SQLException e) {
            MessageUtils.sendMessage(player, "&cErro ao logar: " + e.getMessage());
            plugin.getLogger().severe("Erro ao logar jogador: " + e.getMessage());
        }
        return true;
    }
}