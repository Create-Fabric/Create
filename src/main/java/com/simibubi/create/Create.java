package com.simibubi.create;

import java.util.Random;

import org.slf4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.logging.LogUtils;
import com.simibubi.create.api.behaviour.BlockSpoutingBehaviour;
import com.simibubi.create.compat.Mods;
import com.simibubi.create.compat.computercraft.ComputerCraftProxy;
import com.simibubi.create.compat.trinkets.Trinkets;
import com.simibubi.create.content.contraptions.ContraptionMovementSetting;
import com.simibubi.create.content.decoration.palettes.AllPaletteBlocks;
import com.simibubi.create.content.equipment.potatoCannon.BuiltinPotatoProjectileTypes;
import com.simibubi.create.content.fluids.tank.BoilerHeaters;
import com.simibubi.create.content.kinetics.TorquePropagator;
import com.simibubi.create.content.kinetics.mechanicalArm.AllArmInteractionPointTypes;
import com.simibubi.create.content.redstone.displayLink.AllDisplayBehaviours;
import com.simibubi.create.content.redstone.link.RedstoneLinkNetworkHandler;
import com.simibubi.create.content.schematics.SchematicInstances;
import com.simibubi.create.content.schematics.ServerSchematicLoader;
import com.simibubi.create.content.trains.GlobalRailwayManager;
import com.simibubi.create.content.trains.bogey.BogeySizes;
import com.simibubi.create.content.trains.track.AllPortalTracks;
import com.simibubi.create.foundation.advancement.AllAdvancements;
import com.simibubi.create.foundation.advancement.AllTriggers;
import com.simibubi.create.foundation.block.CopperRegistries;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.data.DamageTypeTagGen;
import com.simibubi.create.foundation.data.TagGen;
import com.simibubi.create.foundation.data.TagLangGen;
import com.simibubi.create.foundation.data.recipe.MechanicalCraftingRecipeGen;
import com.simibubi.create.foundation.data.recipe.ProcessingRecipeGen;
import com.simibubi.create.foundation.data.recipe.SequencedAssemblyRecipeGen;
import com.simibubi.create.foundation.data.recipe.StandardRecipeGen;
import com.simibubi.create.foundation.events.CommonEvents;
import com.simibubi.create.foundation.item.ItemDescription;
import com.simibubi.create.foundation.item.KineticStats;
import com.simibubi.create.foundation.item.TooltipHelper.Palette;
import com.simibubi.create.foundation.item.TooltipModifier;
import com.simibubi.create.foundation.ponder.FabricPonderProcessing;
import com.simibubi.create.foundation.recipe.AllIngredients;
import com.simibubi.create.foundation.utility.AttachedRegistry;
import com.simibubi.create.infrastructure.command.ServerLagger;
import com.simibubi.create.infrastructure.config.AllConfigs;
import com.simibubi.create.infrastructure.data.CreateDatagen;
import com.simibubi.create.infrastructure.data.CreateRecipeSerializerTagsProvider;
import com.simibubi.create.infrastructure.data.GeneratedEntriesProvider;
import com.simibubi.create.infrastructure.worldgen.AllBiomeModifiers;
import com.simibubi.create.infrastructure.worldgen.AllFeatures;
import com.simibubi.create.infrastructure.worldgen.AllPlacementModifiers;

