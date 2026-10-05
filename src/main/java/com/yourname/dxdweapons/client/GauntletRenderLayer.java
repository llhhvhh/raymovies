package com.yourname.dxdweapons.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
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
    // Tunables: the item model spans a full 1x1x1 cube (16 px) with its origin at the bottom corner.
    // ARM_DOWN_RA  - how far down the 12 px arm the gauntlet sits (0 = shoulder, ~1 = wrist)
    // WIDTH_RATIO  - x/z size; the arm is ~4 px wide
    // LENGTH_RATIO - y size; the bracer should span most of the forearm
    private static final float ARM_DOWN = 0.34F;
    private static final float WIDTH_RATIO = 0.30F;
    private static final float LENGTH_RATIO = 0.44F;

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
        // Puts us in arm space: origin at the shoulder, the arm running down -Y.
        getParentModel().rightArm.translateAndRotate(poseStack);

        poseStack.translate(0.0F, -ARM_DOWN, 0.0F);
        // The texture is drawn fist-up, cuff-down; flip it so the fist sits at the hand end.
        poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
        poseStack.scale(WIDTH_RATIO, LENGTH_RATIO, WIDTH_RATIO);
        poseStack.translate(-0.5F, -0.5F, -0.5F);

        // NONE so only the manual transform above applies - a display context would fight it.
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