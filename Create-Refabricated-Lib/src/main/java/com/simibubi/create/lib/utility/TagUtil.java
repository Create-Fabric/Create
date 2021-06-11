package com.simibubi.create.lib.utility;

import java.util.Arrays;
import java.util.Locale;

import net.fabricmc.fabric.impl.tag.extension.TagDelegate;
import net.minecraft.block.Block;
import net.minecraft.item.DyeColor;
import net.minecraft.item.DyeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ResourceLocation;

public class TagUtil {
	private static final String CREATE = "create";
	private static final String COMMON = "c";
	private static final String TIC = "tconstruct";

	// blocks
	public static final ITag.INamedTag<Block> WINDMILL_SAILS = new TagDelegate<>(new ResourceLocation(CREATE, "windmill_sails"), BlockTags::getCollection);
	public static final ITag.INamedTag<Block> FAN_HEATERS = new TagDelegate<>(new ResourceLocation(CREATE, "fan_heaters"), BlockTags::getCollection);
	public static final ITag.INamedTag<Block> WINDOWABLE = new TagDelegate<>(new ResourceLocation(CREATE, "windowable"), BlockTags::getCollection);
	public static final ITag.INamedTag<Block> NON_MOVABLE = new TagDelegate<>(new ResourceLocation(CREATE, "non_movable"), BlockTags::getCollection);
	public static final ITag.INamedTag<Block> BRITTLE = new TagDelegate<>(new ResourceLocation(CREATE, "brittle"), BlockTags::getCollection);
	public static final ITag.INamedTag<Block> BLOCKS$SEATS = new TagDelegate<>(new ResourceLocation(CREATE, "seats"), BlockTags::getCollection);
	public static final ITag.INamedTag<Block> SAILS = new TagDelegate<>(new ResourceLocation(CREATE, "sails"), BlockTags::getCollection);
	public static final ITag.INamedTag<Block> BLOCKS$VALVE_HANDLES = new TagDelegate<>(new ResourceLocation(CREATE, "valve_handles"), BlockTags::getCollection);
	public static final ITag.INamedTag<Block> FAN_TRANSPARENT = new TagDelegate<>(new ResourceLocation(CREATE, "fan_transparent"), BlockTags::getCollection);
	public static final ITag.INamedTag<Block> SAFE_NBT = new TagDelegate<>(new ResourceLocation(CREATE, "safe_nbt"), BlockTags::getCollection);

	// items
	public static final ITag.INamedTag<Item> CRUSHED_ORES = new TagDelegate<>(new ResourceLocation(CREATE, "crushed_ores"), ItemTags::getCollection);
	public static final ITag.INamedTag<Item> ITEMS$SEATS = new TagDelegate<>(new ResourceLocation(CREATE, "seats"), ItemTags::getCollection);
	public static final ITag.INamedTag<Item> ITEMS$VALVE_HANDLES = new TagDelegate<>(new ResourceLocation(CREATE, "valve_handles"), ItemTags::getCollection);
	public static final ITag.INamedTag<Item> UPRIGHT_ON_BELT = new TagDelegate<>(new ResourceLocation(CREATE, "upright_on_belt"), ItemTags::getCollection);
	public static final ITag.INamedTag<Item> CREATE_INGOTS = new TagDelegate<>(new ResourceLocation(CREATE, "create_ingots"), ItemTags::getCollection);
	public static final ITag.INamedTag<Item> BEACON_PAYMENT = new TagDelegate<>(new ResourceLocation(COMMON, "beacon_payment"), ItemTags::getCollection);
	public static final ITag.INamedTag<Item> INGOTS = new TagDelegate<>(new ResourceLocation(COMMON, "ingots"), ItemTags::getCollection);
	public static final ITag.INamedTag<Item> NUGGETS = new TagDelegate<>(new ResourceLocation(COMMON, "nuggets"), ItemTags::getCollection);
	public static final ITag.INamedTag<Item> PLATES = new TagDelegate<>(new ResourceLocation(COMMON, "plates"), ItemTags::getCollection);
	public static final ITag.INamedTag<Item> COBBLESTONE = new TagDelegate<>(new ResourceLocation(COMMON, "cobblestone"), ItemTags::getCollection);

