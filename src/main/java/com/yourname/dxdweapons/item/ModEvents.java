package com.yourname.dxdweapons.item;

import com.yourname.dxdweapons.attachment.ModAttachments;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

public class ModEvents {
    @SubscribeEvent
    static void onLivingIncomingDamage(LivingIncomingDamageEvent event) {
        if (event.getEntity().level().isClientSide) return;
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
            player.removeEffect(com.yourname.dxdweapons.effect.ModEffects.BOOSTED);
        }
    }
}