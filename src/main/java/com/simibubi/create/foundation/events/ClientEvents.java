package com.simibubi.create.foundation.events;

import java.util.List;

import com.jozufozu.flywheel.fabric.event.FlywheelEvents;
import com.mojang.blaze3d.shaders.FogShape;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllFluids;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllKeys;
import com.simibubi.create.AllPackets;
import com.simibubi.create.AllParticleTypes;
import com.simibubi.create.Create;
import com.simibubi.create.CreateClient;
import com.simibubi.create.content.contraptions.ContraptionHandler;
import com.simibubi.create.content.contraptions.ContraptionHandlerClient;
import com.simibubi.create.content.contraptions.actors.trainControls.ControlsHandler;
import com.simibubi.create.content.contraptions.chassis.ChassisRangeDisplay;
import com.simibubi.create.content.contraptions.minecart.CouplingHandlerClient;
import com.simibubi.create.content.contraptions.minecart.CouplingPhysics;
import com.simibubi.create.content.contraptions.minecart.CouplingRenderer;
import com.simibubi.create.content.contraptions.minecart.capability.CapabilityMinecartController;
import com.simibubi.create.content.contraptions.render.ContraptionRenderDispatcher;
import com.simibubi.create.content.decoration.girder.GirderWrenchBehavior;
import com.simibubi.create.content.equipment.armor.BacktankArmorLayer;
import com.simibubi.create.content.equipment.armor.DivingHelmetItem;
import com.simibubi.create.content.equipment.armor.NetheriteBacktankFirstPersonRenderer;
import com.simibubi.create.content.equipment.armor.NetheriteDivingHandler;
import com.simibubi.create.content.equipment.blueprint.BlueprintOverlayRenderer;
import com.simibubi.create.content.equipment.clipboard.ClipboardValueSettingsHandler;
import com.simibubi.create.content.equipment.extendoGrip.ExtendoGripRenderHandler;
import com.simibubi.create.content.equipment.symmetryWand.SymmetryHandler;
import com.simibubi.create.content.equipment.toolbox.ToolboxHandlerClient;
import com.simibubi.create.content.equipment.zapper.ZapperItem;
import com.simibubi.create.content.equipment.zapper.terrainzapper.WorldshaperRenderHandler;
import com.simibubi.create.content.kinetics.KineticDebugger;
import com.simibubi.create.content.kinetics.belt.item.BeltConnectorHandler;
import com.simibubi.create.content.kinetics.fan.AirCurrent;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPointHandler;
import com.simibubi.create.content.kinetics.turntable.TurntableHandler;
import com.simibubi.create.content.logistics.depot.EjectorTargetHandler;
import com.simibubi.create.content.processing.sequenced.SequencedAssemblyRecipe;
import com.simibubi.create.content.redstone.displayLink.DisplayLinkBlockItem;
import com.simibubi.create.content.redstone.link.LinkRenderer;
import com.simibubi.create.content.redstone.link.controller.LinkedControllerClientHandler;
import com.simibubi.create.content.trains.CameraDistanceModifier;
import com.simibubi.create.content.trains.TrainHUD;
import com.simibubi.create.content.trains.entity.CarriageContraptionEntity;
import com.simibubi.create.content.trains.entity.CarriageCouplingRenderer;
import com.simibubi.create.content.trains.entity.TrainRelocator;
import com.simibubi.create.content.trains.schedule.TrainHatArmorLayer;
import com.simibubi.create.content.trains.track.CurvedTrackInteraction;
import com.simibubi.create.content.trains.track.TrackBlockItem;
import com.simibubi.create.content.trains.track.TrackBlockOutline;
import com.simibubi.create.content.trains.track.TrackPlacement;
import com.simibubi.create.content.trains.track.TrackTargetingClient;
import com.simibubi.create.foundation.blockEntity.behaviour.edgeInteraction.EdgeInteractionRenderer;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringRenderer;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollValueHandler;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollValueRenderer;
import com.simibubi.create.foundation.fluid.FluidHelper;
import com.simibubi.create.foundation.item.TooltipModifier;
import com.simibubi.create.foundation.networking.LeftClickPacket;
import com.simibubi.create.foundation.placement.PlacementHelpers;
import com.simibubi.create.foundation.ponder.PonderTooltipHandler;
import com.simibubi.create.foundation.render.SuperRenderTypeBuffer;
import com.simibubi.create.foundation.sound.SoundScapes;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.CameraAngleAnimationService;
import com.simibubi.create.foundation.utility.ServerSpeedProvider;
import com.simibubi.create.foundation.utility.worldWrappers.WrappedClientWorld;
import com.simibubi.create.infrastructure.config.AllConfigs;
import com.simibubi.create.infrastructure.gui.OpenCreateMenuButton;

