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
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import zeldaswordskills.client.model.ModelWizzrobe;
import zeldaswordskills.entity.mobs.EntityGrandWizzrobe;
import zeldaswordskills.ref.ModInfo;

@SideOnly(Side.CLIENT)
public class RenderEntityWizzrobeGrand extends RenderEntityWizzrobe
{
	private static final ResourceLocation FIRE = new ResourceLocation(ModInfo.ID, "textures/entity/wizzrobe_fire_grand.png");
	private static final ResourceLocation GALE = new ResourceLocation(ModInfo.ID, "textures/entity/wizzrobe_wind_grand.png");
	private static final ResourceLocation ICE = new ResourceLocation(ModInfo.ID, "textures/entity/wizzrobe_ice_grand.png");
	private static final ResourceLocation THUNDER = new ResourceLocation(ModInfo.ID, "textures/entity/wizzrobe_lightning_grand.png");

	public RenderEntityWizzrobeGrand(ModelWizzrobe model, float scale) {
		super(model, 0.5F, null);
	}

	@Override
	protected ResourceLocation getEntityTexture(Entity entity) {
		return this.getWizzrobeTexture((EntityGrandWizzrobe) entity);
	}

	protected ResourceLocation getWizzrobeTexture(EntityGrandWizzrobe wizzrobe) {
		switch (wizzrobe.getMagicType()) {
		case FIRE: return RenderEntityWizzrobeGrand.FIRE;
		case ICE: return RenderEntityWizzrobeGrand.ICE;
		case LIGHTNING: return RenderEntityWizzrobeGrand.THUNDER;
		case WIND: return RenderEntityWizzrobeGrand.GALE;
		default: return RenderEntityWizzrobeGrand.GALE;
		}
	}
}
