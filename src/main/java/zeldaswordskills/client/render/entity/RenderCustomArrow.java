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
import net.minecraft.client.renderer.entity.RenderArrow;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import zeldaswordskills.entity.projectile.EntityArrowCustom;

@SideOnly(Side.CLIENT)
public class RenderCustomArrow extends RenderArrow
{
	protected final ResourceLocation texture;

	public RenderCustomArrow(RenderManager manager, ResourceLocation texture) {
		super(manager);
		this.texture = texture;
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityArrow arrow) {
		return this.texture;
	}

	public static class Factory implements IRenderFactory<EntityArrowCustom>
	{
		protected final ResourceLocation texture;
		public Factory(ResourceLocation texture) {
			this.texture = texture;
		}

		@Override
		public Render<? super EntityArrowCustom> createRenderFor(RenderManager manager) {
			return new RenderCustomArrow(manager, this.texture);
		}
	}
}
