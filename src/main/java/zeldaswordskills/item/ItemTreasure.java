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

import net.minecraft.client.renderer.texture.IIconRegister;
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
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;
import net.minecraft.village.MerchantRecipe;
import zeldaswordskills.api.entity.NpcHelper;
import zeldaswordskills.api.item.IRightClickEntity;
import zeldaswordskills.api.item.IUnenchantable;
import zeldaswordskills.creativetab.ZSSCreativeTabs;
import zeldaswordskills.entity.ZSSVillagerInfo;
import zeldaswordskills.entity.npc.EntityNpcMaskTrader;
import zeldaswordskills.entity.npc.EntityNpcOrca;
import zeldaswordskills.entity.player.quests.IQuest;
import zeldaswordskills.entity.player.quests.QuestBase;
import zeldaswordskills.entity.player.quests.QuestBiggoronSword;
import zeldaswordskills.entity.player.quests.QuestMaskSales;
import zeldaswordskills.entity.player.quests.QuestMaskShop;
import zeldaswordskills.entity.player.quests.ZSSQuests;
import zeldaswordskills.ref.ModInfo;
import zeldaswordskills.ref.Sounds;
import zeldaswordskills.util.PlayerUtils;
import zeldaswordskills.util.TimedAddItem;
import zeldaswordskills.util.TimedChatDialogue;
import cpw.mods.fml.common.eventhandler.Event.Result;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * 
 * Rare items with no use other than as potential trades for upgrades
 *
 */
public class ItemTreasure extends Item implements IRightClickEntity, IUnenchantable
{
	/** All the different treasure types */
	public static enum Treasures {
		CLAIM_CHECK("claim_check", true),
		COJIRO("cojiro", true),
		EVIL_CRYSTAL("evil_crystal"),
		EYE_DROPS("eye_drops", true),
		EYEBALL_FROG("eyeball_frog", true),
		GORON_SWORD("goron_sword", true),
		JELLY_BLOB("jelly_blob", "generic", 32, 64, false),
		MONSTER_CLAW("monster_claw", "generic", 24, 64, false),
		ODD_MUSHROOM("odd_mushroom", true),
		ODD_POTION("odd_potion", true),
		POACHER_SAW("poacher_saw", true),
		POCKET_EGG("pocket_egg", true),
		PRESCRIPTION("prescription", true),
		TENTACLE("tentacle", "generic", 16, 64, true),
		ZELDAS_LETTER("zeldas_letter"),
		KNIGHTS_CREST("knights_crest", "knights_crest", 32, 64, false);

		public final String name;
		/** Unlocalized string used to retrieve chat comment when an NPC is not interested in trading */
		public final String uninterested;
		private final int value;
		private final int maxStackSize;
		private final boolean isBiggoronTrade;

		private Treasures(String name) {
			this(name, "generic", 0, 1, false);
		}

		private Treasures(String name, boolean isBiggoronTrade) {
			this(name, "generic", 0, 1, isBiggoronTrade);
		}

		private Treasures(String name, int value) {
			this(name, "generic", value, 1, false);
		}

		private Treasures(String name, String uninterested, int value, int maxStackSize, boolean isBiggoronTrade) {
			this.name = name;
			this.uninterested = uninterested;
			this.value = value;
			this.maxStackSize = maxStackSize;
			this.isBiggoronTrade = isBiggoronTrade;
		}
		/** Whether this treasure is salable (currently used only for monster parts) */
		public boolean canSell() { return value > 0; }
		/** The price at which the hunter will buy this treasure */
		public int getValue() { return value; }
		/** The maximum stack size for this treasure */
		public int getMaxStackSize() { return maxStackSize; }
		/** True for items that are part of Biggoron's Trading sequence */
		public boolean isBiggoronTrade() { return isBiggoronTrade; }

		public static Treasures byDamage(int damage) {
			return values()[damage % values().length];
		}
	};

