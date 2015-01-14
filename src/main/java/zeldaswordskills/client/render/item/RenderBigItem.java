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
import net.minecraft.util.IIcon;
import net.minecraftforge.client.IItemRenderer;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderBigItem implements IItemRenderer
{
	/** Standard item scale value is 0.5F */
	private final float scale;

	public RenderBigItem(float scale) {
		this.scale = scale;
	}

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
			renderEquippedItem(item, (EntityLivingBase) data[1], true);
			break;
		case EQUIPPED:
			renderEquippedItem(item, (EntityLivingBase) data[1], false);
			break;
		default:
		}
	}

	private void renderEquippedItem(ItemStack stack, EntityLivingBase entity, boolean firstPerson) {
		GL11.glPushMatrix();
		float f = scale;
		if (firstPerson) {
			f *= 1.75F;
			GL11.glTranslatef(-0.35F * scale, -0.125F * scale, 0.0F);
		} else {
			f *= (entity instanceof EntityPlayer ? 2.0F : 1.75F);
			GL11.glTranslatef(1.0F - f, -0.125F * scale, 0.05F * scale);
		}
		GL11.glScalef(f, f, f);
		IIcon icon = stack.getItem().getIcon(stack, 0);
		Tessellator tessellator = Tessellator.instance;
		ItemRenderer.renderItemIn2D(tessellator, icon.getMaxU(), icon.getMinV(), icon.getMinU(), icon.getMaxV(), icon.getIconWidth(), icon.getIconHeight(), 0.0625F);
		GL11.glPopMatrix();
	}
}
