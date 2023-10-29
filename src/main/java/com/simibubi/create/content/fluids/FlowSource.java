package com.simibubi.create.content.fluids;

import java.lang.ref.WeakReference;
import java.util.function.Predicate;

import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.utility.BlockFace;

import io.github.fabricators_of_create.porting_lib.transfer.TransferUtil;
import io.github.fabricators_of_create.porting_lib.fluids.FluidStack;
import io.github.fabricators_of_create.porting_lib.util.StorageProvider;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.ResourceAmount;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.world.level.Level;

public abstract class FlowSource {

	BlockFace location;

	public FlowSource(BlockFace location) {
		this.location = location;
	}

	public FluidStack provideFluid(Predicate<FluidStack> extractionPredicate) {
		Storage<FluidVariant> tank = provideHandler();
		if (tank == null)
			return FluidStack.EMPTY;
		try (Transaction t = TransferUtil.getTransaction()) {
			Predicate<FluidVariant> test = v -> extractionPredicate.test(new FluidStack(v, 1));
			ResourceAmount<FluidVariant> resource = TransferUtil.extractMatching(tank, test, 1, t);
			return resource == null ? FluidStack.EMPTY : new FluidStack(resource.resource(), resource.amount());
		}
	}

	// Layer III. PFIs need active attention to prevent them from disengaging early
	public void keepAlive() {}

	public abstract boolean isEndpoint();

	public void manageSource(Level world) {}

	public void whileFlowPresent(Level world, boolean pulling) {}

	public Storage<FluidVariant> provideHandler() {
		return null;
	}

	public static class FluidHandler extends FlowSource {
		StorageProvider<FluidVariant> provider;
		private Level level;

		public FluidHandler(BlockFace location) {
			super(location);
			this.provider = null;
			this.level = null;
		}

		public void manageSource(Level world) {
			if (world != this.level) {
				this.level = world;
				this.provider = StorageProvider.createForFluids(world, location.getConnectedPos());
			}
		}

		@Override
		public Storage<FluidVariant> provideHandler() {
			return provider == null ? null : provider.get(location.getOppositeFace());
		}

		@Override
		public boolean isEndpoint() {
			return true;
		}
	}

	public static class OtherPipe extends FlowSource {
		WeakReference<FluidTransportBehaviour> cached;

		public OtherPipe(BlockFace location) {
			super(location);
		}

		@Override
		public void manageSource(Level world) {
			if (cached != null && cached.get() != null && !cached.get().blockEntity.isRemoved())
				return;
			cached = null;
			FluidTransportBehaviour fluidTransportBehaviour =
				BlockEntityBehaviour.get(world, location.getConnectedPos(), FluidTransportBehaviour.TYPE);
			if (fluidTransportBehaviour != null)
				cached = new WeakReference<>(fluidTransportBehaviour);
		}

		@Override
		public FluidStack provideFluid(Predicate<FluidStack> extractionPredicate) {
			if (cached == null || cached.get() == null)
				return FluidStack.EMPTY;
			FluidTransportBehaviour behaviour = cached.get();
			FluidStack providedOutwardFluid = behaviour.getProvidedOutwardFluid(location.getOppositeFace());
			return extractionPredicate.test(providedOutwardFluid) ? providedOutwardFluid : FluidStack.EMPTY;
		}

		@Override
		public boolean isEndpoint() {
			return false;
		}

	}

	public static class Blocked extends FlowSource {

		public Blocked(BlockFace location) {
			super(location);
		}

		@Override
		public boolean isEndpoint() {
			return false;
		}

	}

}
