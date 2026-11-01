package com.example.client;

import com.example.item.PistolItem;
import com.example.network.PistolFirePayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;

public final class PistolControls {
	private static final int RECOIL_TICKS = 4;
	private static int recoilUntil;
	public static boolean holdingPistol() {
		Minecraft client = Minecraft.getInstance();
		return client.player != null && client.player.getMainHandItem().getItem() instanceof PistolItem;
	}

	public static boolean isAiming() {
		Minecraft client = Minecraft.getInstance();
		return holdingPistol() && client.gui.screen() == null && !client.player.isSpectator()
				&& !client.player.isUsingItem() && !client.player.isShiftKeyDown()
				&& client.options.keyUse.isDown();
	}

	public static void handleAttackClicks() {
		Minecraft client = Minecraft.getInstance();
		if (!holdingPistol()) {
			return;
		}
		while (client.options.keyAttack.consumeClick()) {
			if (!client.player.isUsingItem() && !client.player.isSpectator()
					&& !client.player.getCooldowns().isOnCooldown(client.player.getMainHandItem())
					&& ClientPlayNetworking.canSend(PistolFirePayload.TYPE)) {
				boolean aiming = isAiming();
				ClientPlayNetworking.send(new PistolFirePayload(aiming));
				client.player.getCooldowns().addCooldown(client.player.getMainHandItem(), PistolItem.getFireCooldownTicks(aiming));
				recoilUntil = client.player.tickCount + RECOIL_TICKS;
			}
		}
	}

	public static boolean isRecoiling() {
		Minecraft client = Minecraft.getInstance();
		return holdingPistol() && client.player.tickCount < recoilUntil;
	}
}
