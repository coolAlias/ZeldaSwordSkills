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

import zeldaswordskills.api.block.IWhipBlock.WhipType;
import zeldaswordskills.entity.projectile.EntityWhip;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * 
 * Copied from RenderEntityHookshot, which was copied from the vanilla leash rendering in RenderLiving.
 *
 */
@SideOnly(Side.CLIENT)
public class RenderEntityWhip extends Render
{
	public RenderEntityWhip() {}

	@Override
	public void doRender(Entity entity, double x, double y, double z, float yaw, float partialTick) {
		renderLeash((EntityWhip) entity, x, y, z, yaw, partialTick);
	}

	@Override
	protected ResourceLocation getEntityTexture(Entity entity) {
		return null;
	}

	/**
	 * Copied from RenderLiving; not really sure what exactly it does
	 */
	private double func_110828_a(double par1, double par3, double par5) {
		return par1 + (par3 - par1) * par5;
	}

	protected void renderLeash(EntityWhip whip, double x, double y, double z, float yaw, float partialTick) {
		Entity entity = whip.getThrower(); // whip is considered 'leashed' to the player
		if (entity != null) {
			y -= (3.0D - (double) whip.height) * 0.5D; // since whip is so small, had to adjust to 3.0D instead of 1.6D
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
			double d12 = func_110828_a((double) whip.prevRotationYaw, (double) whip.prevRotationPitch, (double) partialTick) * 0.01745329238474369D + (Math.PI / 2D);
			d5 = Math.cos(d12) * (double) whip.width * 0.4D;
			d6 = Math.sin(d12) * (double) whip.width * 0.4D;
			if (whip.isInGround()) {
				whip.posX = whip.getDataWatcher().getWatchableObjectFloat(EntityWhip.HIT_POS_X);
				whip.posY = whip.getDataWatcher().getWatchableObjectFloat(EntityWhip.HIT_POS_Y);
				whip.posZ = whip.getDataWatcher().getWatchableObjectFloat(EntityWhip.HIT_POS_Z);
				whip.prevPosX = whip.posX;
				whip.prevPosY = whip.posY;
				whip.prevPosZ = whip.posZ;
			}
			double d13 = func_110828_a(whip.prevPosX, whip.posX, (double) partialTick) + d5;
			double d14 = func_110828_a(whip.prevPosY, whip.posY, (double) partialTick);
			double d15 = func_110828_a(whip.prevPosZ, whip.posZ, (double) partialTick) + d6;
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
			int r = 139;
			int g = 90;
			int b = 43;
			if (whip.getType() == WhipType.WHIP_MAGIC) {
				r = 255;
				g = 0;
				b = 0;
			}

			for (i = 0; i <= 24; ++i) {
				tessellator.setColorRGBA(r, g, b, 255);
				f2 = (float)i / 24.0F;
				tessellator.addVertex(x + d16 * (double) f2 + 0.0D, y + d17 * (double)(f2 * f2 + f2) * 0.5D + (double)((24.0F - (float) i) / 18.0F + 0.125F), z + d18 * (double) f2);
				tessellator.addVertex(x + d16 * (double) f2 + 0.025D, y + d17 * (double)(f2 * f2 + f2) * 0.5D + (double)((24.0F - (float) i) / 18.0F + 0.125F) + 0.025D, z + d18 * (double) f2);
			}

			tessellator.draw();
			tessellator.startDrawing(5);

			for (i = 0; i <= 24; ++i) {
				tessellator.setColorRGBA(r, g, b, 255);
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
