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
import net.minecraft.entity.INpc;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.Icon;
import net.minecraft.util.StatCollector;
import net.minecraft.village.MerchantRecipe;
import zeldaswordskills.ZSSAchievements;
import zeldaswordskills.creativetab.ZSSCreativeTabs;
import zeldaswordskills.entity.EntityGoron;
import zeldaswordskills.entity.EntityMaskTrader;
import zeldaswordskills.entity.ZSSPlayerInfo;
import zeldaswordskills.entity.ZSSVillagerInfo;
import zeldaswordskills.lib.ModInfo;
import zeldaswordskills.lib.Sounds;
import zeldaswordskills.util.MerchantRecipeHelper;
import zeldaswordskills.util.PlayerUtils;
import zeldaswordskills.util.TimedChatDialogue;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * 
 * Rare items with no use other than as potential trades for upgrades
 *
 */
public class ItemTreasure extends Item
{
	/** All the different treasure types */
	public static enum Treasures {
		CLAIM_CHECK("claim_check"),
		COJIRO("cojiro"),
		EVIL_CRYSTAL("evil_crystal"),
		EYE_DROPS("eye_drops"),
		EYEBALL_FROG("eyeball_frog"),
		GORON_SWORD("goron_sword"),
		JELLY_BLOB("jelly_blob",true,32),
		MONSTER_CLAW("monster_claw",true,24),
		ODD_MUSHROOM("odd_mushroom"),
		ODD_POTION("odd_potion"),
		POACHER_SAW("poacher_saw"),
		POCKET_EGG("pocket_egg"),
		PRESCRIPTION("prescription"),
		TENTACLE("tentacle",true,16),
		ZELDAS_LETTER("zeldas_letter");

		public final String name;
		private final boolean canSell;
		private final int value;
		private Treasures(String name) { this(name, false, 0); }
		private Treasures(String name, boolean canSell, int value) {
			this.name = name;
			this.canSell = canSell;
			// this.value = value;
			// TODO there is a vanilla bug that prevents distinguishing between subtypes for the items to buy
			this.value = 24;
		}
		/** Whether this treasure is salable (currently used only for monster parts) */
		public boolean canSell() { return canSell; }
		/** The price at which the hunter will buy this treasure */
		public int getValue() { return value; }
	};

	@SideOnly(Side.CLIENT)
	private Icon[] iconArray;

	public ItemTreasure(int id) {
		super(id);
		setMaxDamage(0);
		setMaxStackSize(1);
		setHasSubtypes(true);
		setCreativeTab(ZSSCreativeTabs.tabMisc);
	}

