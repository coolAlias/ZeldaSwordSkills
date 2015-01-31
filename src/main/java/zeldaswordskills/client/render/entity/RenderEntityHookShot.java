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

package zeldaswordskills.client.render.entity;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import zeldaswordskills.entity.projectile.EntityHookShot;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * 
 * A mish-mash of arrow and leash rendering, so... holy $hit it actually worked!!!
 *
 */
@SideOnly(Side.CLIENT)
public class RenderEntityHookShot extends Render
{
	/** Use arrow texture for now */
	private static final ResourceLocation arrowTexture = new ResourceLocation("textures/entity/arrow.png");

	public RenderEntityHookShot() {}

	@Override
	public void doRender(Entity entity, double x, double y, double z, float yaw, float partialTick) {
		renderArrow((EntityHookShot) entity, x, y, z, yaw, partialTick);
		renderLeash((EntityHookShot) entity, x, y, z, yaw, partialTick);
	}

	@Override
	protected ResourceLocation getEntityTexture(Entity entity) {
		return arrowTexture;
	}

	public void renderArrow(EntityHookShot hookshot, double x, double y, double z, float yaw, float partialTick) {
		bindEntityTexture(hookshot);
		GL11.glPushMatrix();
		GL11.glTranslated(x, y, z);
		GL11.glRotatef(hookshot.prevRotationYaw + (hookshot.rotationYaw - hookshot.prevRotationYaw) * partialTick - 90.0F, 0.0F, 1.0F, 0.0F);
		GL11.glRotatef(hookshot.prevRotationPitch + (hookshot.rotationPitch - hookshot.prevRotationPitch) * partialTick, 0.0F, 0.0F, 1.0F);
		Tessellator tessellator = Tessellator.instance;
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
		GL11.glEnable(GL12.GL_RESCALE_NORMAL);
		GL11.glRotatef(45.0F, 1.0F, 0.0F, 0.0F);
		GL11.glScalef(f10, f10, f10);
		GL11.glTranslatef(-4.0F, 0.0F, 0.0F);
		GL11.glNormal3f(f10, 0.0F, 0.0F);
		tessellator.startDrawingQuads();
		tessellator.addVertexWithUV(-7.0D, -2.0D, -2.0D, (double)f6, (double)f8);
		tessellator.addVertexWithUV(-7.0D, -2.0D, 2.0D, (double)f7, (double)f8);
		tessellator.addVertexWithUV(-7.0D, 2.0D, 2.0D, (double)f7, (double)f9);
		tessellator.addVertexWithUV(-7.0D, 2.0D, -2.0D, (double)f6, (double)f9);
		tessellator.draw();
		GL11.glNormal3f(-f10, 0.0F, 0.0F);
		tessellator.startDrawingQuads();
		tessellator.addVertexWithUV(-7.0D, 2.0D, -2.0D, (double)f6, (double)f8);
		tessellator.addVertexWithUV(-7.0D, 2.0D, 2.0D, (double)f7, (double)f8);
		tessellator.addVertexWithUV(-7.0D, -2.0D, 2.0D, (double)f7, (double)f9);
		tessellator.addVertexWithUV(-7.0D, -2.0D, -2.0D, (double)f6, (double)f9);
		tessellator.draw();

		for (int i = 0; i < 4; ++i) {
			GL11.glRotatef(90.0F, 1.0F, 0.0F, 0.0F);
			GL11.glNormal3f(0.0F, 0.0F, f10);
			tessellator.startDrawingQuads();
			tessellator.addVertexWithUV(-8.0D, -2.0D, 0.0D, (double)f2, (double)f4);
			tessellator.addVertexWithUV(8.0D, -2.0D, 0.0D, (double)f3, (double)f4);
			tessellator.addVertexWithUV(8.0D, 2.0D, 0.0D, (double)f3, (double)f5);
			tessellator.addVertexWithUV(-8.0D, 2.0D, 0.0D, (double)f2, (double)f5);
			tessellator.draw();
		}

		GL11.glDisable(GL12.GL_RESCALE_NORMAL);
		GL11.glPopMatrix();
	}

	/**
	 * Copied from RenderLiving; not really sure what exactly it does
	 */
	private double func_110828_a(double par1, double par3, double par5) {
		return par1 + (par3 - par1) * par5;
	}

