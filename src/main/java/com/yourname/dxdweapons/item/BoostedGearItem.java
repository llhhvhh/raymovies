package com.yourname.dxdweapons.item;

import com.yourname.dxdweapons.attachment.ModAttachments;
import com.yourname.dxdweapons.effect.ModEffects;
import com.yourname.dxdweapons.network.EquipModeHandler;
import com.yourname.dxdweapons.sound.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.ItemAbility;
import net.neoforged.neoforge.common.ItemAbilities;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

public class BoostedGearItem extends Item {
    private static final Random RANDOM = new Random();
    private static final ResourceLocation REACH_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath("raymovies", "boosted_gear_reach");
    private static final ResourceLocation BASE_REACH_ID = ResourceLocation.fromNamespaceAndPath("raymovies", "boosted_gear_base_reach");
    private static final ResourceLocation BASE_DAMAGE_ID = ResourceLocation.fromNamespaceAndPath("raymovies", "boosted_gear_damage");
    private static final ResourceLocation BASE_SPEED_ID = ResourceLocation.fromNamespaceAndPath("raymovies", "boosted_gear_speed");
    private static final ResourceLocation BOOST_SPEED_ID = ResourceLocation.fromNamespaceAndPath("raymovies", "boosted_gear_boost_speed");
    private static final Map<UUID, Integer> BOOST_TRACKER = new HashMap<>();
    private static final String COOLDOWN_TAG = "cooldown_ticks";
    private static final String BOOST_LEVEL_TAG = "boost_level";
    private static final int MAX_AMPLIFIER = 9;
    private static final double THRUST_HITBOX_MARGIN = 0.125;
    private static final double THRUST_RANGE = 4.5;
    private static final int THRUST_COOLDOWN_TICKS = 20;
    private static final double DASH_SPEED = 0.8;
    private static final int DASH_TICKS = 12;
    /** Dash damage as a fraction of the wielder's attack damage attribute. */
    private static final float DASH_DAMAGE_SCALE = 1.0F;
    private static final double DASH_KNOCKBACK = 0.4;
    private static final Map<UUID, Integer> THRUST_READY_AT = new HashMap<>();
    private static final Map<UUID, DashState> DASH_STATES = new HashMap<>();
    private static final int BOOST_TICKS = 200;
    private static final double BOOST_REACH_BONUS = 3.0;
    private static final double BOOST_SPEED_BONUS = 0.4;

    public static final class DashState {
        private int remaining;
        private final double dirX;
        private final double dirZ;
        private final Set<Integer> alreadyHit = new HashSet<>();

        private DashState(int remaining, double dirX, double dirZ) {
            this.remaining = remaining;
            this.dirX = dirX;
            this.dirZ = dirZ;
        }

        private DashState step() {
            this.remaining--;
            return this;
        }
    }

    public static boolean isDashing(UUID uuid) {
        return DASH_STATES.containsKey(uuid);
    }

    public static void tickDash(ServerPlayer player) {
        DashState state = DASH_STATES.get(player.getUUID());
        if (state == null) return;
        DASH_STATES.remove(player.getUUID());

        Level level = player.level();
        Vec3 step = new Vec3(state.dirX * DASH_SPEED, 0.0, state.dirZ * DASH_SPEED);

        if (!level.noCollision(player, player.getBoundingBox().move(step))) {
            player.setIgnoreFallDamageFromCurrentImpulse(false);
            return;
        }

        Vec3 pos = player.position();
        player.teleportTo(pos.x + step.x, pos.y, pos.z + step.z);
        player.setDeltaMovement(step.x, player.getDeltaMovement().y, step.z);
        player.setIgnoreFallDamageFromCurrentImpulse(true);
        player.hasImpulse = true;
        player.hurtMarked = true;

        damagePassedThrough(player, state);

        if (state.remaining > 1) {
            DASH_STATES.put(player.getUUID(), state.step());
        } else {
            player.setIgnoreFallDamageFromCurrentImpulse(false);
        }
    }

