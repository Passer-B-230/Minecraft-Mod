package com.example.entity;

import com.example.ModItems;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class ThrowingKnifeEntity extends ThrowableItemProjectile {
	public ThrowingKnifeEntity(EntityType<? extends ThrowingKnifeEntity> type, Level level) {
		super(type, level);
	}

	public ThrowingKnifeEntity(EntityType<? extends ThrowingKnifeEntity> type, Level level, LivingEntity owner, ItemStack stack) {
		super(type, owner, level, stack);
	}

	@Override
	protected Item getDefaultItem() {
		return ModItems.THROWING_KNIFE;
	}

	@Override
	protected void onHitEntity(EntityHitResult hitResult) {
		super.onHitEntity(hitResult);
		if (level() instanceof ServerLevel serverLevel) {
			hitResult.getEntity().hurtServer(serverLevel, damageSources().thrown(this, getOwner()), 6.0F);
		}
	}

	@Override
	protected void onHit(HitResult hitResult) {
		super.onHit(hitResult);
		if (!level().isClientSide()) {
			discard();
		}
	}
}
