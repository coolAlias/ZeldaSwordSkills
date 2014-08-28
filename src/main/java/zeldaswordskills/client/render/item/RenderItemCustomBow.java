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

import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Icon;
import net.minecraftforge.client.IItemRenderer;

import org.lwjgl.opengl.GL11;

import zeldaswordskills.ZSSMain;
import zeldaswordskills.handler.BattlegearEvents;
import zeldaswordskills.item.ItemHeroBow;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * 
 * Renders custom bow in 1st and 3rd person
 * 
 * Parts of this code were adapted from Battlegear2's BowRenderer, so
 * thanks to them for having an open-source project.
 *
 */
@SideOnly(Side.CLIENT)
public class RenderItemCustomBow implements IItemRenderer {

	public RenderItemCustomBow() {}

	@Override
	public boolean handleRenderType(ItemStack item, ItemRenderType type) {
		return type == ItemRenderType.EQUIPPED || type == ItemRenderType.EQUIPPED_FIRST_PERSON;
	}

	@Override
	public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper) {
		return false;
	}

	@Override
	public void renderItem(ItemRenderType type, ItemStack item, Object... data) {
		switch (type){
		case EQUIPPED_FIRST_PERSON:
			renderEquippedBow(item, (EntityLivingBase) data[1], true);
			break;
		case EQUIPPED:
			renderEquippedBow(item, (EntityLivingBase) data[1], false);
			break;
		default:
		}
	}

	private void renderEquippedBow(ItemStack stack, EntityLivingBase entity, boolean firstPerson) {
		GL11.glPushMatrix();
		Tessellator tessellator = Tessellator.instance;

		if (entity instanceof EntityPlayer) {
			if (!firstPerson) {
				GL11.glRotated(15.0D, 0.02D, 0.01D, 0.0D);
				GL11.glTranslated(0.1D, -0.3D, 0.2D);
			}
			EntityPlayer player = (EntityPlayer) entity;
			Icon icon = stack.getItem().getIcon(stack, 0, player, player.getItemInUse(), player.getItemInUseCount());
			ItemRenderer.renderItemIn2D(tessellator, icon.getMaxU(), icon.getMinV(), icon.getMinU(), icon.getMaxV(), icon.getIconWidth(), icon.getIconHeight(), 0.0625F);
			int useDuration = player.getItemInUseDuration();
			if (useDuration > 0) {
				int drawAmount = (useDuration > 17 ? 2 : (useDuration > 13 ? 1 : 0));
				ItemStack arrowStack = (stack.getItem() instanceof ItemHeroBow ? ((ItemHeroBow) stack.getItem()).getArrow(stack) : new ItemStack(Item.arrow));
				if (ZSSMain.isBG2Enabled) {
					ItemStack quiverArrow = BattlegearEvents.getQuiverArrow(stack, player);
					if (quiverArrow != null) {
						arrowStack = quiverArrow;
					}
				}
				if (arrowStack != null) {
					icon = arrowStack.getIconIndex();
					GL11.glPushMatrix();
					// Thanks to BG2 team for the translation calculations:
					GL11.glTranslatef(-(-3F+drawAmount)/16F, -(-3F+drawAmount)/16F, firstPerson?-0.5F/16F:0.5F/16F);
					ItemRenderer.renderItemIn2D(tessellator, icon.getMinU(), icon.getMinV(), icon.getMaxU(), icon.getMaxV(), icon.getIconWidth(), icon.getIconHeight(), 0.0625F);
					GL11.glPopMatrix();
				}
			}
		} else {
			Icon icon = stack.getItem().getIcon(stack, 0);
			ItemRenderer.renderItemIn2D(tessellator, icon.getMaxU(), icon.getMinV(), icon.getMinU(), icon.getMaxV(), icon.getIconWidth(), icon.getIconHeight(), 0.0625F);
		}

		GL11.glPopMatrix();
	}
}
