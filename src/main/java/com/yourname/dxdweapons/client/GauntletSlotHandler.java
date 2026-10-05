package com.yourname.dxdweapons.client;

import com.yourname.dxdweapons.DxDRayMod;
import com.yourname.dxdweapons.attachment.ModAttachments;
import com.yourname.dxdweapons.item.BoostedGearItem;
import com.yourname.dxdweapons.network.GauntletSlotPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = DxDRayMod.MODID, value = Dist.CLIENT)
public class GauntletSlotHandler {
    private static final int SLOT_X = 75;
    private static final int SLOT_Y = 42;
    private static final int SLOT_SIZE = 18;

    @SubscribeEvent
    public static void onScreenRender(ScreenEvent.Render.Post event) {
        if (!(event.getScreen() instanceof InventoryScreen screen)) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        int left = screen.getGuiLeft();
        int top = screen.getGuiTop();
        int x = left + SLOT_X;
        int y = top + SLOT_Y;

        ItemStack gauntlet = mc.player.getData(ModAttachments.GAUNTLET_SLOT);

        fillSlotBg(event.getGuiGraphics(), x, y);

        if (!gauntlet.isEmpty()) {
            event.getGuiGraphics().renderItem(gauntlet, x + 1, y + 1);
            event.getGuiGraphics().renderItemDecorations(mc.font, gauntlet, x + 1, y + 1);

            int boostLevel = BoostedGearItem.getBoostLevelStatic(gauntlet);
            if (boostLevel > 0) {
                String text = String.valueOf(boostLevel);
                int textWidth = mc.font.width(text);
                int textX = x + (SLOT_SIZE - textWidth) / 2 + 1;
                int textY = y + (SLOT_SIZE - 8) / 2 + 1;
                event.getGuiGraphics().drawString(mc.font, text, textX, textY, 0xFFFF0000, true);
            }
        }

        int mouseX = (int) event.getMouseX();
        int mouseY = (int) event.getMouseY();
        if (mouseX >= x && mouseX < x + SLOT_SIZE && mouseY >= y && mouseY < y + SLOT_SIZE && !gauntlet.isEmpty()) {
            event.getGuiGraphics().renderTooltip(mc.font, gauntlet, mouseX, mouseY);
        }
    }

    private static void fillSlotBg(GuiGraphics gfx, int x, int y) {
        gfx.fill(x + 1, y + 1, x + SLOT_SIZE - 1, y + SLOT_SIZE - 1, 0xFF8B8B8B);
        gfx.fill(x, y, x + 1, y + SLOT_SIZE, 0xFFFFFFFF);
        gfx.fill(x + SLOT_SIZE - 1, y, x + SLOT_SIZE, y + SLOT_SIZE, 0xFF373737);
        gfx.fill(x, y, x + SLOT_SIZE, y + 1, 0xFFFFFFFF);
        gfx.fill(x, y + SLOT_SIZE - 1, x + SLOT_SIZE, y + SLOT_SIZE, 0xFF373737);
    }

    @SubscribeEvent
    public static void onMouseClick(ScreenEvent.MouseButtonPressed.Pre event) {
        if (event.getButton() != 0) return;
        if (!(event.getScreen() instanceof InventoryScreen screen)) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        int left = screen.getGuiLeft();
        int top = screen.getGuiTop();
        int slotStartX = left + SLOT_X;
        int slotStartY = top + SLOT_Y;
        int mouseX = (int) event.getMouseX();
        int mouseY = (int) event.getMouseY();

        if (mouseX >= slotStartX && mouseX < slotStartX + SLOT_SIZE &&
            mouseY >= slotStartY && mouseY < slotStartY + SLOT_SIZE) {
            event.setCanceled(true);
            handleSlotClick(mc.player);
        }
    }

    /**
     * The client never mutates its own inventory here: it only states the intent and lets
     * {@code GauntletSlotPacket} move the real stack, then waits for the authoritative
     * {@code GauntletSyncPacket} echo. That avoids losing the item when the backpack is full.
     */
    private static void handleSlotClick(LocalPlayer player) {
        boolean gauntletEquipped = !player.getData(ModAttachments.GAUNTLET_SLOT).isEmpty();
        PacketDistributor.sendToServer(new GauntletSlotPacket(!gauntletEquipped));
    }
}
