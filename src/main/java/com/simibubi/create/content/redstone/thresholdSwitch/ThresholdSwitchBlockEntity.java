package com.simibubi.create.content.redstone.thresholdSwitch;

import java.util.List;

import com.simibubi.create.content.redstone.DirectedDirectionalBlock;
import com.simibubi.create.content.redstone.FilteredDetectorFilterSlot;
import com.simibubi.create.content.redstone.displayLink.DisplayLinkBlock;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.inventory.CapManipulationBehaviourBase.InterfaceProvider;
import com.simibubi.create.foundation.blockEntity.behaviour.inventory.InvManipulationBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.inventory.TankManipulationBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.inventory.VersionedInventoryTrackerBehaviour;
import com.simibubi.create.foundation.utility.BlockFace;

import io.github.fabricators_of_create.porting_lib.transfer.TransferUtil;
import io.github.fabricators_of_create.porting_lib.fluids.FluidStack;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.ticks.TickPriority;

public class ThresholdSwitchBlockEntity extends SmartBlockEntity {

	public float onWhenAbove;
	public float offWhenBelow;
	public float currentLevel;
	private boolean redstoneState;
	private boolean inverted;
	private boolean poweredAfterDelay;

	private FilteringBehaviour filtering;
	private InvManipulationBehaviour observedInventory;
	private TankManipulationBehaviour observedTank;
	private VersionedInventoryTrackerBehaviour invVersionTracker;

