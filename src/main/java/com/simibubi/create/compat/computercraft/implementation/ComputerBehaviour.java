package com.simibubi.create.compat.computercraft.implementation;

import com.simibubi.create.AllBlockEntityTypes;
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

import dan200.computercraft.api.peripheral.PeripheralLookup;

public class ComputerBehaviour extends AbstractComputerBehaviour {

	public ComputerBehaviour(SmartBlockEntity te) {
		super(te);
		registerPeripherals();
	}

	public static void registerPeripherals() {
		PeripheralLookup.get().registerForBlockEntities(
				(blockEntity, context) -> {
					if (blockEntity instanceof SpeedControllerBlockEntity scbe)
						return new SpeedControllerPeripheral(scbe, scbe.targetSpeed);
					return null;
				},
				AllBlockEntityTypes.ROTATION_SPEED_CONTROLLER.get());

		PeripheralLookup.get().registerForBlockEntities(
				(blockEntity, context) -> {
					if (blockEntity instanceof DisplayLinkBlockEntity dlbe)
						return new DisplayLinkPeripheral(dlbe);
					return null;
				},
				AllBlockEntityTypes.DISPLAY_LINK.get());

		PeripheralLookup.get().registerForBlockEntities(
				(blockEntity, context) -> {
					if (blockEntity instanceof DisplayLinkBlockEntity dlbe)
						return new DisplayLinkPeripheral(dlbe);
					return null;
				},
				AllBlockEntityTypes.DISPLAY_LINK.get());

		PeripheralLookup.get().registerForBlockEntities(
				(blockEntity, context) -> {
					if (blockEntity instanceof SequencedGearshiftBlockEntity sgbe)
						return new SequencedGearshiftPeripheral(sgbe);
					return null;
				},
				AllBlockEntityTypes.SEQUENCED_GEARSHIFT.get());

		PeripheralLookup.get().registerForBlockEntities(
				(blockEntity, context) -> {
					if (blockEntity instanceof SpeedGaugeBlockEntity sgbe)
						return new SpeedGaugePeripheral(sgbe);
					return null;
				},
				AllBlockEntityTypes.SPEEDOMETER.get());

		PeripheralLookup.get().registerForBlockEntities(
				(blockEntity, context) -> {
					if (blockEntity instanceof StressGaugeBlockEntity sgbe)
						return new StressGaugePeripheral(sgbe);
					return null;
				},
				AllBlockEntityTypes.STRESSOMETER.get());

		PeripheralLookup.get().registerForBlockEntities(
				(blockEntity, context) -> {
					if (blockEntity instanceof StationBlockEntity sbe)
						return new StationPeripheral(sbe);
					return null;
				},
				AllBlockEntityTypes.TRACK_STATION.get());
	}
}
