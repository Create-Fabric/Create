package com.simibubi.create.lib.mixin.common;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.simibubi.create.lib.extensions.EntitySelectionContextExtensions;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.shapes.EntitySelectionContext;

@Mixin(EntitySelectionContext.class)
public abstract class EntitySelectionContextMixin implements EntitySelectionContextExtensions {
	@Unique
	private Entity create$cachedEntity;

	@Inject(at = @At("TAIL"), method = "<init>(Lnet/minecraft/entity/Entity;)V")
	private void create$onTailEntityInit(Entity entity, CallbackInfo ci) {
		create$cachedEntity = entity;
	}

	@Override
	@Nullable
	public Entity create$getCachedEntity() {
		return create$cachedEntity;
	}
}
