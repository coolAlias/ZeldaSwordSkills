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
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import zeldaswordskills.entity.projectile.EntityHookShot;

/**
 * 
 * A mish-mash of arrow and leash rendering, so... holy $hit it actually worked!!!
 *
 */
@SideOnly(Side.CLIENT)
public class RenderEntityHookShot extends Render<EntityHookShot>
{
	/** Use arrow texture for now */
	private static final ResourceLocation arrowTexture = new ResourceLocation("textures/entity/arrow.png");

	public RenderEntityHookShot(RenderManager renderManager) {
		super(renderManager);
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityHookShot entity) {
		return arrowTexture;
	}

	public void doRender(EntityHookShot entity, double x, double y, double z, float yaw, float partialTick) {
		bindEntityTexture(entity);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.pushMatrix();
		GlStateManager.translate(x, y, z);
		GlStateManager.rotate(entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTick - 90.0F, 0.0F, 1.0F, 0.0F);
		GlStateManager.rotate(entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTick, 0.0F, 0.0F, 1.0F);
		Tessellator tessellator = Tessellator.getInstance();
		WorldRenderer renderer = tessellator.getWorldRenderer();
		byte b0 = 0;
		float f2 = 0.0F;
		float f3 = 0.5F;
		float f4 = (float)(0 + b0 * 10) / 32.0F;
		float f5 = (float)(5 + b0 * 10) / 32.0F;
		float f6 = 0.0F;
		float f7 = 0.15625F;
		float f8 = (float)(5 + b0 * 10) / 32.0F;
		float f9 = (float)(10 + b0 * 10) / 32.0F;
		float f10 = 0.05625F;
		GlStateManager.enableRescaleNormal();
		GlStateManager.rotate(45.0F, 1.0F, 0.0F, 0.0F);
		GlStateManager.scale(f10, f10, f10);
		GlStateManager.translate(-4.0F, 0.0F, 0.0F);
		GL11.glNormal3f(f10, 0.0F, 0.0F);
		renderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		renderer.pos(-7.0D, -2.0D, -2.0D).tex((double)f4, (double)f6).endVertex();
		renderer.pos(-7.0D, -2.0D, 2.0D).tex((double)f5, (double)f6).endVertex();
		renderer.pos(-7.0D, 2.0D, 2.0D).tex((double)f5, (double)f7).endVertex();
		renderer.pos(-7.0D, 2.0D, -2.0D).tex((double)f4, (double)f7).endVertex();
		tessellator.draw();
		GL11.glNormal3f(-f10, 0.0F, 0.0F);
		renderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		renderer.pos(-7.0D, 2.0D, -2.0D).tex((double)f6, (double)f8).endVertex();
		renderer.pos(-7.0D, 2.0D, 2.0D).tex((double)f7, (double)f8).endVertex();
		renderer.pos(-7.0D, -2.0D, 2.0D).tex((double)f7, (double)f9).endVertex();
		renderer.pos(-7.0D, -2.0D, -2.0D).tex((double)f6, (double)f9).endVertex();
		tessellator.draw();
		for (int i = 0; i < 4; ++i) {
			GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
			GL11.glNormal3f(0.0F, 0.0F, f10);
			renderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
			renderer.pos(-8.0D, -2.0D, 0.0D).tex((double)f2, (double)f4).endVertex();
			renderer.pos(8.0D, -2.0D, 0.0D).tex((double)f3, (double)f4).endVertex();
			renderer.pos(8.0D, 2.0D, 0.0D).tex((double)f3, (double)f5).endVertex();
			renderer.pos(-8.0D, 2.0D, 0.0D).tex((double)f2, (double)f5).endVertex();
			tessellator.draw();
		}
		GlStateManager.disableRescaleNormal();
		GlStateManager.popMatrix();
		renderLeash(entity, x, y, z, yaw, partialTick);
		super.doRender(entity, x, y, z, yaw, partialTick);
	}

	/**
	 * Copied from RenderLiving: Gets the value between start and end according to pct
	 */
	private double interpolateValue(double start, double end, double pct) {
		return start + (end - start) * pct;
	}

