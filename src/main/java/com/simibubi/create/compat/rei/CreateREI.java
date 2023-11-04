package com.simibubi.create.compat.rei;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import javax.annotation.ParametersAreNonnullByDefault;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.Create;
import com.simibubi.create.compat.rei.category.BlockCuttingCategory;
import com.simibubi.create.compat.rei.category.BlockCuttingCategory.CondensedBlockCuttingRecipe;
import com.simibubi.create.compat.rei.category.CreateRecipeCategory;
import com.simibubi.create.compat.rei.category.CrushingCategory;
import com.simibubi.create.compat.rei.category.DeployingCategory;
import com.simibubi.create.compat.rei.category.FanBlastingCategory;
import com.simibubi.create.compat.rei.category.FanHauntingCategory;
import com.simibubi.create.compat.rei.category.FanSmokingCategory;
import com.simibubi.create.compat.rei.category.FanWashingCategory;
import com.simibubi.create.compat.rei.category.ItemApplicationCategory;
import com.simibubi.create.compat.rei.category.ItemDrainCategory;
import com.simibubi.create.compat.rei.category.MechanicalCraftingCategory;
import com.simibubi.create.compat.rei.category.MillingCategory;
import com.simibubi.create.compat.rei.category.MixingCategory;
import com.simibubi.create.compat.rei.category.MysteriousItemConversionCategory;
import com.simibubi.create.compat.rei.category.PackingCategory;
import com.simibubi.create.compat.rei.category.PolishingCategory;
import com.simibubi.create.compat.rei.category.PressingCategory;
import com.simibubi.create.compat.rei.category.ProcessingViaFanCategory;
import com.simibubi.create.compat.rei.category.SawingCategory;
import com.simibubi.create.compat.rei.category.SequencedAssemblyCategory;
import com.simibubi.create.compat.rei.category.SpoutCategory;
import com.simibubi.create.compat.rei.display.BasinDisplay;
import com.simibubi.create.compat.rei.display.CreateDisplay;
import com.simibubi.create.content.equipment.sandPaper.SandPaperPolishingRecipe;
import com.simibubi.create.content.fluids.VirtualFluid;
import com.simibubi.create.content.fluids.potion.PotionMixingRecipes;
import com.simibubi.create.content.fluids.transfer.EmptyingRecipe;
import com.simibubi.create.content.fluids.transfer.FillingRecipe;
import com.simibubi.create.content.kinetics.crafter.MechanicalCraftingRecipe;
import com.simibubi.create.content.kinetics.crusher.AbstractCrushingRecipe;
import com.simibubi.create.content.kinetics.deployer.DeployerApplicationRecipe;
import com.simibubi.create.content.kinetics.deployer.ItemApplicationRecipe;
import com.simibubi.create.content.kinetics.deployer.ManualApplicationRecipe;
import com.simibubi.create.content.kinetics.fan.processing.HauntingRecipe;
import com.simibubi.create.content.kinetics.fan.processing.SplashingRecipe;
import com.simibubi.create.content.kinetics.press.MechanicalPressBlockEntity;
import com.simibubi.create.content.kinetics.press.PressingRecipe;
import com.simibubi.create.content.kinetics.saw.CuttingRecipe;
import com.simibubi.create.content.kinetics.saw.SawBlockEntity;
import com.simibubi.create.content.processing.basin.BasinRecipe;
import com.simibubi.create.content.processing.sequenced.SequencedAssemblyRecipe;
import com.simibubi.create.foundation.config.ConfigBase.ConfigBool;
import com.simibubi.create.foundation.data.recipe.LogStrippingFakeRecipes;
import com.simibubi.create.foundation.gui.menu.AbstractSimiContainerScreen;
import com.simibubi.create.foundation.item.TagDependentIngredientItem;
import com.simibubi.create.foundation.recipe.IRecipeTypeInfo;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.infrastructure.config.AllConfigs;
import com.simibubi.create.infrastructure.config.CRecipes;

