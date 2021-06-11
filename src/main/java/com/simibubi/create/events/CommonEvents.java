package com.simibubi.create.events;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.mojang.brigadier.CommandDispatcher;
import com.simibubi.create.AllFluids;
import com.simibubi.create.Create;
import com.simibubi.create.content.contraptions.components.crusher.CrushingWheelTileEntity;
import com.simibubi.create.content.contraptions.components.deployer.DeployerFakePlayer;
import com.simibubi.create.content.contraptions.components.structureMovement.ContraptionHandler;
import com.simibubi.create.content.contraptions.components.structureMovement.mounted.MinecartContraptionItem;
import com.simibubi.create.content.contraptions.components.structureMovement.train.CouplingHandler;
import com.simibubi.create.content.contraptions.components.structureMovement.train.CouplingPhysics;
import com.simibubi.create.content.contraptions.components.structureMovement.train.MinecartCouplingItem;
import com.simibubi.create.content.contraptions.components.structureMovement.train.capability.CapabilityMinecartController;
import com.simibubi.create.content.contraptions.fluids.recipe.FluidTransferRecipes;
import com.simibubi.create.content.contraptions.fluids.recipe.PotionMixingRecipeManager;
import com.simibubi.create.content.contraptions.wrench.WrenchItem;
import com.simibubi.create.content.curiosities.armor.DivingBootsItem;
import com.simibubi.create.content.curiosities.armor.DivingHelmetItem;
import com.simibubi.create.content.curiosities.symmetry.SymmetryHandler;
import com.simibubi.create.content.curiosities.tools.ExtendoGripItem;
import com.simibubi.create.content.curiosities.zapper.ZapperInteractionHandler;
import com.simibubi.create.content.curiosities.zapper.ZapperItem;
import com.simibubi.create.content.logistics.block.funnel.FunnelItem;
import com.simibubi.create.content.logistics.item.LinkedControllerServerHandler;
import com.simibubi.create.foundation.command.AllCommands;
import com.simibubi.create.foundation.config.ui.OpenCreateMenuButton;
import com.simibubi.create.foundation.fluid.FluidHelper;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.ServerSpeedProvider;
import com.simibubi.create.foundation.utility.WorldAttached;
import com.simibubi.create.foundation.utility.recipe.RecipeFinder;
import com.simibubi.create.lib.event.BlockPlaceCallback;
import com.simibubi.create.lib.event.DataPackReloadCallback;
import com.simibubi.create.lib.event.EntityEyeHeightCallback;
import com.simibubi.create.lib.event.FluidPlaceBlockCallback;
import com.simibubi.create.lib.event.LivingEntityEvents;
import com.simibubi.create.lib.event.MobEntitySetTargetCallback;
import com.simibubi.create.lib.event.StartRidingCallback;

import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.networking.v1.EntityTrackingEvents;
import net.minecraft.block.BlockState;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.minecart.AbstractMinecartEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.resources.DataPackRegistries;
import net.minecraft.resources.IFutureReloadListener;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

public class CommonEvents {

	public static void onServerTick(MinecraftServer server) {
		Create.SCHEMATIC_RECEIVER.tick();
		Create.LAGGER.tick();
		ServerSpeedProvider.serverTick(server);
	}

	public static void onChunkUnloaded(World world, Chunk chunk) {
		CapabilityMinecartController.onChunkUnloaded(world, chunk);
	}

	public static BlockState whenFluidsMeet(IWorld world, BlockPos pos, BlockState state) {
		FluidState fluidState = state.getFluidState();

		if (fluidState.isSource() && FluidHelper.isLava(fluidState.getFluid()))
			return null;

		for (Direction direction : Iterate.directions) {
			FluidState metFluidState = fluidState.isSource() ? fluidState : world.getFluidState(pos.offset(direction));
			if (!metFluidState.isTagged(FluidTags.WATER))
				continue;
			BlockState lavaInteraction = AllFluids.getLavaInteraction(metFluidState);
			if (lavaInteraction == null)
				continue;
			return lavaInteraction;
		}

		return null;
	}

	public static void onWorldTick(World world) {
		ContraptionHandler.tick(world);
		CapabilityMinecartController.tick(world);
		CouplingPhysics.tick(world);
		LinkedControllerServerHandler.tick(world);
	}

	public static void onUpdateLivingEntity(LivingEntity entityLiving) {
		World world = entityLiving.world;
		if (world == null)
			return;
		ContraptionHandler.entitiesWhoJustDismountedGetSentToTheRightLocation(entityLiving, world);
	}

	public static void onEntityAdded(Entity entity, World world) {
		ContraptionHandler.addSpawnedContraptionsToCollisionList(entity, world);
	}

	public static ActionResultType onEntityAttackedByPlayer(PlayerEntity playerEntity, World world, Hand hand, Entity entity, @Nullable EntityRayTraceResult entityRayTraceResult) {
		WrenchItem.wrenchInstaKillsMinecarts(playerEntity, world, hand, entity, entityRayTraceResult);
		return ActionResultType.PASS;
	}

