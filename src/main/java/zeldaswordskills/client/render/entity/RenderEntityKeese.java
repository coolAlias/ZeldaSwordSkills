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
import net.minecraft.client.renderer.entity.RenderBat;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.passive.EntityBat;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * 
 * @author credits to Jones7789 for most of the Keese textures
 *
 */
@SideOnly(Side.CLIENT)
public class RenderEntityKeese extends RenderBat
{
	protected final ResourceLocation texture;

	public RenderEntityKeese(RenderManager renderManager, ResourceLocation texture) {
		super(renderManager);
		this.texture = texture;
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityBat entity) {
		return this.texture;
	}

	public static class Factory implements IRenderFactory<EntityBat>
	{
		private final ResourceLocation texture;
		public Factory(ResourceLocation texture) {
			this.texture = texture;
		}

		@Override
		public Render<? super EntityBat> createRenderFor(RenderManager manager) {
			return new RenderEntityKeese(manager, this.texture);
		}
	}
}
