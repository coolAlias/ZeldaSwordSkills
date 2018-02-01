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

package zeldaswordskills.api.entity;

import net.minecraft.item.ItemStack;
import net.minecraft.util.Vec3;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import zeldaswordskills.api.entity.CustomExplosion.IgnoreLiquid;
import zeldaswordskills.item.ZSSItems;
import zeldaswordskills.util.WorldUtils;

/**
 * 
 * Use these in conjunction with IEntityBomb and the static createExplosion methods
 * to automatically apply certain characteristics to custom explosions.
 * 
 * If different traits are required, create a CustomExplosion object and change its
 * variables as needed, rather than using the static methods.
 *
 */
public enum BombType {
	/** Applies vanilla explosion rules */
	BOMB_STANDARD("standard", IgnoreLiquid.NONE, true),
	/** Ignores water when determining which blocks to destroy; less effective in the Nether */
	BOMB_WATER("water", IgnoreLiquid.WATER, false),
	/** Ignores lava when determining which blocks to destroy */
	BOMB_FIRE("fire", IgnoreLiquid.LAVA, false),
	/** Same as BOMB_STANDARD, except some bomb flower seeds may be dispersed */
	BOMB_FLOWER("flower", IgnoreLiquid.NONE, true);

	/** The unlocalized name of the bomb type, e.g. 'standard', 'fire', etc. */
	public final String unlocalizedName;

	/** Type of liquid to exclude from the explosion calculations */
	public final IgnoreLiquid ignoreLiquidType;

	/** Whether this bomb type immediately explodes when in the Nether */
	public final boolean explodesInHell;

	private BombType(String name, IgnoreLiquid ignoreLiquidType, boolean explodesInHell) {
		this.unlocalizedName = name;
		this.ignoreLiquidType = ignoreLiquidType;
		this.explodesInHell = explodesInHell;
	}

	/**
	 * Performs any post-explosion effect for this bomb type
	 * Currently only BOMB_FLOWER has any special after-effect
	 */
	public void postExplosionEffect(World world, Explosion explosion) {
		if (this == BOMB_FLOWER) {
			Vec3 pos = explosion.getPosition();
			disperseSeeds(world, pos.xCoord, pos.yCoord, pos.zCoord);
		}
	}

	/**
	 * Scatters several bomb flower seeds around the given coordinates
	 */
	private void disperseSeeds(World world, double x, double y, double z) {
		int n = world.rand.nextInt(3) + 1;
		for (int i = 0; i < n; ++i) {
			WorldUtils.spawnItemWithRandom(world, new ItemStack(ZSSItems.bombFlowerSeed), x, y, z, 0.15F);
		}
	}
}
