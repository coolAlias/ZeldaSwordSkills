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

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderSnowball;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.client.registry.IRenderFactory;

/**
 * 
 * Item damage sensitive version of RenderSnowball
 *
 */
public class RenderItemProjectile<T extends Entity> extends RenderSnowball<T>
{
	protected final int damage;

	public RenderItemProjectile(RenderManager manager, Item item, int damage, RenderItem renderItem) {
		super(manager, item, renderItem);
		this.damage = damage;
	}

	@Override
	public ItemStack func_177082_d(T entity) {
		return new ItemStack(this.field_177084_a, 1, this.damage);
	}

	public static class Factory<T extends Entity> implements IRenderFactory<T>
	{
		private final Item item;
		private final int damage;

		public Factory(Item item, int damage) {
			this.item = item;
			this.damage = damage;
		}

		@Override
		public Render<T> createRenderFor(RenderManager manager) {
			return new RenderItemProjectile<T>(manager, this.item, this.damage, Minecraft.getMinecraft().getRenderItem());
		}
	}
}
