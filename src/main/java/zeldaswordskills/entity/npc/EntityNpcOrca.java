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

package zeldaswordskills.entity.npc;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import zeldaswordskills.entity.ZSSPlayerSkills;
import zeldaswordskills.item.ItemTreasure;
import zeldaswordskills.item.ItemTreasure.Treasures;
import zeldaswordskills.ref.Sounds;
import zeldaswordskills.util.PlayerUtils;

/**
 * 
 * Orca will teach Link some special sword skills in exchange for Knight's Crests.
 * 
 * Spawned by naming any villager 'Orca' and interacting while holding a Knight's Crest.
 *
 */
public class EntityNpcOrca extends EntityNpcBase
{
	public EntityNpcOrca(World world) {
		super(world);
	}

	@Override
	protected String getNameTagOnSpawn() {
		return "Orca";
	}

	@Override
	protected String getLivingSound() {
		return Sounds.VILLAGER_HAGGLE;
	}

	@Override
	protected String getHurtSound() {
		return Sounds.VILLAGER_HIT;
	}

	@Override
	protected String getDeathSound() {
		return Sounds.VILLAGER_DEATH;
	}

	@Override
	public boolean interact(EntityPlayer player) {
		if (!player.worldObj.isRemote) {
			ItemStack stack = player.getHeldItem();
			if (ZSSPlayerSkills.get(player).completedCrests()) {
				PlayerUtils.sendTranslatedChat(player, "chat.zss.npc.orca.master." + player.worldObj.rand.nextInt(4));
			} else if (stack != null && stack.getItem() instanceof ItemTreasure && stack.getItemDamage() == Treasures.KNIGHTS_CREST.ordinal()) {
				ZSSPlayerSkills.get(player).giveCrest();
			} else {
				PlayerUtils.sendTranslatedChat(player, "chat.zss.npc.orca.idle." + rand.nextInt(4));
			}
		}
		return true;
	}
}