	public ThresholdSwitchBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
		onWhenAbove = .75f;
		offWhenBelow = .25f;
		currentLevel = -1;
		redstoneState = false;
		inverted = false;
		poweredAfterDelay = false;
		setLazyTickRate(10);
	}

	@Override
	protected void read(CompoundTag compound, boolean clientPacket) {
		onWhenAbove = compound.getFloat("OnAbove");
		offWhenBelow = compound.getFloat("OffBelow");
		currentLevel = compound.getFloat("Current");
		redstoneState = compound.getBoolean("Powered");
		inverted = compound.getBoolean("Inverted");
		poweredAfterDelay = compound.getBoolean("PoweredAfterDelay");
		super.read(compound, clientPacket);
	}

	protected void writeCommon(CompoundTag compound) {
		compound.putFloat("OnAbove", onWhenAbove);
		compound.putFloat("OffBelow", offWhenBelow);
		compound.putBoolean("Inverted", inverted);
	}

	@Override
	public void write(CompoundTag compound, boolean clientPacket) {
		writeCommon(compound);
		compound.putFloat("Current", currentLevel);
		compound.putBoolean("Powered", redstoneState);
		compound.putBoolean("PoweredAfterDelay", poweredAfterDelay);
		super.write(compound, clientPacket);
	}

	@Override
	public void writeSafe(CompoundTag compound) {
		writeCommon(compound);
		super.writeSafe(compound);
	}

	public float getStockLevel() {
		return currentLevel;
	}

	public void updateCurrentLevel() {
		if (Transaction.isOpen()) {
			// can't do this during close callbacks.
			// lazyTick should catch any updates we miss.
			return;
		}
		boolean changed = false;
		float occupied = 0;
		float totalSpace = 0;
		float prevLevel = currentLevel;

		BlockPos target = worldPosition.relative(ThresholdSwitchBlock.getTargetDirection(getBlockState()));
		BlockEntity targetBlockEntity = level.getBlockEntity(target);

		if (targetBlockEntity instanceof ThresholdSwitchObservable observable) {
			currentLevel = observable.getPercent() / 100f;

//		} else if (StorageDrawers.isDrawer(targetBlockEntity) && observedInventory.hasInventory()) {
//			currentLevel = StorageDrawers.getTrueFillLevel(observedInventory.getInventory(), filtering);

		} else if (observedInventory.hasInventory() || observedTank.hasInventory()) {
			if (observedInventory.hasInventory()) {

				// Item inventory
				Storage<ItemVariant> inv = observedInventory.getInventory();
				if (invVersionTracker.stillWaiting(inv)) {
					occupied = prevLevel;
					totalSpace = 1f;

				} else {
					invVersionTracker.awaitNewVersion(inv);
					if (invVersionTracker.stillWaiting(inv)) {
						occupied = prevLevel;
						totalSpace = 1f;

					} else {
						invVersionTracker.awaitNewVersion(inv);
						for (StorageView<ItemVariant> view : inv) {
							long space = view.getCapacity();
							long count = view.getAmount();
							if (space == 0)
								continue;

							totalSpace += 1;
							if (filtering.test(view.getResource().toStack()))
								occupied += count * (1f / space);
						}
					}
				}
			}

			if (observedTank.hasInventory()) {
				// Fluid inventory
				try (Transaction t = TransferUtil.getTransaction()) {
					Storage<FluidVariant> tank = observedTank.getInventory();
					for (StorageView<FluidVariant> view : tank) {
						long space = view.getCapacity();
						long count = view.getAmount();
						if (space == 0)
							continue;

						totalSpace += 1;
						if (filtering.test(new FluidStack(view)))
							occupied += count * (1f / space);
					}
				}
			}

			currentLevel = occupied / totalSpace;

			// fabric: since fluid amounts are 81x larger, we lose floating point precision. Let's just round a little.
			if (currentLevel > 0.999) {
				currentLevel = 1;
			} else if (currentLevel < 0.001) {
				currentLevel = 0;
			}

		} else {
			// No compatible inventories found
			if (currentLevel == -1)
				return;
			level.setBlock(worldPosition, getBlockState().setValue(ThresholdSwitchBlock.LEVEL, 0), 3);
			currentLevel = -1;
			redstoneState = false;
			sendData();
			scheduleBlockTick();
			return;
		}

		currentLevel = Mth.clamp(currentLevel, 0, 1);
		changed = currentLevel != prevLevel;

		boolean previouslyPowered = redstoneState;
		if (redstoneState && currentLevel <= offWhenBelow)
			redstoneState = false;
		else if (!redstoneState && currentLevel >= onWhenAbove)
			redstoneState = true;
		boolean update = previouslyPowered != redstoneState;

		int displayLevel = 0;
		if (currentLevel > 0)
			displayLevel = (int) (1 + currentLevel * 4);
		level.setBlock(worldPosition, getBlockState().setValue(ThresholdSwitchBlock.LEVEL, displayLevel),
			update ? 3 : 2);

		if (update)
			scheduleBlockTick();

		if (changed || update) {
			DisplayLinkBlock.notifyGatherers(level, worldPosition);
			notifyUpdate();
		}
	}

	protected void scheduleBlockTick() {
		Block block = getBlockState().getBlock();
		if (!level.getBlockTicks()
			.willTickThisTick(worldPosition, block))
			level.scheduleTick(worldPosition, block, 2, TickPriority.NORMAL);
	}

	@Override
	public void lazyTick() {
		super.lazyTick();
		if (level.isClientSide)
			return;
		updateCurrentLevel();
	}

	@Override
	public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
		behaviours.add(filtering = new FilteringBehaviour(this, new FilteredDetectorFilterSlot(true))
			.withCallback($ -> {
				this.updateCurrentLevel();
				invVersionTracker.reset();
			}));

		behaviours.add(invVersionTracker = new VersionedInventoryTrackerBehaviour(this));

		InterfaceProvider towardBlockFacing =
			(w, p, s) -> new BlockFace(p, DirectedDirectionalBlock.getTargetDirection(s));

		behaviours.add(observedInventory = new InvManipulationBehaviour(this, towardBlockFacing).bypassSidedness());
		behaviours.add(observedTank = new TankManipulationBehaviour(this, towardBlockFacing).bypassSidedness());
	}

	public float getLevelForDisplay() {
		return currentLevel == -1 ? 0 : currentLevel;
	}

	public boolean getState() {
		return redstoneState;
	}

	public boolean shouldBePowered() {
		return inverted != redstoneState;
	}

	public void updatePowerAfterDelay() {
		poweredAfterDelay = shouldBePowered();
		level.blockUpdated(worldPosition, getBlockState().getBlock());
	}

	public boolean isPowered() {
		return poweredAfterDelay;
	}

	public boolean isInverted() {
		return inverted;
	}

	public void setInverted(boolean inverted) {
		if (inverted == this.inverted)
			return;
		this.inverted = inverted;
		updatePowerAfterDelay();
	}
}
