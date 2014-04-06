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

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import zeldaswordskills.client.model.ModelBomb;
import zeldaswordskills.entity.projectile.EntityBomb;
import zeldaswordskills.item.ItemBomb;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * 
 * Renders flashing bomb entity.
 *
 */
@SideOnly(Side.CLIENT)
public class RenderEntityBomb extends Render
{
	protected ModelBase model;

	public RenderEntityBomb() {
		model = new ModelBomb();
	}

	@Override
	protected ResourceLocation getEntityTexture(Entity bomb) {
		boolean isFlashing = bomb.ticksExisted % 13 > 10;
		switch(((EntityBomb) bomb).getType()) {
		case BOMB_FIRE: return isFlashing ? ItemBomb.fireFlash : ItemBomb.fireBase;
		case BOMB_WATER: return isFlashing ? ItemBomb.waterFlash : ItemBomb.waterBase;
		default: return isFlashing ? ItemBomb.bombFlash : ItemBomb.bombBase;
		}
	}

	@Override
	public void doRender(Entity bomb, double x, double y, double z, float yaw, float partialTick) {
		renderEntityModel(bomb, x, y, z, yaw, partialTick);
	}

	public void renderEntityModel(Entity bomb, double x, double y, double z, float yaw, float partialTick) {
		GL11.glPushMatrix();
		float scale = bomb.ticksExisted % 13 > 10 ? 1.65F : 1.25F;
		bindTexture(getEntityTexture(bomb));
		GL11.glTranslated(x, y, z);
		GL11.glEnable(GL12.GL_RESCALE_NORMAL);
		GL11.glRotatef(150, 1F, 1F, 300F);
		GL11.glScalef(scale, scale, scale);
		GL11.glTranslatef(0.0F, -1.0F, 0.0F);
		model.render(bomb, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0475F);
		GL11.glDisable(GL12.GL_RESCALE_NORMAL);
		GL11.glPopMatrix();
	}
}
