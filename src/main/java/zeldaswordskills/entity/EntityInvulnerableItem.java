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

package zeldaswordskills.entity;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

/**
 * 
 * Standard EntityItem but invulnerable to damage
 *
 */
public class EntityInvulnerableItem extends EntityItem {

	public EntityInvulnerableItem(World world) {
		super(world);
	}

	public EntityInvulnerableItem(World world, double x, double y, double z) {
		super(world, x, y, z);
	}

	public EntityInvulnerableItem(World world, double x, double y, double z, ItemStack stack) {
		super(world, x, y, z, stack);
	}

	@Override
	public boolean isEntityInvulnerable(DamageSource source) {
		return source != DamageSource.outOfWorld;
	}
}