	// TIC compat
	public static final ITag.INamedTag<Block> SLIMY_LOGS = new TagDelegate<>(new ResourceLocation(TIC, "slimy_logs"), BlockTags::getCollection);
	public static final ITag.INamedTag<Item> SLIMEBALLS = new TagDelegate<>(new ResourceLocation(TIC, "slime_balls"), ItemTags::getCollection);

	// dyes
	public static final ITag.INamedTag<Item> BLACK_DYES = new TagDelegate<>(new ResourceLocation(COMMON, "black_dyes"), ItemTags::getCollection);
	public static final ITag.INamedTag<Item> BLUE_DYES = new TagDelegate<>(new ResourceLocation(COMMON, "blue_dyes"), ItemTags::getCollection);
	public static final ITag.INamedTag<Item> BROWN_DYES = new TagDelegate<>(new ResourceLocation(COMMON, "brown_dyes"), ItemTags::getCollection);
	public static final ITag.INamedTag<Item> CYAN_DYES = new TagDelegate<>(new ResourceLocation(COMMON, "cyan_dyes"), ItemTags::getCollection);
	public static final ITag.INamedTag<Item> GRAY_DYES = new TagDelegate<>(new ResourceLocation(COMMON, "gray_dyes"), ItemTags::getCollection);
	public static final ITag.INamedTag<Item> GREEN_DYES = new TagDelegate<>(new ResourceLocation(COMMON, "green_dyes"), ItemTags::getCollection);
	public static final ITag.INamedTag<Item> LIGHT_BLUE_DYES = new TagDelegate<>(new ResourceLocation(COMMON, "light_blue_dyes"), ItemTags::getCollection);
	public static final ITag.INamedTag<Item> LIGHT_GRAY_DYES = new TagDelegate<>(new ResourceLocation(COMMON, "light_gray_dyes"), ItemTags::getCollection);
	public static final ITag.INamedTag<Item> LIME_DYES = new TagDelegate<>(new ResourceLocation(COMMON, "lime_dyes"), ItemTags::getCollection);
	public static final ITag.INamedTag<Item> MAGENTA_DYES = new TagDelegate<>(new ResourceLocation(COMMON, "magenta_dyes"), ItemTags::getCollection);
	public static final ITag.INamedTag<Item> ORANGE_DYES = new TagDelegate<>(new ResourceLocation(COMMON, "orange_dyes"), ItemTags::getCollection);
	public static final ITag.INamedTag<Item> PINK_DYES = new TagDelegate<>(new ResourceLocation(COMMON, "pink_dyes"), ItemTags::getCollection);
	public static final ITag.INamedTag<Item> PURPLE_DYES = new TagDelegate<>(new ResourceLocation(COMMON, "purple_dyes"), ItemTags::getCollection);
	public static final ITag.INamedTag<Item> RED_DYES = new TagDelegate<>(new ResourceLocation(COMMON, "red_dyes"), ItemTags::getCollection);
	public static final ITag.INamedTag<Item> WHITE_DYES = new TagDelegate<>(new ResourceLocation(COMMON, "white_dyes"), ItemTags::getCollection);
	public static final ITag.INamedTag<Item> YELLOW_DYES = new TagDelegate<>(new ResourceLocation(COMMON, "yellow_dyes"), ItemTags::getCollection);

