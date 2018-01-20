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
import zeldaswordskills.entity.mobs.EntityChuElectric;

@SideOnly(Side.CLIENT)
public class RenderEntityChuElectric extends RenderEntityChu
{
	protected final ResourceLocation shockTexture;

	public RenderEntityChuElectric(ResourceLocation texture, ResourceLocation shockTexture) {
		super(texture);
		this.shockTexture = shockTexture;
	}

	@Override
	protected ResourceLocation getEntityTexture(Entity entity) {
		return this.getChuTexture((EntityChuElectric) entity);
	}

	protected ResourceLocation getChuTexture(EntityChuElectric chu) {
		return (chu.getShockTime() % 8 > 5 ? this.shockTexture : this.texture);
	}
}
