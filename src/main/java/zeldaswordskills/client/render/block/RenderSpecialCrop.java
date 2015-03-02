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

package zeldaswordskills.client.render.block;

import net.minecraft.block.Block;
import net.minecraft.block.IGrowable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import cpw.mods.fml.client.registry.RenderingRegistry;

/**
 * 
 * Renders a crop-type block as an 'X' while growing and as '#' when fully mature.
 * 
 * A crop is considered 'mature' when bonemeal cannot be applied, i.e. when 
 * {@code IGrowable#func_149851_a} returns FALSE. If the block is not an
 * {@code IGrowable}, then metadata of 7 (ignoring bit 8) is considered 'mature'. 
 * 
 * Does not render anything in the inventory, as crops are expected to have a seed item.
 *
 */
public class RenderSpecialCrop implements ISimpleBlockRenderingHandler 
{
	public static final int renderId = RenderingRegistry.getNextAvailableRenderId();

	public RenderSpecialCrop() {}

	@Override
	public void renderInventoryBlock(Block block, int meta, int modelID, RenderBlocks renderer) {
		// No rendering - crops are not available as inventory items
	}

	@Override
	public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer) {
		boolean isMature = false;
		if (block instanceof IGrowable) {
			isMature = !((IGrowable) block).func_149851_a(Minecraft.getMinecraft().theWorld, x, y, z, true);
		} else {
			isMature = (world.getBlockMetadata(x, y, z) & 0x7) == 7;
		}
		if (isMature) {
			renderer.renderBlockCrops(block, x, y, z);
		} else {
			renderer.renderCrossedSquares(block, x, y, z);
		}
		return true;
	}

	@Override
	public boolean shouldRender3DInInventory(int modelId) {
		return false;
	}

	@Override
	public int getRenderId() {
		return renderId;
	}
}
