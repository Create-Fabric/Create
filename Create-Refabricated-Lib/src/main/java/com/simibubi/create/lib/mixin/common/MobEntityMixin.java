package com.simibubi.create.lib.mixin.common;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.simibubi.create.lib.event.MobEntitySetTargetCallback;
import com.simibubi.create.lib.item.EquipmentItem;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;

@Mixin(MobEntity.class)
public abstract class MobEntityMixin {
	@Inject(at = @At("HEAD"), method = "getSlotForItemStack(Lnet/minecraft/item/ItemStack;)Lnet/minecraft/inventory/EquipmentSlotType;", cancellable = true)
	private static void create$getSlotForItemStack(ItemStack itemStack, CallbackInfoReturnable<EquipmentSlotType> cir) {
		if (itemStack.getItem() instanceof EquipmentItem) {
			cir.setReturnValue(((EquipmentItem) itemStack.getItem()).getEquipmentSlot(itemStack));
		}
	}

	@Inject(method = "setAttackTarget(Lnet/minecraft/entity/LivingEntity;)V", at = @At("TAIL"))
	private void create$setTarget(LivingEntity target, CallbackInfo ci) {
		MobEntitySetTargetCallback.EVENT.invoker().onMobEntitySetTarget((MobEntity) (Object) this, target);
	}
}
