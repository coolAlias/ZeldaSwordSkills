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

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.IItemRenderer;

import org.lwjgl.opengl.GL11;

import zeldaswordskills.client.model.ModelHookShot;
import zeldaswordskills.ref.ModInfo;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderItemHookShot implements IItemRenderer
{
	private static final ResourceLocation texture = new ResourceLocation(ModInfo.ID, "textures/entity/hookshot.png");

	private final ModelHookShot model = new ModelHookShot();

	private final Minecraft mc;

	public RenderItemHookShot() {
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
	public void renderItem(ItemRenderType type, ItemStack item, Object... data) {
		GL11.glPushMatrix();
		mc.renderEngine.bindTexture(texture);
		if(data[1] instanceof EntityPlayer) {
			if (type == ItemRenderType.EQUIPPED) {
				// par2 is rotation, par3 is up-down, par4 left-right
				GL11.glRotatef(90, 0.5F, 0.7F, 0.1F);
				GL11.glScalef(1.5F, 1.5F, 1.5F);
				GL11.glTranslatef(0.9F, 0F, 0.6F);
				/*
				CLOSER (but pointing too much towards the ground)
				GL11.glRotatef(90, 1F, 0.15F, 0.1F);
				GL11.glScalef(1.5F, 1.5F, 1.5F);
				GL11.glTranslatef(0.9F, 0F, 0.6F);

				PRETTY CLOSE:
				GL11.glRotatef(90, 1.8F, 0.6F, 0.6F);
				GL11.glScalef(1.5F, 1.5F, 1.5F);
				GL11.glTranslatef(0.9F, 0F, 0.6F);
				BASELINE:
				GL11.glRotatef(90, 1F, 0F, 1F); // 3 parameter is vertical rotation
				GL11.glScalef(1.5F, 1.5F, 1.5F);
				GL11.glTranslatef(0.7F, -0.2F, 1F);
				 */
			} else {
				GL11.glRotatef(90, 1F, -0.5F, 0.5F);
				GL11.glScalef(1.7F, 1.7F, 1.7F);
				GL11.glTranslatef(0.8F, -0.15F, 0.45F);
				/*
				PRETTY DANG GOOD:
				GL11.glRotatef(90, 1F, -0.5F, 0.5F);
				GL11.glScalef(1.7F, 1.7F, 1.7F);
				GL11.glTranslatef(0.8F, -0.15F, 0.45F);

				BASELINE:
				// translatef in order of lower-higher
				// par2 is left-right, par3 is forward-back
				GL11.glRotatef(90, 1F, -0.5F, 0.5F);
				GL11.glScalef(1.7F, 1.7F, 1.7F);
				GL11.glTranslatef(0.5F, -0.4F, 0.7F);
				 */
			}
		} else {
			// Entity rendering is working:
			if (type == ItemRenderType.ENTITY) {
				GL11.glRotatef(90, 0F, 0F, 1F);
				GL11.glScalef(1.5F, 1.5F, 1.5F);
				GL11.glTranslatef(0.7F, -0.2F, 1.35F);
			} else {
				// This one I have no idea if it's working or not; most likely not
				GL11.glRotatef(268, 1F, 1F, 300F);
				GL11.glTranslatef(-0.4F, -0.1F, -0.3F);
			}
		}
		model.render((Entity) data[1], 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F);
		GL11.glPopMatrix();
	}
}
