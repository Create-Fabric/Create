package com.simibubi.create.lib.mixin.common;

import java.util.List;
import java.util.Random;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.simibubi.create.lib.extensions.TemplateExtensions;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.gen.feature.template.PlacementSettings;
import net.minecraft.world.gen.feature.template.Template;

@Mixin(Template.class)
public abstract class TemplateMixin implements TemplateExtensions {
	@Shadow
	@Final
	private List<Template.EntityInfo> entities;

	@Unique
	@Override
	public List<Template.EntityInfo> create$getEntities() {
		return entities;
	}

	@Inject(at = @At(value = "INVOKE", shift = At.Shift.BEFORE, target = "Lnet/minecraft/world/gen/feature/template/Template;spawnEntities(Lnet/minecraft/world/IServerWorld;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/Mirror;Lnet/minecraft/util/Rotation;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/MutableBoundingBox;Z)V"),
			method = "place(Lnet/minecraft/world/IServerWorld;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/gen/feature/template/PlacementSettings;Ljava/util/Random;I)Z", cancellable = true)
	public void create$place(IServerWorld iServerWorld, BlockPos blockPos, BlockPos blockPos2, PlacementSettings placementSettings, Random random, int i, CallbackInfoReturnable<Boolean> cir) {
		create$addEntitiesToWorld(iServerWorld, blockPos, placementSettings);
		cir.setReturnValue(true);
	}
}
