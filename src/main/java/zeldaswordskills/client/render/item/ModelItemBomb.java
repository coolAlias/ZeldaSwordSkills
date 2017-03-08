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

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.model.IFlexibleBakedModel;
import net.minecraftforge.client.model.IPerspectiveAwareModel;
import net.minecraftforge.client.model.ISmartItemModel;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import zeldaswordskills.api.entity.BombType;
import zeldaswordskills.client.model.ModelBomb;
import zeldaswordskills.client.render.entity.RenderEntityBomb;
import zeldaswordskills.item.ItemBomb;
import zeldaswordskills.ref.ModInfo;

@SuppressWarnings("deprecation")
@SideOnly(Side.CLIENT)
public class ModelItemBomb implements ISmartItemModel, IPerspectiveAwareModel
{
	protected final ModelBomb bombModel;
	private final IFlexibleBakedModel baseModel;
	private IFlexibleBakedModel emptyModel;
	private BombType type;
	private boolean isFlashing;

	public ModelItemBomb(IBakedModel baseModel) {
		bombModel = new ModelBomb();
		this.baseModel = (baseModel instanceof IFlexibleBakedModel ? (IFlexibleBakedModel) baseModel : new IFlexibleBakedModel.Wrapper(baseModel, DefaultVertexFormats.ITEM));
	}

	@Override
	public IBakedModel handleItemState(ItemStack stack) {
		isFlashing = (stack.hasTagCompound() && stack.getTagCompound().hasKey("time") && stack.getTagCompound().getInteger("time") % 13 > 10);
		type = ItemBomb.getType(stack);
		return this;
	}

	@Override
	public Pair<? extends IFlexibleBakedModel, Matrix4f> handlePerspective(ItemCameraTransforms.TransformType cameraTransformType) {
		// Render as 2D sprite when in GUI
		if (cameraTransformType == ItemCameraTransforms.TransformType.GUI) {
			ForgeHooksClient.handleCameraTransforms(baseModel, cameraTransformType);
			return Pair.of(this, null);
		}
		GlStateManager.pushMatrix();
		switch (cameraTransformType) {
		case FIRST_PERSON:
			GlStateManager.translate(0.5F, 0.5F, 0.5F);
			GlStateManager.rotate(180.0F, 1.0F, 0.0F, 0.0F);
			GlStateManager.translate(-0.75F, -0.5F, 0.5F);
			if (isFlashing) {
				GlStateManager.scale(1.2F, 1.2F, 1.2F);
			}
			break;
		case THIRD_PERSON:
			GlStateManager.rotate(80.0F, 1.0F, 0.0F, 0.0F);
			GlStateManager.translate(0.0F, -0.525F, -0.1F);
			GlStateManager.scale(0.5F, 0.5F, 0.5F);
			if (isFlashing) {
				GlStateManager.scale(1.2F, 1.2F, 1.2F);
			}
			break;
		case GROUND:
			GlStateManager.rotate(180.0F, 1.0F, 0.0F, 0.0F);
			GlStateManager.translate(0.0F, -0.725F, 0.0F);
			GlStateManager.scale(0.825F, 0.825F, 0.825F);
			break;
		case FIXED: // e.g. inside a ceramic jar
			GlStateManager.rotate(90.0F, 1.0F, 1.0F, 0.0F);
			GlStateManager.translate(0F, -0.325F, 0F);
			GlStateManager.scale(0.5F, 0.5F, 0.5F);
			break;
		default:
		}
		Minecraft.getMinecraft().getTextureManager().bindTexture(getTexture(type, isFlashing));
		// first Entity parameter not used for anything in ModelBomb, so null is safe
		bombModel.render(null, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0475F);
		GlStateManager.popMatrix();
		if (this.emptyModel == null) {
			ModelResourceLocation resource = new ModelResourceLocation(ModInfo.ID + ":empty", "inventory");
			this.emptyModel = new IFlexibleBakedModel.Wrapper(Minecraft.getMinecraft().getRenderItem().getItemModelMesher().getModelManager().getModel(resource), DefaultVertexFormats.ITEM);
		}
		// return empty model to render nothing - bomb model already rendered
		return Pair.of(emptyModel, null);
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
		return false;
	}

	@Override
	public TextureAtlasSprite getParticleTexture() {
		return baseModel.getParticleTexture();
	}

	@Override
	public ItemCameraTransforms getItemCameraTransforms() {
		return baseModel.getItemCameraTransforms();
	}

	private ResourceLocation getTexture(BombType type, boolean isFlashing) {
		int i = type.ordinal();
		return (isFlashing) ? RenderEntityBomb.flashTextures[i] : RenderEntityBomb.bombTextures[i];
	}

	@Override
	public VertexFormat getFormat() {
		return baseModel.getFormat();
	}
}
