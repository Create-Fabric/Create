package com.simibubi.create.lib.mixin.common;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.simibubi.create.lib.extensions.Matrix4fExtensions;
import com.simibubi.create.lib.utility.MixinHelper;

import net.minecraft.util.math.vector.Matrix4f;

@Mixin(Matrix4f.class)
public abstract class Matrix4fMixin implements Matrix4fExtensions {
	@Shadow
	protected float a00;
	@Shadow
	protected float a01;
	@Shadow
	protected float a02;
	@Shadow
	protected float a03;
	@Shadow
	protected float a10;
	@Shadow
	protected float a11;
	@Shadow
	protected float a12;
	@Shadow
	protected float a13;
	@Shadow
	protected float a20;
	@Shadow
	protected float a21;
	@Shadow
	protected float a22;
	@Shadow
	protected float a23;
	@Shadow
	protected float a30;
	@Shadow
	protected float a31;
	@Shadow
	protected float a32;
	@Shadow
	protected float a33;

	@Override
	public void create$set(@NotNull Matrix4f other) {
		Matrix4fMixin o = MixinHelper.cast(other); // This will look weird in the merged class

		a00 = o.a00;
		a01 = o.a01;
		a02 = o.a02;
		a03 = o.a03;

		a10 = o.a10;
		a11 = o.a11;
		a12 = o.a12;
		a13 = o.a13;

		a20 = o.a20;
		a21 = o.a21;
		a22 = o.a22;
		a23 = o.a23;

		a30 = o.a30;
		a31 = o.a31;
		a32 = o.a32;
		a33 = o.a33;
	}

	@ApiStatus.Internal
	@Override
	@Contract(mutates = "this")
	public void create$fromFloatArray(float[] floats) {
		a00 = floats[0];
		a01 = floats[1];
		a02 = floats[2];
		a03 = floats[3];

		a10 = floats[4];
		a11 = floats[5];
		a12 = floats[6];
		a13 = floats[7];

		a20 = floats[8];
		a21 = floats[9];
		a22 = floats[10];
		a23 = floats[11];

		a30 = floats[12];
		a31 = floats[13];
		a32 = floats[14];
		a33 = floats[15];
	}

	@Override
	public float[] create$writeMatrix() {
		return new float[]{
				a00,
				a10,
				a20,
				a30,
				a01,
				a11,
				a21,
				a31,
				a02,
				a12,
				a22,
				a32,
				a03,
				a13,
				a23,
				a33,
		};
	}
}