	@Override
	public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity entity) {
		if (!player.worldObj.isRemote) {
			if (entity instanceof EntityVillager) {
				EntityVillager villager = (EntityVillager) entity;
				ZSSVillagerInfo villagerInfo = ZSSVillagerInfo.get(villager);
				Treasures treasure = Treasures.values()[stack.getItemDamage() % Treasures.values().length];
				MerchantRecipe trade = ZSSVillagerInfo.getTreasureTrade(treasure);
				villager.playLivingSound();
				if (treasure == Treasures.ZELDAS_LETTER) {
					if (!(entity instanceof EntityGoron) && villager.getCustomNameTag().contains("Mask Salesman")) {
						EntityMaskTrader trader = new EntityMaskTrader(villager.worldObj);
						trader.setLocationAndAngles(villager.posX, villager.posY, villager.posZ, villager.rotationYaw, villager.rotationPitch);
						trader.setCustomNameTag(villager.getCustomNameTag());
						if (!trader.worldObj.isRemote) {
							trader.worldObj.spawnEntityInWorld(trader);
						}
						villager.setDead();
						PlayerUtils.playSound(player, Sounds.SUCCESS, 1.0F, 1.0F);
						player.triggerAchievement(ZSSAchievements.maskTrader);
						if (ZSSPlayerInfo.get(player).getCurrentMaskStage() == 0) {
							List<String> chat = new ArrayList<String>(5);
							for (int i = 0; i < 5; ++i) {
								chat.add(StatCollector.translateToLocal("chat.zss.treasure." + treasure.name + ".success." + i));
							}
							new TimedChatDialogue(player, chat);
						} else {
							player.addChatMessage(StatCollector.translateToLocal("chat.zss.treasure." + treasure.name + ".already_open"));
						}
						player.setCurrentItemOrArmor(0, null);
					} else {
						player.addChatMessage(StatCollector.translateToLocal("chat.zss.treasure." + treasure.name + ".fail"));
					}
				} else if (trade != null && villagerInfo.isInterested(treasure, stack)) {
					ItemStack required = trade.getSecondItemToBuy();
					if (required == null || PlayerUtils.consumeInventoryItems(player, trade.getSecondItemToBuy())) {
						PlayerUtils.playSound(player, Sounds.SUCCESS, 1.0F, 1.0F);
						player.setCurrentItemOrArmor(0, trade.getItemToSell());
						player.addChatMessage(StatCollector.translateToLocal("chat." + getUnlocalizedName(stack).substring(5) + ".give"));
						player.addChatMessage(StatCollector.translateToLocalFormatted("chat.zss.treasure.received", trade.getItemToSell().getDisplayName()));
						if (treasure == Treasures.TENTACLE) {
							player.triggerAchievement(ZSSAchievements.treasureFirst);
						}
						if (villagerInfo.onTradedTreasure(treasure, player.getHeldItem())) {
							player.addChatMessage(StatCollector.translateToLocal("chat." + getUnlocalizedName(stack).substring(5) + ".next"));
						} else if (treasure == Treasures.CLAIM_CHECK) {
							player.triggerAchievement(ZSSAchievements.treasureBiggoron);
							MerchantRecipeHelper.addUniqueTrade(villager.getRecipes(player), new MerchantRecipe(new ItemStack(ZSSItems.masterOre,3), new ItemStack(Item.diamond,4), new ItemStack(ZSSItems.swordBiggoron)));
						}
					} else {
						player.addChatMessage(StatCollector.translateToLocalFormatted("chat.zss.treasure.trade.fail", required.stackSize, required.getDisplayName(), (required.stackSize > 1 ? "s" : "")));
					}
				} else if (treasure.canSell() && villagerInfo.isHunter()) {
					ItemStack treasureStack = new ItemStack(ZSSItems.treasure,1,treasure.ordinal());
					int price = villagerInfo.isMonsterHunter() ? treasure.getValue() + treasure.getValue() / 2 : treasure.getValue();
					if (MerchantRecipeHelper.addToListWithCheck(villager.getRecipes(player), new MerchantRecipe(treasureStack, new ItemStack(Item.emerald, price)))) {
						PlayerUtils.playSound(player, Sounds.SUCCESS, 1.0F, 1.0F);
						player.addChatMessage(StatCollector.translateToLocalFormatted("chat.zss.treasure.hunter.new", treasureStack.getDisplayName()));
					} else {
						player.addChatMessage(StatCollector.translateToLocalFormatted("chat.zss.treasure.hunter.old", treasureStack.getDisplayName()));
					}
				} else {
					if (villagerInfo.isFinalTrade(treasure, stack)) {
						player.addChatMessage(StatCollector.translateToLocal("chat." + getUnlocalizedName(stack).substring(5) + ".wait"));
					} else {
						player.addChatMessage(StatCollector.translateToLocal("chat.zss.treasure.uninterested"));
					}
				}
			} else if (entity instanceof INpc) {
				player.addChatMessage(StatCollector.translateToLocal("chat.zss.treasure.uninterested"));
			}
		}
		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Icon getIconFromDamage(int par1) {
		return iconArray[par1 % Treasures.values().length];
	}

	@Override
	public String getUnlocalizedName(ItemStack stack) {
		return super.getUnlocalizedName() + "." + Treasures.values()[stack.getItemDamage() % Treasures.values().length].name;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(int item, CreativeTabs tab, List list) {
		for (int i = 0; i < Treasures.values().length; ++i) {
			list.add(new ItemStack(item, 1, i));
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister register) {
		iconArray = new Icon[Treasures.values().length];
		for (int i = 0; i < Treasures.values().length; ++i) {
			iconArray[i] = register.registerIcon(ModInfo.ID + ":" + Treasures.values()[i].name);
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack,	EntityPlayer player, List list, boolean par4) {
		list.add(EnumChatFormatting.ITALIC + StatCollector.translateToLocal("tooltip." + getUnlocalizedName(stack).substring(5) + ".desc.0"));
	}
}
