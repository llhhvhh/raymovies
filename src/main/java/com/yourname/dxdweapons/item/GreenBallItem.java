package com.yourname.dxdweapons.item;

import com.yourname.dxdweapons.effect.ModEffects;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Random;

public class GreenBallItem extends SwordItem {
    private static final Random RANDOM = new Random();
    private static final String BOOST_LEVEL_TAG = "green_ball_boost_level";
    private static final String COOLDOWN_TAG = "green_ball_cooldown";
    private static final int MAX_BOOST = 5;
    private static final int BOOST_PER_LAYER = 3;

    public GreenBallItem(Properties properties) {
        super(Tiers.DIAMOND, properties);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker instanceof Player player) {
            int boostLevel = getBoostLevel(stack);
            if (boostLevel > 0) {
                int newLevel = boostLevel - 1;
                setBoostLevel(stack, newLevel);
                player.removeEffect(ModEffects.GREEN_BALL_BOOST);
                if (newLevel > 0) {
                    applyBoostEffect(player, newLevel);
                }
            }
        }
        if (!target.level().isClientSide) {
            if (RANDOM.nextFloat() < 0.2f) {
                target.setRemainingFireTicks(80);
            }
        }
        return super.hurtEnemy(stack, target, attacker);
    }

    /**
     * The effect grants {@code BOOST_PER_LAYER * (amplifier + 1)} attack damage, so the amplifier has
     * to track the stored boost level - otherwise the bonus would stay at one layer forever.
     */
    private static void applyBoostEffect(Player player, int boostLevel) {
        player.addEffect(new MobEffectInstance(ModEffects.GREEN_BALL_BOOST, -1,
                Math.max(0, boostLevel - 1), false, true));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            return triggerBoost(level, player, stack);
        }
        return InteractionResultHolder.sidedSuccess(stack, true);
    }

    public InteractionResultHolder<ItemStack> triggerBoost(Level level, Player player, ItemStack stack) {
        if (isOnCooldown(level, stack)) {
            return InteractionResultHolder.fail(stack);
        }

        int currentBoost = getBoostLevel(stack);
        if (currentBoost >= MAX_BOOST) {
            return InteractionResultHolder.fail(stack);
        }

        int newBoost = currentBoost + 1;
        setBoostLevel(stack, newBoost);

        player.removeEffect(ModEffects.GREEN_BALL_BOOST);
        applyBoostEffect(player, newBoost);

        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 100, 0, false, true));

        int cd = Math.max(10, 40 - currentBoost * 5);
        setCooldown(stack, cd);

        return InteractionResultHolder.success(stack);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (level.isClientSide) return;
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) return;
        CompoundTag tag = customData.copyTag();
        if (!tag.contains(COOLDOWN_TAG)) return;
        int remaining = tag.getInt(COOLDOWN_TAG);
        if (remaining <= 0) {
            tag.remove(COOLDOWN_TAG);
        } else {
            tag.putInt(COOLDOWN_TAG, remaining - 1);
        }
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        int boost = getBoostLevel(stack);
        tooltipComponents.add(Component.literal("Boost: " + boost + "/" + MAX_BOOST + " (+" + (boost * BOOST_PER_LAYER) + " 攻击力)")
                .withStyle(style -> style.withColor(0x55FF55)));
        int cd = getCooldownTicks(stack);
        if (cd > 0) {
            tooltipComponents.add(Component.literal("Cooldown: " + (cd / 20.0F) + "s")
                    .withStyle(style -> style.withColor(0xFFAA00)));
        }
    }

    public static int getBoostLevel(ItemStack stack) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData != null) {
            CompoundTag tag = customData.copyTag();
            if (tag.contains(BOOST_LEVEL_TAG)) {
                return tag.getInt(BOOST_LEVEL_TAG);
            }
        }
        return 0;
    }

    private static void setBoostLevel(ItemStack stack, int level) {
        CustomData existing = stack.get(DataComponents.CUSTOM_DATA);
        CompoundTag tag = existing != null ? existing.copyTag() : new CompoundTag();
        tag.putInt(BOOST_LEVEL_TAG, level);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    public boolean isOnCooldown(Level level, ItemStack stack) {
        return getCooldownTicks(stack) > 0;
    }

    private static int getCooldownTicks(ItemStack stack) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData != null) {
            CompoundTag tag = customData.copyTag();
            if (tag.contains(COOLDOWN_TAG)) {
                return tag.getInt(COOLDOWN_TAG);
            }
        }
        return 0;
    }

    private static void setCooldown(ItemStack stack, int duration) {
        CustomData existing = stack.get(DataComponents.CUSTOM_DATA);
        CompoundTag tag = existing != null ? existing.copyTag() : new CompoundTag();
        tag.putInt(COOLDOWN_TAG, duration);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }
}
