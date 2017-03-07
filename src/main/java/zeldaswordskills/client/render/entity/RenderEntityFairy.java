/**
    Copyright (C) <2017> <coolAlias>

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

import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import zeldaswordskills.entity.passive.EntityFairy;
import zeldaswordskills.ref.ModInfo;

@SideOnly(Side.CLIENT)
public class RenderEntityFairy extends Render<EntityFairy>
{
	/** Copy of XP Orb texture */
	private static final ResourceLocation texture = new ResourceLocation(ModInfo.ID, "textures/entity/fairy.png");

	public RenderEntityFairy(RenderManager renderManager) {
		super(renderManager);
		shadowSize = 0.15F;
		shadowOpaque = 0.75F;
	}

	@Override
	public void doRender(EntityFairy fairy, double x, double y, double z, float yaw, float partialTick) {
		GlStateManager.pushMatrix();
		GlStateManager.translate(x, y, z);
		bindEntityTexture(fairy);
		int i = 10;
		float f0 = (float)(i % 4 * 16 + 0) / 64.0F;
		float f1 = (float)(i % 4 * 16 + 16) / 64.0F;
		float f2 = (float)(i / 4 * 16 + 0) / 64.0F;
		float f3 = (float)(i / 4 * 16 + 16) / 64.0F;
		float f4 = 1.0F;
		float f5 = 0.5F;
		float f6 = 0.25F;
		int j = fairy.getBrightnessForRender(partialTick);
		int k = j % 65536;
		int l = j / 65536;
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, k / 1.0F, l / 1.0F);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		float f8 = 255.0F;
		float f9 = (0x00EE00 + partialTick + fairy.ticksExisted) / 2.0F;
		l = (int)((MathHelper.sin(f9) + 1.0F) * 0.5F * f8);
		int i1 = (int)f8;
		int j1 = (int)((MathHelper.sin(f9 + 4.1887903F) + 1.0F) * 0.1F * f8);
		GlStateManager.rotate(180.0F - renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
		GlStateManager.rotate(-renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
		float f7 = 0.3F;
		GlStateManager.scale(f7, f7, f7);
		Tessellator tessellator = Tessellator.getInstance();
		WorldRenderer renderer = tessellator.getWorldRenderer();
		renderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
		renderer.pos((0.0F - f5), (0.0F - f6), 0.0D).tex(f0, f3).color(l, i1, j1, 128).normal(0.0F, 1.0F, 0.0F).endVertex();
		renderer.pos((f4 - f5), (0.0F - f6), 0.0D).tex(f1, f3).color(l, i1, j1, 128).normal(0.0F, 1.0F, 0.0F).endVertex();
		renderer.pos((f4 - f5), (1.0F - f6), 0.0D).tex(f1, f2).color(l, i1, j1, 128).normal(0.0F, 1.0F, 0.0F).endVertex();
		renderer.pos((0.0F - f5), (1.0F - f6), 0.0D).tex(f0, f2).color(l, i1, j1, 128).normal(0.0F, 1.0F, 0.0F).endVertex();
		tessellator.draw();
		GlStateManager.disableBlend();
		GlStateManager.disableRescaleNormal();
		GlStateManager.popMatrix();
		super.doRender(fairy, x, y, z, yaw, partialTick);
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityFairy entity) {
		return texture;
	}

	public static class Factory implements IRenderFactory<EntityFairy> {
		@Override
		public Render<? super EntityFairy> createRenderFor(RenderManager manager) {
			return new RenderEntityFairy(manager);
		}
	}
}
