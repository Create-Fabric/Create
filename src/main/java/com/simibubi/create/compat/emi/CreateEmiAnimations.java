package com.simibubi.create.compat.emi;

import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import com.jozufozu.flywheel.core.PartialModel;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.math.Vector3f;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.components.deployer.DeployerBlock;
import com.simibubi.create.content.contraptions.components.saw.SawBlock;
import com.simibubi.create.content.contraptions.processing.burner.BlazeBurnerBlock.HeatLevel;
import com.simibubi.create.foundation.fluid.FluidRenderer;
import com.simibubi.create.foundation.gui.CustomLightingSettings;
import com.simibubi.create.foundation.gui.ILightingSettings;
import com.simibubi.create.foundation.gui.UIRenderHelper;
import com.simibubi.create.foundation.gui.element.GuiGameElement;
import com.simibubi.create.foundation.utility.AnimationTickHolder;

import dev.emi.emi.api.widget.WidgetHolder;
import io.github.fabricators_of_create.porting_lib.util.FluidStack;

public class CreateEmiAnimations {
	private static final BlockState WHEEL = AllBlocks.CRUSHING_WHEEL.getDefaultState().setValue(BlockStateProperties.AXIS, Axis.X);
	public static final ILightingSettings DEFAULT_LIGHTING = CustomLightingSettings.builder()
		.firstLightRotation(12.5f, 45.0f)
		.secondLightRotation(-20.0f, 50.0f)
		.build();
	

	public static GuiGameElement.GuiRenderBuilder defaultBlockElement(BlockState state) {
		return GuiGameElement.of(state)
				.lighting(DEFAULT_LIGHTING);
	}

	public static GuiGameElement.GuiRenderBuilder defaultBlockElement(PartialModel partial) {
		return GuiGameElement.of(partial)
				.lighting(DEFAULT_LIGHTING);
	}

	public static float getCurrentAngle() {
		return (AnimationTickHolder.getRenderTime() * 4f) % 360;
	}

	public static BlockState shaft(Axis axis) {
		return AllBlocks.SHAFT.getDefaultState().setValue(BlockStateProperties.AXIS, axis);
	}

	public static PartialModel cogwheel() {
		return AllBlockPartials.SHAFTLESS_COGWHEEL;
	}

	public static GuiGameElement.GuiRenderBuilder blockElement(BlockState state) {
		return defaultBlockElement(state);
	}

	public static GuiGameElement.GuiRenderBuilder blockElement(PartialModel partial) {
		return defaultBlockElement(partial);
	}

	public static void addPress(WidgetHolder widgets, int x, int y, boolean basin) {
		widgets.addDrawable(x, y, 0, 0, (matrices, mouseX, mouseY, delta) -> {
			renderPress(matrices, 0, basin);
		});
	}

	public static void renderPress(PoseStack matrices, int offset, boolean basin) {
		matrices.translate(0, 0, 200);
		matrices.mulPose(Vector3f.XP.rotationDegrees(-15.5f));
		matrices.mulPose(Vector3f.YP.rotationDegrees(22.5f));
		int scale = basin ? 23 : 24;

		blockElement(shaft(Axis.Z))
				.rotateBlock(0, 0, getCurrentAngle())
				.scale(scale)
				.render(matrices);

		blockElement(AllBlocks.MECHANICAL_PRESS.getDefaultState())
				.scale(scale)
				.render(matrices);

		blockElement(AllBlockPartials.MECHANICAL_PRESS_HEAD)
				.atLocal(0, -getAnimatedHeadOffset(offset), 0)
				.scale(scale)
				.render(matrices);

		if (basin) {
			blockElement(AllBlocks.BASIN.getDefaultState())
					.atLocal(0, 1.65, 0)
					.scale(scale)
					.render(matrices);
		}
	}

	private static float getAnimatedHeadOffset(int offset) {
		float cycle = (AnimationTickHolder.getRenderTime() - offset * 8) % 30;
		if (cycle < 10) {
			float progress = cycle / 10;
			return -(progress * progress * progress);
		}
		if (cycle < 15)
			return -1;
		if (cycle < 20)
			return -1 + (1 - ((20 - cycle) / 5));
		return 0;
	}

