package com.simibubi.create.events;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.AllFluids;
import com.simibubi.create.AllItems;
import com.simibubi.create.Create;
import com.simibubi.create.CreateClient;
import com.simibubi.create.content.contraptions.KineticDebugger;
import com.simibubi.create.content.contraptions.base.IRotate;
import com.simibubi.create.content.contraptions.components.fan.AirCurrent;
import com.simibubi.create.content.contraptions.components.flywheel.engine.EngineBlock;
import com.simibubi.create.content.contraptions.components.flywheel.engine.FurnaceEngineBlock;
import com.simibubi.create.content.contraptions.components.structureMovement.ContraptionHandler;
import com.simibubi.create.content.contraptions.components.structureMovement.ContraptionHandlerClient;
import com.simibubi.create.content.contraptions.components.structureMovement.chassis.ChassisRangeDisplay;
import com.simibubi.create.content.contraptions.components.structureMovement.render.ContraptionRenderDispatcher;
import com.simibubi.create.content.contraptions.components.structureMovement.train.CouplingHandlerClient;
import com.simibubi.create.content.contraptions.components.structureMovement.train.CouplingPhysics;
import com.simibubi.create.content.contraptions.components.structureMovement.train.CouplingRenderer;
import com.simibubi.create.content.contraptions.components.structureMovement.train.capability.CapabilityMinecartController;
import com.simibubi.create.content.contraptions.components.turntable.TurntableHandler;
import com.simibubi.create.content.contraptions.goggles.GoggleOverlayRenderer;
import com.simibubi.create.content.contraptions.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.contraptions.relays.belt.item.BeltConnectorHandler;
import com.simibubi.create.content.curiosities.armor.CopperBacktankArmorLayer;
import com.simibubi.create.content.curiosities.symmetry.SymmetryHandler;
import com.simibubi.create.content.curiosities.tools.BlueprintOverlayRenderer;
import com.simibubi.create.content.curiosities.tools.ExtendoGripRenderHandler;
import com.simibubi.create.content.curiosities.zapper.ZapperItem;
import com.simibubi.create.content.curiosities.zapper.ZapperRenderHandler;
import com.simibubi.create.content.curiosities.zapper.terrainzapper.WorldshaperRenderHandler;
import com.simibubi.create.content.logistics.block.depot.EjectorTargetHandler;
import com.simibubi.create.content.logistics.block.mechanicalArm.ArmInteractionPointHandler;
import com.simibubi.create.content.logistics.item.LinkedControllerClientHandler;
import com.simibubi.create.foundation.block.ItemUseOverrides;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.fluid.FluidHelper;
import com.simibubi.create.foundation.item.ItemDescription;
import com.simibubi.create.foundation.item.TooltipHelper;
import com.simibubi.create.foundation.networking.AllPackets;
import com.simibubi.create.foundation.networking.LeftClickPacket;
import com.simibubi.create.foundation.ponder.PonderTooltipHandler;
import com.simibubi.create.foundation.render.KineticRenderer;
import com.simibubi.create.foundation.render.backend.FastRenderDispatcher;
import com.simibubi.create.foundation.render.backend.RenderWork;
import com.simibubi.create.foundation.renderState.SuperRenderTypeBuffer;
import com.simibubi.create.foundation.sound.SoundScapes;
import com.simibubi.create.foundation.tileEntity.behaviour.edgeInteraction.EdgeInteractionHandler;
import com.simibubi.create.foundation.tileEntity.behaviour.edgeInteraction.EdgeInteractionRenderer;
import com.simibubi.create.foundation.tileEntity.behaviour.filtering.FilteringHandler;
import com.simibubi.create.foundation.tileEntity.behaviour.filtering.FilteringRenderer;
import com.simibubi.create.foundation.tileEntity.behaviour.linked.LinkHandler;
import com.simibubi.create.foundation.tileEntity.behaviour.linked.LinkRenderer;
import com.simibubi.create.foundation.tileEntity.behaviour.scrollvalue.ScrollValueHandler;
import com.simibubi.create.foundation.tileEntity.behaviour.scrollvalue.ScrollValueRenderer;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.ServerSpeedProvider;
import com.simibubi.create.foundation.utility.placement.PlacementHelpers;
import com.simibubi.create.foundation.utility.worldWrappers.WrappedClientWorld;
import com.simibubi.create.lib.event.ClientWorldEvents;
import com.simibubi.create.lib.event.FogEvents;
import com.simibubi.create.lib.event.LeftClickAirCallback;
import com.simibubi.create.lib.event.OverlayRenderCallback;
import com.simibubi.create.lib.event.PlayerTickEndCallback;
import com.simibubi.create.lib.event.RenderHandCallback;
import com.simibubi.create.lib.event.RenderTickStartCallback;
import com.simibubi.create.lib.event.RenderTooltipBorderColorCallback;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.network.play.ClientPlayNetHandler;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;

