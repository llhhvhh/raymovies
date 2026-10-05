package com.yourname.dxdweapons.network;

import com.yourname.dxdweapons.DxDRayMod;
import com.yourname.dxdweapons.attachment.ModAttachments;
import com.yourname.dxdweapons.item.BoostedGearItem;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.Optional;

@EventBusSubscriber(modid = DxDRayMod.MODID)
public class HolyLockerHandler {
    public static final ResourceKey<Level> HOLY_LOCKER_KEY = ResourceKey.create(
            Registries.DIMENSION,
            ResourceLocation.fromNamespaceAndPath(DxDRayMod.MODID, "holy_locker")
    );

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(HolyLockerPacket.TYPE, HolyLockerPacket.STREAM_CODEC, HolyLockerPacket::handle);
    }

    public static void teleportToHolyLocker(ServerPlayer player) {
        ServerLevel targetLevel = player.serverLevel().getServer().getLevel(HOLY_LOCKER_KEY);
        if (targetLevel == null) return;

        if (player.level().dimension() == HOLY_LOCKER_KEY) {
            // Already in holy locker → return to the position where the player entered
            Optional<ModAttachments.PlayerPos> entry = player.getData(ModAttachments.HOLY_LOCKER_ENTRY);
            if (entry.isPresent()) {
                ModAttachments.PlayerPos pos = entry.get();
                ServerLevel returnLevel = player.serverLevel().getServer().getLevel(pos.dimension());
                if (returnLevel != null) {
                    player.teleportTo(returnLevel, pos.x(), pos.y(), pos.z(), pos.yaw(), pos.pitch());
                    player.setData(ModAttachments.HOLY_LOCKER_ENTRY, Optional.empty());
                    return;
                }
            }
            // Fallback: overworld spawn
            ServerLevel overworld = player.serverLevel().getServer().getLevel(Level.OVERWORLD);
            if (overworld != null) {
                player.teleportTo(overworld, 0.5, 65, 0.5, player.getYRot(), player.getXRot());
            }
        } else {
            // Go to holy locker, remember where the player came from
            player.setData(ModAttachments.HOLY_LOCKER_ENTRY,
                    Optional.of(new ModAttachments.PlayerPos(
                            player.serverLevel().dimension(),
                            player.getX(), player.getY(), player.getZ(),
                            player.getYRot(), player.getXRot())));
            ensureSpawnPlatform(targetLevel);
            player.teleportTo(targetLevel, 0.5, 129.0, 0.5, player.getYRot(), player.getXRot());
        }
    }

    private static void ensureSpawnPlatform(ServerLevel level) {
        BlockPos platformCenter = new BlockPos(0, 128, 0);
        boolean needsPlatform = level.getBlockState(platformCenter).isAir();
        if (!needsPlatform) return;

        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                level.setBlockAndUpdate(platformCenter.offset(x, 0, z), Blocks.END_STONE.defaultBlockState());
            }
        }
        level.setBlockAndUpdate(platformCenter, Blocks.GLOWSTONE.defaultBlockState());
    }

    public static boolean isHolyLockerDimension(ServerPlayer player) {
        return player.level().dimension() == HOLY_LOCKER_KEY;
    }

    public record HolyLockerPacket() implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<HolyLockerPacket> TYPE = new CustomPacketPayload.Type<>(
                ResourceLocation.fromNamespaceAndPath(DxDRayMod.MODID, "holy_locker"));
        public static final StreamCodec<ByteBuf, HolyLockerPacket> STREAM_CODEC = StreamCodec.unit(new HolyLockerPacket());

        @Override
        public Type<HolyLockerPacket> type() {
            return TYPE;
        }

        public static void handle(HolyLockerPacket data, IPayloadContext context) {
            context.enqueueWork(() -> {
                if (context.player() instanceof ServerPlayer player) {
                    // Must match the client's hasGear(): hand, offhand or the attachment slot.
                    boolean hasGear = player.getMainHandItem().getItem() instanceof BoostedGearItem
                            || player.getOffhandItem().getItem() instanceof BoostedGearItem
                            || (!player.getData(ModAttachments.GAUNTLET_SLOT).isEmpty()
                            && player.getData(ModAttachments.GAUNTLET_SLOT).getItem() instanceof BoostedGearItem);
                    if (hasGear) {
                        teleportToHolyLocker(player);
                    }
                }
            });
        }
    }
}
