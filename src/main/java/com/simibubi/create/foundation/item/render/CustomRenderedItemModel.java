package com.simibubi.create.foundation.item.render;

import net.fabricmc.fabric.api.renderer.v1.model.ForwardingBakedModel;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.resources.ResourceLocation;

public class CustomRenderedItemModel extends ForwardingBakedModel {

	public CustomRenderedItemModel(BakedModel originalModel) {
		this.wrapped = originalModel;
	}

	@Override
	public boolean isCustomRenderer() {
		return true;
	}

//	@Override
//	public BakedModel applyTransform(ItemTransforms.TransformType cameraTransformType, PoseStack mat, boolean leftHand) {
//		// Super call returns originalModel, but we want to return this, else BEWLR
//		// won't be used.
//		super.applyTransform(cameraTransformType, mat, leftHand);
//		return this;
//	}

	public BakedModel getOriginalModel() {
		return wrapped;
	}

}
