package org.caique.caiquemorais.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.caique.caiquemorais.Caiquemorais;
import org.caique.caiquemorais.punishment.PunishmentManager;
import org.caique.caiquemorais.utils.MessageUtils;

public class UnbanCommand implements CommandExecutor {
    private final Caiquemorais plugin;
    private final PunishmentManager punishmentManager;

    public UnbanCommand(Caiquemorais plugin) {
        this.plugin = plugin;
        this.punishmentManager = new PunishmentManager(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Apenas jogadores podem usar este comando!");
            return true;
        }

        Player player = (Player) sender;
        if (!player.hasPermission("caiquemorais.unban")) {
            MessageUtils.sendMessage(player, "§cVocê não tem permissão para usar este comando!");
            return true;
        }

        if (args.length != 1) {
            MessageUtils.sendMessage(player, "§cUso: /desbanir <jogador>");
            return true;
        }

        String targetName = args[0];
        punishmentManager.unbanPlayer(player, targetName);
        return true;
    }
}