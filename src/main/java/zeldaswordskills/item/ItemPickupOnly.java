/**
    Copyright (C) <2014> <coolAlias>

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

import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import zeldaswordskills.api.item.IHandlePickup;
import zeldaswordskills.entity.ZSSEntityInfo;
import zeldaswordskills.entity.buff.Buff;
import zeldaswordskills.lib.Config;
import zeldaswordskills.lib.ModInfo;
import zeldaswordskills.util.PlayerUtils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * 
 * A class for Items that cannot be stored in the inventory, but have some
 * sort of effect when picked up (i.e. collided with)
 *
 */
public class ItemPickupOnly extends Item implements IHandlePickup {

	public ItemPickupOnly(int id) {
		super(id);
		setMaxStackSize(1);
	}
	
	@Override
	public boolean onPickupItem(ItemStack stack, EntityPlayer player) {
		if (this == ZSSItems.smallHeart) {
			if (player.getHealth() < player.getMaxHealth() || Config.alwaysPickupHearts()) {
				player.heal(1.0F);
			} else {
				return false;
			}
		} else if (this == ZSSItems.powerPiece) {
			PlayerUtils.playSound(player, ModInfo.SOUND_SUCCESS, 0.6F, 1.0F);
			ZSSEntityInfo.get(player).applyBuff(Buff.ATTACK_UP, 600, 100);
			ZSSEntityInfo.get(player).applyBuff(Buff.EVADE_UP, 600, 25);
			ZSSEntityInfo.get(player).applyBuff(Buff.RESIST_STUN, 600, 100);
		}
		stack.stackSize = 0;
		return true;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister register) {
		itemIcon = register.registerIcon(ModInfo.ID + ":" + getUnlocalizedName().substring(9));
	}
}