import dev.architectury.fluid.FluidStack;
import io.github.fabricators_of_create.porting_lib.mixin.common.accessor.RecipeManagerAccessor;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.client.registry.entry.EntryRegistry;
import me.shedaniel.rei.api.client.registry.screen.ExclusionZones;
import me.shedaniel.rei.api.client.registry.screen.ScreenRegistry;
import me.shedaniel.rei.api.client.registry.transfer.TransferHandlerRegistry;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes;
import me.shedaniel.rei.plugin.common.BuiltinPlugin;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.SmokingRecipe;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;

@SuppressWarnings("unused")
@ParametersAreNonnullByDefault
public class CreateREI implements REIClientPlugin {

	private static final ResourceLocation ID = Create.asResource("rei_plugin");

	private final List<CreateRecipeCategory<?>> allCategories = new ArrayList<>();

	private void loadCategories() {
		allCategories.clear();

		CreateRecipeCategory<?>

		milling = builder(AbstractCrushingRecipe.class).addTypedRecipes(AllRecipeTypes.MILLING)
		.catalyst(AllBlocks.MILLSTONE::get).doubleItemIcon(AllBlocks.MILLSTONE.get(), AllItems.WHEAT_FLOUR.get())
				.emptyBackground(177, 59)
		.build("milling", MillingCategory::new),

		crushing = builder(AbstractCrushingRecipe.class)
				.addTypedRecipes(AllRecipeTypes.CRUSHING)
				.addTypedRecipesExcluding(AllRecipeTypes.MILLING::getType, AllRecipeTypes.CRUSHING::getType)
				.catalyst(AllBlocks.CRUSHING_WHEEL::get)
				.doubleItemIcon(AllBlocks.CRUSHING_WHEEL.get(), AllItems.CRUSHED_GOLD.get())
				.emptyBackground(177, 106)
				.build("crushing", CrushingCategory::new),

		pressing = builder(PressingRecipe.class)
				.addTypedRecipes(AllRecipeTypes.PRESSING)
				.catalyst(AllBlocks.MECHANICAL_PRESS::get)
				.doubleItemIcon(AllBlocks.MECHANICAL_PRESS.get(), AllItems.IRON_SHEET.get())
				.emptyBackground(177, 88)
				.build("pressing", PressingCategory::new),

		washing = builder(SplashingRecipe.class)
				.addTypedRecipes(AllRecipeTypes.SPLASHING)
				.catalystStack(ProcessingViaFanCategory.getFan("fan_washing"))
				.doubleItemIcon(AllItems.PROPELLER.get(), Items.WATER_BUCKET)
				.emptyBackground(178, 75)
				.build("fan_washing", FanWashingCategory::new),

		smoking = builder(SmokingRecipe.class)
				.addTypedRecipes(() -> RecipeType.SMOKING)
				.catalystStack(ProcessingViaFanCategory.getFan("fan_smoking"))
				.doubleItemIcon(AllItems.PROPELLER.get(), Items.CAMPFIRE)
				.emptyBackground(178, 75)
				.build("fan_smoking", FanSmokingCategory::new),

		blasting = builder(AbstractCookingRecipe.class)
				.addTypedRecipesExcluding(() -> RecipeType.SMELTING, () -> RecipeType.BLASTING)
				.addTypedRecipes(() -> RecipeType.BLASTING)
				.removeRecipes(() -> RecipeType.SMOKING)
				.catalystStack(ProcessingViaFanCategory.getFan("fan_blasting"))
				.doubleItemIcon(AllItems.PROPELLER.get(), Items.LAVA_BUCKET)
				.emptyBackground(178, 75)
				.build("fan_blasting", FanBlastingCategory::new),

		haunting = builder(HauntingRecipe.class)
				.addTypedRecipes(AllRecipeTypes.HAUNTING)
				.catalystStack(ProcessingViaFanCategory.getFan("fan_haunting"))
				.doubleItemIcon(AllItems.PROPELLER.get(), Items.SOUL_CAMPFIRE)
				.emptyBackground(178, 75)
				.build("fan_haunting", FanHauntingCategory::new),

		mixing = builder(BasinRecipe.class)
				.addTypedRecipes(AllRecipeTypes.MIXING)
				.catalyst(AllBlocks.MECHANICAL_MIXER::get)
				.catalyst(AllBlocks.BASIN::get)
				.doubleItemIcon(AllBlocks.MECHANICAL_MIXER.get(), AllBlocks.BASIN.get())
				.emptyBackground(177, 110)
				.displayFactory(BasinDisplay::mixing)
				.build("mixing", MixingCategory::standard),

		autoShapeless = builder(BasinRecipe.class)
				.enableWhen(c -> c.allowShapelessInMixer)
				.addAllRecipesIf(r -> r instanceof CraftingRecipe && !(r instanceof ShapedRecipe)
								&& r.getIngredients()
								.size() > 1
								&& !MechanicalPressBlockEntity.canCompress(r) && !AllRecipeTypes.shouldIgnoreInAutomation(r),
						BasinRecipe::convertShapeless)
				.catalyst(AllBlocks.MECHANICAL_MIXER::get)
				.catalyst(AllBlocks.BASIN::get)
				.doubleItemIcon(AllBlocks.MECHANICAL_MIXER.get(), Items.CRAFTING_TABLE)
				.emptyBackground(177, 94)
				.displayFactory(BasinDisplay::autoShapeless)
				.build("automatic_shapeless", MixingCategory::autoShapeless),

		brewing = builder(BasinRecipe.class)
				.addRecipes(() -> PotionMixingRecipes.ALL)
				.catalyst(AllBlocks.MECHANICAL_MIXER::get)
				.catalyst(AllBlocks.BASIN::get)
				.doubleItemIcon(AllBlocks.MECHANICAL_MIXER.get(), Blocks.BREWING_STAND)
				.emptyBackground(177, 110)
				.displayFactory(BasinDisplay::brewing)
				.build("automatic_brewing", MixingCategory::autoBrewing),

		packing = builder(BasinRecipe.class)
				.addTypedRecipes(AllRecipeTypes.COMPACTING)
				.catalyst(AllBlocks.MECHANICAL_PRESS::get)
				.catalyst(AllBlocks.BASIN::get)
				.doubleItemIcon(AllBlocks.MECHANICAL_PRESS.get(), AllBlocks.BASIN.get())
				.emptyBackground(177, 110)
				.displayFactory(BasinDisplay::packing)
				.build("packing", PackingCategory::standard),

		autoSquare = builder(BasinRecipe.class)
				.enableWhen(c -> c.allowShapedSquareInPress)
				.addAllRecipesIf(
						r -> (r instanceof CraftingRecipe) && !(r instanceof MechanicalCraftingRecipe)
								&& MechanicalPressBlockEntity.canCompress(r) && !AllRecipeTypes.shouldIgnoreInAutomation(r),
						BasinRecipe::convertShapeless)
				.catalyst(AllBlocks.MECHANICAL_PRESS::get)
				.catalyst(AllBlocks.BASIN::get)
				.doubleItemIcon(AllBlocks.MECHANICAL_PRESS.get(), Blocks.CRAFTING_TABLE)
				.emptyBackground(177, 94)
				.displayFactory(BasinDisplay::autoSquare)
				.build("automatic_packing", PackingCategory::autoSquare),

		sawing = builder(CuttingRecipe.class)
				.addTypedRecipes(AllRecipeTypes.CUTTING)
				.catalyst(AllBlocks.MECHANICAL_SAW::get)
				.doubleItemIcon(AllBlocks.MECHANICAL_SAW.get(), Items.OAK_LOG)
				.emptyBackground(177, 76)
				.build("sawing", SawingCategory::new),

		blockCutting = builder(CondensedBlockCuttingRecipe.class)
				.enableWhen(c -> c.allowStonecuttingOnSaw)
				.addRecipes(() -> CondensedBlockCuttingRecipe.condenseRecipes(getTypedRecipesExcluding(RecipeType.STONECUTTING, AllRecipeTypes::shouldIgnoreInAutomation)))
				.catalyst(AllBlocks.MECHANICAL_SAW::get)
				.doubleItemIcon(AllBlocks.MECHANICAL_SAW.get(), Items.STONE_BRICK_STAIRS)
				.emptyBackground(177, 76)
				.build("block_cutting", BlockCuttingCategory::new),

		woodCutting = builder(CondensedBlockCuttingRecipe.class)
				.enableIf(c -> c.allowWoodcuttingOnSaw.get() && FabricLoader.getInstance()
						.isModLoaded("druidcraft"))
				.addRecipes(() -> CondensedBlockCuttingRecipe.condenseRecipes(getTypedRecipesExcluding(SawBlockEntity.woodcuttingRecipeType.get(), AllRecipeTypes::shouldIgnoreInAutomation)))
				.catalyst(AllBlocks.MECHANICAL_SAW::get)
				.doubleItemIcon(AllBlocks.MECHANICAL_SAW.get(), Items.OAK_STAIRS)
				.emptyBackground(177, 76)
				.build("wood_cutting", BlockCuttingCategory::new),

		polishing = builder(SandPaperPolishingRecipe.class)
				.addTypedRecipes(AllRecipeTypes.SANDPAPER_POLISHING)
				.catalyst(AllItems.SAND_PAPER::get)
				.catalyst(AllItems.RED_SAND_PAPER::get)
				.itemIcon(AllItems.SAND_PAPER.get())
				.emptyBackground(177, 55)
				.build("sandpaper_polishing", PolishingCategory::new),

		item_application = builder(ItemApplicationRecipe.class)
				.addTypedRecipes(AllRecipeTypes.ITEM_APPLICATION)
				.addRecipes(LogStrippingFakeRecipes::createRecipes)
				.itemIcon(AllItems.BRASS_HAND.get())
				.emptyBackground(177, 66)
				.build("item_application", ItemApplicationCategory::new),

		deploying = builder(DeployerApplicationRecipe.class)
				.addTypedRecipes(AllRecipeTypes.DEPLOYING)
				.addTypedRecipes(AllRecipeTypes.SANDPAPER_POLISHING::getType, DeployerApplicationRecipe::convert)
				.addTypedRecipes(AllRecipeTypes.ITEM_APPLICATION::getType, ManualApplicationRecipe::asDeploying)
				.catalyst(AllBlocks.DEPLOYER::get)
				.catalyst(AllBlocks.DEPOT::get)
				.catalyst(AllItems.BELT_CONNECTOR::get)
				.itemIcon(AllBlocks.DEPLOYER.get())
				.emptyBackground(177, 76)
				.build("deploying", DeployingCategory::new),

		spoutFilling = builder(FillingRecipe.class)
				.addTypedRecipes(AllRecipeTypes.FILLING)
				.addRecipeListConsumer(recipes -> SpoutCategory.consumeRecipes(recipes::add))
				.catalyst(AllBlocks.SPOUT::get)
				.doubleItemIcon(AllBlocks.SPOUT.get(), Items.WATER_BUCKET)
				.emptyBackground(177, 76)
				.build("spout_filling", SpoutCategory::new),

		draining = builder(EmptyingRecipe.class)
				.addRecipeListConsumer(recipes -> ItemDrainCategory.consumeRecipes(recipes::add))
				.addTypedRecipes(AllRecipeTypes.EMPTYING)
				.catalyst(AllBlocks.ITEM_DRAIN::get)
				.doubleItemIcon(AllBlocks.ITEM_DRAIN.get(), Items.WATER_BUCKET)
				.emptyBackground(177, 56)
				.build("draining", ItemDrainCategory::new),

		autoShaped = builder(CraftingRecipe.class)
				.enableWhen(c -> c.allowRegularCraftingInCrafter)
				.addAllRecipesIf(r -> r instanceof CraftingRecipe && !(r instanceof ShapedRecipe)
						&& r.getIngredients()
						.size() == 1
						&& !AllRecipeTypes.shouldIgnoreInAutomation(r))
				.addTypedRecipesIf(() -> RecipeType.CRAFTING,
						recipe -> recipe instanceof ShapedRecipe && !AllRecipeTypes.shouldIgnoreInAutomation(recipe))
				.catalyst(AllBlocks.MECHANICAL_CRAFTER::get)
				.itemIcon(AllBlocks.MECHANICAL_CRAFTER.get())
				.emptyBackground(177, 107)
				.build("automatic_shaped", MechanicalCraftingCategory::new),

	mechanicalCrafting = builder(CraftingRecipe.class)
			.addTypedRecipes(AllRecipeTypes.MECHANICAL_CRAFTING)
			.catalyst(AllBlocks.MECHANICAL_CRAFTER::get).itemIcon(AllBlocks.MECHANICAL_CRAFTER.get())
				.emptyBackground(177, 107)
			.build("mechanical_crafting", MechanicalCraftingCategory::new),

		seqAssembly = builder(SequencedAssemblyRecipe.class)
				.addTypedRecipes(AllRecipeTypes.SEQUENCED_ASSEMBLY)
				.itemIcon(AllItems.PRECISION_MECHANISM.get())
				.emptyBackground(180, 120)
				.build("sequenced_assembly", SequencedAssemblyCategory::new),

		mysteryConversion = builder(ConversionRecipe.class)
				.addRecipes(() -> MysteriousItemConversionCategory.RECIPES)
				.itemIcon(AllItems.CHROMATIC_COMPOUND.get())
				.emptyBackground(177, 50)
				.build("mystery_conversion", MysteriousItemConversionCategory::new);

	}

