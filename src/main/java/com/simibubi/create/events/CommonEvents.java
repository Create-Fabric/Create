package com.simibubi.create.events;

import java.util.concurrent.Executor;

import io.github.fabricators_of_create.porting_lib.event.client.ClientWorldEvents;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.Commands;

import org.jetbrains.annotations.Nullable;

import com.mojang.brigadier.CommandDispatcher;
import com.simibubi.create.AllFluids;
import com.simibubi.create.Create;
import com.simibubi.create.content.contraptions.components.crusher.CrushingWheelTileEntity;
import com.simibubi.create.content.contraptions.components.deployer.DeployerFakePlayer;
import com.simibubi.create.content.contraptions.components.deployer.ManualApplicationRecipe;
import com.simibubi.create.content.contraptions.components.structureMovement.ContraptionHandler;
import com.simibubi.create.content.contraptions.components.structureMovement.glue.SuperGlueHandler;
import com.simibubi.create.content.contraptions.components.structureMovement.glue.SuperGlueItem;
import com.simibubi.create.content.contraptions.components.structureMovement.interaction.controls.ControlsServerHandler;
import com.simibubi.create.content.contraptions.components.structureMovement.mounted.MinecartContraptionItem;
import com.simibubi.create.content.contraptions.components.structureMovement.train.CouplingHandler;
import com.simibubi.create.content.contraptions.components.structureMovement.train.CouplingPhysics;
import com.simibubi.create.content.contraptions.components.structureMovement.train.MinecartCouplingItem;
import com.simibubi.create.content.contraptions.components.structureMovement.train.capability.CapabilityMinecartController;
import com.simibubi.create.content.contraptions.fluids.FluidBottleItemHook;
import com.simibubi.create.content.contraptions.processing.burner.BlazeBurnerHandler;
import com.simibubi.create.content.contraptions.wrench.WrenchItem;
import com.simibubi.create.content.curiosities.armor.DivingBootsItem;
import com.simibubi.create.content.curiosities.armor.DivingHelmetItem;
import com.simibubi.create.content.curiosities.bell.HauntedBellPulser;
import com.simibubi.create.content.curiosities.symmetry.SymmetryHandler;
import com.simibubi.create.content.curiosities.toolbox.ToolboxHandler;
import com.simibubi.create.content.curiosities.tools.ExtendoGripItem;
import com.simibubi.create.content.curiosities.weapons.PotatoProjectileTypeManager;
import com.simibubi.create.content.curiosities.zapper.ZapperInteractionHandler;
import com.simibubi.create.content.curiosities.zapper.ZapperItem;
import com.simibubi.create.content.logistics.item.LinkedControllerServerHandler;
import com.simibubi.create.content.logistics.trains.entity.CarriageEntityHandler;
import com.simibubi.create.content.logistics.trains.management.schedule.ScheduleItemEntityInteraction;
import com.simibubi.create.foundation.block.ItemUseOverrides;
import com.simibubi.create.foundation.command.AllCommands;
import com.simibubi.create.foundation.fluid.FluidHelper;
import com.simibubi.create.foundation.tileEntity.behaviour.edgeInteraction.EdgeInteractionHandler;
import com.simibubi.create.foundation.tileEntity.behaviour.filtering.FilteringHandler;
import com.simibubi.create.foundation.tileEntity.behaviour.linked.LinkHandler;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.ServerSpeedProvider;
import com.simibubi.create.foundation.utility.WorldAttached;
import com.simibubi.create.foundation.utility.fabric.AbstractMinecartExtensions;
import com.simibubi.create.foundation.utility.recipe.RecipeFinder;
import com.simibubi.create.foundation.worldgen.AllOreFeatureConfigEntries;

