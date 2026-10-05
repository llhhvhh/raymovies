package com.yourname.dxdweapons.network;

import com.yourname.dxdweapons.DxDRayMod;
import com.yourname.dxdweapons.item.GreenBallItem;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record GreenBallBoostPacket() implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<GreenBallBoostPacket> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(DxDRayMod.MODID, "green_ball_boost"));
    public static final StreamCodec<ByteBuf, GreenBallBoostPacket> STREAM_CODEC = StreamCodec.unit(new GreenBallBoostPacket());

    @Override
    public Type<GreenBallBoostPacket> type() {
        return TYPE;
    }

    public static void handle(GreenBallBoostPacket data, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                ItemStack mainHand = player.getMainHandItem();
                ItemStack offHand = player.getOffhandItem();
                if (mainHand.getItem() instanceof GreenBallItem gb) {
                    gb.triggerBoost(player.serverLevel(), player, mainHand);
                } else if (offHand.getItem() instanceof GreenBallItem gb) {
                    gb.triggerBoost(player.serverLevel(), player, offHand);
                }
            }
        });
    }
}
