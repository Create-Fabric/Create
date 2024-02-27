package com.simibubi.create.compat.rei.category;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllFluids;
import com.simibubi.create.compat.rei.display.CreateDisplay;
import com.simibubi.create.content.fluids.potion.PotionFluidHandler;
import com.simibubi.create.content.processing.recipe.ProcessingOutput;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import com.simibubi.create.foundation.fluid.FluidIngredient;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.infrastructure.config.AllConfigs;

import io.github.fabricators_of_create.porting_lib.util.FluidStack;
import io.github.fabricators_of_create.porting_lib.util.FluidTextUtil;
import io.github.fabricators_of_create.porting_lib.util.FluidUnit;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Slot;
import me.shedaniel.rei.api.client.gui.widgets.Tooltip;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.client.util.ClientEntryStacks;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;

public abstract class CreateRecipeCategory<T extends Recipe<?>> implements DisplayCategory<CreateDisplay<T>> {

	protected final CategoryIdentifier<CreateDisplay<T>> type;
	protected final Component title;
	protected final Renderer background;
	protected final Renderer icon;

	private final Supplier<List<T>> recipes;
	private final List<Supplier<? extends ItemStack>> catalysts;

	private final int width;
	private final int height;

	private final Function<T, ? extends CreateDisplay<T>> displayFactory;

	public CreateRecipeCategory(Info<T> info) {
		this.type = info.recipeType();
		this.title = info.title();
		this.background = info.background();
		this.icon = info.icon();
		this.recipes = info.recipes();
		this.catalysts = info.catalysts();
		this.width = info.width();
		this.height = info.height();
		this.displayFactory = info.displayFactory();
	}

	@Override
	public CategoryIdentifier<CreateDisplay<T>> getCategoryIdentifier() {
		return type;
	}

	public void registerRecipes(DisplayRegistry registry) {
		for (T recipe : recipes.get()) {
			registry.add(displayFactory.apply(recipe), recipe);
		}
	}

	public void registerCatalysts(CategoryRegistry registry) {
		catalysts.forEach(s -> registry.addWorkstations(type, EntryStack.of(VanillaEntryTypes.ITEM, s.get())));
	}

	@Override
	public Component getTitle() {
		return title;
	}

	@Override
	public int getDisplayHeight() {
		return height;
	}

	@Override
	public int getDisplayWidth(CreateDisplay<T> display) {
		return width;
	}

	@Override
	public Renderer getIcon() {
		return icon;
	}

	public static AllGuiTextures getRenderedSlot(Recipe<?> recipe, int index) {
		AllGuiTextures jeiSlot = AllGuiTextures.JEI_SLOT;
		if (!(recipe instanceof ProcessingRecipe))
			return jeiSlot;
		ProcessingRecipe<?> processingRecipe = (ProcessingRecipe<?>) recipe;
		List<ProcessingOutput> rollableResults = processingRecipe.getRollableResults();
		if (rollableResults.size() <= index)
			return jeiSlot;
		if (processingRecipe.getRollableResults()
			.get(index)
			.getChance() == 1)
			return jeiSlot;
		return AllGuiTextures.JEI_CHANCE_SLOT;
	}

	public static void addStochasticTooltip(List<Widget> itemStacks, List<ProcessingOutput> results) {
		addStochasticTooltip(itemStacks, results, 1);
	}

	public static void addStochasticTooltip(List<Widget> itemStacks, List<ProcessingOutput> results,
											int startIndex) {
		itemStacks.stream().filter(widget -> widget instanceof Slot).forEach(widget -> {
			Slot slot = (Slot) widget;

			int slotIndex = itemStacks.indexOf(widget);
			
			ClientEntryStacks.setTooltipProcessor(slot.getCurrentEntry(), (entryStack, tooltip) -> {
				if (slotIndex < startIndex)
					return;
				ProcessingOutput output = results.get(slotIndex - startIndex);
				float chance = output.getChance();
				if (chance != 1)
					tooltip.add(Lang.translateDirect("recipe.processing.chance", chance < 0.01 ? "<1" : (int) (chance * 100))
							.withStyle(ChatFormatting.GOLD));
				return tooltip;
			});
		});
	}

