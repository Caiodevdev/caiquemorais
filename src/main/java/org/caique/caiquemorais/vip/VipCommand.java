package org.caique.caiquemorais.vip;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.caique.caiquemorais.Caiquemorais;
import org.caique.caiquemorais.database.PlayerData;
import org.caique.caiquemorais.utils.MessageUtils;

import java.sql.SQLException;

public class VipCommand implements CommandExecutor {
    private final Caiquemorais plugin;
    private final PlayerData playerData;

    public VipCommand(Caiquemorais plugin) {
        this.plugin = plugin;
        this.playerData = new PlayerData(plugin.getDatabaseManager());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Apenas jogadores podem usar este comando!");
            return true;
        }

        Player player = (Player) sender;
        try {
            long expiration = playerData.getVipExpiration(player);
            String tag = playerData.getTag(player);
            if (expiration == 0 || expiration < System.currentTimeMillis()) {
                MessageUtils.sendMessage(player, "§cVocê não possui VIP ativo.");
            } else {
                long daysLeft = (expiration - System.currentTimeMillis()) / (1000 * 60 * 60 * 24);
                MessageUtils.sendMessage(player, "§eSeu VIP (" + tag + ") expira em: §f" + daysLeft + " dias.");
            }
        } catch (SQLException e) {
            MessageUtils.sendMessage(player, "§cErro ao verificar VIP: " + e.getMessage());
        }
        return true;
    }
}