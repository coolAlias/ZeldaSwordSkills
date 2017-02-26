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

package zeldaswordskills.item.crafting;

import java.lang.reflect.Field;

import com.google.common.base.Throwables;

import cpw.mods.fml.relauncher.ReflectionHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.inventory.ContainerWorkbench;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.SlotCrafting;
import net.minecraft.item.crafting.IRecipe;

/**
 * 
 * Base class for crafting recipes that depend on the player.
 * 
 * Based on code by diesieben07 found at http://www.minecraftforge.net/forum/topic/22927-player-based-crafting-recipes/.
 *
 */
public abstract class RecipePlayerBased implements IRecipe
{
	protected static final Field eventHandlerField = ReflectionHelper.findField(InventoryCrafting.class, "field_70465_c", "eventHandler");
	protected static final Field containerPlayerPlayerField = ReflectionHelper.findField(ContainerPlayer.class, "field_82862_h", "thePlayer");
	protected static final Field slotCraftingPlayerField = ReflectionHelper.findField(SlotCrafting.class, "field_75238_b", "thePlayer");

	protected static EntityPlayer findPlayer(InventoryCrafting inv) {
		try {
			Container container = (Container) eventHandlerField.get(inv);
			if (container instanceof ContainerPlayer) {
				return (EntityPlayer) containerPlayerPlayerField.get(container);
			} else if (container instanceof ContainerWorkbench) {
				return (EntityPlayer) slotCraftingPlayerField.get(container.getSlot(0));
			}
		} catch (Exception e) {
			throw Throwables.propagate(e);
		}
		return null;
	}
}
