package com.simibubi.create.foundation.fluid;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.simibubi.create.Create;
import com.simibubi.create.content.fluids.tank.CreativeFluidTankBlockEntity;
import com.simibubi.create.content.fluids.transfer.GenericItemEmptying;
import com.simibubi.create.content.fluids.transfer.GenericItemFilling;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.utility.Pair;
import com.simibubi.create.foundation.utility.RegisteredObjects;

import io.github.fabricators_of_create.porting_lib.transfer.TransferUtil;
import io.github.fabricators_of_create.porting_lib.util.FluidStack;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

public class FluidHelper {

	public static enum FluidExchange {
		ITEM_TO_TANK, TANK_TO_ITEM;
	}

	public static boolean isWater(Fluid fluid) {
		return convertToStill(fluid) == Fluids.WATER;
	}

	public static boolean isLava(Fluid fluid) {
		return convertToStill(fluid) == Fluids.LAVA;
	}

	@SuppressWarnings("deprecation")
	public static boolean isTag(Fluid fluid, TagKey<Fluid> tag) {
		return fluid.is(tag);
	}

	public static boolean isTag(FluidState fluid, TagKey<Fluid> tag) {
		return fluid.is(tag);
	}

	public static boolean isTag(FluidStack fluid, TagKey<Fluid> tag) {
		return isTag(fluid.getFluid(), tag);
	}

	public static SoundEvent getFillSound(FluidStack fluid) {
		return FluidVariantAttributes.getFillSound(fluid.getType());
	}

	public static SoundEvent getEmptySound(FluidStack fluid) {
		return FluidVariantAttributes.getEmptySound(fluid.getType());
	}

	public static boolean hasBlockState(Fluid fluid) {
		return !fluid.defaultFluidState().createLegacyBlock().isAir();
	}

	public static FluidStack copyStackWithAmount(FluidStack fs, long amount) {
		if (amount <= 0)
			return FluidStack.EMPTY;
		if (fs.isEmpty())
			return FluidStack.EMPTY;
		FluidStack copy = fs.copy();
		copy.setAmount(amount);
		return copy;
	}

	public static Fluid convertToFlowing(Fluid fluid) {
		if (fluid == Fluids.WATER)
			return Fluids.FLOWING_WATER;
		if (fluid == Fluids.LAVA)
			return Fluids.FLOWING_LAVA;
		if (fluid instanceof FlowingFluid)
			return ((FlowingFluid) fluid).getFlowing();
		return fluid;
	}

	public static Fluid convertToStill(Fluid fluid) {
		if (fluid == Fluids.FLOWING_WATER)
			return Fluids.WATER;
		if (fluid == Fluids.FLOWING_LAVA)
			return Fluids.LAVA;
		if (fluid instanceof FlowingFluid)
			return ((FlowingFluid) fluid).getSource();
		return fluid;
	}

	public static JsonElement serializeFluidStack(FluidStack stack) {
		JsonObject json = new JsonObject();
		json.addProperty("fluid", RegisteredObjects.getKeyOrThrow(stack.getFluid())
			.toString());
		json.addProperty("amount", stack.getAmount());
		if (stack.hasTag())
			json.addProperty("nbt", stack.getTag()
				.toString());
		return json;
	}

	public static FluidStack deserializeFluidStack(JsonObject json) {
		ResourceLocation id = new ResourceLocation(GsonHelper.getAsString(json, "fluid"));
		Fluid fluid = BuiltInRegistries.FLUID.get(id);
		if (fluid == null)
			throw new JsonSyntaxException("Unknown fluid '" + id + "'");
		int amount = GsonHelper.getAsInt(json, "amount");
		if (!json.has("nbt"))
			return new FluidStack(fluid, amount);

		try {
			JsonElement element = json.get("nbt");
			CompoundTag nbt = TagParser.parseTag(
					element.isJsonObject() ? Create.GSON.toJson(element) : GsonHelper.convertToString(element, "nbt"));
			return new FluidStack(FluidVariant.of(fluid, nbt), amount, nbt);
		} catch (CommandSyntaxException e) {
			throw new JsonSyntaxException("Failed to read NBT", e);
		}
	}

	public static boolean tryEmptyItemIntoBE(Level worldIn, Player player, InteractionHand handIn, ItemStack heldItem,
		SmartBlockEntity be, Direction side) {
		if (!GenericItemEmptying.canItemBeEmptied(worldIn, heldItem))
			return false;

		Pair<FluidStack, ItemStack> emptyingResult = GenericItemEmptying.emptyItem(worldIn, heldItem, true);

		Storage<FluidVariant> tank = FluidStorage.SIDED.find(worldIn, be.getBlockPos(), null, be, side);
		FluidStack fluidStack = emptyingResult.getFirst();

		if (tank == null)
			return false;
		if (worldIn.isClientSide)
			return true;

		try (Transaction t = TransferUtil.getTransaction()) {
			long inserted = tank.insert(fluidStack.getType(), fluidStack.getAmount(), t);
			if (inserted != fluidStack.getAmount())
				return false;

			ItemStack copyOfHeld = heldItem.copy();
			emptyingResult = GenericItemEmptying.emptyItem(worldIn, copyOfHeld, false);
			t.commit();

			if (!player.isCreative() && !(be instanceof CreativeFluidTankBlockEntity)) {
				if (copyOfHeld.isEmpty())
					player.setItemInHand(handIn, emptyingResult.getSecond());
				else {
					player.setItemInHand(handIn, copyOfHeld);
					player.getInventory().placeItemBackInInventory(emptyingResult.getSecond());
				}
			}
			return true;
		}
	}

