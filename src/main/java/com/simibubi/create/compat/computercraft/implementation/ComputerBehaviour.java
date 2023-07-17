package com.simibubi.create.compat.computercraft.implementation;

import com.mojang.datafixers.types.Func;
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

import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.peripheral.PeripheralLookup;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.function.Function;

public class ComputerBehaviour extends AbstractComputerBehaviour {

	public ComputerBehaviour(SmartBlockEntity te) {
		super(te);
		registerPeripherals();
	}

	public static void registerPeripherals() {
		registerPeripheral(SpeedControllerBlockEntity.class, AllBlockEntityTypes.ROTATION_SPEED_CONTROLLER.get(), (speedControllerBlockEntity -> new SpeedControllerPeripheral(speedControllerBlockEntity, speedControllerBlockEntity.targetSpeed)));

		registerPeripheral(DisplayLinkBlockEntity.class, AllBlockEntityTypes.DISPLAY_LINK.get(), DisplayLinkPeripheral::new);

		registerPeripheral(DisplayLinkBlockEntity.class, AllBlockEntityTypes.DISPLAY_LINK.get(), DisplayLinkPeripheral::new);

		registerPeripheral(SequencedGearshiftBlockEntity.class, AllBlockEntityTypes.SEQUENCED_GEARSHIFT.get(), SequencedGearshiftPeripheral::new);

		registerPeripheral(SpeedGaugeBlockEntity.class, AllBlockEntityTypes.SPEEDOMETER.get(), SpeedGaugePeripheral::new);

		registerPeripheral(StressGaugeBlockEntity.class, AllBlockEntityTypes.STRESSOMETER.get(), StressGaugePeripheral::new);

		registerPeripheral(StationBlockEntity.class, AllBlockEntityTypes.TRACK_STATION.get(), StationPeripheral::new);
	}

	public static <T extends BlockEntity> void registerPeripheral(Class<T> blockEnityClass, BlockEntityType<T> type, Function<T, IPeripheral> provider) {
		PeripheralLookup.get().registerForBlockEntities(
				(blockEntity, context) -> {
					if (blockEnityClass.isInstance(blockEntity)) {
						return provider.apply((T) blockEntity);
					}
					return null;
				},
				type);
	}

}
