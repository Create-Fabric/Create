package com.simibubi.create.compat.emi;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.contraptions.processing.ProcessingRecipe;
import com.simibubi.create.foundation.gui.AllGuiTextures;

import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.world.item.crafting.Recipe;

public abstract class FanEmiRecipe<T extends Recipe<?>> extends CreateEmiRecipe<T> {
	protected static final int SCALE = 24;

	public FanEmiRecipe(EmiRecipeCategory type, T recipe) {
		super(type, recipe, 134, 76);
	}
	protected abstract void renderAttachedBlock(PoseStack matrices);

	@Override
	public void addWidgets(WidgetHolder widgets) {
		addTexture(widgets, AllGuiTextures.JEI_SHADOW, 24, 29);
		addTexture(widgets, AllGuiTextures.JEI_LONG_ARROW, 32, 51);
		addTexture(widgets, AllGuiTextures.JEI_SHADOW, 43, 39);

		addSlot(widgets, input.get(0), 2, 48);
		
		addSlot(widgets, output.get(0), 114, 48).recipeContext(this);

		CreateEmiAnimations.addFan(widgets, 34, 33, this::renderAttachedBlock);
	}

	public static abstract class MultiOutput<T extends ProcessingRecipe<?>> extends FanEmiRecipe<T> {

		public MultiOutput(EmiRecipeCategory type, T recipe) {
			super(type, recipe);
			if (output.size() > 1) {
				width = 178;
			}
		}

		@Override
		public void addWidgets(WidgetHolder widgets) {
			if (output.size() == 1) {
				super.addWidgets(widgets);
				return;
			}
			int size = output.size();
			int xOff = 1 - Math.min(3, size);
			
			addSlot(widgets, input.get(0), 21 + 5 * xOff, 48);
			addTexture(widgets, AllGuiTextures.JEI_LONG_ARROW, 54 + 7 * xOff, 51);
			
			xOff = 9 * xOff;
			int yOff = 0;
			if (output.size() > 3) {
				yOff -= 12 + 16 * ((output.size() - 4) / 3);
			}

			for (int i = 0; i < size; i++) {
				int x = (i % 3) * 19 + xOff;
				int y = (i / 3) * 19 + yOff;
				addChancedSlot(widgets, output.get(i), 140 + x, 47 + y, i).recipeContext(this);
			}

			CreateEmiAnimations.addFan(widgets, 56, 33, this::renderAttachedBlock);
		}
	}
}
