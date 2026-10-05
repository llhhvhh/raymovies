package com.yourname.dxdweapons.network;

import com.yourname.dxdweapons.attachment.ModAttachments;
import com.yourname.dxdweapons.item.BoostedGearItem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * "空间领域" aura: while active, every unit inside the radius other than the caster takes damage
 * on a fixed interval. It no longer touches terrain at all, so it can never corrupt a world.
 */
public class SpatialEffectHandler {
    private static final int BASE_RADIUS = 3;
    private static final int MAX_RADIUS = 24;
    private static final int BASE_DURATION_TICKS = 100;
    private static final int MAX_DURATION_TICKS = 7600;
    private static final int TICK_INTERVAL = 40;
    private static final float TICK_DAMAGE = 0.5F;
    private static final double MAX_HITS_PER_PULSE = 64;

    private static final Map<UUID, ActiveEffect> activeEffects = new HashMap<>();

    private static int calculateRadius(ServerPlayer player) {
        int level = player.experienceLevel;
        if (level < 10) return 0;
        return Math.min(MAX_RADIUS, BASE_RADIUS + (level - 10) / 2);
    }

    private static int calculateDuration(ServerPlayer player) {
        int level = player.experienceLevel;
        if (level < 10) return 0;
        int duration = BASE_DURATION_TICKS + (level - 10) * 120;
        return Math.min(MAX_DURATION_TICKS, duration);
    }

    public static void activate(ServerPlayer player) {
        UUID uuid = player.getUUID();
        if (activeEffects.containsKey(uuid)) {
            deactivate(player);
            return;
        }
        if (player.experienceLevel < 10) return;

        int radius = calculateRadius(player);
        if (radius <= 0) return;

        activeEffects.put(uuid, new ActiveEffect(radius, calculateDuration(player), TICK_INTERVAL));
        BoostedGearItem.clearBoostState(player);
    }

    public static void deactivate(ServerPlayer player) {
        activeEffects.remove(player.getUUID());
    }

    public static void deactivate(UUID uuid) {
        activeEffects.remove(uuid);
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (activeEffects.isEmpty()) return;

        List<UUID> expired = new ArrayList<>();

        for (Map.Entry<UUID, ActiveEffect> entry : activeEffects.entrySet()) {
            ActiveEffect effect = entry.getValue();
            effect.ticksLeft--;

            ServerPlayer player = event.getServer().getPlayerList().getPlayer(entry.getKey());
            if (player == null) {
                expired.add(entry.getKey());
                continue;
            }
            if (!hasGear(player)) {
                expired.add(entry.getKey());
                continue;
            }

            if (effect.pulseTicksLeft-- <= 0) {
                effect.pulseTicksLeft = TICK_INTERVAL;
                pulse(player, effect.radius);
            }

            if (effect.ticksLeft <= 0) {
                expired.add(entry.getKey());
            }
        }

        for (UUID uuid : expired) {
            activeEffects.remove(uuid);
        }
    }

    private static void pulse(ServerPlayer player, int radius) {
        AABB box = player.getBoundingBox().inflate(radius);
        List<LivingEntity> targets = player.level().getEntitiesOfClass(LivingEntity.class, box,
                e -> e != player && e.isAlive() && !e.isSpectator());

        if (targets.size() > MAX_HITS_PER_PULSE) {
            targets = targets.subList(0, (int) MAX_HITS_PER_PULSE);
        }

        DamageSource source = player.damageSources().magic();
        for (LivingEntity target : targets) {
            target.hurt(source, TICK_DAMAGE);
        }
    }

    private static boolean hasGear(ServerPlayer player) {
        return player.getMainHandItem().getItem() instanceof BoostedGearItem
                || player.getOffhandItem().getItem() instanceof BoostedGearItem
                || (!player.getData(ModAttachments.GAUNTLET_SLOT).isEmpty()
                && player.getData(ModAttachments.GAUNTLET_SLOT).getItem() instanceof BoostedGearItem);
    }

    private static final class ActiveEffect {
        private final int radius;
        private int ticksLeft;
        private int pulseTicksLeft;

        private ActiveEffect(int radius, int ticksLeft, int pulseTicksLeft) {
            this.radius = radius;
            this.ticksLeft = ticksLeft;
            this.pulseTicksLeft = pulseTicksLeft;
        }
    }
}