package com.yourname.dxdweapons.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.yourname.dxdweapons.item.BoostedGearItem;
import com.yourname.dxdweapons.network.BoostPacket;
import com.yourname.dxdweapons.sound.ModSounds;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

import static com.yourname.dxdweapons.DxDRayMod.MODID;

@EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
public class ModKeyBindings {
    public static final KeyMapping BOOST_KEY = new KeyMapping(
            "key." + MODID + ".boost",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_V,
            "key.category." + MODID
    );
    public static final KeyMapping SPATIAL_KEY = new KeyMapping(
            "key." + MODID + ".spatial",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_R,
            "key.category." + MODID
    );
    public static final KeyMapping EQUIP_MODE_KEY = new KeyMapping(
            "key." + MODID + ".equip_mode",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_G,
            "key.category." + MODID
    );
    /** Bound to the middle mouse button directly instead of reusing vanilla's pick-block key. */
    public static final KeyMapping HOLY_LOCKER_KEY = new KeyMapping(
            "key." + MODID + ".holy_locker",
            InputConstants.Type.MOUSE,
            GLFW.GLFW_KEY_3,
            "key.category." + MODID
    );

    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(BOOST_KEY);
        event.register(SPATIAL_KEY);
        event.register(EQUIP_MODE_KEY);
        event.register(HOLY_LOCKER_KEY);
    }
}
