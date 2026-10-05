package com.yourname.dxdweapons.item;

import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;

public class ZombieDropHandler {
    @SubscribeEvent
    static void onLivingDrops(LivingDropsEvent event) {
        if (event.getEntity().level().isClientSide) return;
        if (event.getEntity() instanceof Zombie && event.getSource().getEntity() != null) {
            if (event.getEntity().getRandom().nextFloat() < 0.01f) {
                event.getDrops().add(new ItemEntity(event.getEntity().level(),
                        event.getEntity().getX(), event.getEntity().getY(), event.getEntity().getZ(),
                        new ItemStack(ModItems.CHESS_PAWN.get())));
            }
        }
    }
}
