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

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.IChatComponent;

/**
 * 
 * Simple 'quest' to have Barnes offer a single-use bomb bag trade to the player
 * after the player carelessly waves a ticking bomb in his face.
 * 
 * Quest begins when player shows bomb to Barnes and ends when they purchase the bag.
 *
 */
public final class QuestBombBagTrade extends QuestBase
{
	public QuestBombBagTrade() {}

	@Override
	protected boolean onBegin(EntityPlayer player, Object... data) {
		return true;
	}

	@Override
	public boolean canComplete(EntityPlayer player, Object... data) {
		return false; // prevent accidental completion from generic quest handlers
	}

	@Override
	protected boolean onComplete(EntityPlayer player, Object... data) {
		return true;
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
		return null;
	}

	@Override
	public boolean requiresSync() {
		return true; // used to populated trades which can be used during interaction event on both sides
	}
}
