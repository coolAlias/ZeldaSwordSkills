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

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import zeldaswordskills.api.item.IHandlePickup;
import zeldaswordskills.api.item.IUnenchantable;
import zeldaswordskills.creativetab.ZSSCreativeTabs;
import zeldaswordskills.entity.player.ZSSPlayerInfo;
import zeldaswordskills.ref.Config;
import zeldaswordskills.ref.Sounds;
import zeldaswordskills.util.PlayerUtils;

/**
 * 
 * A class for Items that cannot be stored in the inventory, but have some
 * sort of effect when picked up (i.e. collided with).
 * 
 * For the item to have an effect, it must extend this class (or use an anonymous class)
 * that provides an implementation for {@link #onPickupItem(ItemStack, EntityPlayer)}
 *
 */
public abstract class ItemPickupOnly extends BaseModItem implements IHandlePickup, IUnenchantable
{
	public ItemPickupOnly() {
		super();
		setMaxStackSize(1);
		setCreativeTab(ZSSCreativeTabs.tabTools);
	}

	public static class ItemMagicJar extends ItemPickupOnly
	{
		/** Amount of magic to restore */
		private final int restoreMp;
		public ItemMagicJar(int restoreMp) {
			super();
			this.restoreMp = restoreMp;
		}
		@Override
		public boolean onPickupItem(ItemStack stack, EntityPlayer player) {
			ZSSPlayerInfo info = ZSSPlayerInfo.get(player);
			if (info.getCurrentMagic() < info.getMaxMagic() || Config.alwaysPickupHearts()) {
				--stack.stackSize;
				info.restoreMagic(restoreMp);
				PlayerUtils.playSound(player, Sounds.SUCCESS_MAGIC, 0.6F, 1.0F);
				return true;
			}
			return false;
		}
	}
}