public class ClientEvents {

	private static final String itemPrefix = "item." + Create.ID;
	private static final String blockPrefix = "block." + Create.ID;

	public static void onTickStart(Minecraft client) {
		LinkedControllerClientHandler.tick();
		AirCurrent.tickClientPlayerSounds();
	}

	public static void onTick(Minecraft client) {
		World world = client.world;
		if (!isGameActive())
			return;

//		if (event.phase == Phase.START) {
//			LinkedControllerClientHandler.tick();
//			AirCurrent.tickClientPlayerSounds();
//			return;
//		}

		SoundScapes.tick();
		AnimationTickHolder.tick();
		FastRenderDispatcher.tick();
		ScrollValueHandler.tick();

		CreateClient.SCHEMATIC_SENDER.tick();
		CreateClient.SCHEMATIC_AND_QUILL_HANDLER.tick();
		CreateClient.SCHEMATIC_HANDLER.tick();

		ContraptionHandler.tick(world);
		CapabilityMinecartController.tick(world);
		CouplingPhysics.tick(world);

		PonderTooltipHandler.tick();
		// ScreenOpener.tick();
		ServerSpeedProvider.clientTick();
		BeltConnectorHandler.tick();
		FilteringRenderer.tick();
		LinkRenderer.tick();
		ScrollValueRenderer.tick();
		ChassisRangeDisplay.tick();
		EdgeInteractionRenderer.tick();
		WorldshaperRenderHandler.tick();
		CouplingHandlerClient.tick();
		CouplingRenderer.tickDebugModeRenders();
		KineticDebugger.tick();
		ZapperRenderHandler.tick();
		ExtendoGripRenderHandler.tick();
		// CollisionDebugger.tick();
		ArmInteractionPointHandler.tick();
		EjectorTargetHandler.tick();
		PlacementHelpers.tick();
		CreateClient.OUTLINER.tickOutlines();
		CreateClient.GHOST_BLOCKS.tickGhosts();
		ContraptionRenderDispatcher.tick();
		BlueprintOverlayRenderer.tick();
	}

	public static void onJoin(ClientPlayNetHandler handler, PacketSender sender, Minecraft client) {
		CreateClient.checkGraphicsFanciness();
	}

	public static void onLoadWorld(Minecraft client, ClientWorld world) {
		if (world.isRemote() && !(world instanceof WrappedClientWorld)) {
			CreateClient.invalidateRenderers(world);
			AnimationTickHolder.reset();
			KineticRenderer renderer = CreateClient.KINETIC_RENDERER.get(world);
			renderer.invalidate();
			((ClientWorld) world).loadedTileEntityList.forEach(renderer::add);
		}

		/*
		 * i was getting nullPointers when trying to call this during client setup,
		 * so i assume minecraft's language manager isn't yet fully loaded at that time.
		 * not sure where else to call this tho :S
		 */
		IHaveGoggleInformation.numberFormat.update();
	}

	public static void onUnloadWorld(Minecraft client, ClientWorld world) {
		if (world
			.isRemote()) {
			CreateClient.invalidateRenderers(world);
			AnimationTickHolder.reset();
		}
	}

	public static void onRenderWorld(WorldRenderContext event) {
		Vector3d cameraPos = Minecraft.getInstance().gameRenderer.getActiveRenderInfo()
			.getProjectedView();
		float pt = AnimationTickHolder.getPartialTicks();

		MatrixStack ms = event.matrixStack();
		ms.push();
		ms.translate(-cameraPos.getX(), -cameraPos.getY(), -cameraPos.getZ());
		SuperRenderTypeBuffer buffer = SuperRenderTypeBuffer.getInstance();

		CouplingRenderer.renderAll(ms, buffer);
		CreateClient.SCHEMATIC_HANDLER.render(ms, buffer);
		CreateClient.GHOST_BLOCKS.renderAll(ms, buffer);

		CreateClient.OUTLINER.renderOutlines(ms, buffer, pt);
		// LightVolumeDebugger.render(ms, buffer);
		buffer.draw();
		RenderSystem.enableCull();

		ms.pop();

		RenderWork.runAll();
	}

