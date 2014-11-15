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

import java.util.List;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import zeldaswordskills.api.item.IUnenchantable;
import zeldaswordskills.creativetab.ZSSCreativeTabs;
import zeldaswordskills.ref.ModInfo;
import zeldaswordskills.util.MerchantRecipeHelper;
import zeldaswordskills.util.PlayerUtils;
import zeldaswordskills.util.TargetUtils;
import zeldaswordskills.world.TeleporterNoPortal;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * 
 * Link's Magic Mirror
 * 
 * Whenever you're lost, gaze into the mirror to find your way! Teleports Link back to the
 * world of light when in the dark (the Nether), as well as provides egress from dungeons.
 *
 */
public class ItemMagicMirror extends Item implements IUnenchantable
{
	@SideOnly(Side.CLIENT)
	private IIcon[] iconArray;

	public ItemMagicMirror() {
		super();
		setFull3D();
		setMaxDamage(16);
		setMaxStackSize(1);
		setCreativeTab(ZSSCreativeTabs.tabTools);
	}

	/**
	 * Returns time required before mirror's effect occurs
	 */
	@Override
	public int getMaxItemUseDuration(ItemStack stack) {
		return 140;
	}

	@Override
	public EnumAction getItemUseAction(ItemStack stack) {
		return EnumAction.block;
	}

	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
		player.setItemInUse(stack, getMaxItemUseDuration(stack));
		return stack;
	}

	@Override
	public void onPlayerStoppedUsing(ItemStack stack, World world, EntityPlayer player, int ticksRemaining) {
		if (!world.isRemote && ticksRemaining < (getMaxItemUseDuration(stack) / 2)) {
			switch(player.dimension) {
			case -1:
				((EntityPlayerMP) player).mcServer.getConfigurationManager().transferPlayerToDimension((EntityPlayerMP) player, 0, new TeleporterNoPortal((WorldServer) world));
				TeleporterNoPortal.adjustPosY(player);
				break;
			case 0:
				double[] coordinates = getLastPosition(stack);
				if (coordinates != null && !TargetUtils.canEntitySeeSky(world, player)) {
					player.setPositionAndUpdate(coordinates[0], coordinates[1], coordinates[2]);
				}
				break;
			default: break;
			}

			stack.damageItem(1, player);
			if (stack.stackSize == 0 || stack.getItemDamage() == stack.getMaxDamage()) {
				player.destroyCurrentEquippedItem();
			}
		}
	}

	@Override
	public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity entity) {
		if (!player.worldObj.isRemote && entity.getClass().isAssignableFrom(EntityVillager.class)) {
			EntityVillager villager = (EntityVillager) entity;
			MerchantRecipeList trades = villager.getRecipes(player);
			if (villager.getProfession() == 1 && trades != null) {
				MerchantRecipe trade = new MerchantRecipe(stack.copy(), new ItemStack(Items.emerald, 8));
				if (player.worldObj.rand.nextFloat() < 0.2F && MerchantRecipeHelper.addToListWithCheck(trades, trade)) {
					PlayerUtils.sendChat(player, StatCollector.translateToLocal("chat.zss.trade.generic.sell.1"));
				} else {
					PlayerUtils.sendChat(player, StatCollector.translateToLocal("chat.zss.trade.generic.sorry.1"));
				}
			} else {
				PlayerUtils.sendChat(player, StatCollector.translateToLocal("chat.zss.trade.generic.sorry.0"));
			}
		}
		return true;
	}

	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean isHeld) {
		if (!world.isRemote && entity.dimension == 0 && world.getTotalWorldTime() % 10 == 0) {
			if (TargetUtils.canEntitySeeSky(world, entity)) {
				setLastPosition(stack, entity);
			}
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean hasEffect(ItemStack stack, int pass) {
		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(ItemStack stack, int renderPass, EntityPlayer player, ItemStack usingItem, int useRemaining) {
		if (usingItem == null) { return iconArray[0]; }
		int ticksInUse = stack.getMaxItemUseDuration() - useRemaining;

		if (ticksInUse > getMaxItemUseDuration(stack) / 2) {
			return iconArray[3];
		} else if (ticksInUse > getMaxItemUseDuration(stack) / 3) {
			return iconArray[2];
		} else if (ticksInUse > getMaxItemUseDuration(stack) / 7) {
			return iconArray[1];
		} else {
			return iconArray[0];
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister register) {
		itemIcon = register.registerIcon(ModInfo.ID + ":" + getUnlocalizedName().substring(9));
		iconArray = new IIcon[4];
		for (int i = 0; i < iconArray.length; ++i) {
			iconArray[i] = register.registerIcon(ModInfo.ID + ":" + getUnlocalizedName().substring(9) + (i > 0 ? i : ""));
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack itemstack, EntityPlayer player, List list, boolean isHeld) {
		list.add(EnumChatFormatting.ITALIC + StatCollector.translateToLocal("tooltip.zss.magicmirror.desc.0"));
		list.add(EnumChatFormatting.ITALIC + StatCollector.translateToLocal("tooltip.zss.magicmirror.desc.1"));
	}

	/**
	 * Returns last recorded above-ground position for player, or null if none available
	 */
	protected double[] getLastPosition(ItemStack stack) {
		double[] coordinates = null;
		if (stack.hasTagCompound() && stack.getTagCompound().hasKey("mmLastPosX")) {
			coordinates = new double[3];
			coordinates[0] = stack.getTagCompound().getDouble("mmLastPosX");
			coordinates[1] = stack.getTagCompound().getDouble("mmLastPosY");
			coordinates[2] = stack.getTagCompound().getDouble("mmLastPosZ");
		}
		return coordinates;
	}

	/**
	 * Records entity's current position in the stack's NBT tag, creating the tag if necessary
	 */
	protected void setLastPosition(ItemStack stack, Entity entity) {
		if (!stack.hasTagCompound()) {
			stack.setTagCompound(new NBTTagCompound());
		}
		stack.getTagCompound().setDouble("mmLastPosX", entity.posX);
		stack.getTagCompound().setDouble("mmLastPosY", entity.posY);
		stack.getTagCompound().setDouble("mmLastPosZ", entity.posZ);
	}
}