    /** Hits every unit the dash slides through, each one at most once per dash. */
    private static void damagePassedThrough(ServerPlayer player, DashState state) {
        AABB box = player.getBoundingBox().inflate(THRUST_HITBOX_MARGIN);
        List<LivingEntity> targets = player.level().getEntitiesOfClass(LivingEntity.class, box,
                e -> e != player && e.isAlive() && !e.isSpectator());

        if (targets.isEmpty()) return;

        float damage = (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE) * DASH_DAMAGE_SCALE;
        DamageSource source = player.damageSources().playerAttack(player);
        boolean hitAny = false;

        for (LivingEntity target : targets) {
            if (!state.alreadyHit.add(target.getId())) continue;
            if (target.hurt(source, damage)) {
                hitAny = true;
                knockbackFromDash(player, target, state);
            }
        }

        if (hitAny) {
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.PLAYER_ATTACK_SWEEP, player.getSoundSource(), 0.7F, 1.3F);
        }
    }

    private static void knockbackFromDash(ServerPlayer player, LivingEntity target, DashState state) {
        double push = DASH_KNOCKBACK;
        Vec3 motion = new Vec3(state.dirX * push, 0.35, state.dirZ * push);
        target.hurtMarked = true;
        target.push(motion.x, motion.y, motion.z);
        target.hasImpulse = true;
    }

    public BoostedGearItem(Properties properties) {
        super(properties.attributes(ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE,
                        new AttributeModifier(BASE_DAMAGE_ID, 8.0, AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED,
                        new AttributeModifier(BASE_SPEED_ID, -2.4, AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ENTITY_INTERACTION_RANGE,
                        new AttributeModifier(BASE_REACH_ID, 1.5, AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.HAND)
                .build()));
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker instanceof ServerPlayer player) {
            BOOST_TRACKER.remove(player.getUUID());
            clearBoostModifiers(player);
            player.removeEffect(ModEffects.BOOSTED);
            setBoostLevel(stack, 0);
        } else if (attacker instanceof Player player) {
            player.removeEffect(ModEffects.BOOSTED);
            setBoostLevel(stack, 0);
        }
        if (!target.level().isClientSide) {
            if (RANDOM.nextFloat() < 0.3f) {
                target.setRemainingFireTicks(100);
            }
        }
        return true;
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return true;
    }

    @Override
    public boolean canAttackBlock(BlockState state, Level level, BlockPos pos, Player player) {
        return true;
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remainingUseDuration) {
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) {
            return InteractionResultHolder.sidedSuccess(stack, true);
        }
        return InteractionResultHolder.success(thrustAttack(level, player, stack));
    }

    private ItemStack thrustAttack(Level level, Player player, ItemStack stack) {
        UUID uuid = player.getUUID();
        Integer cd = THRUST_READY_AT.get(uuid);
        if (cd != null && cd > 0) {
            return stack;
        }

        // The forward dash is a transformed-mode ability only. The piercing jab itself still works
        // for everyone, so without equip mode this degrades to a plain thrust with no cooldown.
        boolean canDash = EquipModeHandler.isEquipMode(uuid);

        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        double reach = Math.min(player.getAttributeValue(Attributes.ENTITY_INTERACTION_RANGE), THRUST_RANGE);
        Vec3 end = eye.add(look.scale(reach));

        ClipContext clip = new ClipContext(eye, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player);
        if (level.clip(clip).getType() != HitResult.Type.MISS) {
            return stack;
        }

        if (canDash) {
            THRUST_READY_AT.put(uuid, THRUST_COOLDOWN_TICKS);
            DASH_STATES.put(uuid, new DashState(DASH_TICKS, look.x, look.z));
            player.setIgnoreFallDamageFromCurrentImpulse(true);
            player.hasImpulse = true;
            player.hurtMarked = true;
        }

        AABB search = player.getBoundingBox().expandTowards(look.scale(reach)).inflate(1.0, 0.5, 1.0);
        List<LivingEntity> candidates = level.getEntitiesOfClass(LivingEntity.class, search,
                e -> e != player && e.isAttackable() && !e.isSpectator());

        List<LivingEntity> targets = new ArrayList<>();
        List<Double> distances = new ArrayList<>();
        for (LivingEntity entity : candidates) {
            Optional<Vec3> point = entity.getBoundingBox().inflate(THRUST_HITBOX_MARGIN).clip(eye, end);
            if (point.isPresent()) {
                targets.add(entity);
                distances.add(eye.distanceTo(point.get()));
            }
        }

        List<Integer> order = new ArrayList<>();
        for (int i = 0; i < targets.size(); i++) order.add(i);
        order.sort(Comparator.comparingDouble(distances::get));

        float damage = (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE);
        DamageSource source = player.damageSources().playerAttack(player);
        boolean hitAny = false;

        for (int index : order) {
            LivingEntity target = targets.get(index);
            if (target.hurt(source, damage)) {
                hurtEnemy(stack, target, player);
                postHurtEnemy(stack, target, player);
                hitAny = true;
            }
        }

        if (hitAny) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.PLAYER_ATTACK_SWEEP, player.getSoundSource(), 1.0F, 1.0F);
        }
        return stack;
    }

    public InteractionResultHolder<ItemStack> triggerBoost(Level level, Player player, ItemStack stack) {
        if (isOnCooldown(level, stack)) {
            return InteractionResultHolder.fail(stack);
        }

        int boostLevel = Math.min(getBoostLevelStatic(stack) + 1, MAX_AMPLIFIER + 1);
        setBoostLevel(stack, boostLevel);
        player.addEffect(new MobEffectInstance(ModEffects.BOOSTED, BOOST_TICKS, boostLevel - 1, false, true));

        applyBoostModifiers(player);
        player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, BOOST_TICKS, 0, false, true));

        BOOST_TRACKER.put(player.getUUID(), BOOST_TICKS);

        level.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.BOOST.get(), SoundSource.PLAYERS, 1.0F, 1.0F);

        int cd;
        if (player.experienceLevel < 10) {
            cd = 200;
        } else {
            int reductions = (player.experienceLevel - 10) / 5;
            cd = Math.max(10, 80 - reductions * 10);
        }
        setCooldown(stack, cd);
        return InteractionResultHolder.success(stack);
    }

    private static void applyBoostModifiers(LivingEntity player) {
        AttributeInstance reach = player.getAttribute(Attributes.ENTITY_INTERACTION_RANGE);
        if (reach != null) {
            reach.removeModifier(REACH_MODIFIER_ID);
            reach.addTransientModifier(new AttributeModifier(REACH_MODIFIER_ID, BOOST_REACH_BONUS,
                    AttributeModifier.Operation.ADD_VALUE));
        }
        AttributeInstance speed = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null) {
            speed.removeModifier(BOOST_SPEED_ID);
            speed.addTransientModifier(new AttributeModifier(BOOST_SPEED_ID, BOOST_SPEED_BONUS,
                    AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }
    }

    private static void clearBoostModifiers(LivingEntity player) {
        AttributeInstance reach = player.getAttribute(Attributes.ENTITY_INTERACTION_RANGE);
        if (reach != null) {
            reach.removeModifier(REACH_MODIFIER_ID);
        }
        AttributeInstance speed = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null) {
            speed.removeModifier(BOOST_SPEED_ID);
        }
    }

    /**
     * Per-player boost upkeep. Runs for every player every tick, so it keeps working no matter
     * where the gauntlet is stored (hand, offhand, or the {@code ModAttachments.GAUNTLET_SLOT}
     * attachment, which never receives {@code inventoryTick}).
     */
    public static void tickBoost(ServerPlayer player) {
        UUID uuid = player.getUUID();

        Integer cd = THRUST_READY_AT.get(uuid);
        if (cd != null) {
            if (cd <= 1) {
                THRUST_READY_AT.remove(uuid);
            } else {
                THRUST_READY_AT.put(uuid, cd - 1);
            }
        }

        Integer remaining = BOOST_TRACKER.get(uuid);
        if (remaining == null) {
            tickCooldowns(player);
            return;
        }

        if (remaining <= 1) {
            BOOST_TRACKER.remove(uuid);
            clearBoostModifiers(player);
            player.removeEffect(ModEffects.BOOSTED);
        } else {
            BOOST_TRACKER.put(uuid, remaining - 1);
        }

        tickCooldowns(player);
    }

    private static void tickCooldowns(ServerPlayer player) {
        tickCooldown(player.getMainHandItem());
        tickCooldown(player.getOffhandItem());
        tickCooldown(player.getData(ModAttachments.GAUNTLET_SLOT));
    }

    public static void clearBoostState(ServerPlayer player) {
        UUID uuid = player.getUUID();
        if (BOOST_TRACKER.remove(uuid) != null) {
            clearBoostModifiers(player);
            player.removeEffect(ModEffects.BOOSTED);
        }
        THRUST_READY_AT.remove(uuid);
        DASH_STATES.remove(uuid);
    }

    private static void tickCooldown(ItemStack stack) {
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
        int level = getBoostLevelStatic(stack);
        int cd = getCooldownTicks(stack);
        tooltipComponents.add(Component.literal("Boost: " + level + "/" + (MAX_AMPLIFIER + 1)).withStyle(style -> style.withColor(0x55FF55)));
        if (cd > 0) {
            tooltipComponents.add(Component.literal("Cooldown: " + (cd / 20.0F) + "s").withStyle(style -> style.withColor(0xFFAA00)));
        }
    }

    public static int getBoostLevelStatic(ItemStack stack) {
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

    private static final Set<ItemAbility> PICKAXE_ACTIONS = Set.of(ItemAbilities.PICKAXE_DIG);
    private static final Set<ItemAbility> AXE_ACTIONS = Set.of(ItemAbilities.AXE_DIG, ItemAbilities.AXE_STRIP, ItemAbilities.AXE_SCRAPE, ItemAbilities.AXE_WAX_OFF);
    private static final Set<ItemAbility> SHOVEL_ACTIONS = Set.of(ItemAbilities.SHOVEL_DIG, ItemAbilities.SHOVEL_FLATTEN);
    private static final Set<ItemAbility> HOE_ACTIONS = Set.of(ItemAbilities.HOE_DIG, ItemAbilities.HOE_TILL);

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        ToolMode mode = getToolModeStatic(stack);
        if (mode == ToolMode.NONE) return super.getDestroySpeed(stack, state);
        float speed = Tiers.NETHERITE.getSpeed();
        if (mode == ToolMode.SHOVEL) speed = 6.5f;
        if (mode == ToolMode.HOE) speed = 0f;
        if (state.is(BlockTags.MINEABLE_WITH_PICKAXE) && mode == ToolMode.PICKAXE) return speed;
        if (state.is(BlockTags.MINEABLE_WITH_AXE) && mode == ToolMode.AXE) return speed;
        if (state.is(BlockTags.MINEABLE_WITH_SHOVEL) && mode == ToolMode.SHOVEL) return speed;
        if (state.is(BlockTags.MINEABLE_WITH_HOE) && mode == ToolMode.HOE) return speed;
        return 1.0f;
    }

    @Override
    public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
        ToolMode mode = getToolModeStatic(stack);
        if (mode == ToolMode.NONE) return super.isCorrectToolForDrops(stack, state);
        if (state.is(BlockTags.MINEABLE_WITH_PICKAXE) && mode == ToolMode.PICKAXE) return true;
        if (state.is(BlockTags.MINEABLE_WITH_AXE) && mode == ToolMode.AXE) return true;
        if (state.is(BlockTags.MINEABLE_WITH_SHOVEL) && mode == ToolMode.SHOVEL) return true;
        if (state.is(BlockTags.MINEABLE_WITH_HOE) && mode == ToolMode.HOE) return true;
        return false;
    }

    @Override
    public boolean canPerformAction(ItemStack stack, ItemAbility itemAbility) {
        ToolMode mode = getToolModeStatic(stack);
        return switch (mode) {
            case PICKAXE -> PICKAXE_ACTIONS.contains(itemAbility);
            case AXE -> AXE_ACTIONS.contains(itemAbility);
            case SHOVEL -> SHOVEL_ACTIONS.contains(itemAbility);
            case HOE -> HOE_ACTIONS.contains(itemAbility);
            case NONE -> false;
        };
    }

    public static ToolMode getToolModeStatic(ItemStack stack) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData != null) {
            CompoundTag tag = customData.copyTag();
            if (tag.contains("tool_mode")) {
                try {
                    return ToolMode.valueOf(tag.getString("tool_mode"));
                } catch (IllegalArgumentException e) {
                    return ToolMode.NONE;
                }
            }
        }
        return ToolMode.NONE;
    }

    public static void setToolMode(ItemStack stack, ToolMode mode) {
        CustomData existing = stack.get(DataComponents.CUSTOM_DATA);
        CompoundTag tag = existing != null ? existing.copyTag() : new CompoundTag();
        tag.putString("tool_mode", mode.name());
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        ToolMode mode = getToolModeStatic(context.getItemInHand());
        if (mode == ToolMode.NONE) return InteractionResult.PASS;
        Level level = context.getLevel();
        net.minecraft.core.BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        Player player = context.getPlayer();

        if (mode == ToolMode.SHOVEL) {
            if (state.is(Blocks.GRASS_BLOCK)) {
                level.playSound(player, pos, SoundEvents.SHOVEL_FLATTEN, SoundSource.BLOCKS, 1.0F, 1.0F);
                if (!level.isClientSide) {
                    level.setBlock(pos, Blocks.DIRT_PATH.defaultBlockState(), 11);
                }
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
        }
        if (mode == ToolMode.HOE) {
            BlockState tilled = null;
            if (state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.DIRT) || state.is(Blocks.DIRT_PATH)) {
                tilled = Blocks.FARMLAND.defaultBlockState();
            } else if (state.is(Blocks.COARSE_DIRT)) {
                tilled = Blocks.DIRT.defaultBlockState();
            } else if (state.is(Blocks.ROOTED_DIRT)) {
                tilled = Blocks.DIRT.defaultBlockState();
                if (!level.isClientSide) {
                    Block.popResource(level, pos, new ItemStack(net.minecraft.world.item.Items.HANGING_ROOTS));
                }
            }
            if (tilled != null) {
                level.playSound(player, pos, SoundEvents.HOE_TILL, SoundSource.BLOCKS, 1.0F, 1.0F);
                if (!level.isClientSide) {
                    level.setBlock(pos, tilled, 11);
                }
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
        }
        return InteractionResult.PASS;
    }
}
