package org.caique.caiquemorais;

import org.bukkit.plugin.java.JavaPlugin;
import org.caique.caiquemorais.chat.ChatListener;
import org.caique.caiquemorais.chat.ChatManager;
import org.caique.caiquemorais.commands.GlobalChatCommand;
import org.caique.caiquemorais.commands.LoginCommand;
import org.caique.caiquemorais.commands.RegisterCommand;
import org.caique.caiquemorais.commands.TagsCommand;
import org.caique.caiquemorais.database.DatabaseManager;
import org.caique.caiquemorais.tags.TagManager;

public final class Caiquemorais extends JavaPlugin {
    private DatabaseManager databaseManager;
    private TagManager tagManager;
    private ChatManager chatManager;

    @Override
    public void onEnable() {
        saveDefaultConfig(); // Gera config.yml se não existir
        databaseManager = new DatabaseManager(this);
        databaseManager.connect();

        tagManager = new TagManager(this);
        chatManager = new ChatManager(tagManager);

        // Registrar comandos
        getCommand("login").setExecutor(new LoginCommand(this));
        getCommand("register").setExecutor(new RegisterCommand(this));
        getCommand("g").setExecutor(new GlobalChatCommand(this, chatManager));
        getCommand("tags").setExecutor(new TagsCommand(this, tagManager));

        // Registrar eventos
        getServer().getPluginManager().registerEvents(new ChatListener(this, chatManager), this);

        getLogger().info("Plugin Caiquemorais iniciado com sucesso!");
    }

    @Override
    public void onDisable() {
        if (databaseManager != null) {
            databaseManager.disconnect();
        }
        getLogger().info("Plugin Caiquemorais desativado.");
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public TagManager getTagManager() {
        return tagManager;
    }

    public ChatManager getChatManager() {
        return chatManager;
    }
}