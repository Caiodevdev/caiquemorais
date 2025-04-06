package org.caique.caiquemorais.vip;

import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapView;
import org.caique.caiquemorais.Caiquemorais;
import org.caique.caiquemorais.database.PlayerData;
import org.caique.caiquemorais.tags.TagManager;
import org.caique.caiquemorais.utils.MessageUtils;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.ChatMessageType;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.imageio.ImageIO;

public class VipManager {
    private final Caiquemorais plugin;
    private final TagManager tagManager;
    private final PlayerData playerData;
    private final Map<UUID, Long> qrCodeExpirations = new HashMap<>();
    private final Map<UUID, ItemStack> qrCodeMaps = new HashMap<>();

    public VipManager(Caiquemorais plugin) {
        this.plugin = plugin;
        this.tagManager = plugin.getTagManager();
        this.playerData = new PlayerData(plugin.getDatabaseManager());
    }

    public Inventory createVipGui(Player player) {
        try {
            long vipExpiration = playerData.getVipExpiration(player);
            String currentTag = playerData.getTag(player);
            boolean hasActiveVip = vipExpiration > System.currentTimeMillis() && (currentTag.equals("VIP") || currentTag.equals("VIP+"));

            if (hasActiveVip) {
                return createExtendVipGui(player, currentTag);
            } else {
                return createPurchaseVipGui();
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Erro ao verificar VIP do jogador " + player.getName() + ": " + e.getMessage());
            MessageUtils.sendMessage(player, "§cErro ao abrir a GUI de VIP. Tente novamente mais tarde.");
            return createPurchaseVipGui(); // Fallback para a GUI de compra
        }
    }

    private Inventory createPurchaseVipGui() {
        Inventory gui = Bukkit.createInventory(null, 27, "§8Comprar VIP");

        ItemStack vipItem = new ItemStack(Material.SUNFLOWER);
        ItemMeta vipMeta = vipItem.getItemMeta();
        vipMeta.setDisplayName("§eVIP");
        vipMeta.setLore(Arrays.asList(
                "§7Preço: R$10,00",
                "§7Duração: 30 dias",
                "§7Benefícios:",
                "§e- Tag [VIP] amarela",
                "§e- +10% de XP",
                "§aClique para comprar!"
        ));
        vipItem.setItemMeta(vipMeta);
        gui.setItem(11, vipItem);

        ItemStack vipPlusItem = new ItemStack(Material.SUNFLOWER);
        ItemMeta vipPlusMeta = vipPlusItem.getItemMeta();
        vipPlusMeta.setDisplayName("§eVIP§b+");
        vipPlusMeta.setLore(Arrays.asList(
                "§7Preço: R$20,00",
                "§7Duração: 30 dias",
                "§7Benefícios:",
                "§e- Tag [VIP§b+§e] amarela com + azul",
                "§e- +20% de XP",
                "§aClique para comprar!"
        ));
        vipPlusItem.setItemMeta(vipPlusMeta);
        gui.setItem(15, vipPlusItem);

        return gui;
    }

    private Inventory createExtendVipGui(Player player, String currentTag) {
        Inventory gui = Bukkit.createInventory(null, 27, "§8Estender VIP");
        try {
            long daysLeft = (playerData.getVipExpiration(player) - System.currentTimeMillis()) / (1000 * 60 * 60 * 24);

            ItemStack extendItem = new ItemStack(Material.CLOCK);
            ItemMeta extendMeta = extendItem.getItemMeta();
            extendMeta.setDisplayName("§eEstender " + (currentTag.equals("VIP+") ? "§eVIP§b+" : "§eVIP"));
            extendMeta.setLore(Arrays.asList(
                    "§7Seu VIP atual expira em: §e" + daysLeft + " dias",
                    "§7Preço: R$" + (currentTag.equals("VIP") ? "10,00" : "20,00"),
                    "§7Estender por mais 30 dias",
                    "§aClique para estender!"
            ));
            extendItem.setItemMeta(extendMeta);
            gui.setItem(13, extendItem);
        } catch (SQLException e) {
            plugin.getLogger().severe("Erro ao calcular dias restantes do VIP do jogador " + player.getName() + ": " + e.getMessage());
            MessageUtils.sendMessage(player, "§cErro ao carregar informações do VIP. Tente novamente mais tarde.");
        }
        return gui;
    }

    public Inventory createConfirmationGui(String vipType, boolean isExtension) {
        Inventory gui = Bukkit.createInventory(null, 27, "§8Confirmar " + (isExtension ? "Extensão" : "Compra"));
        String price = vipType.equals("VIP") ? "R$10,00" : "R$20,00";

        ItemStack confirm = new ItemStack(Material.LIME_DYE);
        ItemMeta confirmMeta = confirm.getItemMeta();
        confirmMeta.setDisplayName("§aSim");
        confirmMeta.setLore(Arrays.asList("§7Confirmar " + (isExtension ? "extensão" : "compra") + " de " + vipType + " por " + price));
        confirm.setItemMeta(confirmMeta);
        gui.setItem(11, confirm);

        ItemStack cancel = new ItemStack(Material.RED_DYE);
        ItemMeta cancelMeta = cancel.getItemMeta();
        cancelMeta.setDisplayName("§cNão");
        cancelMeta.setLore(Arrays.asList("§7Cancelar " + (isExtension ? "extensão" : "compra")));
        cancel.setItemMeta(cancelMeta);
        gui.setItem(15, cancel);

        return gui;
    }

    public void giveQrCodeMap(Player player, String vipType, boolean isExtension) {
        ItemStack map = createQrCodeMap();
        MapMeta mapMeta = (MapMeta) map.getItemMeta();
        mapMeta.setDisplayName("§aQR Code de Pagamento");
        mapMeta.setLore(Arrays.asList(
                "§7Escaneie este mapa com seu celular!",
                "§eValidade: 1 minuto"
        ));
        map.setItemMeta(mapMeta);

        int slot = player.getInventory().firstEmpty();
        if (slot == -1) {
            player.getWorld().dropItem(player.getLocation(), map);
            MessageUtils.sendMessage(player, "§cSeu inventário está cheio! O mapa foi dropado no chão.");
        } else {
            player.getInventory().setItem(slot, map);
            player.getInventory().setHeldItemSlot(slot);
        }

        qrCodeMaps.put(player.getUniqueId(), map);
        UUID playerId = player.getUniqueId();
        qrCodeExpirations.put(playerId, System.currentTimeMillis() + 60000);
        startQrCodeTimer(player, vipType, isExtension);
    }

    private ItemStack createQrCodeMap() {
        ItemStack map = new ItemStack(Material.FILLED_MAP);
        MapMeta mapMeta = (MapMeta) map.getItemMeta();
        MapView view = Bukkit.createMap(Bukkit.getWorlds().get(0));
        view.getRenderers().clear();
        view.addRenderer(new QrCodeRenderer(plugin));
        mapMeta.setMapView(view);
        map.setItemMeta(mapMeta);
        return map;
    }

    private void startQrCodeTimer(Player player, String vipType, boolean isExtension) {
        UUID playerId = player.getUniqueId();
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!qrCodeExpirations.containsKey(playerId)) return;

            long timeLeft = qrCodeExpirations.get(playerId) - System.currentTimeMillis();
            if (timeLeft <= 0) {
                MessageUtils.sendTitle(player, "§cExpirado!", "§fO QR Code venceu.", 10, 40, 10);
                MessageUtils.playSound(player, Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                qrCodeExpirations.remove(playerId);
                removeQrCodeMap(player);
                return;
            }

            int secondsLeft = (int) (timeLeft / 1000);
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText("§eTempo restante: §f" + secondsLeft + "s"));
        }, 0L, 20L);
    }

    private void removeQrCodeMap(Player player) {
        ItemStack map = qrCodeMaps.remove(player.getUniqueId());
        if (map != null) {
            player.getInventory().removeItem(map);
        }
    }

    public void completePurchase(Player player, String vipType, boolean isExtension) {
        try {
            if (!isExtension) {
                if (vipType.equals("VIP")) {
                    tagManager.setPlayerTag(player, "VIP");
                } else if (vipType.equals("VIP+")) {
                    tagManager.setPlayerTag(player, "VIP+");
                }
            }

            long currentExpiration = playerData.getVipExpiration(player);
            long newExpiration = (currentExpiration > System.currentTimeMillis() ? currentExpiration : System.currentTimeMillis())
                    + (30L * 24 * 60 * 60 * 1000); // 30 dias
            playerData.setVipExpiration(player, newExpiration);

            MessageUtils.sendTitle(player, "§a" + (isExtension ? "Extensão Concluída!" : "Compra Concluída!"),
                    "§fVocê " + (isExtension ? "estendeu seu " : "agora é ") + vipType + "!", 10, 60, 10);
            MessageUtils.sendMessage(player, "§aParabéns! Seu " + vipType + " foi " + (isExtension ? "estendido" : "adquirido") + " por 30 dias!");
            MessageUtils.playSound(player, Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 1.5f);
            Bukkit.broadcastMessage("§e[VIP] " + player.getName() + " " + (isExtension ? "estendeu seu " : "agora é ") + vipType + "!");

            Firework fw = (Firework) player.getWorld().spawnEntity(player.getLocation(), EntityType.FIREWORK);
            fw.getFireworkMeta().addEffect(FireworkEffect.builder().withColor(org.bukkit.Color.YELLOW).with(FireworkEffect.Type.STAR).build());
            fw.detonate();

            qrCodeExpirations.remove(player.getUniqueId());
            removeQrCodeMap(player);
        } catch (SQLException e) {
            plugin.getLogger().severe("Erro ao completar " + (isExtension ? "extensão" : "compra") + ": " + e.getMessage());
        }
    }

    public void updateTagColors() {
        tagManager.getAvailableTags().put("VIP", ChatColor.YELLOW.toString());
        tagManager.getAvailableTags().put("VIP+", ChatColor.YELLOW.toString());
    }
}