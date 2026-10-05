package com.yourname.dxdweapons.network;

import com.yourname.dxdweapons.DxDRayMod;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = DxDRayMod.MODID)
public class ModMessages {
    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(BoostPacket.TYPE, BoostPacket.STREAM_CODEC, BoostPacket::handle);
        registrar.playToServer(GauntletSlotPacket.TYPE, GauntletSlotPacket.STREAM_CODEC, GauntletSlotPacket::handle);
        registrar.playToClient(GauntletSyncPacket.TYPE, GauntletSyncPacket.STREAM_CODEC, GauntletSyncPacket::handle);
        registrar.playToServer(SpatialPacket.TYPE, SpatialPacket.STREAM_CODEC, SpatialPacket::handle);
        registrar.playToServer(EquipModePacket.TYPE, EquipModePacket.STREAM_CODEC, EquipModePacket::handle);
        registrar.playToServer(FlyPacket.TYPE, FlyPacket.STREAM_CODEC, FlyPacket::handle);
        registrar.playToServer(ToolModePacket.TYPE, ToolModePacket.STREAM_CODEC, ToolModePacket::handle);
        registrar.playToServer(GreenBallBoostPacket.TYPE, GreenBallBoostPacket.STREAM_CODEC, GreenBallBoostPacket::handle);
    }
}
