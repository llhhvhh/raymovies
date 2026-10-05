package com.yourname.dxdweapons.entity;

import com.yourname.dxdweapons.DxDRayMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModEntities {
    private static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, DxDRayMod.MODID);

    public static final Supplier<EntityType<TraderEntity>> TRADER = ENTITY_TYPES.register("trader",
            () -> EntityType.Builder.of(TraderEntity::new, MobCategory.CREATURE)
                    .sized(0.6f, 1.95f)
                    .clientTrackingRange(10)
                    .build("trader"));

    public static AttributeSupplier createTraderAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 0.5)
                .build();
    }

    public static void register(IEventBus bus) {
        ENTITY_TYPES.register(bus);
        bus.addListener(ModEntities::onRegisterAttributes);
    }

    private static void onRegisterAttributes(EntityAttributeCreationEvent event) {
        event.put(TRADER.get(), createTraderAttributes());
    }
}