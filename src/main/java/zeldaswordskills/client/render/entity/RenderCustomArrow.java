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

import net.minecraft.client.renderer.entity.RenderArrow;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import zeldaswordskills.entity.projectile.EntityArrowBomb;
import zeldaswordskills.entity.projectile.EntityArrowCustom;
import zeldaswordskills.entity.projectile.EntityArrowElemental;
import zeldaswordskills.ref.ModInfo;

@SideOnly(Side.CLIENT)
public class RenderCustomArrow extends RenderArrow
{
	private static final ResourceLocation bombArrow = new ResourceLocation(ModInfo.ID, "textures/entity/arrow_bomb.png");
	private static final ResourceLocation fireArrow = new ResourceLocation(ModInfo.ID, "textures/entity/arrow_fire.png");
	private static final ResourceLocation iceArrow = new ResourceLocation(ModInfo.ID, "textures/entity/arrow_ice.png");
	private static final ResourceLocation lightArrow = new ResourceLocation(ModInfo.ID, "textures/entity/arrow_light.png");

	public RenderCustomArrow(RenderManager renderManager) {
		super(renderManager);
	}

	@Override
	public void doRender(Entity entity, double dx, double dy, double dz, float yaw, float partialTick) {
		super.doRender(entity, dx, dy, dz, yaw, partialTick);
	}

	@Override
	protected ResourceLocation getEntityTexture(Entity entity) {
		return getArrowTexture((EntityArrowCustom) entity);
	}

	protected ResourceLocation getArrowTexture(EntityArrowCustom arrow) {
		if (arrow instanceof EntityArrowBomb) {
			return getBombArrowTexture((EntityArrowBomb) arrow);
		} else if (arrow instanceof EntityArrowElemental) {
			return getElementalArrowTexture((EntityArrowElemental) arrow);
		}
		return super.getEntityTexture(arrow);
	}

	protected ResourceLocation getBombArrowTexture(EntityArrowBomb arrow) {
		switch(arrow.getType()) {
		case BOMB_FIRE:
		case BOMB_WATER:
		default: return bombArrow;
		}
	}

	protected ResourceLocation getElementalArrowTexture(EntityArrowElemental arrow) {
		switch(arrow.getType()) {
		case FIRE: return fireArrow;
		case ICE: return iceArrow;
		case LIGHT: return lightArrow;
		default: return super.getEntityTexture(arrow);
		}
	}
}