	// helper methods
	public static ITag.INamedTag getTagFromResourceLocation(ResourceLocation location) {
		String name = (String) Arrays.stream(location.getPath().split("/")).toArray()[location.getPath().split("/").length - 1];
		switch (name) {
			case "windmill_sails": return WINDMILL_SAILS;
			case "fan_heaters": return FAN_HEATERS;
			case "windowable": return WINDOWABLE;
			case "non_movable": return NON_MOVABLE;
			case "brittle": return BRITTLE;
			case "seats": if (location.toString().contains("item")) {return ITEMS$SEATS;} return BLOCKS$SEATS;
			case "sails": return SAILS;
			case "valve_handles": if (location.toString().contains("item")) {return ITEMS$VALVE_HANDLES;} return BLOCKS$VALVE_HANDLES;
			case "fan_transparent": return FAN_TRANSPARENT;
			case "safe_nbt": return SAFE_NBT;
			case "crushed_ores": return CRUSHED_ORES;
			case "upright_on_belt": return UPRIGHT_ON_BELT;
			case "create_ingots": return CREATE_INGOTS;
			case "beacon_payment": return BEACON_PAYMENT;
			case "ingots": return INGOTS;
			case "nuggets": return NUGGETS;
			case "plates": return PLATES;
			case "cobblestone": return COBBLESTONE;

			case "slimy_logs": return SLIMY_LOGS;
			case "slimeballs": return SLIMEBALLS;

			case "black_dyes": return BLACK_DYES;
			case "blue_dyes": return BLUE_DYES;
			case "brown_dyes": return BROWN_DYES;
			case "cyan_dyes": return CYAN_DYES;
			case "gray_dyes": return GRAY_DYES;
			case "green_dyes": return GREEN_DYES;
			case "light_blue_dyes": return LIGHT_BLUE_DYES;
			case "light_gray_dyes": return LIGHT_GRAY_DYES;
			case "lime_dyes": return LIME_DYES;
			case "magenta_dyes": return MAGENTA_DYES;
			case "orange_dyes": return ORANGE_DYES;
			case "pink_dyes": return PINK_DYES;
			case "purple_dyes": return PURPLE_DYES;
			case "red_dyes": return RED_DYES;
			case "white_dyes": return WHITE_DYES;
			case "yellow_dyes": return YELLOW_DYES;
		}
		return null;
	}

	public static boolean isDye(Item item) {
		return  item.isIn(BLACK_DYES) ||
				item.isIn(BLUE_DYES) ||
				item.isIn(BROWN_DYES) ||
				item.isIn(CYAN_DYES) ||
				item.isIn(GRAY_DYES) ||
				item.isIn(GREEN_DYES) ||
				item.isIn(LIGHT_BLUE_DYES) ||
				item.isIn(LIGHT_GRAY_DYES) ||
				item.isIn(LIME_DYES) ||
				item.isIn(MAGENTA_DYES) ||
				item.isIn(ORANGE_DYES) ||
				item.isIn(PINK_DYES) ||
				item.isIn(PURPLE_DYES) ||
				item.isIn(RED_DYES) ||
				item.isIn(WHITE_DYES) ||
				item.isIn(YELLOW_DYES);
	}

	public static DyeColor getColorFromStack(ItemStack stack) {
		Item item = stack.getItem();
		if (item instanceof DyeItem) {
			return ((DyeItem) stack.getItem()).getDyeColor();
		}

		if (item.isIn(BLACK_DYES)) return DyeColor.BLACK;
		if (item.isIn(BLUE_DYES)) return DyeColor.BLUE;
		if (item.isIn(BROWN_DYES)) return DyeColor.BROWN;
		if (item.isIn(CYAN_DYES)) return DyeColor.CYAN;
		if (item.isIn(GRAY_DYES)) return DyeColor.GRAY;
		if (item.isIn(GREEN_DYES)) return DyeColor.GREEN;
		if (item.isIn(LIGHT_BLUE_DYES)) return DyeColor.LIGHT_BLUE;
		if (item.isIn(LIGHT_GRAY_DYES)) return DyeColor.LIGHT_GRAY;
		if (item.isIn(LIME_DYES)) return DyeColor.LIME;
		if (item.isIn(MAGENTA_DYES)) return DyeColor.MAGENTA;
		if (item.isIn(ORANGE_DYES)) return DyeColor.ORANGE;
		if (item.isIn(PINK_DYES)) return DyeColor.PINK;
		if (item.isIn(PURPLE_DYES)) return DyeColor.PURPLE;
		if (item.isIn(RED_DYES)) return DyeColor.RED;
		if (item.isIn(WHITE_DYES)) return DyeColor.WHITE;
		if (item.isIn(YELLOW_DYES)) return DyeColor.YELLOW;

		// item is not in color tags, default to white I guess
		return DyeColor.WHITE;
	}
}
