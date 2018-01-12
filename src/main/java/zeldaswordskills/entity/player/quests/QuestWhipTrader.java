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
import zeldaswordskills.api.block.IWhipBlock.WhipType;
import zeldaswordskills.api.entity.EnumVillager;
import zeldaswordskills.api.item.RupeeValueRegistry;
import zeldaswordskills.entity.player.ZSSPlayerWallet;
import zeldaswordskills.item.ItemWhip;
import zeldaswordskills.item.ZSSItems;
import zeldaswordskills.ref.Sounds;
import zeldaswordskills.util.PlayerUtils;

/**
 * 
 * Simple quest that acts as a flag for adding or removing whip trades for villagers.
 * 
 */
public class QuestWhipTrader extends QuestBase
{
	public QuestWhipTrader() {
		this.set(FLAG_BEGIN); // quest is automatically begun
	}

	/**
	 * Number of rupees required to upgrade from a short to a long whip.
	 * <br>Player gets a discount the first time they upgrade.
	 */
	public int getPrice(EntityPlayer player) {
		ItemStack stack = new ItemStack(ZSSItems.whip, 1, WhipType.WHIP_SHORT.ordinal());
		int price = RupeeValueRegistry.getRupeeValue(stack, ((ItemWhip) ZSSItems.whip).getDefaultRupeeValue(stack));
		return (this.isComplete(player) ? price * 2 : price);
	}

	@Override
	protected boolean onBegin(EntityPlayer player, Object... data) {
		return false; // already begun
	}

	/**
	 * Expects data[0] to be the EntityVillager
	 */
	@Override
	public boolean canComplete(EntityPlayer player, Object... data) {
		if (!super.canComplete(player, data) || data == null || !(data[0] instanceof EntityVillager)) {
			return false;
		} else if (!EnumVillager.BUTCHER.is((EntityVillager) data[0])) {
			return false;
		} else if (ZSSPlayerWallet.get(player).getRupees() < this.getPrice(player)) {
			return false;
		}
		ItemStack stack = player.getHeldItem();
		return stack != null && stack.getItem() == ZSSItems.whip && stack.getItemDamage() == WhipType.WHIP_SHORT.ordinal();
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
		if (hasBegun(player) && !isComplete(player) && data != null && data[0] instanceof EntityVillager) {
			EnumVillager profession = EnumVillager.get((EntityVillager) data[0]);
			if (profession != null) {
				return new ChatComponentTranslation("chat.zss.whip.hint." + profession.unlocalizedName, this.getPrice(player));
			} else {
				return new ChatComponentTranslation("chat.zss.whip.hint.generic", this.getPrice(player));
			}
		}
		return null;
	}

	@Override
	protected boolean onComplete(EntityPlayer player, Object... data) {
		int price = this.getPrice(player);
		boolean flag = ZSSPlayerWallet.get(player).spendRupees(price);
		if (flag && !PlayerUtils.consumeHeldItem(player, ZSSItems.whip, WhipType.WHIP_SHORT.ordinal(), 1)) {
			ZSSPlayerWallet.get(player).addRupees(price);
			return false;
		}
		PlayerUtils.playSound(player, Sounds.SUCCESS, 1.0F, 1.0F);
		player.addChatMessage(new ChatComponentTranslation("chat.zss.whip.butcher.complete"));
		player.setCurrentItemOrArmor(0, new ItemStack(ZSSItems.whip, 1, WhipType.WHIP_LONG.ordinal()));
		return true;
	}
}
