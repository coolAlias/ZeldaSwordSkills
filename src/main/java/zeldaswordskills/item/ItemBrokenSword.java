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

package zeldaswordskills.item;

import java.util.List;

import mods.battlegear2.api.PlayerEventChild.OffhandAttackEvent;
import mods.battlegear2.api.weapons.IBattlegearWeapon;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.Icon;
import net.minecraft.util.StatCollector;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import zeldaswordskills.ZSSAchievements;
import zeldaswordskills.creativetab.ZSSCreativeTabs;
import zeldaswordskills.entity.EntityGoron;
import zeldaswordskills.entity.ZSSPlayerSkills;
import zeldaswordskills.lib.ModInfo;
import zeldaswordskills.skills.SkillBase;
import zeldaswordskills.util.LogHelper;
import zeldaswordskills.util.MerchantRecipeHelper;
import zeldaswordskills.util.PlayerUtils;

import com.google.common.collect.Multimap;

import cpw.mods.fml.common.Optional;
import cpw.mods.fml.common.Optional.Method;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * 
 * Broken version of each sword; can only be repaired by a blacksmith.
 *
 */
@Optional.Interface(iface="mods.battlegear2.api.weapons.IBattlegearWeapon", modid="battlegear2", striprefs=true)
public class ItemBrokenSword extends Item implements IBattlegearWeapon
{
	public ItemBrokenSword(int id) {
		super(id);
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
			Item brokenItem = Item.itemsList[stack.getItemDamage()];
			if (!(brokenItem instanceof ItemSword) || (brokenItem instanceof ItemZeldaSword && !((ItemZeldaSword) brokenItem).givesBrokenItem)) {
				LogHelper.warning("Broken sword contained an invalid item: " + brokenItem + "; defaulting to Ordon Sword");
				brokenItem = ZSSItems.swordOrdon;
				stack.setItemDamage(brokenItem.itemID);
			}
			if (villager.getProfession() == 3 || isGoron) {
				if (brokenItem != ZSSItems.swordGiant) {
					PlayerUtils.sendTranslatedChat(player, "chat.zss.trade.sword.broken");
					MerchantRecipeHelper.addToListWithCheck(trades, new MerchantRecipe(stack.copy(), new ItemStack(Item.emerald, 5), new ItemStack(stack.getItemDamage(), 1, 0)));
				} else if (isGoron && villager.getCustomNameTag().equals("Medigoron")) {
					if (ZSSPlayerSkills.get(player).getSkillLevel(SkillBase.bonusHeart) > 9) {
						player.triggerAchievement(ZSSAchievements.swordBroken);
						PlayerUtils.sendTranslatedChat(player, "chat.zss.trade.sword.broken.giant.1");
						PlayerUtils.sendTranslatedChat(player, "chat.zss.trade.sword.broken.giant.2");
						MerchantRecipeHelper.addToListWithCheck(trades, new MerchantRecipe(stack.copy(), new ItemStack(Item.emerald, 5), new ItemStack(stack.getItemDamage(), 1, 0)));
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
	@SideOnly(Side.CLIENT)
	public Icon getIconFromDamage(int damage) {
		if (Item.itemsList[damage] instanceof ItemZeldaSword) {
			return Item.itemsList[damage].getIconFromDamage(-1);
		} else {
			return itemIcon;
		}
	}

	@Override
	public String getItemDisplayName(ItemStack stack) {
		Item sword = (stack.getItemDamage() > 0 ? Item.itemsList[stack.getItemDamage()] : null);
		String name = (sword instanceof ItemZeldaSword && ((ItemZeldaSword) sword).givesBrokenItem) ? sword.getUnlocalizedName() : ZSSItems.swordOrdon.getUnlocalizedName();
		return StatCollector.translateToLocal(getUnlocalizedName() + ".name") + " " + StatCollector.translateToLocal(name + ".name");
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(int itemID, CreativeTabs tab, List list) {
		list.add(new ItemStack(itemID, 1, ZSSItems.swordKokiri.itemID));
		list.add(new ItemStack(itemID, 1, ZSSItems.swordOrdon.itemID));
		list.add(new ItemStack(itemID, 1, ZSSItems.swordGiant.itemID));
		list.add(new ItemStack(itemID, 1, ZSSItems.swordDarknut.itemID));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack,	EntityPlayer player, List list, boolean par4) {
		list.add(EnumChatFormatting.ITALIC + StatCollector.translateToLocal("tooltip.zss.sword_broken.desc.0"));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister register) {
		itemIcon = register.registerIcon(ModInfo.ID + ":broken_sword_ordon");
	}

	@Override
	public Multimap getItemAttributeModifiers() {
		Multimap multimap = super.getItemAttributeModifiers();
		multimap.put(SharedMonsterAttributes.attackDamage.getAttributeUnlocalizedName(), new AttributeModifier(field_111210_e, "Weapon modifier", 2.0D, 0));
		return multimap;
	}

	@Method(modid="battlegear2")
	@Override
	public boolean sheatheOnBack(ItemStack stack) {
		return false;
	}

	@Method(modid="battlegear2")
	@Override
	public boolean isOffhandHandDual(ItemStack stack) {
		return true;
	}

	@Method(modid="battlegear2")
	@Override
	public boolean offhandAttackEntity(OffhandAttackEvent event, ItemStack main, ItemStack offhand) {
		return true;
	}

	@Method(modid="battlegear2")
	@Override
	public boolean offhandClickAir(PlayerInteractEvent event, ItemStack main, ItemStack offhand) {
		return true;
	}

	@Method(modid="battlegear2")
	@Override
	public boolean offhandClickBlock(PlayerInteractEvent event, ItemStack main, ItemStack offhand) {
		return true;
	}

	@Method(modid="battlegear2")
	@Override
	public void performPassiveEffects(Side side, ItemStack main, ItemStack offhand) {}

	@Method(modid="battlegear2")
	@Override
	public boolean allowOffhand(ItemStack main, ItemStack offhand) {
		return true;
	}
}
