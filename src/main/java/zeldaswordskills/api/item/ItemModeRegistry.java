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

package zeldaswordskills.api.item;

import net.minecraft.entity.projectile.EntityArrow;
import zeldaswordskills.api.item.ItemMode.ItemModeEntity;
import zeldaswordskills.entity.projectile.EntitySeedShot;

/**
 * 
 * Container for various ICyclableItem mode registries.
 * 
 * Entries should be registered during or after the FML post-initialization event.
 *
 */
public class ItemModeRegistry
{
	/** Registry of cyclable arrow modes for Hero's Bow */
	public static final ItemModeSpecialAmmo<EntityArrow> ARROW_MODES = new ItemModeSpecialAmmo<EntityArrow>();

	/** Registry of cyclable seed shot modes for Slingshots */
	public static final ItemModeEntity<EntitySeedShot> SEED_MODES = new ItemModeEntity<EntitySeedShot>();

}
