package org.caique.caiquemorais.database;

import org.bukkit.configuration.file.FileConfiguration;
import org.caique.caiquemorais.Caiquemorais;

import java.sql.*;

public class DatabaseManager {
    private final Caiquemorais plugin;
    private Connection connection;

    public DatabaseManager(Caiquemorais plugin) {
        this.plugin = plugin;
    }

    public void connect() {
        FileConfiguration config = plugin.getConfig();
        String host = config.getString("database.host");
        int port = config.getInt("database.port");
        String database = config.getString("database.database");
        String username = config.getString("database.username");
        String password = config.getString("database.password");

        try {
            connection = DriverManager.getConnection(
                    "jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false",
                    username,
                    password
            );
            createTable();
            plugin.getLogger().info("Conectado ao MySQL com sucesso!");
        } catch (SQLException e) {
            plugin.getLogger().severe("Erro ao conectar ao MySQL: " + e.getMessage());
        }
    }

    public void disconnect() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                plugin.getLogger().info("Desconectado do MySQL.");
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Erro ao desconectar: " + e.getMessage());
        }
    }

    private void createTable() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS players (" +
                "uuid VARCHAR(36) PRIMARY KEY, " +
                "username VARCHAR(16) NOT NULL, " +
                "password VARCHAR(255) NOT NULL, " +
                "is_logged_in BOOLEAN DEFAULT FALSE, " +
                "tag VARCHAR(10) DEFAULT '', " +
                "vip_expires BIGINT DEFAULT 0" + // Coluna para expiração do VIP (timestamp)
                ")";
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }

    public Connection getConnection() {
        return connection;
    }
}