	public static boolean tryFillItemFromBE(Level world, Player player, InteractionHand handIn, ItemStack heldItem,
		SmartBlockEntity be, Direction side) {
		if (!GenericItemFilling.canItemBeFilled(world, heldItem))
			return false;

		Storage<FluidVariant> tank = FluidStorage.SIDED.find(world, be.getBlockPos(), null, be, side);

		if (tank == null)
			return false;

		try (Transaction t = TransferUtil.getTransaction()) {
			for (FluidStack fluid : TransferUtil.getAllFluids(tank)) {
				if (fluid.isEmpty())
					continue;
				long requiredAmountForItem = GenericItemFilling.getRequiredAmountForItem(world, heldItem, fluid.copy());
				if (requiredAmountForItem == -1)
					continue;
				if (requiredAmountForItem > fluid.getAmount())
					continue;

				if (world.isClientSide)
					return true;

				if (player.isCreative() || be instanceof CreativeFluidTankBlockEntity)
					heldItem = heldItem.copy();
				ItemStack out = GenericItemFilling.fillItem(world, requiredAmountForItem, heldItem, fluid.copy());

				FluidStack copy = fluid.copy();
				copy.setAmount(requiredAmountForItem);
				tank.extract(copy.getType(), copy.getAmount(), t);
				t.commit();

				if (!player.isCreative())
					player.getInventory().placeItemBackInInventory(out);
				be.notifyUpdate();
				return true;
			}
		}

		return false;
	}

//	@Nullable
//	public static FluidExchange exchange(IFluidHandler fluidTank, IFluidHandlerItem fluidItem, FluidExchange preferred,
//										 int maxAmount) {
//		return exchange(fluidTank, fluidItem, preferred, true, maxAmount);
//	}
//
//	@Nullable
//	public static FluidExchange exchangeAll(IFluidHandler fluidTank, IFluidHandlerItem fluidItem,
//		FluidExchange preferred) {
//		return exchange(fluidTank, fluidItem, preferred, false, Integer.MAX_VALUE);
//	}
//
//	@Nullable
//	private static FluidExchange exchange(IFluidHandler fluidTank, IFluidHandlerItem fluidItem, FluidExchange preferred,
//		boolean singleOp, int maxTransferAmountPerTank) {
//
//		// Locks in the transfer direction of this operation
//		FluidExchange lockedExchange = null;
//
//		for (int tankSlot = 0; tankSlot < fluidTank.getTanks(); tankSlot++) {
//			for (int slot = 0; slot < fluidItem.getTanks(); slot++) {
//
//				FluidStack fluidInTank = fluidTank.getFluidInTank(tankSlot);
//				long tankCapacity = fluidTank.getTankCapacity(tankSlot) - fluidInTank.getAmount();
//				boolean tankEmpty = fluidInTank.isEmpty();
//
//				FluidStack fluidInItem = fluidItem.getFluidInTank(tankSlot);
//				long itemCapacity = fluidItem.getTankCapacity(tankSlot) - fluidInItem.getAmount();
//				boolean itemEmpty = fluidInItem.isEmpty();
//
//				boolean undecided = lockedExchange == null;
//				boolean canMoveToTank = (undecided || lockedExchange == FluidExchange.ITEM_TO_TANK) && tankCapacity > 0;
//				boolean canMoveToItem = (undecided || lockedExchange == FluidExchange.TANK_TO_ITEM) && itemCapacity > 0;
//
//				// Incompatible Liquids
//				if (!tankEmpty && !itemEmpty && !fluidInItem.isFluidEqual(fluidInTank))
//					continue;
//
//				// Transfer liquid to tank
//				if (((tankEmpty || itemCapacity <= 0) && canMoveToTank)
//					|| undecided && preferred == FluidExchange.ITEM_TO_TANK) {
//
//					long amount = fluidTank.fill(
//						fluidItem.drain(Math.min(maxTransferAmountPerTank, tankCapacity), false),
//						false);
//					if (amount > 0) {
//						lockedExchange = FluidExchange.ITEM_TO_TANK;
//						if (singleOp)
//							return lockedExchange;
//						continue;
//					}
//				}
//
//				// Transfer liquid from tank
//				if (((itemEmpty || tankCapacity <= 0) && canMoveToItem)
//					|| undecided && preferred == FluidExchange.TANK_TO_ITEM) {
//
//					long amount = fluidItem.fill(
//						fluidTank.drain(Math.min(maxTransferAmountPerTank, itemCapacity), false),
//						false);
//					if (amount > 0) {
//						lockedExchange = FluidExchange.TANK_TO_ITEM;
//						if (singleOp)
//							return lockedExchange;
//						continue;
//					}
//
//				}
//
//			}
//		}
//
//		return null;
//	}

}
