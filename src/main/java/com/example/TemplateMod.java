package com.example;

import net.fabricmc.api.ModInitializer;

import net.minecraft.resources.Identifier;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import com.example.enchantment.LifestealEffect;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TemplateMod implements ModInitializer {
	public static final String MOD_ID = "template-mod";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		Registry.register(BuiltInRegistries.ENCHANTMENT_ENTITY_EFFECT_TYPE, id("lifesteal"), LifestealEffect.CODEC);
		ModSounds.initialize();
		ModBlocks.initialize();
		ModItems.initialize();
		net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry.serverboundPlay()
				.register(com.example.network.PistolFirePayload.TYPE, com.example.network.PistolFirePayload.CODEC);
		net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.registerGlobalReceiver(
				com.example.network.PistolFirePayload.TYPE, (payload, context) -> com.example.item.PistolItem.fire(context.player(), payload.aiming()));

		LOGGER.info("Hello Fabric world!");
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
