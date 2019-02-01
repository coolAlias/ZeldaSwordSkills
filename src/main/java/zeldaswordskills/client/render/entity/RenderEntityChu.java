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
import net.minecraft.client.renderer.entity.RenderSlime;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import zeldaswordskills.entity.mobs.EntityChu;

@SideOnly(Side.CLIENT)
public class RenderEntityChu extends RenderSlime
{
	protected final ResourceLocation texture;

	public RenderEntityChu(RenderManager renderManager, ModelBase model, float shadowSize, ResourceLocation texture) {
		super(renderManager, model, shadowSize);
		this.texture = texture;
	}

	@Override
	protected ResourceLocation getEntityTexture(EntitySlime entity) {
		return this.texture;
	}

	public static class Factory implements IRenderFactory<EntityChu>
	{
		private final ModelBase model;
		private final float shadowSize;
		private final ResourceLocation texture;
		public Factory(ModelBase model, float shadowSize, ResourceLocation texture) {
			this.model = model;
			this.shadowSize = shadowSize;
			this.texture = texture;
		}

		@Override
		public Render<? super EntityChu> createRenderFor(RenderManager manager) {
			return new RenderEntityChu(manager, this.model, this.shadowSize, this.texture);
		}
	}
}
