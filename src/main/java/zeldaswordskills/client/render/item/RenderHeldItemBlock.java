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

import zeldaswordskills.item.ItemHeldBlock;

public class RenderHeldItemBlock implements IItemRenderer
{
	private RenderBlocks blockRenderer;

	public RenderHeldItemBlock() {
		blockRenderer = new RenderBlocks();
	}

	@Override
	public boolean handleRenderType(ItemStack item, ItemRenderType type) {
		return true;
	}

	@Override
	public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper) {
		return false;
	}

	@Override
	public void renderItem(ItemRenderType type, ItemStack stack, Object... data) {
		Item item = stack.getItem();
		if (item instanceof ItemHeldBlock) {
			Block block = ((ItemHeldBlock) item).getBlockFromStack(stack);
			if (block != null && stack.getItemSpriteNumber() == 0 && RenderBlocks.renderItemIn3d(block.getRenderType())) {
				if (type == ItemRenderType.INVENTORY) {
					GL11.glPushMatrix();
					GL11.glEnable(GL11.GL_LIGHTING);
					GL11.glTranslatef(-2F, 3F, 0.0F);
					GL11.glScalef(10.0F, 10.0F, 10.0F);
					GL11.glTranslatef(1.0F, 0.5F, 1.0F);
					GL11.glScalef(1.0F, 1.0F, -1.0F);
					GL11.glRotatef(210.0F, 1.0F, 0.0F, 0.0F);
					GL11.glRotatef(45.0F, 0.0F, 1.0F, 0.0F);
					int i1 = Item.getItemFromBlock(block).getColorFromItemStack(stack, 0);
					float f = (float)(i1 >> 16 & 255) / 255.0F;
					float f1 = (float)(i1 >> 8 & 255) / 255.0F;
					float f2 = (float)(i1 & 255) / 255.0F;
					GL11.glColor4f(f, f1, f2, 1.0F);
					GL11.glRotatef(-90.0F, 0.0F, 1.0F, 0.0F);
					blockRenderer.renderBlockAsItem(block, ((ItemHeldBlock) item).getMetaFromStack(stack), 1.0F);
					GL11.glPopMatrix();
				} else {
					GL11.glPushMatrix();
					if (type == ItemRenderType.EQUIPPED_FIRST_PERSON) {
						GL11.glTranslatef(1.0F, 0.325F, -0.875F);
						GL11.glScalef(1.25F, 1.25F, 1.25F);
					} else {
						GL11.glRotatef(45.0F, 0.825F, 0.325F, 0.25F);
						GL11.glRotatef(15.0F, -0.125F, 0.0F, 0.0F);
						GL11.glTranslatef(1.1F, 0.625F, -0.5F);
						GL11.glScalef(1.325F, 1.325F, 1.325F);
					}
					blockRenderer.renderBlockAsItem(block, ((ItemHeldBlock) item).getMetaFromStack(stack), 1.0F);
					GL11.glPopMatrix();
				}
			}
		}
	}
}
