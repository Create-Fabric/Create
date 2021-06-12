package com.simibubi.create.lib.mixin.client;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.simibubi.create.lib.item.UseFirstBehaviorItem;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.multiplayer.PlayerController;
import net.minecraft.client.network.play.ClientPlayNetHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.ItemUseContext;
import net.minecraft.network.play.client.CPlayerTryUseItemOnBlockPacket;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockRayTraceResult;

@Environment(EnvType.CLIENT)
@Mixin(PlayerController.class)
public abstract class PlayerControllerMixin {
	@Final
	@Shadow
	private ClientPlayNetHandler connection;

	@Inject(at = @At("HEAD"),
			method = "func_217292_a(Lnet/minecraft/client/entity/player/ClientPlayerEntity;Lnet/minecraft/client/world/ClientWorld;Lnet/minecraft/util/Hand;Lnet/minecraft/util/math/BlockRayTraceResult;)Lnet/minecraft/util/ActionResultType;",
			cancellable = true)
	public void create$func_217292_a(ClientPlayerEntity clientPlayerEntity, ClientWorld clientWorld, Hand hand, BlockRayTraceResult blockRayTraceResult, CallbackInfoReturnable<ActionResultType> cir) {
		if (clientPlayerEntity.getHeldItem(hand).getItem() instanceof UseFirstBehaviorItem) {
			ItemUseContext create$itemUseContext = new ItemUseContext(clientPlayerEntity, hand, blockRayTraceResult);
			ActionResultType create$result = ((UseFirstBehaviorItem) clientPlayerEntity.getHeldItem(hand).getItem()).onItemUseFirst(clientPlayerEntity.getHeldItem(hand), create$itemUseContext);
			if (create$result != ActionResultType.PASS) {
				this.connection.sendPacket(new CPlayerTryUseItemOnBlockPacket(hand, blockRayTraceResult));
				cir.setReturnValue(create$result);
			}
		}
	}
}