	public static void onRenderOverlay(MatrixStack stack, float partialTicks, MainWindow window, OverlayRenderCallback.Types type) {
//		MatrixStack ms = event.getMatrixStack();
		IRenderTypeBuffer.Impl buffers = Minecraft.getInstance()
			.getBufferBuilders()
			.getEntityVertexConsumers();
		int light = 0xF000F0;
		int overlay = OverlayTexture.DEFAULT_UV;
//		float pt = event.getPartialTicks();

		if (type == OverlayRenderCallback.Types.AIR) {
			CopperBacktankArmorLayer.renderRemainingAirOverlay(stack, buffers, light, overlay, partialTicks);
			return;
		}
//		if (type != OverlayRenderCallback.Types.HOTBAR)
//			return;

		onRenderHotbar(stack, buffers, light, overlay, partialTicks);
	}

	public static void onRenderHotbar(MatrixStack ms, IRenderTypeBuffer buffer, int light, int overlay,
		float partialTicks) {
		CreateClient.SCHEMATIC_HANDLER.renderOverlay(ms, buffer, light, overlay, partialTicks);
		LinkedControllerClientHandler.renderOverlay(ms, buffer, light, overlay, partialTicks);
		BlueprintOverlayRenderer.renderOverlay(ms, buffer, light, overlay, partialTicks);
		GoggleOverlayRenderer.renderOverlay(ms, buffer, light, overlay, partialTicks);
	}

	public static RenderTooltipBorderColorCallback.BorderColorEntry getItemTooltipColor(ItemStack stack,
			int originalBorderColorStart, int originalBorderColorEnd) {
		return PonderTooltipHandler.handleTooltipColor(stack, originalBorderColorStart, originalBorderColorEnd);
	}

	public static void addToItemTooltip(ItemStack stack, ITooltipFlag iTooltipFlag, List<ITextComponent> itemTooltip) {
		if (!AllConfigs.CLIENT.tooltips.get())
			return;
		if (Minecraft.getInstance().player == null)
			return;

		String translationKey = stack.getItem()
			.getTranslationKey(stack);
		if (!translationKey.startsWith(itemPrefix) && !translationKey.startsWith(blockPrefix))
			return;

		if (TooltipHelper.hasTooltip(stack, Minecraft.getInstance().player)) {
			List<ITextComponent> toolTip = new ArrayList<>();
			toolTip.add(itemTooltip.remove(0));
			TooltipHelper.getTooltip(stack)
				.addInformation(toolTip);
			itemTooltip.addAll(0, toolTip);
		}

		if (stack.getItem() instanceof BlockItem) {
			BlockItem item = (BlockItem) stack.getItem();
			if (item.getBlock() instanceof IRotate || item.getBlock() instanceof EngineBlock) {
				List<ITextComponent> kineticStats = ItemDescription.getKineticStats(item.getBlock());
				if (!kineticStats.isEmpty()) {
					itemTooltip
						.add(new StringTextComponent(""));
					itemTooltip
						.addAll(kineticStats);
				}
			}
		}

		PonderTooltipHandler.addToTooltip(itemTooltip, stack);
	}

	public static void onRenderTick() {
		if (!isGameActive())
			return;
		TurntableHandler.gameRenderTick();
	}

	protected static boolean isGameActive() {
		return !(Minecraft.getInstance().world == null || Minecraft.getInstance().player == null);
	}

	public static float getFogDensity(ActiveRenderInfo info, float currentDensity) {
//		ActiveRenderInfo info = event.getInfo();
		FluidState fluidState = info.getFluidState();
		if (fluidState.isEmpty())
			return currentDensity;
		Fluid fluid = fluidState.getFluid();

		if (fluid.isEquivalentTo(AllFluids.CHOCOLATE.get())) {
//			event.setDensity(5f);
//			event.setCanceled(true);
			return 5f;
		}

		if (fluid.isEquivalentTo(AllFluids.HONEY.get())) {
//			event.setDensity(1.5f);
//			event.setCanceled(true);
			return 1.5f;
		}

		if (FluidHelper.isWater(fluid) && AllItems.DIVING_HELMET.get()
			.isWornBy(Minecraft.getInstance().renderViewEntity)) {
//			event.setDensity(0.010f);
//			event.setCanceled(true);
			return 0.010f;
		}
		return currentDensity;
	}

	public static Vector3f getFogColor(ActiveRenderInfo info, Vector3f currentColor) {
//		ActiveRenderInfo info = event.getInfo();
		FluidState fluidState = info.getFluidState();
		if (fluidState.isEmpty())
			return currentColor;
		Fluid fluid = fluidState.getFluid();

		if (fluid.isEquivalentTo(AllFluids.CHOCOLATE.get())) {
//			event.setRed(98 / 256f);
//			event.setGreen(32 / 256f);
//			event.setBlue(32 / 256f);
			return new Vector3f(98 / 256f, 32 / 256f, 32 / 256f);
		}

		if (fluid.isEquivalentTo(AllFluids.HONEY.get())) {
//			event.setRed(234 / 256f);
//			event.setGreen(174 / 256f);
//			event.setBlue(47 / 256f);
			return new Vector3f(234 / 256f, 174 / 256f, 47 / 256f);
		}
		return  currentColor;
	}

