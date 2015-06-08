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

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderSlime;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import zeldaswordskills.entity.mobs.EntityChu;
import zeldaswordskills.ref.ModInfo;

@SideOnly(Side.CLIENT)
public class RenderEntityChu extends RenderSlime
{
	private static final ResourceLocation redChu = new ResourceLocation(ModInfo.ID, "textures/entity/chu_red.png");
	private static final ResourceLocation greenChu = new ResourceLocation(ModInfo.ID, "textures/entity/chu_green.png");
	private static final ResourceLocation blueChu = new ResourceLocation(ModInfo.ID, "textures/entity/chu_blue.png");
	private static final ResourceLocation blueChuShock = new ResourceLocation(ModInfo.ID, "textures/entity/chu_blue_shock.png");
	private static final ResourceLocation yellowChu = new ResourceLocation(ModInfo.ID, "textures/entity/chu_yellow.png");
	private static final ResourceLocation yellowChuShock = new ResourceLocation(ModInfo.ID, "textures/entity/chu_yellow_shock.png");

	public RenderEntityChu(RenderManager renderManager, ModelBase model, float shadowSize) {
		super(renderManager, model, shadowSize);
	}

	@Override
	protected ResourceLocation getEntityTexture(Entity entity) {
		return getChuTexture((EntityChu) entity);
	}

	protected ResourceLocation getChuTexture(EntityChu chu) {
		switch(chu.getType()) {
		case RED: return redChu;
		case GREEN: return greenChu;
		case BLUE: return (chu.getShockTime() % 8 > 5 ? blueChuShock : blueChu);
		case YELLOW: return (chu.getShockTime() % 8 > 5 ? yellowChuShock : yellowChu);
		default: return redChu;
		}
	}
}
