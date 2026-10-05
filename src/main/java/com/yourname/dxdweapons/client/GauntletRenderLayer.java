package com.yourname.dxdweapons.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.yourname.dxdweapons.attachment.ModAttachments;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
public class GauntletRenderLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    private final ItemRenderer itemRenderer;

    public GauntletRenderLayer(LivingEntityRenderer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> renderer, ItemRenderer itemRenderer) {
        super(renderer);
        this.itemRenderer = itemRenderer;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, AbstractClientPlayer player, float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        ItemStack gauntlet = player.getData(ModAttachments.GAUNTLET_SLOT);
        if (gauntlet.isEmpty()) return;

        poseStack.pushPose();
        getParentModel().rightArm.translateAndRotate(poseStack);
        poseStack.translate(0.15, 0.0, 0.0);
        poseStack.scale(0.25f, 0.25f, 0.25f);
        itemRenderer.renderStatic(player, gauntlet, ItemDisplayContext.NONE, false, poseStack, buffer, player.level(), packedLight, OverlayTexture.NO_OVERLAY, player.getId());
        poseStack.popPose();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void registerAll(EntityRenderersEvent.AddLayers event) {
        var itemRenderer = Minecraft.getInstance().getItemRenderer();
        for (var skin : event.getSkins()) {
            var renderer = event.getSkin(skin);
            if (renderer instanceof LivingEntityRenderer living) {
                living.addLayer(new GauntletRenderLayer(
                    (LivingEntityRenderer) living,
                    itemRenderer
                ));
            }
        }
    }
}