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
import net.minecraft.item.ItemStack;
import net.minecraft.util.Icon;
import net.minecraftforge.client.IItemRenderer;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * 
 * Renders custom bow in 1st and 3rd person, adding FOV effect (TODO)
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
		if (entity instanceof EntityPlayer) {
			GL11.glPushMatrix();
			if (!firstPerson) {
				GL11.glRotated(15.0D, 0.02D, 0.01D, 0.0D);
				GL11.glTranslated(0.1D, -0.3D, 0.2D);
			}
			EntityPlayer player = (EntityPlayer) entity;
			Icon icon = stack.getItem().getIcon(stack, 0, player, player.getItemInUse(), player.getItemInUseCount());
			Tessellator tessellator = Tessellator.instance;
			ItemRenderer.renderItemIn2D(tessellator, icon.getMaxU(), icon.getMinV(), icon.getMinU(), icon.getMaxV(), icon.getIconWidth(), icon.getIconHeight(), 0.0625F);
			GL11.glPopMatrix();
		}
	}
}