	/**
	 * Copied from RenderLiving#renderLeash
	 */
	protected void renderLeash(EntityHookShot hookshot, double x, double y, double z, float yaw, float partialTick) {
		Entity entity = hookshot.getThrower();
		if (entity != null) {
			// TODO mess with this to get it looking right with the models
			y -= (3.0D - (double) hookshot.height) * 0.5D;
			double d0 = interpolateValue((double) entity.prevRotationYaw, (double) entity.rotationYaw, (double)(partialTick * 0.5F)) * 0.01745329238474369D;
			double d1 = interpolateValue((double) entity.prevRotationPitch, (double) entity.rotationPitch, (double)(partialTick * 0.5F)) * 0.01745329238474369D;
			double d2 = Math.cos(d0);
			double d3 = Math.sin(d0);
			double d4 = Math.sin(d1);
			double d5 = Math.cos(d1);
			double d6 = interpolateValue(entity.prevPosX, entity.posX, (double) partialTick) - d2 * 0.7D - d3 * 0.5D * d5;
			double d7 = interpolateValue(entity.prevPosY + (double) entity.getEyeHeight() * 0.7D, entity.posY + (double) entity.getEyeHeight() * 0.7D, (double) partialTick) - d4 * 0.5D - 0.25D;
			double d8 = interpolateValue(entity.prevPosZ, entity.posZ, (double) partialTick) - d3 * 0.7D + d2 * 0.5D * d5;
			double d9 = interpolateValue((double) hookshot.prevRotationYaw, (double) hookshot.prevRotationPitch, (double) partialTick) * 0.01745329238474369D + (Math.PI / 2D);
			d2 = Math.cos(d9) * (double) hookshot.width * 0.4D;
			d3 = Math.sin(d9) * (double) hookshot.width * 0.4D;
			if (hookshot.isInGround()) {
				hookshot.posX = hookshot.getDataWatcher().getWatchableObjectFloat(EntityHookShot.HIT_POS_X);
				hookshot.posY = hookshot.getDataWatcher().getWatchableObjectFloat(EntityHookShot.HIT_POS_Y);
				hookshot.posZ = hookshot.getDataWatcher().getWatchableObjectFloat(EntityHookShot.HIT_POS_Z);
				hookshot.prevPosX = hookshot.posX;
				hookshot.prevPosY = hookshot.posY;
				hookshot.prevPosZ = hookshot.posZ;
			}
			double d10 = interpolateValue(hookshot.prevPosX, hookshot.posX, (double) partialTick) + d2;
			double d11 = interpolateValue(hookshot.prevPosY, hookshot.posY, (double) partialTick);
			double d12 = interpolateValue(hookshot.prevPosZ, hookshot.posZ, (double) partialTick) + d3;
			x += d2;
			z += d3;
			double d13 = (double)((float)(d6 - d10));
			double d14 = (double)((float)(d7 - d11));
			double d15 = (double)((float)(d8 - d12));
			GlStateManager.disableTexture2D();
			GlStateManager.disableLighting();
			GlStateManager.disableCull();
			Tessellator tessellator = Tessellator.getInstance();
			WorldRenderer renderer = tessellator.getWorldRenderer();
			renderer.begin(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_COLOR);
			float grey = (1.0F/255) * 128F;
			float lgrey = grey / 2;
			for (int i = 0; i <= 24; ++i) {
				float rgb = (i % 2 == 0 ? grey : lgrey);
				float f3 = (float) i / 24.0F;
				renderer.pos(x + d13 * (double)f3 + 0.0D, y + d14 * (double)(f3 * f3 + f3) * 0.5D + (double)((24.0F - (float)i) / 18.0F + 0.125F), z + d15 * (double)f3).color(rgb, rgb, rgb, 1.0F).endVertex();
				renderer.pos(x + d13 * (double)f3 + 0.025D, y + d14 * (double)(f3 * f3 + f3) * 0.5D + (double)((24.0F - (float)i) / 18.0F + 0.125F) + 0.025D, z + d15 * (double)f3).color(rgb, rgb, rgb, 1.0F).endVertex();
			}
			tessellator.draw();
			renderer.begin(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_COLOR);
			for (int i = 0; i <= 24; ++i) {
				float rgb = (i % 2 == 0 ? grey : lgrey);
				float f3 = (float)i / 24.0F;
				renderer.pos(x + d13 * (double)f3 + 0.0D, y + d14 * (double)(f3 * f3 + f3) * 0.5D + (double)((24.0F - (float)i) / 18.0F + 0.125F) + 0.025D, z + d15 * (double)f3).color(rgb, rgb, rgb, 1.0F).endVertex();
				renderer.pos(x + d13 * (double)f3 + 0.025D, y + d14 * (double)(f3 * f3 + f3) * 0.5D + (double)((24.0F - (float)i) / 18.0F + 0.125F), z + d15 * (double)f3 + 0.025D).color(rgb, rgb, rgb, 1.0F).endVertex();
			}
			tessellator.draw();
			GlStateManager.enableLighting();
			GlStateManager.enableTexture2D();
			GlStateManager.enableCull();
		}
	}

	public static class Factory implements IRenderFactory<EntityHookShot> {
		@Override
		public Render<? super EntityHookShot> createRenderFor(RenderManager manager) {
			return new RenderEntityHookShot(manager);
		}
	}
}
