package com.example.client;

import com.mojang.serialization.MapCodec;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperty;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public record PistolRecoilProperty() implements ConditionalItemModelProperty {
	public static final MapCodec<PistolRecoilProperty> CODEC = MapCodec.unit(new PistolRecoilProperty());

	@Override
	public boolean get(ItemStack stack, ClientLevel level, LivingEntity owner, int seed, ItemDisplayContext context) {
		return PistolControls.isRecoiling();
	}

	@Override
	public MapCodec<PistolRecoilProperty> type() {
		return CODEC;
	}
}