import io.github.fabricators_of_create.porting_lib.data.ExistingFileHelper;
import io.github.tropheusj.milk.Milk;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public class Create implements ModInitializer {

	public static final String ID = "create";
	public static final String NAME = "Create";
	public static final String VERSION = "0.5.1f";

	public static final Logger LOGGER = LogUtils.getLogger();

	public static final Gson GSON = new GsonBuilder().setPrettyPrinting()
		.disableHtmlEscaping()
		.create();

	/** Use the {@link Random} of a local {@link Level} or {@link Entity} or create one */
	@Deprecated
	public static final Random RANDOM = new Random();

	/**
	 * <b>Other mods should not use this field!</b> If you are an addon developer, create your own instance of
	 * {@link CreateRegistrate}.
	 */
	public static final CreateRegistrate REGISTRATE = CreateRegistrate.create(ID);

	static {
		REGISTRATE.setTooltipModifierFactory(item -> {
			return new ItemDescription.Modifier(item, Palette.STANDARD_CREATE)
				.andThen(TooltipModifier.mapNull(KineticStats.create(item)));
		});
	}

	public static final ServerSchematicLoader SCHEMATIC_RECEIVER = new ServerSchematicLoader();
	public static final RedstoneLinkNetworkHandler REDSTONE_LINK_NETWORK_HANDLER = new RedstoneLinkNetworkHandler();
	public static final TorquePropagator TORQUE_PROPAGATOR = new TorquePropagator();
	public static final GlobalRailwayManager RAILWAYS = new GlobalRailwayManager();
	public static final ServerLagger LAGGER = new ServerLagger();

	@Override
	public void onInitialize() { // onCtor
		AllSoundEvents.prepare();
		AllTags.init();
		AllBlocks.register();
		AllItems.register();
		AllFluids.register();
		AllPaletteBlocks.register();
		AllMenuTypes.register();
		AllEntityTypes.register();
		AllBlockEntityTypes.register();
		AllEnchantments.register();
		AllRecipeTypes.register();
		AllIngredients.register();

		// fabric exclusive, squeeze this in here to register before stuff is used
		REGISTRATE.register();

		AllParticleTypes.register();
		AllStructureProcessorTypes.register();
		AllEntityDataSerializers.register();
		AllFeatures.register();
		AllPlacementModifiers.register();
		AllCreativeModeTabs.register();
		BogeySizes.init();
		AllBogeyStyles.register();

		AllConfigs.register();

		AllMovementBehaviours.registerDefaults();
		AllInteractionBehaviours.registerDefaults();
		AllPortalTracks.registerDefaults();
		AllDisplayBehaviours.registerDefaults();
		ContraptionMovementSetting.registerDefaults();
		AllArmInteractionPointTypes.register();
		BlockSpoutingBehaviour.registerDefaults();
		ComputerCraftProxy.register();

		Milk.enableMilkFluid();
		CopperRegistries.inject();

		Create.init();
//		modEventBus.addListener(EventPriority.LOW, Create::gatherData); // CreateData entrypoint
		AllSoundEvents.register();

		// causes class loading issues or something
		// noinspection Convert2MethodRef
		Mods.TRINKETS.executeIfInstalled(() -> () -> Trinkets.init());

		// fabric exclusive
		CommonEvents.register();
		AllPackets.getChannel().initServerListener();
		FabricPonderProcessing.init();
		AllBiomeModifiers.bootstrap(); // moved out of datagen
	}

	public static void init() {
		AllPackets.registerPackets();
		SchematicInstances.register();
		BuiltinPotatoProjectileTypes.register();

//		event.enqueueWork(() -> {
			AllAdvancements.register();
			AllTriggers.register();
			BoilerHeaters.registerDefaults();
			AllFluids.registerFluidInteractions();
//		});

		// fabric: registration not done yet, do it later
		ServerLifecycleEvents.SERVER_STARTING.register(server -> AttachedRegistry.unwrapAll());
	}

	public static void gatherData(FabricDataGenerator.Pack pack, ExistingFileHelper helper) {
		CreateDatagen.addExtraRegistrateData();

		TagGen.datagen();
		TagLangGen.datagen();

		pack.addProvider(AllSoundEvents::provider);

		pack.addProvider(AllAdvancements::new);
		pack.addProvider(StandardRecipeGen::new);
		pack.addProvider(MechanicalCraftingRecipeGen::new);
		pack.addProvider(SequencedAssemblyRecipeGen::new);
		pack.addProvider(ProcessingRecipeGen::registerAll);
		pack.addProvider(GeneratedEntriesProvider::new);
		pack.addProvider(DamageTypeTagGen::new);
		pack.addProvider(CreateRecipeSerializerTagsProvider::new);
	}

	public static ResourceLocation asResource(String path) {
		return new ResourceLocation(ID, path);
	}

}

