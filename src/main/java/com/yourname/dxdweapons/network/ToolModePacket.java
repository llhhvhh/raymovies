package com.yourname.dxdweapons.network;

import com.yourname.dxdweapons.attachment.ModAttachments;
import com.yourname.dxdweapons.item.BoostedGearItem;
import com.yourname.dxdweapons.item.ToolMode;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import static com.yourname.dxdweapons.DxDRayMod.MODID;

public record ToolModePacket(ToolMode mode) implements CustomPacketPayload {
    public static final Type<ToolModePacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(MODID, "tool_mode"));
    public static final StreamCodec<ByteBuf, ToolModePacket> STREAM_CODEC = StreamCodec.of(
            (buf, pkt) -> ByteBufCodecs.VAR_INT.encode(buf, pkt.mode.ordinal()),
            buf -> {
                int ordinal = ByteBufCodecs.VAR_INT.decode(buf);
                ToolMode[] values = ToolMode.values();
                if (ordinal < 0 || ordinal >= values.length) {
                    throw new IllegalArgumentException("Invalid tool mode ordinal: " + ordinal);
                }
                return new ToolModePacket(values[ordinal]);
            }
    );

    @Override
    public Type<ToolModePacket> type() {
        return TYPE;
    }

    public static void handle(ToolModePacket pkt, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer player)) return;
            // Fall back through hand -> offhand -> attachment, same order as BoostPacket.
            ItemStack stack = player.getMainHandItem();
            if (!(stack.getItem() instanceof BoostedGearItem)) {
                stack = player.getOffhandItem();
            }
            if (!(stack.getItem() instanceof BoostedGearItem)) {
                stack = player.getData(ModAttachments.GAUNTLET_SLOT);
            }
            if (stack.getItem() instanceof BoostedGearItem) {
                BoostedGearItem.setToolMode(stack, pkt.mode);
            }
        });
    }
}