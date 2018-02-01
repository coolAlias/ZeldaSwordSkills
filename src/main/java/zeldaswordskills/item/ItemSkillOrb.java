/**
    Copyright (C) <2018> <coolAlias>

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

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import net.minecraft.world.World;
import zeldaswordskills.ZSSAchievements;
import zeldaswordskills.api.item.IUnenchantable;
import zeldaswordskills.creativetab.ZSSCreativeTabs;
import zeldaswordskills.entity.player.ZSSPlayerSkills;
import zeldaswordskills.handler.TradeHandler.EnumVillager;
import zeldaswordskills.ref.Sounds;
import zeldaswordskills.skills.SkillBase;
import zeldaswordskills.util.MerchantRecipeHelper;
import zeldaswordskills.util.PlayerUtils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemSkillOrb extends Item implements IUnenchantable 
{
	@SideOnly(Side.CLIENT)
	private List<IIcon> icons;

	public ItemSkillOrb() {
		super();
		setMaxDamage(0);
		setHasSubtypes(true);
		setCreativeTab(ZSSCreativeTabs.tabSkills);
	}

	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
		if (!player.worldObj.isRemote) {
			SkillBase skill = SkillBase.getSkill(stack.getItemDamage());
			if (skill != null) {
				ZSSPlayerSkills skills = ZSSPlayerSkills.get(player);
				if (skills.grantSkill(skill)) {
					PlayerUtils.playRandomizedSound(player, Sounds.LEVELUP, 1.0F, 1.0F);
					PlayerUtils.sendTranslatedChat(player, "chat.zss.skill.levelup",
							new ChatComponentTranslation(skill.getTranslationString()),
							skills.getSkillLevel(skill));
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
					PlayerUtils.sendTranslatedChat(player, "chat.zss.skill.maxlevel", new ChatComponentTranslation(skill.getTranslationString()));
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
			if (EnumVillager.LIBRARIAN.is(villager) && trades != null) {
				MerchantRecipe trade = new MerchantRecipe(stack.copy(), new ItemStack(Items.emerald, 16));
				if (player.worldObj.rand.nextFloat() < 0.2F && MerchantRecipeHelper.addToListWithCheck(trades, trade)) {
					PlayerUtils.sendTranslatedChat(player, "chat.zss.trade.generic.sell.1");
				} else {
					PlayerUtils.sendTranslatedChat(player, "chat.zss.trade.generic.sorry.1");
				}
			} else {
				PlayerUtils.sendTranslatedChat(player, "chat.zss.trade.generic.sorry.0");
			}
		}
		return true;
	}

	@Override
	public String getItemStackDisplayName(ItemStack stack) {
		SkillBase skill = SkillBase.getSkill(stack.getItemDamage());
		if (skill == SkillBase.bonusHeart) {
			return skill.getDisplayName(); // special case: "Heart Container" instead of "Skill Orb of..."
		}
		return StatCollector.translateToLocalFormatted(getUnlocalizedName() + ".name", (skill == null ? "" : skill.getDisplayName()));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIconFromDamage(int damage) {
		return icons.get(damage % icons.size());
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(Item item, CreativeTabs tab, List list) {
		for (SkillBase skill : SkillBase.getSkills()) {
			list.add(new ItemStack(item, 1, skill.getId()));
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister register) {
		icons = new ArrayList<IIcon>(SkillBase.getNumSkills());
		for (SkillBase skill : SkillBase.getSkills()) {
			icons.add(register.registerIcon(skill.getIconTexture()));
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack,	EntityPlayer player, List list, boolean par4) {
		if (SkillBase.doesSkillExist(stack.getItemDamage())) {
			SkillBase skill = ZSSPlayerSkills.get(player).getPlayerSkill(SkillBase.getSkill(stack.getItemDamage()));
			if (skill != null && skill.getLevel() > 0) {
				if (skill.getId() != SkillBase.bonusHeart.getId()) {
					list.add(EnumChatFormatting.GOLD + skill.getLevelDisplay(true));
				}
				list.addAll(skill.getTranslatedTooltip(player));
			} else {
				list.add(StatCollector.translateToLocal("tooltip.zss.skillorb.desc.0"));
			}
		}
	}
}
