package org.caique.caiquemorais.scoreboard;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;
import org.caique.caiquemorais.Caiquemorais;
import org.caique.caiquemorais.lobby.LobbyManager;
import org.caique.caiquemorais.tags.TagManager;

public class ScoreboardManager {
    private final Caiquemorais plugin;
    private final TagManager tagManager;
    private final LobbyManager lobbyManager;

    public ScoreboardManager(Caiquemorais plugin, LobbyManager lobbyManager) {
        this.plugin = plugin;
        this.tagManager = plugin.getTagManager();
        this.lobbyManager = lobbyManager;
        startUpdateTask();
    }

    public void showScoreboard(Player player) {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective objective = scoreboard.registerNewObjective("mcgames_" + player.getUniqueId(), "dummy",
                ChatColor.AQUA + "" + ChatColor.BOLD + "MCGAMES");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        // Espaço entre o título e o Rank
        Score spacerTitle = objective.getScore(ChatColor.BLACK + "");
        spacerTitle.setScore(5);

        // Linha do Rank (usando a tag do jogador)
        String playerTag = tagManager.getPlayerTag(player);
        if (playerTag == null || playerTag.isEmpty()) {
            playerTag = ChatColor.GRAY + "Nenhuma";
        }
        Score rank = objective.getScore(ChatColor.WHITE + "Rank: " + playerTag);
        rank.setScore(4);

        // Espaço entre Rank e Lobby
        Score spacer1 = objective.getScore(ChatColor.RED + "");
        spacer1.setScore(3);

        // Linha do Lobby (usando o lobby atual do jogador)
        int lobbyId = lobbyManager.getPlayerLobby(player);
        String lobbyDisplay = lobbyId == -1 ? "Nenhum" : "#" + lobbyId;
        Score lobby = objective.getScore(ChatColor.WHITE + "Lobby: " + ChatColor.GREEN + lobbyDisplay);
        lobby.setScore(2);

        // Linha de Jogadores (sem espaço entre Lobby e Jogadores)
        Score players = objective.getScore(ChatColor.WHITE + "Jogadores: " + ChatColor.AQUA + Bukkit.getOnlinePlayers().size());
        players.setScore(1);

        // Espaço entre Jogadores e Website
        Score spacer3 = objective.getScore(ChatColor.YELLOW + "");
        spacer3.setScore(0);

        // Linha do Website
        Score website = objective.getScore(ChatColor.GREEN + "www.mcgames.com.br");
        website.setScore(-1);

        // Aplicar a scoreboard ao jogador
        player.setScoreboard(scoreboard);
    }

    private void startUpdateTask() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player online : Bukkit.getOnlinePlayers()) {
                showScoreboard(online);
            }
        }, 0L, 20L);
    }
}