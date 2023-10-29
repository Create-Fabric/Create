package com.simibubi.create.content.fluids.potion;

import java.util.Collection;
import java.util.List;

import com.simibubi.create.AllFluids;
import com.simibubi.create.content.fluids.VirtualFluid;
import com.simibubi.create.foundation.utility.RegisteredObjects;

import io.github.fabricators_of_create.porting_lib.fluids.FluidStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.Potions;

public class PotionFluid extends VirtualFluid {

	public PotionFluid(Properties properties) {
		super(properties);
	}

	public static FluidStack of(long amount, Potion potion) {
		FluidStack fluidStack = new FluidStack(AllFluids.POTION.get()
				.getSource(), amount);
		return addPotionToFluidStack(fluidStack, potion);
	}

	public static FluidStack withEffects(long amount, Potion potion, List<MobEffectInstance> customEffects) {
		FluidStack fluidStack = of(amount, potion);
		return appendEffects(fluidStack, customEffects);
	}

	public static FluidStack addPotionToFluidStack(FluidStack fs, Potion potion) {
		ResourceLocation resourcelocation = RegisteredObjects.getKeyOrThrow(potion);
		if (potion == Potions.EMPTY) {
			fs.removeChildTag("Potion");
			return new FluidStack(fs.getFluid(), fs.getAmount(), fs.getTag());
		}
		fs.getOrCreateTag()
				.putString("Potion", resourcelocation.toString());
		return new FluidStack(fs.getFluid(), fs.getAmount(), fs.getTag());
	}

	public static FluidStack appendEffects(FluidStack fs, Collection<MobEffectInstance> customEffects) {
		if (customEffects.isEmpty())
			return fs;
		CompoundTag compoundnbt = fs.getOrCreateTag();
		ListTag listnbt = compoundnbt.getList("CustomPotionEffects", 9);
		for (MobEffectInstance effectinstance : customEffects)
			listnbt.add(effectinstance.save(new CompoundTag()));
		compoundnbt.put("CustomPotionEffects", listnbt);
		return new FluidStack(fs.getFluid(), fs.getAmount(), fs.getTag());
	}

	public enum BottleType {
		REGULAR, SPLASH, LINGERING;
	}

// fabric: PotionFluidVariantRenderHandler and PotionFluidVariantAttributeHandler in AllFluids
// --------------------------------
//	public static class PotionFluidType extends TintedFluidType {
//
//		public PotionFluidType(Properties properties, ResourceLocation stillTexture, ResourceLocation flowingTexture) {
//			super(properties, stillTexture, flowingTexture);
//		}
//
//		@Override
//		public int getTintColor(FluidStack stack) {
//			CompoundTag tag = stack.getOrCreateTag();
//			int color = PotionUtils.getColor(PotionUtils.getAllEffects(tag)) | 0xff000000;
//			return color;
//		}
//
//		@Override
//		public String getDescriptionId(FluidStack stack) {
//			CompoundTag tag = stack.getOrCreateTag();
//			ItemLike itemFromBottleType =
//				PotionFluidHandler.itemFromBottleType(NBTHelper.readEnum(tag, "Bottle", BottleType.class));
//			return PotionUtils.getPotion(tag)
//				.getName(itemFromBottleType.asItem()
//					.getDescriptionId() + ".effect.");
//		}
//
//		@Override
//		protected int getTintColor(FluidState state, BlockAndTintGetter getter, BlockPos pos) {
//			return NO_TINT;
//		}
//
//	}
}
