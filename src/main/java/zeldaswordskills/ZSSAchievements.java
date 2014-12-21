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

package zeldaswordskills;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.Achievement;
import net.minecraftforge.common.AchievementPage;
import zeldaswordskills.block.BlockSacredFlame;
import zeldaswordskills.block.ZSSBlocks;
import zeldaswordskills.item.ItemInstrument.Instrument;
import zeldaswordskills.item.ItemTreasure.Treasures;
import zeldaswordskills.item.ZSSItems;
import zeldaswordskills.skills.SkillBase;

public class ZSSAchievements
{
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
		int dx = -3, dy = 1;
		// BOMB TREE
		bombsAway = new Achievement("zss.bombs_away", "bombs_away", dx, dy, ZSSItems.bomb, null).registerStat();
		bombJunkie = new Achievement("zss.bomb_junkie", "bomb_junkie", dx - 2, dy - 3, ZSSItems.bomb, bombsAway).setSpecial().registerStat();

		// BOMB->BOSS TREE
		bossBattle = new Achievement("zss.boss_battle", "boss_battle", dx, dy - 5, ZSSItems.keyBig, bombsAway).registerStat();
		bossComplete = new Achievement("zss.boss_complete", "boss_complete", dx - 1, dy - 7, ZSSItems.keySkeleton, bossBattle).setSpecial().registerStat();

		// BOSS->MASTER SWORD TREE
		swordPendant = new Achievement("zss.sword.pendant", "sword.pendant", dx - 6, dy - 5, ZSSItems.pendant, bossBattle).registerStat();
		swordMaster = new Achievement("zss.sword.master", "sword.master", dx - 8, dy - 3, ZSSItems.swordMaster, swordPendant).registerStat();
		swordTempered = new Achievement("zss.sword.tempered", "sword.tempered", dx - 4, dy - 2, ZSSItems.masterOre, swordMaster).registerStat();
		swordEvil = new Achievement("zss.sword.evil", "sword.evil", dx - 6, dy - 1, ZSSItems.swordTempered, swordTempered).registerStat();
		swordGolden = new Achievement("zss.sword.golden", "sword.golden", dx - 6, dy + 1, ZSSItems.swordGolden, swordEvil).registerStat();
		swordFlame = new Achievement("zss.sword.flame", "sword.flame", dx - 6, dy + 3, new ItemStack(ZSSBlocks.sacredFlame,1,BlockSacredFlame.DIN), swordGolden).registerStat();
		swordTrue = new Achievement("zss.sword.true", "sword.true", dx - 6, dy + 5, ZSSItems.swordMasterTrue, swordFlame).setSpecial().registerStat();
		shieldMirror = new Achievement("zss.shield.mirror", "shield.mirror", dx - 6, dy + 7, ZSSItems.shieldMirror, swordTrue).setSpecial().registerStat();

		// BOMB->FAIRY TREE
		fairyCatcher = new Achievement("zss.fairy.catcher", "fairy.catcher", dx + 2, dy, ZSSItems.fairyBottle, bombsAway).registerStat();
		fairyEmerald = new Achievement("zss.fairy.emerald", "fairy.emerald", dx + 2, dy - 2, Items.emerald, fairyCatcher).registerStat();
		fairyBow = new Achievement("zss.fairy.bow", "fairy.bow", dx + 2, dy - 4, ZSSItems.heroBow, fairyEmerald).registerStat();
		fairyBowMax = new Achievement("zss.fairy.bow_max", "fairy.bow_max", dx + 2, dy - 6, ZSSItems.arrowLight, fairyBow).setSpecial().registerStat();
		fairyEnchantment = new Achievement("zss.fairy.enchantment", "fairy.enchantment", dx + 4, dy - 2, Items.melon_seeds, fairyEmerald).registerStat();
		fairySlingshot = new Achievement("zss.fairy.slingshot", "fairy.slingshot", dx + 4, dy - 4, ZSSItems.slingshot, fairyEnchantment).registerStat();
		fairySupershot = new Achievement("zss.fairy.supershot", "fairy.supershot", dx + 4, dy - 6, ZSSItems.supershot, fairySlingshot).setSpecial().registerStat();

