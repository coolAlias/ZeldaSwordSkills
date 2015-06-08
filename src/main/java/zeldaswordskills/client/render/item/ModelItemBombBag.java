/**
    Copyright (C) <2015> <coolAlias>

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

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.model.IPerspectiveAwareModel;
import net.minecraftforge.client.model.ISmartItemModel;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.apache.commons.lang3.tuple.Pair;

import zeldaswordskills.client.RenderHelperQ;
import zeldaswordskills.item.ItemBombBag;
import zeldaswordskills.ref.ModInfo;

import com.google.common.collect.Lists;

@SuppressWarnings("deprecation")
@SideOnly(Side.CLIENT)
public class ModelItemBombBag implements ISmartItemModel, IPerspectiveAwareModel
{
	private final IBakedModel baseModel;
	private int bombsHeld;

	public ModelItemBombBag(IBakedModel baseModel) {
		this.baseModel = baseModel;
	}

	@Override
	public IBakedModel handleItemState(ItemStack stack) {
		if (stack.getItem() instanceof ItemBombBag) {
			this.bombsHeld = ((ItemBombBag) stack.getItem()).getBombsHeld(stack);
		}
		return this;
	}

	@Override
	public Pair<IBakedModel, Matrix4f> handlePerspective(ItemCameraTransforms.TransformType cameraTransformType) {
		switch (cameraTransformType) {
		case FIRST_PERSON:
			RenderItem.applyVanillaTransform(baseModel.getItemCameraTransforms().firstPerson);
			return Pair.of(baseModel, null);
		case GUI:
			RenderItem.applyVanillaTransform(baseModel.getItemCameraTransforms().gui);
			return Pair.of((IBakedModel)(new ModelItemBombBagGui(baseModel, bombsHeld)), null);
		case HEAD:
			RenderItem.applyVanillaTransform(baseModel.getItemCameraTransforms().head);
			return Pair.of(baseModel, null);
		case THIRD_PERSON:
			RenderItem.applyVanillaTransform(baseModel.getItemCameraTransforms().thirdPerson);
			return Pair.of(baseModel, null);
		default:
			break;
		}
		return Pair.of(baseModel, null);
	}

	@Override
	public List getFaceQuads(EnumFacing face) {
		return baseModel.getFaceQuads(face);
	}

	@Override
	public List getGeneralQuads() {
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
	public TextureAtlasSprite getTexture() {
		return baseModel.getTexture();
	}

	@Override
	public ItemCameraTransforms getItemCameraTransforms() {
		return baseModel.getItemCameraTransforms();
	}

	/**
	 * Model specifically for the GUI view; renders number of bombs held overlaid as digits
	 */
	private static class ModelItemBombBagGui implements IBakedModel
	{
		private final IBakedModel baseModel;
		private final TextureAtlasSprite tensSprite, onesSprite;

		public ModelItemBombBagGui(IBakedModel baseModel, int bombsHeld) {
			this.baseModel = baseModel;
			int tens = (bombsHeld / 10);
			int ones = (bombsHeld % 10);
			String digit = ModInfo.ID + ":items/digits/";
			this.tensSprite = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(digit + tens);
			this.onesSprite = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(digit + ones);
		}

		@Override
		public List getFaceQuads(EnumFacing face) {
			return baseModel.getFaceQuads(face);
		}

		@Override
		public List getGeneralQuads() {
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
		public TextureAtlasSprite getTexture() {
			return baseModel.getTexture();
		}

		@Override
		public ItemCameraTransforms getItemCameraTransforms() {
			return baseModel.getItemCameraTransforms();
		}
	}
}
