/**
    Copyright (C) <2017> <coolAlias>

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

package zeldaswordskills.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import zeldaswordskills.entity.player.ZSSPlayerSkills;
import zeldaswordskills.item.ZSSItems;
import zeldaswordskills.skills.SkillBase;

/**
 * 
 * Container class for interacting with the Skills Gui; slots hold skill orbs for
 * each skill the player has, but cannot be taken or moved.
 *
 */
public class ContainerSkills extends AbstractContainer
{
	private final IInventory inventory;

	public ContainerSkills(EntityPlayer player) {
		ZSSPlayerSkills skills = ZSSPlayerSkills.get(player);
		inventory = new InventoryBasic("", true, SkillBase.getNumSkills());
		boolean flag = false;
		int x, y, i1;

		for (SkillBase skill : SkillBase.getSkills()) {
			if (skills.hasSkill(skill)) {
				inventory.setInventorySlotContents(skill.getId(), new ItemStack(ZSSItems.skillOrb, 1, skill.getId()));
			}
		}

		addSlotToContainer(new Slot(inventory, 0, 65, 141));

		for (int i = 1; i < inventory.getSizeInventory(); ++i) {
			if (i == SkillBase.bonusHeart.getId()) {
				flag = true;
				continue;
			}
			int bottom = 3;
			int sideBar = 5;
			int rightSide = bottom + sideBar;
			i1 = (flag ? i - 1 : i);
			if (i1 > bottom) {
				x = (i1 > rightSide ? 108 : 22);
				y = 120 - (i1 > rightSide ? (i1 - (rightSide + 1)) : (i1 - (sideBar - 1))) * 21;
			} else {
				x = 44 + (i1 - 1) * 21;
				y = 120;
			}
			addSlotToContainer(new Slot(inventory, i, x, y));
		}
	}

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return true;
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int index) {
		return null;
	}

	@Override
	public ItemStack slotClick(int slotIndex, int button, int par3, EntityPlayer player) {
		return null;
	}
}
