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
import zeldaswordskills.lib.Sounds;
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
		event.manager.addSound(Sounds.BOMB_WHISTLE + ".ogg");
		event.manager.addSound(Sounds.BOSS_BATTLE + ".ogg");
		event.manager.addSound(Sounds.BOSS_SPAWN + ".ogg");
		event.manager.addSound(Sounds.CASH_SALE + ".ogg");
		event.manager.addSound(Sounds.HOOKSHOT + ".ogg");
		event.manager.addSound(Sounds.CHU_MERGE + ".ogg");
		event.manager.addSound(Sounds.CORK + ".ogg");
		event.manager.addSound(Sounds.FAIRY_BLESSING + ".ogg");
		event.manager.addSound(Sounds.FAIRY_LAUGH + ".ogg");
		event.manager.addSound(Sounds.FAIRY_LIVING + ".ogg");
		event.manager.addSound(Sounds.FAIRY_SKILL + ".ogg");
		event.manager.addSound(Sounds.FLAME_ABSORB + ".ogg");
		event.manager.addSound(Sounds.LEVELUP + ".ogg");
		event.manager.addSound(Sounds.LOCK_CHEST + ".ogg");
		event.manager.addSound(Sounds.LOCK_DOOR + ".ogg");
		event.manager.addSound(Sounds.LOCK_RATTLE + ".ogg");
		event.manager.addSound(Sounds.MAGIC_FAIL + ".ogg");
		event.manager.addSound(Sounds.MAGIC_FIRE + ".ogg");
		event.manager.addSound(Sounds.MAGIC_ICE + ".ogg");
		event.manager.addSound(Sounds.MASTER_SWORD + ".ogg");
		event.manager.addSound(Sounds.SECRET_MEDLEY + ".ogg");
		event.manager.addSound(Sounds.SPECIAL_DROP + ".ogg");
		event.manager.addSound(Sounds.SUCCESS + ".ogg");
		event.manager.addSound(Sounds.WEB_SPLAT + ".ogg");
		event.manager.addSound(Sounds.WHOOSH + ".ogg");

		// the following have 2
		for (int i = 1; i < 3; ++i) {
			event.manager.addSound(Sounds.MORTAL_DRAW + String.valueOf(i) + ".ogg");
			event.manager.addSound(Sounds.ROCK_FALL + String.valueOf(i) + ".ogg");
			event.manager.addSound(Sounds.WHIRLWIND + String.valueOf(i) + ".ogg");
		}

		// the following have 3
		for (int i = 1; i < 4; ++i) {
			event.manager.addSound(Sounds.ARMOR_BREAK + String.valueOf(i) + ".ogg");
			event.manager.addSound(Sounds.GRUNT + String.valueOf(i) + ".ogg");
			event.manager.addSound(Sounds.HAMMER + String.valueOf(i) + ".ogg");
			event.manager.addSound(Sounds.HIT_RUSTY + String.valueOf(i) + ".ogg");
			event.manager.addSound(Sounds.HURT_FLESH + String.valueOf(i) + ".ogg");
			event.manager.addSound(Sounds.LEAPING_BLOW + String.valueOf(i) + ".ogg");
			event.manager.addSound(Sounds.SLAM + String.valueOf(i) + ".ogg");
			event.manager.addSound(Sounds.SPIN_ATTACK + String.valueOf(i) + ".ogg");
		}
		// 4 files each
		for (int i = 1; i < 5; ++i) {
			event.manager.addSound(Sounds.BREAK_JAR + String.valueOf(i) + ".ogg");
			event.manager.addSound(Sounds.HIT_PEG + String.valueOf(i) + ".ogg");
			event.manager.addSound(Sounds.SHOCK + String.valueOf(i) + ".ogg");
			event.manager.addSound(Sounds.SWORD_CUT + String.valueOf(i) + ".ogg");
			event.manager.addSound(Sounds.SWORD_MISS + String.valueOf(i) + ".ogg");
			event.manager.addSound(Sounds.SWORD_STRIKE + String.valueOf(i) + ".ogg");
		}
	}
}
