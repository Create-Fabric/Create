package com.simibubi.create.foundation.block.connected;

import java.util.Arrays;
import java.util.Random;
import java.util.function.Supplier;

import com.simibubi.create.foundation.block.connected.ConnectedTextureBehaviour.CTContext;
import com.simibubi.create.foundation.utility.Iterate;

import net.fabricmc.fabric.api.renderer.v1.model.ForwardingBakedModel;
import net.fabricmc.fabric.api.renderer.v1.model.SpriteFinder;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockDisplayReader;

public class CTModel extends ForwardingBakedModel {

	private static final ThreadLocal<SpriteFinder> SPRITE_FINDER = ThreadLocal.withInitial(() -> SpriteFinder.get(Minecraft.getInstance().getModelManager().method_24153(AtlasTexture.LOCATION_BLOCKS_TEXTURE)));

	private ConnectedTextureBehaviour behaviour;

	private class CTData {
		int[] indices;

		public CTData() {
			indices = new int[6];
			Arrays.fill(indices, -1);
		}

		void put(Direction face, int texture) {
			indices[face.getIndex()] = texture;
		}

		int get(Direction face) {
			return indices[face.getIndex()];
		}
	}

	public CTModel(IBakedModel originalModel, ConnectedTextureBehaviour behaviour) {
		wrapped = originalModel;
		this.behaviour = behaviour;
	}

	protected CTData createCTData(IBlockDisplayReader world, BlockPos pos, BlockState state) {
		CTData data = new CTData();
		for (Direction face : Iterate.directions) {
			if (!Block.shouldSideBeRendered(state, world, pos, face) && !behaviour.buildContextForOccludedDirections())
				continue;
			CTSpriteShiftEntry spriteShift = behaviour.get(state, face);
			if (spriteShift == null)
				continue;
			CTContext ctContext = behaviour.buildContext(world, pos, state, face);
			data.put(face, spriteShift.getTextureIndex(ctContext));
		}
		return data;
	}

	@Override
	public boolean isVanillaAdapter() {
		return false;
	}

	@Override
	public void emitBlockQuads(IBlockDisplayReader blockView, BlockState state, BlockPos pos, Supplier<Random> randomSupplier, RenderContext context) {
		CTData data = createCTData(blockView, pos, state);
		context.pushTransform(quad -> {
			TextureAtlasSprite original = SPRITE_FINDER.get().find(quad, 0);
			CTSpriteShiftEntry spriteShift = behaviour.get(state, quad.lightFace());
			if (spriteShift == null)
				return true;
			if (original != spriteShift.getOriginal())
				return true;
			int index = data.get(quad.lightFace());
			if (index == -1)
				return true;
			for (int vertexIndex = 0; vertexIndex < 4; vertexIndex++) {
				float u = quad.spriteU(vertexIndex, 0);
				float v = quad.spriteV(vertexIndex, 0);
				u = spriteShift.getTargetU(u, index);
				v = spriteShift.getTargetV(v, index);
				quad.sprite(vertexIndex, 0, u, v);
			}
			return true;
		});
		super.emitBlockQuads(blockView, state, pos, randomSupplier, context);
		context.popTransform();
	}

}
