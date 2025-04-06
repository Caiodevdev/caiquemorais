package org.caique.caiquemorais.punishment;

import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.caique.caiquemorais.Caiquemorais;
import org.caique.caiquemorais.database.DatabaseManager;
import org.caique.caiquemorais.utils.MessageUtils;

import java.sql.*;
import java.util.*;
import java.util.Date;

public class PunishmentManager {
    private final Caiquemorais plugin;
    private final DatabaseManager db;
    private final Map<String, PunishmentType> punishmentTypes;

    public PunishmentManager(Caiquemorais plugin) {
        this.plugin = plugin;
        this.db = plugin.getDatabaseManager();
        this.punishmentTypes = new HashMap<>();

        // Definir tipos de punições (exemplo: 5 tipos)
        punishmentTypes.put("Hack", new PunishmentType("Ban por Hack", 30L * 24 * 60 * 60 * 1000)); // 30 dias
        punishmentTypes.put("Xingamento", new PunishmentType("Ban por Xingamento", 7L * 24 * 60 * 60 * 1000)); // 7 dias
        punishmentTypes.put("Spam", new PunishmentType("Ban por Spam", 3L * 24 * 60 * 60 * 1000)); // 3 dias
        punishmentTypes.put("Propaganda", new PunishmentType("Ban por Propaganda", 14L * 24 * 60 * 60 * 1000)); // 14 dias
        punishmentTypes.put("BugAbuse", new PunishmentType("Ban por Abuso de Bug", 10L * 24 * 60 * 60 * 1000)); // 10 dias
    }

    public void applyPunishment(Player punisher, String targetUuid, String targetName, String reason, String proofLink) {
        PunishmentType type = punishmentTypes.get(reason);
        if (type == null) return;

        long issuedAt = System.currentTimeMillis();
        long expiresAt = issuedAt + type.getDuration();

        // Salvar no banco de dados
        String sql = "INSERT INTO punishments (uuid, username, punisher_uuid, punisher_name, reason, proof_link, punishment_type, duration, issued_at, expires_at, active) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = db.getConnection().prepareStatement(sql)) {
            stmt.setString(1, targetUuid);
            stmt.setString(2, targetName);
            stmt.setString(3, punisher.getUniqueId().toString());
            stmt.setString(4, punisher.getName());
            stmt.setString(5, reason);
            stmt.setString(6, proofLink);
            stmt.setString(7, type.getName());
            stmt.setLong(8, type.getDuration());
            stmt.setLong(9, issuedAt);
            stmt.setLong(10, expiresAt);
            stmt.setBoolean(11, true);
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Erro ao aplicar punição: " + e.getMessage());
            return;
        }

        // Aplicar banimento
        String kickMessage = "§4§lVocê foi banido do servidor!\n" +
                "\n" +
                "§7Servidor: §eMinecraftGames\n" +
                "§7Motivo: §f" + type.getName() + "\n" +
                "§7Punido por: §f" + punisher.getName() + "\n" +
                "§7Prova: §b" + proofLink + "\n" +
                "§7Duração: §f" + formatDuration(type.getDuration()) + "\n" +
                "§7Compre unban em: §bhttps://minecraftgames.com/unban\n" +
                "\n";
        Bukkit.getBanList(BanList.Type.NAME).addBan(targetName, kickMessage, new Date(expiresAt), punisher.getName());

        // Desconectar o jogador, se estiver online
        Player target = Bukkit.getPlayer(UUID.fromString(targetUuid));
        if (target != null) {
            target.kickPlayer(kickMessage);
        }

        // Feedback para o punisher
        MessageUtils.sendTitle(punisher, "§aPunição Aplicada", "§f" + targetName + " foi banido!", 10, 60, 10);
        MessageUtils.sendMessage(punisher, "§aVocê baniu " + targetName + " por " + type.getName() + " (" + formatDuration(type.getDuration()) + ").");
        MessageUtils.playSound(punisher, Sound.ENTITY_VILLAGER_YES, 1.0f, 1.0f);

        // Anunciar no chat global
        Bukkit.broadcastMessage("§c[Punição] " + targetName + " foi banido por " + punisher.getName() + " (" + type.getName() + ").");
    }

    public void unbanPlayer(Player punisher, String targetName) {
        String targetUuid = null;
        List<Punishment> punishments = getPunishments(targetName);
        for (Punishment punishment : punishments) {
            if (punishment.isActive()) {
                targetUuid = punishment.getUuid();
                break;
            }
        }

        if (targetUuid == null) {
            MessageUtils.sendMessage(punisher, "§cO jogador " + targetName + " não tem punições ativas.");
            MessageUtils.playSound(punisher, Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }

        // Desativar a punição no banco de dados
        String sql = "UPDATE punishments SET active = FALSE WHERE uuid = ? AND active = TRUE";
        try (PreparedStatement stmt = db.getConnection().prepareStatement(sql)) {
            stmt.setString(1, targetUuid);
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Erro ao desbanir jogador: " + e.getMessage());
            return;
        }

        // Remover o banimento
        Bukkit.getBanList(BanList.Type.NAME).pardon(targetName);

        // Feedback
        MessageUtils.sendTitle(punisher, "§aJogador Desbanido", "§f" + targetName + " foi desbanido!", 10, 60, 10);
        MessageUtils.sendMessage(punisher, "§aVocê desbaniu " + targetName + " com sucesso.");
        MessageUtils.playSound(punisher, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
        Bukkit.broadcastMessage("§a[Punição] " + targetName + " foi desbanido por " + punisher.getName() + ".");
    }

    public List<Punishment> getPunishments(String targetName) {
        List<Punishment> punishments = new ArrayList<>();
        String sql = "SELECT * FROM punishments WHERE username = ? ORDER BY issued_at DESC";
        try (PreparedStatement stmt = db.getConnection().prepareStatement(sql)) {
            stmt.setString(1, targetName);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                punishments.add(new Punishment(
                        rs.getInt("id"),
                        rs.getString("uuid"),
                        rs.getString("username"),
                        rs.getString("punisher_uuid"),
                        rs.getString("punisher_name"),
                        rs.getString("reason"),
                        rs.getString("proof_link"),
                        rs.getString("punishment_type"),
                        rs.getLong("duration"),
                        rs.getLong("issued_at"),
                        rs.getLong("expires_at"),
                        rs.getBoolean("active")
                ));
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Erro ao buscar punições: " + e.getMessage());
        }
        return punishments;
    }

    public Map<String, PunishmentType> getPunishmentTypes() {
        return punishmentTypes;
    }

    private String formatDuration(long duration) {
        long days = duration / (24 * 60 * 60 * 1000);
        long hours = (duration % (24 * 60 * 60 * 1000)) / (60 * 60 * 1000);
        long minutes = (duration % (60 * 60 * 1000)) / (60 * 1000);
        return days + " dias, " + hours + " horas, " + minutes + " minutos";
    }
}