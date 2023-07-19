package com.simibubi.create.compat.computercraft.implementation;

import com.simibubi.create.compat.computercraft.AbstractComputerBehaviour;
import com.simibubi.create.compat.computercraft.implementation.peripherals.DisplayLinkPeripheral;
import com.simibubi.create.compat.computercraft.implementation.peripherals.SequencedGearshiftPeripheral;
import com.simibubi.create.compat.computercraft.implementation.peripherals.SpeedControllerPeripheral;
import com.simibubi.create.compat.computercraft.implementation.peripherals.SpeedGaugePeripheral;
import com.simibubi.create.compat.computercraft.implementation.peripherals.StationPeripheral;
import com.simibubi.create.compat.computercraft.implementation.peripherals.StressGaugePeripheral;
import com.simibubi.create.content.kinetics.gauge.SpeedGaugeBlockEntity;
import com.simibubi.create.content.kinetics.gauge.StressGaugeBlockEntity;
import com.simibubi.create.content.kinetics.speedController.SpeedControllerBlockEntity;
import com.simibubi.create.content.kinetics.transmission.sequencer.SequencedGearshiftBlockEntity;
import com.simibubi.create.content.redstone.displayLink.DisplayLinkBlockEntity;
import com.simibubi.create.content.trains.station.StationBlockEntity;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;

import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.Nullable;

public class ComputerBehaviour extends AbstractComputerBehaviour {

	@Nullable
	public static IPeripheral peripheralProvider(Level level, BlockPos blockPos) {
		AbstractComputerBehaviour behavior = BlockEntityBehaviour.get(level, blockPos, AbstractComputerBehaviour.TYPE);
		if (behavior instanceof ComputerBehaviour real)
			return real.getPeripheral();
		return null;
	}

	IPeripheral peripheral;

	public ComputerBehaviour(SmartBlockEntity te) {
		super(te);
		this.peripheral = getPeripheralFor(te);
	}

	public static IPeripheral getPeripheralFor(SmartBlockEntity be) {
		if (be instanceof SpeedControllerBlockEntity scbe)
			return new SpeedControllerPeripheral(scbe, scbe.targetSpeed);
		if (be instanceof DisplayLinkBlockEntity dlbe)
			return new DisplayLinkPeripheral(dlbe);
		if (be instanceof SequencedGearshiftBlockEntity sgbe)
			return new SequencedGearshiftPeripheral(sgbe);
		if (be instanceof SpeedGaugeBlockEntity sgbe)
			return new SpeedGaugePeripheral(sgbe);
		if (be instanceof StressGaugeBlockEntity sgbe)
			return new StressGaugePeripheral(sgbe);
		if (be instanceof StationBlockEntity sbe)
			return new StationPeripheral(sbe);

		throw new IllegalArgumentException(
			"No peripheral available for " + BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(be.getType()));
	}

	@Override
	public <T> T getPeripheral() {
		//noinspection unchecked
		return (T) peripheral;
	}
}
