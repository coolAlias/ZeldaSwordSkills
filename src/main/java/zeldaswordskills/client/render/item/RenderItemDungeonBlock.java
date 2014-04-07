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

package zeldaswordskills.client.render.item;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.IItemRenderer;

import org.lwjgl.opengl.GL11;

import zeldaswordskills.block.ZSSBlocks;
import zeldaswordskills.item.ItemDungeonBlock;

public class RenderItemDungeonBlock implements IItemRenderer
{
	private RenderBlocks blockRenderer;

	public RenderItemDungeonBlock() {
		blockRenderer = new RenderBlocks();
	}

	@Override
	public boolean handleRenderType(ItemStack item, ItemRenderType type) {
		return true;
	}

	@Override
	public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper) {
		return true;
	}

	@Override
	public void renderItem(ItemRenderType type, ItemStack stack, Object... data) {
		Item item = stack.getItem();
		if (item instanceof ItemDungeonBlock) {
			Block block = ((ItemDungeonBlock) item).getBlockFromStack(stack);
			if (block == ZSSBlocks.secretStone) {
				block = (stack.getItemDamage() == 0 ? Block.stone : Block.obsidian);
			} else if (block == ZSSBlocks.dungeonCore) {
				block = (stack.getItemDamage() == 0 ? Block.cobblestoneMossy : Block.stoneBrick);
			}
			if (block != null && stack.getItemSpriteNumber() == 0 && RenderBlocks.renderItemIn3d(block.getRenderType())) {
				GL11.glPushMatrix();
				switch(type) {
				case EQUIPPED_FIRST_PERSON:
					GL11.glTranslatef(0.5F, 0.5F, 0.5F);
					break;
				case EQUIPPED:
					GL11.glTranslatef(0.5F, 0.5F, 0.5F);
					break;
				default:
				}
				blockRenderer.renderBlockAsItem(block, ((ItemDungeonBlock) item).getMetaFromStack(stack), 1.0F);
				GL11.glPopMatrix();
			}
		}
	}
}
