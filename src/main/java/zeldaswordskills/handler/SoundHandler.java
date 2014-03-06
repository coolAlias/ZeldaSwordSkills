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

package zeldaswordskills.handler;

import net.minecraftforge.client.event.sound.SoundLoadEvent;
import net.minecraftforge.event.ForgeSubscribe;
import zeldaswordskills.lib.ModInfo;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * 
 * SoundManager is only available on the client side; when run on a server, it will crash
 * if sounds are registered indiscriminately.
 *
 */
@SideOnly(Side.CLIENT)
public class SoundHandler {
	
	@ForgeSubscribe
	public void onLoadSound(SoundLoadEvent event) {
		// the following sounds have only 1 file each
		event.manager.addSound(ModInfo.SOUND_BOMB_WHISTLE + ".ogg");
		event.manager.addSound(ModInfo.SOUND_BOSS_BATTLE + ".ogg");
		event.manager.addSound(ModInfo.SOUND_BOSS_SPAWN + ".ogg");
		event.manager.addSound(ModInfo.SOUND_CASH_SALE + ".ogg");
		event.manager.addSound(ModInfo.SOUND_CHAIN + ".ogg");
		event.manager.addSound(ModInfo.SOUND_CHU_MERGE + ".ogg");
		event.manager.addSound(ModInfo.SOUND_CORK + ".ogg");
		event.manager.addSound(ModInfo.SOUND_FAIRY_BLESSING + ".ogg");
		event.manager.addSound(ModInfo.SOUND_FAIRY_LAUGH + ".ogg");
		event.manager.addSound(ModInfo.SOUND_FAIRY_LIVING + ".ogg");
		event.manager.addSound(ModInfo.SOUND_FAIRY_SKILL + ".ogg");
		event.manager.addSound(ModInfo.SOUND_FLAME_ABSORB + ".ogg");
		event.manager.addSound(ModInfo.SOUND_LEVELUP + ".ogg");
		event.manager.addSound(ModInfo.SOUND_LOCK_CHEST + ".ogg");
		event.manager.addSound(ModInfo.SOUND_LOCK_DOOR + ".ogg");
		event.manager.addSound(ModInfo.SOUND_LOCK_RATTLE + ".ogg");
		event.manager.addSound(ModInfo.SOUND_MAGIC_FAIL + ".ogg");
		event.manager.addSound(ModInfo.SOUND_MASTER_SWORD + ".ogg");
		event.manager.addSound(ModInfo.SOUND_SECRET_MEDLEY + ".ogg");
		event.manager.addSound(ModInfo.SOUND_SPECIAL_DROP + ".ogg");
		event.manager.addSound(ModInfo.SOUND_SUCCESS + ".ogg");
		event.manager.addSound(ModInfo.SOUND_WEB_SPLAT + ".ogg");
		event.manager.addSound(ModInfo.SOUND_WHOOSH + ".ogg");

		// the following have 2
		for (int i = 1; i < 3; ++i) {
			event.manager.addSound(ModInfo.SOUND_MORTALDRAW + String.valueOf(i) + ".ogg");
			event.manager.addSound(ModInfo.SOUND_ROCK_FALL + String.valueOf(i) + ".ogg");
			event.manager.addSound(ModInfo.SOUND_WHIRLWIND + String.valueOf(i) + ".ogg");
		}
		
		// the following have 3
		for (int i = 1; i < 4; ++i) {
			event.manager.addSound(ModInfo.SOUND_ARMORBREAK + String.valueOf(i) + ".ogg");
			event.manager.addSound(ModInfo.SOUND_HAMMER + String.valueOf(i) + ".ogg");
			event.manager.addSound(ModInfo.SOUND_HIT_RUSTY + String.valueOf(i) + ".ogg");
			event.manager.addSound(ModInfo.SOUND_LEAPINGBLOW + String.valueOf(i) + ".ogg");
			event.manager.addSound(ModInfo.SOUND_SLAM + String.valueOf(i) + ".ogg");
			event.manager.addSound(ModInfo.SOUND_SPINATTACK + String.valueOf(i) + ".ogg");
			event.manager.addSound(ModInfo.SOUND_YELL + String.valueOf(i) + ".ogg");
		}
		// 4 files each
		for (int i = 1; i < 5; ++i) {
			event.manager.addSound(ModInfo.SOUND_BREAK_JAR + String.valueOf(i) + ".ogg");
			event.manager.addSound(ModInfo.SOUND_HIT_PEG + String.valueOf(i) + ".ogg");
			event.manager.addSound(ModInfo.SOUND_SHOCK + String.valueOf(i) + ".ogg");
			event.manager.addSound(ModInfo.SOUND_SWORDCUT + String.valueOf(i) + ".ogg");
			event.manager.addSound(ModInfo.SOUND_SWORDMISS + String.valueOf(i) + ".ogg");
			event.manager.addSound(ModInfo.SOUND_SWORDSTRIKE + String.valueOf(i) + ".ogg");
		}
	}
}
