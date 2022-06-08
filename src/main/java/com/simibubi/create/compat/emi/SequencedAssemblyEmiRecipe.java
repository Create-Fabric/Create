package com.simibubi.create.compat.emi;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.item.crafting.RecipeType;
import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.content.contraptions.itemAssembly.SequencedAssemblyRecipe;
import com.simibubi.create.content.contraptions.itemAssembly.SequencedRecipe;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.utility.Lang;

import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;

public class SequencedAssemblyEmiRecipe extends CreateEmiRecipe<SequencedAssemblyRecipe> {
	public static final String[] ROMAN = {
		"I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX",
		"X", "XI", "XII", "XIII", "XIV", "XV", "XVI", "XVII", "XVIII", "XIX",
		"XX", "XXI", "XXII", "XXIII", "XXIV", "XXV", "XXVI", "XXVII", "XXVIII", "XXIX", "-" };
	private final int margin = 3;
	private int width;

	public SequencedAssemblyEmiRecipe(SequencedAssemblyRecipe recipe) {
		super(CreateEmiPlugin.SEQUENCED_ASSEMBLY, recipe, 180, 120);
		for (SequencedRecipe<?> r : recipe.getSequence()) {
			width += getSubCategory(r).getWidth() + margin;
		}
		width -= margin;
	}

	@Override
	public int getDisplayWidth() {
		return Math.max(150, width);
	}

	@Override
	public void addWidgets(WidgetHolder widgets) {
		int xOff = recipe.getOutputChance() == 1 ? 0 : -7;
		int mid = widgets.getWidth() / 2;

		addTexture(widgets, AllGuiTextures.JEI_LONG_ARROW, mid - 38 + xOff, 94);

		widgets.addDrawable(mid - 38 + xOff, 94, AllGuiTextures.JEI_LONG_ARROW.width, 20, (matrices, mx, my, delta) -> {})
			.tooltip((mouseX, mouseY) -> List.of(ClientTooltipComponent.create(
				Lang.translate("recipe.assembly.repeat", recipe.getLoops()).getVisualOrderText())));
		
		if (recipe.getOutputChance() != 1) {
			float chance = recipe.getOutputChance();
			addTexture(widgets, AllGuiTextures.JEI_CHANCE_SLOT, mid + 60 + xOff, 90)
				.tooltip((mouseX, mouseY) -> List.of(
					ClientTooltipComponent.create(Lang.translate("recipe.assembly.junk").getVisualOrderText()),
					ClientTooltipComponent.create(Lang.translate("recipe.processing.chance", chance > 0.99 ? "<1" : 100 - (int) (chance * 100))
						.withStyle(ChatFormatting.GOLD).getVisualOrderText())
				));
		}

		addSlot(widgets, EmiIngredient.of(recipe.getIngredient()), mid - 64 + xOff, 90);

		addChancedSlot(widgets, EmiStack.of(recipe.getResultItem()), mid + 41 + xOff, 90, recipe.getOutputChance()).recipeContext(this);
		
		int sx = width / -2 + mid;
		int x = sx;
		int index = 0;
		for (SequencedRecipe<?> recipe : recipe.getSequence()) {
			SequencedAssemblySubCategory category = getSubCategory(recipe);
			category.addWidgets(widgets, x, 0, recipe, index++);
			x += category.getWidth() + margin;
		}

		widgets.addDrawable(0, 0, 0, 0, (matrices, mouseX, mouseY, delta) -> {
			Minecraft client = Minecraft.getInstance();
			matrices.pushPose();
			matrices.translate(0, 15, 0);
			if (recipe.getOutputChance() != 1) {
				client.font.drawShadow(matrices, "?", mid + 69 + xOff - client.font.width("?") / 2, 80, 0xefefef);
			}

			if (recipe.getLoops() > 1) {
				matrices.pushPose();
				matrices.translate(15, 9, 0);
				AllIcons.I_SEQ_REPEAT.render(matrices, mid - 40 + xOff, 75);
				client.font.drawShadow(matrices, "x" + recipe.getLoops(), mid - 24 + xOff, 80, 0x888888);
				matrices.popPose();
			}
			matrices.popPose();

			int cx = sx;
			for (int i = 0; i < recipe.getSequence().size(); i++) {
				String text = ROMAN[Math.min(i, ROMAN.length)];
				int w = getSubCategory(recipe.getSequence().get(i)).getWidth();
				int off = w / 2 - client.font.width(text) / 2;
				client.font.drawShadow(matrices, text, cx + off, 2, 0x888888);
				cx += w + margin;
			}
		});
	}

	public SequencedAssemblySubCategory getSubCategory(SequencedRecipe<?> recipe) {
		RecipeType<?> type = recipe.getRecipe().getType();
		if (type == AllRecipeTypes.PRESSING.getType()) {
			return new SequencedAssemblySubCategory.AssemblyPressing();
		} else if (type == AllRecipeTypes.FILLING.getType()) {
			return new SequencedAssemblySubCategory.AssemblySpouting();
		} else if (type == AllRecipeTypes.DEPLOYING.getType()) {
			return new SequencedAssemblySubCategory.AssemblyDeploying();
		} else if (type == AllRecipeTypes.CUTTING.getType()) {
			return new SequencedAssemblySubCategory.AssemblyCutting();
		}
		return null;
	}
}
