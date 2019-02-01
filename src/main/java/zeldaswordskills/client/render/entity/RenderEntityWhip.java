/**
 * Copyright (C) <2019> <coolAlias>
 *
 * This file is part of coolAlias' Zelda Sword Skills Minecraft Mod; as such,
 * you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
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
import zeldaswordskills.api.block.IWhipBlock.WhipType;
import zeldaswordskills.entity.projectile.EntityWhip;

/**
 * 
 * Copied from RenderEntityHookshot, which was copied from the vanilla leash rendering in RenderLiving.
 *
 */
@SideOnly(Side.CLIENT)
public class RenderEntityWhip extends Render<EntityWhip>
{
	public RenderEntityWhip(RenderManager renderManager) {
		super(renderManager);
	}

	@Override
	public void doRender(EntityWhip entity, double x, double y, double z, float yaw, float partialTick) {
		renderLeash((EntityWhip) entity, x, y, z, yaw, partialTick);
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityWhip entity) {
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
			double d0 = interpolateValue((double) entity.prevRotationYaw, (double) entity.rotationYaw, (double)(partialTick * 0.5F)) * 0.01745329238474369D;
			double d1 = interpolateValue((double) entity.prevRotationPitch, (double) entity.rotationPitch, (double)(partialTick * 0.5F)) * 0.01745329238474369D;
			double d2 = Math.cos(d0);
			double d3 = Math.sin(d0);
			double d4 = Math.sin(d1);
			double d5 = Math.cos(d1);
			double d6 = interpolateValue(entity.prevPosX, entity.posX, (double) partialTick) - d2 * 0.7D - d3 * 0.5D * d5;
			double d7 = interpolateValue(entity.prevPosY + (double) entity.getEyeHeight() * 0.7D, entity.posY + (double) entity.getEyeHeight() * 0.7D, (double) partialTick) - d4 * 0.5D - 0.25D;
			double d8 = interpolateValue(entity.prevPosZ, entity.posZ, (double) partialTick) - d3 * 0.7D + d2 * 0.5D * d5;
			double d9 = interpolateValue((double) whip.prevRotationYaw, (double) whip.prevRotationPitch, (double) partialTick) * 0.01745329238474369D + (Math.PI / 2D);
			d2 = Math.cos(d9) * (double) whip.width * 0.4D;
			d3 = Math.sin(d9) * (double) whip.width * 0.4D;
			if (whip.isInGround()) {
				whip.posX = whip.getDataWatcher().getWatchableObjectFloat(EntityWhip.HIT_POS_X);
				whip.posY = whip.getDataWatcher().getWatchableObjectFloat(EntityWhip.HIT_POS_Y);
				whip.posZ = whip.getDataWatcher().getWatchableObjectFloat(EntityWhip.HIT_POS_Z);
				whip.prevPosX = whip.posX;
				whip.prevPosY = whip.posY;
				whip.prevPosZ = whip.posZ;
			}
			double d10 = interpolateValue(whip.prevPosX, whip.posX, (double) partialTick) + d2;
			double d11 = interpolateValue(whip.prevPosY, whip.posY, (double) partialTick);
			double d12 = interpolateValue(whip.prevPosZ, whip.posZ, (double) partialTick) + d3;
			x += d2;
			z += d3;
			double d13 = (double)((float)(d6 - d10));
			double d14 = (double)((float)(d7 - d11));
			double d15 = (double)((float)(d8 - d12));
			GlStateManager.disableTexture2D();
			GlStateManager.disableLighting();
			GlStateManager.disableCull();
			int r = 139;
			int g = 90;
			int b = 43;
			if (whip.getType() == WhipType.WHIP_MAGIC) {
				r = 255;
				g = 0;
				b = 0;
			}
			Tessellator tessellator = Tessellator.getInstance();
			WorldRenderer renderer = tessellator.getWorldRenderer();
			renderer.begin(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_COLOR);
			for (int i = 0; i <= 24; ++i) {
				float f3 = (float) i / 24.0F;
				renderer.pos(x + d13 * (double)f3 + 0.0D, y + d14 * (double)(f3 * f3 + f3) * 0.5D + (double)((24.0F - (float)i) / 18.0F + 0.125F), z + d15 * (double)f3).color(r, g, b, 1.0F).endVertex();
				renderer.pos(x + d13 * (double)f3 + 0.025D, y + d14 * (double)(f3 * f3 + f3) * 0.5D + (double)((24.0F - (float)i) / 18.0F + 0.125F) + 0.025D, z + d15 * (double)f3).color(r, g, b, 1.0F).endVertex();
			}
			tessellator.draw();
			renderer.begin(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_COLOR);
			for (int i = 0; i <= 24; ++i) {
				float f3 = (float) i / 24.0F;
				renderer.pos(x + d10 * (double)f3 + 0.0D, y + d11 * (double)(f3 * f3 + f3) * 0.5D + (double)((24.0F - (float)i) / 18.0F + 0.125F) + 0.025D, z + d12 * (double)f3).color(r, g, b, 1.0F).endVertex();
				renderer.pos(x + d10 * (double)f3 + 0.025D, y + d11 * (double)(f3 * f3 + f3) * 0.5D + (double)((24.0F - (float)i) / 18.0F + 0.125F), z + d12 * (double)f3 + 0.025D).color(r, g, b, 1.0F).endVertex();
			}
			tessellator.draw();
			GlStateManager.enableLighting();
			GlStateManager.enableTexture2D();
			GlStateManager.enableCull();
		}
	}

	public static class Factory implements IRenderFactory<EntityWhip> {
		@Override
		public Render<? super EntityWhip> createRenderFor(RenderManager manager) {
			return new RenderEntityWhip(manager);
		}
	}
}
