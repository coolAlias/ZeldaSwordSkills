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

package zeldaswordskills.item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import zeldaswordskills.api.item.IMagicArrow;
import zeldaswordskills.api.item.ISpecialAmmunition;
import zeldaswordskills.creativetab.ZSSCreativeTabs;
import zeldaswordskills.ref.ModInfo;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * 
 * A class simply to allow detection of Zelda arrows when using
 * Battlegear2's quiver system.
 *
 */
public class ItemZeldaArrow extends Item implements IRupeeValue, ISpecialAmmunition
{
	/** Default purchase price, in rupees */
	private final int price;

	/** Required level of Hero's Bow to fire this arrow */
	private final int level;

	/**
	 * @param name Used as texture name; unlocalized name is 'zss.name'
	 */
	public ItemZeldaArrow(String name, int price, int level) {
		super();
		this.price = price;
		this.level = level;
		setUnlocalizedName("zss." + name);
		setTextureName(ModInfo.ID + ":" + name);
		setCreativeTab(ZSSCreativeTabs.tabCombat);
	}

	@Override
	public int getDefaultRupeeValue(ItemStack stack) {
		return this.price;
	}

	@Override
	public int getRequiredLevelForAmmo(ItemStack stack) {
		return this.level;
	}

	public static class ItemMagicArrow extends ItemZeldaArrow implements IMagicArrow
	{
		/** Magic cost to shoot this arrow */
		private final float magic;

		public ItemMagicArrow(String name, int price, int level, float magic) {
			super(name, price, level);
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
