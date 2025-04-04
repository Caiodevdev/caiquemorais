package org.caique.caiquemorais.commands;

import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.caique.caiquemorais.Caiquemorais;
import org.caique.caiquemorais.tags.TagManager;
import org.caique.caiquemorais.utils.MessageUtils;

import java.sql.SQLException;
import java.util.Map;

public class TagsCommand implements CommandExecutor {
    private final Caiquemorais plugin;
    private final TagManager tagManager;

    public TagsCommand(Caiquemorais plugin, TagManager tagManager) {
        this.plugin = plugin;
        this.tagManager = tagManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Apenas jogadores podem usar este comando!");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            MessageUtils.sendMessage(player, "&eTags disponíveis:");
            for (Map.Entry<String, String> entry : tagManager.getAvailableTags().entrySet()) {
                MessageUtils.sendMessage(player, entry.getValue() + "[" + entry.getKey() + "]");
            }
            MessageUtils.sendMessage(player, "&eUse /tags <tag> para selecionar!");
            return true;
        }

        String selectedTag = args[0].toUpperCase();
        if (!tagManager.getAvailableTags().containsKey(selectedTag)) {
            MessageUtils.sendMessage(player, "&cTag inválida! Use /tags para ver as opções.");
            MessageUtils.playSound(player, Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return true;
        }

        try {
            tagManager.setPlayerTag(player, selectedTag);
            MessageUtils.sendMessage(player, "&aTag " + tagManager.getAvailableTags().get(selectedTag) + "[" + selectedTag + "] &asetada com sucesso!");
            MessageUtils.playSound(player, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
        } catch (SQLException e) {
            MessageUtils.sendMessage(player, "&cErro ao setar tag: " + e.getMessage());
        }
        return true;
    }
}