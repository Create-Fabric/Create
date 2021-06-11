package com.simibubi.create;

import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.simibubi.create.content.CreateItemGroup;
import com.simibubi.create.content.contraptions.TorquePropagator;
import com.simibubi.create.content.contraptions.components.structureMovement.train.capability.CapabilityMinecartController;
import com.simibubi.create.content.logistics.RedstoneLinkNetworkHandler;
import com.simibubi.create.content.palettes.AllPaletteBlocks;
import com.simibubi.create.content.palettes.PalettesItemGroup;
import com.simibubi.create.content.schematics.SchematicProcessor;
import com.simibubi.create.content.schematics.ServerSchematicLoader;
import com.simibubi.create.content.schematics.filtering.SchematicInstances;
import com.simibubi.create.events.CommonEvents;
import com.simibubi.create.foundation.advancement.AllTriggers;
import com.simibubi.create.foundation.command.ChunkUtil;
import com.simibubi.create.foundation.command.ServerLagger;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.networking.AllPackets;
import com.simibubi.create.foundation.worldgen.AllWorldFeatures;
import com.simibubi.create.lib.event.BiomeLoadingCallback;
import com.simibubi.create.lib.utility.MinecartController;
import com.tterrag.registrate.util.NonNullLazyValue;

import net.fabricmc.api.ModInitializer;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeGenerationSettings;

public class Create implements ModInitializer {

	public static final String ID = "create";
	public static final String NAME = "Create";
	public static final String VERSION = "0.3.2";

	public static final Logger LOGGER = LogManager.getLogger();

	public static final Gson GSON = new GsonBuilder().setPrettyPrinting()
		.disableHtmlEscaping()
		.create();

	public static final ItemGroup BASE_CREATIVE_TAB = new CreateItemGroup();
	public static final ItemGroup PALETTES_CREATIVE_TAB = new PalettesItemGroup();

	public static final ServerSchematicLoader SCHEMATIC_RECEIVER = new ServerSchematicLoader();
	public static final RedstoneLinkNetworkHandler REDSTONE_LINK_NETWORK_HANDLER = new RedstoneLinkNetworkHandler();
	public static final TorquePropagator TORQUE_PROPAGATOR = new TorquePropagator();
	public static final ServerLagger LAGGER = new ServerLagger();
	public static final ChunkUtil CHUNK_UTIL = new ChunkUtil();
	public static final Random RANDOM = new Random();

	private static final NonNullLazyValue<CreateRegistrate> REGISTRATE = CreateRegistrate.lazy(ID);

	@Override
	public void onInitialize() {
//		IEventBus modEventBus = FMLJavaModLoadingContext.get()
//				.getModEventBus();

		AllSoundEvents.prepare();
		AllBlocks.register();
		AllItems.register();
		AllFluids.register();
		AllTags.register();
		AllPaletteBlocks.register();
		AllContainerTypes.register();
		AllEntityTypes.register();
		AllTileEntities.register();
		AllMovementBehaviours.register();
		AllConfigs.register();
		AllWorldFeatures.register();


//		IEventBus modEventBus = FMLJavaModLoadingContext.get()
//			.getModEventBus();
//		IEventBus forgeEventBus = MinecraftForge.EVENT_BUS;

//		modEventBus.addListener(Create::init); // I think this can just be run now
		init();
//		modEventBus.addGenericListener(Feature.class, AllWorldFeatures::registerOreFeatures);
//		modEventBus.addGenericListener(Placement.class, AllWorldFeatures::registerDecoratorFeatures);
//		modEventBus.addGenericListener(IRecipeSerializer.class, AllRecipeTypes::register);
//		modEventBus.addGenericListener(ParticleType.class, AllParticleTypes::register);
//		modEventBus.addGenericListener(SoundEvent.class, AllSoundEvents::register);
//		modEventBus.addListener(AllConfigs::onLoad);
//		modEventBus.addListener(AllConfigs::onReload);
//		modEventBus.addListener(EventPriority.LOWEST, this::gatherData); // method commented, don't need datagen
//		forgeEventBus.addListener(EventPriority.HIGH, Create::onBiomeLoad);

		// fabric events
		// register events are painful, hopefully these just work fine here
		// if they don't, that's a problem for when we can actually debug
		AllRecipeTypes.register();
		AllParticleTypes.register();
		AllSoundEvents.register();
		CommonEvents.register();

		REGISTRATE.get().register();

		BiomeLoadingCallback.EVENT.register(Create::onBiomeLoad);
		MinecartController.InitController.initController = new com.simibubi.create.content.contraptions.components.structureMovement.train.capability.MinecartController(null);
	}

	public static void init() {
		CapabilityMinecartController.register();
		SchematicInstances.register();

		CHUNK_UTIL.init();
//		MinecraftForge.EVENT_BUS.register(chunkUtil); // init() handles registering events

		AllPackets.registerPackets();
		AllTriggers.register();

//		event.enqueueWork(() -> { // I think this can just be run on initialize too
			SchematicProcessor.register();
			AllWorldFeatures.registerFeatures();
//		}
	}

//	public void gatherData(GatherDataEvent event) {
//		DataGenerator gen = event.getGenerator();
//		gen.addProvider(new AllAdvancements(gen));
//		gen.addProvider(new LangMerger(gen));
//		gen.addProvider(AllSoundEvents.provider(gen));
//		gen.addProvider(new StandardRecipeGen(gen));
//		gen.addProvider(new MechanicalCraftingRecipeGen(gen));
//		ProcessingRecipeGen.registerAll(gen);
//	}

	public static BiomeGenerationSettings.Builder onBiomeLoad(ResourceLocation key, Biome.Category category, BiomeGenerationSettings.Builder generation) {
		return AllWorldFeatures.reload(key, category, generation);
	}

	public static CreateRegistrate registrate() {
		return REGISTRATE.get();
	}

	public static ResourceLocation asResource(String path) {
		return new ResourceLocation(ID, path);
	}

}
