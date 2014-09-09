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

package zeldaswordskills.skills;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * 
 * Interface for Skills that are Combo-capable. Only one such skill should be active at a time.
 *
 */
public interface ICombo {

	/** Returns the Combo instance for associated class */
	public Combo getCombo();

	/** Should assign the instance of Combo retrieved from getCombo() to the argument combo */
	public void setCombo(Combo combo);

	/** Returns true if a combo is currently in progress */
	public boolean isComboInProgress();

	/**
	 * Should be called every time the player attacks (e.g. using MouseEvent for left click attack.)
	 * If a combo should end when the player misses, this is the place to handle that.
	 * @return true if the attack should continue as normal (i.e. the player will hit the target)
	 */
	@SideOnly(Side.CLIENT)
	public boolean onAttack(EntityPlayer player);

	/**
	 * Should be called when an EntityPlayer actively using a Combo damages an entity, creating a new
	 * Combo if necessary and either combo.add(player, damage) or combo.addDamageOnly(player, damage).
	 * LivingHurtEvent is only called server side, but Combo will update itself automatically.
	 * @param player should be gotten from '(EntityPlayer) event.source.getEntity()' if event.source.getEntity() is correct type
	 */
	public void onHurtTarget(EntityPlayer player, LivingHurtEvent event);

	/**
	 * Should be called when a player actively using a Combo receives damage. Useful for ending a
	 * combo when damage exceeds a certain threshold. Note that LivingHurtEvent only gets called
	 * on the server side, but Combo class will self-update if endCombo(player) is called.
	 * @param player should be gotten from '(EntityPlayer) event.entity' if event.entity is correct type
	 */
	public void onPlayerHurt(EntityPlayer player, LivingHurtEvent event);
}
