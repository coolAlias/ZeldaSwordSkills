/**
    Copyright (C) <2015> <coolAlias>

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

package zeldaswordskills;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.Achievement;
import net.minecraftforge.common.AchievementPage;
import zeldaswordskills.block.BlockSacredFlame;
import zeldaswordskills.block.ZSSBlocks;
import zeldaswordskills.item.ItemInstrument.Instrument;
import zeldaswordskills.item.ItemTreasure.Treasures;
import zeldaswordskills.item.ZSSItems;
import zeldaswordskills.lib.Config;
import zeldaswordskills.skills.SkillBase;

public class ZSSAchievements
{
	private static int id;

	public static AchievementPage page;

	public static Achievement
	bombsAway,
	bombJunkie,
	bossBattle,
	bossComplete,
	skillBasic,
	skillGain,
	skillMortal,
	skillMaster,
	skillMasterAll,
	skillHeart,
	skillHeartBar,
	skillHeartsGalore,
	swordBroken,
	comboBasic,
	comboPerfect,
	comboLegend,
	hammerTime,
	movingBlocks,
	hardHitter,
	heavyLifter,
	maskTrader,
	maskSold,
	maskShop,
	swordPendant,
	swordMaster,
	swordTempered,
	swordEvil,
	swordGolden,
	swordFlame,
	swordTrue,
	treasureFirst,
	treasureSecond,
	treasureBiggoron,
	fairyCatcher,
	fairyEmerald,
	fairyBow,
	fairyBowMax,
	fairyEnchantment,
	fairySlingshot,
	fairySupershot,
	fairyBoomerang,
	shieldMirror,
	orcaThief,
	orcaDeknighted,
	orcaRequest,
	orcaFirstLesson,
	orcaCanOpener,
	orcaSecondLesson,
	orcaMaster,
	ocarinaCraft,
	ocarinaSong,
	ocarinaScarecrow,
	ocarinaMaestro;

	public static void preInit() {
		id = Config.getStartingAchievementID();
		int dx = -3, dy = 1;
		// BOMB TREE
		bombsAway = new Achievement(id++, "zss.bombs_away", dx, dy, ZSSItems.bomb, null).registerAchievement();
		bombJunkie = new Achievement(id++, "zss.bomb_junkie", dx - 2, dy - 3, ZSSItems.bomb, bombsAway).setSpecial().registerAchievement();

		// BOMB->BOSS TREE
		bossBattle = new Achievement(id++, "zss.boss_battle", dx, dy - 5, ZSSItems.keyBig, bombsAway).registerAchievement();
		bossComplete = new Achievement(id++, "zss.boss_complete", dx - 1, dy - 7, ZSSItems.keySkeleton, bossBattle).setSpecial().registerAchievement();

		// BOSS->MASTER SWORD TREE
		swordPendant = new Achievement(id++, "zss.sword.pendant", dx - 6, dy - 5, ZSSItems.pendant, bossBattle).registerAchievement();
		swordMaster = new Achievement(id++, "zss.sword.master", dx - 8, dy - 3, ZSSItems.swordMaster, swordPendant).registerAchievement();
		swordTempered = new Achievement(id++, "zss.sword.tempered", dx - 4, dy - 2, ZSSItems.masterOre, swordMaster).registerAchievement();
		swordEvil = new Achievement(id++, "zss.sword.evil", dx - 6, dy - 1, ZSSItems.swordTempered, swordTempered).registerAchievement();
		swordGolden = new Achievement(id++, "zss.sword.golden", dx - 6, dy + 1, ZSSItems.swordGolden, swordEvil).registerAchievement();
		swordFlame = new Achievement(id++, "zss.sword.flame", dx - 6, dy + 3, new ItemStack(ZSSBlocks.sacredFlame, 1, BlockSacredFlame.DIN), swordGolden).registerAchievement();
		swordTrue = new Achievement(id++, "zss.sword.true", dx - 6, dy + 5, ZSSItems.swordMasterTrue, swordFlame).setSpecial().registerAchievement();
		shieldMirror = new Achievement(id++, "zss.shield.mirror", dx - 6, dy + 7, ZSSItems.shieldMirror, swordTrue).setSpecial().registerAchievement();

		// BOMB->FAIRY TREE
		fairyCatcher = new Achievement(id++, "zss.fairy.catcher", dx + 2, dy, ZSSItems.fairyBottle, bombsAway).registerAchievement();
		fairyEmerald = new Achievement(id++, "zss.fairy.emerald", dx + 2, dy - 2, Item.emerald, fairyCatcher).registerAchievement();
		fairyBow = new Achievement(id++, "zss.fairy.bow", dx + 2, dy - 4, ZSSItems.heroBow, fairyEmerald).registerAchievement();
		fairyBowMax = new Achievement(id++, "zss.fairy.bow_max", dx + 2, dy - 6, ZSSItems.arrowLight, fairyBow).setSpecial().registerAchievement();
		fairyEnchantment = new Achievement(id++, "zss.fairy.enchantment", dx + 4, dy - 2, Item.melonSeeds, fairyEmerald).registerAchievement();
		fairySlingshot = new Achievement(id++, "zss.fairy.slingshot", dx + 4, dy - 4, ZSSItems.slingshot, fairyEnchantment).registerAchievement();
		fairySupershot = new Achievement(id++, "zss.fairy.supershot", dx + 4, dy - 6, ZSSItems.supershot, fairySlingshot).setSpecial().registerAchievement();

		// BOMB->HAMMER TREE
		hammerTime = new Achievement(id++, "zss.hammer.wood", dx - 3, dy, ZSSItems.hammer, bombsAway).registerAchievement();
		movingBlocks = new Achievement(id++, "zss.hammer.silver", dx - 3, dy + 2, ZSSItems.gauntletsSilver, hammerTime).registerAchievement();
		hardHitter = new Achievement(id++, "zss.hammer.skull", dx - 3, dy + 4, ZSSBlocks.pegRusty, movingBlocks).registerAchievement();
		heavyLifter = new Achievement(id++, "zss.hammer.golden", dx - 3, dy + 6, ZSSItems.hammerMegaton, hardHitter).setSpecial().registerAchievement();

		// BOMB->ZELDA'S LETTER TREE
		maskTrader = new Achievement(id++, "zss.mask.trader", dx, dy + 3, new ItemStack(ZSSItems.treasure,1,Treasures.ZELDAS_LETTER.ordinal()), bombsAway).registerAchievement();
		maskSold = new Achievement(id++, "zss.mask.sold", dx, dy + 5, ZSSItems.maskKeaton, maskTrader).registerAchievement();
		maskShop = new Achievement(id++, "zss.mask.shop", dx, dy + 7, ZSSItems.maskTruth, maskSold).setSpecial().registerAchievement();

		// SKILL TREE
		dx = 5;
		dy = 1;
		skillBasic = new Achievement(id++, "zss.skill.basic", dx, dy, new ItemStack(ZSSItems.skillOrb,1,SkillBase.swordBasic.getId()), null).registerAchievement();

		// SKILL->COMBO TREE
		comboBasic = new Achievement(id++, "zss.combo.basic", dx + 2, dy, Item.swordWood, skillBasic).registerAchievement();
		comboPerfect = new Achievement(id++, "zss.combo.perfect", dx + 4, dy, Item.swordIron, comboBasic).registerAchievement();
		comboLegend = new Achievement(id++, "zss.combo.legend", dx + 3, dy + 2, Item.swordDiamond, comboPerfect).setSpecial().registerAchievement();

		// SKILL->PROGRESSION TREE
		skillGain = new Achievement(id++, "zss.skill.gain", dx + 1, dy - 3, new ItemStack(ZSSItems.skillOrb, 1, SkillBase.parry.getId()), skillBasic).registerAchievement();
		skillMortal = new Achievement(id++, "zss.skill.mortal", dx + 1, dy - 5, new ItemStack(ZSSItems.skillOrb, 1, SkillBase.mortalDraw.getId()), skillGain).setSpecial().registerAchievement();
		skillMaster = new Achievement(id++, "zss.skill.master", dx + 3, dy - 3, new ItemStack(ZSSItems.skillOrb, 1, SkillBase.dodge.getId()), skillGain).registerAchievement();
		skillMasterAll = new Achievement(id++, "zss.skill.master_all", dx + 5, dy - 3, new ItemStack(ZSSItems.skillOrb, 1, SkillBase.armorBreak.getId()), skillMaster).setSpecial().registerAchievement();

		// SKILL->HEARTS TREE
		skillHeart = new Achievement(id++, "zss.skill.heart", dx, dy + 2, ZSSItems.smallHeart, skillBasic).registerAchievement();
		skillHeartBar = new Achievement(id++, "zss.skill.heartbar", dx, dy + 4, new ItemStack(ZSSItems.skillOrb, 1, SkillBase.bonusHeart.getId()), skillHeart).registerAchievement();
		skillHeartsGalore = new Achievement(id++, "zss.skill.hearts_galore", dx, dy + 6, new ItemStack(ZSSItems.skillOrb,1,SkillBase.bonusHeart.getId()), skillHeartBar).setSpecial().registerAchievement();
		fairyBoomerang = new Achievement(id++, "zss.fairy.boomerang", dx - 2, dy + 4, ZSSItems.boomerangMagic, skillHeartBar).setSpecial().registerAchievement();

		// HEART->BIGGORON TREE
		swordBroken = new Achievement(id++, "zss.sword.broken", dx + 3, dy + 4, new ItemStack(ZSSItems.swordBroken, 1, ZSSItems.swordGiant.itemID), skillHeartBar).registerAchievement();
		treasureFirst = new Achievement(id++, "zss.treasure.first", dx + 3, dy + 6, new ItemStack(ZSSItems.treasure, 1, Treasures.TENTACLE.ordinal()), swordBroken).registerAchievement();
		treasureSecond = new Achievement(id++, "zss.treasure.second", dx + 3, dy + 8, new ItemStack(ZSSItems.treasure, 1, Treasures.POCKET_EGG.ordinal()), treasureFirst).registerAchievement();
		treasureBiggoron = new Achievement(id++, "zss.treasure.biggoron", dx + 1, dy + 8, ZSSItems.swordBiggoron, treasureSecond).setSpecial().registerAchievement();

		// ORCA'S QUEST TREE
		dx = 2;
		dy = 1;
		orcaThief = new Achievement(id++, "zss.orca.thief", dx, dy + 1, ZSSItems.whip, null).registerAchievement();
		orcaDeknighted = new Achievement(id++, "zss.orca.deknighted", dx - 2, dy + 2, new ItemStack(ZSSItems.treasure, 1, Treasures.KNIGHTS_CREST.ordinal()), orcaThief).registerAchievement();
		orcaRequest = new Achievement(id++, "zss.orca.request", dx - 2, dy + 4, new ItemStack(Item.writableBook), orcaDeknighted).registerAchievement();
		orcaFirstLesson = new Achievement(id++, "zss.orca.first", dx - 2, dy + 6, new ItemStack(Item.stick), orcaRequest).registerAchievement();
		orcaCanOpener = new Achievement(id++, "zss.orca.canopener", dx, dy + 6, Item.plateIron, orcaFirstLesson).registerAchievement();
		orcaSecondLesson = new Achievement(id++, "zss.orca.second", dx - 2, dy + 8, new ItemStack(ZSSItems.skillOrb, 1, SkillBase.superSpinAttack.getId()), orcaFirstLesson).registerAchievement();
		orcaMaster = new Achievement(id++, "zss.orca.master", dx - 3, dy + 9, ZSSItems.swordDarknut, orcaSecondLesson).setSpecial().registerAchievement();

		// OCARINA TREE
		dx = 3;
		dy = 0;
		ocarinaCraft = new Achievement(id++, "zss.ocarina.craft", dx, dy, new ItemStack(ZSSItems.instrument, 1, Instrument.OCARINA_FAIRY.ordinal()), null).registerAchievement();
		ocarinaSong = new Achievement(id++, "zss.ocarina.song", dx, dy - 2, Item.writableBook, ocarinaCraft).registerAchievement();
		ocarinaScarecrow = new Achievement(id++, "zss.ocarina.scarecrow", dx - 1, dy - 4, Block.pumpkin, ocarinaSong).registerAchievement().setSpecial();
		ocarinaMaestro = new Achievement(id++, "zss.ocarina.maestro", dx, dy - 6, new ItemStack(ZSSItems.instrument, 1, Instrument.OCARINA_TIME.ordinal()), ocarinaSong).registerAchievement().setSpecial();

		page = new AchievementPage("Zelda",
				bombsAway,
				bombJunkie,
				bossBattle,
				bossComplete,
				skillBasic,
				skillGain,
				skillMortal,
				skillMaster,
				skillMasterAll,
				skillHeart,
				skillHeartBar,
				skillHeartsGalore,
				swordBroken,
				treasureFirst,
				treasureSecond,
				treasureBiggoron,
				comboBasic,
				comboPerfect,
				comboLegend,
				hammerTime,
				movingBlocks,
				hardHitter,
				heavyLifter,
				maskTrader,
				maskSold,
				maskShop,
				swordPendant,
				swordMaster,
				swordTempered,
				swordEvil,
				swordGolden,
				swordFlame,
				swordTrue,
				fairyCatcher,
				fairyEmerald,
				fairyBow,
				fairyBowMax,
				fairyEnchantment,
				fairySlingshot,
				fairySupershot,
				fairyBoomerang,
				shieldMirror,
				orcaThief,
				orcaDeknighted,
				orcaRequest,
				orcaFirstLesson,
				orcaCanOpener,
				orcaSecondLesson,
				orcaMaster,
				ocarinaCraft,
				ocarinaSong,
				ocarinaScarecrow,
				ocarinaMaestro);
		AchievementPage.registerAchievementPage(page);
	}
}
