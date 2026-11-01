package com.example.item;

import com.example.ModEntities;
import com.example.ModSounds;
import com.example.TemplateMod;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ThrowingKnifeItem extends Item {
	public ThrowingKnifeItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		if (!level.isClientSide()) {
			ItemStack stack = player.getItemInHand(hand);
			int multishotLevel = getEnchantmentLevel(level, stack, "multishot_throw");
			if (multishotLevel > 0) {
				ModEntities.spawnKnife(level, player, stack, -10.0F);
				ModEntities.spawnKnife(level, player, stack, 10.0F);
			}
			ModEntities.spawnKnife(level, player, stack, 0.0F);
			level.playSound(null, player, ModSounds.THROWING_KNIFE, net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.0F);
			if (!player.getAbilities().instabuild) {
				stack.shrink(1);
			}
			int quickThrowLevel = getEnchantmentLevel(level, stack, "quick_throw");
			player.getCooldowns().addCooldown(stack, Math.max(1, 10 - quickThrowLevel * 2));
		}

		return InteractionResult.SUCCESS;
	}

	private static int getEnchantmentLevel(Level level, ItemStack stack, String name) {
		return level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).get(TemplateMod.id(name))
				.map(enchantment -> EnchantmentHelper.getItemEnchantmentLevel(enchantment, stack))
				.orElse(0);
	}
}
