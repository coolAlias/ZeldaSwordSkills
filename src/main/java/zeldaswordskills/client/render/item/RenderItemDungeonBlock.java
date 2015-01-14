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
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.IItemRenderer;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import zeldaswordskills.block.ZSSBlocks;
import zeldaswordskills.item.ItemDungeonBlock;

@SideOnly(Side.CLIENT)
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
			if (block == ZSSBlocks.dungeonStone) {
				block = (stack.getItemDamage() == 0 ? Blocks.stone : Blocks.obsidian);
			} else if (block == ZSSBlocks.dungeonCore) {
				block = (stack.getItemDamage() == 0 ? Blocks.mossy_cobblestone : Blocks.stonebrick);
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
				/*
				// When not using render helper:
				GL11.glPushMatrix();
				if (type == ItemRenderType.EQUIPPED_FIRST_PERSON) {
					// looks right, but swing animation is oddly stunted:
					GL11.glRotatef(30.0F, 0.0F, 0.0F, 1.0F);
					GL11.glRotatef(40.0F, 0.0F, 1.0F, 0.0F);
					GL11.glTranslatef(0.75F, -0.625F, 1.25F);
					GL11.glTranslatef(0.5F, 0.5F, 0.5F);
				} else {
					GL11.glRotatef(-46.0F, 0.0F, 1.0F, 0.0F);
					GL11.glRotatef(17.5F, 1.0F, 0.0F, 0.0F);
					GL11.glRotatef(15.0F, 0.0F, 0.0F, 1.0F);
					GL11.glTranslatef(0.6F, -0.15F, -0.65F);
					GL11.glScalef(0.675F, 0.675F, 0.675F);
					GL11.glTranslatef(0.5F, 0.5F, 0.5F);
				}
				blockRenderer.renderBlockAsItem(block, ((ItemDungeonBlock) item).getMetaFromStack(stack), 1.0F);
				GL11.glPopMatrix();
				 */
			}
		}
	}
}
