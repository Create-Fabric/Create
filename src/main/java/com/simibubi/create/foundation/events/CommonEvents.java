package com.simibubi.create.foundation.events;

import java.util.concurrent.Executor;

import com.simibubi.create.api.event.PipeCollisionEvent;

import com.simibubi.create.content.fluids.FluidReactions;

import org.jetbrains.annotations.Nullable;

import com.mojang.brigadier.CommandDispatcher;
import com.simibubi.create.AllFluids;
import com.simibubi.create.Create;
import com.simibubi.create.content.contraptions.ContraptionHandler;
import com.simibubi.create.content.contraptions.actors.trainControls.ControlsServerHandler;
import com.simibubi.create.content.contraptions.glue.SuperGlueHandler;
import com.simibubi.create.content.contraptions.glue.SuperGlueItem;
import com.simibubi.create.content.contraptions.minecart.CouplingHandler;
import com.simibubi.create.content.contraptions.minecart.CouplingPhysics;
import com.simibubi.create.content.contraptions.minecart.MinecartCouplingItem;
import com.simibubi.create.content.contraptions.minecart.capability.CapabilityMinecartController;
import com.simibubi.create.content.contraptions.mounted.MinecartContraptionItem;
import com.simibubi.create.content.equipment.armor.DivingBootsItem;
import com.simibubi.create.content.equipment.armor.DivingHelmetItem;
import com.simibubi.create.content.equipment.armor.NetheriteDivingHandler;
import com.simibubi.create.content.equipment.bell.HauntedBellPulser;
import com.simibubi.create.content.equipment.clipboard.ClipboardValueSettingsHandler;
import com.simibubi.create.content.equipment.extendoGrip.ExtendoGripItem;
import com.simibubi.create.content.equipment.potatoCannon.PotatoProjectileTypeManager;
import com.simibubi.create.content.equipment.symmetryWand.SymmetryHandler;
import com.simibubi.create.content.equipment.toolbox.ToolboxHandler;
import com.simibubi.create.content.equipment.wrench.WrenchEventHandler;
import com.simibubi.create.content.equipment.wrench.WrenchItem;
import com.simibubi.create.content.equipment.zapper.ZapperInteractionHandler;
import com.simibubi.create.content.equipment.zapper.ZapperItem;
import com.simibubi.create.content.kinetics.belt.BeltHelper;
import com.simibubi.create.content.fluids.FluidBottleItemHook;
import com.simibubi.create.content.kinetics.crank.ValveHandleBlock;
import com.simibubi.create.content.kinetics.crusher.CrushingWheelBlockEntity;
import com.simibubi.create.content.kinetics.deployer.DeployerFakePlayer;
import com.simibubi.create.content.kinetics.deployer.ManualApplicationRecipe;
import com.simibubi.create.content.processing.burner.BlazeBurnerHandler;
import com.simibubi.create.content.redstone.link.LinkHandler;
import com.simibubi.create.content.redstone.link.controller.LinkedControllerServerHandler;
import com.simibubi.create.content.trains.entity.CarriageEntityHandler;
import com.simibubi.create.content.trains.schedule.ScheduleItemEntityInteraction;
import com.simibubi.create.foundation.block.ItemUseOverrides;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsInputHandler;
import com.simibubi.create.foundation.blockEntity.behaviour.edgeInteraction.EdgeInteractionHandler;
import com.simibubi.create.foundation.fluid.FluidHelper;
import com.simibubi.create.foundation.recipe.RecipeFinder;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.ServerSpeedProvider;
import com.simibubi.create.foundation.utility.WorldAttached;
import com.simibubi.create.foundation.utility.fabric.AbstractMinecartExtensions;
import com.simibubi.create.infrastructure.command.AllCommands;
import com.simibubi.create.infrastructure.worldgen.AllOreFeatureConfigEntries;

