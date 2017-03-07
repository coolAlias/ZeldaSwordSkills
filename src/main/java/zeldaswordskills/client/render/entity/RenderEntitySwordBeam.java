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
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import zeldaswordskills.api.item.ArmorIndex;
import zeldaswordskills.item.ZSSItems;
import zeldaswordskills.ref.ModInfo;

@SideOnly(Side.CLIENT)
public class RenderEntitySwordBeam extends Render<EntityThrowable>
{
	private static final ResourceLocation TEXTURE = new ResourceLocation(ModInfo.ID + ":textures/entity/sword_beam.png");

	public RenderEntitySwordBeam(RenderManager renderManager) {
		super(renderManager);
		this.shadowSize = 0.25F;
		this.shadowOpaque = 0.75F;
	}

	@Override
	public void doRender(EntityThrowable entity, double x, double y, double z, float yaw, float partialTick) {
		GlStateManager.pushMatrix();
		GlStateManager.translate(x, y, z);
		GlStateManager.pushAttrib();
		GlStateManager.enableBlend();
		GlStateManager.enableLighting();
		GlStateManager.enableTexture2D();
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GlStateManager.scale(1.5F, 1.25F, 1.5F);
		this.bindTexture(this.getEntityTexture(entity));
		Tessellator tessellator = Tessellator.getInstance();
		WorldRenderer renderer = tessellator.getWorldRenderer();
		GlStateManager.rotate(180.0F - renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
		GlStateManager.rotate(-renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
		float rgb = this.getRgb(entity);
		GlStateManager.color(rgb, rgb, rgb);
		renderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_NORMAL);
		renderer.pos(-0.5D, -0.25D, 0.0D).tex(0, 1).normal(0.0F, 1.0F, 0.0F).endVertex();
		renderer.pos(0.5D, -0.25D, 0.0D).tex(1, 1).normal(0.0F, 1.0F, 0.0F).endVertex();
		renderer.pos(0.5D, 0.75D, 0.0D).tex(1, 0).normal(0.0F, 1.0F, 0.0F).endVertex();
		renderer.pos(-0.5D, 0.75D, 0.0D).tex(0, 0).normal(0.0F, 1.0F, 0.0F).endVertex();
		tessellator.draw();
		GlStateManager.popAttrib();
		GlStateManager.popMatrix();
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityThrowable entity) {
		return RenderEntitySwordBeam.TEXTURE;
	}

	private float getRgb(EntityThrowable entity) {
		EntityLivingBase thrower = entity.getThrower();
		if (thrower != null && thrower.getCurrentArmor(ArmorIndex.WORN_HELM) != null && thrower.getCurrentArmor(ArmorIndex.WORN_HELM).getItem() == ZSSItems.maskFierce) {
			return 0.0F; //  nice and dark for the Fierce Diety
		}
		return 1.0F;
	}

	public static class Factory implements IRenderFactory<EntityThrowable> {
		@Override
		public Render<? super EntityThrowable> createRenderFor(RenderManager manager) {
			return new RenderEntitySwordBeam(manager);
		}
	}
}
