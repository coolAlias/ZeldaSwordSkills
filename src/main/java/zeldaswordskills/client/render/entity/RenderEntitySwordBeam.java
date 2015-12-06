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
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.lwjgl.opengl.GL11;

import zeldaswordskills.api.item.ArmorIndex;
import zeldaswordskills.item.ZSSItems;
import zeldaswordskills.ref.ModInfo;

@SideOnly(Side.CLIENT)
public class RenderEntitySwordBeam extends Render
{
	private static final ResourceLocation texture = new ResourceLocation(ModInfo.ID + ":textures/entity/sword_beam.png");

	public RenderEntitySwordBeam(RenderManager renderManager) {
		super(renderManager);
		shadowSize = 0.25F;
		shadowOpaque = 0.75F;
	}

	public void renderBeam(Entity entity, double x, double y, double z, float yaw, float partialTick) {
		GlStateManager.pushMatrix();
		GlStateManager.translate(x, y, z);
		GlStateManager.pushAttrib();
		GlStateManager.enableBlend();
		GlStateManager.enableLighting();
		GlStateManager.enableTexture2D();
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GlStateManager.scale(1.5F, 1.25F, 1.5F);
		bindTexture(texture);
		Tessellator tessellator = Tessellator.getInstance();
		WorldRenderer renderer = tessellator.getWorldRenderer();
		GlStateManager.rotate(180.0F - renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
		GlStateManager.rotate(-renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
		float rgb = getRgb(entity);
		GlStateManager.color(rgb, rgb, rgb);
		renderer.startDrawingQuads();
		renderer.setNormal(0.0F, 1.0F, 0.0F);
		renderer.addVertexWithUV(-0.5D, -0.25D, 0.0D, 0, 1);
		renderer.addVertexWithUV(0.5D, -0.25D, 0.0D, 1, 1);
		renderer.addVertexWithUV(0.5D, 0.75D, 0.0D, 1, 0);
		renderer.addVertexWithUV(-0.5D, 0.75D, 0.0D, 0, 0);
		tessellator.draw();
		GlStateManager.popAttrib();
		GlStateManager.popMatrix();
	}

	@Override
	public void doRender(Entity entity, double x, double y, double z, float yaw, float partialTick) {
		renderBeam(entity, x, y, z, yaw, partialTick);
	}

	@Override
	protected ResourceLocation getEntityTexture(Entity entity) {
		return texture;
	}

	private float getRgb(Entity entity) {
		if (entity instanceof EntityThrowable) {
			EntityLivingBase thrower = ((EntityThrowable) entity).getThrower();
			if (thrower != null && thrower.getCurrentArmor(ArmorIndex.WORN_HELM) != null && thrower.getCurrentArmor(ArmorIndex.WORN_HELM).getItem() == ZSSItems.maskFierce) {
				return 0.0F; //  nice and dark for the Fierce Diety
			}
		}
		return 1.0F;
	}
}
