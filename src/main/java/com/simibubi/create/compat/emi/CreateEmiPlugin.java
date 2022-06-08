package com.simibubi.create.compat.emi;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.compat.emi.BlockCuttingEmiRecipe.CondensedBlockCuttingRecipe;
import com.simibubi.create.compat.rei.ConversionRecipe;
import com.simibubi.create.compat.rei.ToolboxColoringRecipeMaker;
import com.simibubi.create.content.contraptions.components.crafter.MechanicalCraftingRecipe;
import com.simibubi.create.content.contraptions.components.crusher.CrushingRecipe;
import com.simibubi.create.content.contraptions.components.millstone.MillingRecipe;
import com.simibubi.create.content.contraptions.components.mixer.MixingRecipe;
import com.simibubi.create.content.contraptions.components.press.MechanicalPressTileEntity;
import com.simibubi.create.content.contraptions.components.saw.SawTileEntity;
import com.simibubi.create.content.contraptions.fluids.VirtualFluid;
import com.simibubi.create.content.contraptions.fluids.actors.FillingRecipe;
import com.simibubi.create.content.contraptions.fluids.actors.GenericItemFilling;
import com.simibubi.create.content.contraptions.fluids.potion.PotionFluidHandler;
import com.simibubi.create.content.contraptions.fluids.recipe.PotionMixingRecipes;
import com.simibubi.create.content.contraptions.processing.BasinRecipe;
import com.simibubi.create.content.contraptions.processing.EmptyingRecipe;
import com.simibubi.create.content.contraptions.processing.ProcessingRecipeBuilder;
import com.simibubi.create.foundation.fluid.FluidIngredient;
import com.simibubi.create.foundation.gui.container.AbstractSimiContainerScreen;
import com.simibubi.create.foundation.item.TagDependentIngredientItem;

import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiCraftingRecipe;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.Bounds;
import io.github.fabricators_of_create.porting_lib.transfer.TransferUtil;
import io.github.fabricators_of_create.porting_lib.util.FluidStack;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.BlastingRecipe;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.item.crafting.SmokingRecipe;
import net.minecraft.world.level.material.Fluids;

