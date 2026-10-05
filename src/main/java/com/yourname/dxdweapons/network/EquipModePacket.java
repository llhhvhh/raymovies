package com.yourname.dxdweapons.network;

import com.yourname.dxdweapons.DxDRayMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record EquipModePacket(boolean mode) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<EquipModePacket> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(DxDRayMod.MODID, "equip_mode"));
    public static final StreamCodec<ByteBuf, EquipModePacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, EquipModePacket::mode,
            EquipModePacket::new);

    @Override
    public Type<EquipModePacket> type() {
        return TYPE;
    }

    public static void handle(EquipModePacket data, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                EquipModeHandler.setEquipMode(player.getUUID(), data.mode);
                if (!data.mode) {
                    SpatialEffectHandler.deactivate(player);
                    BlessingFlightHandler.disableFlight(player);
                }
            }
        });
    }
}
