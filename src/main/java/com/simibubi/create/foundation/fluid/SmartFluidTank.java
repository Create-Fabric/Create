package com.simibubi.create.foundation.fluid;

import java.util.function.Consumer;

import io.github.fabricators_of_create.porting_lib.util.FluidStack;
import io.github.fabricators_of_create.porting_lib.transfer.fluid.FluidTank;

public class SmartFluidTank extends FluidTank {

	private Consumer<FluidStack> updateCallback;

	public SmartFluidTank(long capacity, Consumer<FluidStack> updateCallback) {
		super(capacity);
		this.updateCallback = updateCallback;
	}

	@Override
	protected void onContentsChanged() {
		super.onContentsChanged();
		updateCallback.accept(getFluid());
	}

	@Override
	public void setFluid(FluidStack stack) {
		super.setFluid(stack);
		updateCallback.accept(stack);
	}
}
