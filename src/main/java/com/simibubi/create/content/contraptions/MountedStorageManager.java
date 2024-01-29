package com.simibubi.create.content.contraptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.simibubi.create.content.contraptions.Contraption.ContraptionInvWrapper;
import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import com.simibubi.create.foundation.fluid.CombinedTankWrapper;
import com.simibubi.create.foundation.utility.Components;
import com.simibubi.create.foundation.utility.NBTHelper;

import io.github.fabricators_of_create.porting_lib.transfer.TransferUtil;
import io.github.fabricators_of_create.porting_lib.transfer.fluid.FluidTank;
import io.github.fabricators_of_create.porting_lib.transfer.item.ItemStackHandler;
import io.github.fabricators_of_create.porting_lib.fluids.FluidStack;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraft.world.phys.Vec3;

public class MountedStorageManager {

	protected ContraptionInvWrapper inventory;
	protected ContraptionInvWrapper fuelInventory;
	protected CombinedTankWrapper fluidInventory;
	protected Map<BlockPos, MountedStorage> storage;
	protected Map<BlockPos, MountedFluidStorage> fluidStorage;

	public MountedStorageManager() {
		storage = new HashMap<>();
		fluidStorage = new HashMap<>();
	}

	public void entityTick(AbstractContraptionEntity entity) {
		fluidStorage.forEach((pos, mfs) -> mfs.tick(entity, pos, entity.level().isClientSide));
	}

	public void createHandlers() {
		Collection<MountedStorage> itemHandlers = storage.values();

		inventory = wrapItems(itemHandlers.stream()
			.filter(MountedStorage::isValid)
			.map(MountedStorage::getItemHandler)
			.toList(), false);

		fuelInventory = wrapItems(itemHandlers.stream()
			.filter(MountedStorage::canUseForFuel)
			.map(MountedStorage::getItemHandler)
			.toList(), true);

		fluidInventory = wrapFluids(fluidStorage.values()
			.stream()
			.map(MountedFluidStorage::getFluidHandler)
			.collect(Collectors.toList()));
	}

	protected ContraptionInvWrapper wrapItems(Collection<? extends Storage<ItemVariant>> list, boolean fuel) {
		return new ContraptionInvWrapper(Arrays.copyOf(list.toArray(), list.size(), Storage[].class));
	}

	protected CombinedTankWrapper wrapFluids(Collection<? extends Storage<FluidVariant>> list) {
		return new CombinedTankWrapper(Arrays.copyOf(list.toArray(), list.size(), Storage[].class));
	}

	public void addBlock(BlockPos localPos, BlockEntity be) {
		if (be != null && MountedStorage.canUseAsStorage(be))
			storage.put(localPos, new MountedStorage(be));
		if (be != null && MountedFluidStorage.canUseAsStorage(be))
			fluidStorage.put(localPos, new MountedFluidStorage(be));
	}

	public void read(CompoundTag nbt, Map<BlockPos, BlockEntity> presentBlockEntities, boolean clientPacket) {
		storage.clear();
		NBTHelper.iterateCompoundList(nbt.getList("Storage", Tag.TAG_COMPOUND), c -> storage
			.put(NbtUtils.readBlockPos(c.getCompound("Pos")), MountedStorage.deserialize(c.getCompound("Data"))));

		fluidStorage.clear();
		NBTHelper.iterateCompoundList(nbt.getList("FluidStorage", Tag.TAG_COMPOUND), c -> fluidStorage
			.put(NbtUtils.readBlockPos(c.getCompound("Pos")), MountedFluidStorage.deserialize(c.getCompound("Data"))));

		if (clientPacket && presentBlockEntities != null)
			bindTanks(presentBlockEntities);

		List<Storage<ItemVariant>> handlers = new ArrayList<>();
		List<Storage<ItemVariant>> fuelHandlers = new ArrayList<>();
		for (MountedStorage mountedStorage : storage.values()) {
			Storage<ItemVariant> itemHandler = mountedStorage.getItemHandler();
			handlers.add(itemHandler);
			if (mountedStorage.canUseForFuel())
				fuelHandlers.add(itemHandler);
		}

		inventory = wrapItems(handlers, false);
		fuelInventory = wrapItems(fuelHandlers, true);
		fluidInventory = wrapFluids(fluidStorage.values()
			.stream()
			.map(MountedFluidStorage::getFluidHandler)
			.map(tank -> (Storage<FluidVariant>) tank)
			.toList());
	}

	public void bindTanks(Map<BlockPos, BlockEntity> presentBlockEntities) {
		fluidStorage.forEach((pos, mfs) -> {
			BlockEntity blockEntity = presentBlockEntities.get(pos);
			if (!(blockEntity instanceof FluidTankBlockEntity))
				return;
			FluidTankBlockEntity tank = (FluidTankBlockEntity) blockEntity;
			FluidTank tankInventory = tank.getTankInventory();
			if (tankInventory instanceof FluidTank)
				((FluidTank) tankInventory).setFluid(mfs.tank.getFluid());
			tank.getFluidLevel()
				.startWithValue(tank.getFillState());
			mfs.assignBlockEntity(tank);
		});
	}

