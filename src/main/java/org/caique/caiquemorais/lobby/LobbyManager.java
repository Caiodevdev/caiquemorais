package org.caique.caiquemorais.lobby;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.caique.caiquemorais.Caiquemorais;

import java.util.HashMap;
import java.util.Map;

public class LobbyManager {
    private final Caiquemorais plugin;
    private final Map<Integer, Lobby> lobbies;
    private final Map<Player, Integer> playerLobbies;

    public LobbyManager(Caiquemorais plugin) {
        this.plugin = plugin;
        this.lobbies = new HashMap<>();
        this.playerLobbies = new HashMap<>();
        initializeLobbies();
    }

    private void initializeLobbies() {
        for (int i = 1; i <= 3; i++) {
            String worldName = "world_lobby" + i;
            World world = Bukkit.getWorld(worldName);
            if (world == null) {
                plugin.getLogger().warning("Mundo " + worldName + " não encontrado! Criando...");
                world = Bukkit.createWorld(new org.bukkit.WorldCreator(worldName));
                world.setSpawnLocation(0, 100, 0);
            }
            lobbies.put(i, new Lobby(i, world));
        }
    }

    public void assignPlayerToLobby(Player player) {
        int targetLobbyId = 1;
        int minPlayers = Integer.MAX_VALUE;
        for (Map.Entry<Integer, Lobby> entry : lobbies.entrySet()) {
            int playerCount = getPlayerCountInLobby(entry.getKey());
            if (playerCount < minPlayers) {
                minPlayers = playerCount;
                targetLobbyId = entry.getKey();
            }
        }

        playerLobbies.put(player, targetLobbyId);
        Lobby lobby = lobbies.get(targetLobbyId);
        player.teleport(lobby.getSpawnLocation());
        player.sendMessage(ChatColor.GREEN + "Você foi colocado no Lobby #" + targetLobbyId);
    }

    public void movePlayerToLobby(Player player, int lobbyId) {
        if (!lobbies.containsKey(lobbyId)) {
            player.sendMessage(ChatColor.RED + "Lobby #" + lobbyId + " não existe!");
            return;
        }

        playerLobbies.put(player, lobbyId);
        Lobby lobby = lobbies.get(lobbyId);
        player.teleport(lobby.getSpawnLocation());
        player.sendMessage(ChatColor.GREEN + "Você foi movido para o Lobby #" + lobbyId);
    }

    public int getPlayerLobby(Player player) {
        return playerLobbies.getOrDefault(player, -1);
    }

    public long getPlayersInLobby(int lobbyId) {
        return playerLobbies.values().stream().filter(id -> id == lobbyId).count();
    }

    public void removePlayer(Player player) {
        playerLobbies.remove(player);
    }

    private int getPlayerCountInLobby(int lobbyId) {
        return (int) playerLobbies.values().stream().filter(id -> id == lobbyId).count();
    }

    private static class Lobby {
        private final int id;
        private final World world;

        public Lobby(int id, World world) {
            this.id = id;
            this.world = world;
        }

        public Location getSpawnLocation() {
            return world.getSpawnLocation();
        }
    }
}