/**
    Copyright (C) <2018> <coolAlias>

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

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelSlime;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;
import zeldaswordskills.entity.mobs.EntityChu;

@SideOnly(Side.CLIENT)
public class RenderEntityChu extends RenderLiving
{
	protected final ModelBase scaleAmount;

	protected final ResourceLocation texture;

	public RenderEntityChu(ResourceLocation texture) {
		super(new ModelSlime(16), 0.25F);
		this.texture = texture;
		this.scaleAmount = new ModelSlime(0);
	}

	@Override
	protected ResourceLocation getEntityTexture(Entity entity) {
		return this.texture;
	}

	/**
	 * Determines whether Chu Render should pass or not.
	 */
	protected int shouldChuRenderPass(EntityChu chu, int renderPass, float partialTick) {
		if (chu.isInvisible()) {
			return 0;
		} else if (renderPass == 0) {
			this.setRenderPassModel(scaleAmount);
			GL11.glEnable(GL11.GL_NORMALIZE);
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			return 1;
		} else {
			if (renderPass == 1) {
				GL11.glDisable(GL11.GL_BLEND);
				GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			}
			return -1;
		}
	}

	/**
	 * sets the scale for the chu based on getSize in EntityChu
	 */
	protected void scaleEntity(EntityChu chu, float partialTick) {
		float f1 = (float) chu.getSize();
		float f2 = (chu.prevSquishFactor + (chu.squishFactor - chu.prevSquishFactor) * partialTick) / (f1 * 0.5F + 1.0F);
		float f3 = 1.0F / (f2 + 1.0F);
		GL11.glScalef(f3 * f1, 1.0F / f3 * f1, f3 * f1);
	}

	@Override
	protected void preRenderCallback(EntityLivingBase entity, float partialTick) {
		this.scaleEntity((EntityChu) entity, partialTick);
	}

	@Override
	protected int shouldRenderPass(EntityLivingBase entity, int renderPass, float partialTick) {
		return this.shouldChuRenderPass((EntityChu) entity, renderPass, partialTick);
	}
}
