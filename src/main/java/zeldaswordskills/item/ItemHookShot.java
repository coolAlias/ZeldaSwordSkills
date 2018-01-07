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

import java.util.List;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import net.minecraft.world.World;
import zeldaswordskills.api.block.IHookable.HookshotType;
import zeldaswordskills.api.item.IUnenchantable;
import zeldaswordskills.creativetab.ZSSCreativeTabs;
import zeldaswordskills.entity.projectile.EntityHookShot;
import zeldaswordskills.handler.TradeHandler.EnumVillager;
import zeldaswordskills.ref.ModInfo;
import zeldaswordskills.ref.Sounds;
import zeldaswordskills.util.MerchantRecipeHelper;
import zeldaswordskills.util.PlayerUtils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * 
 * When right-clicked, the hookshot shoots out up to 8 blocks away, grabbing on to a suitable
 * material and pulling the player to that position. There are multiple variations:
 * 
 * Hookshot: grapples wood
 * Stoneshot: grapples stone; if it hits wood, it will destroy the block
 * Multishot: can grapple wood, stone, clay, and ground (but not sand)
 * Extended (add-on): doubles the reach distance
 * 
 * Note that all variations will break glass upon impact.
 * 
 * Only the first type (Hookshot) can be found; the others must be built from this base hookshot
 * by finding components and getting help from villagers.
 *
 */
public class ItemHookShot extends Item implements IUnenchantable
{
	protected static final String[] shotNames = {"Hookshot","Clawshot","Multishot"};

	@SideOnly(Side.CLIENT)
	private IIcon[] iconArray;

	public ItemHookShot() {
		super();
		setMaxStackSize(1);
		setHasSubtypes(true);
		setCreativeTab(ZSSCreativeTabs.tabTools);
		setFull3D();
	}

	/** Returns this hookshot's enum Type from stack damage value */
	public HookshotType getType(int damage) {
		return (damage > -1 ? HookshotType.values()[damage % HookshotType.values().length] : HookshotType.WOOD_SHOT);
	}

	@Override
	public EnumAction getItemUseAction(ItemStack par1ItemStack) {
		return EnumAction.block;
	}

	@Override
	public int getMaxItemUseDuration(ItemStack stack) {
		return 16000;
	}

	@Override
	public boolean isItemTool(ItemStack stack) {
		return true;
	}

	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
		player.setItemInUse(stack, getMaxItemUseDuration(stack));
		EntityHookShot shot = new EntityHookShot(player.worldObj, player);
		shot.setThrower(player);
		shot.setType(getType(stack.getItemDamage()));
		if (!player.worldObj.isRemote) {
			player.worldObj.spawnEntityInWorld(shot);
			player.worldObj.playSoundAtEntity(player, Sounds.HOOKSHOT, 1.0F, 1.0F);
		}
		return stack;
	}

	@Override
	public void onUsingTick(ItemStack stack, EntityPlayer player, int count) {
		if (count % 9 == 8 && !player.worldObj.isRemote) {
			player.worldObj.playSoundAtEntity(player, Sounds.HOOKSHOT, 1.0F, 1.0F);
		}
	}

	@Override
	public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity entity) {
		if (!player.worldObj.isRemote && entity.getClass() == EntityVillager.class) {
			EntityVillager villager = (EntityVillager) entity;
			MerchantRecipeList trades = villager.getRecipes(player);
			if (EnumVillager.BLACKSMITH.is(villager)) {
				MerchantRecipe trade = new MerchantRecipe(stack.copy(), new ItemStack(Items.emerald, 16));
				if (MerchantRecipeHelper.addToListWithCheck(trades, trade) || player.worldObj.rand.nextFloat() < 0.5F) {
					PlayerUtils.sendTranslatedChat(player, "chat.zss.trade.generic.sell.0");
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
	@SideOnly(Side.CLIENT)
	public IIcon getIconFromDamage(int type) {
		return iconArray[getType(type).ordinal() / 2];
	}

	@Override
	public String getUnlocalizedName(ItemStack stack) {
		return getUnlocalizedName() + "." + stack.getItemDamage();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(Item item, CreativeTabs tab, List list) {
		for (int i = 0; i < HookshotType.values().length; ++i) {
			list.add(new ItemStack(item, 1, i));
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister register) {
		iconArray = new IIcon[HookshotType.values().length / 2];
		for (int i = 0; i < HookshotType.values().length; ++i) {
			iconArray[i / 2] = register.registerIcon(ModInfo.ID + ":" + shotNames[getType(i).ordinal() / 2].toLowerCase());
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack,	EntityPlayer player, List list, boolean par4) {
		list.add(EnumChatFormatting.ITALIC + StatCollector.translateToLocal("tooltip.zss.hookshot.desc.0"));
		switch(getType(stack.getItemDamage())) {
		case WOOD_SHOT:
		case WOOD_SHOT_EXT:
			list.add(EnumChatFormatting.ITALIC + StatCollector.translateToLocal("tooltip.zss.hookshot.desc.10"));
			list.add(EnumChatFormatting.ITALIC + StatCollector.translateToLocal("tooltip.zss.hookshot.desc.11"));
			break;
		case CLAW_SHOT:
		case CLAW_SHOT_EXT:
			list.add(EnumChatFormatting.ITALIC + StatCollector.translateToLocal("tooltip.zss.hookshot.desc.20"));
			list.add(EnumChatFormatting.ITALIC + StatCollector.translateToLocal("tooltip.zss.hookshot.desc.21"));
			break;
		case MULTI_SHOT:
			list.add(EnumChatFormatting.ITALIC + StatCollector.translateToLocal("tooltip.zss.hookshot.desc.30"));
			list.add(EnumChatFormatting.ITALIC + StatCollector.translateToLocal("tooltip.zss.hookshot.desc.31"));
			break;
		case MULTI_SHOT_EXT:
			list.add(EnumChatFormatting.ITALIC + StatCollector.translateToLocal("tooltip.zss.hookshot.desc.32"));
			list.add(EnumChatFormatting.ITALIC + StatCollector.translateToLocal("tooltip.zss.hookshot.desc.31"));
			break;
		}
	}
}