	private <T extends Recipe<?>> CategoryBuilder<T> builder(Class<? extends T> recipeClass) {
		return new CategoryBuilder<>(recipeClass);
	}

	@Override
	public String getPluginProviderName() {
		return ID.toString();
	}

	@Override
	public void registerCategories(CategoryRegistry registry) {
		loadCategories();
		allCategories.forEach(category -> {
			registry.add(category);
			category.registerCatalysts(registry);
		});
	}

	@Override
	public void registerDisplays(DisplayRegistry registry) {
		allCategories.forEach(c -> c.registerRecipes(registry));

		List<CraftingRecipe> recipes = ToolboxColoringRecipeMaker.createRecipes().toList();

		for (CraftingRecipe recipe : recipes) {
			Collection<Display> displays = registry.tryFillDisplay(recipe);
			for (Display display : displays) {
				if (Objects.equals(display.getCategoryIdentifier(), BuiltinPlugin.CRAFTING)) {
					registry.add(display, recipe);
				}
			}
		}
	}

	@Override
	public void registerExclusionZones(ExclusionZones zones) {
		zones.register(AbstractSimiContainerScreen.class, new SlotMover());
	}

	@Override
	public void registerScreens(ScreenRegistry registry) {
		registry.registerDraggableStackVisitor(new GhostIngredientHandler<>());
	}