	public static void leftClickEmpty(ClientPlayerEntity player) {
		ItemStack stack = player.getHeldItemMainhand();
		if (stack.getItem() instanceof ZapperItem) {
			AllPackets.channel.sendToServer(new LeftClickPacket());
		}
	}

	public static void register() {
		ClientTickEvents.END_CLIENT_TICK.register(ClientEvents::onTick);
		ClientTickEvents.START_CLIENT_TICK.register(ClientEvents::onTickStart);
		RenderTickStartCallback.EVENT.register(ClientEvents::onRenderTick);
		ClientPlayConnectionEvents.JOIN.register(ClientEvents::onJoin);
		ClientWorldEvents.LOAD.register(ClientEvents::onLoadWorld);
		ClientWorldEvents.UNLOAD.register(ClientEvents::onUnloadWorld);
		WorldRenderEvents.END.register(ClientEvents::onRenderWorld);
		ItemTooltipCallback.EVENT.register(ClientEvents::addToItemTooltip);
		RenderTooltipBorderColorCallback.EVENT.register(ClientEvents::getItemTooltipColor);
		LeftClickAirCallback.EVENT.register(ClientEvents::leftClickEmpty);
		OverlayRenderCallback.EVENT.register(ClientEvents::onRenderOverlay);
		FogEvents.SET_DENSITY.register(ClientEvents::getFogDensity);
		FogEvents.SET_COLOR.register(ClientEvents::getFogColor);

		ClientChunkEvents.CHUNK_UNLOAD.register(CommonEvents::onChunkUnloaded);
		ClientTickEvents.END_WORLD_TICK.register(CommonEvents::onWorldTick);
		ClientEntityEvents.ENTITY_LOAD.register(CommonEvents::onEntityAdded);
		ClientWorldEvents.LOAD.register((client, world) -> CommonEvents.onLoadWorld(world));
		ClientWorldEvents.UNLOAD.register((client, world) -> CommonEvents.onUnloadWorld(world));

		// External Events

		RenderHandCallback.EVENT.register(ExtendoGripRenderHandler::onRenderPlayerHand);
		RenderHandCallback.EVENT.register(ZapperRenderHandler::onRenderPlayerHand);
		UseBlockCallback.EVENT.register(ItemUseOverrides::onBlockActivated);
		UseBlockCallback.EVENT.register(EdgeInteractionHandler::onBlockActivated);
		UseBlockCallback.EVENT.register(FilteringHandler::onBlockActivated);
		UseBlockCallback.EVENT.register(LinkHandler::onBlockActivated);
		UseBlockCallback.EVENT.register(ArmInteractionPointHandler::rightClickingBlocksSelectsThem);
		UseBlockCallback.EVENT.register(EjectorTargetHandler::rightClickingBlocksSelectsThem);
		UseBlockCallback.EVENT.register(FurnaceEngineBlock::usingFurnaceEngineOnFurnacePreventsGUI);
		AttackBlockCallback.EVENT.register(ArmInteractionPointHandler::leftClickingBlocksDeselectsThem);
		AttackBlockCallback.EVENT.register(EjectorTargetHandler::leftClickingBlocksDeselectsThem);
		WorldRenderEvents.END.register(SymmetryHandler::render);
		ClientTickEvents.END_CLIENT_TICK.register(SymmetryHandler::onClientTick);
		PlayerBlockBreakEvents.AFTER.register(SymmetryHandler::onBlockDestroyed);
		PlayerTickEndCallback.EVENT.register(ContraptionHandlerClient::preventRemotePlayersWalkingAnimations);
		UseBlockCallback.EVENT.register(ContraptionHandlerClient::rightClickingOnContraptionsGetsHandledLocally);
		OverlayRenderCallback.EVENT.register(PlacementHelpers::onRender);
	}

//	public static void loadCompleted(FMLLoadCompleteEvent event) {
//		ModContainer createContainer = ModList.get().getModContainerById(Create.ID).orElseThrow(() -> new IllegalStateException("Create Mod Container missing after loadCompleted"));
//		createContainer.registerExtensionPoint(ExtensionPoint.CONFIGGUIFACTORY, () -> (mc, previousScreen) -> BaseConfigScreen.forCreate(previousScreen));
//	}
}
