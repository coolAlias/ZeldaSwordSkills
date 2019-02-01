/**
 * Copyright (C) <2019> <coolAlias>
 *
 * This file is part of coolAlias' Zelda Sword Skills Minecraft Mod; as such,
 * you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package zeldaswordskills.item;

import java.util.List;

import com.google.common.collect.Multimap;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.util.StatCollector;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import zeldaswordskills.ZSSAchievements;
import zeldaswordskills.ZSSMain;
import zeldaswordskills.api.item.IUnenchantable;
import zeldaswordskills.api.item.IWeapon;
import zeldaswordskills.api.item.WeaponRegistry;
import zeldaswordskills.creativetab.ZSSCreativeTabs;
import zeldaswordskills.entity.ZSSVillagerInfo.EnumVillager;
import zeldaswordskills.entity.npc.EntityGoron;
import zeldaswordskills.entity.player.ZSSPlayerSkills;
import zeldaswordskills.ref.ModInfo;
import zeldaswordskills.skills.SkillBase;
import zeldaswordskills.util.MerchantRecipeHelper;
import zeldaswordskills.util.PlayerUtils;

/**
 * 
 * Broken version of each sword; can only be repaired by a blacksmith.
 *
 */
public class ItemBrokenSword extends BaseModItem implements IUnenchantable, IWeapon
{
	private static final String[] parentSwords = new String[]{"sword_kokiri","sword_ordon","sword_giant","sword_darknut"};

	public ItemBrokenSword() {
		super();
		setFull3D();
		setMaxDamage(0);
		setMaxStackSize(1);
		setHasSubtypes(true);
		setCreativeTab(ZSSCreativeTabs.tabCombat);
	}

	@Override
	public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity entity) {
		if (entity instanceof EntityVillager && !player.worldObj.isRemote) {
			boolean isGoron = (entity instanceof EntityGoron);
			EntityVillager villager = (EntityVillager) entity;
			MerchantRecipeList trades = villager.getRecipes(player);
			Item brokenItem = ItemBrokenSword.getSwordByDamage(stack.getItemDamage());
			if (!(brokenItem instanceof ItemSword) || (brokenItem instanceof ItemZeldaSword && !((ItemZeldaSword) brokenItem).givesBrokenItem)) {
				ZSSMain.logger.warn("Broken sword contained an invalid item: " + brokenItem + "; defaulting to Ordon Sword");
				stack.setItemDamage(1);
				brokenItem = ZSSItems.swordOrdon;
			}
			if (EnumVillager.BLACKSMITH.is(villager) || isGoron) {
				if (brokenItem != ZSSItems.swordGiant) {
					PlayerUtils.sendTranslatedChat(player, "chat.zss.trade.sword.broken");
					MerchantRecipeHelper.addToListWithCheck(trades, new MerchantRecipe(stack.copy(), new ItemStack(Items.emerald, 5), new ItemStack(brokenItem)));
				} else if (isGoron && villager.getCustomNameTag().equals("Medigoron")) {
					if (ZSSPlayerSkills.get(player).getSkillLevel(SkillBase.bonusHeart) > 9) {
						player.triggerAchievement(ZSSAchievements.swordBroken);
						PlayerUtils.sendTranslatedChat(player, "chat.zss.trade.sword.broken.giant.1");
						PlayerUtils.sendTranslatedChat(player, "chat.zss.trade.sword.broken.giant.2");
						MerchantRecipeHelper.addToListWithCheck(trades, new MerchantRecipe(stack.copy(), new ItemStack(Items.emerald, 5), new ItemStack(brokenItem)));
					} else {
						PlayerUtils.sendTranslatedChat(player, "chat.zss.trade.sword.big");
						PlayerUtils.sendTranslatedChat(player, "chat.zss.trade.sword.later");
					}
				} else {
					PlayerUtils.sendTranslatedChat(player, "chat.zss.trade.sword.sorry");
				}
			} else {
				PlayerUtils.sendTranslatedChat(player, "chat.zss.trade.sword.sorry");
			}
			return true;
		}
		return false;
	}

	@Override
	public String getItemStackDisplayName(ItemStack stack) {
		Item sword = ItemBrokenSword.getSwordByDamage(stack.getItemDamage());
		if (sword == null) {
			ZSSMain.logger.warn("Unable to determine parent sword for broken sword with damage value " + stack.getItemDamage());
			return super.getItemStackDisplayName(stack);
		}
		return StatCollector.translateToLocalFormatted(getUnlocalizedName() + ".name", StatCollector.translateToLocal(sword.getUnlocalizedName() + ".name"));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(Item item, CreativeTabs tab, List<ItemStack> list) {
		for (int i = 0; i < parentSwords.length; ++i) {
			list.add(new ItemStack(item, 1, i));
		}
	}

	@Override
	public String[] getVariants() {
		String[] variants = new String[parentSwords.length];
		for (int i = 0; i < parentSwords.length; ++i) {
			variants[i] = ModInfo.ID + ":broken_" + parentSwords[i];
		}
		return variants;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, EntityPlayer player, List<String> list, boolean advanced) {
		list.add(StatCollector.translateToLocal("tooltip.zss.sword_broken.desc.0"));
	}

	@Override
	public Multimap<String, AttributeModifier> getAttributeModifiers(ItemStack stack) {
		Multimap<String, AttributeModifier> multimap = super.getAttributeModifiers(stack);
		multimap.put(SharedMonsterAttributes.attackDamage.getAttributeUnlocalizedName(), new AttributeModifier(itemModifierUUID, "Weapon modifier", 2.0D, 0));
		return multimap;
	}

	@Override
	public boolean isSword(ItemStack stack) {
		return !WeaponRegistry.INSTANCE.isSwordForbidden(this);
	}

	@Override
	public boolean isWeapon(ItemStack stack) {
		return !WeaponRegistry.INSTANCE.isWeaponForbidden(this);
	}

	/**
	 * Returns the broken version of the item, if any (may return NULL)
	 */
	public static ItemStack getBrokenSwordFor(Item item) {
		String name = item.getUnlocalizedName();
		name = name.substring(name.lastIndexOf(".") + 1);
		for (int i = 0; i < parentSwords.length; ++i) {
			if (parentSwords[i].equals(name)) {
				return new ItemStack(ZSSItems.swordBroken, 1, i);
			}
		}
		return null;
	}

	/**
	 * Returns the full version of this broken sword
	 */
	public static Item getSwordByDamage(int damage) {
		if (damage > -1 && damage < parentSwords.length) {
			return GameRegistry.findItem(ModInfo.ID, parentSwords[damage]);
		}
		return ZSSItems.swordOrdon;
	}
}