import io.github.fabricators_of_create.porting_lib.event.client.CameraSetupCallback;
import io.github.fabricators_of_create.porting_lib.event.client.CameraSetupCallback.CameraInfo;
import io.github.fabricators_of_create.porting_lib.event.client.ClientWorldEvents;
import io.github.fabricators_of_create.porting_lib.event.client.DrawSelectionEvents;
import io.github.fabricators_of_create.porting_lib.event.client.FogEvents;
import io.github.fabricators_of_create.porting_lib.event.client.FogEvents.ColorData;
import io.github.fabricators_of_create.porting_lib.event.client.InteractEvents;
import io.github.fabricators_of_create.porting_lib.event.client.ParticleManagerRegistrationCallback;
import io.github.fabricators_of_create.porting_lib.event.client.RenderArmCallback;
import io.github.fabricators_of_create.porting_lib.event.client.RenderHandCallback;
import io.github.fabricators_of_create.porting_lib.event.client.RenderTickStartCallback;
import io.github.fabricators_of_create.porting_lib.event.client.RenderTooltipBorderColorCallback;
import io.github.fabricators_of_create.porting_lib.event.common.AttackAirCallback;
import io.github.fabricators_of_create.porting_lib.event.common.MountEntityCallback;
import io.github.fabricators_of_create.porting_lib.event.common.PlayerTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback.RegistrationHelper;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.Vec3;

public class ClientEvents {

	public static void onTickStart(Minecraft client) {
		LinkedControllerClientHandler.tick();
		ControlsHandler.tick();
		AirCurrent.tickClientPlayerSounds();
		// fabric: fix #608
		// This tracks the current held item. Because of event order differences from forge, it gets changed too
		// quickly for the update packet to work properly. Move to here to fix.
		ArmInteractionPointHandler.tick();
	}

	public static void onTick(Minecraft client) {
		if (!isGameActive())
			return;

		Level world = Minecraft.getInstance().level;

		SoundScapes.tick();
		AnimationTickHolder.tick();

		CreateClient.SCHEMATIC_SENDER.tick();
		CreateClient.SCHEMATIC_AND_QUILL_HANDLER.tick();
		CreateClient.GLUE_HANDLER.tick();
		CreateClient.SCHEMATIC_HANDLER.tick();
		CreateClient.ZAPPER_RENDER_HANDLER.tick();
		CreateClient.POTATO_CANNON_RENDER_HANDLER.tick();
		CreateClient.SOUL_PULSE_EFFECT_HANDLER.tick(world);
		CreateClient.RAILWAYS.clientTick();

		ContraptionHandler.tick(world);
		CapabilityMinecartController.tick(world);
		CouplingPhysics.tick(world);

		PonderTooltipHandler.tick();
		// ScreenOpener.tick();
		ServerSpeedProvider.clientTick();
		BeltConnectorHandler.tick();
//		BeltSlicer.tickHoveringInformation();
		FilteringRenderer.tick();
		LinkRenderer.tick();
		ScrollValueRenderer.tick();
		ChassisRangeDisplay.tick();
		EdgeInteractionRenderer.tick();
		GirderWrenchBehavior.tick();
		WorldshaperRenderHandler.tick();
		CouplingHandlerClient.tick();
		CouplingRenderer.tickDebugModeRenders();
		KineticDebugger.tick();
		ExtendoGripRenderHandler.tick();
		// CollisionDebugger.tick();
		// fabric: fix #608, see above
//		ArmInteractionPointHandler.tick();
		EjectorTargetHandler.tick();
		PlacementHelpers.tick();
		CreateClient.OUTLINER.tickOutlines();
		CreateClient.GHOST_BLOCKS.tickGhosts();
		ContraptionRenderDispatcher.tick(world);
		BlueprintOverlayRenderer.tick();
		ToolboxHandlerClient.clientTick();
		TrackTargetingClient.clientTick();
		TrackPlacement.clientTick();
		TrainRelocator.clientTick();
		DisplayLinkBlockItem.clientTick();
		CurvedTrackInteraction.clientTick();
		CameraDistanceModifier.tick();
		CameraAngleAnimationService.tick();
		TrainHUD.tick();
		ClipboardValueSettingsHandler.clientTick();
		CreateClient.VALUE_SETTINGS_HANDLER.tick();
		ScrollValueHandler.tick();
		NetheriteBacktankFirstPersonRenderer.clientTick();
		// fabric: see comment
		AllKeys.fixBinds();
	}

