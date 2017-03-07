/**
    Copyright (C) <2017> <coolAlias>

    This file is part of coolAlias' Zelda Sword Skills Minecraft Mod; as such,
    you can redistribute it and/or modify it under the terms of the GNU
    General Public License as published by the Free Software Foundation,
    either version 3 of the License, or (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package zeldaswordskills.client.render.item;

import java.util.List;

import javax.vecmath.Matrix4f;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.Lists;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.model.IFlexibleBakedModel;
import net.minecraftforge.client.model.IPerspectiveAwareModel;
import net.minecraftforge.client.model.ISmartItemModel;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import zeldaswordskills.client.RenderHelperQ;
import zeldaswordskills.item.ItemBombBag;
import zeldaswordskills.ref.ModInfo;

@SuppressWarnings("deprecation")
@SideOnly(Side.CLIENT)
public class ModelItemBombBag implements ISmartItemModel, IPerspectiveAwareModel
{
	private final IFlexibleBakedModel baseModel;
	private int bombsHeld;

	public ModelItemBombBag(IBakedModel baseModel) {
		this.baseModel = (baseModel instanceof IFlexibleBakedModel ? (IFlexibleBakedModel) baseModel : new IFlexibleBakedModel.Wrapper(baseModel, DefaultVertexFormats.ITEM));
	}

	@Override
	public IBakedModel handleItemState(ItemStack stack) {
		if (stack.getItem() instanceof ItemBombBag) {
			this.bombsHeld = ((ItemBombBag) stack.getItem()).getBombsHeld(stack);
		}
		return this;
	}

	@Override
	public Pair<? extends IFlexibleBakedModel, Matrix4f> handlePerspective(ItemCameraTransforms.TransformType cameraTransformType) {
		ForgeHooksClient.handleCameraTransforms(baseModel, cameraTransformType);
		if (cameraTransformType == ItemCameraTransforms.TransformType.GUI) {
			return Pair.of(new ModelItemBombBagGui(baseModel, bombsHeld), null);
		}
		return Pair.of(this, null);
	}

	@Override
	public List<BakedQuad> getFaceQuads(EnumFacing face) {
		return baseModel.getFaceQuads(face);
	}

	@Override
	public List<BakedQuad> getGeneralQuads() {
		return baseModel.getGeneralQuads();
	}

	@Override
	public boolean isAmbientOcclusion() {
		return baseModel.isAmbientOcclusion();
	}

	@Override
	public boolean isGui3d() {
		return baseModel.isGui3d();
	}

	@Override
	public boolean isBuiltInRenderer() {
		return baseModel.isBuiltInRenderer();
	}

	@Override
	public TextureAtlasSprite getParticleTexture() {
		return baseModel.getParticleTexture();
	}

	@Override
	public ItemCameraTransforms getItemCameraTransforms() {
		return baseModel.getItemCameraTransforms();
	}

	@Override
	public VertexFormat getFormat() {
		return baseModel.getFormat();
	}

	/**
	 * Model specifically for the GUI view; renders number of bombs held overlaid as digits
	 */
	private static class ModelItemBombBagGui implements IFlexibleBakedModel
	{
		private final IFlexibleBakedModel baseModel;
		private final TextureAtlasSprite tensSprite, onesSprite;

		public ModelItemBombBagGui(IFlexibleBakedModel baseModel, int bombsHeld) {
			this.baseModel = baseModel;
			int tens = (bombsHeld / 10);
			int ones = (bombsHeld % 10);
			String digit = ModInfo.ID + ":items/digits/";
			this.tensSprite = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(digit + tens);
			this.onesSprite = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(digit + ones);
		}

		@Override
		public List<BakedQuad> getFaceQuads(EnumFacing face) {
			return baseModel.getFaceQuads(face);
		}

		@Override
		public List<BakedQuad> getGeneralQuads() {
			List<BakedQuad> quads = Lists.newArrayList(baseModel.getGeneralQuads());
			quads.add(RenderHelperQ.createBakedQuadForFace(0.25F, 1, 0.5F, 1, -0.01F, 0, tensSprite, EnumFacing.SOUTH));
			quads.add(RenderHelperQ.createBakedQuadForFace(0.5F, 1, 0.5F, 1, -0.01F, 0, onesSprite, EnumFacing.SOUTH));
			return quads;
		}

		@Override
		public boolean isAmbientOcclusion() {
			return baseModel.isAmbientOcclusion();
		}

		@Override
		public boolean isGui3d() {
			return baseModel.isGui3d();
		}

		@Override
		public boolean isBuiltInRenderer() {
			return baseModel.isBuiltInRenderer();
		}

		@Override
		public TextureAtlasSprite getParticleTexture() {
			return baseModel.getParticleTexture();
		}

		@Override
		public ItemCameraTransforms getItemCameraTransforms() {
			return baseModel.getItemCameraTransforms();
		}

		@Override
		public VertexFormat getFormat() {
			return baseModel.getFormat();
		}
	}
}
