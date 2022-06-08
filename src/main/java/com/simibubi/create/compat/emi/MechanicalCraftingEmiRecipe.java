package com.simibubi.create.compat.emi;

import java.util.List;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipe;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.gui.AllGuiTextures;

import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.api.widget.WidgetHolder;

public class MechanicalCraftingEmiRecipe extends CreateEmiRecipe<CraftingRecipe> {
	private static final int MAX_SIZE = 100;
	private int recipeAmount = 0;

	public MechanicalCraftingEmiRecipe(EmiRecipeCategory category, CraftingRecipe recipe) {
		super(category, recipe, 177, 109, c -> {});
		this.input = recipe.getIngredients().stream().map(EmiIngredient::of).toList();
		this.output = List.of(EmiStack.of(recipe.getResultItem()));
		for (Ingredient ingredient : recipe.getIngredients()) {
			if (!ingredient.isEmpty()) {
				recipeAmount++;
			}
		}
	}

	public float getScale() {
		int w = getWidth();
		int h = getHeight();
		return Math.min(1, MAX_SIZE / (19f * Math.max(w, h)));
	}

	public int getYPadding() {
		return 3 + 50 - (int) (getScale() * getHeight() * 19 * .5);
	}

	public int getXPadding() {
		return 3 + 50 - (int) (getScale() * getWidth() * 19 * .5);
	}

	private int getWidth() {
		return recipe instanceof ShapedRecipe ? ((ShapedRecipe) recipe).getWidth() : 1;
	}

	private int getHeight() {
		return recipe instanceof ShapedRecipe ? ((ShapedRecipe) recipe).getHeight() : 1;
	}
	
	@Override
	public void addWidgets(WidgetHolder widgets) {
		float scale = getScale();
		int xOff = getXPadding();
		int yOff = getYPadding() + 4;

		addTexture(widgets, AllGuiTextures.JEI_DOWN_ARROW, 128, 59);
		addTexture(widgets, AllGuiTextures.JEI_SHADOW, 113, 38);

		for (int i = 0; i < recipe.getIngredients().size(); i++) {
			int row = i / getWidth();
			int col = i % getWidth();
			widgets.add(new CrafterSlotWidget(input.get(row * getWidth() + col),
				(int) ((col * 19 + xOff) * scale), (int) ((row * 19 + yOff) * scale)))
				.backgroundTexture(AllGuiTextures.JEI_SLOT.location, AllGuiTextures.JEI_SLOT.startX, AllGuiTextures.JEI_SLOT.startY);
		}

		addSlot(widgets, output.get(0), 134, 81).recipeContext(this);

		CreateEmiAnimations.addCrafter(widgets, 132, 38);

		widgets.addText(new TextComponent("" + recipeAmount).getVisualOrderText(), 142, 39, -1, true);
	}

	public class CrafterSlotWidget extends SlotWidget {
		private final Bounds bounds;

		public CrafterSlotWidget(EmiIngredient stack, int x, int y) {
			super(stack, x, y);
			int w = (int) (18 * getScale());
			this.bounds = new Bounds(x, y, w, w);
		}

		@Override
		public Bounds getBounds() {
			return bounds;
		}

		@Override
		public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
			PoseStack view = RenderSystem.getModelViewStack();
			view.pushPose();
			float scale = getScale();
			view.translate(x, y, 0);
			view.scale(scale, scale, scale);
			view.translate(-x, -y, 0);
			RenderSystem.applyModelViewMatrix();
			super.render(matrices, mouseX, mouseY, delta);
			view.popPose();
			RenderSystem.applyModelViewMatrix();
		}
	}
}