	@Override
	public void registerTransferHandlers(TransferHandlerRegistry registry) {
		registry.register(new BlueprintTransferHandler());
	}

//	@Override // FIXME RECIPE VIEWERS
//	public void registerFluidSubtypes(ISubtypeRegistration registration) {
//		PotionFluidSubtypeInterpreter interpreter = new PotionFluidSubtypeInterpreter();
//		PotionFluid potionFluid = AllFluids.POTION.get();
//		registration.registerSubtypeInterpreter(FabricTypes.FLUID_STACK, potionFluid.getSource(), interpreter);
//		registration.registerSubtypeInterpreter(FabricTypes.FLUID_STACK, potionFluid.getFlowing(), interpreter);
//	}

	@Override
	public void registerEntries(EntryRegistry registry) {
		registry.removeEntryIf(entryStack -> {
			if(entryStack.getType() == VanillaEntryTypes.ITEM) {
				ItemStack itemStack = entryStack.castValue();
				if(itemStack.getItem() instanceof TagDependentIngredientItem tagItem) {
					return tagItem.shouldHide();
				}
			} else if(entryStack.getType() == VanillaEntryTypes.FLUID) {
				FluidStack fluidStack = entryStack.castValue();
				return fluidStack.getFluid() instanceof VirtualFluid;
			}
			return false;
		});
	}

