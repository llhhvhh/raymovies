package com.yourname.dxdweapons.client;

import com.yourname.dxdweapons.DxDRayMod;
import com.yourname.dxdweapons.entity.TraderEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;

public class TraderRenderer extends HumanoidMobRenderer<TraderEntity, HumanoidModel<TraderEntity>> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            DxDRayMod.MODID, "textures/entity/trader.png");

    public TraderRenderer(EntityRendererProvider.Context context) {
        // trader.png is a 64x64 player skin, so the PLAYER layer is the UV-correct match.
        // Harmless here: the extra hat part samples a transparent region and TraderEntity can
        // never become a baby (getBreedOffspring returns null), so no child scaling occurs.
        super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER)), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(TraderEntity entity) {
        return TEXTURE;
    }
}