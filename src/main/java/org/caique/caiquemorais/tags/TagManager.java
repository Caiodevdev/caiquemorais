package org.caique.caiquemorais.tags;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.caique.caiquemorais.Caiquemorais;
import org.caique.caiquemorais.database.PlayerData;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class TagManager {
    private final Caiquemorais plugin;
    private final Map<String, String> availableTags;
    private final Scoreboard scoreboard;

    public TagManager(Caiquemorais plugin) {
        this.plugin = plugin;
        this.availableTags = new HashMap<>();
        this.scoreboard = plugin.getServer().getScoreboardManager().getMainScoreboard();

        availableTags.put("AJD", ChatColor.LIGHT_PURPLE.toString());
        availableTags.put("MOD", ChatColor.DARK_GREEN.toString());
        availableTags.put("COORD", ChatColor.AQUA.toString());
        availableTags.put("GER", ChatColor.DARK_RED.toString());
        availableTags.put("ADM", ChatColor.DARK_BLUE.toString());
    }

    public void setPlayerTag(Player player, String tag) throws SQLException {
        if (!availableTags.containsKey(tag)) {
            plugin.getLogger().warning("Tentativa de setar tag inválida: " + tag);
            return;
        }

        PlayerData playerData = new PlayerData(plugin.getDatabaseManager());
        playerData.setTag(player, tag);
        plugin.getLogger().info("Tag " + tag + " setada para " + player.getName());
        updatePlayerDisplay(player);
    }

    public String getPlayerTag(Player player) {
        try {
            PlayerData playerData = new PlayerData(plugin.getDatabaseManager());
            String tag = playerData.getTag(player);
            return tag.isEmpty() ? "" : availableTags.get(tag) + "[" + tag + "]" + ChatColor.RESET;
        } catch (SQLException e) {
            plugin.getLogger().severe("Erro ao pegar tag: " + e.getMessage());
            return "";
        }
    }

    public Map<String, String> getAvailableTags() {
        return availableTags;
    }

    private void updatePlayerDisplay(Player player) {
        try {
            PlayerData playerData = new PlayerData(plugin.getDatabaseManager());
            String tag = playerData.getTag(player);
            if (tag.isEmpty()) return;

            String teamName = "tag_" + tag;
            Team team = scoreboard.getTeam(teamName);

            if (team == null) {
                team = scoreboard.registerNewTeam(teamName);
                team.setPrefix(availableTags.get(tag) + "[" + tag + "] ");
            }

            // Remove o jogador de qualquer time anterior
            for (Team existingTeam : scoreboard.getTeams()) {
                if (existingTeam.hasEntry(player.getName()) && !existingTeam.getName().equals(teamName)) {
                    existingTeam.removeEntry(player.getName());
                }
            }

            team.addEntry(player.getName());
            player.setDisplayName(team.getPrefix() + player.getName() + ChatColor.RESET);
            player.setPlayerListName(team.getPrefix() + player.getName() + ChatColor.RESET);

            for (Player online : plugin.getServer().getOnlinePlayers()) {
                online.setScoreboard(scoreboard);
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Erro ao atualizar display: " + e.getMessage());
        }
    }

    public void loadPlayerTag(Player player) {
        updatePlayerDisplay(player);
    }
}