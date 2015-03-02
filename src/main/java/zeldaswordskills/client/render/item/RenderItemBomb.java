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

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.IItemRenderer;

import org.lwjgl.opengl.GL11;

import zeldaswordskills.api.entity.BombType;
import zeldaswordskills.client.model.ModelBomb;
import zeldaswordskills.client.render.entity.RenderEntityBomb;
import zeldaswordskills.item.ItemBomb;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderItemBomb implements IItemRenderer
{
	protected final ModelBomb model;

	private final Minecraft mc;

	// TODO expand to allow various spherical objects to be rendered with this class
	// TODO pass in the model to render and possibly size
	public RenderItemBomb() {
		model = new ModelBomb();
		mc = Minecraft.getMinecraft();
	}

	@Override
	public boolean handleRenderType(ItemStack item, ItemRenderType type) {
		return type != ItemRenderType.FIRST_PERSON_MAP && type != ItemRenderType.INVENTORY;
	}

	@Override
	public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper) {
		return type == ItemRenderType.ENTITY && (helper == ItemRendererHelper.ENTITY_BOBBING || helper == ItemRendererHelper.ENTITY_ROTATION);
	}

	@Override
	public void renderItem(ItemRenderType type, ItemStack stack, Object... data) {
		GL11.glPushMatrix();
		float size = 0.0475F;

		boolean isFlashing = (stack.hasTagCompound() && stack.getTagCompound().hasKey("time") && stack.getTagCompound().getInteger("time") % 13 > 10);
		mc.getTextureManager().bindTexture(getTexture(ItemBomb.getType(stack), isFlashing));
		if (isFlashing && type != ItemRenderType.EQUIPPED_FIRST_PERSON) {
			GL11.glScalef(1.2F, 1.2F, 1.2F);
		}

		if(data[1] instanceof EntityPlayer) {
			if (type == ItemRenderType.EQUIPPED) {
				GL11.glRotatef(268, 1F, 1F, 300F);
				GL11.glTranslatef(-0.5F, -0.2F, -0.1F);
			} else {
				GL11.glRotatef(210, 0F, 0F, 300F);
				GL11.glTranslatef(-0.5F, -0.9F, -0.5F);
			}
		} else {
			if (type == ItemRenderType.ENTITY) {
				GL11.glRotatef(150, 1F, 1F, 300F);
				GL11.glScalef(2.0F, 2.0F, 2.0F);
				GL11.glTranslatef(0.0F, -0.85F, 0.0F);
			} else {
				GL11.glRotatef(268, 1F, 1F, 300F);
				GL11.glTranslatef(-0.4F, -0.1F, -0.3F);
			}
		}

		model.render((Entity) data[1], 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, size);
		GL11.glPopMatrix();
	}

	private ResourceLocation getTexture(BombType type, boolean isFlashing) {
		int i = type.ordinal();
		return (isFlashing) ? RenderEntityBomb.flashTextures[i] : RenderEntityBomb.bombTextures[i];
	}
}
