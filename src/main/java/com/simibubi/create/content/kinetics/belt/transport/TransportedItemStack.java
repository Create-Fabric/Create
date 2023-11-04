package com.simibubi.create.content.kinetics.belt.transport;

import java.util.Random;

import com.simibubi.create.content.kinetics.belt.BeltHelper;
import com.simibubi.create.content.kinetics.fan.processing.FanProcessingType;

import io.github.fabricators_of_create.porting_lib.util.NBTSerializer;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public class TransportedItemStack implements Comparable<TransportedItemStack> {

	private static Random R = new Random();

	// fabric: can't use null in some places, have a static empty stack
	public static final TransportedItemStack EMPTY = new TransportedItemStack(ItemStack.EMPTY);

	public ItemStack stack;
	public float beltPosition;
	public float sideOffset;
	public int angle;
	public int insertedAt;
	public Direction insertedFrom;
	public boolean locked;
	public boolean lockedExternally;

	public float prevBeltPosition;
	public float prevSideOffset;

	public FanProcessingType processedBy;
	public int processingTime;

	public TransportedItemStack(ItemStack stack) {
		this.stack = stack;
		boolean centered = BeltHelper.isItemUpright(stack);
		angle = centered ? 180 : R.nextInt(360);
		sideOffset = prevSideOffset = getTargetSideOffset();
		insertedFrom = Direction.UP;
	}

	public float getTargetSideOffset() {
		return (angle - 180) / (360 * 3f);
	}

	@Override
	public int compareTo(TransportedItemStack o) {
		return beltPosition < o.beltPosition ? 1 : beltPosition > o.beltPosition ? -1 : 0;
	}

	public TransportedItemStack getSimilar() {
		TransportedItemStack copy = new TransportedItemStack(stack.copy());
		copy.beltPosition = beltPosition;
		copy.insertedAt = insertedAt;
		copy.insertedFrom = insertedFrom;
		copy.prevBeltPosition = prevBeltPosition;
		copy.prevSideOffset = prevSideOffset;
		copy.processedBy = processedBy;
		copy.processingTime = processingTime;
		return copy;
	}

	public TransportedItemStack copy() {
		TransportedItemStack copy = getSimilar();
		copy.angle = angle;
		copy.sideOffset = sideOffset;
		return copy;
	}

	// fabric: full copy, used for snapshots
	public TransportedItemStack fullCopy() {
		TransportedItemStack copy = copy();
		copy.locked = locked;
		copy.lockedExternally = lockedExternally;
		return copy;
	}

	public CompoundTag serializeNBT() {
		CompoundTag nbt = new CompoundTag();
		nbt.put("Item", NBTSerializer.serializeNBT(stack));
		nbt.putFloat("Pos", beltPosition);
		nbt.putFloat("PrevPos", prevBeltPosition);
		nbt.putFloat("Offset", sideOffset);
		nbt.putFloat("PrevOffset", prevSideOffset);
		nbt.putInt("InSegment", insertedAt);
		nbt.putInt("Angle", angle);
		nbt.putInt("InDirection", insertedFrom.get3DDataValue());
		if (locked)
			nbt.putBoolean("Locked", locked);
		if (lockedExternally)
			nbt.putBoolean("LockedExternally", lockedExternally);
		return nbt;
	}

	public static TransportedItemStack read(CompoundTag nbt) {
		TransportedItemStack stack = new TransportedItemStack(ItemStack.of(nbt.getCompound("Item")));
		stack.beltPosition = nbt.getFloat("Pos");
		stack.prevBeltPosition = nbt.getFloat("PrevPos");
		stack.sideOffset = nbt.getFloat("Offset");
		stack.prevSideOffset = nbt.getFloat("PrevOffset");
		stack.insertedAt = nbt.getInt("InSegment");
		stack.angle = nbt.getInt("Angle");
		stack.insertedFrom = Direction.from3DDataValue(nbt.getInt("InDirection"));
		stack.locked = nbt.getBoolean("Locked");
		stack.lockedExternally = nbt.getBoolean("LockedExternally");
		return stack;
	}

	// fabric: a proper equals() is required for correct functionality with transfer snapshots. (#903)
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		TransportedItemStack that = (TransportedItemStack) o;

		if (Float.compare(that.beltPosition, beltPosition) != 0) return false;
		if (Float.compare(that.sideOffset, sideOffset) != 0) return false;
		if (angle != that.angle) return false;
		if (insertedAt != that.insertedAt) return false;
		if (locked != that.locked) return false;
		if (lockedExternally != that.lockedExternally) return false;
		if (Float.compare(that.prevBeltPosition, prevBeltPosition) != 0) return false;
		if (Float.compare(that.prevSideOffset, prevSideOffset) != 0) return false;
		if (processingTime != that.processingTime) return false;
		if (!ItemStack.matches(stack, that.stack)) return false;
		if (insertedFrom != that.insertedFrom) return false;
		return processedBy == that.processedBy;
	}
}
