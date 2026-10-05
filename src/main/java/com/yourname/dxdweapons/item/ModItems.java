package com.yourname.dxdweapons.item;

import com.yourname.dxdweapons.DxDRayMod;
import com.yourname.dxdweapons.entity.ModEntities;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SpawnEggItem;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(DxDRayMod.MODID);

    public static final DeferredItem<BoostedGearItem> BOOSTED_GEAR = ITEMS.register("boosted_gear",
            () -> new BoostedGearItem(new Item.Properties()
                    .stacksTo(1)
                    .rarity(Rarity.EPIC)
                    .fireResistant()));

    public static final DeferredItem<ChessPawnItem> CHESS_PAWN = ITEMS.register("chess_pawn",
            () -> new ChessPawnItem(new Item.Properties()
                    .stacksTo(16)));

    public static final DeferredItem<GreenBallItem> GREEN_BALL = ITEMS.register("green_ball",
            () -> new GreenBallItem(new Item.Properties()
                    .stacksTo(1)
                    .fireResistant()));

    public static final DeferredItem<SpawnEggItem> TRADER_SPAWN_EGG = ITEMS.register("trader_spawn_egg",
            () -> new SpawnEggItem(ModEntities.TRADER.get(), 0x8B5E34, 0xE8B88A,
                    new Item.Properties()));

    public static final DeferredItem<Tier10ExpBottleItem> TIER10_EXP_BOTTLE =
            ITEMS.register("tier10_exp_bottle",
                    () -> new Tier10ExpBottleItem(new Item.Properties().stacksTo(16)));

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}
