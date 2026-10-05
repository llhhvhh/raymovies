package com.yourname.dxdweapons;

import com.yourname.dxdweapons.attachment.ModAttachments;
import com.yourname.dxdweapons.client.ClientEvents;
import com.yourname.dxdweapons.client.FirstPersonGauntletRender;
import com.yourname.dxdweapons.client.GauntletRenderLayer;
import com.yourname.dxdweapons.client.BoostLevelOverlay;
import com.yourname.dxdweapons.client.GauntletSlotHandler;
import com.yourname.dxdweapons.client.NoCullBakedModel;
import com.yourname.dxdweapons.client.TraderRenderer;
import com.yourname.dxdweapons.effect.ModEffects;
import com.yourname.dxdweapons.entity.ModEntities;
import com.yourname.dxdweapons.event.ServerEvents;
import com.yourname.dxdweapons.item.ModEvents;
import com.yourname.dxdweapons.item.ModItems;
import com.yourname.dxdweapons.item.ZombieDropHandler;
import com.yourname.dxdweapons.network.SpatialEffectHandler;
import com.yourname.dxdweapons.sound.ModSounds;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

import java.util.function.Supplier;

@Mod(DxDRayMod.MODID)
public class DxDRayMod {
    public static final String MODID = "raymovies";
    public static final Logger LOGGER = LogUtils.getLogger();

    private static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public static final Supplier<CreativeModeTab> DXD_TAB = CREATIVE_MODE_TABS.register("dxd_tab",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup." + MODID))
                    .withTabsBefore(CreativeModeTabs.COMBAT)
                    .icon(() -> new ItemStack(ModItems.BOOSTED_GEAR.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.BOOSTED_GEAR.get());
                        output.accept(ModItems.CHESS_PAWN.get());
                        output.accept(ModItems.GREEN_BALL.get());
                        output.accept(ModItems.TRADER_SPAWN_EGG.get());
                    })
                    .build());

    public DxDRayMod(IEventBus modEventBus, ModContainer modContainer) {
        ModItems.register(modEventBus);
        ModEffects.register(modEventBus);
        ModSounds.register(modEventBus);
        ModAttachments.register(modEventBus);
        ModEntities.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);

        NeoForge.EVENT_BUS.register(ModEvents.class);
        NeoForge.EVENT_BUS.register(ServerEvents.class);
        NeoForge.EVENT_BUS.register(ZombieDropHandler.class);
        NeoForge.EVENT_BUS.register(SpatialEffectHandler.class);

        // ClientEvents and GauntletSlotHandler use @EventBusSubscriber and must NOT be registered here,
// otherwise their handlers would run twice.
        if (FMLEnvironment.dist == Dist.CLIENT) {
            NeoForge.EVENT_BUS.register(FirstPersonGauntletRender.class);
            NeoForge.EVENT_BUS.register(BoostLevelOverlay.class);
            modEventBus.addListener(GauntletRenderLayer::registerAll);
            modEventBus.addListener(DxDRayMod::onModifyBakingResult);
            modEventBus.addListener(DxDRayMod::onRegisterEntityRenderers);
        }
    }

    private static void onRegisterEntityRenderers(net.neoforged.neoforge.client.event.EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.TRADER.get(), TraderRenderer::new);
    }

    private static void onModifyBakingResult(ModelEvent.ModifyBakingResult event) {
        var models = event.getModels();
        ModelResourceLocation key = ModelResourceLocation.standalone(
                ResourceLocation.fromNamespaceAndPath(MODID, "boosted_gear"));
        BakedModel original = models.get(key);
        if (original != null) {
            models.put(key, new NoCullBakedModel(original));
            LOGGER.info("Replaced boosted_gear model with NoCullBakedModel");
        } else {
            LOGGER.warn("Could not find boosted_gear model for key: {}", key);
        }
    }
}
