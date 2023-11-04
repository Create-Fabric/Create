package com.simibubi.create.api.event;

import net.fabricmc.fabric.api.event.Event;

import net.fabricmc.fabric.api.event.EventFactory;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;

/**
 * This Event is fired when two fluids meet in a pipe ({@link Flow})<br>
 * or when a fluid in a pipe meets with a fluid in the world
 * ({@link Spill}).<br>
 * <br>
 * If it is not null, the event's BlockState will be placed in world after
 * firing.
 */
public class PipeCollisionEvent {

	public static final Event<FlowCallback> FLOW = EventFactory.createArrayBacked(FlowCallback.class, callbacks -> event -> {
		for (FlowCallback callback : callbacks)
			callback.handleFlow(event);
	});

	public static final Event<SpillCallback> SPILL = EventFactory.createArrayBacked(SpillCallback.class, callbacks -> event -> {
		for (SpillCallback callback : callbacks)
			callback.handleSpill(event);
	});

	private final Level level;
	private final BlockPos pos;
	protected final Fluid firstFluid, secondFluid;

	@Nullable
	private BlockState state;

	protected PipeCollisionEvent(Level level, BlockPos pos, Fluid firstFluid, Fluid secondFluid,
		@Nullable BlockState defaultState) {
		this.level = level;
		this.pos = pos;
		this.firstFluid = firstFluid;
		this.secondFluid = secondFluid;
		this.state = defaultState;
	}

	public Level getLevel() {
		return level;
	}

	public BlockPos getPos() {
		return pos;
	}

	@Nullable
	public BlockState getState() {
		return state;
	}

	public void setState(@Nullable BlockState state) {
		this.state = state;
	}

	public static class Flow extends PipeCollisionEvent {

		public Flow(Level level, BlockPos pos, Fluid firstFluid, Fluid secondFluid, @Nullable BlockState defaultState) {
			super(level, pos, firstFluid, secondFluid, defaultState);
		}

		public Fluid getFirstFluid() {
			return firstFluid;
		}

		public Fluid getSecondFluid() {
			return secondFluid;
		}
	}

	public static class Spill extends PipeCollisionEvent {

		public Spill(Level level, BlockPos pos, Fluid worldFluid, Fluid pipeFluid, @Nullable BlockState defaultState) {
			super(level, pos, worldFluid, pipeFluid, defaultState);
		}

		public Fluid getWorldFluid() {
			return firstFluid;
		}

		public Fluid getPipeFluid() {
			return secondFluid;
		}
	}

	@FunctionalInterface
	public interface FlowCallback {
		void handleFlow(Flow event);
	}

	@FunctionalInterface
	public interface SpillCallback {
		void handleSpill(Spill event);
	}
}
