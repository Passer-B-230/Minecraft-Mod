package com.example;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;

public class ModItemIds {
	public static final ResourceKey<Item> RUBY = create("ruby");
	public static final ResourceKey<Item> POISONOUS_APPLE = create("poisonous_apple");
	public static final ResourceKey<Item> THROWING_KNIFE = create("throwing_knife");
	public static final ResourceKey<Item> PISTOL = create("pistol");
	public static final ResourceKey<Item> BULLET = create("bullet");
	public static final ResourceKey<Item> LANDMINE = create("landmine");

	public static ResourceKey<Item> create(String name) {
		return ResourceKey.create(Registries.ITEM, TemplateMod.id(name));
	}
}
