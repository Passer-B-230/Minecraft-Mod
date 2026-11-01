package com.example.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CarpetBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.entity.InsideBlockEffectApplier;

public class LandmineBlock extends CarpetBlock {
	private static final VoxelShape SHAPE = Block.box(4, 0, 4, 12, 2, 12);

	public LandmineBlock(Properties properties) {
		super(properties);
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPE;
	}

	@Override
	protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
		BlockPos below = pos.below();
		return level.getBlockState(below).isFaceSturdy(level, below, Direction.UP);
	}

	@Override
	public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
		trigger(level, pos, entity);
		super.stepOn(level, pos, state, entity);
	}

	@Override
	protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity,
			InsideBlockEffectApplier effectApplier, boolean isPrecise) {
		if (entity instanceof LivingEntity) {
			trigger(level, pos, entity);
		}
		super.entityInside(state, level, pos, entity, effectApplier, isPrecise);
	}

	private static void trigger(Level level, BlockPos pos, Entity entity) {
		if (!level.isClientSide() && level.getBlockState(pos).getBlock() instanceof LandmineBlock) {
			level.setBlock(pos, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 3);
			double x = pos.getX() + 0.5D;
			double y = pos.getY() + 0.65D;
			double z = pos.getZ() + 0.5D;
			level.explode(entity, x, y, z,
					3.0F, false, Level.ExplosionInteraction.BLOCK);

			// Ground-level explosions can lose all damage to block occlusion. Apply a
			// short-range direct blast as well so stepping on the mine is reliable.
			AABB damageArea = new AABB(x, y, z, x, y, z).inflate(1.5D);
			for (Entity nearby : level.getEntities((Entity) null, damageArea,
					nearby -> nearby instanceof LivingEntity && nearby.isAlive())) {
				nearby.hurt(level.damageSources().explosion(entity, entity), 16.0F);
			}
		}
	}
}
