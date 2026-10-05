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

import static com.yourname.dxdweapons.DxDRayMod.MODID;

public record BoostPacket() implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<BoostPacket> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(DxDRayMod.MODID, "boost"));
    public static final StreamCodec<ByteBuf, BoostPacket> STREAM_CODEC = StreamCodec.unit(new BoostPacket());

    @Override
    public Type<BoostPacket> type() {
        return TYPE;
    }

    public static void handle(BoostPacket data, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                if (!EquipModeHandler.isEquipMode(player.getUUID())) return;
                ItemStack stack = player.getOffhandItem();
                if (!(stack.getItem() instanceof BoostedGearItem)) {
                    stack = player.getData(ModAttachments.GAUNTLET_SLOT);
                }
                if (stack.getItem() instanceof BoostedGearItem bg) {
                    bg.triggerBoost(player.serverLevel(), player, stack);
                }
            }
        });
    }
}
