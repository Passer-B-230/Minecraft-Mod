package com.example;

import com.example.entity.ThrowingKnifeEntity;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ModEntities {
	public static final ResourceKey<EntityType<?>> THROWING_KNIFE_KEY = ResourceKey.create(
			Registries.ENTITY_TYPE, com.example.TemplateMod.id("throwing_knife")
	);

	public static final EntityType<ThrowingKnifeEntity> THROWING_KNIFE = Registry.register(
			BuiltInRegistries.ENTITY_TYPE,
			THROWING_KNIFE_KEY,
			EntityType.Builder.<ThrowingKnifeEntity>of((type, level) -> new ThrowingKnifeEntity(type, level), MobCategory.MISC)
				.sized(0.25F, 0.25F)
				.clientTrackingRange(4)
				.updateInterval(1)
				.build(THROWING_KNIFE_KEY)
	);

	public static void spawnKnife(Level level, LivingEntity owner, ItemStack stack) {
		spawnKnife(level, owner, stack, 0.0F);
	}

	public static void spawnKnife(Level level, LivingEntity owner, ItemStack stack, float yawOffset) {
		ThrowingKnifeEntity knife = new ThrowingKnifeEntity(THROWING_KNIFE, level, owner, stack);
		knife.shootFromRotation(owner, owner.getXRot(), owner.getYRot() + yawOffset, 0.0F, 2.0F, 1.0F);
		level.addFreshEntity(knife);
	}
}
