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

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.INpc;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.StatCollector;
import net.minecraft.village.MerchantRecipe;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import zeldaswordskills.ZSSAchievements;
import zeldaswordskills.api.item.IUnenchantable;
import zeldaswordskills.creativetab.ZSSCreativeTabs;
import zeldaswordskills.entity.ZSSPlayerInfo;
import zeldaswordskills.entity.ZSSPlayerSkills;
import zeldaswordskills.entity.ZSSVillagerInfo;
import zeldaswordskills.entity.npc.EntityNpcMaskTrader;
import zeldaswordskills.entity.npc.EntityNpcOrca;
import zeldaswordskills.ref.ModInfo;
import zeldaswordskills.ref.Sounds;
import zeldaswordskills.util.PlayerUtils;
import zeldaswordskills.util.TimedChatDialogue;

/**
 * 
 * Rare items with no use other than as potential trades for upgrades
 *
 */
public class ItemTreasure extends BaseModItem implements IUnenchantable
{
	/** All the different treasure types */
	public static enum Treasures {
		CLAIM_CHECK("claim_check"),
		COJIRO("cojiro"),
		EVIL_CRYSTAL("evil_crystal"),
		EYE_DROPS("eye_drops"),
		EYEBALL_FROG("eyeball_frog"),
		GORON_SWORD("goron_sword"),
		JELLY_BLOB("jelly_blob","default",true,32,64),
		MONSTER_CLAW("monster_claw","default",true,24,64),
		ODD_MUSHROOM("odd_mushroom"),
		ODD_POTION("odd_potion"),
		POACHER_SAW("poacher_saw"),
		POCKET_EGG("pocket_egg"),
		PRESCRIPTION("prescription"),
		TENTACLE("tentacle","default",true,16,64),
		ZELDAS_LETTER("zeldas_letter"),
		KNIGHTS_CREST("knights_crest","knights_crest",true,32,64);

		public final String name;
		/** Unlocalized string used to retrieve chat comment when an NPC is not interested in trading */
		public final String uninterested;
		private final boolean canSell;
		private final int value;
		private final int maxStackSize;

		private Treasures(String name) {
			this(name, "default", false, 0, 1);
		}

		private Treasures(String name, boolean canSell, int value) {
			this(name, "default", canSell, value, 1);
		}

		private Treasures(String name, String uninterested, boolean canSell, int value, int maxStackSize) {
			this.name = name;
			this.uninterested = uninterested;
			this.canSell = canSell;
			// this.value = value;
			// TODO there is a vanilla bug that prevents distinguishing between subtypes for the items to buy
			this.value = 24;
			this.maxStackSize = maxStackSize;
		}
		/** Whether this treasure is salable (currently used only for monster parts) */
		public boolean canSell() { return canSell; }
		/** The price at which the hunter will buy this treasure */
		public int getValue() { return value; }
		/** The maximum stack size for this treasure */
		public int getMaxStackSize() { return maxStackSize; }

		public static Treasures byDamage(int damage) {
			return values()[damage % values().length];
		}
	};

	public ItemTreasure() {
		super();
		setMaxDamage(0);
		setMaxStackSize(1);
		setHasSubtypes(true);
		setCreativeTab(ZSSCreativeTabs.tabMisc);
	}

	@Override
	public int getItemStackLimit(ItemStack stack) {
		return Treasures.byDamage(stack.getItemDamage()).getMaxStackSize();
	}

