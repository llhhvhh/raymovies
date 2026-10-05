package com.yourname.dxdweapons.attachment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.yourname.dxdweapons.DxDRayMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.Optional;
import java.util.function.Supplier;

public class ModAttachments {
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, DxDRayMod.MODID);

    public static final Supplier<AttachmentType<ItemStack>> GAUNTLET_SLOT = ATTACHMENT_TYPES.register("gauntlet_slot",
            () -> AttachmentType.builder(() -> ItemStack.EMPTY)
                    .serialize(ItemStack.OPTIONAL_CODEC)
                    .copyOnDeath()
                    .build());

    public static final Supplier<AttachmentType<Optional<PlayerPos>>> HOLY_LOCKER_ENTRY = ATTACHMENT_TYPES.register("holy_locker_entry",
            () -> AttachmentType.<Optional<PlayerPos>>builder(() -> Optional.empty())
                    .serialize(Codec.optionalField("pos", PlayerPos.CODEC, true).codec())
                    .build());

    public record PlayerPos(ResourceKey<Level> dimension, double x, double y, double z, float yaw, float pitch) {
        public static final Codec<PlayerPos> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                ResourceKey.codec(Registries.DIMENSION).fieldOf("dimension").forGetter(PlayerPos::dimension),
                Codec.DOUBLE.fieldOf("x").forGetter(PlayerPos::x),
                Codec.DOUBLE.fieldOf("y").forGetter(PlayerPos::y),
                Codec.DOUBLE.fieldOf("z").forGetter(PlayerPos::z),
                Codec.FLOAT.fieldOf("yaw").forGetter(PlayerPos::yaw),
                Codec.FLOAT.fieldOf("pitch").forGetter(PlayerPos::pitch)
        ).apply(inst, PlayerPos::new));
    }

    public static void register(IEventBus bus) {
        ATTACHMENT_TYPES.register(bus);
    }
}