		// BOMB->HAMMER TREE
		hammerTime = new Achievement("zss.hammer.wood", "hammer.wood", dx - 3, dy, ZSSItems.hammer, bombsAway).registerStat();
		movingBlocks = new Achievement("zss.hammer.silver", "hammer.silver", dx - 3, dy + 2, ZSSItems.gauntletsSilver, hammerTime).registerStat();
		hardHitter = new Achievement("zss.hammer.skull", "hammer.skull", dx - 3, dy + 4, Item.getItemFromBlock(ZSSBlocks.pegRusty), movingBlocks).registerStat();
		heavyLifter = new Achievement("zss.hammer.golden", "hammer.golden", dx - 3, dy + 6, ZSSItems.hammerMegaton, hardHitter).setSpecial().registerStat();

		// BOMB->ZELDA'S LETTER TREE
		maskTrader = new Achievement("zss.mask.trader", "mask.trader", dx, dy + 3, new ItemStack(ZSSItems.treasure,1,Treasures.ZELDAS_LETTER.ordinal()), bombsAway).registerStat();
		maskSold = new Achievement("zss.mask.sold", "mask.sold", dx, dy + 5, ZSSItems.maskKeaton, maskTrader).registerStat();
		maskShop = new Achievement("zss.mask.shop", "mask.shop", dx, dy + 7, ZSSItems.maskTruth, maskSold).setSpecial().registerStat();

		// SKILL TREE
		dx = 5;
		dy = 1;
		skillBasic = new Achievement("zss.skill.basic", "skill.basic", dx, dy, new ItemStack(ZSSItems.skillOrb,1,SkillBase.swordBasic.getId()), null).registerStat();

		// SKILL->COMBO TREE
		comboBasic = new Achievement("zss.combo.basic", "combo.basic", dx + 2, dy, Items.wooden_sword, skillBasic).registerStat();
		comboPerfect = new Achievement("zss.combo.perfect", "combo.perfect", dx + 4, dy, Items.iron_sword, comboBasic).registerStat();
		comboLegend = new Achievement("zss.combo.legend", "combo.legend", dx + 3, dy + 2, Items.diamond_sword, comboPerfect).setSpecial().registerStat();

		// SKILL->PROGRESSION TREE
		skillGain = new Achievement("zss.skill.gain", "skill.gain", dx + 1, dy - 3, new ItemStack(ZSSItems.skillOrb,1,SkillBase.parry.getId()), skillBasic).registerStat();
		skillMortal = new Achievement("zss.skill.mortal", "skill.mortal", dx + 1, dy - 5, new ItemStack(ZSSItems.skillOrb,1,SkillBase.mortalDraw.getId()), skillGain).setSpecial().registerStat();
		skillMaster = new Achievement("zss.skill.master", "skill.master", dx + 3, dy - 3, new ItemStack(ZSSItems.skillOrb,1,SkillBase.dodge.getId()), skillGain).registerStat();
		skillMasterAll = new Achievement("zss.skill.master_all", "skill.master_all", dx + 5, dy - 3, new ItemStack(ZSSItems.skillOrb,1,SkillBase.armorBreak.getId()), skillMaster).setSpecial().registerStat();

