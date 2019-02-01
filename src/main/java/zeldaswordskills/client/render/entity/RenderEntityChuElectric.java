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

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import zeldaswordskills.entity.mobs.EntityChuElectric;

@SideOnly(Side.CLIENT)
public class RenderEntityChuElectric extends RenderEntityChu
{
	protected final ResourceLocation shockTexture;

	public RenderEntityChuElectric(RenderManager renderManager, ModelBase model, float shadowSize, ResourceLocation texture, ResourceLocation shockTexture) {
		super(renderManager, model, shadowSize, texture);
		this.shockTexture = shockTexture;
	}

	@Override
	protected ResourceLocation getEntityTexture(EntitySlime entity) {
		return this.getChuTexture((EntityChuElectric) entity);
	}

	protected ResourceLocation getChuTexture(EntityChuElectric chu) {
		return (chu.getShockTime() % 8 > 5 ? this.shockTexture : this.texture);
	}

	public static class Factory implements IRenderFactory<EntityChuElectric>
	{
		private final ModelBase model;
		private final float shadowSize;
		private final ResourceLocation texture;
		private final ResourceLocation shockTexture;
		public Factory(ModelBase model, float shadowSize, ResourceLocation texture, ResourceLocation shockTexture) {
			this.model = model;
			this.shadowSize = shadowSize;
			this.texture = texture;
			this.shockTexture = shockTexture;
		}

		@Override
		public Render<? super EntityChuElectric> createRenderFor(RenderManager manager) {
			return new RenderEntityChuElectric(manager, this.model, this.shadowSize, this.texture, this.shockTexture);
		}
	}
}
