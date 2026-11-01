package com.example.client;

import com.mojang.serialization.MapCodec;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperty;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public record PistolAimingProperty() implements ConditionalItemModelProperty {
	public static final MapCodec<PistolAimingProperty> CODEC = MapCodec.unit(new PistolAimingProperty());

	@Override
	public boolean get(ItemStack stack, ClientLevel level, LivingEntity owner, int seed, ItemDisplayContext context) {
		return context.firstPerson() && PistolControls.isAiming();
	}

	@Override
	public MapCodec<PistolAimingProperty> type() {
		return CODEC;
	}
}
