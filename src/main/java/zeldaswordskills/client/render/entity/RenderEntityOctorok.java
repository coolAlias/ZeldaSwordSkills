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

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;
import zeldaswordskills.client.model.ModelOctorok;
import zeldaswordskills.entity.mobs.EntityOctorok;

@SideOnly(Side.CLIENT)
public class RenderEntityOctorok extends RenderLiving
{
	protected final ResourceLocation texture;

	public RenderEntityOctorok(ModelBase model, float shadowSize, ResourceLocation texture) {
		super(new ModelOctorok(), shadowSize);
		this.texture = texture;
	}

	public void renderLivingSquid(EntityOctorok entity, double dx, double dy, double dz, float f, float f1) {
		super.doRender(entity, dx, dy, dz, f, f1);
	}

	protected float handleRotationFloat(EntityOctorok octorok, float f) {
		return octorok.prevTentacleAngle + (octorok.tentacleAngle - octorok.prevTentacleAngle) * f;
	}

	@Override
	public void doRender(EntityLiving entity, double dx, double dy, double dz, float f, float f1) {
		this.renderLivingSquid((EntityOctorok) entity, dx, dy, dz, f, f1);
	}

	@Override
	protected float handleRotationFloat(EntityLivingBase entity, float f) {
		return this.handleRotationFloat((EntityOctorok) entity, f);
	}

	@Override
	protected ResourceLocation getEntityTexture(Entity entity) {
		return this.texture;
	}
}
