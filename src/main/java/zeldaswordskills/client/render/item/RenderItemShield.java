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

import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Icon;
import net.minecraftforge.client.IItemRenderer;

import org.lwjgl.opengl.GL11;

import zeldaswordskills.item.ItemZeldaShield;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * 
 * @author based on the ShieldRenderer code from Battlegear2
 *
 */
@SideOnly(Side.CLIENT)
public class RenderItemShield implements IItemRenderer
{
	public RenderItemShield() {}

	@Override
	public boolean handleRenderType(ItemStack item, ItemRenderType type) {
		return type != ItemRenderType.FIRST_PERSON_MAP && type != ItemRenderType.INVENTORY;
	}

	@Override
	public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper) {
		return type == ItemRenderType.ENTITY && (helper == ItemRendererHelper.ENTITY_BOBBING || helper == ItemRendererHelper.ENTITY_ROTATION);
	}

	@Override
	public void renderItem(ItemRenderType type, ItemStack item, Object... data) {
		if (item.getItem() instanceof ItemZeldaShield) {
			ItemZeldaShield shield = (ItemZeldaShield) item.getItem();
			GL11.glPushMatrix();
			Tessellator tessellator = Tessellator.instance;
			Icon icon = shield.getIconIndex(item);

			switch (type){
			case ENTITY:
				GL11.glTranslatef(-0.5F, -0.25F, 0);
				GL11.glColor3f(1.0F, 1.0F, 1.0F);
				ItemRenderer.renderItemIn2D(tessellator,
						icon.getMaxU(),
						icon.getMinV(),
						icon.getMinU(),
						icon.getMaxV(),
						icon.getIconWidth(),
						icon.getIconHeight(), 0.0625F);
				GL11.glTranslatef(0, 0, -0.0625F);
				icon = shield.getBackIcon();
				ItemRenderer.renderItemIn2D(tessellator,
						icon.getMaxU(),
						icon.getMinV(),
						icon.getMinU(),
						icon.getMaxV(),
						icon.getIconWidth(),
						icon.getIconHeight(), 1F/256F);
				break;
			case EQUIPPED:
			case EQUIPPED_FIRST_PERSON:
				GL11.glColor3f(1.0F, 1.0F, 1.0F);
				// rule out possibility of being sheathed on back or equipped in battlegear slots
				if (data[1] instanceof EntityPlayer && ((EntityPlayer) data[1]).getHeldItem() != null
						&& ((EntityPlayer) data[1]).getHeldItem().getItem() == item.getItem())
				{
					boolean flag = ((EntityPlayer) data[1]).getItemInUse() != null;
					if (type == ItemRenderType.EQUIPPED) {
						GL11.glRotatef(90, 0.0F, -1.0F, 0.0F);
						GL11.glRotatef(110, 1.0F, 0.0F, 0.0F);
						GL11.glRotatef((flag ? 90 : 60), 0.0F, 1.0F, 0.0F);
						GL11.glRotatef(15, 0.0F, 0.0F, -1.0F);
						if (flag) {
							GL11.glTranslatef(-0.45F, -1.2F, -0.25F);
						} else {
							// For Flush with arm when unarmored, simply use: GL11.glTranslatef(-0.375F, -1.1F, -0.135F);
							GL11.glRotatef(15, -1.0F, 0.0F, 0.0F);
							GL11.glTranslatef(-0.385F, -1.1F, -0.325F);
						}
					} else {
						GL11.glRotatef(90, 0.0F, -1.0F, 0.0F);
						GL11.glRotatef(45, 1.0F, 0.0F, 0.0F);
						GL11.glRotatef(30, 0.0F, -1.0F, 0.0F);
						GL11.glTranslatef(-0.75F, -0.5F, -0.25F);
						if (flag) {
							GL11.glRotatef(120, -1.0F, 0.0F, 0.0F);
							GL11.glRotatef(90, 0.0F, 1.0F, 0.0F);
							GL11.glRotatef(180, 1.0F, 0.0F, 0.0F);
							GL11.glTranslatef(-0.8F, -0.325F, 0.25F);
						}
					}
				}
				ItemRenderer.renderItemIn2D(tessellator,
						icon.getMaxU(),
						icon.getMinV(),
						icon.getMinU(),
						icon.getMaxV(),
						icon.getIconWidth(),
						icon.getIconHeight(), 0.0625F);
				GL11.glTranslatef(0, 0, 1F/256F);
				icon = shield.getBackIcon();
				ItemRenderer.renderItemIn2D(tessellator,
						icon.getMaxU(),
						icon.getMinV(),
						icon.getMinU(),
						icon.getMaxV(),
						icon.getIconWidth(),
						icon.getIconHeight(), 1F/256F);
				break;
			default:
			}

			GL11.glPopMatrix();
		}
	}
}
