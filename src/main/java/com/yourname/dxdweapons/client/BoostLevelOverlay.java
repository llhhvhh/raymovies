package com.yourname.dxdweapons.client;

import com.yourname.dxdweapons.attachment.ModAttachments;
import com.yourname.dxdweapons.item.BoostedGearItem;
import com.yourname.dxdweapons.item.GreenBallItem;
import com.yourname.dxdweapons.item.ToolMode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

public class BoostLevelOverlay {
    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        ItemStack main = mc.player.getMainHandItem();
        ItemStack off = mc.player.getOffhandItem();
        ItemStack slotted = mc.player.getData(ModAttachments.GAUNTLET_SLOT);

        if (main.getItem() instanceof BoostedGearItem
                || off.getItem() instanceof BoostedGearItem
                || (!slotted.isEmpty() && slotted.getItem() instanceof BoostedGearItem)) {
            ItemStack stack = main.getItem() instanceof BoostedGearItem ? main
                    : (off.getItem() instanceof BoostedGearItem ? off : slotted);
            int boost = BoostedGearItem.getBoostLevelStatic(stack);
            ToolMode mode = BoostedGearItem.getToolModeStatic(stack);
            int w = mc.getWindow().getGuiScaledWidth();
            int h = mc.getWindow().getGuiScaledHeight();
            int x = w / 2 + 104;
            int y = h - 44;

            if (boost > 0) {
                event.getGuiGraphics().drawString(mc.font, "§cBoost " + boost, x, y, 0xFFFFFF);
            }
            if (mode != ToolMode.NONE) {
                String label = switch (mode) {
                    case PICKAXE -> "镐";
                    case AXE -> "斧";
                    case SHOVEL -> "铲";
                    case HOE -> "锄";
                    default -> "";
                };
                event.getGuiGraphics().drawString(mc.font, "§e" + label, x, y + (boost > 0 ? 10 : 0), 0xFFFFFF);
            }
        } else if (main.getItem() instanceof GreenBallItem || off.getItem() instanceof GreenBallItem) {
            ItemStack stack = main.getItem() instanceof GreenBallItem ? main : off;
            int boost = GreenBallItem.getBoostLevel(stack);
            int w = mc.getWindow().getGuiScaledWidth();
            int h = mc.getWindow().getGuiScaledHeight();
            int x = w / 2 + 104;
            int y = h - 44;

            if (boost > 0) {
                event.getGuiGraphics().drawString(mc.font, "§aGreen Boost " + boost + "/5", x, y, 0xFFFFFF);
            }
        }
    }
}
