package org.caique.caiquemorais;

import org.bukkit.plugin.java.JavaPlugin;
import org.caique.caiquemorais.chat.ChatListener;
import org.caique.caiquemorais.chat.ChatManager;
import org.caique.caiquemorais.commands.*;
import org.caique.caiquemorais.database.DatabaseManager;
import org.caique.caiquemorais.hotbar.HotbarManager;
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

    @Override
    public void onEnable() {
        saveDefaultConfig();
        databaseManager = new DatabaseManager(this);
        databaseManager.connect();

        tagManager = new TagManager(this);
        chatManager = new ChatManager(tagManager);
        vipManager = new VipManager(this);
        hotbarManager = new HotbarManager(this);

        // Registrar comandos
        getCommand("login").setExecutor(new LoginCommand(this));
        getCommand("register").setExecutor(new RegisterCommand(this));
        getCommand("g").setExecutor(new GlobalChatCommand(this, chatManager));
        getCommand("tags").setExecutor(new TagsCommand(this, tagManager));
        getCommand("vipinfo").setExecutor(new VipCommand(this));

        // Registrar eventos
        getServer().getPluginManager().registerEvents(new ChatListener(this, chatManager), this);
        getServer().getPluginManager().registerEvents(new VipPurchaseListener(this, vipManager), this);
        getServer().getPluginManager().registerEvents(hotbarManager, this);

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
}