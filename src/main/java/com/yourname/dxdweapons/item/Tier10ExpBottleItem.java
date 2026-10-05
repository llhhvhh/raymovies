package com.yourname.dxdweapons.item;

import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * A bottle that grants 10 experience levels outright instead of dropping XP orbs.
 * Handy for reaching the level 10 gate that unlocks flight, blessing and the spatial aura.
 */
public class Tier10ExpBottleItem extends Item {
    public static final int LEVELS = 10;

    public Tier10ExpBottleItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide) {
            player.giveExperienceLevels(LEVELS);
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0F, 1.6F);
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("使用后立即获得 " + LEVELS + " 级经验")
                .withStyle(style -> style.withColor(0x55FF55)));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }
}