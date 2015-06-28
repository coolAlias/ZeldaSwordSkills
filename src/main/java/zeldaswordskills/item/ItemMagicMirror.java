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

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.renderer.ItemModelMesher;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import zeldaswordskills.api.item.IUnenchantable;
import zeldaswordskills.creativetab.ZSSCreativeTabs;
import zeldaswordskills.ref.ModInfo;
import zeldaswordskills.util.MerchantRecipeHelper;
import zeldaswordskills.util.PlayerUtils;
import zeldaswordskills.util.TargetUtils;
import zeldaswordskills.world.TeleporterNoPortal;

/**
 * 
 * Link's Magic Mirror
 * 
 * Whenever you're lost, gaze into the mirror to find your way! Teleports Link back to the
 * world of light when in the dark (the Nether), as well as provides egress from dungeons.
 *
 */
public class ItemMagicMirror extends BaseModItem implements IUnenchantable
{
	@SideOnly(Side.CLIENT)
	private static List<ModelResourceLocation> models;

	/** Ticks required to be in use before effect will occur */
	private static final int USE_TIME = 140;

	public ItemMagicMirror() {
		super();
		setFull3D();
		setMaxDamage(16);
		setMaxStackSize(1);
		setCreativeTab(ZSSCreativeTabs.tabTools);
	}

	@Override
	public int getMaxItemUseDuration(ItemStack stack) {
		return USE_TIME;
	}

	@Override
	public EnumAction getItemUseAction(ItemStack stack) {
		return EnumAction.BLOCK;
	}

	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
		player.setItemInUse(stack, getMaxItemUseDuration(stack));
		return stack;
	}

	@Override
	public ItemStack onItemUseFinish(ItemStack stack, World world, EntityPlayer player) {
		if (!world.isRemote) {
			switch(player.dimension) {
			case -1:
				((EntityPlayerMP) player).mcServer.getConfigurationManager().transferPlayerToDimension((EntityPlayerMP) player, 0, new TeleporterNoPortal((WorldServer) world));
				TeleporterNoPortal.adjustPosY(player);
				break;
			case 0:
				BlockPos pos = getLastPosition(stack);
				if (pos != null && !TargetUtils.canEntitySeeSky(world, player)) {
					player.setPositionAndUpdate(pos.getX() + 0.5D, pos.getY() + 0.15D, pos.getZ() + 0.5D);
				}
				break;
			default: break;
			}
			stack.damageItem(1, player);
			/*
			// TODO test if stack is removed
			stack.damageItem(1, player);
			if (stack.stackSize == 0 || stack.getItemDamage() == stack.getMaxDamage()) {
				player.destroyCurrentEquippedItem();
			}
			 */
		}
		return stack;
	}

	@Override
	public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity entity) {
		if (!player.worldObj.isRemote && entity.getClass().isAssignableFrom(EntityVillager.class)) {
			EntityVillager villager = (EntityVillager) entity;
			MerchantRecipeList trades = villager.getRecipes(player);
			if (villager.getProfession() == 1 && trades != null) {
				MerchantRecipe trade = new MerchantRecipe(stack.copy(), new ItemStack(Items.emerald, 8));
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
	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
		return !ItemStack.areItemsEqual(oldStack, newStack);
	}

	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean isHeld) {
		if (!world.isRemote && world.provider.isSurfaceWorld() && world.getTotalWorldTime() % 10 == 0) {
			boolean flag = (entity instanceof EntityPlayer && !((EntityPlayer) entity).isUsingItem());
			if (flag && TargetUtils.canEntitySeeSky(world, entity)) {
				setLastPosition(stack, new BlockPos(entity));
			}
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean hasEffect(ItemStack stack) {
		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public ModelResourceLocation getModel(ItemStack stack, EntityPlayer player, int ticksRemaining) {
		if (!player.isUsingItem()) {
			return models.get(0);
		}
		int i = (ticksRemaining < 30 ? 3 : ticksRemaining < 70 ? 2 : ticksRemaining < 110 ? 1 : 0);
		return models.get(i);
	}

	@Override
	public String[] getVariants() {
		String name = getUnlocalizedName();
		name = ModInfo.ID + ":" + name.substring(name.lastIndexOf(".") + 1);
		String[] variants = new String[4];
		for (int i = 0; i < variants.length; ++i) {
			variants[i] = name + "_" + i;
		}
		return variants;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerRenderers(ItemModelMesher mesher) {
		String name = getUnlocalizedName();
		name = ModInfo.ID + ":" + name.substring(name.lastIndexOf(".") + 1);
		models = new ArrayList<ModelResourceLocation>();
		for (int i = 0; i < 4; ++i) {
			models.add(new ModelResourceLocation(name + "_" + i, "inventory"));
		}
		// Register the first model as the base resource
		mesher.register(this, 0, models.get(0));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack itemstack, EntityPlayer player, List list, boolean isHeld) {
		list.add(EnumChatFormatting.ITALIC + StatCollector.translateToLocal("tooltip.zss.magic_mirror.desc.0"));
		list.add(EnumChatFormatting.ITALIC + StatCollector.translateToLocal("tooltip.zss.magic_mirror.desc.1"));
	}

	/**
	 * Returns last recorded above-ground position for player, or null if none available
	 */
	protected BlockPos getLastPosition(ItemStack stack) {
		if (stack.hasTagCompound() && stack.getTagCompound().hasKey("mmLastPos")) {
			return BlockPos.fromLong(stack.getTagCompound().getLong("mmLastPos"));
		}
		return null;
	}

	/**
	 * Records entity's current position in the stack's NBT tag, creating the tag if necessary
	 */
	protected void setLastPosition(ItemStack stack, BlockPos pos) {
		if (!stack.hasTagCompound()) {
			stack.setTagCompound(new NBTTagCompound());
		}
		stack.getTagCompound().setLong("mmLastPos", pos.toLong());
	}
}