		// SKILL->HEARTS TREE
		skillHeart = new Achievement("zss.skill.heart", "skill.heart", dx, dy + 2, ZSSItems.smallHeart, skillBasic).registerStat();
		skillHeartBar = new Achievement("zss.skill.heartbar", "skill.heartbar", dx, dy + 4, new ItemStack(ZSSItems.skillOrb,1,SkillBase.bonusHeart.getId()), skillHeart).registerStat();
		skillHeartsGalore = new Achievement("zss.skill.hearts_galore", "skill.hearts_galore", dx, dy + 6, new ItemStack(ZSSItems.skillOrb,1,SkillBase.bonusHeart.getId()), skillHeartBar).setSpecial().registerStat();
		fairyBoomerang = new Achievement("zss.fairy.boomerang", "fairy.boomerang", dx - 2, dy + 4, ZSSItems.boomerangMagic, skillHeartBar).setSpecial().registerStat();

		// HEART->BIGGORON TREE
		swordBroken = new Achievement("zss.sword.broken", "sword.broken", dx + 3, dy + 4, new ItemStack(ZSSItems.swordBroken,1,Item.getIdFromItem(ZSSItems.swordGiant)), skillHeartBar).registerStat();
		treasureFirst = new Achievement("zss.treasure.first", "treasure.first", dx + 3, dy + 6, new ItemStack(ZSSItems.treasure,1,Treasures.TENTACLE.ordinal()), swordBroken).registerStat();
		treasureSecond = new Achievement("zss.treasure.second", "treasure.second", dx + 3, dy + 8, new ItemStack(ZSSItems.treasure,1,Treasures.POCKET_EGG.ordinal()), treasureFirst).registerStat();
		treasureBiggoron = new Achievement("zss.treasure.biggoron", "treasure.biggoron", dx + 1, dy + 8, ZSSItems.swordBiggoron, treasureSecond).setSpecial().registerStat();

		// ORCA'S QUEST TREE
		dx = 2;
		dy = 1;
		orcaThief = new Achievement("zss.orca.thief", "orca.thief", dx, dy + 1, ZSSItems.whip, null).registerStat();
		orcaDeknighted = new Achievement("zss.orca.deknighted", "orca.deknighted", dx - 2, dy + 2, new ItemStack(ZSSItems.treasure, 1, Treasures.KNIGHTS_CREST.ordinal()), orcaThief).registerStat();
		orcaRequest = new Achievement("zss.orca.request", "orca.request", dx - 2, dy + 4, new ItemStack(Items.writable_book), orcaDeknighted).registerStat();
		orcaFirstLesson = new Achievement("zss.orca.first", "orca.first", dx - 2, dy + 6, new ItemStack(Items.stick), orcaRequest).registerStat();
		orcaCanOpener = new Achievement("zss.orca.canopener", "orca.canopener", dx, dy + 6, Items.iron_chestplate, orcaFirstLesson).registerStat();
		orcaSecondLesson = new Achievement("zss.orca.second", "orca.second", dx - 2, dy + 8, new ItemStack(ZSSItems.skillOrb,1,SkillBase.superSpinAttack.getId()), orcaFirstLesson).registerStat();
		orcaMaster = new Achievement("zss.orca.master", "orca.master", dx - 3, dy + 9, ZSSItems.swordDarknut, orcaSecondLesson).setSpecial().registerStat();
		
		// OCARINA TREE
		dx = 3;
		dy = 0;
		ocarinaCraft = new Achievement("zss.ocarina.craft", "ocarina.craft", dx, dy, new ItemStack(ZSSItems.instrument,1,Instrument.OCARINA_FAIRY.ordinal()), null);
		ocarinaSong = new Achievement("zss.ocarina.song", "ocarina.song", dx, dy - 2, Items.writable_book, ocarinaCraft);
		ocarinaScarecrow = new Achievement("zss.ocarina.scarecrow", "ocarina.scarecrow", dx - 1, dy - 4, Item.getItemFromBlock(Blocks.pumpkin), ocarinaSong).setSpecial();
		ocarinaMaestro = new Achievement("zss.ocarina.maestro", "ocarina.maestro", dx, dy - 6, new ItemStack(ZSSItems.instrument,1,Instrument.OCARINA_TIME.ordinal()), ocarinaSong).setSpecial();

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