	public static void addStochasticTooltip(ProcessingOutput output, Tooltip tooltip) {
		float chance = output.getChance();
		if (chance != 1)
			tooltip.add(Lang.translateDirect("recipe.processing.chance", chance < 0.01 ? "<1" : (int) (chance * 100))
					.withStyle(ChatFormatting.GOLD));
	}

	public static Slot basicSlot(int x, int y) {
		return Widgets.createSlot(point(x, y)).disableBackground();
	}

	public static Slot basicSlot(int x, int y, Point origin) {
		return Widgets.createSlot(point(origin.getX() + x, origin.getY() + y)).disableBackground();
	}

	public static List<FluidStack> withImprovedVisibility(List<FluidStack> stacks) {
		return stacks.stream()
			.map(CreateRecipeCategory::withImprovedVisibility)
			.collect(Collectors.toList());
	}

	public static FluidStack withImprovedVisibility(FluidStack stack) {
		FluidStack display = stack.copy();
		long displayedAmount = (long) (stack.getAmount() * .75f) + 250;
		display.setAmount(displayedAmount);
		return display;
	}

	public static dev.architectury.fluid.FluidStack convertToREIFluid(FluidStack stack) {
		return dev.architectury.fluid.FluidStack.create(stack.getFluid(), stack.getAmount(), stack.getTag());
	}

	public static List<dev.architectury.fluid.FluidStack> convertToREIFluids(List<FluidStack> stacks) {
		List<dev.architectury.fluid.FluidStack> newFluids = new ArrayList<>();
		stacks.forEach(fluidStack -> newFluids.add(convertToREIFluid(fluidStack)));
		return newFluids;
	}

	public static void setFluidRenderRatio(Slot slot) {
		slot.getEntries().forEach(entryStack -> {
			dev.architectury.fluid.FluidStack stack = entryStack.castValue();
			ClientEntryStacks.setFluidRenderRatio(entryStack.cast(), stack.getAmount() / (float) FluidConstants.BUCKET);
		});
	}

	public static void addFluidTooltip(List<Widget> fluidStacks, List<FluidIngredient> inputs,
		List<FluidStack> outputs) {
		addFluidTooltip(fluidStacks, inputs, outputs, -1);
	}

	public static void addFluidTooltip(List<Widget> fluidStacks, List<FluidIngredient> inputs,
		List<FluidStack> outputs, int index) {
		List<Long> amounts = new ArrayList<>();
		inputs.forEach(f -> amounts.add(f.getRequiredAmount()));
		outputs.forEach(f -> amounts.add(f.getAmount()));

		fluidStacks.stream().filter(widget -> widget instanceof Slot slot && slot.getCurrentEntry().getType() == VanillaEntryTypes.FLUID).forEach(widget -> {
			Slot slot = (Slot) widget;
			ClientEntryStacks.setTooltipProcessor(slot.getCurrentEntry(), (entryStack, tooltip) -> {
				dev.architectury.fluid.FluidStack fluidStack = entryStack.castValue();
				FluidStack fluid = new FluidStack(fluidStack.getFluid(), fluidStack.getAmount(), fluidStack.getTag());
				if (fluid.getFluid()
						.isSame(AllFluids.POTION.get())) {
					Component name = fluid.getDisplayName();
					if (tooltip.entries().isEmpty())
						tooltip.entries().add(0, Tooltip.entry(name));
					else
						tooltip.entries().set(0, Tooltip.entry(name));

					ArrayList<Component> potionTooltip = new ArrayList<>();
					PotionFluidHandler.addPotionTooltip(fluid, potionTooltip, 1);
					ArrayList<Tooltip.Entry> potionEntries = new ArrayList<>();
					potionTooltip.forEach(component -> potionEntries.add(Tooltip.entry(component)));
					// why 2 here??? it works though
					tooltip.entries().addAll(2, potionEntries.stream().toList());
				}

				FluidUnit unit = AllConfigs.client().fluidUnitType.get();
				String amount = FluidTextUtil.getUnicodeMillibuckets(amounts.get(0), unit, AllConfigs.client().simplifyFluidUnit.get());
				Component text = new TextComponent(String.valueOf(amount)).append(Lang.translateDirect(unit.getTranslationKey())).withStyle(ChatFormatting.GOLD);
				if (tooltip.entries().isEmpty())
					tooltip.entries().add(0, Tooltip.entry(text));
				else {
					List<Component> siblings = tooltip.entries().get(0).getAsText().getSiblings();
					siblings.add(new TextComponent(" "));
					siblings.add(text);
				}
				tooltip.entries().remove(1); // Remove REI added amount
				return tooltip;
			});
		});
	}

