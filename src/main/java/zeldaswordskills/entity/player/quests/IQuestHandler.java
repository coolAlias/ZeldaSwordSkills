/**
 * Copyright (C) <2019> <coolAlias>
 *
 * This file is part of coolAlias' Zelda Sword Skills Minecraft Mod; as such,
 * you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package zeldaswordskills.entity.player.quests;

import net.minecraft.entity.player.EntityPlayer;

public interface IQuestHandler {

	/**
	 * Call right after the quest has begun {@link IQuest#begin}
	 */
	void onQuestBegun(IQuest quest, EntityPlayer player);

	/**
	 * Call whenever a quest's status changes from {@link IQuest#update}
	 */
	void onQuestChanged(IQuest quest, EntityPlayer player);

	/**
	 * Call right after the quest has been completed {@link IQuest#complete}
	 */
	void onQuestCompleted(IQuest quest, EntityPlayer player);

}