	private class CategoryBuilder<T extends Recipe<?>> {
		private final Class<? extends T> recipeClass;
		private Predicate<CRecipes> predicate = cRecipes -> true;

		private Renderer background;
		private Renderer icon;

		private int width;
		private int height;

		private Function<T, ? extends CreateDisplay<T>> displayFactory;

		private final List<Consumer<List<T>>> recipeListConsumers = new ArrayList<>();
		private final List<Supplier<? extends ItemStack>> catalysts = new ArrayList<>();

		public CategoryBuilder(Class<? extends T> recipeClass) {
			this.recipeClass = recipeClass;
		}

		public CategoryBuilder<T> enableIf(Predicate<CRecipes> predicate) {
			this.predicate = predicate;
			return this;
		}

		public CategoryBuilder<T> enableWhen(Function<CRecipes, ConfigBool> configValue) {
			predicate = c -> configValue.apply(c).get();
			return this;
		}

		public CategoryBuilder<T> addRecipeListConsumer(Consumer<List<T>> consumer) {
			recipeListConsumers.add(consumer);
			return this;
		}

		public CategoryBuilder<T> addRecipes(Supplier<Collection<? extends T>> collection) {
			return addRecipeListConsumer(recipes -> recipes.addAll(collection.get()));
		}

		public CategoryBuilder<T> addAllRecipesIf(Predicate<Recipe<?>> pred) {
			return addRecipeListConsumer(recipes -> consumeAllRecipes(recipe -> {
				if (pred.test(recipe)) {
					recipes.add((T) recipe);
				}
			}));
		}

