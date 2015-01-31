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

import net.minecraft.client.renderer.entity.RenderBat;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import zeldaswordskills.entity.mobs.EntityKeese;
import zeldaswordskills.ref.ModInfo;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * 
 * @author credits to Jones7789 for most of the Keese textures
 *
 */
@SideOnly(Side.CLIENT)
public class RenderEntityKeese extends RenderBat
{
	private static final ResourceLocation base = new ResourceLocation(ModInfo.ID, "textures/entity/keese_base.png");
	private static final ResourceLocation cursed = new ResourceLocation(ModInfo.ID, "textures/entity/keese_cursed.png");
	private static final ResourceLocation fire = new ResourceLocation(ModInfo.ID, "textures/entity/keese_fire.png");
	private static final ResourceLocation ice = new ResourceLocation(ModInfo.ID, "textures/entity/keese_ice.png");
	private static final ResourceLocation thunder = new ResourceLocation(ModInfo.ID, "textures/entity/keese_thunder.png");
	private static final ResourceLocation thunder_shock = new ResourceLocation(ModInfo.ID, "textures/entity/keese_thunder_shock.png");

	public RenderEntityKeese() {
		super();
	}

	protected ResourceLocation getKeeseTextures(EntityKeese keese) {
		switch(keese.getType()) {
		case CURSED: return cursed;
		case FIRE: return fire;
		case ICE: return ice;
		case THUNDER: return (keese.getShockTime() % 8 > 5 ? thunder_shock : thunder);
		default: return base;
		}
	}

	@Override
	protected ResourceLocation getEntityTexture(Entity entity) {
		return getKeeseTextures((EntityKeese) entity);
	}
}