import io.github.fabricators_of_create.porting_lib.event.common.BlockEvents;
import io.github.fabricators_of_create.porting_lib.event.common.EntityEvents;
import io.github.fabricators_of_create.porting_lib.event.common.EntityReadExtraDataCallback;
import io.github.fabricators_of_create.porting_lib.event.common.FluidPlaceBlockCallback;
import io.github.fabricators_of_create.porting_lib.event.common.LivingEntityEvents;
import io.github.fabricators_of_create.porting_lib.event.common.MinecartEvents;
import io.github.fabricators_of_create.porting_lib.event.common.MobEntitySetTargetCallback;
import io.github.fabricators_of_create.porting_lib.event.common.MountEntityCallback;
import io.github.fabricators_of_create.porting_lib.event.common.ProjectileImpactCallback;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.packs.PackType;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.EntityHitResult;

public class CommonEvents {

	public static void onServerTick(MinecraftServer server) {
		Create.SCHEMATIC_RECEIVER.tick();
		Create.LAGGER.tick();
		ServerSpeedProvider.serverTick(server);
		Create.RAILWAYS.sync.serverTick();
	}

	public static void onChunkUnloaded(Level world, LevelChunk chunk) {
		CapabilityMinecartController.onChunkUnloaded(world, chunk);
	}

	public static void playerLoggedIn(ServerGamePacketListenerImpl handler, PacketSender sender, MinecraftServer server) {
		ToolboxHandler.playerLogin(handler.getPlayer());
		Create.RAILWAYS.playerLogin(handler.getPlayer());
	}

	public static void playerLoggedOut(ServerGamePacketListenerImpl handler, MinecraftServer server) {
		Player player = handler.getPlayer();
		Create.RAILWAYS.playerLogout(player);
	}

	public static BlockState whenFluidsMeet(LevelAccessor world, BlockPos pos, BlockState blockState) {
		FluidState fluidState = blockState.getFluidState();

		if (fluidState.isSource() && FluidHelper.isLava(fluidState.getType()))
			return null;

		for (Direction direction : Iterate.directions) {
			FluidState metFluidState =
				fluidState.isSource() ? fluidState : world.getFluidState(pos.relative(direction));
			if (!metFluidState.is(FluidTags.WATER))
				continue;
			BlockState lavaInteraction = AllFluids.getLavaInteraction(metFluidState);
			if (lavaInteraction == null)
				continue;
			return lavaInteraction;
		}
		return null;
	}

	public static void onWorldTick(Level world) {
		if (!world.isClientSide()) {
			ContraptionHandler.tick(world);
			CapabilityMinecartController.tick(world);
			CouplingPhysics.tick(world);
			LinkedControllerServerHandler.tick(world);
			ControlsServerHandler.tick(world);
			Create.RAILWAYS.tick(world);
		}
	}

	public static void onUpdateLivingEntity(LivingEntity entityLiving) {
		Level world = entityLiving.level;
		if (world == null)
			return;
		ContraptionHandler.entitiesWhoJustDismountedGetSentToTheRightLocation(entityLiving, world);
		ToolboxHandler.entityTick(entityLiving, world);
	}

	public static void onEntityAdded(Entity entity, Level world) {
		ContraptionHandler.addSpawnedContraptionsToCollisionList(entity, world);
	}

	public static InteractionResult onEntityAttackedByPlayer(Player playerEntity, Level world, InteractionHand hand, Entity entity, @Nullable EntityHitResult entityRayTraceResult) {
		return WrenchItem.wrenchInstaKillsMinecarts(playerEntity, world, hand, entity, entityRayTraceResult);
	}

