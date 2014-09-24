/**
    Copyright (C) <2014> <coolAlias>

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

import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import zeldaswordskills.client.model.ModelCube;
import zeldaswordskills.entity.projectile.EntityMagicSpell;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderEntityMagicSpell extends Render
{
	private final ModelCube box1 = new ModelCube(4);
	private final ModelCube box2 = new ModelCube(4);

	public RenderEntityMagicSpell() {}

	@Override
	public void doRender(Entity entity, double dx, double dy, double dz, float yaw, float partialTick) {
		GL11.glPushMatrix();
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240f, 240f);
		float scale = ((EntityMagicSpell) entity).getArea();
		float roll = ((float) entity.ticksExisted + partialTick) * 40;
		while (roll > 360) roll -= 360;
		GL11.glTranslated(dx, dy, dz);
		GL11.glScalef(scale, scale, scale);
		GL11.glRotatef(yaw, 0, 1, 0);
		GL11.glRotatef(roll, 0.8F, 0F, -0.6F);
		bindEntityTexture(entity);
		Tessellator.instance.setBrightness(0xf000f0);
		box1.render(entity);
		GL11.glRotatef(45, 1, 0, 1);
		box2.render(entity);
		GL11.glDisable(GL12.GL_RESCALE_NORMAL);
		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glPopMatrix();
	}

	@Override
	protected ResourceLocation getEntityTexture(Entity entity) {
		return ((EntityMagicSpell) entity).getType().getEntityTexture();
	}
}
