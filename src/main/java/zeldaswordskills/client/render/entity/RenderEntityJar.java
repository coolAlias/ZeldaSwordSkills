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

import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import zeldaswordskills.block.ZSSBlocks;
import zeldaswordskills.entity.projectile.EntityCeramicJar;

public class RenderEntityJar extends Render
{
	private final RenderBlocks blockRenderer = new RenderBlocks();

	public RenderEntityJar() {}

	public void renderJar(EntityCeramicJar entity, double dx, double dy, double dz, float yaw, float partialTick) {
		GL11.glPushMatrix();
		GL11.glTranslated(dx, dy, dz);
		GL11.glRotatef(yaw, 0, 1, 0);
		float roll = ((float) entity.ticksExisted + partialTick) * 5;
		while (roll > 360) { roll -= 360; }
		GL11.glRotatef(roll, -0.25F, 0.1F, 0);
		GL11.glScalef(1.0F, 1.0F, 1.0F);
		bindEntityTexture(entity);
		blockRenderer.renderBlockAsItem(ZSSBlocks.ceramicJar, 0, entity.getBrightness(partialTick));
		GL11.glPopMatrix();
	}

	@Override
	public void doRender(Entity entity, double dx, double dy, double dz, float yaw, float partialTick) {
		renderJar((EntityCeramicJar) entity, dx, dy, dz, yaw, partialTick);
	}

	@Override
	protected ResourceLocation getEntityTexture(Entity entity) {
		return TextureMap.locationBlocksTexture;
	}
}
