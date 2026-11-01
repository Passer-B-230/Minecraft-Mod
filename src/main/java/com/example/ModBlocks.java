package com.example;

import com.example.block.LandmineBlock;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class ModBlocks {
	public static final ResourceKey<Block> LANDMINE_KEY = ResourceKey.create(Registries.BLOCK, TemplateMod.id("landmine"));
	public static final Block LANDMINE = Registry.register(BuiltInRegistries.BLOCK, LANDMINE_KEY,
			new LandmineBlock(BlockBehaviour.Properties.of().setId(LANDMINE_KEY)
					.strength(0.5F).sound(SoundType.METAL).noOcclusion().noCollision()));

	public static void initialize() {
		// Calling this method loads the class and registers its blocks before their items.
	}
}
