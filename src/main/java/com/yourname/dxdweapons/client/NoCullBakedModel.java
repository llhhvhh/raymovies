package com.yourname.dxdweapons.client;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.model.BakedModelWrapper;

import java.util.List;

/**
 * Renders an item model with culling disabled. The boosted_gear model is an {@code item/generated}
 * sprite, so it has to be drawn with the item atlas and the entity vertex format - binding it to the
 * block atlas with {@link DefaultVertexFormat#BLOCK} would sample the wrong texture region.
 */
public class NoCullBakedModel extends BakedModelWrapper<BakedModel> {
    private static final ResourceLocation ITEM_ATLAS =
            ResourceLocation.withDefaultNamespace("textures/atlas/items.png");

    private static RenderType createNoCullRenderType(boolean fabulous) {
        RenderType.CompositeState state = RenderType.CompositeState.builder()
                .setShaderState(RenderType.RENDERTYPE_ENTITY_CUTOUT_SHADER)
                .setTextureState(new RenderStateShard.TextureStateShard(ITEM_ATLAS, false, false))
                .setTransparencyState(RenderType.NO_TRANSPARENCY)
                .setCullState(RenderType.NO_CULL)
                .setLightmapState(RenderType.LIGHTMAP)
                .setOverlayState(RenderType.OVERLAY)
                .createCompositeState(fabulous);

        return RenderType.create(
                "raymovies_nocull",
                DefaultVertexFormat.NEW_ENTITY,
                VertexFormat.Mode.QUADS,
                1536,
                false,
                false,
                state
        );
    }

    public NoCullBakedModel(BakedModel originalModel) {
        super(originalModel);
    }

    @Override
    public List<RenderType> getRenderTypes(ItemStack itemStack, boolean fabulous) {
        return List.of(createNoCullRenderType(fabulous));
    }

    @Override
    public List<BakedModel> getRenderPasses(ItemStack itemStack, boolean fabulous) {
        return List.of(this);
    }
}