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

package zeldaswordskills.entity.player;

import java.util.EnumMap;
import java.util.Map.Entry;

import com.google.common.collect.Maps;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;
import zeldaswordskills.entity.mobs.EntityChu.ChuType;

/**
 * 
 * Tracker for the number of Chu Jellies a player has given to Doc Bandam.
 *
 */
public class ChuJellyTracker
{
	private final EnumMap<ChuType, Integer> jelliesGiven;

	public ChuJellyTracker() {
		this.jelliesGiven = Maps.newEnumMap(ChuType.class);
		for (ChuType type : ChuType.values()) {
			this.jelliesGiven.put(type, 0);
		}
	}

	public static ChuJellyTracker get(EntityPlayer player) {
		return ZSSPlayerInfo.get(player).getChuJellyTracker();
	}

	/**
	 * Adds the given amount of chu jellies to the current amount
	 */
	public void addJelly(ChuType type, int amount) {
		this.jelliesGiven.put(type, this.getJelliesReceived(type) + amount);
	}

	/**
	 * Returns the number of jellies of this type that the villager has received
	 */
	public int getJelliesReceived(ChuType type) {
		return this.jelliesGiven.get(type);
	}

	/**
	 * True if the player has given enough jellies to purchase Chu potions of this type
	 */
	public boolean canBuyType(ChuType type) {
		return this.getJelliesReceived(type) > 14;
	}

	/**
	 * Gives jellies to the villager up to the next free potion amount
	 * @return true if the player should receive a free potion
	 */
	public boolean giveJellies(ChuType type, ItemStack stack) {
		int jellies = this.getJelliesReceived(type);
		int required = 15 - (jellies % 15);
		while (required > 0 && stack.stackSize > 0) {
			--stack.stackSize;
			++jellies;
			--required;
		}
		this.jelliesGiven.put(type, jellies);
		return required == 0;
	}

	public void saveNBTData(NBTTagCompound compound) {
		NBTTagList taglist = new NBTTagList();
		for (Entry<ChuType, Integer> entry : this.jelliesGiven.entrySet()) {
			NBTTagCompound skillTag = new NBTTagCompound();
			skillTag.setInteger("ordinal", entry.getKey().ordinal());
			skillTag.setInteger("value", entry.getValue());
			taglist.appendTag(skillTag);
		}
		compound.setTag("ChuJellyTracker", taglist);
	}

	public void loadNBTData(NBTTagCompound compound) {
		NBTTagList taglist = compound.getTagList("ChuJellyTracker", Constants.NBT.TAG_COMPOUND);
		for (int i = 0; i < taglist.tagCount(); ++i) {
			NBTTagCompound entry = taglist.getCompoundTagAt(i);
			ChuType type = ChuType.values()[entry.getInteger("ordinal") % ChuType.values().length];
			this.jelliesGiven.put(type, entry.getInteger("value"));
		}
	}
}
