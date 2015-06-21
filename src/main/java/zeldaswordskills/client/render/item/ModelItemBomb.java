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
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IPerspectiveAwareModel;
import net.minecraftforge.client.model.ISmartItemModel;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.apache.commons.lang3.tuple.Pair;

import zeldaswordskills.ZSSMain;
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
	private final IBakedModel baseModel;
	private final IBakedModel emptyModel;
	private BombType type;
	private boolean isFlashing;

	public ModelItemBomb(IBakedModel baseModel) {
		bombModel = new ModelBomb();
		this.baseModel = baseModel;
		ModelResourceLocation resource = new ModelResourceLocation(ModInfo.ID + ":empty", "inventory");
		this.emptyModel = Minecraft.getMinecraft().getRenderItem().getItemModelMesher().getModelManager().getModel(resource);
		if (emptyModel == null) {
			ZSSMain.logger.warn("Failed to retrieve model for resource location: " + resource);
		}
	}

	@Override
	public IBakedModel handleItemState(ItemStack stack) {
		isFlashing = (stack.hasTagCompound() && stack.getTagCompound().hasKey("time") && stack.getTagCompound().getInteger("time") % 13 > 10);
		type = ItemBomb.getType(stack);
		return this;
	}

	@Override
	public Pair<IBakedModel, Matrix4f> handlePerspective(ItemCameraTransforms.TransformType cameraTransformType) {
		// gui renders as 2D sprite; this is apparently also what renders when the item is dropped
		if (cameraTransformType == ItemCameraTransforms.TransformType.GUI) {
			RenderItem.applyVanillaTransform(baseModel.getItemCameraTransforms().gui);
			return Pair.of(baseModel, null);
		}
		GlStateManager.pushMatrix();
		switch (cameraTransformType) {
		case FIRST_PERSON:
			// TODO the NBT updating causes the 1st person rendering to occur much lower than normal
			// once that is fixed, the bomb will render too high and will need to be adjusted accordingly
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
		default:
			break;
		}
		Minecraft.getMinecraft().getTextureManager().bindTexture(getTexture(type, isFlashing));
		// first Entity parameter not used for anything in ModelBomb, so null is safe
		bombModel.render(null, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0475F);
		GlStateManager.popMatrix();
		// return empty model to render nothing - bomb model already rendered
		return Pair.of(emptyModel, null);
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
		return false;
	}

	@Override
	public TextureAtlasSprite getTexture() {
		return baseModel.getTexture();
	}

	@Override
	public ItemCameraTransforms getItemCameraTransforms() {
		return baseModel.getItemCameraTransforms();
	}

	private ResourceLocation getTexture(BombType type, boolean isFlashing) {
		int i = type.ordinal();
		return (isFlashing) ? RenderEntityBomb.flashTextures[i] : RenderEntityBomb.bombTextures[i];
	}
}
