package org.caique.caiquemorais.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.caique.caiquemorais.Caiquemorais;
import org.caique.caiquemorais.punishment.Punishment;
import org.caique.caiquemorais.punishment.PunishmentManager;
import org.caique.caiquemorais.utils.MessageUtils;

import java.util.Date;
import java.util.List;

public class CheckPunishCommand implements CommandExecutor {
    private final Caiquemorais plugin;
    private final PunishmentManager punishmentManager;

    public CheckPunishCommand(Caiquemorais plugin) {
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
        if (!player.hasPermission("caiquemorais.checkpunish")) {
            MessageUtils.sendMessage(player, "§cVocê não tem permissão para usar este comando!");
            return true;
        }

        if (args.length != 1) {
            MessageUtils.sendMessage(player, "§cUso: /checkpunir <jogador>");
            return true;
        }

        String targetName = args[0];
        List<Punishment> punishments = punishmentManager.getPunishments(targetName);

        if (punishments.isEmpty()) {
            MessageUtils.sendMessage(player, "§eO jogador §f" + targetName + " §enão tem punições registradas.");
            return true;
        }

        MessageUtils.sendMessage(player, "§ePunições de §f" + targetName + "§e:");
        for (Punishment punishment : punishments) {
            long timeLeft = punishment.getExpiresAt() - System.currentTimeMillis();
            String status = punishment.isActive() ? (timeLeft > 0 ? "§cAtiva (§f" + formatDuration(timeLeft) + " restantes)" : "§cAtiva (permanente)") : "§aInativa";
            MessageUtils.sendMessage(player, "§7- §eID: §f" + punishment.getId() +
                    " §eTipo: §f" + punishment.getPunishmentType() +
                    " §eMotivo: §f" + punishment.getReason() +
                    " §ePor: §f" + punishment.getPunisherName() +
                    " §eData: §f" + new Date(punishment.getIssuedAt()).toString() +
                    " §eProva: §f" + punishment.getProofLink() +
                    " §eStatus: " + status);
        }

        return true;
    }

    private String formatDuration(long duration) {
        long days = duration / (24 * 60 * 60 * 1000);
        long hours = (duration % (24 * 60 * 60 * 1000)) / (60 * 60 * 1000);
        long minutes = (duration % (60 * 60 * 1000)) / (60 * 1000);
        return days + " dias, " + hours + " horas, " + minutes + " minutos";
    }
}