	@SideOnly(Side.CLIENT)
	private IIcon[] iconArray;

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
		if (player.worldObj.isRemote) {
			return true;
		} else if (entity instanceof EntityNpcMaskTrader && ((EntityNpcMaskTrader) entity).checkShopStatus(player, false, true)) {
			return true; // allows quest to complete without having to convert a villager if Salesman already in town
		} else if (entity instanceof EntityVillager && Result.DEFAULT == NpcHelper.convertVillager(player, (EntityVillager) entity, false)) {
			// villager not converted, try other treasure interactions (note that conversion handles beginnings of several quests)
			handleTrade(stack, player, entity, true);
		} else if (entity instanceof INpc) {
			handleTrade(stack, player, entity, true);
		}
		return true;
	}

	@Override
	public boolean onRightClickEntity(ItemStack stack, EntityPlayer player, Entity entity) {
		return !player.worldObj.isRemote && handleTrade(stack, player, entity, false);
	}

	private boolean handleTrade(ItemStack stack, EntityPlayer player, Entity entity, boolean isLeftClick) {
		Treasures treasure = Treasures.byDamage(stack.getItemDamage());
		if (treasure.isBiggoronTrade()) {
			return handleBiggoronQuest(stack, player, entity, isLeftClick);
		} else if (entity instanceof EntityVillager) {
			if (treasure == Treasures.ZELDAS_LETTER) {
				PlayerUtils.sendTranslatedChat(player, "chat.zss.treasure." + treasure.name + (((EntityVillager) entity).isChild() ? ".child" : ".fail") + (isLeftClick ? ".give" : ".show"));
			} else if (!handleVillagerTrade(stack, player, (EntityVillager) entity, isLeftClick)) {
				PlayerUtils.sendTranslatedChat(player, "chat.zss.treasure." + treasure.uninterested + ".uninterested");
			}
			return true;
		} else if (entity instanceof INpc) {
			if (entity instanceof EntityAgeable && ((EntityAgeable) entity).isChild()) {
				handleChildTrade(stack, player, isLeftClick);
			} else if (treasure == Treasures.ZELDAS_LETTER && entity instanceof EntityNpcMaskTrader) {
				String s = (ZSSQuests.get(player).hasCompleted(QuestMaskSales.class) ? "open" : (ZSSQuests.get(player).hasCompleted(QuestMaskShop.class) ? "opening" : "hint." + itemRand.nextInt(4)));
				PlayerUtils.sendTranslatedChat(player, "chat.zss.npc.mask_salesman.shop." + s);
			} else if (treasure == Treasures.KNIGHTS_CREST && entity instanceof EntityNpcOrca) {
				PlayerUtils.sendTranslatedChat(player, "chat.zss.treasure." + treasure.uninterested + ".uninterested.orca");
			} else {
				PlayerUtils.sendTranslatedChat(player, "chat.zss.treasure." + treasure.uninterested + ".uninterested");
			}
			return true;
		}
		return false;
	}

	/**
	 * Attempts to sell the item to a villager, returning true if any chat message occurred.
	 * If the villager is a child, the child interaction is performed instead.
	 * @return only false if {@link ZSSVillagerInfo#getTreasureTrade} returns null
	 */
	public boolean handleVillagerTrade(ItemStack stack, EntityPlayer player, EntityVillager villager, boolean isLeftClick) {
		Treasures treasure = Treasures.byDamage(stack.getItemDamage());
		ZSSVillagerInfo villagerInfo = ZSSVillagerInfo.get(villager);
		MerchantRecipe trade = villagerInfo.getTreasureTrade(treasure);
		villager.playLivingSound();
		if (villager.isChild()) {
			handleChildTrade(stack, player, isLeftClick);
		} else if (trade == null) {
			return false;
		} else if (isLeftClick) {
			// 1st item to buy is always the treasure itself, which is in hand and will be replaced
			ItemStack required = trade.getSecondItemToBuy();
			if (required == null || PlayerUtils.consumeInventoryItem(player, required, required.stackSize)) {
				PlayerUtils.playSound(player, Sounds.SUCCESS, 1.0F, 1.0F);
				if (stack.stackSize > 1) {
					--stack.stackSize;
					PlayerUtils.addItemToInventory(player, trade.getItemToSell().copy());
				} else {
					player.setCurrentItemOrArmor(0, trade.getItemToSell().copy());
				}
				if (villagerInfo.isHunter()) {
					PlayerUtils.sendFormattedChat(player, "chat.zss.treasure.hunter.trade." + itemRand.nextInt(4));
				} else {
					new TimedChatDialogue(player, 0, 500,
							new ChatComponentTranslation("chat.zss.treasure." + treasure.name + ".trade"),
							new ChatComponentTranslation("chat.zss.treasure.generic.received", new ChatComponentTranslation(trade.getItemToSell().getUnlocalizedName() + ".name")));
				}
			} else { // can only be true when required stack is not null
				PlayerUtils.sendFormattedChat(player, "chat.zss.treasure.generic.trade.fail", required.stackSize, new ChatComponentTranslation(required.getUnlocalizedName() + ".name"), (required.stackSize > 1 ? "s" : ""));
			}
		} else if (villagerInfo.isHunter()) {
			new TimedChatDialogue(player, 0, 1000,
					new ChatComponentTranslation("chat.zss.treasure.hunter.interested.0", new ChatComponentTranslation(trade.getItemToBuy().getUnlocalizedName() + ".name")),
					new ChatComponentTranslation("chat.zss.treasure.hunter.interested.1", trade.getItemToSell().stackSize));
		} else {
			PlayerUtils.sendFormattedChat(player, "chat.zss.treasure.generic.interested", new ChatComponentTranslation(trade.getItemToSell().getUnlocalizedName() + ".name"));
		}
		return true;
	}

	private boolean handleBiggoronQuest(ItemStack stack, EntityPlayer player, Entity entity, boolean isLeftClick) {
		ZSSQuests quests = ZSSQuests.get(player);
		IQuest quest = quests.get(QuestBiggoronSword.class);
		if (quest == null) {
			quest = new QuestBiggoronSword();
			quests.add(quest);
		}
		if (QuestBase.checkQuestProgress(player, quest, QuestBase.DEFAULT_QUEST_HANDLER, entity, isLeftClick)) {
			return true;
		} else if (entity instanceof EntityVillager && ((EntityVillager) entity).isChild()) {
			handleChildTrade(stack, player, isLeftClick); // handle non-salable trading sequence treasures
			return true;
		}
		return false;
	}

	private void handleChildTrade(ItemStack stack, EntityPlayer player, boolean isLeftClick) {
		PlayerUtils.sendTranslatedChat(player, "chat.zss.treasure.generic.child" + (isLeftClick ? ".give" : ".show"));
		if (isLeftClick) {
			player.setCurrentItemOrArmor(0, null);
			if (itemRand.nextFloat() < 0.99F || Treasures.byDamage(stack.getItemDamage()).isBiggoronTrade()) {
				new TimedChatDialogue(player, 1500, 1250, new ChatComponentTranslation("chat.zss.treasure.generic.child.return"));
				new TimedAddItem(player, stack.copy(), 1500);
			} else {
				new TimedChatDialogue(player, 1500, 1250, new ChatComponentTranslation("chat.zss.treasure.generic.child.broken"));
			}
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIconFromDamage(int par1) {
		return iconArray[par1 % Treasures.values().length];
	}

	@Override
	public String getUnlocalizedName(ItemStack stack) {
		return super.getUnlocalizedName() + "." + Treasures.byDamage(stack.getItemDamage()).name;
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
	public void registerIcons(IIconRegister register) {
		iconArray = new IIcon[Treasures.values().length];
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
