package com.simibubi.create.content.contraptions.fluids.actors;

import java.util.Iterator;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.simibubi.create.foundation.fluid.FluidHelper;
import com.simibubi.create.foundation.fluid.SmartFluidTank;
import io.github.fabricators_of_create.porting_lib.util.FluidStack;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleViewIterator;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.BlockPos;

public class HosePulleyFluidHandler implements Storage<FluidVariant> {

	// The dynamic interface

	@Override
	public long insert(FluidVariant resource, long maxAmount, TransactionContext transaction) {
		if (!internalTank.isEmpty() && !internalTank.getFluid().canFill(resource))
			return 0;
		if (resource.isBlank() || !FluidHelper.hasBlockState(resource.getFluid()))
			return 0;

		long diff = maxAmount;
		long totalAmountAfterFill = diff + internalTank.getFluidAmount();

		if (predicate.get() && totalAmountAfterFill >= FluidConstants.BUCKET) {
			if (filler.tryDeposit(resource.getFluid(), rootPosGetter.get(), transaction)) {
				drainer.counterpartActed();
				diff -= FluidConstants.BUCKET;
			}
		}
		internalTank.updateSnapshots(transaction);
		if (diff <= 0) {
			internalTank.extract(resource, -diff, transaction);
			return maxAmount - diff;
		}

		return internalTank.insert(resource, diff, transaction);
	}

	@Override
	public long extract(FluidVariant resource, long maxAmount, TransactionContext transaction) {
		if (resource != null && !internalTank.isEmpty() && !internalTank.getFluid().canFill(resource))
			return 0;
		internalTank.updateSnapshots(transaction);
		if (internalTank.getFluidAmount() >= FluidConstants.BUCKET)
			return internalTank.extract(resource, maxAmount, transaction);
		BlockPos pos = rootPosGetter.get();
		FluidStack returned = drainer.getDrainableFluid(pos);
		if (!predicate.get() || !drainer.pullNext(pos, transaction))
			return internalTank.extract(resource, maxAmount, transaction);

		filler.counterpartActed();
		FluidStack leftover = returned.copy();
		long available = FluidConstants.BUCKET + internalTank.getFluidAmount();
		long drained;

		if (!internalTank.isEmpty() && !internalTank.getFluid()
				.isFluidEqual(returned) || returned.isEmpty())
			return internalTank.extract(resource, maxAmount, transaction);

		if (resource != null && !returned.canFill(resource))
			return 0;

		drained = Math.min(maxAmount, available);
		returned.setAmount(drained);
		leftover.setAmount(available - drained);
		if (!leftover.isEmpty())
			internalTank.setFluid(leftover);
		return returned.getAmount();
	}

	@Override
	public Iterator<StorageView<FluidVariant>> iterator(TransactionContext transaction) {
		return SingleViewIterator.create(internalTank, transaction);
	}

	//

	private SmartFluidTank internalTank;
	private FluidFillingBehaviour filler;
	private FluidDrainingBehaviour drainer;
	private Supplier<BlockPos> rootPosGetter;
	private Supplier<Boolean> predicate;

	public HosePulleyFluidHandler(SmartFluidTank internalTank, FluidFillingBehaviour filler,
		FluidDrainingBehaviour drainer, Supplier<BlockPos> rootPosGetter, Supplier<Boolean> predicate) {
		this.internalTank = internalTank;
		this.filler = filler;
		this.drainer = drainer;
		this.rootPosGetter = rootPosGetter;
		this.predicate = predicate;
	}
}
