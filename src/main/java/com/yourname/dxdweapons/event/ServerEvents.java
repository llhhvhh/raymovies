package com.yourname.dxdweapons.event;

import com.yourname.dxdweapons.DxDRayMod;
import com.yourname.dxdweapons.attachment.ModAttachments;
import com.yourname.dxdweapons.effect.ModEffects;
import com.yourname.dxdweapons.item.BoostedGearItem;
import com.yourname.dxdweapons.network.BlessingFlightHandler;
import com.yourname.dxdweapons.network.FlyPacket;
import com.yourname.dxdweapons.network.EquipModeHandler;
import com.yourname.dxdweapons.network.GauntletSyncPacket;
import com.yourname.dxdweapons.network.HolyLockerHandler;
import com.yourname.dxdweapons.network.SpatialEffectHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class ServerEvents {
    private static final ResourceLocation BLESSING_SPEED_ID = ResourceLocation.fromNamespaceAndPath("raymovies", "blessing_speed");
    private static final Set<UUID> BLESSING_ACTIVE = new HashSet<>();
    private static final Map<UUID, Integer> BLESSING_SPEED_LEVEL = new HashMap<>();

    /** Temporarily disabled: the 24-block 6x6/4x4 cherry tree is unwanted for now. Flip to true to restore. */
    private static final boolean SPAWN_CHERRY_TREE = false;

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ItemStack gauntlet = player.getData(ModAttachments.GAUNTLET_SLOT);
            PacketDistributor.sendToPlayer(player, new GauntletSyncPacket(gauntlet));

            if (!player.getPersistentData().getBoolean("raymovies:patchouli_book_given")) {
                player.getPersistentData().putBoolean("raymovies:patchouli_book_given", true);
                player.getServer().getCommands().performPrefixedCommand(
                        player.createCommandSourceStack(),
                        "give @s patchouli:guide_book[patchouli:book=\"raymovies:recipe_book\"]");
            }

            if (!player.getPersistentData().getBoolean("raymovies:village_spawned")) {
                player.getPersistentData().putBoolean("raymovies:village_spawned", true);
                ServerLevel overworld = player.getServer().getLevel(Level.OVERWORLD);
                if (overworld != null) {
                    BlockPos spawnPos = player.blockPosition();
                    int safeY = overworld.getHeight(Heightmap.Types.WORLD_SURFACE, spawnPos.getX(), spawnPos.getZ());
                    player.teleportTo(spawnPos.getX() + 0.5, safeY, spawnPos.getZ() + 0.5);

                    TagKey<Structure> villageTag = TagKey.create(Registries.STRUCTURE, ResourceLocation.fromNamespaceAndPath("raymovies", "village_plains_only"));
                    BlockPos nearest = overworld.findNearestMapStructure(villageTag, player.blockPosition(), 2000, false);
                    if (nearest != null) {
                        BlockPos bellPos = findNearestBell(overworld, nearest, 32);
                        BlockPos teleTarget;
                        if (bellPos != null) {
                            int bellSurfaceY = overworld.getHeight(Heightmap.Types.WORLD_SURFACE, bellPos.getX(), bellPos.getZ());
                            teleTarget = new BlockPos(bellPos.getX(), bellSurfaceY, bellPos.getZ());
                        } else {
                            int surfaceY = overworld.getHeight(Heightmap.Types.WORLD_SURFACE, nearest.getX(), nearest.getZ());
                            teleTarget = new BlockPos(nearest.getX(), surfaceY, nearest.getZ());
                        }
                        player.teleportTo(teleTarget.getX() + 0.5, teleTarget.getY(), teleTarget.getZ() + 0.5);

                        if (SPAWN_CHERRY_TREE) {
                            int treeX = nearest.getX();
                            int treeZ = nearest.getZ();
                            int treeY = overworld.getHeight(Heightmap.Types.WORLD_SURFACE, treeX, treeZ);
                            buildCherryTree(overworld, treeX, treeY, treeZ);
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity().level().isClientSide) return;
        ServerPlayer player = (ServerPlayer) event.getEntity();

        BlessingFlightHandler.tick(player);
        BoostedGearItem.tickBoost(player);
        BoostedGearItem.tickDash(player);

        boolean hasGear = player.getMainHandItem().getItem() instanceof BoostedGearItem
                || player.getOffhandItem().getItem() instanceof BoostedGearItem
                || (!player.getData(ModAttachments.GAUNTLET_SLOT).isEmpty()
                && player.getData(ModAttachments.GAUNTLET_SLOT).getItem() instanceof BoostedGearItem);

        boolean hasBlessing = hasGear
                && EquipModeHandler.isEquipMode(player.getUUID())
                && player.experienceLevel >= 10;

        if (hasBlessing) {
            applyBlessing(player);
        } else {
            clearBlessing(player);
            if (!player.isCreative() && !player.isSpectator()) {
                BlessingFlightHandler.disableFlight(player);
            }
        }
    }

    private static void applyBlessing(ServerPlayer player) {
        UUID uuid = player.getUUID();
        int level = player.experienceLevel;

        if (!BLESSING_ACTIVE.contains(uuid)) {
            player.addEffect(new MobEffectInstance(ModEffects.BLESSING, -1, 0, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, -1, 0, true, false, false));
            BLESSING_ACTIVE.add(uuid);
            BLESSING_SPEED_LEVEL.put(uuid, Integer.MIN_VALUE);
        }

        Integer applied = BLESSING_SPEED_LEVEL.get(uuid);
        if (applied != level) {
            AttributeInstance attr = player.getAttribute(Attributes.MOVEMENT_SPEED);
            if (attr != null) {
                attr.removeModifier(BLESSING_SPEED_ID);
                double bonus = (level - 10) * 0.01;
                attr.addTransientModifier(new AttributeModifier(BLESSING_SPEED_ID, bonus,
                        AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
            }
            BLESSING_SPEED_LEVEL.put(uuid, level);
        }

        if (player.tickCount % 2 == 0) {
            AABB box = player.getBoundingBox().inflate(15.0);
            for (LivingEntity target : player.level().getEntitiesOfClass(LivingEntity.class, box)) {
                if (target == player) continue;
                target.invulnerableTime = 0;
                target.hurtTime = 0;
            }
        }
    }

    private static void clearBlessing(ServerPlayer player) {
        UUID uuid = player.getUUID();
        if (BLESSING_ACTIVE.remove(uuid)) {
            player.removeEffect(ModEffects.BLESSING);
            player.removeEffect(MobEffects.FIRE_RESISTANCE);
        }
        if (BLESSING_SPEED_LEVEL.remove(uuid) != null) {
            AttributeInstance attr = player.getAttribute(Attributes.MOVEMENT_SPEED);
            if (attr != null) {
                attr.removeModifier(BLESSING_SPEED_ID);
            }
        }
    }

    private static void buildCherryTree(ServerLevel level, int x, int y, int z) {
        BlockState log = Blocks.CHERRY_LOG.defaultBlockState();
        BlockState leaves = Blocks.CHERRY_LEAVES.defaultBlockState();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for (int i = 0; i < 24; i++) {
            int half = i < 5 ? 3 : 2;
            for (int dx = -half; dx < half; dx++) {
                for (int dz = -half; dz < half; dz++) {
                    pos.set(x + dx, y + i, z + dz);
                    level.setBlockAndUpdate(pos, log);
                }
            }
        }

        int topY = y + 24;
        for (int dy = 0; dy <= 5; dy++) {
            int r = dy < 3 ? 5 : 4;
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    if (dx * dx + dz * dz <= r * r + 1) {
                        pos.set(x + dx, topY + dy, z + dz);
                        if (level.getBlockState(pos).isAir()) {
                            level.setBlockAndUpdate(pos, leaves);
                        }
                    }
                }
            }
        }
    }

    private static BlockPos findNearestBell(ServerLevel level, BlockPos center, int radius) {
        BlockPos closest = null;
        double closestDist = Double.MAX_VALUE;
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-radius, -radius, -radius), center.offset(radius, radius, radius))) {
            if (level.getBlockState(pos).is(Blocks.BELL)) {
                double dist = pos.distSqr(center);
                if (dist < closestDist) {
                    closestDist = dist;
                    closest = pos.immutable();
                }
            }
        }
        return closest;
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ItemStack gauntlet = player.getData(ModAttachments.GAUNTLET_SLOT);
            PacketDistributor.sendToPlayer(player, new GauntletSyncPacket(gauntlet));
            BoostedGearItem.clearBoostState(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            BlessingFlightHandler.disableFlight(player);
            EquipModeHandler.setEquipMode(player.getUUID(), false);
            SpatialEffectHandler.deactivate(player);
            clearBlessing(player);
            BoostedGearItem.clearBoostState(player);
            FlyPacket.clearCooldown(player.getUUID());
            SpatialEffectHandler.deactivate(player.getUUID());
        }
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.getPlayer() instanceof ServerPlayer player) {
            if (HolyLockerHandler.isHolyLockerDimension(player)) {
                event.setCanceled(true);
            }
        }
    }
}
