package com.example.client;

import com.example.entity.ThrowingKnifeEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.ThrownItemRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemDisplayContext;

public class ThrowingKnifeRenderer extends EntityRenderer<ThrowingKnifeEntity, ThrowingKnifeRenderState> {
	private final ItemModelResolver itemModelResolver;

	public ThrowingKnifeRenderer(EntityRendererProvider.Context context) {
		super(context);
		this.itemModelResolver = context.getItemModelResolver();
	}

	@Override
	protected int getBlockLightLevel(ThrowingKnifeEntity entity, BlockPos blockPos) {
		return super.getBlockLightLevel(entity, blockPos);
	}

	@Override
	public void submit(ThrowingKnifeRenderState state, PoseStack poseStack,
			SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
		poseStack.pushPose();
		poseStack.rotateDegrees(Axis.YP, state.yRot - 90.0F);
		poseStack.rotateDegrees(Axis.ZP, state.xRot);
		state.item.submit(poseStack, submitNodeCollector, state.lightCoords,
				OverlayTexture.NO_OVERLAY, state.outlineColor);
		poseStack.popPose();
		super.submit(state, poseStack, submitNodeCollector, camera);
	}

	@Override
	public ThrowingKnifeRenderState createRenderState() {
		return new ThrowingKnifeRenderState();
	}

	@Override
	public void extractRenderState(ThrowingKnifeEntity entity, ThrowingKnifeRenderState state, float partialTicks) {
		super.extractRenderState(entity, state, partialTicks);
		state.xRot = entity.getXRot(partialTicks);
		state.yRot = entity.getYRot(partialTicks);
		this.itemModelResolver.updateForNonLiving(state.item, entity.getItem(), ItemDisplayContext.GROUND, entity);
	}
}
