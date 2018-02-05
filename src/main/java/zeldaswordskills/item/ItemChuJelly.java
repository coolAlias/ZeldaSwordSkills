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

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;
import zeldaswordskills.api.entity.EnumVillager;
import zeldaswordskills.api.item.IRightClickEntity;
import zeldaswordskills.api.item.IUnenchantable;
import zeldaswordskills.creativetab.ZSSCreativeTabs;
import zeldaswordskills.entity.ZSSVillagerInfo;
import zeldaswordskills.entity.mobs.EntityChu.ChuType;
import zeldaswordskills.entity.player.ChuJellyTracker;
import zeldaswordskills.ref.ModInfo;
import zeldaswordskills.ref.Sounds;
import zeldaswordskills.util.PlayerUtils;

public class ItemChuJelly extends Item implements IRightClickEntity, IUnenchantable
{
	@SideOnly(Side.CLIENT)
	private IIcon[] iconArray;

	public static final Map<ChuType, Item> POTION_MAP = new EnumMap<ChuType, Item>(ChuType.class);

	public ItemChuJelly() {
		super();
		setMaxDamage(0);
		setHasSubtypes(true);
		setCreativeTab(ZSSCreativeTabs.tabMisc);
	}

	/** Safe method for obtaining chu type from the stack, regardless of stack damage value */
	public ChuType getType(ItemStack stack) {
		return ChuType.values()[stack.getItemDamage() % ChuType.values().length];
	}

	@Override
	public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity entity) {
		if (!player.worldObj.isRemote && entity.getClass() == EntityVillager.class) {
			EntityVillager villager = (EntityVillager) entity;
			ChuJellyTracker jellyTracker = ChuJellyTracker.get(player);
			/*
			How is this supposed to work?
				- Give 15 jellies of one type to receive a free potion
					: GIVE, not SELL!!! easy solution.
				- After that, potions of that type are available for purchase, and can still sell jellies
				- i.e. a repeatable quest that can be done for free potions, and complete it once to unlock each potion trade

			Note that it's not very feasible to send a bunch of chat messages while the trading GUI is open,
			and the player won't be readily available after it has been closed...
			 */
			if (ZSSVillagerInfo.get(villager).isChuTrader()) {
				ChuType type = getType(stack);
				if (POTION_MAP.containsKey(type)) {
					if (jellyTracker.getJelliesReceived(type) == 0) {
						PlayerUtils.sendTranslatedChat(player, "chat.zss.chu_jelly.doc.first");
						jellyTracker.addJelly(type, 1);
						--stack.stackSize;
					} else if (jellyTracker.giveJellies(type, stack)) {
						ItemStack potion = new ItemStack(POTION_MAP.get(type));
						player.worldObj.playSoundAtEntity(player, Sounds.SUCCESS, 1.0F, 1.0F);
						PlayerUtils.addItemToInventory(player, potion.copy());
						if (jellyTracker.canBuyType(type)) {
							PlayerUtils.sendTranslatedChat(player, "chat.zss.chu_jelly.doc.repeat");
						} else {
							PlayerUtils.sendTranslatedChat(player, "chat.zss.chu_jelly.doc.new_stock");
						}
					} else if (jellyTracker.canBuyType(type)) {
						PlayerUtils.sendTranslatedChat(player, "chat.zss.chu_jelly.doc.in_stock");
					} else {
						PlayerUtils.sendTranslatedChat(player, "chat.zss.chu_jelly.doc.need_more");
					}
				} else {
					PlayerUtils.sendTranslatedChat(player, "chat.zss.chu_jelly.doc.unknown");
				}
			} else {
				EnumVillager profession = EnumVillager.get(villager);
				PlayerUtils.sendTranslatedChat(player, "chat.zss.chu_jelly.villager." + (profession == null ? "generic" : profession.unlocalizedName));
			}
		}
		return true;
	}

	@Override
	public boolean onRightClickEntity(ItemStack stack, EntityPlayer player, Entity entity) {
		if (entity.getClass() == EntityVillager.class) {
			EntityVillager villager = (EntityVillager) entity;
			String chat = null;
			if (ZSSVillagerInfo.get(villager).isChuTrader()) {

			} else {
				EnumVillager profession = EnumVillager.get(villager);
				chat = "chat.zss.chu_jelly.villager." + (profession == null ? "generic" : profession.unlocalizedName);
			}
			if (chat != null && !player.worldObj.isRemote) {
				PlayerUtils.sendTranslatedChat(player, chat);
			}
			return true;
		}
		return false;
	}

	@Override
	public String getUnlocalizedName(ItemStack stack) {
		return this.getUnlocalizedName() + "." + this.getType(stack).ordinal();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(Item item, CreativeTabs tab, List list) {
		for (int i = 0; i < ChuType.values().length; ++i) {
			list.add(new ItemStack(item, 1, i));
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIconFromDamage(int damage) {
		return iconArray[damage % ChuType.values().length];
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister register) {
		iconArray = new IIcon[ChuType.values().length];
		for (int i = 0; i < iconArray.length; ++i) {
			iconArray[i] = register.registerIcon(ModInfo.ID + ":" + getUnlocalizedName().substring(9) + i);
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack,	EntityPlayer player, List list, boolean isHeld) {
		list.add(StatCollector.translateToLocal("tooltip.zss.jelly_chu.desc.0"));
	}

	public static void initializeJellies() {
		POTION_MAP.put(ChuType.RED, ZSSItems.potionRed);
		POTION_MAP.put(ChuType.GREEN, ZSSItems.potionGreen);
		POTION_MAP.put(ChuType.BLUE, ZSSItems.potionBlue);
		POTION_MAP.put(ChuType.YELLOW, ZSSItems.potionYellow);
	}
}