	public static void onJoin(ClientPacketListener handler, PacketSender sender, Minecraft client) {
		CreateClient.checkGraphicsFanciness();
	}

	public static void onLeave(ClientPacketListener handler, Minecraft client) {
		CreateClient.RAILWAYS.cleanUp();
	}

	public static void onLoadWorld(Minecraft client, ClientLevel world) {
		if (world.isClientSide() && world instanceof ClientLevel && !(world instanceof WrappedClientWorld)) {
			CreateClient.invalidateRenderers();
			AnimationTickHolder.reset();
		}
	}

	public static void onUnloadWorld(Minecraft client, ClientLevel world) {
		if (world
			.isClientSide()) {
			CreateClient.invalidateRenderers();
			CreateClient.SOUL_PULSE_EFFECT_HANDLER.refresh();
			AnimationTickHolder.reset();
			ControlsHandler.levelUnloaded(world);
		}
	}

	public static void onRenderWorld(WorldRenderContext event) {
		PoseStack ms = event.matrixStack();
		ms.pushPose();
		SuperRenderTypeBuffer buffer = SuperRenderTypeBuffer.getInstance();
		float partialTicks = AnimationTickHolder.getPartialTicks();
		Vec3 camera = Minecraft.getInstance().gameRenderer.getMainCamera()
			.getPosition();

		TrackBlockOutline.drawCurveSelection(ms, buffer, camera);
		TrackTargetingClient.render(ms, buffer, camera);
		CouplingRenderer.renderAll(ms, buffer, camera);
		CarriageCouplingRenderer.renderAll(ms, buffer, camera);
		CreateClient.SCHEMATIC_HANDLER.render(ms, buffer, camera);
		CreateClient.GHOST_BLOCKS.renderAll(ms, buffer, camera);
		CreateClient.OUTLINER.renderOutlines(ms, buffer, camera, partialTicks);

		buffer.draw();
		RenderSystem.enableCull();
		ms.popPose();
	}

	public static boolean onCameraSetup(CameraInfo info) {
		float partialTicks = AnimationTickHolder.getPartialTicks();

		if (CameraAngleAnimationService.isYawAnimating())
			info.yaw = CameraAngleAnimationService.getYaw(partialTicks);

		if (CameraAngleAnimationService.isPitchAnimating())
			info.pitch = CameraAngleAnimationService.getPitch(partialTicks);
		return false;
	}

	public static RenderTooltipBorderColorCallback.BorderColorEntry getItemTooltipColor(ItemStack stack, int originalBorderColorStart, int originalBorderColorEnd) {
		return PonderTooltipHandler.handleTooltipColor(stack, originalBorderColorStart, originalBorderColorEnd);
	}

	public static void addToItemTooltip(ItemStack stack, TooltipFlag iTooltipFlag, List<Component> itemTooltip) {
		if (!AllConfigs.client().tooltips.get())
			return;
		Player player = Minecraft.getInstance().player;
		if (player == null)
			return;

		Item item = stack.getItem();
		TooltipModifier modifier = TooltipModifier.REGISTRY.get(item);
		if (modifier != null && modifier != TooltipModifier.EMPTY) {
			modifier.modify(stack, player, iTooltipFlag, itemTooltip);
		}

		PonderTooltipHandler.addToTooltip(stack, itemTooltip);
		SequencedAssemblyRecipe.addToTooltip(stack, itemTooltip);
	}

