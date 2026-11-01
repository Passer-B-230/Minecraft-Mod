package com.example.client;

import com.example.ModEntities;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.renderer.entity.EntityRenderers;

public class TemplateModClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		EntityRenderers.register(ModEntities.THROWING_KNIFE, ThrowingKnifeRenderer::new);
		net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperties.ID_MAPPER.put(
				com.example.TemplateMod.id("pistol_aiming"), PistolAimingProperty.CODEC);
		net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperties.ID_MAPPER.put(
				com.example.TemplateMod.id("pistol_recoil"), PistolRecoilProperty.CODEC);
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.
	}
}