public class CreateEmiPlugin implements EmiPlugin {
	public static final EmiRecipeCategory MILLING = new EmiRecipeCategory(
		new ResourceLocation("create", "milling"), EmiStack.of(AllBlocks.MILLSTONE.get()));
	public static final EmiRecipeCategory CRUSHING = new EmiRecipeCategory(
		new ResourceLocation("create", "crushing"), EmiStack.of(AllBlocks.CRUSHING_WHEEL.get()));
	public static final EmiRecipeCategory SANDPAPER_POLISHING = new EmiRecipeCategory(
		new ResourceLocation("create", "sandpaper_polishing"), EmiStack.of(AllItems.SAND_PAPER.get()));
	public static final EmiRecipeCategory PRESSING = new EmiRecipeCategory(
		new ResourceLocation("create", "pressing"), EmiStack.of(AllBlocks.MECHANICAL_PRESS.get()));
	public static final EmiRecipeCategory FAN_WASHING = new EmiRecipeCategory(
		new ResourceLocation("create", "fan_washing"), EmiStack.of(AllItems.PROPELLER.get()));
	public static final EmiRecipeCategory FAN_SMOKING = new EmiRecipeCategory(
		new ResourceLocation("create", "fan_smoking"), EmiStack.of(AllItems.PROPELLER.get()));
	public static final EmiRecipeCategory FAN_BLASTING = new EmiRecipeCategory(
		new ResourceLocation("create", "fan_blasting"), EmiStack.of(AllItems.PROPELLER.get()));
	public static final EmiRecipeCategory FAN_HAUNTING = new EmiRecipeCategory(
		new ResourceLocation("create", "fan_haunting"), EmiStack.of(AllItems.PROPELLER.get()));
	public static final EmiRecipeCategory MIXING = new EmiRecipeCategory(
		new ResourceLocation("create", "mixing"), EmiStack.of(AllBlocks.MECHANICAL_MIXER.get()));
	public static final EmiRecipeCategory SHAPELESS = new EmiRecipeCategory(
		new ResourceLocation("create", "shapeless"), EmiStack.of(AllBlocks.MECHANICAL_MIXER.get()));
	public static final EmiRecipeCategory BREWING = new EmiRecipeCategory(
		new ResourceLocation("create", "brewing"), EmiStack.of(AllBlocks.MECHANICAL_MIXER.get()));
	public static final EmiRecipeCategory SAWING = new EmiRecipeCategory(
		new ResourceLocation("create", "sawing"), EmiStack.of(AllBlocks.MECHANICAL_SAW.get()));
	public static final EmiRecipeCategory BLOCK_CUTTING = new EmiRecipeCategory(
		new ResourceLocation("create", "block_cutting"), EmiStack.of(AllBlocks.MECHANICAL_SAW.get()));
	public static final EmiRecipeCategory WOOD_CUTTING = new EmiRecipeCategory(
		new ResourceLocation("create", "wood_cutting"), EmiStack.of(AllBlocks.MECHANICAL_SAW.get()));
	public static final EmiRecipeCategory PACKING = new EmiRecipeCategory(
		new ResourceLocation("create", "packing"), EmiStack.of(AllBlocks.MECHANICAL_PRESS.get()));
	public static final EmiRecipeCategory AUTOMATIC_PACKING = new EmiRecipeCategory(
		new ResourceLocation("create", "automatic_packing"), EmiStack.of(AllBlocks.MECHANICAL_PRESS.get()));
	public static final EmiRecipeCategory DEPLOYING = new EmiRecipeCategory(
		new ResourceLocation("create", "deploying"), EmiStack.of(AllBlocks.DEPLOYER.get()));
	public static final EmiRecipeCategory MYSTERY_CONVERSION = new EmiRecipeCategory(
		new ResourceLocation("create", "mystery_conversion"), EmiStack.of(AllItems.CHROMATIC_COMPOUND.get()));
	public static final EmiRecipeCategory SPOUT_FILLING = new EmiRecipeCategory(
		new ResourceLocation("create", "spout_filling"), EmiStack.of(AllBlocks.SPOUT.get()));
	public static final EmiRecipeCategory DRAINING = new EmiRecipeCategory(
		new ResourceLocation("create", "draining"), EmiStack.of(AllBlocks.ITEM_DRAIN.get()));
	public static final EmiRecipeCategory SHAPED = new EmiRecipeCategory(
		new ResourceLocation("create", "shaped"), EmiStack.of(AllBlocks.MECHANICAL_CRAFTER.get()));
	public static final EmiRecipeCategory MECHANICAL_CRAFTING = new EmiRecipeCategory(
		new ResourceLocation("create", "mechanical_crafting"), EmiStack.of(AllBlocks.MECHANICAL_CRAFTER.get()));
	public static final EmiRecipeCategory SEQUENCED_ASSEMBLY = new EmiRecipeCategory(
		new ResourceLocation("create", "sequenced_assembly"), EmiStack.of(AllItems.PRECISION_MECHANISM.get()));

