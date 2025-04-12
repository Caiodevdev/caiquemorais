package org.caique.caiquemorais.hotbar;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.caique.caiquemorais.Caiquemorais;
import org.caique.caiquemorais.lobby.LobbyManager;
import org.caique.caiquemorais.utils.MessageUtils;
import org.caique.caiquemorais.vip.VipManager;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HotbarManager implements Listener {
    private final Caiquemorais plugin;
    private final VipManager vipManager;
    private final LobbyManager lobbyManager;
    private final Map<UUID, Long> vipCooldowns = new HashMap<>();
    private final Map<UUID, Long> lobbyCooldowns = new HashMap<>();
    private final Map<UUID, ItemStack[]> hotbarItemsOnDeath = new HashMap<>();

    public HotbarManager(Caiquemorais plugin, LobbyManager lobbyManager) {
        this.plugin = plugin;
        this.vipManager = plugin.getVipManager();
        this.lobbyManager = lobbyManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        giveVipItem(player);
        giveLobbyItem(player);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        UUID playerId = player.getUniqueId();

        // Salvar os itens da hotbar (slots 0 a 8)
        ItemStack[] hotbarItems = new ItemStack[9];
        for (int i = 0; i < 9; i++) {
            hotbarItems[i] = player.getInventory().getItem(i);
        }
        hotbarItemsOnDeath.put(playerId, hotbarItems);

        // Remover os itens da hotbar dos drops
        event.getDrops().removeIf(item -> isHotbarItem(item));
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        // Restaurar os itens da hotbar
        ItemStack[] hotbarItems = hotbarItemsOnDeath.get(playerId);
        if (hotbarItems != null) {
            for (int i = 0; i < 9; i++) {
                player.getInventory().setItem(i, hotbarItems[i]);
            }
            hotbarItemsOnDeath.remove(playerId);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();

        // Se for a GUI de lobbies, processar normalmente
        if (event.getView().getTitle().equals("§aEscolha um Lobby")) {
            event.setCancelled(true);
            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null || clickedItem.getType() != Material.PLAYER_HEAD) return;

            int lobbyId = clickedItem.getItemMeta().getEnchantLevel(org.bukkit.enchantments.Enchantment.DURABILITY) + 1;
            lobbyManager.movePlayerToLobby(player, lobbyId);
            plugin.getScoreboardManager().showScoreboard(player);
            player.closeInventory();
            return;
        }

        // Impedir mover os itens da hotbar (slots 0 a 8)
        ItemStack currentItem = event.getCurrentItem();
        ItemStack cursorItem = event.getCursor();
        int slot = event.getSlot();

        // Verificar se o slot clicado é da hotbar (0 a 8)
        if (slot >= 0 && slot <= 8 && currentItem != null && isHotbarItem(currentItem)) {
            event.setCancelled(true);
            return;
        }

        // Impedir colocar outro item na hotbar (cursor não vazio)
        if (slot >= 0 && slot <= 8 && cursorItem != null && cursorItem.getType() != Material.AIR) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        ItemStack item = event.getItemDrop().getItemStack();
        if (isHotbarItem(item)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (item == null) return;

        // Verificar se é o item de VIP
        if (item.getType() == Material.EMERALD && item.getItemMeta().getDisplayName().equals("§aComprar VIP")) {
            event.setCancelled(true);
            UUID playerId = player.getUniqueId();
            long currentTime = System.currentTimeMillis();
            long lastUse = vipCooldowns.getOrDefault(playerId, 0L);

            if (!player.hasPermission("caiquemorais.lobby")) {
                MessageUtils.sendMessage(player, "&cVocê não tem permissão para trocar de lobby!");
                MessageUtils.playSound(player, Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                return;
            }

            if (currentTime - lastUse < 5000) {
                MessageUtils.sendMessage(player, "§cAguarde " + ((5000 - (currentTime - lastUse)) / 1000) + "s para usar novamente!");
                MessageUtils.playSound(player, Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                return;
            }

            vipCooldowns.put(playerId, currentTime);
            vipManager.updateTagColors();
            player.openInventory(vipManager.createVipGui(player));
            MessageUtils.playSound(player, Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);
        }

        // Verificar se é o item de Lobbies
        if (item.getType() == Material.CLOCK && item.getItemMeta().getDisplayName().equals("§eTrocar Lobby")) {
            event.setCancelled(true);
            UUID playerId = player.getUniqueId();
            long currentTime = System.currentTimeMillis();
            long lastUse = lobbyCooldowns.getOrDefault(playerId, 0L);

            if (!player.hasPermission("caiquemorais.lobby")) {
                MessageUtils.sendMessage(player, "&cVocê não tem permissão para trocar de lobby!");
                MessageUtils.playSound(player, Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                return;
            }

            if (currentTime - lastUse < 5000) {
                MessageUtils.sendMessage(player, "§cAguarde " + ((5000 - (currentTime - lastUse)) / 1000) + "s para usar novamente!");
                MessageUtils.playSound(player, Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                return;
            }

            lobbyCooldowns.put(playerId, currentTime);
            player.openInventory(createLobbyGui(player));
            MessageUtils.playSound(player, Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);
        }
    }

    public void giveVipItem(Player player) {
        ItemStack emerald = new ItemStack(Material.EMERALD);
        ItemMeta meta = emerald.getItemMeta();
        meta.setDisplayName("§aComprar VIP");
        emerald.setItemMeta(meta);
        player.getInventory().setItem(8, emerald);
    }

    public void giveLobbyItem(Player player) {
        ItemStack clock = new ItemStack(Material.CLOCK);
        ItemMeta meta = clock.getItemMeta();
        meta.setDisplayName("§eTrocar Lobby");
        clock.setItemMeta(meta);
        player.getInventory().setItem(4, clock);
    }

    private Inventory createLobbyGui(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, "§aEscolha um Lobby");
        int currentLobby = lobbyManager.getPlayerLobby(player);

        // Preencher com vidro cinza como fundo
        ItemStack grayPane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta grayMeta = grayPane.getItemMeta();
        grayMeta.setDisplayName(" ");
        grayPane.setItemMeta(grayMeta);
        for (int i = 0; i < 27; i++) {
            gui.setItem(i, grayPane);
        }

        // Adicionar itens dos lobbies (Lobby #1, #2, #3)
        for (int lobbyId = 1; lobbyId <= 3; lobbyId++) {
            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta headMeta = (SkullMeta) head.getItemMeta();
            headMeta.setDisplayName("§eLobby #" + lobbyId);

            // Contar jogadores no lobby
            long playerCount = lobbyManager.getPlayersInLobby(lobbyId);
            boolean isCurrentLobby = currentLobby == lobbyId;
            headMeta.setLore(Arrays.asList(
                    "§7Jogadores: §f" + playerCount,
                    isCurrentLobby ? "§cVocê está aqui!" : "§aClique para entrar!"
            ));

            // Definir o dono da cabeça como o próprio jogador (pra mostrar a skin dele)
            headMeta.setOwningPlayer(player);
            head.setItemMeta(headMeta);

            // Adicionar um "enchant" invisível pra identificar o lobby (hack pra armazenar o ID)
            head.addUnsafeEnchantment(org.bukkit.enchantments.Enchantment.DURABILITY, lobbyId - 1);

            // Posicionar no inventário (slots 11, 13, 15)
            int slot = 10 + (lobbyId * 2);
            gui.setItem(slot, head);

            // Adicionar fundo verde se for o lobby atual
            if (isCurrentLobby) {
                ItemStack greenPane = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
                ItemMeta greenMeta = greenPane.getItemMeta();
                greenMeta.setDisplayName(" ");
                greenPane.setItemMeta(greenMeta);
                gui.setItem(slot - 9, greenPane); // Slot acima do item
            }
        }

        return gui;
    }

    private boolean isHotbarItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        String displayName = item.getItemMeta().getDisplayName();
        return (item.getType() == Material.EMERALD && displayName.equals("§aComprar VIP")) ||
                (item.getType() == Material.CLOCK && displayName.equals("§eTrocar Lobby"));
    }
}