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

import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Icon;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import zeldaswordskills.entity.projectile.EntityBoomerang;

public class RenderEntityBoomerang extends Render
{
	public RenderEntityBoomerang() {}

	public void renderBoomerang(EntityBoomerang entity, double x, double y, double z, float yaw, float partialTick) {
		ItemStack boomerang = entity.getBoomerang();
		if (boomerang != null) {
			GL11.glPushMatrix();
			GL11.glTranslated(x, y, z);
			float rotation = ((float) entity.ticksExisted + partialTick) * 50;
			while (rotation > 360) rotation -= 360;
			GL11.glRotatef(entity.rotationYaw + (entity.prevRotationYaw - entity.rotationYaw) * partialTick - 60.0F, 0.0F, 1.0F, 0.0F);
			GL11.glRotatef(Math.abs(entity.rotationPitch + (entity.prevRotationPitch - entity.rotationPitch)) * partialTick - rotation, 0.0F, 0.0F, 1.0F);
			GL11.glTranslatef(-0.5F, -0.5F, 0.0F);
			Icon icon = boomerang.getItem().getIconFromDamage(0);
			bindTexture(renderManager.renderEngine.getResourceLocation(boomerang.getItem().getSpriteNumber()));
			Tessellator tessellator = Tessellator.instance;
			ItemRenderer.renderItemIn2D(tessellator, icon.getMaxU(), icon.getMinV(), icon.getMinU(), icon.getMaxV(), icon.getIconWidth(), icon.getIconHeight(), 0.0625F);
			GL11.glPopMatrix();
		}
	}
	
	@Override
	public void doRender(Entity entity, double x, double y, double z, float yaw, float partialTick) {
		renderBoomerang((EntityBoomerang) entity, x, y, z, yaw, partialTick);
	}

	@Override
	protected ResourceLocation getEntityTexture(Entity entity) {
		return null;
	}
}