	@SuppressWarnings({"rawtypes", "unchecked"})
	@Override
	public void register(EmiRegistry registry) {
		registry.removeEmiStacks(s -> {
			if (s.getKey() instanceof ItemVariant item) {
				if (item.getItem() instanceof TagDependentIngredientItem tag) {
					return tag.shouldHide();
				}
			} else if (s.getKey() instanceof FluidVariant fluid) {
				return fluid.getFluid() instanceof VirtualFluid;
			}
			return false;
		});

		registry.addGenericExclusionArea((screen, consumer) -> {
			if (screen instanceof AbstractSimiContainerScreen<?> simi) {
				simi.getExtraAreas().forEach(r -> consumer.accept(new Bounds(r.getX(), r.getY(), r.getWidth(), r.getHeight())));
			}
		});

		// TODO potentially add all potion fluid variants, drag drop handler and blueprint handler are unimplemented
		
		ToolboxColoringRecipeMaker.createRecipes().forEach(r -> {
			registry.addRecipe(new EmiCraftingRecipe(
				r.getIngredients().stream().map(EmiIngredient::of).toList(),
				EmiStack.of(r.getResultItem()), null));
		});

		registry.addCategory(MILLING);
		registry.addCategory(CRUSHING);
		registry.addCategory(SANDPAPER_POLISHING);
		registry.addCategory(PRESSING);
		registry.addCategory(FAN_WASHING);
		registry.addCategory(FAN_SMOKING);
		registry.addCategory(FAN_BLASTING);
		registry.addCategory(FAN_HAUNTING);
		registry.addCategory(MIXING);
		registry.addCategory(SHAPELESS);
		registry.addCategory(BREWING);
		registry.addCategory(SAWING);
		registry.addCategory(BLOCK_CUTTING);
		registry.addCategory(WOOD_CUTTING);
		registry.addCategory(PACKING);
		registry.addCategory(AUTOMATIC_PACKING);
		registry.addCategory(DEPLOYING);
		registry.addCategory(MYSTERY_CONVERSION);
		registry.addCategory(SPOUT_FILLING);
		registry.addCategory(DRAINING);
		registry.addCategory(SHAPED);
		registry.addCategory(MECHANICAL_CRAFTING);
		registry.addCategory(SEQUENCED_ASSEMBLY);

		registry.addWorkstation(MILLING, EmiStack.of(AllBlocks.MILLSTONE.get()));
		registry.addWorkstation(CRUSHING, EmiStack.of(AllBlocks.CRUSHING_WHEEL.get()));
		registry.addWorkstation(SANDPAPER_POLISHING, EmiStack.of(AllItems.SAND_PAPER.get()));
		registry.addWorkstation(PRESSING, EmiStack.of(AllBlocks.MECHANICAL_PRESS.get()));
		registry.addWorkstation(FAN_WASHING, EmiStack.of(AllItems.PROPELLER.get()));
		registry.addWorkstation(FAN_SMOKING, EmiStack.of(AllItems.PROPELLER.get()));
		registry.addWorkstation(FAN_BLASTING, EmiStack.of(AllItems.PROPELLER.get()));
		registry.addWorkstation(FAN_HAUNTING, EmiStack.of(AllItems.PROPELLER.get()));
		registry.addWorkstation(MIXING, EmiStack.of(AllBlocks.MECHANICAL_MIXER.get()));
		registry.addWorkstation(MIXING, EmiStack.of(AllBlocks.BASIN.get()));
		registry.addWorkstation(SHAPELESS, EmiStack.of(AllBlocks.MECHANICAL_MIXER.get()));
		registry.addWorkstation(SHAPELESS, EmiStack.of(AllBlocks.BASIN.get()));
		registry.addWorkstation(BREWING, EmiStack.of(AllBlocks.MECHANICAL_MIXER.get()));
		registry.addWorkstation(BREWING, EmiStack.of(AllBlocks.BASIN.get()));
		registry.addWorkstation(SAWING, EmiStack.of(AllBlocks.MECHANICAL_SAW.get()));
		registry.addWorkstation(BLOCK_CUTTING, EmiStack.of(AllBlocks.MECHANICAL_SAW.get()));
		registry.addWorkstation(WOOD_CUTTING, EmiStack.of(AllBlocks.MECHANICAL_SAW.get()));
		registry.addWorkstation(PACKING, EmiStack.of(AllBlocks.MECHANICAL_PRESS.get()));
		registry.addWorkstation(PACKING, EmiStack.of(AllBlocks.BASIN.get()));
		registry.addWorkstation(AUTOMATIC_PACKING, EmiStack.of(AllBlocks.MECHANICAL_PRESS.get()));
		registry.addWorkstation(AUTOMATIC_PACKING, EmiStack.of(AllBlocks.BASIN.get()));
		registry.addWorkstation(DEPLOYING, EmiStack.of(AllBlocks.DEPLOYER.get()));
		registry.addWorkstation(SPOUT_FILLING, EmiStack.of(AllBlocks.SPOUT.get()));
		registry.addWorkstation(DRAINING, EmiStack.of(AllBlocks.ITEM_DRAIN.get()));
		registry.addWorkstation(SHAPED, EmiStack.of(AllBlocks.MECHANICAL_CRAFTER.get()));
		registry.addWorkstation(MECHANICAL_CRAFTING, EmiStack.of(AllBlocks.MECHANICAL_CRAFTER.get()));

		RecipeManager manager = registry.getRecipeManager();

		List<MillingRecipe> millingRecipes = (List<MillingRecipe>) (List) manager.getAllRecipesFor(AllRecipeTypes.MILLING.getType());
		List<CrushingRecipe> crushingRecipes = (List<CrushingRecipe>) (List) manager.getAllRecipesFor(AllRecipeTypes.CRUSHING.getType());
		List<SmokingRecipe> smokingRecipes = manager.getAllRecipesFor(RecipeType.SMOKING);
		List<BlastingRecipe> blastingRecipes = manager.getAllRecipesFor(RecipeType.BLASTING);
		for (CrushingRecipe recipe : crushingRecipes) {
			registry.addRecipe(new CrushingEmiRecipe(recipe));
		}
		outer:
		for (MillingRecipe recipe : millingRecipes) {
			registry.addRecipe(new MillingEmiRecipe(recipe));
			for (CrushingRecipe crush : crushingRecipes) {
				if (doInputsMatch(recipe, crush)) {
					continue outer;
				}
			}
			registry.addRecipe(new CrushingEmiRecipe(recipe));
		}
		addAll(registry, AllRecipeTypes.SANDPAPER_POLISHING, PolishingEmiRecipe::new);
		addAll(registry, AllRecipeTypes.PRESSING, PressingEmiRecipe::new);
		for (SmokingRecipe recipe : smokingRecipes) {
			registry.addRecipe(new FanSmokingEmiRecipe(recipe));
		}
		outer:
		for (AbstractCookingRecipe recipe : manager.getAllRecipesFor(RecipeType.SMELTING)) {
			for (AbstractCookingRecipe smoking : smokingRecipes) {
				if (doInputsMatch(recipe, smoking)) {
					continue outer;
				}
			}
			for (AbstractCookingRecipe blasting : blastingRecipes) {
				if (doInputsMatch(recipe, blasting)) {
					continue outer;
				}
			}
			registry.addRecipe(new FanBlastingEmiRecipe(recipe));
		}
		for (AbstractCookingRecipe recipe : blastingRecipes) {
			registry.addRecipe(new FanBlastingEmiRecipe(recipe));
		}
		addAll(registry, AllRecipeTypes.SPLASHING, FanWashingEmiRecipe::new);
		addAll(registry, AllRecipeTypes.HAUNTING, FanHauntingEmiRecipe::new);
		addAll(registry, AllRecipeTypes.MIXING, MIXING, MixingEmiRecipe::new);
		for (CraftingRecipe recipe : manager.getAllRecipesFor(RecipeType.CRAFTING)) {
			if (recipe instanceof ShapelessRecipe && !MechanicalPressTileEntity.canCompress(recipe)
					&& !AllRecipeTypes.shouldIgnoreInAutomation(recipe)) {
				registry.addRecipe(new ShapelessEmiRecipe(SHAPELESS, BasinRecipe.convertShapeless(recipe)));
			}
		}
		for (MixingRecipe recipe : PotionMixingRecipes.ALL) {
			registry.addRecipe(new MixingEmiRecipe(BREWING, recipe));
		}
		addAll(registry, AllRecipeTypes.CUTTING, SawingEmiRecipe::new);
		for (CondensedBlockCuttingRecipe recipe : CondensedBlockCuttingRecipe
				.condenseRecipes(manager.getAllRecipesFor(RecipeType.STONECUTTING).stream()
				.filter(r -> !AllRecipeTypes.shouldIgnoreInAutomation(r)).toList(), "block_cutting")) {
			registry.addRecipe(new BlockCuttingEmiRecipe(BLOCK_CUTTING, recipe));
		}
		if (FabricLoader.getInstance().isModLoaded("druidcraft")) {
			for (CondensedBlockCuttingRecipe recipe : CondensedBlockCuttingRecipe
					.condenseRecipes(manager.getAllRecipesFor((RecipeType<Recipe<Container>>) SawTileEntity.woodcuttingRecipeType.get())
					.stream().filter(r -> !AllRecipeTypes.shouldIgnoreInAutomation(r)).toList(), "block_cutting")) {
				registry.addRecipe(new BlockCuttingEmiRecipe(BLOCK_CUTTING, recipe));
			}
		}
		addAll(registry, AllRecipeTypes.COMPACTING, PackingEmiRecipe::new);
		for (CraftingRecipe recipe : manager.getAllRecipesFor(RecipeType.CRAFTING)) {
			if (!(recipe instanceof MechanicalCraftingRecipe)
					&& MechanicalPressTileEntity.canCompress(recipe)
					&& !AllRecipeTypes.shouldIgnoreInAutomation(recipe)) {
				registry.addRecipe(new AutomaticPackingEmiRecipe(BasinRecipe.convertShapeless(recipe)));
			}
		}
		addAll(registry, AllRecipeTypes.DEPLOYING, DeployingEmiRecipe::new);
		for (ConversionRecipe recipe : MysteriousConversionEmiRecipe.RECIPES) {
			registry.addRecipe(new MysteriousConversionEmiRecipe(recipe));
		}
		addAll(registry, AllRecipeTypes.FILLING, SpoutEmiRecipe::new);
		addAll(registry, AllRecipeTypes.EMPTYING, DrainEmiRecipe::new);
		addAll(registry, AllRecipeTypes.SEQUENCED_ASSEMBLY, SequencedAssemblyEmiRecipe::new);
		addAll(registry, AllRecipeTypes.MECHANICAL_CRAFTING, MECHANICAL_CRAFTING, MechanicalCraftingEmiRecipe::new);

		// Introspective recipes based on present stacks need to make sure
		// all stacks are populated by other plugins
		registry.addDeferredRecipes(this::addDeferredRecipes);
	}

