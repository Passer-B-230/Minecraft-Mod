package com.example.enchantment;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.item.enchantment.effects.EnchantmentEntityEffect;
import net.minecraft.world.phys.Vec3;

public record LifestealEffect(LevelBasedValue amount) implements EnchantmentEntityEffect {
	public static final MapCodec<LifestealEffect> CODEC = RecordCodecBuilder.mapCodec(
			instance -> instance.group(LevelBasedValue.CODEC.fieldOf("amount").forGetter(LifestealEffect::amount))
					.apply(instance, LifestealEffect::new)
	);

	@Override
	public void apply(ServerLevel level, int enchantmentLevel, EnchantedItemInUse item, Entity entity, Vec3 position) {
		if (entity instanceof LivingEntity livingEntity) {
			livingEntity.heal(Math.max(0.0F, amount.calculate(enchantmentLevel)));
		}
	}

	@Override
	public MapCodec<LifestealEffect> codec() {
		return CODEC;
	}
}