	@Override
	public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity entity) {
		if (!player.worldObj.isRemote) {
			Treasures treasure = Treasures.byDamage(stack.getItemDamage());
			if (entity instanceof EntityVillager) {
				EntityVillager villager = (EntityVillager) entity;
				ZSSVillagerInfo villagerInfo = ZSSVillagerInfo.get(villager);
				MerchantRecipe trade = ZSSVillagerInfo.getTreasureTrade(treasure);
				boolean isBaseVillager = entity.getClass().isAssignableFrom(EntityVillager.class);
				villager.playLivingSound();
				if (treasure == Treasures.KNIGHTS_CREST && isBaseVillager && villager.getCustomNameTag().equals("Orca")) {
					if (villager.isChild()) {
						PlayerUtils.sendTranslatedChat(player, "chat.zss.trade.generic.child");
						return true;
					}
					EntityNpcOrca orca = new EntityNpcOrca(villager.worldObj);
					orca.setLocationAndAngles(villager.posX, villager.posY, villager.posZ, villager.rotationYaw, villager.rotationPitch);
					orca.setCustomNameTag(villager.getCustomNameTag());
					if (!orca.worldObj.isRemote) {
						orca.worldObj.spawnEntityInWorld(orca);
					}
					villager.setDead();
					PlayerUtils.playSound(player, Sounds.SUCCESS, 1.0F, 1.0F);
					ZSSPlayerSkills.get(player).giveCrest();
				} else if (treasure == Treasures.ZELDAS_LETTER) {
					if (villager.isChild()) {
						PlayerUtils.sendTranslatedChat(player, "chat.zss.treasure." + treasure.name + ".child");
					} else if (isBaseVillager && villager.getCustomNameTag().contains("Mask Salesman")) {
						EntityNpcMaskTrader trader = new EntityNpcMaskTrader(villager.worldObj);
						trader.setLocationAndAngles(villager.posX, villager.posY, villager.posZ, villager.rotationYaw, villager.rotationPitch);
						trader.setCustomNameTag(villager.getCustomNameTag());
						if (!trader.worldObj.isRemote) {
							trader.worldObj.spawnEntityInWorld(trader);
						}
						villager.setDead();
						PlayerUtils.playSound(player, Sounds.SUCCESS, 1.0F, 1.0F);
						player.triggerAchievement(ZSSAchievements.maskTrader);
						if (ZSSPlayerInfo.get(player).getCurrentMaskStage() == 0) {
							IChatComponent[] chat = new IChatComponent[5];
							for (int i = 0; i < 5; ++i) {
								chat[i] = new ChatComponentTranslation("chat.zss.treasure." + treasure.name + ".success." + i);
							}
							new TimedChatDialogue(player, chat);
						} else {
							PlayerUtils.sendTranslatedChat(player, "chat.zss.treasure." + treasure.name + ".already_open");
						}
						player.setCurrentItemOrArmor(0, null);
					} else {
						PlayerUtils.sendTranslatedChat(player, "chat.zss.treasure." + treasure.name + ".fail");
					}
				} else if (trade != null && villagerInfo.isInterested(treasure, stack)) {
					// This section is allowed for child villagers due to Biggoron's Trade Sequence requirements
					ItemStack required = trade.getSecondItemToBuy();
					if (required == null || PlayerUtils.consumeInventoryItem(player, required, required.stackSize)) {
						PlayerUtils.playSound(player, Sounds.SUCCESS, 1.0F, 1.0F);
						player.setCurrentItemOrArmor(0, trade.getItemToSell());
						PlayerUtils.sendTranslatedChat(player, "chat." + getUnlocalizedName(stack).substring(5) + ".give");
						PlayerUtils.sendFormattedChat(player, "chat.zss.treasure.received", trade.getItemToSell().getDisplayName());
						if (villagerInfo.onTradedTreasure(player, treasure, player.getHeldItem())) {
							PlayerUtils.sendTranslatedChat(player, "chat." + getUnlocalizedName(stack).substring(5) + ".next");
						}
					} else {
						PlayerUtils.sendFormattedChat(player, "chat.zss.treasure.trade.fail", required.stackSize, required.getDisplayName(), (required.stackSize > 1 ? "s" : ""));
					}
				} else if (treasure.canSell() && villagerInfo.isHunter()) {
					villagerInfo.addHunterTrade(player, new ItemStack(ZSSItems.treasure,1,treasure.ordinal()), treasure.getValue());
				} else {
					if (villagerInfo.isFinalTrade(treasure, stack)) {
						PlayerUtils.sendTranslatedChat(player, "chat." + getUnlocalizedName(stack).substring(5) + ".wait");
					} else {
						PlayerUtils.sendTranslatedChat(player, "chat.zss.treasure.uninterested." + treasure.uninterested);
					}
				}
			} else if (entity instanceof INpc) {
				if (entity instanceof EntityAgeable && ((EntityAgeable) entity).isChild()) {
					PlayerUtils.sendTranslatedChat(player, "chat.zss.trade.generic.child");
				} else if (treasure == Treasures.KNIGHTS_CREST && entity instanceof EntityNpcOrca) {
					PlayerUtils.sendTranslatedChat(player, "chat.zss.treasure.uninterested." + treasure.uninterested + ".orca");
				} else {
					PlayerUtils.sendTranslatedChat(player, "chat.zss.treasure.uninterested." + treasure.uninterested);
				}
			}
		}
		return true;
	}

	@Override
	public String getUnlocalizedName(ItemStack stack) {
		return getUnlocalizedName() + "." + Treasures.byDamage(stack.getItemDamage()).name;
	}

	@Override
	public String[] getVariants() {
		String[] variants = new String[Treasures.values().length];
		for (Treasures treasure : Treasures.values()) {
			variants[treasure.ordinal()] = ModInfo.ID + ":" + treasure.name;
		}
		return variants;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(Item item, CreativeTabs tab, List list) {
		for (int i = 0; i < Treasures.values().length; ++i) {
			list.add(new ItemStack(item, 1, i));
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack,	EntityPlayer player, List list, boolean par4) {
		list.add(EnumChatFormatting.ITALIC + StatCollector.translateToLocal("tooltip." + getUnlocalizedName(stack).substring(5) + ".desc.0"));
	}
}
