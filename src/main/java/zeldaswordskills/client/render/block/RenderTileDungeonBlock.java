/**
    Copyright (C) <2014> <coolAlias>

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

package zeldaswordskills.client.render.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockGrass;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.world.IBlockAccess;
import zeldaswordskills.block.BlockSecretStone;
import zeldaswordskills.block.ZSSBlocks;
import zeldaswordskills.block.tileentity.TileEntityDungeonBlock;
import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import cpw.mods.fml.client.registry.RenderingRegistry;

public class RenderTileDungeonBlock implements ISimpleBlockRenderingHandler
{
	public static final int renderId = RenderingRegistry.getNextAvailableRenderId();

	public RenderTileDungeonBlock() {}

	@Override
	public void renderInventoryBlock(Block block, int metadata, int modelID, RenderBlocks renderer) {}

	@Override
	public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer) {
		int meta = world.getBlockMetadata(x, y, z);
		TileEntity te = world.getBlockTileEntity(x, y, z);
		if (te instanceof TileEntityDungeonBlock) {
			block = ((TileEntityDungeonBlock) te).getRenderBlock();
			if (block == null || block == ZSSBlocks.secretStone || block == ZSSBlocks.dungeonCore) {
				int blockId = BlockSecretStone.getIdFromMeta(meta);
				block = (blockId > 0 ? Block.blocksList[blockId] : null);
				meta = 0;
			} else {
				meta = ((TileEntityDungeonBlock) te).getRenderMetadata();
			}
		}
		if (block == null || (!block.isOpaqueCube() && block != Block.ice)) {
			return false;
		}
		// ideally, all blocks will simply render right here by render type
		if (meta == 0 || block == Block.ice) {
			return renderer.renderBlockByRenderType(block, x, y, z);
		}
		// the following is for backwards compatibility with meta-based secret stone blocks
		// it can be removed if the structure generation code ever transitions to tile entity
		boolean rendered = false;
		Icon icon = block.getIcon(0, meta);
		if (icon != null) {
			int l = block.colorMultiplier(world, x, y, z);
			float r = (float)(l >> 16 & 255) / 255.0F;
			float g = (float)(l >> 8 & 255) / 255.0F;
			float b = (float)(l & 255) / 255.0F;
			float f3 = 0.5F;
			float f4 = 1.0F;
			float f5 = 0.8F;
			float f6 = 0.6F;
			float f7 = f4 * r;
			float f8 = f4 * g;
			float f9 = f4 * b;
			float f10 = f3;
			float f11 = f5;
			float f12 = f6;
			float f13 = f3;
			float f14 = f5;
			float f15 = f6;
			float f16 = f3;
			float f17 = f5;
			float f18 = f6;

			if (block != Block.grass) {
				f10 = f3 * r;
				f11 = f5 * r;
				f12 = f6 * r;
				f13 = f3 * g;
				f14 = f5 * g;
				f15 = f6 * g;
				f16 = f3 * b;
				f17 = f5 * b;
				f18 = f6 * b;
			}
			Tessellator tessellator = Tessellator.instance;
			tessellator.setColorOpaque_F(r, g, b);
			tessellator.setBrightness(983055);
			int brightness = block.getMixedBrightnessForBlock(world, x, y, z);

			if (renderer.renderAllFaces || block.shouldSideBeRendered(world, x, y - 1, z, 0)) {
				tessellator.setBrightness(renderer.renderMinY > 0.0D ? brightness : block.getMixedBrightnessForBlock(world, x, y - 1, z));
				tessellator.setColorOpaque_F(f10, f13, f16);
				renderer.renderFaceYNeg(block, x, y, z, block.getIcon(0, meta));
				rendered = true;
			}
			if (renderer.renderAllFaces || block.shouldSideBeRendered(world, x, y + 1, z, 1)) {
				tessellator.setBrightness(renderer.renderMaxY < 1.0D ? brightness : block.getMixedBrightnessForBlock(world, x, y + 1, z));
				tessellator.setColorOpaque_F(f7, f8, f9);
				renderer.renderFaceYPos(block, x, y, z, block.getIcon(1, meta));
				rendered = true;
			}
			if (renderer.renderAllFaces || block.shouldSideBeRendered(world, x, y, z - 1, 2)) {
				tessellator.setBrightness(renderer.renderMinZ > 0.0D ? brightness : block.getMixedBrightnessForBlock(world, x, y, z - 1));
				tessellator.setColorOpaque_F(f11, f14, f17);
				icon = block.getIcon(2, meta);
				renderer.renderFaceZNeg(block, x, y, z, icon);
				if (RenderBlocks.fancyGrass && icon.getIconName().equals("grass_side") && !renderer.hasOverrideBlockTexture()) {
					tessellator.setColorOpaque_F(f11 * r, f14 * g, f17 * b);
					renderer.renderFaceZNeg(block, x, y, z, BlockGrass.getIconSideOverlay());
				}
				rendered = true;
			}
			if (renderer.renderAllFaces || block.shouldSideBeRendered(world, x, y, z + 1, 3)) {
				tessellator.setBrightness(renderer.renderMaxZ < 1.0D ? brightness : block.getMixedBrightnessForBlock(world, x, y, z + 1));
				tessellator.setColorOpaque_F(f11, f14, f17);
				icon = block.getIcon(3, meta);
				renderer.renderFaceZPos(block, x, y, z, icon);
				if (RenderBlocks.fancyGrass && icon.getIconName().equals("grass_side") && !renderer.hasOverrideBlockTexture()) {
					tessellator.setColorOpaque_F(f11 * r, f14 * g, f17 * b);
					renderer.renderFaceZPos(block, x, y, z, BlockGrass.getIconSideOverlay());
				}
				rendered = true;
			}
			if (renderer.renderAllFaces || block.shouldSideBeRendered(world, x - 1, y, z, 4)) {
				tessellator.setBrightness(renderer.renderMinX > 0.0D ? brightness : block.getMixedBrightnessForBlock(world, x - 1, y, z));
				tessellator.setColorOpaque_F(f12, f15, f18);
				icon = block.getIcon(4, meta);
				renderer.renderFaceXNeg(block, x, y, z, icon);
				if (RenderBlocks.fancyGrass && icon.getIconName().equals("grass_side") && !renderer.hasOverrideBlockTexture()) {
					tessellator.setColorOpaque_F(f12 * r, f15 * g, f18 * b);
					renderer.renderFaceXNeg(block, x, y, z, BlockGrass.getIconSideOverlay());
				}
				rendered = true;
			}
			if (renderer.renderAllFaces || block.shouldSideBeRendered(world, x + 1, y, z, 5)) {
				tessellator.setBrightness(renderer.renderMaxX < 1.0D ? brightness : block.getMixedBrightnessForBlock(world, x + 1, y, z));
				tessellator.setColorOpaque_F(f12, f15, f18);
				icon = block.getIcon(5, meta);
				renderer.renderFaceXPos(block, x, y, z, icon);
				if (RenderBlocks.fancyGrass && icon.getIconName().equals("grass_side") && !renderer.hasOverrideBlockTexture()) {
					tessellator.setColorOpaque_F(f12 * r, f15 * g, f18 * b);
					renderer.renderFaceXPos(block, x, y, z, BlockGrass.getIconSideOverlay());
				}
				rendered = true;
			}
		}
		return rendered;
	}

	@Override
	public boolean shouldRender3DInInventory() {
		return false;
	}

	@Override
	public int getRenderId() {
		return renderId;
	}
}
