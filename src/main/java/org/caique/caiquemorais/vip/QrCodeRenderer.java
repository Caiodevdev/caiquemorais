package org.caique.caiquemorais.vip;

import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.caique.caiquemorais.Caiquemorais;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class QrCodeRenderer extends MapRenderer {
    private final Caiquemorais plugin;
    private BufferedImage qrCodeImage;

    public QrCodeRenderer(Caiquemorais plugin) {
        this.plugin = plugin;
        try {
            File qrCodeFile = new File(plugin.getDataFolder(), "qrcode.png");
            if (!qrCodeFile.exists()) {
                plugin.getLogger().warning("Arquivo qrcode.png não encontrado em " + qrCodeFile.getPath());
                return;
            }
            qrCodeImage = ImageIO.read(qrCodeFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Erro ao carregar qrcode.png: " + e.getMessage());
        }
    }

    @Override
    public void render(MapView map, MapCanvas canvas, Player player) {
        if (qrCodeImage != null) {
            canvas.drawImage(0, 0, qrCodeImage);
        }
    }
}