package com.yourname.dxdweapons.sound;

import com.yourname.dxdweapons.DxDRayMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModSounds {
    private static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(Registries.SOUND_EVENT, DxDRayMod.MODID);

    public static final DeferredHolder<SoundEvent, SoundEvent> BOOST = SOUND_EVENTS.register("boosted_gear.boost",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(DxDRayMod.MODID, "boosted_gear.boost")));

    public static void register(IEventBus modEventBus) {
        SOUND_EVENTS.register(modEventBus);
    }
}
