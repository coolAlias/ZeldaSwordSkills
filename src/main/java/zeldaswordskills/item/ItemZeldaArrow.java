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

package zeldaswordskills.item;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import zeldaswordskills.creativetab.ZSSCreativeTabs;
import zeldaswordskills.ref.ModInfo;

/**
 * 
 * A class simply to allow detection of Zelda arrows when using
 * Battlegear2's quiver system.
 *
 */
public class ItemZeldaArrow extends Item
{
	/** Whether this arrow should glow as if enchanted */
	private final boolean isMagic;

	/**
	 * @param name Used as texture name; unlocalized name is 'zss.name'
	 * @param isMagic Gives this arrow the enchanted glow if true
	 */
	public ItemZeldaArrow(String name, boolean isMagic) {
		super();
		this.isMagic = isMagic;
		setUnlocalizedName("zss." + name);
		setTextureName(ModInfo.ID + ":" + name);
		setCreativeTab(ZSSCreativeTabs.tabCombat);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean hasEffect(ItemStack stack, int pass) {
		return isMagic;
	}
}
