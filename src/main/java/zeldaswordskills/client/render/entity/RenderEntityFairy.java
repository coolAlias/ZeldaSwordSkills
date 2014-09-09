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

package zeldaswordskills.client.render.entity;

import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import zeldaswordskills.entity.EntityFairy;
import zeldaswordskills.lib.ModInfo;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderEntityFairy extends Render
{
	/** Copy of XP Orb texture */
	private static final ResourceLocation texture = new ResourceLocation(ModInfo.ID, "textures/entity/fairy.png");

	public RenderEntityFairy() {
		shadowSize = 0.15F;
		shadowOpaque = 0.75F;
	}

	public void renderFairy(EntityFairy fairy, double x, double y, double z, float yaw, float partialTick) {
		GL11.glPushMatrix();
		GL11.glTranslated(x, y, z);
		bindEntityTexture(fairy);
		int i = 10;
		float f2 = (i % 4 * 16 + 0) / 64.0F;
		float f3 = (i % 4 * 16 + 16) / 64.0F;
		float f4 = (i / 4 * 16 + 0) / 64.0F;
		float f5 = (i / 4 * 16 + 16) / 64.0F;
		float f6 = 1.0F;
		float f7 = 0.5F;
		float f8 = 0.25F;
		int j = fairy.getBrightnessForRender(partialTick);
		int k = j % 65536;
		int l = j / 65536;
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, k / 1.0F, l / 1.0F);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		float f9 = 255.0F;
		float f10 = (0x00EE00 + partialTick + fairy.ticksExisted) / 2.0F;
		l = (int)((MathHelper.sin(f10) + 1.0F) * 0.5F * f9);
		int i1 = (int)f9;
		int j1 = (int)((MathHelper.sin(f10 + 4.1887903F) + 1.0F) * 0.1F * f9);
		int k1 = l << 16 | i1 << 8 | j1;
		GL11.glRotatef(180.0F - renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
		GL11.glRotatef(-renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
		float f11 = 0.3F;
		GL11.glScalef(f11, f11, f11);
		Tessellator tessellator = Tessellator.instance;
		tessellator.startDrawingQuads();
		tessellator.setColorRGBA_I(k1, 128);
		tessellator.setNormal(0.0F, 1.0F, 0.0F);
		tessellator.addVertexWithUV((0.0F - f7), (0.0F - f8), 0.0D, f2, f5);
		tessellator.addVertexWithUV((f6 - f7), (0.0F - f8), 0.0D, f3, f5);
		tessellator.addVertexWithUV((f6 - f7), (1.0F - f8), 0.0D, f3, f4);
		tessellator.addVertexWithUV((0.0F - f7), (1.0F - f8), 0.0D, f2, f4);
		tessellator.draw();
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glDisable(GL12.GL_RESCALE_NORMAL);
		GL11.glPopMatrix();
	}

	@Override
	public void doRender(Entity entity, double x, double y, double z, float yaw, float partialTick) {
		renderFairy((EntityFairy) entity, x, y, z, yaw, partialTick);
	}

	@Override
	protected ResourceLocation getEntityTexture(Entity entity) {
		return texture;
	}
}
