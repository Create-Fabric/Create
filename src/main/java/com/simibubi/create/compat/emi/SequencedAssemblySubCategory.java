package com.simibubi.create.compat.emi;

import java.util.List;
import java.util.function.BiFunction;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import com.simibubi.create.content.contraptions.components.deployer.DeployerApplicationRecipe;
import com.simibubi.create.content.contraptions.itemAssembly.SequencedRecipe;
import com.simibubi.create.foundation.utility.Lang;

import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import io.github.fabricators_of_create.porting_lib.util.FluidStack;

public abstract class SequencedAssemblySubCategory {
	private final int width;

	public SequencedAssemblySubCategory(int width) {
		this.width = width;
	}

	public int getWidth() {
		return width;
	}

	public abstract void addWidgets(WidgetHolder widgets, int x, int y, SequencedRecipe<?> recipe, int index);

	// TODO tooltips reference first item in an ingredient, EMI has canonical names for tags, use that instead?
	public static BiFunction<Integer, Integer, List<ClientTooltipComponent>> getTooltip(SequencedRecipe<?> recipe, int index) {
		return (mouseX, mouseY) -> List.of(
			ClientTooltipComponent.create(Lang.translate("recipe.assembly.step", index + 1).getVisualOrderText()),
			ClientTooltipComponent.create(recipe.getAsAssemblyRecipe()
				.getDescriptionForAssembly()
				.plainCopy()
				.withStyle(ChatFormatting.DARK_GREEN).getVisualOrderText())
		);
	}

	public static class AssemblyPressing extends SequencedAssemblySubCategory {

		public AssemblyPressing() {
			super(25);
		}

		@Override
		public void addWidgets(WidgetHolder widgets, int x, int y, SequencedRecipe<?> recipe, int index) {
			widgets.addDrawable(x, y, getWidth(), 96, (matrices, mouseX, mouseY, delta) -> {
				float scale = 0.6f;
				matrices.translate(3, 54, 0);
				matrices.scale(scale, scale, scale);
				CreateEmiAnimations.renderPress(matrices, index, false);
			}).tooltip(getTooltip(recipe, index));
		}
	}

	public static class AssemblySpouting extends SequencedAssemblySubCategory {

		public AssemblySpouting() {
			super(25);
		}

		@Override
		public void addWidgets(WidgetHolder widgets, int x, int y, SequencedRecipe<?> recipe, int index) {
			FluidStack fluid = recipe.getRecipe().getFluidIngredients().get(0).getMatchingFluidStacks().get(0);
			CreateEmiRecipe.addSlot(widgets, CreateEmiRecipe.fluidStack(fluid), x + 3, y + 13);
			widgets.addDrawable(x, y, getWidth(), 96, (matrices, mouseX, mouseY, delta) -> {
				float scale = 0.75f;
				matrices.translate(3, 54, 0);
				matrices.scale(scale, scale, scale);
				CreateEmiAnimations.renderSpout(matrices, index, recipe.getRecipe()
					.getFluidIngredients().get(0).getMatchingFluidStacks());
			}).tooltip(getTooltip(recipe, index));
		}
	}

	public static class AssemblyDeploying extends SequencedAssemblySubCategory {

		public AssemblyDeploying() {
			super(25);
		}

		@Override
		public void addWidgets(WidgetHolder widgets, int x, int y, SequencedRecipe<?> recipe, int index) {
			EmiIngredient ingredient = EmiIngredient.of(recipe.getRecipe().getIngredients().get(1));
			if (recipe.getAsAssemblyRecipe() instanceof DeployerApplicationRecipe deploy && deploy.shouldKeepHeldItem()) {
				for (EmiStack stack : ingredient.getEmiStacks()) {
					stack.setRemainder(stack);
				}
			}
			CreateEmiRecipe.addSlot(widgets, ingredient, x + 3, y + 13);
			widgets.addDrawable(x, y, getWidth(), 96, (matrices, mouseX, mouseY, delta) -> {
				float scale = 0.75f;
				matrices.translate(3, 54, 0);
				matrices.scale(scale, scale, scale);
				CreateEmiAnimations.renderDeployer(matrices, index);
			}).tooltip(getTooltip(recipe, index));
		}
	}

	public static class AssemblyCutting extends SequencedAssemblySubCategory {

		public AssemblyCutting() {
			super(25);
		}

		@Override
		public void addWidgets(WidgetHolder widgets, int x, int y, SequencedRecipe<?> recipe, int index) {
			widgets.addDrawable(x, y, getWidth(), 96, (matrices, mouseX, mouseY, delta) -> {
				matrices.translate(0, 54.5f, 0);
				float scale = 0.6f;
				matrices.scale(scale, scale, scale);
				matrices.translate(getWidth() / 2, 30, 0);
				CreateEmiAnimations.renderSaw(matrices, index);
			}).tooltip(getTooltip(recipe, index));
		}
	}
}
