package org.caique.caiquemorais.database;

import org.bukkit.entity.Player;

import java.sql.*;

public class PlayerData {
    private final DatabaseManager db;

    public PlayerData(DatabaseManager db) {
        this.db = db;
    }

    public void registerPlayer(Player player, String password) throws SQLException {
        String sql = "INSERT INTO players (uuid, username, password) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = db.getConnection().prepareStatement(sql)) {
            stmt.setString(1, player.getUniqueId().toString());
            stmt.setString(2, player.getName());
            stmt.setString(3, password);
            stmt.executeUpdate();
        }
    }

    public boolean isRegistered(Player player) throws SQLException {
        String sql = "SELECT 1 FROM players WHERE uuid = ?";
        try (PreparedStatement stmt = db.getConnection().prepareStatement(sql)) {
            stmt.setString(1, player.getUniqueId().toString());
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        }
    }

    public boolean checkPassword(Player player, String password) throws SQLException {
        String sql = "SELECT password FROM players WHERE uuid = ?";
        try (PreparedStatement stmt = db.getConnection().prepareStatement(sql)) {
            stmt.setString(1, player.getUniqueId().toString());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("password").equals(password);
            }
            return false;
        }
    }

    public void setLoggedIn(Player player, boolean loggedIn) throws SQLException {
        String sql = "UPDATE players SET is_logged_in = ? WHERE uuid = ?";
        try (PreparedStatement stmt = db.getConnection().prepareStatement(sql)) {
            stmt.setBoolean(1, loggedIn);
            stmt.setString(2, player.getUniqueId().toString());
            stmt.executeUpdate();
        }
    }

    public boolean isLoggedIn(Player player) throws SQLException {
        String sql = "SELECT is_logged_in FROM players WHERE uuid = ?";
        try (PreparedStatement stmt = db.getConnection().prepareStatement(sql)) {
            stmt.setString(1, player.getUniqueId().toString());
            ResultSet rs = stmt.executeQuery();
            return rs.next() && rs.getBoolean("is_logged_in");
        }
    }

    public void setTag(Player player, String tag) throws SQLException {
        String sql = "UPDATE players SET tag = ? WHERE uuid = ?";
        try (PreparedStatement stmt = db.getConnection().prepareStatement(sql)) {
            stmt.setString(1, tag);
            stmt.setString(2, player.getUniqueId().toString());
            stmt.executeUpdate();
        }
    }

    public String getTag(Player player) throws SQLException {
        String sql = "SELECT tag FROM players WHERE uuid = ?";
        try (PreparedStatement stmt = db.getConnection().prepareStatement(sql)) {
            stmt.setString(1, player.getUniqueId().toString());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("tag");
            }
            return "";
        }
    }

    public void setVipExpiration(Player player, long expirationTime) throws SQLException {
        if (!isRegistered(player)) {
            registerPlayer(player, "default_password"); // Registra o jogador se não existir
        }
        String sql = "UPDATE players SET vip_expires = ? WHERE uuid = ?";
        try (PreparedStatement stmt = db.getConnection().prepareStatement(sql)) {
            stmt.setLong(1, expirationTime);
            stmt.setString(2, player.getUniqueId().toString());
            stmt.executeUpdate();
        }
    }

    public long getVipExpiration(Player player) throws SQLException {
        if (!isRegistered(player)) {
            return 0; // Retorna 0 se o jogador não estiver registrado
        }
        String sql = "SELECT vip_expires FROM players WHERE uuid = ?";
        try (PreparedStatement stmt = db.getConnection().prepareStatement(sql)) {
            stmt.setString(1, player.getUniqueId().toString());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getLong("vip_expires");
            }
            return 0;
        }
    }
}