package com.yourname.dxdweapons.item;

import com.yourname.dxdweapons.attachment.ModAttachments;
import com.yourname.dxdweapons.effect.ModEffects;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

import java.util.Set;

public class ModEvents {
    /**
     * Environmental damage you take from being shoved into the world. A dashing wielder should not
     * be punished for ploughing through a cactus, clipping a wall or running into lava.
     * Suffocation is covered by {@link DamageTypes#IN_WALL} in modern Minecraft.
     */
    private static final Set<ResourceKey<DamageType>> DASH_IMMUNE_DAMAGE = Set.of(
            DamageTypes.CACTUS,
            DamageTypes.IN_FIRE,
            DamageTypes.ON_FIRE,
            DamageTypes.LAVA,
            DamageTypes.FALL,
            DamageTypes.IN_WALL,
            DamageTypes.CRAMMING,
            DamageTypes.DROWN,
            DamageTypes.DRY_OUT,
            DamageTypes.FLY_INTO_WALL,
            DamageTypes.HOT_FLOOR
    );

    @SubscribeEvent
    static void onLivingIncomingDamage(LivingIncomingDamageEvent event) {
        if (event.getEntity().level().isClientSide) return;

        // Dash i-frames: cancel collision/environment damage while a dash is in progress.
        if (event.getEntity() instanceof ServerPlayer dashing
                && BoostedGearItem.isDashing(dashing.getUUID())
                && DASH_IMMUNE_DAMAGE.contains(event.getSource().type())) {
            event.setCanceled(true);
            return;
        }

        if (!(event.getSource().getEntity() instanceof Player player)) return;

        ItemStack mainHand = player.getMainHandItem();
        ItemStack offHand = player.getOffhandItem();
        boolean hasGauntlet = mainHand.getItem() instanceof BoostedGearItem
                || offHand.getItem() instanceof BoostedGearItem
                || (!player.getData(ModAttachments.GAUNTLET_SLOT).isEmpty()
                && player.getData(ModAttachments.GAUNTLET_SLOT).getItem() instanceof BoostedGearItem);
        if (!hasGauntlet) return;

        // Attacking consumes the boost, so the reach/speed modifiers and the per-player upkeep
        // state have to go too - removing just the effect used to leave the bonuses stuck on.
        if (player instanceof ServerPlayer serverPlayer) {
            BoostedGearItem.clearBoostState(serverPlayer);
        } else {
            player.removeEffect(ModEffects.BOOSTED);
        }
    }
}