	public static void addBlazeBurner(WidgetHolder widgets, int x, int y, HeatLevel level) {
		widgets.addDrawable(x, y, 0, 0, (matrices, mouseX, mouseY, delta) -> {
			matrices.translate(0, 0, 200);
			matrices.mulPose(Vector3f.XP.rotationDegrees(-15.5f));
			matrices.mulPose(Vector3f.YP.rotationDegrees(22.5f));
			int scale = 23;

			blockElement(AllBlocks.BLAZE_BURNER.getDefaultState())
				.atLocal(0, 1.65, 0)
				.scale(scale)
				.render(matrices);

			float offset = (Mth.sin(AnimationTickHolder.getRenderTime() / 16f) + 0.5f) / 16f;
			PartialModel blaze = AllBlockPartials.BLAZES.get(level);
			blockElement(blaze)
				.atLocal(1, 1.65 + offset, 1)
				.rotate(0, 180, 0)
				.scale(scale)
				.render(matrices);
		});
	}

	public static void addMixer(WidgetHolder widgets, int x, int y) {
		widgets.addDrawable(x, y, 0, 0, (matrices, mouseX, mouseY, delta) -> {
			matrices.translate(0, 0, 200);
			matrices.mulPose(Vector3f.XP.rotationDegrees(-15.5f));
			matrices.mulPose(Vector3f.YP.rotationDegrees(22.5f));
			int scale = 23;

			blockElement(cogwheel())
				.rotateBlock(0, getCurrentAngle() * 2, 0)
				.atLocal(0, 0, 0)
				.scale(scale)
				.render(matrices);

			blockElement(AllBlocks.MECHANICAL_MIXER.getDefaultState())
				.atLocal(0, 0, 0)
				.scale(scale)
				.render(matrices);

			float animation = ((Mth.sin(AnimationTickHolder.getRenderTime() / 32f) + 1) / 5) + .5f;

			blockElement(AllBlockPartials.MECHANICAL_MIXER_POLE)
				.atLocal(0, animation, 0)
				.scale(scale)
				.render(matrices);

			blockElement(AllBlockPartials.MECHANICAL_MIXER_HEAD)
				.rotateBlock(0, getCurrentAngle() * 4, 0)
				.atLocal(0, animation, 0)
				.scale(scale)
				.render(matrices);

			blockElement(AllBlocks.BASIN.getDefaultState())
				.atLocal(0, 1.65, 0)
				.scale(scale)
				.render(matrices);
		});
	}

	public static void addSaw(WidgetHolder widgets, int x, int y) {
		widgets.addDrawable(x, y, 0, 0, (matrices, mouseX, mouseY, delta) -> {
			renderSaw(matrices, 0);
		});
	}

	public static void renderSaw(PoseStack matrices, int offset) {
		matrices.translate(0, 0, 200);
		matrices.translate(2, 22, 0);
		matrices.mulPose(Vector3f.XP.rotationDegrees(-15.5f));
		matrices.mulPose(Vector3f.YP.rotationDegrees(22.5f + 90));
		int scale = 25;

		blockElement(shaft(Axis.X))
			.rotateBlock(-getCurrentAngle(), 0, 0)
			.scale(scale)
			.render(matrices);

		blockElement(AllBlocks.MECHANICAL_SAW.getDefaultState()
			.setValue(SawBlock.FACING, Direction.UP))
			.rotateBlock(0, 0, 0)
			.scale(scale)
			.render(matrices);

		blockElement(AllBlockPartials.SAW_BLADE_VERTICAL_ACTIVE)
			.rotateBlock(0, -90, -90)
			.scale(scale)
			.render(matrices);
	}

	public static void addMillstone(WidgetHolder widgets, int x, int y) {
		widgets.addDrawable(x, y, 0, 0, (matrices, mouseX, mouseY, delta) -> {
			int scale = 22;

			blockElement(AllBlockPartials.MILLSTONE_COG)
				.rotateBlock(22.5, getCurrentAngle() * 2, 0)
				.scale(scale)
				.render(matrices);

			blockElement(AllBlocks.MILLSTONE.getDefaultState())
				.rotateBlock(22.5, 22.5, 0)
				.scale(scale)
				.render(matrices);
		});
	}