		public CategoryBuilder<T> addAllRecipesIf(Predicate<Recipe<?>> pred, Function<Recipe<?>, T> converter) {
			return addRecipeListConsumer(recipes -> consumeAllRecipes(recipe -> {
				if (pred.test(recipe)) {
					recipes.add(converter.apply(recipe));
				}
			}));
		}

		public CategoryBuilder<T> addTypedRecipes(IRecipeTypeInfo recipeTypeEntry) {
			return addTypedRecipes(recipeTypeEntry::getType);
		}

		public CategoryBuilder<T> addTypedRecipes(Supplier<RecipeType<? extends T>> recipeType) {
			return addRecipeListConsumer(recipes -> CreateREI.<T>consumeTypedRecipes(recipes::add, recipeType.get()));
		}

		public CategoryBuilder<T> addTypedRecipes(Supplier<RecipeType<? extends T>> recipeType, Function<Recipe<?>, T> converter) {
			return addRecipeListConsumer(recipes -> CreateREI.<T>consumeTypedRecipes(recipe -> recipes.add(converter.apply(recipe)), recipeType.get()));
		}

		public CategoryBuilder<T> addTypedRecipesIf(Supplier<RecipeType<? extends T>> recipeType, Predicate<Recipe<?>> pred) {
			return addRecipeListConsumer(recipes -> CreateREI.<T>consumeTypedRecipes(recipe -> {
				if (pred.test(recipe)) {
					recipes.add(recipe);
				}
			}, recipeType.get()));
		}

		public CategoryBuilder<T> addTypedRecipesExcluding(Supplier<RecipeType<? extends T>> recipeType,
			Supplier<RecipeType<? extends T>> excluded) {
			return addRecipeListConsumer(recipes -> {
				List<Recipe<?>> excludedRecipes = getTypedRecipes(excluded.get());
				CreateREI.<T>consumeTypedRecipes(recipe -> {
					for (Recipe<?> excludedRecipe : excludedRecipes) {
						if (doInputsMatch(recipe, excludedRecipe)) {
							return;
						}
					}
					recipes.add(recipe);
				}, recipeType.get());
			});
		}

		public CategoryBuilder<T> removeRecipes(Supplier<RecipeType<? extends T>> recipeType) {
			return addRecipeListConsumer(recipes -> {
				List<Recipe<?>> excludedRecipes = getTypedRecipes(recipeType.get());
				recipes.removeIf(recipe -> {
					for (Recipe<?> excludedRecipe : excludedRecipes) {
						if (doInputsMatch(recipe, excludedRecipe)) {
							return true;
						}
					}
					return false;
				});
			});
		}

		public CategoryBuilder<T> catalystStack(Supplier<ItemStack> supplier) {
			catalysts.add(supplier);
			return this;
		}

		public CategoryBuilder<T> catalyst(Supplier<ItemLike> supplier) {
			return catalystStack(() -> new ItemStack(supplier.get()
				.asItem()));
		}

