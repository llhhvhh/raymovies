package com.yourname.dxdweapons.effect;

import com.yourname.dxdweapons.DxDRayMod;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModEffects {
    private static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, DxDRayMod.MODID);

    public static final Holder<MobEffect> BOOSTED = EFFECTS.register("boosted",
            () -> new BoostedEffect(MobEffectCategory.BENEFICIAL, 0xCC0000)
                    .addAttributeModifier(Attributes.ATTACK_DAMAGE,
                            ResourceLocation.fromNamespaceAndPath(DxDRayMod.MODID, "boosted_attack"),
                            4.0, AttributeModifier.Operation.ADD_VALUE));

    public static final Holder<MobEffect> BLESSING = EFFECTS.register("blessing",
            () -> new BlessedEffect(MobEffectCategory.BENEFICIAL, 0xFFD700));

    public static final Holder<MobEffect> GREEN_BALL_BOOST = EFFECTS.register("green_ball_boost",
            () -> new GreenBallBoostEffect(MobEffectCategory.BENEFICIAL, 0x00CC00)
                    .addAttributeModifier(Attributes.ATTACK_DAMAGE,
                            ResourceLocation.fromNamespaceAndPath(DxDRayMod.MODID, "green_ball_boost_attack"),
                            3.0, AttributeModifier.Operation.ADD_VALUE));

    public static void register(IEventBus modEventBus) {
        EFFECTS.register(modEventBus);
    }
}
