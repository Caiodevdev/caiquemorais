package org.caique.caiquemorais.commands;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.caique.caiquemorais.Caiquemorais;
import org.caique.caiquemorais.punishment.Punishment;
import org.caique.caiquemorais.punishment.PunishmentManager;
import org.caique.caiquemorais.utils.MessageUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class CheckPunishCommand implements CommandExecutor {
    private final Caiquemorais plugin;
    private final PunishmentManager punishmentManager;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    public CheckPunishCommand(Caiquemorais plugin) {
        this.plugin = plugin;
        this.punishmentManager = new PunishmentManager(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cApenas jogadores podem usar este comando!"));
            return true;
        }

        Player player = (Player) sender;
        if (!player.hasPermission("caiquemorais.checkpunish")) {
            MessageUtils.sendMessage(player, "&cVocê não tem permissão para usar este comando!");
            MessageUtils.playSound(player, Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return true;
        }

        if (args.length != 1) {
            MessageUtils.sendMessage(player, "&cUso: /checkpunir <jogador>");
            return true;
        }

        String targetName = args[0];
        List<Punishment> punishments = punishmentManager.getPunishments(targetName);

        if (punishments.isEmpty()) {
            MessageUtils.sendMessage(player, "&eO jogador &f" + targetName + " &enão tem punições registradas.");
            MessageUtils.playSound(player, Sound.ENTITY_VILLAGER_YES, 1.0f, 1.0f);
            return true;
        }

        // Cabeçalho simples
        MessageUtils.sendMessage(player, "&ePunições de &f" + targetName + "&e:");

        // Listar punições
        for (Punishment punishment : punishments) {
            long timeLeft = punishment.getExpiresAt() - System.currentTimeMillis();
            String status = punishment.isActive() ? (timeLeft > 0 ? "&a[Ativo]" : "&a[Ativo (Permanente)]") : "&c[Desbanido]";
            String hoverText = "&7Detalhes da Punição:\n" +
                    "&eID: &f" + punishment.getId() + "\n" +
                    "&eTipo: &f" + punishment.getPunishmentType() + "\n" +
                    "&eMotivo: &f" + punishment.getReason() + "\n" +
                    "&eProva: &b" + punishment.getProofLink() + "\n" +
                    "&ePunido por: &f" + punishment.getPunisherName() + "\n" +
                    "&eData: &f" + dateFormat.format(new Date(punishment.getIssuedAt())) + "\n" +
                    "&eExpira em: &f" + (timeLeft > 0 ? dateFormat.format(new Date(punishment.getExpiresAt())) : "Permanente");
            if (!punishment.isActive() && punishment.getUnbannedBy() != null) {
                hoverText += "\n&eDesbanido por: &f" + punishment.getUnbannedBy();
            }

            // Mensagem principal com hover no status
            TextComponent statusComponent = new TextComponent(ChatColor.translateAlternateColorCodes('&', status));
            statusComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    new ComponentBuilder(ChatColor.translateAlternateColorCodes('&', hoverText)).create()));

            TextComponent message = new TextComponent(ChatColor.translateAlternateColorCodes('&',
                    "&7[ID: &f" + punishment.getId() + "&7] &e" + punishment.getPunishmentType() + " "));
            message.addExtra(statusComponent);

            // Enviar mensagem principal
            player.spigot().sendMessage(message);

            // Detalhes indentados
            MessageUtils.sendMessage(player, "  &7Motivo: &f" + punishment.getReason());
            MessageUtils.sendMessage(player, "  &7Prova: &b" + punishment.getProofLink());
            MessageUtils.sendMessage(player, "  &7Punido por: &f" + punishment.getPunisherName());
            MessageUtils.sendMessage(player, "  &7Data: &f" + dateFormat.format(new Date(punishment.getIssuedAt())));
            MessageUtils.sendMessage(player, "  &7Duração: &f" + punishmentManager.formatDuration(punishment.getDuration()));
            MessageUtils.sendMessage(player, "  &7Expira em: &f" + (timeLeft > 0 ? dateFormat.format(new Date(punishment.getExpiresAt())) : "Permanente"));

            // Linha em branco pra separar punições
            MessageUtils.sendMessage(player, "");
        }

        MessageUtils.playSound(player, Sound.ENTITY_VILLAGER_YES, 1.0f, 1.0f);
        return true;
    }
}