	public static void onRenderTick() {
		if (!isGameActive())
			return;
		TurntableHandler.gameRenderTick();
	}

	public static InteractionResult onMount(Entity mounted, Entity mounting, boolean isMounting) {
		if (mounting != Minecraft.getInstance().player)
			return InteractionResult.PASS;

		if (!isMounting) {
			CameraDistanceModifier.reset();
			return InteractionResult.PASS;
		}

		if (!isMounting || !(mounted instanceof CarriageContraptionEntity carriage)) {
			return InteractionResult.PASS;
		}

		CameraDistanceModifier.zoomOut();
		return InteractionResult.PASS;
	}

	protected static boolean isGameActive() {
		return !(Minecraft.getInstance().level == null || Minecraft.getInstance().player == null);
	}

	public static boolean getFogDensity(FogRenderer.FogMode mode, FogType type, Camera camera, float partialTick, float renderDistance, float nearDistance, float farDistance, FogShape shape, FogEvents.FogData fogData) {
		Level level = Minecraft.getInstance().level;
		BlockPos blockPos = camera.getBlockPosition();
		FluidState fluidState = level.getFluidState(blockPos);
		if (camera.getPosition().y >= blockPos.getY() + fluidState.getHeight(level, blockPos))
			return false;
		Fluid fluid = fluidState.getType();
		Entity entity = camera.getEntity();

		if (AllFluids.CHOCOLATE.get()
			.isSame(fluid)) {
			fogData.scaleFarPlaneDistance(1f / 32f * AllConfigs.client().chocolateTransparencyMultiplier.getF());
			return true;
		}

		if (AllFluids.HONEY.get()
			.isSame(fluid)) {
			fogData.scaleFarPlaneDistance(1f / 8f * AllConfigs.client().honeyTransparencyMultiplier.getF());
			return true;
		}

		if (entity.isSpectator())
			return false;

		ItemStack divingHelmet = DivingHelmetItem.getWornItem(entity);
		if (!divingHelmet.isEmpty()) {
			if (FluidHelper.isWater(fluid)) {
				fogData.scaleFarPlaneDistance(6.25f);
				return true;
			} else if (FluidHelper.isLava(fluid) && NetheriteDivingHandler.isNetheriteDivingHelmet(divingHelmet)) {
				fogData.setNearPlaneDistance(-4.0f);
				fogData.setFarPlaneDistance(20.0f);
				return true;
			}
		}
		return false;
	}

	public static void getFogColor(ColorData event, float partialTicks) {
		Camera info = event.getCamera();
		Level level = Minecraft.getInstance().level;
		BlockPos blockPos = info.getBlockPosition();
		FluidState fluidState = level.getFluidState(blockPos);
		if (info.getPosition().y > blockPos.getY() + fluidState.getHeight(level, blockPos))
			return;

		Fluid fluid = fluidState.getType();

		if (AllFluids.CHOCOLATE.get()
			.isSame(fluid)) {
			event.setRed(98 / 255f);
			event.setGreen(32 / 255f);
			event.setBlue(32 / 255f);
			return;
		}

		if (AllFluids.HONEY.get()
			.isSame(fluid)) {
			event.setRed(234 / 255f);
			event.setGreen(174 / 255f);
			event.setBlue(47 / 255f);
			return;
		}
	}

	public static void leftClickEmpty(LocalPlayer player) {
		ItemStack stack = player.getMainHandItem();
		if (stack.getItem() instanceof ZapperItem) {
			AllPackets.getChannel().sendToServer(new LeftClickPacket());
		}
	}

	public static class ModBusEvents {

		public static void registerClientReloadListeners() {
			ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(CreateClient.RESOURCE_RELOAD_LISTENER);
		}
	}

	public static void addEntityRendererLayers(EntityType<? extends LivingEntity> entityType, LivingEntityRenderer<?, ?> entityRenderer,
											   RegistrationHelper registrationHelper, EntityRendererProvider.Context context) {
		BacktankArmorLayer.registerOn(entityRenderer, registrationHelper);
		TrainHatArmorLayer.registerOn(entityRenderer, registrationHelper);
	}

