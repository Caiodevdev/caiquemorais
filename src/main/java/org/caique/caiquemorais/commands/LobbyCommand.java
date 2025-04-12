package org.caique.caiquemorais.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.caique.caiquemorais.Caiquemorais;
import org.caique.caiquemorais.lobby.LobbyManager;
import org.caique.caiquemorais.utils.MessageUtils;

public class LobbyCommand implements CommandExecutor {
    private final Caiquemorais plugin;
    private final LobbyManager lobbyManager;

    public LobbyCommand(Caiquemorais plugin, LobbyManager lobbyManager) {
        this.plugin = plugin;
        this.lobbyManager = lobbyManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Apenas jogadores podem usar este comando!");
            return true;
        }

        Player player = (Player) sender;
        if (args.length != 1) {
            MessageUtils.sendMessage(player, "&cUso: /lobby <número>");
            return true;
        }

        try {
            int lobbyId = Integer.parseInt(args[0]);
            lobbyManager.movePlayerToLobby(player, lobbyId);
            plugin.getScoreboardManager().showScoreboard(player); // Atualizar a scoreboard
        } catch (NumberFormatException e) {
            MessageUtils.sendMessage(player, "&cPor favor, insira um número válido!");
        }

        return true;
    }
}