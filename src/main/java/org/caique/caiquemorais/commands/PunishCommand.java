package org.caique.caiquemorais.commands;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.caique.caiquemorais.Caiquemorais;
import org.caique.caiquemorais.punishment.PunishmentManager;
import org.caique.caiquemorais.utils.MessageUtils;

import java.util.Arrays;
import java.util.UUID;

public class PunishCommand implements CommandExecutor {
    private final Caiquemorais plugin;
    private final PunishmentManager punishmentManager;

    public PunishCommand(Caiquemorais plugin) {
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
        if (!player.hasPermission("caiquemorais.punish")) {
            MessageUtils.sendMessage(player, "§cVocê não tem permissão para usar este comando!");
            return true;
        }

        if (args.length == 0) {
            MessageUtils.sendMessage(player, "§cUso: /punir <jogador>");
            return true;
        }

        if (args.length == 1) {
            String targetName = args[0];
            MessageUtils.sendMessage(player, "§eSelecione o motivo da punição para §f" + targetName + "§e:");
            punishmentManager.getPunishmentTypes().forEach((reason, type) -> {
                TextComponent message = new TextComponent("§7- §f" + type.getName() + " §7(" + formatDuration(type.getDuration()) + ")");
                message.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND,
                        "/punir " + targetName + " " + reason + " [link da prova]"));
                message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        new Text("§eClique para punir por este motivo!")));
                player.spigot().sendMessage(message);
            });
            return true;
        }

        if (args.length >= 3) {
            String targetName = args[0];
            String reason = args[1];
            String proofLink = String.join(" ", Arrays.copyOfRange(args, 2, args.length));

            // Verificar se o motivo é válido
            if (!punishmentManager.getPunishmentTypes().containsKey(reason)) {
                MessageUtils.sendMessage(player, "§cMotivo inválido! Use /punir " + targetName + " para ver os motivos disponíveis.");
                return true;
            }

            // Obter UUID do jogador (online ou offline)
            Player target = Bukkit.getPlayer(targetName);
            String targetUuid;
            if (target != null) {
                targetUuid = target.getUniqueId().toString();
            } else {
                UUID uuid = Bukkit.getOfflinePlayer(targetName).getUniqueId();
                if (uuid == null) {
                    MessageUtils.sendMessage(player, "§cJogador não encontrado!");
                    return true;
                }
                targetUuid = uuid.toString();
            }

            punishmentManager.applyPunishment(player, targetUuid, targetName, reason, proofLink);
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