package com.example.item;

import com.example.ModItems;
import com.example.TemplateMod;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public class PistolItem extends Item {
	// 20 ticks = 1 second; 2 damage points = 1 heart.
	private static final double RANGE = 48.0;
	private static final float DAMAGE = 8.0F;
	public static final int COOLDOWN_TICKS = 10;
	public static final int HIP_FIRE_COOLDOWN_TICKS = 5;
	public static final int MAGAZINE_CAPACITY = 12;
	private static final int RELOAD_TICKS = 30;
	private static final String AMMO_KEY = "template_mod_pistol_ammo";
	private static final ResourceKey<DamageType> BULLET_DAMAGE =
			ResourceKey.create(Registries.DAMAGE_TYPE, TemplateMod.id("bullet"));

	public PistolItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		ItemStack pistol = player.getItemInHand(hand);
		if (player.getCooldowns().isOnCooldown(pistol)) {
			return InteractionResult.FAIL;
		}

		if (player.isUsingItem()) {
			return InteractionResult.FAIL;
		}
		if (player.isShiftKeyDown()) {
			return startReload(level, player, hand, pistol);
		}
		// Normal right click is handled as aiming on the client; it does not use the item.
		// Holding right click repeats use calls; consume without swinging the player's hand.
		return InteractionResult.CONSUME;
	}

	public static int getFireCooldownTicks(boolean aiming) {
		return aiming ? COOLDOWN_TICKS : HIP_FIRE_COOLDOWN_TICKS;
	}

	public static void fire(Player player, boolean aiming) {
		Level level = player.level();
		InteractionHand hand = InteractionHand.MAIN_HAND;
		ItemStack pistol = player.getMainHandItem();
		int cooldownTicks = getFireCooldownTicks(aiming);
		if (!(level instanceof ServerLevel) || !(pistol.getItem() instanceof PistolItem)
				|| !player.isAlive() || player.isSpectator() || player.isUsingItem()
				|| player.getCooldowns().isOnCooldown(pistol)) {
			return;
		}
		if (!player.getAbilities().instabuild && getLoadedAmmo(pistol) == 0) {
			if (level instanceof ServerLevel) {
				player.sendOverlayMessage(Component.translatable("message.template-mod.pistol.no_ammo"));
				level.playSound(null, player, SoundEvents.DISPENSER_FAIL, SoundSource.PLAYERS, 0.5F, 1.5F);
				player.getCooldowns().addCooldown(pistol, cooldownTicks);
			}
			return;
		}

		if (level instanceof ServerLevel serverLevel) {
			shoot(serverLevel, player);
			player.getCooldowns().addCooldown(pistol, cooldownTicks);
			if (!player.getAbilities().instabuild) {
				setLoadedAmmo(pistol, getLoadedAmmo(pistol) - 1);
				pistol.hurtAndBreak(1, player, hand);
			}
			player.sendOverlayMessage(Component.translatable(player.getAbilities().instabuild
					? "message.template-mod.pistol.infinite_ammo" : "message.template-mod.pistol.ammo",
					getLoadedAmmo(pistol), MAGAZINE_CAPACITY));
		}
	}

	// Custom data is saved and synchronized with each individual pistol stack.
	public static int getLoadedAmmo(ItemStack pistol) {
		int ammo = pistol.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)
				.copyTag().getIntOr(AMMO_KEY, 0);
		return Math.clamp(ammo, 0, MAGAZINE_CAPACITY);
	}

	private static void setLoadedAmmo(ItemStack pistol, int ammo) {
		CustomData.update(DataComponents.CUSTOM_DATA, pistol,
				tag -> tag.putInt(AMMO_KEY, Math.clamp(ammo, 0, MAGAZINE_CAPACITY)));
	}

	private static InteractionResult startReload(Level level, Player player, InteractionHand hand, ItemStack pistol) {
		String error = null;
		if (getLoadedAmmo(pistol) >= MAGAZINE_CAPACITY) {
			error = "message.template-mod.pistol.magazine_full";
		} else if (!player.getAbilities().instabuild && findAmmunition(player).isEmpty()) {
			error = "message.template-mod.pistol.no_reserve";
		}
		if (error != null) {
			if (level instanceof ServerLevel) {
				player.sendOverlayMessage(Component.translatable(error));
			}
			return InteractionResult.FAIL;
		}
		player.startUsingItem(hand);
		if (level instanceof ServerLevel) {
			player.sendOverlayMessage(Component.translatable("message.template-mod.pistol.reloading"));
			level.playSound(null, player, SoundEvents.DISPENSER_DISPENSE, SoundSource.PLAYERS, 0.5F, 0.8F);
		}
		return InteractionResult.CONSUME;
	}

	@Override
	public int getUseDuration(ItemStack stack, LivingEntity user) {
		return RELOAD_TICKS;
	}

	@Override
	public ItemStack finishUsingItem(ItemStack pistol, Level level, LivingEntity user) {
		if (level instanceof ServerLevel && user instanceof Player player) {
			int loaded = getLoadedAmmo(pistol);
			int needed = MAGAZINE_CAPACITY - loaded;
			if (player.getAbilities().instabuild) {
				loaded = MAGAZINE_CAPACITY;
			} else {
				// Consume only on completion; releasing right click or switching items cancels for free.
				for (int slot = 0; slot < player.getInventory().getContainerSize() && needed > 0; slot++) {
					ItemStack ammunition = player.getInventory().getItem(slot);
					if (ammunition.is(ModItems.BULLET)) {
						int amount = Math.min(needed, ammunition.getCount());
						ammunition.shrink(amount);
						loaded += amount;
						needed -= amount;
					}
				}
			}
			setLoadedAmmo(pistol, loaded);
			player.getCooldowns().addCooldown(pistol, COOLDOWN_TICKS);
			player.sendOverlayMessage(Component.translatable("message.template-mod.pistol.reloaded", loaded, MAGAZINE_CAPACITY));
			level.playSound(null, player, SoundEvents.DISPENSER_DISPENSE, SoundSource.PLAYERS, 0.5F, 1.3F);
		}
		return pistol;
	}

	@Override
	public boolean releaseUsing(ItemStack pistol, Level level, LivingEntity user, int remainingTime) {
		if (level instanceof ServerLevel && user instanceof Player player) {
			player.sendOverlayMessage(Component.translatable("message.template-mod.pistol.reload_cancelled"));
		}
		return false;
	}

	@Override
	public void appendHoverText(ItemStack pistol, TooltipContext context, TooltipDisplay display,
			Consumer<Component> builder, TooltipFlag flag) {
		builder.accept(Component.translatable("message.template-mod.pistol.ammo", getLoadedAmmo(pistol), MAGAZINE_CAPACITY)
				.withStyle(ChatFormatting.GOLD));
		builder.accept(Component.translatable("tooltip.template-mod.pistol.reload").withStyle(ChatFormatting.GRAY));
	}

	private static ItemStack findAmmunition(Player player) {
		for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
			ItemStack stack = player.getInventory().getItem(slot);
			if (stack.is(ModItems.BULLET) && !stack.isEmpty()) {
				return stack;
			}
		}
		return ItemStack.EMPTY;
	}

	private static void shoot(ServerLevel level, Player player) {
		Vec3 start = player.getEyePosition();
		Vec3 direction = player.getLookAngle();
		Vec3 end = start.add(direction.scale(RANGE));
		// Stop the ray at the first solid block so bullets cannot shoot through walls.
		Vec3 impact = level.clipIncludingBorder(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player))
				.getLocation();
		EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(
				level, player, start, impact,
				player.getBoundingBox().expandTowards(direction.scale(RANGE)).inflate(1.0),
				entity -> !entity.isSpectator() && entity.isPickable() && entity.isAlive(), 0.0F
		);
		if (entityHit != null) {
			impact = entityHit.getLocation();
			DamageSource damage = new DamageSource(level.registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE)
					.getOrThrow(BULLET_DAMAGE), player, player);
			entityHit.getEntity().hurtServer(level, damage, DAMAGE);
		}

		level.playSound(null, player, SoundEvents.FIREWORK_ROCKET_BLAST, SoundSource.PLAYERS, 1.0F, 1.6F);
		// The trace is cosmetic; damage and ammo consumption happen only on the server.
		double distance = start.distanceTo(impact);
		for (double step = 0.8; step < distance; step += 1.0) {
			Vec3 point = start.add(direction.scale(step));
			level.sendParticles(ParticleTypes.CRIT, point.x, point.y, point.z, 1, 0.0, 0.0, 0.0, 0.0);
		}
		level.sendParticles(ParticleTypes.SMOKE, impact.x, impact.y, impact.z, 4, 0.05, 0.05, 0.05, 0.01);
	}
}
