package org.caique.caiquemorais;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.caique.caiquemorais.chat.ChatListener;
import org.caique.caiquemorais.chat.ChatManager;
import org.caique.caiquemorais.commands.*;
import org.caique.caiquemorais.database.DatabaseManager;
import org.caique.caiquemorais.hotbar.HotbarManager;
import org.caique.caiquemorais.lobby.LobbyManager;
import org.caique.caiquemorais.scoreboard.ScoreboardManager;
import org.caique.caiquemorais.tags.TagManager;
import org.caique.caiquemorais.vip.VipCommand;
import org.caique.caiquemorais.vip.VipManager;
import org.caique.caiquemorais.vip.VipPurchaseListener;

public final class Caiquemorais extends JavaPlugin {
    private DatabaseManager databaseManager;
    private TagManager tagManager;
    private ChatManager chatManager;
    private VipManager vipManager;
    private HotbarManager hotbarManager;
    private ScoreboardManager scoreboardManager;
    private LobbyManager lobbyManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        databaseManager = new DatabaseManager(this);
        databaseManager.connect();

        tagManager = new TagManager(this);
        chatManager = new ChatManager(tagManager);
        vipManager = new VipManager(this);
        lobbyManager = new LobbyManager(this);
        hotbarManager = new HotbarManager(this, lobbyManager);
        scoreboardManager = new ScoreboardManager(this, lobbyManager);

        // Registrar comandos
        getCommand("login").setExecutor(new LoginCommand(this));
        getCommand("register").setExecutor(new RegisterCommand(this));
        getCommand("g").setExecutor(new GlobalChatCommand(this, chatManager));
        getCommand("tags").setExecutor(new TagsCommand(this, tagManager));
        getCommand("vipinfo").setExecutor(new VipCommand(this));
        getCommand("punir").setExecutor(new PunishCommand(this));
        getCommand("desbanir").setExecutor(new UnbanCommand(this));
        getCommand("checkpunir").setExecutor(new CheckPunishCommand(this));
        getCommand("lobby").setExecutor(new LobbyCommand(this, lobbyManager));

        // Registrar eventos
        getServer().getPluginManager().registerEvents(new ChatListener(this, chatManager), this);
        getServer().getPluginManager().registerEvents(new VipPurchaseListener(this, vipManager), this);
        getServer().getPluginManager().registerEvents(hotbarManager, this);
        getServer().getPluginManager().registerEvents(new LobbyListener(), this);

        getLogger().info("Plugin Caiquemorais iniciado com sucesso!");
    }

    @Override
    public void onDisable() {
        if (databaseManager != null) {
            databaseManager.disconnect();
        }
        getLogger().info("Plugin Caiquemorais desativado.");
    }

    public DatabaseManager getDatabaseManager() { return databaseManager; }
    public TagManager getTagManager() { return tagManager; }
    public ChatManager getChatManager() { return chatManager; }
    public VipManager getVipManager() { return vipManager; }
    public HotbarManager getHotbarManager() { return hotbarManager; }
    public ScoreboardManager getScoreboardManager() { return scoreboardManager; }
    public LobbyManager getLobbyManager() { return lobbyManager; }

    private class LobbyListener implements Listener {
        @EventHandler
        public void onPlayerJoin(PlayerJoinEvent event) {
            Player player = event.getPlayer();
            lobbyManager.assignPlayerToLobby(player);
            scoreboardManager.showScoreboard(player);
        }

        @EventHandler
        public void onPlayerQuit(PlayerQuitEvent event) {
            lobbyManager.removePlayer(event.getPlayer());
        }
    }
}