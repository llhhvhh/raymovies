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
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Client only tells the server <em>what</em> it wants to do; the server moves its own inventory.
 * No ItemStack is ever accepted from the client, which would allow arbitrary data-component
 * injection into a server-side stack.
 */
public record GauntletSlotPacket(boolean equip) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<GauntletSlotPacket> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(DxDRayMod.MODID, "gauntlet_slot"));
    public static final StreamCodec<ByteBuf, GauntletSlotPacket> STREAM_CODEC = StreamCodec.composite(
            net.minecraft.network.codec.ByteBufCodecs.BOOL, GauntletSlotPacket::equip,
            GauntletSlotPacket::new);

    @Override
    public Type<GauntletSlotPacket> type() {
        return TYPE;
    }

    public static void handle(GauntletSlotPacket data, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;

            ItemStack current = player.getData(ModAttachments.GAUNTLET_SLOT);
            if (data.equip()) {
                if (!current.isEmpty()) return;
                int slot = findGauntletSlot(player);
                if (slot < 0) return;
                ItemStack removed = player.getInventory().removeItem(slot, 1);
                if (removed.isEmpty()) return;
                player.setData(ModAttachments.GAUNTLET_SLOT, removed);
            } else {
                if (current.isEmpty()) return;
                player.setData(ModAttachments.GAUNTLET_SLOT, ItemStack.EMPTY);
                player.getInventory().placeItemBackInInventory(current);
            }

            PacketDistributor.sendToPlayer(player, new GauntletSyncPacket(player.getData(ModAttachments.GAUNTLET_SLOT)));
        });
    }

    private static int findGauntletSlot(ServerPlayer player) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            if (player.getInventory().getItem(i).getItem() instanceof BoostedGearItem) {
                return i;
            }
        }
        return -1;
    }
}