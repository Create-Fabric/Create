package com.simibubi.create.lib.mixin.common;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import com.simibubi.create.lib.utility.Constants;

import net.minecraft.item.crafting.ShapelessRecipe;

@Mixin(ShapelessRecipe.Serializer.class)
public abstract class ShapelessRecipe$SerializerMixin {
	@ModifyConstant(method = "read(Lnet/minecraft/util/ResourceLocation;Lcom/google/gson/JsonObject;)Lnet/minecraft/item/crafting/ShapelessRecipe;",
			constant = @Constant(intValue = 9))
	private static int modifyMaxItemsInRecipe(int original) {
		return Constants.crafting.HEIGHT * Constants.crafting.WIDTH;
	}
}
