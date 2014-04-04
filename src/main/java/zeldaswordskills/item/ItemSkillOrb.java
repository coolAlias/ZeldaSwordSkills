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

package zeldaswordskills.item;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.Icon;
import net.minecraft.util.StatCollector;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import net.minecraft.world.World;
import zeldaswordskills.ZSSAchievements;
import zeldaswordskills.api.item.IFairyUpgrade;
import zeldaswordskills.block.tileentity.TileEntityDungeonCore;
import zeldaswordskills.creativetab.ZSSCreativeTabs;
import zeldaswordskills.entity.ZSSPlayerInfo;
import zeldaswordskills.lib.Sounds;
import zeldaswordskills.skills.SkillBase;
import zeldaswordskills.util.MerchantRecipeHelper;
import zeldaswordskills.util.PlayerUtils;
import zeldaswordskills.util.WorldUtils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemSkillOrb extends Item implements IFairyUpgrade
{
	@SideOnly(Side.CLIENT)
	private List<Icon> icons;

	public ItemSkillOrb(int par1) {
		super(par1);
		setMaxDamage(0);
		setHasSubtypes(true);
		setCreativeTab(ZSSCreativeTabs.tabSkills);
	}

	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
		if (!player.worldObj.isRemote) {
			SkillBase skill = SkillBase.getSkill(stack.getItemDamage());
			if (skill != null) {
				ZSSPlayerInfo skills = ZSSPlayerInfo.get(player);
				if (skills.grantSkill(skill)) {
					world.playSoundAtEntity(player, Sounds.LEVELUP, 1.0F, 1.0F);
					player.addChatMessage(StatCollector.translateToLocalFormatted("chat.zss.skill.levelup",
							skill.getDisplayName(), skills.getSkillLevel(skill)));
					if (skill == SkillBase.bonusHeart) {
						player.triggerAchievement(ZSSAchievements.skillHeart);
						if (skills.getSkillLevel(skill) > 19) {
							player.triggerAchievement(ZSSAchievements.skillHeartsGalore);
						} else if (skills.getSkillLevel(skill) > 9) {
							player.triggerAchievement(ZSSAchievements.skillHeartBar);
						}
					} else if (skill == SkillBase.swordBasic && skills.getSkillLevel(skill) == 1) {
						player.triggerAchievement(ZSSAchievements.skillBasic);
					} else if (skills.getSkillLevel(skill) == skill.getMaxLevel()) {
						player.triggerAchievement(ZSSAchievements.skillMaster);
						boolean flag = true;
						for (SkillBase check : SkillBase.getSkills()) {
							if (check.getId() != SkillBase.bonusHeart.getId()) {
								flag = skills.getSkillLevel(check) == check.getMaxLevel();
								if (!flag) { break; }
							}
						}
						if (flag) {
							player.triggerAchievement(ZSSAchievements.skillMasterAll);
						}
					}
					if (!player.capabilities.isCreativeMode) {
						--stack.stackSize;
					}
				} else {
					player.addChatMessage(StatCollector.translateToLocalFormatted("chat.zss.skill.maxlevel", skill.getDisplayName()));
				}
			}
		}

		return stack;
	}

	@Override
	public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity entity) {
		if (entity instanceof EntityVillager && !player.worldObj.isRemote) {
			EntityVillager villager = (EntityVillager) entity;
			MerchantRecipeList trades = villager.getRecipes(player);
			if (villager.getProfession() == 1 && trades != null) {
				MerchantRecipe trade = new MerchantRecipe(stack.copy(), new ItemStack(Item.emerald, 16));
				if (player.worldObj.rand.nextFloat() < 0.2F && MerchantRecipeHelper.addToListWithCheck(trades, trade)) {
					player.addChatMessage(StatCollector.translateToLocal("chat.zss.trade.generic.sell.1"));
				} else {
					player.addChatMessage(StatCollector.translateToLocal("chat.zss.trade.generic.sorry.1"));
				}
			} else {
				player.addChatMessage(StatCollector.translateToLocal("chat.zss.trade.generic.sorry.0"));
			}
		}
		return true;
	}

	@Override
	public String getItemDisplayName(ItemStack stack) {
		SkillBase skill = SkillBase.getSkill(stack.getItemDamage());
		return StatCollector.translateToLocal(super.getUnlocalizedName() + ".name") + " " + (skill != null ? skill.getDisplayName() : "");
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Icon getIconFromDamage(int damage) {
		return icons.get(damage % icons.size());
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(int itemID, CreativeTabs tab, List list) {
		for (SkillBase skill : SkillBase.getSkills()) {
			list.add(new ItemStack(itemID, 1, skill.getId()));
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister register) {
		icons = new ArrayList<Icon>(SkillBase.getNumSkills());
		for (SkillBase skill : SkillBase.getSkills()) {
			icons.add(register.registerIcon(skill.getIconTexture()));
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack,	EntityPlayer player, List list, boolean par4) {
		if (SkillBase.doesSkillExist(stack.getItemDamage())) {
			SkillBase skill = ZSSPlayerInfo.get(player).getPlayerSkill(SkillBase.getSkill(stack.getItemDamage()));
			if (skill != null && skill.getLevel() > 0) {
				if (skill.getId() != SkillBase.bonusHeart.getId()) {
					list.add(EnumChatFormatting.GOLD + skill.getLevelDisplay(true));
				}
				list.addAll(ZSSPlayerInfo.get(player).getPlayerSkill(skill).getDescription(player));
			} else {
				list.add(EnumChatFormatting.ITALIC + StatCollector.translateToLocal("tooltip.zss.skillorb.desc.unknown"));
			}
		}
	}

	@Override
	public void handleFairyUpgrade(EntityItem item, EntityPlayer player, TileEntityDungeonCore core) {
		if (!ZSSPlayerInfo.get(player).hasReceivedAllOrbs()) {
			if (PlayerUtils.hasMasterSword(player)) {
				if (ZSSPlayerInfo.get(player).canReceiveFairyOrb()) {
					if (ZSSPlayerInfo.get(player).receiveFairyOrb()) {
						player.addChatMessage(StatCollector.translateToLocal("chat.zss.fairy.finalskill"));
						player.addChatMessage(StatCollector.translateToLocal("chat.zss.fairy.memento"));
						WorldUtils.spawnItemWithRandom(core.worldObj, new ItemStack(ZSSItems.skillOrb,1,SkillBase.superSpinAttack.getId()), core.xCoord, core.yCoord + 2, core.zCoord);
					} else {
						player.addChatMessage(StatCollector.translateToLocal("chat.zss.fairy.greeting"));
						player.addChatMessage(StatCollector.translateToLocal("chat.zss.fairy.skillorb"));
					}
					core.worldObj.playSoundEffect(core.xCoord + 0.5D, core.yCoord + 1, core.zCoord + 0.5D, Sounds.FAIRY_SKILL, 1.0F, 1.0F);
					WorldUtils.spawnItemWithRandom(core.worldObj, new ItemStack(ZSSItems.skillOrb,1,SkillBase.superSpinAttack.getId()), core.xCoord, core.yCoord + 2, core.zCoord);
					item.setDead();
					player.triggerAchievement(ZSSAchievements.skillSuper);
				} else {
					core.worldObj.playSoundEffect(core.xCoord + 0.5D, core.yCoord + 1, core.zCoord + 0.5D, Sounds.FAIRY_LAUGH, 1.0F, 1.0F);
					player.addChatMessage(StatCollector.translateToLocal("chat.zss.fairy.laugh.unworthy"));
				}
			} else {
				core.worldObj.playSoundEffect(core.xCoord + 0.5D, core.yCoord + 1, core.zCoord + 0.5D, Sounds.FAIRY_LAUGH, 1.0F, 1.0F);
				player.addChatMessage(StatCollector.translateToLocal("chat.zss.fairy.laugh.sword"));
			}
		}
	}

	@Override
	public boolean hasFairyUpgrade(ItemStack stack) {
		return stack.getItemDamage() == SkillBase.spinAttack.getId();
	}
}
