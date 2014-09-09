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

import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.Achievement;
import net.minecraftforge.common.AchievementPage;
import zeldaswordskills.block.BlockSacredFlame;
import zeldaswordskills.block.ZSSBlocks;
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
	skillSuper,
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
	shieldMirror;

	public static void init() {
		bombsAway = new Achievement("zss.bombs_away", "bombs_away", 0, 1, ZSSItems.bomb, null).registerStat();
		bombJunkie = new Achievement("zss.bomb_junkie", "bomb_junkie", -2, -2, ZSSItems.bomb, bombsAway).setSpecial().registerStat();
		bossBattle = new Achievement("zss.boss_battle", "boss_battle", 0, -4, ZSSItems.keyBig, bombsAway).registerStat();
		bossComplete = new Achievement("zss.boss_complete", "boss_complete", -1, -6, ZSSItems.keySkeleton, bossBattle).setSpecial().registerStat();
		skillBasic = new Achievement("zss.skill.basic", "skill.basic", 5, 1, new ItemStack(ZSSItems.skillOrb,1,SkillBase.swordBasic.getId()), null).registerStat();
		skillGain = new Achievement("zss.skill.gain", "skill.gain", 6, -2, new ItemStack(ZSSItems.skillOrb,1,SkillBase.parry.getId()), skillBasic).registerStat();
		skillMortal = new Achievement("zss.skill.mortal", "skill.mortal", 6, -4, new ItemStack(ZSSItems.skillOrb,1,SkillBase.mortalDraw.getId()), skillGain).setSpecial().registerStat();
		skillMaster = new Achievement("zss.skill.master", "skill.master", 8, -2, new ItemStack(ZSSItems.skillOrb,1,SkillBase.dodge.getId()), skillGain).registerStat();
		skillMasterAll = new Achievement("zss.skill.master_all", "skill.master_all", 10, -2, new ItemStack(ZSSItems.skillOrb,1,SkillBase.armorBreak.getId()), skillMaster).setSpecial().registerStat();
		skillHeart = new Achievement("zss.skill.heart", "skill.heart", 5, 3, ZSSItems.smallHeart, skillBasic).registerStat();
		skillHeartBar = new Achievement("zss.skill.heartbar", "skill.heartbar", 5, 5, new ItemStack(ZSSItems.skillOrb,1,SkillBase.bonusHeart.getId()), skillHeart).registerStat();
		skillHeartsGalore = new Achievement("zss.skill.hearts_galore", "skill.hearts_galore", 5, 7, new ItemStack(ZSSItems.skillOrb,1,SkillBase.bonusHeart.getId()), skillHeartBar).setSpecial().registerStat();
		swordBroken = new Achievement("zss.sword.broken", "sword.broken", 8, 5, new ItemStack(ZSSItems.swordBroken,1,Item.getIdFromItem(ZSSItems.swordGiant)), skillHeartBar).registerStat();
		treasureFirst = new Achievement("zss.treasure.first", "treasure.first", 8, 7, new ItemStack(ZSSItems.treasure,1,Treasures.TENTACLE.ordinal()), swordBroken).registerStat();
		treasureSecond = new Achievement("zss.treasure.second", "treasure.second", 8, 9, new ItemStack(ZSSItems.treasure,1,Treasures.POCKET_EGG.ordinal()), treasureFirst).registerStat();
		treasureBiggoron = new Achievement("zss.treasure.biggoron", "treasure.biggoron", 6, 9, ZSSItems.swordBiggoron, treasureSecond).setSpecial().registerStat();
		comboBasic = new Achievement("zss.combo.basic", "combo.basic", 7, 1, Items.wooden_sword, skillBasic).registerStat();
		comboPerfect = new Achievement("zss.combo.perfect", "combo.perfect", 9, 1, Items.iron_sword, comboBasic).registerStat();
		comboLegend = new Achievement("zss.combo.legend", "combo.legend", 8, 3, Items.diamond_sword, comboPerfect).setSpecial().registerStat();
		hammerTime = new Achievement("zss.hammer.wood", "hammer.wood", -3, 1, ZSSItems.hammer, bombsAway).registerStat();
		movingBlocks = new Achievement("zss.hammer.silver", "hammer.silver", -3, 3, ZSSItems.gauntletsSilver, hammerTime).registerStat();
		hardHitter = new Achievement("zss.hammer.skull", "hammer.skull", -3, 5, new ItemStack(ZSSBlocks.pegRusty), movingBlocks).registerStat();
		heavyLifter = new Achievement("zss.hammer.golden", "hammer.golden", -3, 7, ZSSItems.hammerMegaton, hardHitter).setSpecial().registerStat();
		maskTrader = new Achievement("zss.mask.trader", "mask.trader", 0, 4, new ItemStack(ZSSItems.treasure,1,Treasures.ZELDAS_LETTER.ordinal()), bombsAway).registerStat();
		maskSold = new Achievement("zss.mask.sold", "mask.sold", 0, 6, ZSSItems.maskKeaton, maskTrader).registerStat();
		maskShop = new Achievement("zss.mask.shop", "mask.shop", 0, 8, ZSSItems.maskTruth, maskSold).setSpecial().registerStat();
		swordPendant = new Achievement("zss.sword.pendant", "sword.pendant", -6, -4, ZSSItems.pendant, bossBattle).registerStat();
		swordMaster = new Achievement("zss.sword.master", "sword.master", -8, -2, ZSSItems.swordMaster, swordPendant).registerStat();
		skillSuper = new Achievement("zss.skill.super", "skill.super", -8, -5, new ItemStack(ZSSItems.skillOrb,1,SkillBase.superSpinAttack.getId()), swordMaster).setSpecial().registerStat();
		swordTempered = new Achievement("zss.sword.tempered", "sword.tempered", -4, -1, ZSSItems.masterOre, swordMaster).registerStat();
		swordEvil = new Achievement("zss.sword.evil", "sword.evil", -6, 0, ZSSItems.swordTempered, swordTempered).registerStat();
		swordGolden = new Achievement("zss.sword.golden", "sword.golden", -6, 2, ZSSItems.swordGolden, swordEvil).registerStat();
		swordFlame = new Achievement("zss.sword.flame", "sword.flame", -6, 4, new ItemStack(ZSSBlocks.sacredFlame,1,BlockSacredFlame.DIN), swordGolden).registerStat();
		swordTrue = new Achievement("zss.sword.true", "sword.true", -6, 6, ZSSItems.swordMasterTrue, swordFlame).setSpecial().registerStat();
		fairyCatcher = new Achievement("zss.fairy.catcher", "fairy.catcher", 2, 1, ZSSItems.fairyBottle, bombsAway).registerStat();
		fairyEmerald = new Achievement("zss.fairy.emerald", "fairy.emerald", 2, -1, Items.emerald, fairyCatcher).registerStat();
		fairyBow = new Achievement("zss.fairy.bow", "fairy.bow", 2, -3, ZSSItems.heroBow, fairyEmerald).registerStat();
		fairyBowMax = new Achievement("zss.fairy.bow_max", "fairy.bow_max", 2, -5, ZSSItems.arrowLight, fairyBow).setSpecial().registerStat();
		fairyEnchantment = new Achievement("zss.fairy.enchantment", "fairy.enchantment", 4, -1, Items.melon_seeds, fairyEmerald).registerStat();
		fairySlingshot = new Achievement("zss.fairy.slingshot", "fairy.slingshot", 4, -3, ZSSItems.slingshot, fairyEnchantment).registerStat();
		fairySupershot = new Achievement("zss.fairy.supershot", "fairy.supershot", 4, -5, ZSSItems.supershot, fairySlingshot).setSpecial().registerStat();
		fairyBoomerang = new Achievement("zss.fairy.boomerang", "fairy.boomerang", 3, 5, ZSSItems.boomerangMagic, skillHeartBar).setSpecial().registerStat();
		shieldMirror = new Achievement("zss.shield.mirror", "shield.mirror", -6, 8, ZSSItems.shieldMirror, swordTrue).setSpecial().registerStat();

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
				skillSuper,
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
				shieldMirror);
		AchievementPage.registerAchievementPage(page);
	}
}