		public CategoryBuilder<T> icon(Renderer icon) {
			this.icon = icon;
			return this;
		}

		public CategoryBuilder<T> itemIcon(ItemLike item) {
			icon(new ItemIcon(() -> new ItemStack(item)));
			return this;
		}

		public CategoryBuilder<T> doubleItemIcon(ItemLike item1, ItemLike item2) {
			icon(new DoubleItemIcon(() -> new ItemStack(item1), () -> new ItemStack(item2)));
			return this;
		}

		public CategoryBuilder<T> background(Renderer background) {
			this.background = background;
			return this;
		}

		public CategoryBuilder<T> emptyBackground(int width, int height) {
			background(new EmptyBackground(width, height));
			dimensions(width, height);
			return this;
		}

		public CategoryBuilder<T> width(int width) {
			this.width = width;
			return this;
		}

		public CategoryBuilder<T> height(int height) {
			this.height = height;
			return this;
		}

		public CategoryBuilder<T> dimensions(int width, int height) {
			width(width);
			height(height);
			return this;
		}

		public CategoryBuilder<T> displayFactory(Function<T, ? extends CreateDisplay<T>> factory) {
			this.displayFactory = factory;
			return this;
		}

		public CreateRecipeCategory<T> build(String name, CreateRecipeCategory.Factory<T> factory) {
			Supplier<List<T>> recipesSupplier;
			if (predicate.test(AllConfigs.server().recipes)) {
				recipesSupplier = () -> {
					List<T> recipes = new ArrayList<>();
					if (predicate.test(AllConfigs.server().recipes)) {
						for (Consumer<List<T>> consumer : recipeListConsumers)
							consumer.accept(recipes);
					}
					return recipes;
				};
			} else {
				recipesSupplier = () -> Collections.emptyList();
			}

			if (width <= 0 || height <= 0) {
				Create.LOGGER.warn("Create REI category [{}] has weird dimensions: {}x{}", name, width, height);
			}

			CreateRecipeCategory.Info<T> info = new CreateRecipeCategory.Info<>(
					CategoryIdentifier.of(Create.asResource(name)),
					Lang.translateDirect("recipe." + name), background, icon, recipesSupplier, catalysts, width, height, displayFactory == null ? (recipe) -> new CreateDisplay<>(recipe, CategoryIdentifier.of(Create.asResource(name))) : displayFactory);
			CreateRecipeCategory<T> category = factory.create(info);
			allCategories.add(category);
			return category;
		}
	}

	public static void consumeAllRecipes(Consumer<Recipe<?>> consumer) {
		Minecraft.getInstance().level.getRecipeManager()
			.getRecipes()
			.forEach(consumer);
	}

	public static <T extends Recipe<?>> void consumeTypedRecipes(Consumer<T> consumer, RecipeType<?> type) {
		Map<ResourceLocation, Recipe<?>> map = ((RecipeManagerAccessor) Minecraft.getInstance()
			.getConnection()
			.getRecipeManager()).port_lib$getRecipes().get(type);
		if (map != null) {
			map.values().forEach(recipe -> consumer.accept((T) recipe));
		}
	}

	public static List<Recipe<?>> getTypedRecipes(RecipeType<?> type) {
		List<Recipe<?>> recipes = new ArrayList<>();
		consumeTypedRecipes(recipes::add, type);
		return recipes;
	}

	public static List<Recipe<?>> getTypedRecipesExcluding(RecipeType<?> type, Predicate<Recipe<?>> exclusionPred) {
		List<Recipe<?>> recipes = getTypedRecipes(type);
		recipes.removeIf(exclusionPred);
		return recipes;
	}

	public static boolean doInputsMatch(Recipe<?> recipe1, Recipe<?> recipe2) {
		if (recipe1.getIngredients()
			.isEmpty()
			|| recipe2.getIngredients()
				.isEmpty()) {
			return false;
		}
		ItemStack[] matchingStacks = recipe1.getIngredients()
			.get(0)
			.getItems();
		if (matchingStacks.length == 0) {
			return false;
		}
		return recipe2.getIngredients()
				.get(0)
				.test(matchingStacks[0]);
	}

}