	public static void registerCommands(CommandDispatcher<CommandSource> dispatcher, boolean dedicated) {
		AllCommands.register(dispatcher);
	}

	public static List<IFutureReloadListener> registerReloadListeners(DataPackRegistries dataPackRegistries) {
		List<IFutureReloadListener> listeners = new ArrayList<>();
		listeners.add(RecipeFinder.LISTENER);
		listeners.add(PotionMixingRecipeManager.LISTENER);
		listeners.add(FluidTransferRecipes.LISTENER);
		return listeners;
	}

	public static void serverStopped(MinecraftServer server) {
		Create.SCHEMATIC_RECEIVER.shutdown();
	}

	public static void onLoadWorld(World world) {
		Create.REDSTONE_LINK_NETWORK_HANDLER.onLoadWorld(world);
		Create.TORQUE_PROPAGATOR.onLoadWorld(world);
	}

	public static void onUnloadWorld(World world) {
		Create.REDSTONE_LINK_NETWORK_HANDLER.onUnloadWorld(world);
		Create.TORQUE_PROPAGATOR.onUnloadWorld(world);
		WorldAttached.invalidateWorld(world);
	}

	public static void attachCapabilities(AbstractMinecartEntity cart) {
		CapabilityMinecartController.attach(cart);
	}

	public static void startTracking(Entity target, ServerPlayerEntity player) {
		CapabilityMinecartController.startTracking(target);
	}

	public static void leftClickEmpty(PlayerEntity player) {
		ItemStack stack = player.getHeldItemMainhand();
		if (stack.getItem() instanceof ZapperItem) {
			ZapperInteractionHandler.trySelect(stack, player);
		}
	}

	public static void register() {
		ServerTickEvents.END_SERVER_TICK.register(CommonEvents::onServerTick);
		ServerChunkEvents.CHUNK_UNLOAD.register(CommonEvents::onChunkUnloaded);
		ServerTickEvents.END_WORLD_TICK.register(CommonEvents::onWorldTick);
		ServerEntityEvents.ENTITY_LOAD.register(CommonEvents::onEntityAdded);
		AttackEntityCallback.EVENT.register(CommonEvents::onEntityAttackedByPlayer);
		CommandRegistrationCallback.EVENT.register(CommonEvents::registerCommands);
		ServerLifecycleEvents.SERVER_STOPPED.register(CommonEvents::serverStopped);
		ServerWorldEvents.LOAD.register((server, world) -> CommonEvents.onLoadWorld(world));
		ServerWorldEvents.UNLOAD.register((server, world) -> CommonEvents.onUnloadWorld(world));
		FluidPlaceBlockCallback.EVENT.register(CommonEvents::whenFluidsMeet);
		LivingEntityEvents.TICK.register(CommonEvents::onUpdateLivingEntity);
		EntityTrackingEvents.START_TRACKING.register(CommonEvents::startTracking);
		DataPackReloadCallback.EVENT.register(CommonEvents::registerReloadListeners);

		// External Events

		AttackBlockCallback.EVENT.register(ZapperInteractionHandler::leftClickingBlocksWithTheZapperSelectsTheBlock);
		MobEntitySetTargetCallback.EVENT.register(DeployerFakePlayer::entitiesDontRetaliate);
		StartRidingCallback.EVENT.register(CouplingHandler::onStartRiding);
		LivingEntityEvents.EXPERIENCE_DROP.register(DeployerFakePlayer::deployerKillsDoNotSpawnXP);
		LivingEntityEvents.KNOCKBACK_STRENGTH.register(ExtendoGripItem::attacksByExtendoGripHaveMoreKnockback);
		LivingEntityEvents.TICK.register(ExtendoGripItem::holdingExtendoGripIncreasesRange);
		LivingEntityEvents.TICK.register(DivingBootsItem::accellerateDescentUnderwater);
		LivingEntityEvents.TICK.register(DivingHelmetItem::breatheUnderwater);
		EntityEyeHeightCallback.EVENT.register(DeployerFakePlayer::deployerHasEyesOnHisFeet);
		LivingEntityEvents.DROPS.register(CrushingWheelTileEntity::handleCrushedMobDrops);
		LivingEntityEvents.LOOTING_LEVEL.register(CrushingWheelTileEntity::crushingIsFortunate);
		LivingEntityEvents.DROPS.register(DeployerFakePlayer::deployerCollectsDropsFromKilledEntities);
		UseBlockCallback.EVENT.register(FunnelItem::funnelItemAlwaysPlacesWhenUsed);
		UseEntityCallback.EVENT.register(MinecartCouplingItem::handleInteractionWithMinecart);
		UseEntityCallback.EVENT.register(MinecartContraptionItem::wrenchCanBeUsedToPickUpMinecartContraptions);
		BlockPlaceCallback.EVENT.register(SymmetryHandler::onBlockPlaced);
		ScreenEvents.AFTER_INIT.register(OpenCreateMenuButton.OpenConfigButtonHandler::onGuiInit);
	}

}
