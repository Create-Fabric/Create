package com.simibubi.create.foundation.fluid;

import com.simibubi.create.foundation.utility.Iterate;
import io.github.fabricators_of_create.porting_lib.util.FluidStack;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.CombinedStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

import java.util.List;

/**
 * Combines multiple IFluidHandlers into one interface (See CombinedInvWrapper
 * for items)
 */
public class CombinedTankWrapper extends CombinedStorage<FluidVariant, Storage<FluidVariant>> {
	protected boolean enforceVariety;

	public CombinedTankWrapper(Storage<FluidVariant>... fluidHandlers) {
		super(List.of(fluidHandlers));
	}

	public CombinedTankWrapper enforceVariety() {
		enforceVariety = true;
		return this;
	}

//	@Override
//	public int getTanks() {
//		return tankCount;
//	}

//	@Override
//	public FluidStack getFluidInTank(int tank) {
//		int index = getIndexForSlot(tank);
//		IFluidHandler handler = getHandlerFromIndex(index);
//		tank = getSlotFromIndex(tank, index);
//		return handler.getFluidInTank(tank);
//	}

//	@Override
//	public long getTankCapacity(int tank) {
//		int index = getIndexForSlot(tank);
//		IFluidHandler handler = getHandlerFromIndex(index);
//		int localSlot = getSlotFromIndex(tank, index);
//		return handler.getTankCapacity(localSlot);
//	}

//	@Override
//	public boolean isFluidValid(int tank, FluidStack stack) {
//		int index = getIndexForSlot(tank);
//		IFluidHandler handler = getHandlerFromIndex(index);
//		int localSlot = getSlotFromIndex(tank, index);
//		return handler.isFluidValid(localSlot, stack);
//	}


	@Override
	public long insert(FluidVariant resource, long maxAmount, TransactionContext transaction) {
		return super.insert(resource, maxAmount, transaction);
	}

	@Override
	public long fill(FluidStack resource, boolean sim) {
		if (resource.isEmpty())
			return 0;

		int filled = 0;
		resource = resource.copy();

		boolean fittingHandlerFound = false;
		Outer: for (boolean searchPass : Iterate.trueAndFalse) {
			for (IFluidHandler iFluidHandler : itemHandler) {

				for (int i = 0; i < iFluidHandler.getTanks(); i++)
					if (searchPass && iFluidHandler.getFluidInTank(i)
						.isFluidEqual(resource))
						fittingHandlerFound = true;

				if (searchPass && !fittingHandlerFound)
					continue;

				long filledIntoCurrent = iFluidHandler.fill(resource, sim);
				resource.shrink(filledIntoCurrent);
				filled += filledIntoCurrent;

				if (resource.isEmpty() || fittingHandlerFound || enforceVariety && filledIntoCurrent != 0)
					break Outer;
			}
		}

		return filled;
	}
}
