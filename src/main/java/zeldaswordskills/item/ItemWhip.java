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
import net.minecraft.entity.INpc;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import net.minecraft.world.World;
import zeldaswordskills.api.block.IWhipBlock.WhipType;
import zeldaswordskills.api.entity.EnumVillager;
import zeldaswordskills.api.item.IFairyUpgrade;
import zeldaswordskills.block.tileentity.TileEntityDungeonCore;
import zeldaswordskills.creativetab.ZSSCreativeTabs;
import zeldaswordskills.entity.projectile.EntityWhip;
import zeldaswordskills.ref.ModInfo;
import zeldaswordskills.ref.Sounds;
import zeldaswordskills.util.MerchantRecipeHelper;
import zeldaswordskills.util.PlayerUtils;
import zeldaswordskills.util.WorldUtils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

// TODO BG2 dual-wielding compatibility
public class ItemWhip extends Item implements IFairyUpgrade
{
	@SideOnly(Side.CLIENT)
	private IIcon[] iconArray;

	public ItemWhip() {
		super();
		setFull3D();
		setMaxStackSize(1);
		setHasSubtypes(true);
		setCreativeTab(ZSSCreativeTabs.tabTools);
	}

	/** Returns this whip's enum Type from stack damage value */
	public WhipType getType(ItemStack stack) {
		return getType(stack.getItemDamage());
	}

	/** Returns this whip's enum Type from damage value */
	public WhipType getType(int damage) {
		return (damage > -1 ? WhipType.values()[damage % WhipType.values().length] : WhipType.WHIP_SHORT);
	}

	@Override
	public EnumAction getItemUseAction(ItemStack stack) {
		return EnumAction.block;
	}

	@Override
	public int getMaxItemUseDuration(ItemStack stack) {
		return 32000;
	}

	@Override
	public boolean isItemTool(ItemStack stack) {
		return true;
	}

	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
		player.setItemInUse(stack, getMaxItemUseDuration(stack));
		EntityWhip whip = new EntityWhip(player.worldObj, player);
		whip.setThrower(player);
		whip.setType(getType(stack));
		if (!player.worldObj.isRemote) {
			player.worldObj.spawnEntityInWorld(whip);
			player.worldObj.playSoundAtEntity(player, Sounds.WHIP, 0.4F, 1.0F);
		}
		return stack;
	}

	@Override
	public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity entity) {
		if (!player.worldObj.isRemote && entity.getClass() == EntityVillager.class) {
			EntityVillager villager = (EntityVillager) entity;
			MerchantRecipeList trades = villager.getRecipes(player);
			if (EnumVillager.BUTCHER.is(villager) && trades != null) {
				switch(getType(stack)) {
				case WHIP_SHORT:
					MerchantRecipe trade = new MerchantRecipe(new ItemStack(this, 1, WhipType.WHIP_SHORT.ordinal()), new ItemStack(Items.emerald, 64), new ItemStack(this, 1, WhipType.WHIP_LONG.ordinal()));
					if (MerchantRecipeHelper.addUniqueTrade(trades, trade)) {
						PlayerUtils.sendTranslatedChat(player, "chat.zss.trade.whip.upgrade.new");
					} else {
						PlayerUtils.sendTranslatedChat(player, "chat.zss.trade.whip.upgrade.old");
					}
					break;
				case WHIP_LONG:
					PlayerUtils.sendTranslatedChat(player, "chat.zss.trade.whip.long");
					break;
				case WHIP_MAGIC:
					PlayerUtils.sendTranslatedChat(player, "chat.zss.trade.whip.magic");
					break;
				}
			} else {
				PlayerUtils.sendTranslatedChat(player, "chat.zss.trade.whip.sorry");
			}
		} else if (!player.worldObj.isRemote && entity instanceof INpc) {
			PlayerUtils.sendTranslatedChat(player, "chat.zss.trade.whip.sorry");
		}
		return true;
	}

	@Override
	public void handleFairyUpgrade(EntityItem item, EntityPlayer player, TileEntityDungeonCore core) {
		if (getType(item.getEntityItem()) == WhipType.WHIP_LONG && core.consumeRupees(320)) {
			item.setDead();
			WorldUtils.spawnItemWithRandom(core.getWorldObj(), new ItemStack(ZSSItems.whip, 1, WhipType.WHIP_MAGIC.ordinal()), core.xCoord, core.yCoord + 2, core.zCoord);
			core.getWorldObj().playSoundEffect(core.xCoord + 0.5D, core.yCoord + 1, core.zCoord + 0.5D, Sounds.SECRET_MEDLEY, 1.0F, 1.0F);
			//player.triggerAchievement(ZSSAchievements.magicWhip);
		} else {
			core.getWorldObj().playSoundEffect(core.xCoord + 0.5D, core.yCoord + 1, core.zCoord + 0.5D, Sounds.FAIRY_LAUGH, 1.0F, 1.0F);
			PlayerUtils.sendTranslatedChat(player, "chat.zss.fairy.laugh.unworthy");
		}
	}

	@Override
	public boolean hasFairyUpgrade(ItemStack stack) {
		return (getType(stack) != WhipType.WHIP_MAGIC);
	}

	@Override
	public String getUnlocalizedName(ItemStack stack) {
		return getUnlocalizedName() + "." + stack.getItemDamage();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(Item item, CreativeTabs tab, List list) {
		for (int i = 0; i < WhipType.values().length; ++i) {
			list.add(new ItemStack(item, 1, i));
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIconFromDamage(int type) {
		switch(getType(type)) {
		case WHIP_MAGIC: return iconArray[1];
		default: return iconArray[0];
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister register) {
		iconArray = new IIcon[2];
		iconArray[0] = register.registerIcon(ModInfo.ID + ":whip");
		iconArray[1] = register.registerIcon(ModInfo.ID + ":whip_magic");
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack,	EntityPlayer player, List list, boolean par4) {
		list.add(StatCollector.translateToLocal("tooltip.zss.whip.desc." + getType(stack).ordinal()));
	}
}