	protected void renderLeash(EntityHookShot hookshot, double x, double y, double z, float yaw, float partialTick) {
		Entity entity = hookshot.getThrower();
		if (entity != null) {
			// TODO mess with this to get it looking right with the models
			y -= (3.0D - (double) hookshot.height) * 0.5D;
			Tessellator tessellator = Tessellator.instance;
			double d3 = func_110828_a((double) entity.prevRotationYaw, (double) entity.rotationYaw, (double)(partialTick * 0.5F)) * 0.01745329238474369D;
			double d4 = func_110828_a((double) entity.prevRotationPitch, (double) entity.rotationPitch, (double)(partialTick * 0.5F)) * 0.01745329238474369D;
			double d5 = Math.cos(d3);
			double d6 = Math.sin(d3);
			double d7 = Math.sin(d4);
			double d8 = Math.cos(d4);
			double d9 = func_110828_a(entity.prevPosX, entity.posX, (double) partialTick) - d5 * 0.7D - d6 * 0.5D * d8;
			double d10 = func_110828_a(entity.prevPosY + (double) entity.getEyeHeight() * 0.7D, entity.posY + (double) entity.getEyeHeight() * 0.7D, (double) partialTick) - d7 * 0.5D - 0.25D;
			double d11 = func_110828_a(entity.prevPosZ, entity.posZ, (double) partialTick) - d6 * 0.7D + d5 * 0.5D * d8;
			double d12 = func_110828_a((double) hookshot.prevRotationYaw, (double) hookshot.prevRotationPitch, (double) partialTick) * 0.01745329238474369D + (Math.PI / 2D);
			d5 = Math.cos(d12) * (double) hookshot.width * 0.4D;
			d6 = Math.sin(d12) * (double) hookshot.width * 0.4D;
			if (hookshot.isInGround()) {
				hookshot.posX = hookshot.getDataWatcher().getWatchableObjectFloat(EntityHookShot.HIT_POS_X);
				hookshot.posY = hookshot.getDataWatcher().getWatchableObjectFloat(EntityHookShot.HIT_POS_Y);
				hookshot.posZ = hookshot.getDataWatcher().getWatchableObjectFloat(EntityHookShot.HIT_POS_Z);
				hookshot.prevPosX = hookshot.posX;
				hookshot.prevPosY = hookshot.posY;
				hookshot.prevPosZ = hookshot.posZ;
			}
			double d13 = func_110828_a(hookshot.prevPosX, hookshot.posX, (double) partialTick) + d5;
			double d14 = func_110828_a(hookshot.prevPosY, hookshot.posY, (double) partialTick);
			double d15 = func_110828_a(hookshot.prevPosZ, hookshot.posZ, (double) partialTick) + d6;
			x += d5;
			z += d6;
			double d16 = (double)((float)(d9 - d13));
			double d17 = (double)((float)(d10 - d14));
			double d18 = (double)((float)(d11 - d15));
			GL11.glDisable(GL11.GL_TEXTURE_2D);
			GL11.glDisable(GL11.GL_LIGHTING);
			GL11.glDisable(GL11.GL_CULL_FACE);
			tessellator.startDrawing(5);
			int i;
			float f2;
			float grey = (1.0F/255) * 128F;
			float lgrey = grey / 2;

			for (i = 0; i <= 24; ++i) {
				if (i % 2 == 0) {
					tessellator.setColorRGBA_F(grey, grey, grey, 1.0F);
				} else {
					tessellator.setColorRGBA_F(lgrey, lgrey, lgrey, 1.0F);
				}

				f2 = (float)i / 24.0F;
				tessellator.addVertex(x + d16 * (double) f2 + 0.0D, y + d17 * (double)(f2 * f2 + f2) * 0.5D + (double)((24.0F - (float) i) / 18.0F + 0.125F), z + d18 * (double) f2);
				tessellator.addVertex(x + d16 * (double) f2 + 0.025D, y + d17 * (double)(f2 * f2 + f2) * 0.5D + (double)((24.0F - (float) i) / 18.0F + 0.125F) + 0.025D, z + d18 * (double) f2);
			}

			tessellator.draw();
			tessellator.startDrawing(5);

			for (i = 0; i <= 24; ++i)
			{
				if (i % 2 == 0) {
					tessellator.setColorRGBA_F(grey, grey, grey, 1.0F);
				} else {
					tessellator.setColorRGBA_F(lgrey, lgrey, lgrey, 1.0F);
				}

				f2 = (float) i / 24.0F;
				tessellator.addVertex(x + d16 * (double) f2 + 0.0D, y + d17 * (double)(f2 * f2 + f2) * 0.5D + (double)((24.0F - (float) i) / 18.0F + 0.125F) + 0.025D, z + d18 * (double) f2);
				tessellator.addVertex(x + d16 * (double) f2 + 0.025D, y + d17 * (double)(f2 * f2 + f2) * 0.5D + (double)((24.0F - (float) i) / 18.0F + 0.125F), z + d18 * (double) f2 + 0.025D);
			}

			tessellator.draw();
			GL11.glEnable(GL11.GL_LIGHTING);
			GL11.glEnable(GL11.GL_TEXTURE_2D);
			GL11.glEnable(GL11.GL_CULL_FACE);
		}
	}
}
