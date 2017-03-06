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

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.model.IFlexibleBakedModel;
import net.minecraftforge.client.model.IPerspectiveAwareModel;
import net.minecraftforge.client.model.ISmartItemModel;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import zeldaswordskills.api.item.IDynamicItemBlock;
import zeldaswordskills.item.ItemHeldBlock;
import zeldaswordskills.ref.ModInfo;

@SuppressWarnings("deprecation")
@SideOnly(Side.CLIENT)
public class ModelDynamicItemBlock implements ISmartItemModel {

	/** Default resource location in case dynamic block state to render cannot be determined */
	public static final ModelResourceLocation resource = new ModelResourceLocation(ModInfo.ID + ":dungeon_block", "inventory");
	private final IBakedModel defaultModel;
	private final Minecraft mc;

	public ModelDynamicItemBlock(IBakedModel defaultModel) {
		this.defaultModel = defaultModel;
		this.mc = Minecraft.getMinecraft();
	}

	@Override
	public IBakedModel handleItemState(ItemStack stack) {
		if (stack.getItem() instanceof IDynamicItemBlock) {
			IBlockState renderState = ((IDynamicItemBlock) stack.getItem()).getBlockStateFromStack(stack);
			if (renderState != null) {
				Block block = renderState.getBlock();
				ItemStack itemBlock = new ItemStack(block, 1, block.getMetaFromState(renderState));
				if (itemBlock.getItem() == null) {
					return defaultModel;
				}
				IBakedModel renderModel =  mc.getRenderItem().getItemModelMesher().getItemModel(itemBlock);
				return (stack.getItem() instanceof ItemHeldBlock ? new ItemHeldBlockPerspectiveModel(renderModel) : renderModel);
			}
		}
		return defaultModel;
	}

	@Override
	public TextureAtlasSprite getParticleTexture() {
		return defaultModel.getParticleTexture();
	}

	@Override
	public List<BakedQuad> getFaceQuads(EnumFacing face) {
		return null;
	}

	@Override
	public List<BakedQuad> getGeneralQuads() {
		return null;
	}

	@Override
	public boolean isAmbientOcclusion() {
		return false;
	}

	@Override
	public boolean isGui3d() {
		return false;
	}

	@Override
	public boolean isBuiltInRenderer() {
		return false;
	}

	@Override
	public ItemCameraTransforms getItemCameraTransforms() {
		return null;
	}

	/**
	 * 
	 * Model for held item blocks: render larger, slightly rotated and centered as though
	 * carried with two hands.
	 *
	 */
	private static class ItemHeldBlockPerspectiveModel implements IPerspectiveAwareModel
	{
		private final IBakedModel parent;

		public ItemHeldBlockPerspectiveModel(IBakedModel parent) {
			this.parent = parent;
		}

		@Override
		public List<BakedQuad> getFaceQuads(EnumFacing face) {
			return parent.getFaceQuads(face);
		}

		@Override
		public List<BakedQuad> getGeneralQuads() {
			return parent.getGeneralQuads();
		}

		@Override
		public boolean isAmbientOcclusion() {
			return parent.isAmbientOcclusion();
		}

		@Override
		public boolean isGui3d() {
			return parent.isGui3d();
		}

		@Override
		public boolean isBuiltInRenderer() {
			return parent.isBuiltInRenderer();
		}

		@Override
		public TextureAtlasSprite getParticleTexture() {
			return parent.getParticleTexture();
		}

		@Override
		public ItemCameraTransforms getItemCameraTransforms() {
			return parent.getItemCameraTransforms();
		}

		@Override
		public Pair<? extends IFlexibleBakedModel, Matrix4f> handlePerspective(ItemCameraTransforms.TransformType cameraTransformType) {
			Matrix4f matrix = null;
			switch (cameraTransformType) {
			case FIRST_PERSON:
				GlStateManager.scale(2.0F, 2.0F, 2.0F);
				RenderItem.applyVanillaTransform(parent.getItemCameraTransforms().firstPerson);
				GlStateManager.rotate(45.0F, 0.0F, 1.0F, 0.0F);
				GlStateManager.translate(0.0F, 0.0F, -0.35F);
				break;
			case GUI:
				RenderItem.applyVanillaTransform(parent.getItemCameraTransforms().gui);
				break;
			case HEAD:
				RenderItem.applyVanillaTransform(parent.getItemCameraTransforms().head);
				break;
			case THIRD_PERSON:
				GlStateManager.scale(2.0F, 2.0F, 2.0F);
				RenderItem.applyVanillaTransform(parent.getItemCameraTransforms().thirdPerson);
				GlStateManager.rotate(45.0F, 0.0F, 1.0F, 0.0F);
				GlStateManager.translate(-0.15F, 0.25F, -0.25F);
				break;
			default:
				break;
			}
			return Pair.of(parent, matrix);
		}

		@Override
		public VertexFormat getFormat() {
			return DefaultVertexFormats.ITEM;
		}
	}
}