	public static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher, boolean dedicated) {
		AllCommands.register(dispatcher);
	}

	public static void onEntityEnterSection(Entity entity, long packedOldPos, long packedNewPos) {
		CarriageEntityHandler.onEntityEnterSection(entity, packedOldPos, packedNewPos);
	}

	public static void addReloadListeners() {
		ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(RecipeFinder.LISTENER);
		ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(PotatoProjectileTypeManager.ReloadListener.INSTANCE);
		ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(BeltHelper.LISTENER);
	}

	public static void onDatapackSync(ServerPlayer player, boolean joined) {
		PotatoProjectileTypeManager.syncTo(player);
	}

	public static void serverStopping(MinecraftServer server) {
		Create.SCHEMATIC_RECEIVER.shutdown();
	}

	public static void onLoadWorld(Executor executor, LevelAccessor world) {
		Create.REDSTONE_LINK_NETWORK_HANDLER.onLoadWorld(world);
		Create.TORQUE_PROPAGATOR.onLoadWorld(world);
		Create.RAILWAYS.levelLoaded(world);
	}

	public static void onUnloadWorld(Executor executor, LevelAccessor world) {
		Create.REDSTONE_LINK_NETWORK_HANDLER.onUnloadWorld(world);
		Create.TORQUE_PROPAGATOR.onUnloadWorld(world);
		WorldAttached.invalidateWorld(world);
	}

	// handled by AbstractMinecartMixin
	public static void attachCapabilities(AbstractMinecart cart) {
		CapabilityMinecartController.attach(cart);
	}

	public static void startTracking(Entity target, ServerPlayer player) {
		CapabilityMinecartController.startTracking(target);
	}

	public static void onBiomeLoad() {
		AllOreFeatureConfigEntries.modifyBiomes();
	}

	public static void leftClickEmpty(ServerPlayer player) {
		ItemStack stack = player.getMainHandItem();
		if (stack.getItem() instanceof ZapperItem) {
			ZapperInteractionHandler.trySelect(stack, player);
		}
	}

	public static class ModBusEvents {

//		@SubscribeEvent
//		public static void registerCapabilities(RegisterCapabilitiesEvent event) {
//			event.register(CapabilityMinecartController.class);
//		}

	}

	public static void addPackFinders() {
		ModContainer create = FabricLoader.getInstance().getModContainer(Create.ID)
				.orElseThrow(() -> new IllegalStateException("Create's ModContainer couldn't be found!"));
		ResourceLocation packId = Create.asResource("legacy_copper");
		ResourceManagerHelper.registerBuiltinResourcePack(packId, create, "Create Legacy Copper", ResourcePackActivationType.NORMAL);
	}

	public static void register() {
		// Fabric Events
		ServerTickEvents.END_SERVER_TICK.register(CommonEvents::onServerTick);
		ServerChunkEvents.CHUNK_UNLOAD.register(CommonEvents::onChunkUnloaded);
		ServerTickEvents.END_WORLD_TICK.register(CommonEvents::onWorldTick);
		ServerEntityEvents.ENTITY_LOAD.register(CommonEvents::onEntityAdded);
		ServerLifecycleEvents.SERVER_STOPPED.register(CommonEvents::serverStopping);
		ServerWorldEvents.LOAD.register(CommonEvents::onLoadWorld);
		ServerWorldEvents.UNLOAD.register(CommonEvents::onUnloadWorld);
		ServerPlayConnectionEvents.DISCONNECT.register(CommonEvents::playerLoggedOut);
		AttackEntityCallback.EVENT.register(CommonEvents::onEntityAttackedByPlayer);
		CommandRegistrationCallback.EVENT.register(CommonEvents::registerCommands);
		EntityEvents.START_TRACKING_TAIL.register(CommonEvents::startTracking);
		EntityEvents.ENTERING_SECTION.register(CommonEvents::onEntityEnterSection);
		LivingEntityEvents.TICK.register(CommonEvents::onUpdateLivingEntity);
		ServerPlayConnectionEvents.JOIN.register(CommonEvents::playerLoggedIn);
		FluidPlaceBlockCallback.EVENT.register(CommonEvents::whenFluidsMeet);
		ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register(CommonEvents::onDatapackSync);
		PipeCollisionEvent.FLOW.register(FluidReactions::handlePipeFlowCollisionFallback);
		PipeCollisionEvent.SPILL.register(FluidReactions::handlePipeSpillCollisionFallback);
		// fabric: some features using events on forge don't use events here.
		// they've been left in this class for upstream compatibility.
		CommonEvents.addReloadListeners();
		CommonEvents.onBiomeLoad();
		CommonEvents.addPackFinders();

		// External Events

		UseEntityCallback.EVENT.register(MinecartCouplingItem::handleInteractionWithMinecart);
		UseEntityCallback.EVENT.register(MinecartContraptionItem::wrenchCanBeUsedToPickUpMinecartContraptions);
		UseBlockCallback.EVENT.register(WrenchEventHandler::useOwnWrenchLogicForCreateBlocks);
		UseBlockCallback.EVENT.register(LinkHandler::onBlockActivated);
		UseBlockCallback.EVENT.register(ItemUseOverrides::onBlockActivated);
		UseBlockCallback.EVENT.register(EdgeInteractionHandler::onBlockActivated);
		UseBlockCallback.EVENT.register(FluidBottleItemHook::preventWaterBottlesFromCreatesFluids);
		UseBlockCallback.EVENT.register(SuperGlueItem::glueItemAlwaysPlacesWhenUsed);
		UseBlockCallback.EVENT.register(ManualApplicationRecipe::manualApplicationRecipesApplyInWorld);
		UseBlockCallback.EVENT.register(ValueSettingsInputHandler::onBlockActivated);
		UseBlockCallback.EVENT.register(ClipboardValueSettingsHandler::leftClickToPaste);
		UseBlockCallback.EVENT.register(ValveHandleBlock::onBlockActivated);
		AttackBlockCallback.EVENT.register(ClipboardValueSettingsHandler::rightClickToCopy);
		AttackBlockCallback.EVENT.register(ZapperInteractionHandler::leftClickingBlocksWithTheZapperSelectsTheBlock);
		UseEntityCallback.EVENT.register(ScheduleItemEntityInteraction::interactWithConductor);
		ServerTickEvents.END_WORLD_TICK.register(HauntedBellPulser::hauntedBellCreatesPulse);
		MobEntitySetTargetCallback.EVENT.register(DeployerFakePlayer::entitiesDontRetaliate);
		MountEntityCallback.EVENT.register(CouplingHandler::preventEntitiesFromMoutingOccupiedCart);
		LivingEntityEvents.EXPERIENCE_DROP.register(DeployerFakePlayer::deployerKillsDoNotSpawnXP);
		LivingEntityEvents.ACTUALLY_HURT.register(ExtendoGripItem::bufferLivingAttackEvent);
		LivingEntityEvents.KNOCKBACK_STRENGTH.register(ExtendoGripItem::attacksByExtendoGripHaveMoreKnockback);
		LivingEntityEvents.TICK.register(ExtendoGripItem::holdingExtendoGripIncreasesRange);
		LivingEntityEvents.TICK.register(DivingBootsItem::accellerateDescentUnderwater);
		LivingEntityEvents.TICK.register(DivingHelmetItem::breatheUnderwater);
		LivingEntityEvents.DROPS.register(CrushingWheelBlockEntity::handleCrushedMobDrops);
		LivingEntityEvents.LOOTING_LEVEL.register(CrushingWheelBlockEntity::crushingIsFortunate);
		LivingEntityEvents.DROPS.register(DeployerFakePlayer::deployerCollectsDropsFromKilledEntities);
		LivingEntityEvents.EQUIPMENT_CHANGE.register(NetheriteDivingHandler::onLivingEquipmentChange);
		EntityEvents.EYE_HEIGHT.register(DeployerFakePlayer::deployerHasEyesOnHisFeet);
		BlockEvents.AFTER_PLACE.register(SymmetryHandler::onBlockPlaced);
		BlockEvents.AFTER_PLACE.register(SuperGlueHandler::glueListensForBlockPlacement);
		ProjectileImpactCallback.EVENT.register(BlazeBurnerHandler::onThrowableImpact);
		EntityReadExtraDataCallback.EVENT.register(ExtendoGripItem::addReachToJoiningPlayersHoldingExtendo);
		MinecartEvents.SPAWN.register(AbstractMinecartExtensions::minecartSpawn);
		MinecartEvents.READ.register(AbstractMinecartExtensions::minecartRead);
		MinecartEvents.WRITE.register(AbstractMinecartExtensions::minecartWrite);
		MinecartEvents.REMOVE.register(AbstractMinecartExtensions::minecartRemove);
		PlayerBlockBreakEvents.BEFORE.register(SymmetryHandler::onBlockDestroyed);
	}
}
