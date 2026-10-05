package com.yourname.dxdweapons.network;

import com.yourname.dxdweapons.DxDRayMod;
import com.yourname.dxdweapons.attachment.ModAttachments;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Relays the owner's gauntlet to their own client. Other players already receive the attachment
 * through NeoForge's automatic entity-attachment sync, so this packet only has to cover self.
 * Deliberately free of client-only imports so it stays loadable on a dedicated server.
 */
public record GauntletSyncPacket(ItemStack stack) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<GauntletSyncPacket> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(DxDRayMod.MODID, "gauntlet_sync"));
    public static final StreamCodec<ByteBuf, GauntletSyncPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.fromCodec(ItemStack.OPTIONAL_CODEC), GauntletSyncPacket::stack,
            GauntletSyncPacket::new);

    @Override
    public Type<GauntletSyncPacket> type() {
        return TYPE;
    }

    public static void handle(GauntletSyncPacket data, IPayloadContext context) {
        context.enqueueWork(() -> {
            Entity entity = context.player();
            if (entity != null) {
                entity.setData(ModAttachments.GAUNTLET_SLOT, data.stack);
            }
        });
    }
}