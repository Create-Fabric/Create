package com.simibubi.create.lib.mixin.common;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import com.simibubi.create.lib.extensions.ServerPlayerEntityExtensions;

import net.minecraft.entity.player.ServerPlayerEntity;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin implements ServerPlayerEntityExtensions {
	@Unique
	public boolean create$isFake = false;

	@Unique
	@Override
	public boolean create$isFakePlayer() {
		return create$isFake;
	}

	@Unique
	@Override
	public void create$setFake(boolean fake) {
		create$isFake = fake;
	}
}
