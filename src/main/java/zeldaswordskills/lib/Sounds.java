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

package zeldaswordskills.lib;

public class Sounds {

	// VANILLA SOUNDS
	public static final String BAT_HURT = "mob.bat.hurt";
	public static final String BOW_RELEASE = "random.bow";
	public static final String BOW_HIT = "random.bowhit";
	public static final String CLICK = "random.click";
	/** "damage.hit" is registered to "game.[entity].hurt", where [entity] may also be a category such as "neutral"
	 * "random.successful_hit" is the sound played when a player lands an attack, which is actually what we want here */
	//public static final String DAMAGE_HIT = "damage.hit";
	public static final String DAMAGE_SUCCESSFUL_HIT = "random.successful_hit";
	//public static final String DAMAGE_HOSTILE_HIT = "game.hostile.hurt";
	public static final String EXPLOSION = "random.explode";
	public static final String FIRE_FIZZ = "random.fizz";
	public static final String FIRE_IGNITE = "fire.ignite";
	public static final String ITEM_BREAK = "random.break";
	public static final String GLASS_BREAK = "dig.glass"; // changed from 'random.glass'
	public static final String POP = "random.pop";
	public static final String SLIME_ATTACK = "mob.slime.attack";
	public static final String SPLASH = "random.splash";
	public static final String VILLAGER_DEATH = "mob.villager.death";
	public static final String VILLAGER_HAGGLE = "mob.villager.haggle";
	public static final String VILLAGER_HIT = "mob.villager.hit";
	public static final String VILLAGER_IDLE = "mob.villager.idle";
	public static final String WOOD_CLICK = "random.wood_click";
	public static final String XP_ORB = "random.orb";

	// BLOCK SOUNDS
	public static final String BREAK_JAR = ModInfo.ID + ":break_jar";
	public static final String HIT_PEG = ModInfo.ID + ":hit_peg";
	public static final String HIT_RUSTY = ModInfo.ID + ":hit_rusty";
	public static final String LOCK_CHEST = ModInfo.ID + ":lock_chest";
	public static final String LOCK_DOOR = ModInfo.ID + ":lock_door";
	public static final String LOCK_RATTLE = ModInfo.ID + ":lock_rattle";
	public static final String ROCK_FALL = ModInfo.ID + ":rock_fall";
	public static final String WEB_SPLAT = ModInfo.ID + ":web_splat";

	// ENTITY SOUNDS
	public static final String BOMB_FUSE = ModInfo.ID + ":bomb_fuse";
	public static final String BOMB_WHISTLE = ModInfo.ID + ":bomb_whistle";
	public static final String CHU_MERGE = ModInfo.ID + ":chu_merge";
	public static final String FAIRY_BLESSING = ModInfo.ID + ":fairy_blessing";
	public static final String FAIRY_LAUGH = ModInfo.ID + ":fairy_laugh";
	public static final String FAIRY_LIVING = ModInfo.ID + ":fairy_living";
	public static final String FAIRY_SKILL = ModInfo.ID + ":fairy_skill";
	public static final String SHOCK = ModInfo.ID + ":shock";
	public static final String WHIP_CRACK = ModInfo.ID + ":whip_crack";
	public static final String WHIRLWIND = ModInfo.ID + ":whirlwind";

	// ITEM SOUNDS
	public static final String CORK = ModInfo.ID + ":cork";
	public static final String HAMMER = ModInfo.ID + ":hammer";
	public static final String HOOKSHOT = ModInfo.ID + ":hookshot";
	public static final String MAGIC_FAIL = ModInfo.ID + ":magic_failure";
	public static final String MAGIC_FIRE = ModInfo.ID + ":magic_fire";
	public static final String MAGIC_ICE = ModInfo.ID + ":magic_ice";
	public static final String WHIP = ModInfo.ID + ":whip";
	public static final String WHOOSH = ModInfo.ID + ":whoosh";

	// PLAYER SOUNDS
	public static final String GRUNT = ModInfo.ID + ":grunt";

	// SPECIAL EVENT SOUNDS
	public static final String BOSS_BATTLE = ModInfo.ID + ":boss_battle";
	public static final String BOSS_SPAWN = ModInfo.ID + ":boss_spawn";
	public static final String CASH_SALE = ModInfo.ID + ":cash_sale";
	public static final String FLAME_ABSORB = ModInfo.ID + ":flame_absorb";
	public static final String LEVELUP = ModInfo.ID + ":levelup";
	public static final String MASTER_SWORD = ModInfo.ID + ":master_sword";
	public static final String SECRET_MEDLEY = ModInfo.ID + ":secret_medley";
	public static final String SPECIAL_DROP = ModInfo.ID + ":special_drop";
	public static final String SUCCESS = ModInfo.ID + ":success";

	// SWORD SKILL SOUNDS
	public static final String ARMOR_BREAK = ModInfo.ID + ":armorbreak";
	public static final String LEAPING_BLOW = ModInfo.ID + ":leapingblow";
	public static final String HURT_FLESH = ModInfo.ID + ":hurtflesh";
	public static final String MORTAL_DRAW = ModInfo.ID + ":mortaldraw";
	public static final String SLAM = ModInfo.ID + ":slam";
	public static final String SPIN_ATTACK = ModInfo.ID + ":spinattack";
	public static final String SWORD_CUT = ModInfo.ID + ":swordcut";
	public static final String SWORD_MISS = ModInfo.ID + ":swordmiss";
	public static final String SWORD_STRIKE = ModInfo.ID + ":swordstrike";

}
