package com.simibubi.create.lib.mixin.client;

import javax.annotation.Nonnull;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.lib.event.RenderHandCallback;
import com.simibubi.create.lib.extensions.ItemExtensions;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.FirstPersonRenderer;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

@Environment(EnvType.CLIENT)
@Mixin(FirstPersonRenderer.class)
public abstract class FirstPersonRendererMixin {
	private static int slotMainHand = 0;
	@Shadow
	private ItemStack itemStackMainHand;
	@Shadow
	private ItemStack itemStackOffHand;

	private static boolean create$shouldCauseReequipAnimation(@Nonnull ItemStack from, @Nonnull ItemStack to, int slot) {
		if (from.isEmpty() && to.isEmpty()) return false;
		if (from.isEmpty() || to.isEmpty()) return true;

		boolean changed = false;
		if (slot != -1) {
			changed = slot != slotMainHand;
			slotMainHand = slot;
		}
		return ((ItemExtensions) from.getItem()).create$shouldCauseReequipAnimation(from, to, changed);
	}

	@Inject(at = @At("HEAD"), method = "renderFirstPersonItem(Lnet/minecraft/client/entity/player/AbstractClientPlayerEntity;FFLnet/minecraft/util/Hand;FLnet/minecraft/item/ItemStack;FLcom/mojang/blaze3d/matrix/MatrixStack;Lnet/minecraft/client/renderer/IRenderTypeBuffer;I)V", cancellable = true)
	private void create$onRenderFirstPersonItem(AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand, float swingProgress, ItemStack stack, float equipProgress, MatrixStack matrices, IRenderTypeBuffer vertexConsumers, int light, CallbackInfo ci) {
		if (RenderHandCallback.EVENT.invoker().onRenderHand(player, hand, stack, matrices, vertexConsumers, tickDelta, pitch, swingProgress, equipProgress, light)) {
			ci.cancel();
		}
	}

	@Inject(at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/client/entity/player/ClientPlayerEntity;getCooledAttackStrength(F)F"),
			locals = LocalCapture.CAPTURE_FAILEXCEPTION,
			method = "tick()V")
	public void tick(CallbackInfo ci,
					 ClientPlayerEntity clientPlayerEntity, ItemStack itemStack, ItemStack itemStack2) {
		if (create$shouldCauseReequipAnimation(itemStackMainHand, itemStack, clientPlayerEntity.inventory.currentItem)) {
			itemStackMainHand = itemStack;
		}

		if (create$shouldCauseReequipAnimation(itemStackOffHand, itemStack2, -1)) {
			itemStackOffHand = itemStack2;
		}
	}
}
