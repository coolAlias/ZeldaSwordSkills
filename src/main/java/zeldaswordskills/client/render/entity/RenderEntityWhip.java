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

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import zeldaswordskills.api.block.IWhipBlock.WhipType;
import zeldaswordskills.entity.projectile.EntityWhip;

/**
 * 
 * Copied from RenderEntityHookshot, which was copied from the vanilla leash rendering in RenderLiving.
 *
 */
@SideOnly(Side.CLIENT)
public class RenderEntityWhip extends Render
{
	public RenderEntityWhip(RenderManager renderManager) {
		super(renderManager);
	}

	@Override
	public void doRender(Entity entity, double x, double y, double z, float yaw, float partialTick) {
		renderLeash((EntityWhip) entity, x, y, z, yaw, partialTick);
	}

	@Override
	protected ResourceLocation getEntityTexture(Entity entity) {
		return null;
	}

	/**
	 * Copied from RenderLiving
	 */
	private double interpolateValue(double start, double end, double pct) {
		return start + (end - start) * pct;
	}

	/**
	 * Copied from RenderLiving
	 */
	protected void renderLeash(EntityWhip whip, double x, double y, double z, float yaw, float partialTick) {
		Entity entity = whip.getThrower(); // whip is considered 'leashed' to the player
		if (entity != null) {
			y -= (3.0D - (double) whip.height) * 0.5D; // since whip is so small, had to adjust to 3.0D instead of 1.6D
			double d3 = interpolateValue((double) entity.prevRotationYaw, (double) entity.rotationYaw, (double)(partialTick * 0.5F)) * 0.01745329238474369D;
			double d4 = interpolateValue((double) entity.prevRotationPitch, (double) entity.rotationPitch, (double)(partialTick * 0.5F)) * 0.01745329238474369D;
			double d5 = Math.cos(d3);
			double d6 = Math.sin(d3);
			double d7 = Math.sin(d4);
			double d8 = Math.cos(d4);
			double d9 = interpolateValue(entity.prevPosX, entity.posX, (double) partialTick) - d5 * 0.7D - d6 * 0.5D * d8;
			double d10 = interpolateValue(entity.prevPosY + (double) entity.getEyeHeight() * 0.7D, entity.posY + (double) entity.getEyeHeight() * 0.7D, (double) partialTick) - d7 * 0.5D - 0.25D;
			double d11 = interpolateValue(entity.prevPosZ, entity.posZ, (double) partialTick) - d6 * 0.7D + d5 * 0.5D * d8;
			double d12 = interpolateValue((double) whip.prevRotationYaw, (double) whip.prevRotationPitch, (double) partialTick) * 0.01745329238474369D + (Math.PI / 2D);
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
			double d13 = interpolateValue(whip.prevPosX, whip.posX, (double) partialTick) + d5;
			double d14 = interpolateValue(whip.prevPosY, whip.posY, (double) partialTick);
			double d15 = interpolateValue(whip.prevPosZ, whip.posZ, (double) partialTick) + d6;
			x += d5;
			z += d6;
			double d16 = (double)((float)(d9 - d13));
			double d17 = (double)((float)(d10 - d14));
			double d18 = (double)((float)(d11 - d15));
			GlStateManager.disableTexture2D();
			GlStateManager.disableLighting();
			GlStateManager.disableCull();
			Tessellator tessellator = Tessellator.getInstance();
			WorldRenderer renderer = tessellator.getWorldRenderer();
			renderer.startDrawing(5);
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
				renderer.setColorRGBA(r, g, b, 255);
				f2 = (float)i / 24.0F;
				renderer.addVertex(x + d16 * (double) f2 + 0.0D, y + d17 * (double)(f2 * f2 + f2) * 0.5D + (double)((24.0F - (float) i) / 18.0F + 0.125F), z + d18 * (double) f2);
				renderer.addVertex(x + d16 * (double) f2 + 0.025D, y + d17 * (double)(f2 * f2 + f2) * 0.5D + (double)((24.0F - (float) i) / 18.0F + 0.125F) + 0.025D, z + d18 * (double) f2);
			}
			tessellator.draw();
			renderer.startDrawing(5);
			for (i = 0; i <= 24; ++i) {
				renderer.setColorRGBA(r, g, b, 255);
				f2 = (float) i / 24.0F;
				renderer.addVertex(x + d16 * (double) f2 + 0.0D, y + d17 * (double)(f2 * f2 + f2) * 0.5D + (double)((24.0F - (float) i) / 18.0F + 0.125F) + 0.025D, z + d18 * (double) f2);
				renderer.addVertex(x + d16 * (double) f2 + 0.025D, y + d17 * (double)(f2 * f2 + f2) * 0.5D + (double)((24.0F - (float) i) / 18.0F + 0.125F), z + d18 * (double) f2 + 0.025D);
			}
			tessellator.draw();
			GlStateManager.enableLighting();
			GlStateManager.enableTexture2D();
			GlStateManager.enableCull();
		}
	}
}
