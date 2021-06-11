package com.simibubi.create.foundation.utility;

import com.simibubi.create.lib.utility.TagUtil;

import net.minecraft.block.Blocks;
import net.minecraft.item.DyeColor;
import net.minecraft.item.Item;
import net.minecraft.tags.ITag;
import net.minecraft.util.IItemProvider;

public class DyeHelper {

	public static IItemProvider getWoolOfDye(DyeColor color) {
		switch (color) {
		case BLACK:
			return Blocks.BLACK_WOOL;
		case BLUE:
			return Blocks.BLUE_WOOL;
		case BROWN:
			return Blocks.BROWN_WOOL;
		case CYAN:
			return Blocks.CYAN_WOOL;
		case GRAY:
			return Blocks.GRAY_WOOL;
		case GREEN:
			return Blocks.GREEN_WOOL;
		case LIGHT_BLUE:
			return Blocks.LIGHT_BLUE_WOOL;
		case LIGHT_GRAY:
			return Blocks.LIGHT_GRAY_WOOL;
		case LIME:
			return Blocks.LIME_WOOL;
		case MAGENTA:
			return Blocks.MAGENTA_WOOL;
		case ORANGE:
			return Blocks.ORANGE_WOOL;
		case PINK:
			return Blocks.PINK_WOOL;
		case PURPLE:
			return Blocks.PURPLE_WOOL;
		case RED:
			return Blocks.RED_WOOL;
		case YELLOW:
			return Blocks.YELLOW_WOOL;
		case WHITE:
		default:
			return Blocks.WHITE_WOOL;
		}
	}

	public static ITag.INamedTag<Item> getTagOfDye(DyeColor color) {
		switch (color) {
		case BLACK:
			return TagUtil.BLACK_DYES;
		case BLUE:
			return TagUtil.BLUE_DYES;
		case BROWN:
			return TagUtil.BROWN_DYES;
		case CYAN:
			return TagUtil.CYAN_DYES;
		case GRAY:
			return TagUtil.GRAY_DYES;
		case GREEN:
			return TagUtil.GREEN_DYES;
		case LIGHT_BLUE:
			return TagUtil.LIGHT_BLUE_DYES;
		case LIGHT_GRAY:
			return TagUtil.LIGHT_GRAY_DYES;
		case LIME:
			return TagUtil.LIME_DYES;
		case MAGENTA:
			return TagUtil.MAGENTA_DYES;
		case ORANGE:
			return TagUtil.ORANGE_DYES;
		case PINK:
			return TagUtil.PINK_DYES;
		case PURPLE:
			return TagUtil.PURPLE_DYES;
		case RED:
			return TagUtil.RED_DYES;
		case YELLOW:
			return TagUtil.YELLOW_DYES;
		case WHITE:
		default:
			return TagUtil.WHITE_DYES;
		}
	}
}
