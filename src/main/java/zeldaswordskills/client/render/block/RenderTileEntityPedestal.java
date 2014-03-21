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

package zeldaswordskills.client.render.block;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import zeldaswordskills.block.tileentity.TileEntityPedestal;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderTileEntityPedestal extends TileEntitySpecialRenderer
{
	protected static final ResourceLocation glint = new ResourceLocation("textures/misc/enchanted_item_glint.png");

	protected final TextureManager textureManager = Minecraft.getMinecraft().getTextureManager();
	
	public RenderTileEntityPedestal() {}

	@Override
	public void renderTileEntityAt(TileEntity te, double dx, double dy, double dz, float f) {
		renderPedestal((TileEntityPedestal) te, dx, dy, dz, f);
	}

	public void renderPedestal(TileEntityPedestal pedestal, double dx, double dy, double dz, float f) {
		ItemStack sword = pedestal.getSword();
		if (sword != null) {
			GL11.glPushMatrix();
			GL11.glTranslated(dx + 0.5D, dy + 0.9D, dz + 0.5D);
			textureManager.bindTexture(textureManager.getResourceLocation(sword.getItemSpriteNumber()));
			Tessellator tessellator = Tessellator.instance;
			Icon icon = sword.getItem().getIconFromDamage(sword.getItemDamage());
			if (icon != null) {
				float minU = icon.getMinU();
				float maxU = icon.getMaxU();
				float minV = icon.getMinV();
				float maxV = icon.getMaxV();
				GL11.glRotatef(pedestal.getOrientation() == 0 ? 0F : 90F, 0.0F, 1.0F, 0.0F);
				GL11.glRotatef(135.0F, 0.0F, 0.0F, 1.0F);
				GL11.glTranslatef(-0.5F, -0.5F, 0.03125F);
				ItemRenderer.renderItemIn2D(tessellator, maxU, minV, minU, maxV, icon.getIconWidth(), icon.getIconHeight(), 0.0625F);
				if (sword.hasEffect(0)) {
					GL11.glDepthFunc(GL11.GL_EQUAL);
					GL11.glDisable(GL11.GL_LIGHTING);
					textureManager.bindTexture(glint);
					GL11.glEnable(GL11.GL_BLEND);
					GL11.glBlendFunc(GL11.GL_SRC_COLOR, GL11.GL_ONE);
					float f7 = 0.76F;
					GL11.glColor4f(0.5F * f7, 0.25F * f7, 0.8F * f7, 1.0F);
					GL11.glMatrixMode(GL11.GL_TEXTURE);
					GL11.glPushMatrix();
					float f8 = 0.125F;
					GL11.glScalef(f8, f8, f8);
					float f9 = (float)(Minecraft.getSystemTime() % 3000L) / 3000.0F * 8.0F;
					GL11.glTranslatef(f9, 0.0F, 0.0F);
					GL11.glRotatef(-50.0F, 0.0F, 0.0F, 1.0F);
					ItemRenderer.renderItemIn2D(tessellator, 0.0F, 0.0F, 1.0F, 1.0F, 256, 256, 0.0625F);
					GL11.glPopMatrix();
					GL11.glPushMatrix();
					GL11.glScalef(f8, f8, f8);
					f9 = (float)(Minecraft.getSystemTime() % 4873L) / 4873.0F * 8.0F;
					GL11.glTranslatef(-f9, 0.0F, 0.0F);
					GL11.glRotatef(10.0F, 0.0F, 0.0F, 1.0F);
					ItemRenderer.renderItemIn2D(tessellator, 0.0F, 0.0F, 1.0F, 1.0F, 256, 256, 0.0625F);
					GL11.glPopMatrix();
					GL11.glMatrixMode(GL11.GL_MODELVIEW);
					GL11.glDisable(GL11.GL_BLEND);
					GL11.glEnable(GL11.GL_LIGHTING);
					GL11.glDepthFunc(GL11.GL_LEQUAL);
				}
				GL11.glPopMatrix();
			}
		}
	}
}
