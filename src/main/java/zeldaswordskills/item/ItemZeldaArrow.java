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

package zeldaswordskills.item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import zeldaswordskills.api.item.IMagicArrow;
import zeldaswordskills.api.item.ISpecialAmmunition;
import zeldaswordskills.creativetab.ZSSCreativeTabs;

/**
 * 
 * A class simply to allow detection of Zelda arrows when using
 * Battlegear2's quiver system.
 *
 */
public class ItemZeldaArrow extends BaseModItem implements ISpecialAmmunition
{
	/** Required level of Hero's Bow to fire this arrow */
	private final int level;

	/**
	 * @param name Unlocalized name
	 */
	public ItemZeldaArrow(String name, int level) {
		super();
		this.level = level;
		setUnlocalizedName(name);
		setCreativeTab(ZSSCreativeTabs.tabCombat);
	}

	@Override
	public int getRequiredLevelForAmmo(ItemStack stack) {
		return this.level;
	}

	public static class ItemMagicArrow extends ItemZeldaArrow implements IMagicArrow
	{
		/** Magic cost to shoot this arrow */
		private final float magic;

		public ItemMagicArrow(String name, int level, float magic) {
			super(name, level);
			this.magic = magic;
		}

		@Override
		public float getMagicCost(ItemStack arrow, EntityPlayer player) {
			return this.magic;
		}

		@Override
		@SideOnly(Side.CLIENT)
		public boolean hasEffect(ItemStack stack) {
			return true;
		}
	}
}
