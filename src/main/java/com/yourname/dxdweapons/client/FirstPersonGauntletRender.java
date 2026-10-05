package com.yourname.dxdweapons.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.yourname.dxdweapons.attachment.ModAttachments;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderHandEvent;
public class FirstPersonGauntletRender {
    @SubscribeEvent
    public static void onRenderHand(RenderHandEvent event) {
        if (event.getHand() != InteractionHand.MAIN_HAND) return;

        var player = Minecraft.getInstance().player;
        if (player == null) return;
        ItemStack gauntlet = player.getData(ModAttachments.GAUNTLET_SLOT);
        if (gauntlet.isEmpty()) return;

        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource buffer = event.getMultiBufferSource();
        int packedLight = event.getPackedLight();
        float swing = event.getSwingProgress();
        float equip = event.getEquipProgress();

        poseStack.pushPose();

        // replicate arm transform from renderPlayerArm in ItemInHandRenderer
        float f = 1.0F;
        float f1 = Mth.sqrt(swing);
        float f2 = -0.3F * Mth.sin(f1 * (float) Math.PI);
        float f3 = 0.4F * Mth.sin(f1 * (float) (Math.PI * 2.0));
        float f4 = -0.4F * Mth.sin(swing * (float) Math.PI);
        poseStack.translate(f * (f2 + 0.64000005F), f3 + -0.6F + equip * -0.6F, f4 + -0.71999997F);
        poseStack.mulPose(Axis.YP.rotationDegrees(f * 45.0F));
        float f5 = Mth.sin(swing * swing * (float) Math.PI);
        float f6 = Mth.sin(f1 * (float) Math.PI);
        poseStack.mulPose(Axis.YP.rotationDegrees(f * f6 * 70.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(f * f5 * -20.0F));

        // offset from shoulder pivot to forearm
        poseStack.translate(0.0F, 0.5F, 0.0F);
        poseStack.scale(0.6f, 0.6f, 0.6f);

        Minecraft.getInstance().getItemRenderer().renderStatic(
                player, gauntlet, ItemDisplayContext.NONE, false,
                poseStack, buffer, player.level(),
                packedLight, OverlayTexture.NO_OVERLAY, player.getId());

        poseStack.popPose();
    }
}
