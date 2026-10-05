package com.yourname.dxdweapons.client;

import com.yourname.dxdweapons.item.ToolMode;
import com.yourname.dxdweapons.network.EquipModePacket;
import com.yourname.dxdweapons.network.SpatialPacket;
import com.yourname.dxdweapons.network.ToolModePacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

public class RadialMenuScreen extends Screen {
    private static final int RADIUS = 64;
    private int centerX, centerY;
    private boolean pageTwo = false;

    public RadialMenuScreen() {
        super(Component.literal(""));
    }

    @Override
    protected void init() {
        super.init();
        centerX = width / 2;
        centerY = height / 2;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        int r = RADIUS;
        int color = 0xCC444444;
        int border = 0xFF888888;

        for (int dy = -r; dy <= r; dy++) {
            int rx = (int) Math.sqrt(r * r - dy * dy);
            if (rx < 1) continue;
            guiGraphics.fill(centerX - rx, centerY + dy, centerX + rx, centerY + dy + 1, color);
        }

        guiGraphics.fill(centerX - r, centerY - 1, centerX + r, centerY + 1, border);

        if (!pageTwo) {
            boolean hoverTop = mouseY >= centerY - r && mouseY < centerY;
            boolean hoverBottom = mouseY > centerY && mouseY <= centerY + r;

            if (hoverTop && isWithinCircle(mouseX, mouseY)) {
                int highlight = 0x33FFFFFF;
                for (int dy = -r; dy < 0; dy++) {
                    int rx = (int) Math.sqrt(r * r - dy * dy);
                    if (rx < 1) continue;
                    guiGraphics.fill(centerX - rx, centerY + dy, centerX + rx, centerY + dy + 1, highlight);
                }
            }
            if (hoverBottom && isWithinCircle(mouseX, mouseY)) {
                int highlight = 0x33FFFFFF;
                for (int dy = 1; dy <= r; dy++) {
                    int rx = (int) Math.sqrt(r * r - dy * dy);
                    if (rx < 1) continue;
                    guiGraphics.fill(centerX - rx, centerY + dy, centerX + rx, centerY + dy + 1, highlight);
                }
            }

            Component topText = Component.literal("变身");
            Component bottomText = Component.literal("解除变身");
            int textColor = 0xFFFFFF;
            guiGraphics.drawString(font, topText, centerX - font.width(topText) / 2, centerY - 20, textColor);
            guiGraphics.drawString(font, bottomText, centerX - font.width(bottomText) / 2, centerY + 14, textColor);
        } else {
            guiGraphics.fill(centerX - 1, centerY - r, centerX + 1, centerY + r, border);
            boolean hoverTL = isQuadrantHovered(mouseX, mouseY, -1, -1);
            boolean hoverTR = isQuadrantHovered(mouseX, mouseY, 1, -1);
            boolean hoverBL = isQuadrantHovered(mouseX, mouseY, -1, 1);
            boolean hoverBR = isQuadrantHovered(mouseX, mouseY, 1, 1);

            if (hoverTL) drawQuadrantHighlight(guiGraphics, -1, -1);
            if (hoverTR) drawQuadrantHighlight(guiGraphics, 1, -1);
            if (hoverBL) drawQuadrantHighlight(guiGraphics, -1, 1);
            if (hoverBR) drawQuadrantHighlight(guiGraphics, 1, 1);

            int textColor = 0xFFFFFF;
            guiGraphics.drawString(font, Component.literal("镐"), centerX - 28, centerY - 38, textColor);
            guiGraphics.drawString(font, Component.literal("斧"), centerX + 18, centerY - 38, textColor);
            guiGraphics.drawString(font, Component.literal("铲"), centerX - 28, centerY + 26, textColor);
            guiGraphics.drawString(font, Component.literal("锄"), centerX + 18, centerY + 26, textColor);
        }
    }

    private boolean isQuadrantHovered(int mx, int my, int signX, int signY) {
        if (!isWithinCircle(mx, my)) return false;
        int dx = mx - centerX;
        int dy = my - centerY;
        return (signX < 0 ? dx < 0 : dx > 0) && (signY < 0 ? dy < 0 : dy > 0);
    }

    private void drawQuadrantHighlight(GuiGraphics gg, int signX, int signY) {
        int highlight = 0x33FFFFFF;
        int r = RADIUS;
        for (int dy = -r; dy <= r; dy++) {
            int rx = (int) Math.sqrt(r * r - dy * dy);
            if (rx < 1) continue;
            int x1 = centerX - rx;
            int x2 = centerX + rx;
            if (signY < 0 && dy >= 0) continue;
            if (signY > 0 && dy <= 0) continue;
            if (signX < 0) gg.fill(x1, centerY + dy, centerX, centerY + dy + 1, highlight);
            if (signX > 0) gg.fill(centerX, centerY + dy, x2, centerY + dy + 1, highlight);
        }
    }

    private boolean isWithinCircle(int x, int y) {
        int dx = x - centerX;
        int dy = y - centerY;
        return dx * dx + dy * dy <= RADIUS * RADIUS;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!isWithinCircle((int) mouseX, (int) mouseY)) {
            if (button == 1) {
                onClose();
                return true;
            }
            if (button != 0) return super.mouseClicked(mouseX, mouseY, button);
            onClose();
            return true;
        }
        if (button == 1) {
            pageTwo = !pageTwo;
            return true;
        }
        if (button != 0) return super.mouseClicked(mouseX, mouseY, button);

        int dx = (int) (mouseX - centerX);
        int dy = (int) (mouseY - centerY);

        if (pageTwo) {
            // Dead centre falls through every quadrant test and would silently pick HOE.
            if (dx == 0 && dy == 0) {
                onClose();
                return true;
            }
            ToolMode mode;
            if (dx < 0 && dy < 0) mode = ToolMode.PICKAXE;
            else if (dx > 0 && dy < 0) mode = ToolMode.AXE;
            else if (dx < 0 && dy > 0) mode = ToolMode.SHOVEL;
            else mode = ToolMode.HOE;
            PacketDistributor.sendToServer(new ToolModePacket(mode));
        } else {
            if (dy < 0) {
                EquipModeState.clientEquipMode = true;
                PacketDistributor.sendToServer(new EquipModePacket(true));
            } else {
                EquipModeState.clientEquipMode = false;
                PacketDistributor.sendToServer(new EquipModePacket(false));
                PacketDistributor.sendToServer(new SpatialPacket());
            }
        }
        onClose();
        return true;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
