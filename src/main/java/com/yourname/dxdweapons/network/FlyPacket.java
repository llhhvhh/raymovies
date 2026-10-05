package com.yourname.dxdweapons.network;

import com.yourname.dxdweapons.DxDRayMod;
import com.yourname.dxdweapons.attachment.ModAttachments;
import com.yourname.dxdweapons.item.BoostedGearItem;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import java.util.Map;
import java.util.UUID;

public record FlyPacket() implements CustomPacketPayload {
    private static final long MIN_FLIGHT_INTERVAL_MS = 5000;
    private static final Map<UUID, Long> lastFlightTime = new java.util.HashMap<>();

    public static final CustomPacketPayload.Type<FlyPacket> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(DxDRayMod.MODID, "fly"));
    public static final StreamCodec<ByteBuf, FlyPacket> STREAM_CODEC = StreamCodec.unit(new FlyPacket());

    @Override
    public Type<FlyPacket> type() {
        return TYPE;
    }

    public static void handle(FlyPacket data, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                UUID uuid = player.getUUID();
                long now = System.currentTimeMillis();
                if (now - lastFlightTime.getOrDefault(uuid, 0L) < MIN_FLIGHT_INTERVAL_MS) return;
                if (!EquipModeHandler.isEquipMode(player.getUUID())) return;
                if (BlessingFlightHandler.hasFlight(player.getUUID())) return;
                if (player.getAbilities().flying) return;
                if (player.experienceLevel < 10) return;
                boolean hasGear = player.getMainHandItem().getItem() instanceof BoostedGearItem
                        || player.getOffhandItem().getItem() instanceof BoostedGearItem
                        || (!player.getData(ModAttachments.GAUNTLET_SLOT).isEmpty()
                        && player.getData(ModAttachments.GAUNTLET_SLOT).getItem() instanceof BoostedGearItem);
                if (!hasGear) return;
                lastFlightTime.put(uuid, now);
                BlessingFlightHandler.enableFlight(player, player.experienceLevel);
            }
        });
    }

    /** Called on logout so the per-player rate-limit entry does not linger forever. */
    public static void clearCooldown(UUID uuid) {
        lastFlightTime.remove(uuid);
    }
}