	public static void addCrushingWheels(WidgetHolder widgets, int x, int y) {
		widgets.addDrawable(x, y, 0, 0, (matrices, mouseX, mouseY, delta) -> {
			matrices.translate(0, 0, 100);
			matrices.mulPose(Vector3f.YP.rotationDegrees(-22.5f));
			int scale = 22;
	
			blockElement(WHEEL)
					.rotateBlock(0, 90, -getCurrentAngle())
					.scale(scale)
					.render(matrices);
	
			blockElement(WHEEL)
					.rotateBlock(0, 90, getCurrentAngle())
					.atLocal(2, 0, 0)
					.scale(scale)
					.render(matrices);
		});
	}

	public static void addFan(WidgetHolder widgets, int x, int y, Consumer<PoseStack> renderAttachedBlock) {
		widgets.addDrawable(x, y, 0, 0, (matrices, mouseX, mouseY, delta) -> {
			matrices.translate(0, 0, 200);
			matrices.mulPose(Vector3f.XP.rotationDegrees(-12.5f));
			matrices.mulPose(Vector3f.YP.rotationDegrees(22.5f));
			int scale = 24;
	
			defaultBlockElement(AllBlockPartials.ENCASED_FAN_INNER)
				.rotateBlock(180, 0, getCurrentAngle() * 16)
				.scale(scale)
				.render(matrices);
	
			defaultBlockElement(AllBlocks.ENCASED_FAN.getDefaultState())
				.rotateBlock(0, 180, 0)
				.atLocal(0, 0, 0)
				.scale(scale)
				.render(matrices);
	
			renderAttachedBlock.accept(matrices);
		});
	}

	public static void addDeployer(WidgetHolder widgets, int x, int y) {
		widgets.addDrawable(x, y, 0, 0, (matrices, mouseX, mouseY, delta) -> {
			renderDeployer(matrices, 0);
		});
	}

	public static void renderDeployer(PoseStack matrices, int offset) {
		matrices.translate(0, 0, 100);
		matrices.mulPose(Vector3f.XP.rotationDegrees(-15.5f));
		matrices.mulPose(Vector3f.YP.rotationDegrees(22.5f));
		int scale = 20;

		blockElement(shaft(Axis.Z))
			.rotateBlock(0, 0, getCurrentAngle())
			.scale(scale)
			.render(matrices);

		blockElement(AllBlocks.DEPLOYER.getDefaultState()
			.setValue(DeployerBlock.FACING, Direction.DOWN)
			.setValue(DeployerBlock.AXIS_ALONG_FIRST_COORDINATE, false))
			.scale(scale)
			.render(matrices);

		float cycle = (AnimationTickHolder.getRenderTime() - offset * 8) % 30;
		float off = cycle < 10 ? cycle / 10f : cycle < 20 ? (20 - cycle) / 10f : 0;

		matrices.pushPose();

		matrices.translate(0, off * 17, 0);
		blockElement(AllBlockPartials.DEPLOYER_POLE)
			.rotateBlock(90, 0, 0)
			.scale(scale)
			.render(matrices);
		blockElement(AllBlockPartials.DEPLOYER_HAND_HOLDING)
			.rotateBlock(90, 0, 0)
			.scale(scale)
			.render(matrices);

		matrices.popPose();

		blockElement(AllBlocks.DEPOT.getDefaultState())
			.atLocal(0, 2, 0)
			.scale(scale)
			.render(matrices);
	}

	public static void addSpout(WidgetHolder widgets, int x, int y, List<FluidStack> fluids) {
		widgets.addDrawable(x, y, 0, 0, (matrices, mouseX, mouseY, delta) -> {
			renderSpout(matrices, 0, fluids);
		});
	}

