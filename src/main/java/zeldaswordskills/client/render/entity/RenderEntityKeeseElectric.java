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
import zeldaswordskills.entity.mobs.EntityKeeseThunder;

@SideOnly(Side.CLIENT)
public class RenderEntityKeeseElectric extends RenderEntityKeese
{
	protected final ResourceLocation shockTexture;

	public RenderEntityKeeseElectric(ResourceLocation texture, ResourceLocation shockTexture) {
		super(texture);
		this.shockTexture = shockTexture;
	}

	@Override
	protected ResourceLocation getEntityTexture(Entity entity) {
		return this.getKeeseTexture((EntityKeeseThunder) entity);
	}

	protected ResourceLocation getKeeseTexture(EntityKeeseThunder entity) {
		return (entity.getShockTime() % 8 > 5 ? this.shockTexture : this.texture);
	}
}
