package com.yourname.dxdweapons.client;

import com.yourname.dxdweapons.attachment.ModAttachments;
import com.yourname.dxdweapons.item.BoostedGearItem;
import com.yourname.dxdweapons.item.GreenBallItem;
import com.yourname.dxdweapons.network.BoostPacket;
import com.yourname.dxdweapons.network.FlyPacket;
import com.yourname.dxdweapons.network.GreenBallBoostPacket;
import com.yourname.dxdweapons.network.HolyLockerHandler;
import com.yourname.dxdweapons.network.SpatialPacket;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import static com.yourname.dxdweapons.DxDRayMod.MODID;

@EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
public class ClientEvents {
    private static long lastJumpTime = 0;
    private static boolean wasJumpDown = false;
    private static boolean wasHolyLockerDown = false;
    private static final long DOUBLE_TAP_MS = 500;

    private static boolean hasGear(Minecraft mc) {
        if (mc.player == null) return false;
        return mc.player.getMainHandItem().getItem() instanceof BoostedGearItem
                || mc.player.getOffhandItem().getItem() instanceof BoostedGearItem
                || (!mc.player.getData(ModAttachments.GAUNTLET_SLOT).isEmpty()
                && mc.player.getData(ModAttachments.GAUNTLET_SLOT).getItem() instanceof BoostedGearItem);
    }

    private static boolean hasGreenBall(Minecraft mc) {
        if (mc.player == null) return false;
        return mc.player.getMainHandItem().getItem() instanceof GreenBallItem
                || mc.player.getOffhandItem().getItem() instanceof GreenBallItem;
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        // Never consume gameplay keys while a GUI is open, otherwise pressing V inside the
        // inventory would fire a boost and middle-click would teleport to another dimension.
        if (mc.screen != null) {
            wasJumpDown = mc.options.keyJump.isDown();
            wasHolyLockerDown = ModKeyBindings.HOLY_LOCKER_KEY.isDown();
            drainAll();
            return;
        }

        while (ModKeyBindings.EQUIP_MODE_KEY.consumeClick()) {
            if (hasGear(mc)) {
                mc.setScreen(new RadialMenuScreen());
            }
        }

        if (ModKeyBindings.HOLY_LOCKER_KEY.isDown()) {
            if (hasGear(mc) && !mc.player.isSpectator() && !wasHolyLockerDown) {
                wasHolyLockerDown = true;
                PacketDistributor.sendToServer(new HolyLockerHandler.HolyLockerPacket());
            }
        } else {
            wasHolyLockerDown = false;
        }

        // Always drain, otherwise clicks pile up in KeyMapping.clickCount and fire in a burst
        // the moment equip mode gets enabled.
        while (ModKeyBindings.BOOST_KEY.consumeClick()) {
            if (!EquipModeState.clientEquipMode) continue;
            if (hasGear(mc)) {
                PacketDistributor.sendToServer(new BoostPacket());
            } else if (hasGreenBall(mc)) {
                PacketDistributor.sendToServer(new GreenBallBoostPacket());
            }
        }

        while (ModKeyBindings.SPATIAL_KEY.consumeClick()) {
            if (EquipModeState.clientEquipMode && hasGear(mc)) {
                PacketDistributor.sendToServer(new SpatialPacket());
            }
        }

        if (!hasGear(mc)) {
            wasJumpDown = mc.options.keyJump.isDown();
            return;
        }

        boolean isJumpDown = mc.options.keyJump.isDown();
        if (isJumpDown && !wasJumpDown && !mc.player.getAbilities().flying) {
            long now = System.currentTimeMillis();
            if (now - lastJumpTime < DOUBLE_TAP_MS) {
                PacketDistributor.sendToServer(new FlyPacket());
            }
            lastJumpTime = now;
        }
        wasJumpDown = isJumpDown;
    }

    private static void drainAll() {
        ModKeyBindings.EQUIP_MODE_KEY.consumeClick();
        ModKeyBindings.BOOST_KEY.consumeClick();
        ModKeyBindings.SPATIAL_KEY.consumeClick();
    }

    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        EquipModeState.clientEquipMode = false;
        lastJumpTime = 0;
        wasJumpDown = false;
        wasHolyLockerDown = false;
        drainAll();
    }

    @SubscribeEvent
    public static void onLoggingIn(ClientPlayerNetworkEvent.LoggingIn event) {
        EquipModeState.clientEquipMode = false;
    }
}