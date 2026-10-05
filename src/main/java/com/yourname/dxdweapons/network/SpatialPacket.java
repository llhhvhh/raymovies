package com.yourname.dxdweapons.network;

import com.yourname.dxdweapons.DxDRayMod;
import com.yourname.dxdweapons.attachment.ModAttachments;
import com.yourname.dxdweapons.item.BoostedGearItem;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SpatialPacket() implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SpatialPacket> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(DxDRayMod.MODID, "spatial"));
    public static final StreamCodec<ByteBuf, SpatialPacket> STREAM_CODEC = StreamCodec.unit(new SpatialPacket());

    @Override
    public Type<SpatialPacket> type() {
        return TYPE;
    }

    public static void handle(SpatialPacket data, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                if (!EquipModeHandler.isEquipMode(player.getUUID())) {
                    SpatialEffectHandler.deactivate(player);
                    return;
                }
                boolean hasGear = player.getMainHandItem().getItem() instanceof BoostedGearItem
                        || player.getOffhandItem().getItem() instanceof BoostedGearItem
                        || (!player.getData(ModAttachments.GAUNTLET_SLOT).isEmpty()
                        && player.getData(ModAttachments.GAUNTLET_SLOT).getItem() instanceof BoostedGearItem);
                if (hasGear) {
                    SpatialEffectHandler.activate(player);
                }
            }
        });
    }
}
