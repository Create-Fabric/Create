package com.simibubi.create.content.kinetics.belt.behaviour;

import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.simibubi.create.content.kinetics.belt.transport.TransportedItemStack;
import com.simibubi.create.content.logistics.funnel.BeltFunnelBlock;
import com.simibubi.create.content.logistics.funnel.BeltFunnelBlock.Shape;
import com.simibubi.create.content.logistics.funnel.FunnelBlock;
import com.simibubi.create.content.logistics.funnel.FunnelBlockEntity;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BehaviourType;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;

import io.github.fabricators_of_create.porting_lib.transfer.TransferUtil;
import io.github.fabricators_of_create.porting_lib.util.StorageProvider;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Behaviour for BlockEntities to which belts can transfer items directly in a
 * backup-friendly manner. Example uses: Basin, Saw, Depot
 */
public class DirectBeltInputBehaviour extends BlockEntityBehaviour {

	public static final BehaviourType<DirectBeltInputBehaviour> TYPE = new BehaviourType<>();

	private InsertionCallback tryInsert;
	private OccupiedPredicate isOccupied;
	private AvailabilityPredicate canInsert;
	private Supplier<Boolean> supportsBeltFunnels;
	// fabric: transfer
	private StorageProvider<ItemVariant> targetStorageProvider;

	public DirectBeltInputBehaviour(SmartBlockEntity be) {
		super(be);
		tryInsert = this::defaultInsertionCallback;
		canInsert = d -> true;
		isOccupied = d -> false;
		supportsBeltFunnels = () -> false;
	}

	public DirectBeltInputBehaviour allowingBeltFunnelsWhen(Supplier<Boolean> pred) {
		supportsBeltFunnels = pred;
		return this;
	}

	public DirectBeltInputBehaviour allowingBeltFunnels() {
		supportsBeltFunnels = () -> true;
		return this;
	}

	public DirectBeltInputBehaviour onlyInsertWhen(AvailabilityPredicate pred) {
		canInsert = pred;
		return this;
	}
	
	public DirectBeltInputBehaviour considerOccupiedWhen(OccupiedPredicate pred) {
		isOccupied = pred;
		return this;
	}

	public DirectBeltInputBehaviour setInsertionHandler(InsertionCallback callback) {
		tryInsert = callback;
		return this;
	}

	private ItemStack defaultInsertionCallback(TransportedItemStack inserted, Direction side, boolean simulate) {
		Storage<ItemVariant> storage = getTargetStorage(side);
		if (storage == null)
			return inserted.stack;

		try (Transaction t = TransferUtil.getTransaction()) {
			long trying = inserted.stack.getCount();
			long successful = storage.insert(ItemVariant.of(inserted.stack), inserted.stack.getCount(), t);
			if (trying == successful) {
				if (!simulate) t.commit();
				return ItemStack.EMPTY;
			}
			ItemStack stack = inserted.stack.copy();
			stack.setCount((int) (trying - successful));
			if (!simulate) t.commit();
			return stack;
		}
	}

	public Storage<ItemVariant> getTargetStorage(Direction side) {
		if (getWorld() == null)
			return null;
		if (targetStorageProvider == null)
			targetStorageProvider = StorageProvider.createForItems(getWorld(), getPos());
		return targetStorageProvider.get(side);
	}

	// TODO: verify that this side is consistent across all calls
	public boolean canInsertFromSide(Direction side) {
		return canInsert.test(side);
	}

	public boolean isOccupied(Direction side) {
		return isOccupied.test(side);
	}
	
	public ItemStack handleInsertion(ItemStack stack, Direction side, boolean simulate) {
		return handleInsertion(new TransportedItemStack(stack), side, simulate);
	}

	public ItemStack handleInsertion(TransportedItemStack stack, Direction side, boolean simulate) {
		return tryInsert.apply(stack, side, simulate);
	}

	@Override
	public BehaviourType<?> getType() {
		return TYPE;
	}

	@FunctionalInterface
	public interface InsertionCallback {
		public ItemStack apply(TransportedItemStack stack, Direction side, boolean simulate);
	}

	@FunctionalInterface
	public interface OccupiedPredicate {
		public boolean test(Direction side);
	}
	
	@FunctionalInterface
	public interface AvailabilityPredicate {
		public boolean test(Direction side);
	}

	@Nullable
	public ItemStack tryExportingToBeltFunnel(ItemStack stack, @Nullable Direction side, boolean simulate) {
		BlockPos funnelPos = blockEntity.getBlockPos()
			.above();
		Level world = getWorld();
		BlockState funnelState = world.getBlockState(funnelPos);
		if (!(funnelState.getBlock() instanceof BeltFunnelBlock))
			return null;
		if (funnelState.getValue(BeltFunnelBlock.SHAPE) != Shape.PULLING)
			return null;
		if (side != null && FunnelBlock.getFunnelFacing(funnelState) != side)
			return null;
		BlockEntity be = world.getBlockEntity(funnelPos);
		if (!(be instanceof FunnelBlockEntity))
			return null;
		if (funnelState.getValue(BeltFunnelBlock.POWERED))
			return stack;
		ItemStack insert = FunnelBlock.tryInsert(world, funnelPos, stack, simulate);
		if (insert.getCount() != stack.getCount() && !simulate)
			((FunnelBlockEntity) be).flap(true);
		return insert;
	}

	public boolean canSupportBeltFunnels() {
		return supportsBeltFunnels.get();
	}

}