	public static void renderSpout(PoseStack matrices, int offset, List<FluidStack> fluids) {
		matrices.translate(0, 0, 100);
		matrices.mulPose(Vector3f.XP.rotationDegrees(-15.5f));
		matrices.mulPose(Vector3f.YP.rotationDegrees(22.5f));
		int scale = 20;

		blockElement(AllBlocks.SPOUT.getDefaultState())
			.scale(scale)
			.render(matrices);

		float cycle = (AnimationTickHolder.getRenderTime() - offset * 8) % 30;
		float squeeze = cycle < 20 ? Mth.sin((float) (cycle / 20f * Math.PI)) : 0;
		squeeze *= 20;

		matrices.pushPose();

		blockElement(AllBlockPartials.SPOUT_TOP)
			.scale(scale)
			.render(matrices);
		matrices.translate(0, -3 * squeeze / 32f, 0);
		blockElement(AllBlockPartials.SPOUT_MIDDLE)
			.scale(scale)
			.render(matrices);
		matrices.translate(0, -3 * squeeze / 32f, 0);
		blockElement(AllBlockPartials.SPOUT_BOTTOM)
			.scale(scale)
			.render(matrices);
		matrices.translate(0, -3 * squeeze / 32f, 0);

		matrices.popPose();

		blockElement(AllBlocks.DEPOT.getDefaultState())
			.atLocal(0, 2, 0)
			.scale(scale)
			.render(matrices);

		DEFAULT_LIGHTING.applyLighting();
		MultiBufferSource.BufferSource buffer = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
		matrices.pushPose();
		UIRenderHelper.flipForGuiRender(matrices);
		matrices.scale(16, 16, 16);
		float from = 3f / 16f;
		float to = 17f / 16f;
		FluidRenderer.renderFluidBox(fluids.get(0), from, from, from, to, to, to, buffer, matrices,
			LightTexture.FULL_BRIGHT, false);
		matrices.popPose();

		float width = 1 / 128f * squeeze;
		matrices.translate(scale / 2f, scale * 1.5f, scale / 2f);
		UIRenderHelper.flipForGuiRender(matrices);
		matrices.scale(16, 16, 16);
		matrices.translate(-0.5f, 0, -0.5f);
		from = -width / 2 + 0.5f;
		to = width / 2 + 0.5f;
		FluidRenderer.renderFluidBox(fluids.get(0), from, 0, from, to, 2, to, buffer, matrices,
			LightTexture.FULL_BRIGHT, false);
		buffer.endBatch();
		Lighting.setupFor3DItems();
	}

	public static void addDrain(WidgetHolder widgets, int x, int y, FluidStack fluid) {
		widgets.addDrawable(x, y, 0, 0, (matrices, mouseX, mouseY, delta) -> {
			matrices.translate(0, 0, 100);
			matrices.mulPose(Vector3f.XP.rotationDegrees(-15.5f));
			matrices.mulPose(Vector3f.YP.rotationDegrees(22.5f));
			int scale = 20;

			blockElement(AllBlocks.ITEM_DRAIN.getDefaultState())
				.scale(scale)
				.render(matrices);

			MultiBufferSource.BufferSource buffer = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
			//MatrixStack ms = new MatrixStack();
			UIRenderHelper.flipForGuiRender(matrices);
			matrices.scale(scale, scale, scale);
			float from = 2 / 16f;
			float to = 1f - from;
			FluidRenderer.renderFluidBox(fluid, from, from, from, to, 3/4f, to, buffer, matrices,
				LightTexture.FULL_BRIGHT, false);
			buffer.endBatch();
		});
	}

	public static void addCrafter(WidgetHolder widgets, int x, int y) {
		widgets.addDrawable(x, y, 0, 0, (matrices, mouseX, mouseY, delta) -> {
			matrices.mulPose(Vector3f.XP.rotationDegrees(-15.5f));
			matrices.mulPose(Vector3f.YP.rotationDegrees(-22.5f));
			int scale = 22;
	
			blockElement(cogwheel())
				.rotateBlock(90, 0, getCurrentAngle())
				.scale(scale)
				.render(matrices);
	
			blockElement(AllBlocks.MECHANICAL_CRAFTER.getDefaultState())
				.rotateBlock(0, 180, 0)
				.scale(scale)
				.render(matrices);
		});
	}
}