package com.example;

import com.example.item.ThrowingKnifeItem;
import com.example.item.PistolItem;
import java.util.function.Function;

import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;

public class ModItems {
	public static final Item RUBY = register(ModItemIds.RUBY, Item::new, new Item.Properties());

	public static final Consumable POISON_FOOD_CONSUMABLE_COMPONENT = Consumables.defaultFood()
			.onConsume(new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(MobEffects.POISON, 6 * 20, 1), 1.0f))
			.build();
	public static final FoodProperties POISON_FOOD_COMPONENT = new FoodProperties.Builder()
			.alwaysEdible()
			.build();
	public static final Item POISONOUS_APPLE = register(
			ModItemIds.POISONOUS_APPLE,
			Item::new,
			new Item.Properties().food(POISON_FOOD_COMPONENT, POISON_FOOD_CONSUMABLE_COMPONENT)
	);
	public static final Item THROWING_KNIFE = register(
			ModItemIds.THROWING_KNIFE,
			ThrowingKnifeItem::new,
		new Item.Properties().stacksTo(16).enchantable(10)
	);
	public static final Item PISTOL = register(
			ModItemIds.PISTOL,
			PistolItem::new,
			new Item.Properties().stacksTo(1).durability(500)
	);
	public static final Item BULLET = register(ModItemIds.BULLET, Item::new, new Item.Properties());
	public static final Item LANDMINE = register(ModItemIds.LANDMINE,
			properties -> new BlockItem(ModBlocks.LANDMINE, properties),
			new Item.Properties().useBlockDescriptionPrefix());

	public static Item register(ResourceKey<Item> itemKey, Function<Item.Properties, Item> itemFactory, Item.Properties properties) {
		Item item = itemFactory.apply(properties.setId(itemKey));
		return Registry.register(BuiltInRegistries.ITEM, itemKey, item);
	}

	public static void initialize() {
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.INGREDIENTS)
				.register(entries -> entries.accept(RUBY));
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.FOOD_AND_DRINKS)
				.register(entries -> entries.accept(POISONOUS_APPLE));
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.COMBAT)
				.register(entries -> entries.accept(THROWING_KNIFE));
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.COMBAT)
				.register(entries -> entries.accept(PISTOL));
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.COMBAT)
				.register(entries -> entries.accept(BULLET));
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.COMBAT)
				.register(entries -> entries.accept(LANDMINE));
	}
}