	public static void setFluidTooltip(Slot slot) {
		ClientEntryStacks.setTooltipProcessor(slot.getCurrentEntry(), (entryStack, tooltip) -> {
			dev.architectury.fluid.FluidStack fluidStack = entryStack.castValue();
			FluidStack fluid = new FluidStack(fluidStack.getFluid(), fluidStack.getAmount(), fluidStack.getTag());
			if (fluid.getFluid()
					.isSame(AllFluids.POTION.get())) {
				Component name = fluid.getDisplayName();
				if (tooltip.entries().isEmpty())
					tooltip.entries().add(0, Tooltip.entry(name));
				else
					tooltip.entries().set(0, Tooltip.entry(name));

				ArrayList<Component> potionTooltip = new ArrayList<>();
				PotionFluidHandler.addPotionTooltip(fluid, potionTooltip, 1);
				ArrayList<Tooltip.Entry> potionEntries = new ArrayList<>();
				potionTooltip.forEach(component -> potionEntries.add(Tooltip.entry(component)));
				tooltip.entries().addAll(1, potionEntries.stream().toList());
			}

			FluidUnit unit = AllConfigs.client().fluidUnitType.get();
			String amount = FluidTextUtil.getUnicodeMillibuckets(fluid.getAmount(), unit, AllConfigs.client().simplifyFluidUnit.get());
			Component text = new TextComponent(String.valueOf(amount)).append(Lang.translateDirect("generic.unit.millibuckets")).withStyle(ChatFormatting.GOLD);
			if (tooltip.entries().isEmpty())
				tooltip.entries().add(0, Tooltip.entry(text));
			else {
				List<Component> siblings = tooltip.entries().get(0).getAsText().getSiblings();
				siblings.add(new TextComponent(" "));
				siblings.add(text);
			}
			tooltip.entries().remove(1); // Remove REI added amount
			return tooltip;
		});
	}

	public static Point point(int x, int y) {
		return new Point(x, y);
	}

	public void addWidgets(CreateDisplay<T> display, List<Widget> ingredients, Point origin) {

	}

	public void addWidgets(CreateDisplay<T> display, List<Widget> ingredients, Point origin, Rectangle bounds) {

	}

	@Override
	public List<Widget> setupDisplay(CreateDisplay<T> display, Rectangle bounds) {
		List<Widget> widgets = new ArrayList<>();
		widgets.add(Widgets.createRecipeBase(bounds));
		widgets.add(Widgets.createDrawableWidget((helper, poseStack, mouseX, mouseY, partialTick) -> {
			poseStack.pushPose();
			poseStack.translate(bounds.getX(), bounds.getY() + 4, 0);
			draw(display.getRecipe(), poseStack, mouseX, mouseY);
			draw(display.getRecipe(), display, poseStack, mouseX, mouseY);
			poseStack.popPose();
		}));
		addWidgets(display, widgets, new Point(bounds.getX(), bounds.getY() + 4));
		addWidgets(display, widgets, new Point(bounds.getX(), bounds.getY() + 4), bounds);
		return widgets;
	}

	public void draw(T recipe, PoseStack matrixStack, double mouseX, double mouseY) {}

	public void draw(T recipe, CreateDisplay<T> display, PoseStack matrixStack, double mouseX, double mouseY) {}

	public record Info<T extends Recipe<?>>(CategoryIdentifier<CreateDisplay<T>> recipeType, Component title, Renderer background, Renderer icon, Supplier<List<T>> recipes, List<Supplier<? extends ItemStack>> catalysts, int width, int height, Function<T, ? extends CreateDisplay<T>> displayFactory) {
	}

	public interface Factory<T extends Recipe<?>> {
		CreateRecipeCategory<T> create(CreateRecipeCategory.Info<T> info);
	}
}
