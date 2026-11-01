package com.example.client.mixin;

import com.example.client.PistolControls;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class PistolInputMixin {
	@Inject(method = "handleKeybinds", at = @At("HEAD"))
	private void pistolAttackClicks(CallbackInfo ci) {
		PistolControls.handleAttackClicks();
	}

	@Inject(method = "startUseItem", at = @At("HEAD"), cancellable = true)
	private void preventAimEquipAnimation(CallbackInfo ci) {
		Minecraft client = Minecraft.getInstance();
		if (PistolControls.holdingPistol() && !client.player.isShiftKeyDown()
				&& !client.player.isSpectator()) {
			// Aim is driven by the held key. Vanilla use calls itemUsed every four ticks,
			// repeatedly lowering the weapon even when the use result does not swing.
			ci.cancel();
		}
	}

	@Inject(method = "continueAttack", at = @At("HEAD"), cancellable = true)
	private void preventPistolMining(boolean down, CallbackInfo ci) {
		if (PistolControls.holdingPistol()) {
			ci.cancel();
		}
	}
}