	@SuppressWarnings("unchecked")
	private <T extends Recipe<?>> void addAll(EmiRegistry registry, AllRecipeTypes type, Function<T, EmiRecipe> constructor) {
		for (T recipe : (List<T>) registry.getRecipeManager().getAllRecipesFor(type.getType())) {
			registry.addRecipe(constructor.apply(recipe));
		}
	}

	@SuppressWarnings("unchecked")
	private <T extends Recipe<?>> void addAll(EmiRegistry registry, AllRecipeTypes type, EmiRecipeCategory category,
			BiFunction<EmiRecipeCategory, T, EmiRecipe> constructor) {
		for (T recipe : (List<T>) registry.getRecipeManager().getAllRecipesFor(type.getType())) {
			registry.addRecipe(constructor.apply(category, recipe));
		}
	}

	private void addDeferredRecipes(Consumer<EmiRecipe> consumer) {
		List<FluidVariant> fluids = EmiApi.getIndexStacks().stream()
			.filter(s -> s.getKey() instanceof FluidVariant)
			.map(s -> (FluidVariant) s.getKey())
			.distinct()
			.toList();
		for (EmiStack stack : EmiApi.getIndexStacks()) {
			if (stack.getKey() instanceof ItemVariant iv) {
				Item i = iv.getItem();
				ItemStack is = stack.getItemStack();
				if (i instanceof PotionItem) {
					FluidStack potion = PotionFluidHandler.getFluidFromPotionItem(is);
					Ingredient bottle = Ingredient.of(Items.GLASS_BOTTLE);
					ResourceLocation iid = Registry.ITEM.getKey(i);
					ResourceLocation pid = Registry.POTION.getKey(PotionUtils.getPotion(is));
					consumer.accept(new SpoutEmiRecipe(new ProcessingRecipeBuilder<>(FillingRecipe::new,
						new ResourceLocation("emi", "create/potion_filling/" + pid.getNamespace() + "/" + pid.getPath()
							+ "/from/" + iid.getNamespace() + "/" + iid.getPath()))
								.withItemIngredients(bottle)
								.withFluidIngredients(FluidIngredient.fromFluidStack(potion))
								.withSingleItemOutput(is.copy())
								.build()));
					consumer.accept(new DrainEmiRecipe(new ProcessingRecipeBuilder<>(EmptyingRecipe::new,
						new ResourceLocation("emi", "create/potion_draining/" + pid.getNamespace() + "/" + pid.getPath()
							+ "/from/" + iid.getNamespace() + "/" + iid.getPath()))
								.withItemIngredients(Ingredient.of(is))
								.withFluidOutputs(potion)
								.withSingleItemOutput(new ItemStack(Items.GLASS_BOTTLE))
								.build()));
					continue;
				}
				for (FluidVariant fluid : fluids) {
					if (i == Items.GLASS_BOTTLE && fluid.getFluid() == Fluids.WATER) {
						continue;
					}
					// This will miss fluid containers that hold a minimum of over 1000 L, but perhaps that's preferable.
					FluidStack fs = new FluidStack(fluid, FluidConstants.BUCKET);
					ItemStack copy = is.copy();
					ContainerItemContext ctx = ContainerItemContext.withInitial(copy);
					Storage<FluidVariant> storage = ctx.find(FluidStorage.ITEM);
					if (storage != null && GenericItemFilling.isFluidHandlerValid(copy, storage)) {
						long inserted = 0;
						try (Transaction t = TransferUtil.getTransaction()) {
							inserted = storage.insert(fs.getType(), fs.getAmount(), t);
							t.commit();
						}
						fs.setAmount(inserted);
						ItemStack container = ctx.getItemVariant().toStack((int) ctx.getAmount());
						if (inserted != 0 && !container.sameItemStackIgnoreDurability(copy) && !container.isEmpty()) {
							Ingredient bucket = Ingredient.of(is);
							ResourceLocation itemId = Registry.ITEM.getKey(is.getItem());
							ResourceLocation fluidId = Registry.FLUID.getKey(fs.getFluid());
							consumer.accept(new SpoutEmiRecipe(new ProcessingRecipeBuilder<>(FillingRecipe::new,
							new ResourceLocation("emi", "create/filling/" + itemId.getNamespace() + "/" + itemId.getPath()
									+ "/with/" + fluidId.getNamespace() + "/" + fluidId.getPath()))
								.withItemIngredients(bucket)
								.withFluidIngredients(FluidIngredient.fromFluidStack(fs))
								.withSingleItemOutput(container)
								.build()));
						}
					}
				}
				ItemStack copy = is.copy();
				ContainerItemContext ctx = ContainerItemContext.withInitial(copy);
				Storage<FluidVariant> storage = ctx.find(FluidStorage.ITEM);
				if (storage != null) {
					FluidStack extracted = TransferUtil.extractAnyFluid(storage, FluidConstants.BUCKET);
					ItemStack result = ctx.getItemVariant().toStack((int) ctx.getAmount());
					if (!extracted.isEmpty() && !result.isEmpty()) {
						ResourceLocation itemId = Registry.ITEM.getKey(is.getItem());
						ResourceLocation fluidId = Registry.FLUID.getKey(extracted.getFluid());
						consumer.accept(new DrainEmiRecipe(new ProcessingRecipeBuilder<>(EmptyingRecipe::new,
							new ResourceLocation("emi", "create/draining/" + itemId.getNamespace() + "/" + itemId.getPath()
								+ "/from/" + fluidId.getNamespace() + "/" + fluidId.getPath()))
							.withItemIngredients(Ingredient.of(is))
							.withFluidOutputs(extracted)
							.withSingleItemOutput(result)
							.build()));
					}
				}
			}
		}
	}

	public static boolean doInputsMatch(Recipe<?> a, Recipe<?> b) {
		if (!a.getIngredients().isEmpty() && !b.getIngredients().isEmpty()) {
			ItemStack[] matchingStacks = a.getIngredients().get(0).getItems();
			if (matchingStacks.length != 0) {
				if (b.getIngredients().get(0).test(matchingStacks[0])) {
					return true;
				}
			}
		}
		return false;
	}
}