	public void write(CompoundTag nbt, boolean clientPacket) {
		ListTag storageNBT = new ListTag();
		if (!clientPacket)
			for (BlockPos pos : storage.keySet()) {
				CompoundTag c = new CompoundTag();
				MountedStorage mountedStorage = storage.get(pos);
				if (!mountedStorage.isValid())
					continue;
				c.put("Pos", NbtUtils.writeBlockPos(pos));
				c.put("Data", mountedStorage.serialize());
				storageNBT.add(c);
			}

		ListTag fluidStorageNBT = new ListTag();
		for (BlockPos pos : fluidStorage.keySet()) {
			CompoundTag c = new CompoundTag();
			MountedFluidStorage mountedStorage = fluidStorage.get(pos);
			if (!mountedStorage.isValid())
				continue;
			c.put("Pos", NbtUtils.writeBlockPos(pos));
			c.put("Data", mountedStorage.serialize());
			fluidStorageNBT.add(c);
		}

		nbt.put("Storage", storageNBT);
		nbt.put("FluidStorage", fluidStorageNBT);
	}

	public void removeStorageFromWorld() {
		storage.values()
			.forEach(MountedStorage::removeStorageFromWorld);
		fluidStorage.values()
			.forEach(MountedFluidStorage::removeStorageFromWorld);
	}

	public void addStorageToWorld(StructureBlockInfo block, BlockEntity blockEntity) {
		if (storage.containsKey(block.pos())) {
			MountedStorage mountedStorage = storage.get(block.pos());
			if (mountedStorage.isValid())
				mountedStorage.addStorageToWorld(blockEntity);
		}

		if (fluidStorage.containsKey(block.pos())) {
			MountedFluidStorage mountedStorage = fluidStorage.get(block.pos());
			if (mountedStorage.isValid())
				mountedStorage.addStorageToWorld(blockEntity);
		}
	}

	public void clear() {
		for (Storage<ItemVariant> storage : inventory.parts) {
			if (!(storage instanceof ContraptionInvWrapper wrapper) || !wrapper.isExternal) {
				TransferUtil.clearStorage(storage);
			}
		}
		TransferUtil.clearStorage(fluidInventory);
	}

	public void updateContainedFluid(BlockPos localPos, FluidStack containedFluid) {
		MountedFluidStorage mountedFluidStorage = fluidStorage.get(localPos);
		if (mountedFluidStorage != null)
			mountedFluidStorage.updateFluid(containedFluid);
	}

	public void attachExternal(Storage<ItemVariant> externalStorage) {
		inventory = new ContraptionInvWrapper(externalStorage, inventory);
		fuelInventory = new ContraptionInvWrapper(externalStorage, fuelInventory);
	}

	public ContraptionInvWrapper getItems() {
		return inventory;
	}

	public ContraptionInvWrapper getFuelItems() {
		return fuelInventory;
	}

	public CombinedTankWrapper getFluids() {
		return fluidInventory;
	}

	public boolean handlePlayerStorageInteraction(Contraption contraption, Player player, BlockPos localPos) {
		if (player.level().isClientSide()) {
			BlockEntity localBE = contraption.presentBlockEntities.get(localPos);
			return MountedStorage.canUseAsStorage(localBE);
		}

		MountedStorageManager storageManager = contraption.getStorageForSpawnPacket();
		MountedStorage storage = storageManager.storage.get(localPos);
		if (storage == null || storage.getItemHandler() == null)
			return false;
		ItemStackHandler primary = storage.getItemHandler();
		ItemStackHandler secondary = null;

		StructureBlockInfo info = contraption.getBlocks()
			.get(localPos);
		if (info != null && info.state().hasProperty(ChestBlock.TYPE)) {
			ChestType chestType = info.state().getValue(ChestBlock.TYPE);
			Direction facing = info.state().getOptionalValue(ChestBlock.FACING)
				.orElse(Direction.SOUTH);
			Direction connectedDirection =
				chestType == ChestType.LEFT ? facing.getClockWise() : facing.getCounterClockWise();

			if (chestType != ChestType.SINGLE) {
				MountedStorage storage2 = storageManager.storage.get(localPos.relative(connectedDirection));
				if (storage2 != null && storage2.getItemHandler() != null) {
					secondary = storage2.getItemHandler();
					if (chestType == ChestType.LEFT) {
						// switcheroo
						ItemStackHandler temp = primary;
						primary = secondary;
						secondary = temp;
					}
				}
			}
		}

		int slotCount = primary.getSlotCount() + (secondary == null ? 0 : secondary.getSlotCount());
		if (slotCount == 0)
			return false;
		if (slotCount % 9 != 0)
			return false;

		Supplier<Boolean> stillValid = () -> contraption.entity.isAlive()
			&& player.distanceToSqr(contraption.entity.toGlobalVector(Vec3.atCenterOf(localPos), 0)) < 64;
		Component name = info != null ? info.state().getBlock()
			.getName() : Components.literal("Container");
		player.openMenu(MountedStorageInteraction.createMenuProvider(name, primary, secondary, slotCount, stillValid));

		Vec3 soundPos = contraption.entity.toGlobalVector(Vec3.atCenterOf(localPos), 0);
		player.level().playSound(null, BlockPos.containing(soundPos), SoundEvents.BARREL_OPEN, SoundSource.BLOCKS, 0.75f, 1f);
		return true;
	}

}
