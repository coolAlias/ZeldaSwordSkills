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

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.Achievement;
import net.minecraftforge.common.AchievementPage;
import zeldaswordskills.block.BlockSacredFlame;
import zeldaswordskills.block.ZSSBlocks;
import zeldaswordskills.item.ItemBrokenSword;
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

	// func_180788_c() is basically registerStat() that returns Achievement type
	public static void preInit() {
		int dx = -3, dy = 1;
		// BOMB TREE
		bombsAway = new Achievement("bombs_away", "zss.bombs_away", dx, dy, ZSSItems.bomb, null).func_180788_c();
		bombJunkie = new Achievement("bomb_junkie", "zss.bomb_junkie", dx - 2, dy - 3, ZSSItems.bomb, bombsAway).setSpecial().func_180788_c();

		// BOMB->BOSS TREE
		bossBattle = new Achievement("boss_battle", "zss.boss_battle", dx, dy - 5, ZSSItems.keyBig, bombsAway).func_180788_c();
		bossComplete = new Achievement("boss_complete", "zss.boss_complete", dx - 1, dy - 7, ZSSItems.keySkeleton, bossBattle).setSpecial().func_180788_c();

		// BOSS->MASTER SWORD TREE
		swordPendant = new Achievement("sword.pendant", "zss.sword.pendant", dx - 6, dy - 5, ZSSItems.pendant, bossBattle).func_180788_c();
		swordMaster = new Achievement("sword.master", "zss.sword.master", dx - 8, dy - 3, ZSSItems.swordMaster, swordPendant).func_180788_c();
		swordTempered = new Achievement("sword.tempered", "zss.sword.tempered", dx - 4, dy - 2, ZSSItems.masterOre, swordMaster).func_180788_c();
		swordEvil = new Achievement("sword.evil", "zss.sword.evil", dx - 6, dy - 1, ZSSItems.swordTempered, swordTempered).func_180788_c();
		swordGolden = new Achievement("sword.golden", "zss.sword.golden", dx - 6, dy + 1, ZSSItems.swordGolden, swordEvil).func_180788_c();
		swordFlame = new Achievement("sword.flame", "zss.sword.flame", dx - 6, dy + 3, new ItemStack(ZSSBlocks.sacredFlame, 1, BlockSacredFlame.EnumType.DIN.getMetadata()), swordGolden).func_180788_c();
		swordTrue = new Achievement("sword.true", "zss.sword.true", dx - 6, dy + 5, ZSSItems.swordMasterTrue, swordFlame).setSpecial().func_180788_c();
		shieldMirror = new Achievement("shield.mirror", "zss.shield.mirror", dx - 6, dy + 7, ZSSItems.shieldMirror, swordTrue).setSpecial().func_180788_c();

		// BOMB->FAIRY TREE
		fairyCatcher = new Achievement("fairy.catcher", "zss.fairy.catcher", dx + 2, dy, ZSSItems.fairyBottle, bombsAway).func_180788_c();
		fairyEmerald = new Achievement("fairy.emerald", "zss.fairy.emerald", dx + 2, dy - 2, Items.emerald, fairyCatcher).func_180788_c();
		fairyBow = new Achievement("fairy.bow", "zss.fairy.bow", dx + 2, dy - 4, ZSSItems.heroBow, fairyEmerald).func_180788_c();
		fairyBowMax = new Achievement("fairy.bow_max", "zss.fairy.bow_max", dx + 2, dy - 6, ZSSItems.arrowLight, fairyBow).setSpecial().func_180788_c();
		fairyEnchantment = new Achievement("fairy.enchantment", "zss.fairy.enchantment", dx + 4, dy - 2, Items.melon_seeds, fairyEmerald).func_180788_c();
		fairySlingshot = new Achievement("fairy.slingshot", "zss.fairy.slingshot", dx + 4, dy - 4, ZSSItems.slingshot, fairyEnchantment).func_180788_c();
		fairySupershot = new Achievement("fairy.supershot", "zss.fairy.supershot", dx + 4, dy - 6, ZSSItems.supershot, fairySlingshot).setSpecial().func_180788_c();

		// BOMB->HAMMER TREE
		hammerTime = new Achievement("hammer.wood", "zss.hammer.wood", dx - 3, dy, ZSSItems.hammer, bombsAway).func_180788_c();
		movingBlocks = new Achievement("hammer.silver", "zss.hammer.silver", dx - 3, dy + 2, ZSSItems.gauntletsSilver, hammerTime).func_180788_c();
		hardHitter = new Achievement("hammer.skull", "zss.hammer.skull", dx - 3, dy + 4, Item.getItemFromBlock(ZSSBlocks.pegRusty), movingBlocks).func_180788_c();
		heavyLifter = new Achievement("hammer.golden", "zss.hammer.golden", dx - 3, dy + 6, ZSSItems.hammerMegaton, hardHitter).setSpecial().func_180788_c();

		// BOMB->ZELDA'S LETTER TREE
		maskTrader = new Achievement("mask.trader", "zss.mask.trader", dx, dy + 3, new ItemStack(ZSSItems.treasure, 1, Treasures.ZELDAS_LETTER.ordinal()), bombsAway).func_180788_c();
		maskSold = new Achievement("mask.sold", "zss.mask.sold", dx, dy + 5, ZSSItems.maskKeaton, maskTrader).func_180788_c();
		maskShop = new Achievement("mask.shop", "zss.mask.shop", dx, dy + 7, ZSSItems.maskTruth, maskSold).setSpecial().func_180788_c();

		// SKILL TREE
		dx = 5;
		dy = 1;
		skillBasic = new Achievement("skill.basic", "zss.skill.basic", dx, dy, new ItemStack(ZSSItems.skillOrb, 1, SkillBase.swordBasic.getId()), null).func_180788_c();

		// SKILL->COMBO TREE
		comboBasic = new Achievement("combo.basic", "zss.combo.basic", dx + 2, dy, Items.wooden_sword, skillBasic).func_180788_c();
		comboPerfect = new Achievement("combo.perfect", "zss.combo.perfect", dx + 4, dy, Items.iron_sword, comboBasic).func_180788_c();
		comboLegend = new Achievement("combo.legend", "zss.combo.legend", dx + 3, dy + 2, Items.diamond_sword, comboPerfect).setSpecial().func_180788_c();

		// SKILL->PROGRESSION TREE
		skillGain = new Achievement("skill.gain", "zss.skill.gain", dx + 1, dy - 3, new ItemStack(ZSSItems.skillOrb, 1, SkillBase.parry.getId()), skillBasic).func_180788_c();
		skillMortal = new Achievement("skill.mortal", "zss.skill.mortal", dx + 1, dy - 5, new ItemStack(ZSSItems.skillOrb, 1, SkillBase.mortalDraw.getId()), skillGain).setSpecial().func_180788_c();
		skillMaster = new Achievement("skill.master", "zss.skill.master", dx + 3, dy - 3, new ItemStack(ZSSItems.skillOrb, 1, SkillBase.dodge.getId()), skillGain).func_180788_c();
		skillMasterAll = new Achievement("skill.master_all", "zss.skill.master_all", dx + 5, dy - 3, new ItemStack(ZSSItems.skillOrb, 1, SkillBase.armorBreak.getId()), skillMaster).setSpecial().func_180788_c();

		// SKILL->HEARTS TREE
		skillHeart = new Achievement("skill.heart", "zss.skill.heart", dx, dy + 2, ZSSItems.smallHeart, skillBasic).func_180788_c();
		skillHeartBar = new Achievement("skill.heartbar", "zss.skill.heartbar", dx, dy + 4, new ItemStack(ZSSItems.skillOrb, 1, SkillBase.bonusHeart.getId()), skillHeart).func_180788_c();
		skillHeartsGalore = new Achievement("skill.hearts_galore", "zss.skill.hearts_galore", dx, dy + 6, new ItemStack(ZSSItems.skillOrb, 1, SkillBase.bonusHeart.getId()), skillHeartBar).setSpecial().func_180788_c();
		fairyBoomerang = new Achievement("fairy.boomerang", "zss.fairy.boomerang", dx - 2, dy + 4, ZSSItems.boomerangMagic, skillHeartBar).setSpecial().func_180788_c();

		// HEART->BIGGORON TREE
		swordBroken = new Achievement("sword.broken", "zss.sword.broken", dx + 3, dy + 4, ItemBrokenSword.getBrokenSwordFor(ZSSItems.swordGiant), skillHeartBar).func_180788_c();
		treasureFirst = new Achievement("treasure.first", "zss.treasure.first", dx + 3, dy + 6, new ItemStack(ZSSItems.treasure, 1, Treasures.TENTACLE.ordinal()), swordBroken).func_180788_c();
		treasureSecond = new Achievement("treasure.second", "zss.treasure.second", dx + 3, dy + 8, new ItemStack(ZSSItems.treasure, 1, Treasures.POCKET_EGG.ordinal()), treasureFirst).func_180788_c();
		treasureBiggoron = new Achievement("treasure.biggoron", "zss.treasure.biggoron", dx + 1, dy + 8, ZSSItems.swordBiggoron, treasureSecond).setSpecial().func_180788_c();

		// ORCA'S QUEST TREE
		dx = 2;
		dy = 1;
		orcaThief = new Achievement("orca.thief", "zss.orca.thief", dx, dy + 1, ZSSItems.whip, null).func_180788_c();
		orcaDeknighted = new Achievement("orca.deknighted", "zss.orca.deknighted", dx - 2, dy + 2, new ItemStack(ZSSItems.treasure, 1, Treasures.KNIGHTS_CREST.ordinal()), orcaThief).func_180788_c();
		orcaRequest = new Achievement("orca.request", "zss.orca.request", dx - 2, dy + 4, new ItemStack(Items.writable_book), orcaDeknighted).func_180788_c();
		orcaFirstLesson = new Achievement("orca.first", "zss.orca.first", dx - 2, dy + 6, new ItemStack(Items.stick), orcaRequest).func_180788_c();
		orcaCanOpener = new Achievement("orca.canopener", "zss.orca.canopener", dx, dy + 6, Items.iron_chestplate, orcaFirstLesson).func_180788_c();
		orcaSecondLesson = new Achievement("orca.second", "zss.orca.second", dx - 2, dy + 8, new ItemStack(ZSSItems.skillOrb, 1, SkillBase.superSpinAttack.getId()), orcaFirstLesson).func_180788_c();
		orcaMaster = new Achievement("orca.master", "zss.orca.master", dx - 3, dy + 9, ZSSItems.swordDarknut, orcaSecondLesson).setSpecial().func_180788_c();

		// OCARINA TREE
		dx = 3;
		dy = 0;
		ocarinaCraft = new Achievement("ocarina.craft", "zss.ocarina.craft", dx, dy, new ItemStack(ZSSItems.instrument, 1, Instrument.OCARINA_FAIRY.ordinal()), null).func_180788_c();
		ocarinaSong = new Achievement("ocarina.song", "zss.ocarina.song", dx, dy - 2, Items.writable_book, ocarinaCraft).func_180788_c();
		ocarinaScarecrow = new Achievement("ocarina.scarecrow", "zss.ocarina.scarecrow", dx - 1, dy - 4, Item.getItemFromBlock(Blocks.pumpkin), ocarinaSong).func_180788_c().setSpecial();
		ocarinaMaestro = new Achievement("ocarina.maestro", "zss.ocarina.maestro", dx, dy - 6, new ItemStack(ZSSItems.instrument, 1, Instrument.OCARINA_TIME.ordinal()), ocarinaSong).func_180788_c().setSpecial();

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
