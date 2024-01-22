package com.simibubi.create.compat.emi;

import java.util.List;
import java.util.function.BiFunction;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.simibubi.create.compat.emi.recipes.CreateEmiRecipe;
import com.simibubi.create.content.kinetics.deployer.DeployerApplicationRecipe;
import com.simibubi.create.content.processing.sequenced.SequencedRecipe;
import com.simibubi.create.foundation.utility.Lang;

import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import io.github.fabricators_of_create.porting_lib.util.FluidStack;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;

public abstract class EmiSequencedAssemblySubCategory {
	private final int width;

	public EmiSequencedAssemblySubCategory(int width) {
		this.width = width;
	}

	public int getWidth() {
		return width;
	}

	public abstract void addWidgets(WidgetHolder widgets, int x, int y, SequencedRecipe<?> recipe, int index);

	@Nullable
	public EmiIngredient getAppliedIngredient(SequencedRecipe<?> recipe) {
		return null;
	}

	// TODO tooltips reference first item in an ingredient, EMI has canonical names for tags, use that instead?
	// I tried to implement this and Ingredients not exposing tags is painful
	public static BiFunction<Integer, Integer, List<ClientTooltipComponent>> getTooltip(SequencedRecipe<?> recipe, int index) {
		return (mouseX, mouseY) -> List.of(
			ClientTooltipComponent.create(Lang.translateDirect("recipe.assembly.step", index + 1).getVisualOrderText()),
			ClientTooltipComponent.create(recipe.getAsAssemblyRecipe()
				.getDescriptionForAssembly()
				.plainCopy()
				.withStyle(ChatFormatting.DARK_GREEN).getVisualOrderText())
		);
	}

	public static class AssemblyPressing extends EmiSequencedAssemblySubCategory {

		public AssemblyPressing() {
			super(25);
		}

		@Override
		public void addWidgets(WidgetHolder widgets, int x, int y, SequencedRecipe<?> recipe, int index) {
			widgets.addDrawable(x, y, getWidth(), 96, (graphics, mouseX, mouseY, delta) -> {
				PoseStack matrices = graphics.pose();
				float scale = 0.6f;
				matrices.translate(3, 54, 0);
				matrices.scale(scale, scale, scale);
				CreateEmiAnimations.renderPress(graphics, index, false);
			}).tooltip(getTooltip(recipe, index));
		}
	}

	public static class AssemblySpouting extends EmiSequencedAssemblySubCategory {

		public AssemblySpouting() {
			super(25);
		}

		@Override
		@NotNull
		public EmiIngredient getAppliedIngredient(SequencedRecipe<?> recipe) {
			FluidStack fluid = recipe.getRecipe().getFluidIngredients().get(0).getMatchingFluidStacks().get(0);
			return CreateEmiRecipe.fluidStack(fluid);
		}

		@Override
		public void addWidgets(WidgetHolder widgets, int x, int y, SequencedRecipe<?> recipe, int index) {
			CreateEmiRecipe.addSlot(widgets, getAppliedIngredient(recipe), x + 3, y + 13);
			widgets.addDrawable(x, y, getWidth(), 96, (graphics, mouseX, mouseY, delta) -> {
				PoseStack matrices = graphics.pose();
				float scale = 0.75f;
				matrices.translate(3, 54, 0);
				matrices.scale(scale, scale, scale);
				CreateEmiAnimations.renderSpout(graphics, index, recipe.getRecipe()
					.getFluidIngredients().get(0).getMatchingFluidStacks());
			}).tooltip(getTooltip(recipe, index));
		}
	}

	public static class AssemblyDeploying extends EmiSequencedAssemblySubCategory {

		public AssemblyDeploying() {
			super(25);
		}

		@Override
		@NotNull
		public EmiIngredient getAppliedIngredient(SequencedRecipe<?> recipe) {
			return EmiIngredient.of(recipe.getRecipe().getIngredients().get(1));
		}

		@Override
		public void addWidgets(WidgetHolder widgets, int x, int y, SequencedRecipe<?> recipe, int index) {
			EmiIngredient ingredient = getAppliedIngredient(recipe);
			if (recipe.getAsAssemblyRecipe() instanceof DeployerApplicationRecipe deploy && deploy.shouldKeepHeldItem()) {
				for (EmiStack stack : ingredient.getEmiStacks()) {
					stack.setRemainder(stack);
				}
			}
			CreateEmiRecipe.addSlot(widgets, ingredient, x + 3, y + 13);
			widgets.addDrawable(x, y, getWidth(), 96, (graphics, mouseX, mouseY, delta) -> {
				PoseStack matrices = graphics.pose();
				float scale = 0.75f;
				matrices.translate(3, 54, 0);
				matrices.scale(scale, scale, scale);
				CreateEmiAnimations.renderDeployer(graphics, index);
			}).tooltip(getTooltip(recipe, index));
		}
	}

	public static class AssemblyCutting extends EmiSequencedAssemblySubCategory {

		public AssemblyCutting() {
			super(25);
		}

		@Override
		public void addWidgets(WidgetHolder widgets, int x, int y, SequencedRecipe<?> recipe, int index) {
			widgets.addDrawable(x, y, getWidth(), 96, (graphics, mouseX, mouseY, delta) -> {
				PoseStack matrices = graphics.pose();
				matrices.translate(0, 54.5f, 0);
				float scale = 0.6f;
				matrices.scale(scale, scale, scale);
				matrices.translate(getWidth() / 2, 30, 0);
				CreateEmiAnimations.renderSaw(graphics, index);
			}).tooltip(getTooltip(recipe, index));
		}
	}
}
