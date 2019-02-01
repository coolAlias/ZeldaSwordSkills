/**
 * Copyright (C) <2019> <coolAlias>
 *
 * This file is part of coolAlias' Zelda Sword Skills Minecraft Mod; as such,
 * you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package zeldaswordskills.client.render.entity;

import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.passive.EntityBat;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import zeldaswordskills.entity.mobs.EntityKeeseThunder;

@SideOnly(Side.CLIENT)
public class RenderEntityKeeseElectric extends RenderEntityKeese
{
	protected final ResourceLocation shockTexture;

	public RenderEntityKeeseElectric(RenderManager manager, ResourceLocation texture, ResourceLocation shockTexture) {
		super(manager, texture);
		this.shockTexture = shockTexture;
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityBat entity) {
		return this.getKeeseTexture((EntityKeeseThunder) entity);
	}

	protected ResourceLocation getKeeseTexture(EntityKeeseThunder entity) {
		return (entity.getShockTime() % 8 > 5 ? this.shockTexture : this.texture);
	}

	public static class Factory implements IRenderFactory<EntityKeeseThunder>
	{
		private final ResourceLocation texture;
		private final ResourceLocation shockTexture;
		public Factory(ResourceLocation texture, ResourceLocation shockTexture) {
			this.texture = texture;
			this.shockTexture = shockTexture;
		}

		@Override
		public Render<? super EntityBat> createRenderFor(RenderManager manager) {
			return new RenderEntityKeeseElectric(manager, this.texture, this.shockTexture);
		}
	}
}
