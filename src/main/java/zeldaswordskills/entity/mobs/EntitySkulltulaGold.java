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

package zeldaswordskills.entity.mobs;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import zeldaswordskills.api.block.IWhipBlock.WhipType;
import zeldaswordskills.item.ZSSItems;

public class EntitySkulltulaGold extends EntitySkulltula
{
	public EntitySkulltulaGold(World world) {
		super(world);
	}

	@Override
	public int getTotalArmorValue() {
		return super.getTotalArmorValue() + 2;
	}

	@Override
	protected void dropFewItems(boolean recentlyHit, int lootingLevel) {
		super.dropFewItems(recentlyHit, lootingLevel);
		this.entityDropItem(new ItemStack(ZSSItems.skulltulaToken), 0.0F);
	}

	@Override
	public ItemStack getEntityLoot(EntityPlayer player, WhipType whip) {
		return new ItemStack(ZSSItems.skulltulaToken);
	}

	@Override
	public boolean onLootStolen(EntityPlayer player, boolean wasItemStolen) {
		return wasItemStolen;
	}
}
