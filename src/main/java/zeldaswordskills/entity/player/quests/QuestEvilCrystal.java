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

package zeldaswordskills.entity.player.quests;

import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.IChatComponent;
import zeldaswordskills.api.entity.EnumVillager;
import zeldaswordskills.item.ItemTreasure.Treasures;
import zeldaswordskills.item.ZSSItems;
import zeldaswordskills.ref.Sounds;
import zeldaswordskills.util.PlayerUtils;
import zeldaswordskills.util.TimedAddItem;
import zeldaswordskills.util.TimedChatDialogue;

/**
 * 
 * Repeatable quest to convert evil crystals to empty spirit crystals.
 * 
 */
public class QuestEvilCrystal extends QuestBase
{
	/** Number of light arrows required to cleans each Evil Crystal */
	public static int getArrowsRequired() {
		return 16;
	}

	public QuestEvilCrystal() {
		this.set(FLAG_BEGIN); // quest is automatically begun
	}

	@Override
	protected boolean onBegin(EntityPlayer player, Object... data) {
		return false; // already begun
	}

	/**
	 * Expects data[0] to be the EntityVillager and data[1] to be true if left clicking
	 */
	@Override
	public boolean canComplete(EntityPlayer player, Object... data) {
		if (data == null || data[0].getClass() != EntityVillager.class) {
			return false;
		} else if (!EnumVillager.PRIEST.is((EntityVillager) data[0])) {
			return false;
		} else if (data.length < 2 || !(data[1] instanceof Boolean) || !((Boolean) data[1])) {
			return false;
		}
		ItemStack stack = player.getHeldItem();
		if (stack == null || stack.getItem() != ZSSItems.treasure || Treasures.byDamage(stack.getItemDamage()) != Treasures.EVIL_CRYSTAL) {
			return false;
		}
		return PlayerUtils.hasItem(player, ZSSItems.arrowLight, -1, QuestEvilCrystal.getArrowsRequired());
	}

	@Override
	public void forceComplete(EntityPlayer player, Object... data) {
		this.set(FLAG_COMPLETE);
	}

	@Override
	public boolean update(EntityPlayer player, Object... data) {
		return false;
	}

	@Override
	public IChatComponent getHint(EntityPlayer player, Object... data) {
		ItemStack stack = player.getHeldItem();
		if (stack == null || stack.getItem() != ZSSItems.treasure || Treasures.byDamage(stack.getItemDamage()) != Treasures.EVIL_CRYSTAL) {
			return null;
		}
		if (data != null && data[0].getClass() == EntityVillager.class) {
			EnumVillager profession = EnumVillager.get((EntityVillager) data[0]);
			if (profession == EnumVillager.PRIEST) {
				boolean isLeftClick = (data.length > 1 && data[1] instanceof Boolean && (Boolean) data[1]);
				if (!PlayerUtils.hasItem(player, ZSSItems.arrowLight, -1, 1)) {
					return new ChatComponentTranslation("chat.zss.treasure.evil_crystal.none");
				} else if (!PlayerUtils.hasItem(player, ZSSItems.arrowLight, -1, QuestEvilCrystal.getArrowsRequired())) {
					return new ChatComponentTranslation("chat.zss.treasure.evil_crystal.insufficient");
				} else if (!isLeftClick) {
					return new ChatComponentTranslation("chat.zss.treasure.evil_crystal.hint." + profession.unlocalizedName);
				}
			} else if (profession != null) {
				return new ChatComponentTranslation("chat.zss.treasure.evil_crystal.hint." + profession.unlocalizedName);
			} else {
				return new ChatComponentTranslation("chat.zss.treasure.evil_crystal.hint.generic");
			}
		}
		return null;
	}

	@Override
	protected boolean onComplete(EntityPlayer player, Object... data) {
		boolean flag = PlayerUtils.consumeHeldItem(player, ZSSItems.treasure, Treasures.EVIL_CRYSTAL.ordinal(), 1); 
		if (flag && !PlayerUtils.consumeInventoryItem(player, ZSSItems.arrowLight, QuestEvilCrystal.getArrowsRequired())) {
			PlayerUtils.addItemToInventory(player, new ItemStack(ZSSItems.treasure, 1, Treasures.EVIL_CRYSTAL.ordinal()));
			return false;
		}
		new TimedChatDialogue(player,
				new ChatComponentTranslation("chat.zss.treasure.evil_crystal.complete.0"),
				new ChatComponentTranslation("chat.zss.treasure.evil_crystal.complete.1"));
		new TimedAddItem(player, new ItemStack(ZSSItems.crystalSpirit), 1250, Sounds.SUCCESS);
		return true;
	}
}