	public static void register() {
		ModBusEvents.registerClientReloadListeners();

		ClientTickEvents.END_CLIENT_TICK.register(ClientEvents::onTick);
		ClientTickEvents.START_CLIENT_TICK.register(ClientEvents::onTickStart);
		ClientWorldEvents.LOAD.register(ClientEvents::onLoadWorld);
		ClientWorldEvents.UNLOAD.register(ClientEvents::onUnloadWorld);
		ClientWorldEvents.LOAD.register(CommonEvents::onLoadWorld);
		ClientWorldEvents.UNLOAD.register(CommonEvents::onUnloadWorld);
		ClientChunkEvents.CHUNK_UNLOAD.register(CommonEvents::onChunkUnloaded);
		ClientPlayConnectionEvents.JOIN.register(ClientEvents::onJoin);
		ClientEntityEvents.ENTITY_LOAD.register(CommonEvents::onEntityAdded);
		WorldRenderEvents.AFTER_TRANSLUCENT.register(ClientEvents::onRenderWorld);
		ItemTooltipCallback.EVENT.register(ClientEvents::addToItemTooltip);
		FogEvents.RENDER_FOG.register(ClientEvents::getFogDensity);
		FogEvents.SET_COLOR.register(ClientEvents::getFogColor);
		RenderTickStartCallback.EVENT.register(ClientEvents::onRenderTick);
		RenderTooltipBorderColorCallback.EVENT.register(ClientEvents::getItemTooltipColor);
		AttackAirCallback.EVENT.register(ClientEvents::leftClickEmpty);
		UseBlockCallback.EVENT.register(TrackBlockItem::sendExtenderPacket);
		MountEntityCallback.EVENT.register(ClientEvents::onMount);
		LivingEntityFeatureRendererRegistrationCallback.EVENT.register(ClientEvents::addEntityRendererLayers);
		CameraSetupCallback.EVENT.register(ClientEvents::onCameraSetup);
		DrawSelectionEvents.BLOCK.register(ClipboardValueSettingsHandler::drawCustomBlockSelection);

		// External Events

		ClientTickEvents.END_CLIENT_TICK.register(SymmetryHandler::onClientTick);
		WorldRenderEvents.AFTER_TRANSLUCENT.register(SymmetryHandler::render);
		UseBlockCallback.EVENT.register(ArmInteractionPointHandler::rightClickingBlocksSelectsThem);
		UseBlockCallback.EVENT.register(EjectorTargetHandler::rightClickingBlocksSelectsThem);
		AttackBlockCallback.EVENT.register(ArmInteractionPointHandler::leftClickingBlocksDeselectsThem);
		AttackBlockCallback.EVENT.register(EjectorTargetHandler::leftClickingBlocksDeselectsThem);
		ParticleManagerRegistrationCallback.EVENT.register(AllParticleTypes::registerFactories);
		RenderHandCallback.EVENT.register(ExtendoGripRenderHandler::onRenderPlayerHand);
		InteractEvents.USE.register(ContraptionHandlerClient::rightClickingOnContraptionsGetsHandledLocally);
		RenderArmCallback.EVENT.register(NetheriteBacktankFirstPersonRenderer::onRenderPlayerHand);
		PlayerTickEvents.END.register(ContraptionHandlerClient::preventRemotePlayersWalkingAnimations);
		ClientPlayConnectionEvents.DISCONNECT.register(ClientEvents::onLeave);
		DrawSelectionEvents.BLOCK.register(TrackBlockOutline::drawCustomBlockSelection);
		// we need to add our config button after mod menu, so we register our event with a phase that comes later
		ResourceLocation latePhase = Create.asResource("late");
		ScreenEvents.AFTER_INIT.addPhaseOrdering(Event.DEFAULT_PHASE, latePhase);
		ScreenEvents.AFTER_INIT.register(latePhase, OpenCreateMenuButton.OpenConfigButtonHandler::onGuiInit);

		// Flywheel Events

		FlywheelEvents.BEGIN_FRAME.register(ContraptionRenderDispatcher::beginFrame);
		FlywheelEvents.RENDER_LAYER.register(ContraptionRenderDispatcher::renderLayer);
		FlywheelEvents.RELOAD_RENDERERS.register(ContraptionRenderDispatcher::onRendererReload);
		FlywheelEvents.GATHER_CONTEXT.register(ContraptionRenderDispatcher::gatherContext);
	}

}
