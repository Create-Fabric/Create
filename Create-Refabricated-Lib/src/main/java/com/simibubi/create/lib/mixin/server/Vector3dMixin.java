package com.simibubi.create.lib.mixin.server;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.math.vector.Vector3d;

@Environment(EnvType.SERVER)
@Mixin(Vector3d.class)
public abstract class Vector3dMixin {
	@Shadow
	public abstract Vector3d scale(double mult);

	// They are client-only, but not anymore!

	public Vector3d inverse() {
		return scale(-1.0D);
	}

	public Vector3d method_22882() {
		return inverse();
	}
}
