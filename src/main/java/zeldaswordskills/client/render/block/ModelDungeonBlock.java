/**
 * Copyright (C) <2019> <coolAlias>
 *
 * This file is part of coolAlias' Zelda Sword Skills Minecraft Mod; as such,
 * you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package zeldaswordskills.client.render.block;

import java.util.List;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.model.ISmartBlockModel;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import zeldaswordskills.block.BlockDungeonStone;
import zeldaswordskills.ref.ModInfo;

@SuppressWarnings("deprecation")
@SideOnly(Side.CLIENT)
public class ModelDungeonBlock implements ISmartBlockModel
{
	/** Default resource location */
	public static final ModelResourceLocation resource = new ModelResourceLocation(ModInfo.ID + ":dungeon_block");
	private final IBakedModel defaultModel;
	private final Minecraft mc;

	public ModelDungeonBlock(IBakedModel model) {
		defaultModel = model;
		mc = Minecraft.getMinecraft();
	}

	@Override
	public IBakedModel handleBlockState(IBlockState state) {
		if (state instanceof IExtendedBlockState) {
			IBlockState renderState = ((IExtendedBlockState) state).getValue(BlockDungeonStone.RENDER_BLOCK);
			if (renderState != null) {
				IBakedModel renderModel = mc.getBlockRendererDispatcher().getBlockModelShapes().getModelForState(renderState);
				if (renderModel instanceof ISmartBlockModel) {
					renderModel = ((ISmartBlockModel) renderModel).handleBlockState(renderState);
				}
				return renderModel;
			}
		}
		return defaultModel;
	}

	@Override
	public TextureAtlasSprite getParticleTexture() {
		return defaultModel.getParticleTexture(); // needed for particles
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
}