import io.github.fabricators_of_create.porting_lib.event.common.BlockPlaceCallback;
import io.github.fabricators_of_create.porting_lib.event.common.EntityEvents;
import io.github.fabricators_of_create.porting_lib.event.common.EntityInteractCallback;
import io.github.fabricators_of_create.porting_lib.event.common.EntityReadExtraDataCallback;
import io.github.fabricators_of_create.porting_lib.event.common.FluidPlaceBlockCallback;
import io.github.fabricators_of_create.porting_lib.event.common.LivingEntityEvents;
import io.github.fabricators_of_create.porting_lib.event.common.MinecartEvents;
import io.github.fabricators_of_create.porting_lib.event.common.MobEntitySetTargetCallback;
import io.github.fabricators_of_create.porting_lib.event.common.MountEntityCallback;
import io.github.fabricators_of_create.porting_lib.event.common.ProjectileImpactCallback;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
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
		// on forge, this is only called on ServerLevels
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

	public static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext registryAccess, Commands.CommandSelection environment) {
		AllCommands.register(dispatcher);
	}

	public static void onEntityEnterSection(Entity entity, long packedOldPos, long packedNewPos) {
		CarriageEntityHandler.onEntityEnterSection(entity, packedOldPos, packedNewPos);
	}

	public static void addReloadListeners() {
		ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(RecipeFinder.LISTENER);
		ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(PotatoProjectileTypeManager.ReloadListener.INSTANCE);
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
		ClientWorldEvents.LOAD.register(CommonEvents::onLoadWorld);
		ServerWorldEvents.UNLOAD.register(CommonEvents::onUnloadWorld);
		ClientWorldEvents.UNLOAD.register(CommonEvents::onUnloadWorld);
		ServerPlayConnectionEvents.DISCONNECT.register(CommonEvents::playerLoggedOut);
		AttackEntityCallback.EVENT.register(CommonEvents::onEntityAttackedByPlayer);
		CommandRegistrationCallback.EVENT.register(CommonEvents::registerCommands);
		EntityEvents.START_TRACKING_TAIL.register(CommonEvents::startTracking);
		EntityEvents.ENTERING_SECTION.register(CommonEvents::onEntityEnterSection);
		LivingEntityEvents.TICK.register(CommonEvents::onUpdateLivingEntity);
		ServerPlayConnectionEvents.JOIN.register(CommonEvents::playerLoggedIn);
		FluidPlaceBlockCallback.EVENT.register(CommonEvents::whenFluidsMeet);
		ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register(CommonEvents::onDatapackSync);
		// fabric: some features using events on forge don't use events here.
		// they've been left in this class for upstream compatibility.
		CommonEvents.addReloadListeners();
		CommonEvents.onBiomeLoad();
		CommonEvents.addPackFinders();

		// External Events

		UseEntityCallback.EVENT.register(MinecartCouplingItem::handleInteractionWithMinecart);
		UseEntityCallback.EVENT.register(MinecartContraptionItem::wrenchCanBeUsedToPickUpMinecartContraptions);
		UseBlockCallback.EVENT.register(FilteringHandler::onBlockActivated);
		UseBlockCallback.EVENT.register(LinkHandler::onBlockActivated);
		UseBlockCallback.EVENT.register(ItemUseOverrides::onBlockActivated);
		UseBlockCallback.EVENT.register(EdgeInteractionHandler::onBlockActivated);
		UseBlockCallback.EVENT.register(FluidBottleItemHook::preventWaterBottlesFromCreatesFluids);
		UseBlockCallback.EVENT.register(SuperGlueItem::glueItemAlwaysPlacesWhenUsed);
		UseBlockCallback.EVENT.register(ManualApplicationRecipe::manualApplicationRecipesApplyInWorld);
		EntityInteractCallback.EVENT.register(ScheduleItemEntityInteraction::removeScheduleFromConductor); // note: fabric event fires too late, use porting lib one
		ServerTickEvents.END_WORLD_TICK.register(HauntedBellPulser::hauntedBellCreatesPulse);
		AttackBlockCallback.EVENT.register(ZapperInteractionHandler::leftClickingBlocksWithTheZapperSelectsTheBlock);
		MobEntitySetTargetCallback.EVENT.register(DeployerFakePlayer::entitiesDontRetaliate);
		MountEntityCallback.EVENT.register(CouplingHandler::preventEntitiesFromMoutingOccupiedCart);
		LivingEntityEvents.EXPERIENCE_DROP.register(DeployerFakePlayer::deployerKillsDoNotSpawnXP);
		LivingEntityEvents.HURT.register(ExtendoGripItem::bufferLivingAttackEvent);
		LivingEntityEvents.KNOCKBACK_STRENGTH.register(ExtendoGripItem::attacksByExtendoGripHaveMoreKnockback);
		LivingEntityEvents.TICK.register(ExtendoGripItem::holdingExtendoGripIncreasesRange);
		LivingEntityEvents.TICK.register(DivingBootsItem::accellerateDescentUnderwater);
		LivingEntityEvents.TICK.register(DivingHelmetItem::breatheUnderwater);
		LivingEntityEvents.DROPS.register(CrushingWheelTileEntity::handleCrushedMobDrops);
		LivingEntityEvents.LOOTING_LEVEL.register(CrushingWheelTileEntity::crushingIsFortunate);
		LivingEntityEvents.DROPS.register(DeployerFakePlayer::deployerCollectsDropsFromKilledEntities);
		EntityEvents.EYE_HEIGHT.register(DeployerFakePlayer::deployerHasEyesOnHisFeet);
		BlockPlaceCallback.EVENT.register(SymmetryHandler::onBlockPlaced);
		BlockPlaceCallback.EVENT.register(SuperGlueHandler::glueListensForBlockPlacement);
		ProjectileImpactCallback.EVENT.register(BlazeBurnerHandler::onThrowableImpact);
		EntityReadExtraDataCallback.EVENT.register(ExtendoGripItem::addReachToJoiningPlayersHoldingExtendo);
		MinecartEvents.SPAWN.register(AbstractMinecartExtensions::minecartSpawn);
		MinecartEvents.READ.register(AbstractMinecartExtensions::minecartRead);
		MinecartEvents.WRITE.register(AbstractMinecartExtensions::minecartWrite);
		MinecartEvents.REMOVE.register(AbstractMinecartExtensions::minecartRemove);
		PlayerBlockBreakEvents.BEFORE.register(SymmetryHandler::onBlockDestroyed);
	}

}
