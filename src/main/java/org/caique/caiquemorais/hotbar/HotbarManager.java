package org.caique.caiquemorais.hotbar;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.caique.caiquemorais.Caiquemorais;
import org.caique.caiquemorais.utils.MessageUtils;
import org.caique.caiquemorais.vip.VipManager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HotbarManager implements Listener {
    private final Caiquemorais plugin;
    private final VipManager vipManager;
    private final Map<UUID, Long> cooldowns = new HashMap<>();

    public HotbarManager(Caiquemorais plugin) {
        this.plugin = plugin;
        this.vipManager = plugin.getVipManager();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        giveVipItem(player);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (item == null || item.getType() != Material.EMERALD || !item.getItemMeta().getDisplayName().equals("§aComprar VIP")) {
            return;
        }

        event.setCancelled(true);
        UUID playerId = player.getUniqueId();
        long currentTime = System.currentTimeMillis();
        long lastUse = cooldowns.getOrDefault(playerId, 0L);

        if (currentTime - lastUse < 5000) {
            MessageUtils.sendMessage(player, "§cAguarde " + ((5000 - (currentTime - lastUse)) / 1000) + "s para usar novamente!");
            MessageUtils.playSound(player, Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }

        cooldowns.put(playerId, currentTime);
        vipManager.updateTagColors();
        player.openInventory(vipManager.createVipGui(player));
        MessageUtils.playSound(player, Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);
    }

    public void giveVipItem(Player player) {
        ItemStack emerald = new ItemStack(Material.EMERALD);
        ItemMeta meta = emerald.getItemMeta();
        meta.setDisplayName("§aComprar VIP");
        emerald.setItemMeta(meta);
        player.getInventory().setItem(8, emerald);
    }
}