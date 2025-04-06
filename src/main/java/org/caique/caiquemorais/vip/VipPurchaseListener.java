package org.caique.caiquemorais.vip;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.caique.caiquemorais.Caiquemorais;
import org.caique.caiquemorais.utils.MessageUtils;

public class VipPurchaseListener implements Listener {
    private final Caiquemorais plugin;
    private final VipManager vipManager;

    public VipPurchaseListener(Caiquemorais plugin, VipManager vipManager) {
        this.plugin = plugin;
        this.vipManager = vipManager;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();

        if (title.equals("§8Comprar VIP")) {
            event.setCancelled(true);
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || !clicked.hasItemMeta()) return;

            String displayName = clicked.getItemMeta().getDisplayName();
            if (displayName.equals("§eVIP")) {
                player.openInventory(vipManager.createConfirmationGui("VIP", false));
                MessageUtils.playSound(player, Sound.BLOCK_NOTE_BLOCK_BELL, 1.0f, 1.0f);
            } else if (displayName.equals("§eVIP§b+")) {
                player.openInventory(vipManager.createConfirmationGui("VIP+", false));
                MessageUtils.playSound(player, Sound.BLOCK_NOTE_BLOCK_BELL, 1.0f, 1.0f);
            }
        } else if (title.equals("§8Estender VIP")) {
            event.setCancelled(true);
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || !clicked.hasItemMeta()) return;

            String displayName = clicked.getItemMeta().getDisplayName();
            if (displayName.startsWith("§eEstender")) {
                String vipType = displayName.contains("VIP+") ? "VIP+" : "VIP";
                player.openInventory(vipManager.createConfirmationGui(vipType, true));
                MessageUtils.playSound(player, Sound.BLOCK_NOTE_BLOCK_BELL, 1.0f, 1.0f);
            }
        } else if (title.startsWith("§8Confirmar ")) {
            event.setCancelled(true);
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || !clicked.hasItemMeta()) return;

            String displayName = clicked.getItemMeta().getDisplayName();
            boolean isExtension = title.contains("Extensão");
            if (displayName.equals("§aSim")) {
                String vipType = event.getInventory().getItem(11).getItemMeta().getLore().get(0).contains("VIP+") ? "VIP+" : "VIP";
                player.closeInventory();
                vipManager.giveQrCodeMap(player, vipType, isExtension);
                MessageUtils.playSound(player, Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);
                MessageUtils.sendMessage(player, "§aEscaneie o mapa com seu celular em até 1 minuto!");
            } else if (displayName.equals("§cNão")) {
                player.closeInventory();
                MessageUtils.sendMessage(player, "§c" + (isExtension ? "Extensão" : "Compra") + " cancelada.");
                MessageUtils.playSound(player, Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            }
        }
    }
}