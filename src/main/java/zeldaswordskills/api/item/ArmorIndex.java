/**
    Copyright (C) <2015> <coolAlias>

    This file is part of coolAlias' Zelda Sword Skills Minecraft Mod; as such;
    you can redistribute it and/or modify it under the terms of the GNU
    General Public License as published by the Free Software Foundation;
    either version 3 of the License; or (at your option) any later version.

    This program is distributed in the hope that it will be useful;
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program. If not; see <http://www.gnu.org/licenses/>.
 */

package zeldaswordskills.api.item;

/**
 * 
 * Defined armor indices, because they are all f-ed up in Minecraft
 *
 */
public class ArmorIndex {

	/** Armor type for boots, used only in ItemArmor's constructor */
	public static final int TYPE_BOOTS = 3;
	/** Armor type for leg armor, used only in ItemArmor's constructor */
	public static final int TYPE_LEGS = 2;
	/** Armor type for chest armor, used only in ItemArmor's constructor */
	public static final int TYPE_CHEST = 1;
	/** Armor type for helms, used only in ItemArmor's constructor */
	public static final int TYPE_HELM = 0;
	/** Index for boots when using EntityPlayer.getCurrentItemOrArmor(int) */
	public static final int EQUIPPED_BOOTS = 1;
	/** Index for legs when using EntityPlayer.getCurrentItemOrArmor(int) */
	public static final int EQUIPPED_LEGS = 2;
	/** Index for chest when using EntityPlayer.getCurrentItemOrArmor(int) */
	public static final int EQUIPPED_CHEST = 3;
	/** Index for helm when using EntityPlayer.getCurrentItemOrArmor(int) */
	public static final int EQUIPPED_HELM = 4;
	/** Index for boots when using EntityPlayer.getCurrentArmor(int) */
	public static final int WORN_BOOTS = 0;
	/** Index for legs when using EntityPlayer.getCurrentArmor(int) */
	public static final int WORN_LEGS = 1;
	/** Index for chest when using EntityPlayer.getCurrentArmor(int) */
	public static final int WORN_CHEST = 2;
	/** Index for helm when using EntityPlayer.getCurrentArmor(int) */
	public static final int WORN_HELM = 3;

}
