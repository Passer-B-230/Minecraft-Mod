package com.example;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;

public class ModSounds {
	public static final SoundEvent THROWING_KNIFE = register("throwing_knife");

	private static SoundEvent register(String name) {
		return Registry.register(
				BuiltInRegistries.SOUND_EVENT,
				TemplateMod.id(name),
				SoundEvent.createVariableRangeEvent(TemplateMod.id(name))
		);
	}

	public static void initialize() {
		// Loading this class registers the sound event.
	}
}
