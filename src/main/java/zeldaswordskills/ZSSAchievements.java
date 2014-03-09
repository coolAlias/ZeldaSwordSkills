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

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.Achievement;
import net.minecraftforge.common.AchievementPage;
import zeldaswordskills.block.BlockSacredFlame;
import zeldaswordskills.block.ZSSBlocks;
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
	fairyBoomerang;

	public static void init() {
		id = Config.getStartingAchievementID();
		bombsAway = new Achievement(id++, "bombs_away", 0, 1, ZSSItems.bomb, null).registerAchievement();
		bombJunkie = new Achievement(id++, "bomb_junkie", -2, -2, ZSSItems.bomb, bombsAway).setSpecial().registerAchievement();
		bossBattle = new Achievement(id++, "boss_battle", 0, -4, ZSSItems.keyBig, bombsAway).registerAchievement();
		bossComplete = new Achievement(id++, "boss_complete", -1, -6, ZSSItems.keySkeleton, bossBattle).setSpecial().registerAchievement();
		skillBasic = new Achievement(id++, "skill.basic", 5, 1, new ItemStack(ZSSItems.skillOrb,1,SkillBase.swordBasic.id), null).registerAchievement();
		skillGain = new Achievement(id++, "skill.gain", 6, -2, new ItemStack(ZSSItems.skillOrb,1,SkillBase.parry.id), skillBasic).registerAchievement();
		skillMortal = new Achievement(id++, "skill.mortal", 7, -1, new ItemStack(ZSSItems.skillOrb,1,SkillBase.mortalDraw.id), skillGain).setSpecial().registerAchievement();
		skillMaster = new Achievement(id++, "skill.master", 8, -2, new ItemStack(ZSSItems.skillOrb,1,SkillBase.dodge.id), skillGain).registerAchievement();
		skillMasterAll = new Achievement(id++, "skill.master_all", 10, -2, new ItemStack(ZSSItems.skillOrb,1,SkillBase.armorBreak.id), skillMaster).setSpecial().registerAchievement();
		skillSuper = new Achievement(id++, "skill.super", 6, -4, new ItemStack(ZSSItems.skillOrb,1,SkillBase.superSpinAttack.id), skillGain).setSpecial().registerAchievement();
		skillHeart = new Achievement(id++, "skill.heart", 5, 3, ZSSItems.smallHeart, skillBasic).registerAchievement();
		skillHeartBar = new Achievement(id++, "skill.heartbar", 5, 5, new ItemStack(ZSSItems.skillOrb,1,SkillBase.bonusHeart.id), skillHeart).registerAchievement();
		skillHeartsGalore = new Achievement(id++, "skill.hearts_galore", 5, 7, new ItemStack(ZSSItems.skillOrb,1,SkillBase.bonusHeart.id), skillHeartBar).setSpecial().registerAchievement();
		swordBroken = new Achievement(id++, "sword.broken", 8, 5, new ItemStack(ZSSItems.swordBroken,1,ZSSItems.swordGiant.itemID), skillHeartBar).registerAchievement();
		treasureFirst = new Achievement(id++, "treasure.first", 8, 7, new ItemStack(ZSSItems.treasure,1,Treasures.TENTACLE.ordinal()), swordBroken).registerAchievement();
		treasureSecond = new Achievement(id++, "treasure.second", 8, 9, new ItemStack(ZSSItems.treasure,1,Treasures.POCKET_EGG.ordinal()), treasureFirst).registerAchievement();
		treasureBiggoron = new Achievement(id++, "treasure.biggoron", 6, 9, ZSSItems.swordBiggoron, treasureSecond).setSpecial().registerAchievement();
		comboBasic = new Achievement(id++, "combo.basic", 7, 1, Item.swordWood, skillBasic).registerAchievement();
		comboPerfect = new Achievement(id++, "combo.perfect", 9, 1, Item.swordIron, comboBasic).registerAchievement();
		comboLegend = new Achievement(id++, "combo.legend", 8, 3, Item.swordDiamond, comboPerfect).setSpecial().registerAchievement();
		hammerTime = new Achievement(id++, "hammer.wood", -3, 1, ZSSItems.hammer, bombsAway).registerAchievement();
		movingBlocks = new Achievement(id++, "hammer.silver", -3, 3, ZSSItems.gauntletsSilver, hammerTime).registerAchievement();
		hardHitter = new Achievement(id++, "hammer.skull", -3, 5, new ItemStack(ZSSBlocks.pegRusty), movingBlocks).registerAchievement();
		heavyLifter = new Achievement(id++, "hammer.golden", -3, 7, ZSSItems.hammerMegaton, hardHitter).setSpecial().registerAchievement();
		maskTrader = new Achievement(id++, "mask.trader", 0, 4, new ItemStack(ZSSItems.treasure,1,Treasures.ZELDAS_LETTER.ordinal()), bombsAway).registerAchievement();
		maskSold = new Achievement(id++, "mask.sold", 0, 6, ZSSItems.maskKeaton, maskTrader).registerAchievement();
		maskShop = new Achievement(id++, "mask.shop", 0, 8, ZSSItems.maskTruth, maskSold).setSpecial().registerAchievement();
		swordPendant = new Achievement(id++, "sword.pendant", -6, -4, ZSSItems.pendant, bossBattle).registerAchievement();
		swordMaster = new Achievement(id++, "sword.master", -8, -2, ZSSItems.swordMaster, swordPendant).registerAchievement();
		swordTempered = new Achievement(id++, "sword.tempered", -4, -1, ZSSItems.masterOre, swordMaster).registerAchievement();
		swordEvil = new Achievement(id++, "sword.evil", -6, 0, ZSSItems.swordTempered, swordTempered).registerAchievement();
		swordGolden = new Achievement(id++, "sword.golden", -6, 2, ZSSItems.swordGolden, swordEvil).registerAchievement();
		swordFlame = new Achievement(id++, "sword.flame", -6, 4, new ItemStack(ZSSBlocks.sacredFlame,1,BlockSacredFlame.DIN), swordGolden).registerAchievement();
		swordTrue = new Achievement(id++, "sword.true", -6, 6, ZSSItems.swordMasterTrue, swordFlame).setSpecial().registerAchievement();
		fairyCatcher = new Achievement(id++, "fairy.catcher", 2, 1, ZSSItems.fairyBottle, bombsAway).registerAchievement();
		fairyEmerald = new Achievement(id++, "fairy.emerald", 2, -1, Item.emerald, fairyCatcher).registerAchievement();
		fairyBow = new Achievement(id++, "fairy.bow", 2, -3, ZSSItems.heroBow, fairyEmerald).registerAchievement();
		fairyBowMax = new Achievement(id++, "fairy.bow_max", 2, -5, ZSSItems.arrowLight, fairyBow).setSpecial().registerAchievement();
		fairyEnchantment = new Achievement(id++, "fairy.enchantment", 4, -1, Item.melonSeeds, fairyEmerald).registerAchievement();
		fairySlingshot = new Achievement(id++, "fairy.slingshot", 4, -3, ZSSItems.slingshot, fairyEnchantment).registerAchievement();
		fairySupershot = new Achievement(id++, "fairy.supershot", 4, -5, ZSSItems.supershot, fairySlingshot).setSpecial().registerAchievement();
		fairyBoomerang = new Achievement(id++, "fairy.boomerang", 3, 5, ZSSItems.boomerangMagic, skillHeartBar).setSpecial().registerAchievement();

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
				fairyBoomerang);
		AchievementPage.registerAchievementPage(page);
	}
}
