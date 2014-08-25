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

import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.Icon;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import net.minecraft.world.World;
import zeldaswordskills.api.entity.BombType;
import zeldaswordskills.api.entity.CustomExplosion;
import zeldaswordskills.api.item.ArmorIndex;
import zeldaswordskills.api.item.IHandlePickup;
import zeldaswordskills.api.item.IHandleToss;
import zeldaswordskills.creativetab.ZSSCreativeTabs;
import zeldaswordskills.entity.projectile.EntityBomb;
import zeldaswordskills.lib.Config;
import zeldaswordskills.lib.ModInfo;
import zeldaswordskills.lib.Sounds;
import zeldaswordskills.network.BombTickPacket;
import zeldaswordskills.util.MerchantRecipeHelper;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * 
 * A bomb that works just like in Zelda games. Throw it quick or it will blow up in your face!
 * 
 * They can be found in dungeons as well as purchased from blacksmiths (who make them for relatively
 * cheap) or farmers (who sell them at a higher price). Occasionally dropped from Creepers.
 * 
 * Incrementing the itemstack's item damage each tick resulted in strange rendering behavior;
 * storing the time in NBT instead works nicely.
 *
 */
public class ItemBomb extends Item implements IHandlePickup, IHandleToss
{
	/*========================== RENDER RESOURCE LOCATIONS =========================*/
	/** Standard bomb textures */
	public static final ResourceLocation bombBase = new ResourceLocation(ModInfo.ID, "textures/entity/bomb.png");
	public static final ResourceLocation bombFlash = new ResourceLocation(ModInfo.ID, "textures/entity/bombflash.png");
	/** Water bomb textures */
	public static final ResourceLocation waterBase = new ResourceLocation(ModInfo.ID, "textures/entity/bombwater.png");
	public static final ResourceLocation waterFlash = new ResourceLocation(ModInfo.ID, "textures/entity/bombwaterflash.png");
	/** Fire bomb textures */
	public static final ResourceLocation fireBase = new ResourceLocation(ModInfo.ID, "textures/entity/bombfire.png");
	public static final ResourceLocation fireFlash = new ResourceLocation(ModInfo.ID, "textures/entity/bombfireflash.png");
	
	public static final String[] bombNames = {"Bomb","Water Bomb","Fire Bomb"};
	
	@SideOnly(Side.CLIENT)
	private Icon[] iconArray;
	
	/** Blast radius for bombs (Creeper is 3.0F) */
	public static final float RADIUS = 3.0F;
	
	public ItemBomb(int par1) {
		super(par1);
		setMaxDamage(0);
		setMaxStackSize(1);
		setHasSubtypes(true);
		setCreativeTab(ZSSCreativeTabs.tabTools);
		setFull3D();
	}
	
	/** Shortcut method; returns this bomb's enum Type from stack damage value */
	public static BombType getType(ItemStack stack) {
		return getType(stack.getItemDamage());
	}
	
	/**
	 * Returns this bomb's enum Type from stack damage value
	 */
	public static BombType getType(int damage) {
		return (damage < BombType.values().length ? BombType.values()[damage] : BombType.BOMB_STANDARD);
	}
	
	/**
	 * Return blast radius for this bomb type
	 */
	public static float getRadius(BombType type) {
		return (type == BombType.BOMB_WATER ? (RADIUS * 0.75F) : RADIUS);
	}
	
	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
		if (!world.isRemote) {
			int extraTime = Config.getBombFuseTime();
			if (extraTime == 0) { extraTime = 56; }
			world.spawnEntityInWorld(new EntityBomb(world, player).setType(getType(stack)).addTime(extraTime - stack.getTagCompound().getInteger("time")));
		}
		player.destroyCurrentEquippedItem();
		return stack;
	}
	
	@Override
	public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity entity) {
		if (entity.getClass().isAssignableFrom(EntityVillager.class)) {
			if (!player.worldObj.isRemote) {
				EntityVillager villager = (EntityVillager) entity;
				MerchantRecipeList trades = villager.getRecipes(player);
				BombType bombType = getType(stack);
				ItemStack chest = player.getCurrentArmor(ArmorIndex.WORN_CHEST);
				if (villager.getProfession() == 2) {
					if (chest != null && chest.getItem() == ZSSItems.tunicZoraChest && bombType != BombType.BOMB_FIRE) {
						MerchantRecipe waterBombTrade = new MerchantRecipe(new ItemStack(ZSSItems.bomb,1,BombType.BOMB_STANDARD.ordinal()), new ItemStack(Item.emerald, 5), new ItemStack(ZSSItems.bomb,1,BombType.BOMB_WATER.ordinal()));
						MerchantRecipeHelper.addToListWithCheck(trades, waterBombTrade);
						player.addChatMessage(StatCollector.translateToLocal("chat.zss.trade.bomb.zora"));
					} else if (Config.enableTradeBombBag() && trades.size() >= Config.getFriendTradesRequired()) {
						MerchantRecipe bombBagTrade = new MerchantRecipe(new ItemStack(Item.emerald, Math.min(64, Config.getMinBombBagPrice() + player.worldObj.rand.nextInt(16))), new ItemStack(ZSSItems.bombBag));
						MerchantRecipeHelper.addToListWithCheck(trades, bombBagTrade);
						player.addChatMessage(StatCollector.translateToLocal("chat.zss.trade.bomb.initiate"));
					} else {
						player.addChatMessage(StatCollector.translateToLocal("chat.zss.trade.bomb.failure"));
					}
				} else if (villager.getProfession() == 3 && chest != null && chest.getItem() == ZSSItems.tunicGoronChest && bombType != BombType.BOMB_WATER) {
					MerchantRecipe fireBombTrade = new MerchantRecipe(new ItemStack(ZSSItems.bomb,1,BombType.BOMB_STANDARD.ordinal()), new ItemStack(Item.emerald, 10), new ItemStack(ZSSItems.bomb,1,BombType.BOMB_FIRE.ordinal()));
					MerchantRecipeHelper.addToListWithCheck(trades, fireBombTrade);
					player.addChatMessage(StatCollector.translateToLocal("chat.zss.trade.bomb.goron"));
				} else {
					player.addChatMessage(StatCollector.translateToLocal("chat.zss.trade.bomb.failure"));
				}
			}
			return true;
		} else {
			return (entity instanceof EntityVillager || super.onLeftClickEntity(stack, player, entity));
		}
	}
	
	@Override
	public boolean onPickupItem(ItemStack stack, EntityPlayer player) {
		for (ItemStack invStack : player.inventory.mainInventory) {
			if (invStack != null && invStack.getItem() instanceof ItemBombBag) {
				stack.stackSize -= (stack.stackSize - ((ItemBombBag) invStack.getItem()).addBombs(invStack, stack));
				if (stack.stackSize < 1) {
					break;
				}
			}
		}
		return true;
	}
	
	@Override
	public void onItemTossed(EntityItem item, EntityPlayer player) {
		ItemStack stack = item.getEntityItem();
		if (stack.hasTagCompound() && stack.getTagCompound().hasKey("time")) {
			stack.getTagCompound().setInteger("time", 0);
		}
	}
	
	/**
	 * @param slot inventory slot at which the item resides
	 * @param isHeld true if the item is currently held
	 */
	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean isHeld) {
		if (!stack.hasTagCompound()) {
			stack.setTagCompound(new NBTTagCompound());
			stack.getTagCompound().setInteger("time", 0);
			stack.getTagCompound().setBoolean("inWater", false);
		}
		
		if (isHeld) {
			if (entity instanceof EntityPlayer) {
				if (world.isRemote && Minecraft.getMinecraft().currentScreen == null) {
					PacketDispatcher.sendPacketToServer(new BombTickPacket().makePacket());
				}
			} else {
				tickBomb(stack, world, entity);
			}
		} else {
			stack.getTagCompound().setInteger("time", 0);
			stack.getTagCompound().setBoolean("inWater", false);
		}
	}
	
	/**
	 * Increments bomb's timer and causes explosion if time is out; stack must be held by entity
	 */
	public void tickBomb(ItemStack stack, World world, Entity entity) {
		if (!stack.hasTagCompound()) {
			stack.setTagCompound(new NBTTagCompound());
			stack.getTagCompound().setInteger("time", 0);
			stack.getTagCompound().setBoolean("inWater", false);
		}
		
		BombType type = getType(stack);
		if (type != BombType.BOMB_WATER && world.getBlockMaterial((int) entity.posX, (int) entity.posY + 1, (int) entity.posZ) == Material.water) {
			stack.getTagCompound().setInteger("time", 0);
			stack.getTagCompound().setBoolean("inWater", true);
		}
		if (canTick(world, type, stack.getTagCompound().getBoolean("inWater"))) {
			int time = stack.getTagCompound().getInteger("time");
			if (time % 20 == 0) {
				world.playSoundAtEntity(entity, Sounds.BOMB_FUSE, 1.0F, 2.0F + entity.worldObj.rand.nextFloat() * 0.4F);
			}
			stack.getTagCompound().setInteger("time", ((world.provider.dimensionId == -1 && type == BombType.BOMB_STANDARD) ? Config.getBombFuseTime() : ++time));
			if (time == Config.getBombFuseTime() && !world.isRemote) {
				entity.setCurrentItemOrArmor(0, null);
				CustomExplosion.createExplosion(world, entity.posX, entity.posY, entity.posZ, getRadius(type), type);
			}
		}
	}
	
	/**
	 * Returns true if this type of bomb can tick this update
	 */
	private boolean canTick(World world, BombType type, boolean inWater) {
		if (Config.getBombFuseTime() > 0) {
			switch(type) {
			case BOMB_WATER: return (world.provider.dimensionId != -1);
			default: return (!inWater);
			}
		} else {
			return false;
		}
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public Icon getIconFromDamage(int type) {
		return iconArray[getType(type).ordinal()];
	}

	@Override
	public String getUnlocalizedName(ItemStack stack) {
		return getUnlocalizedName() + "." + stack.getItemDamage();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(int itemID, CreativeTabs tab, List list) {
		for (int i = 0; i < BombType.values().length; ++i) {
			list.add(new ItemStack(itemID, 1, i));
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister register) {
		iconArray = new Icon[BombType.values().length];
		for (int i = 0; i < BombType.values().length; ++i) {
			iconArray[i] = register.registerIcon(ModInfo.ID + ":" + (getUnlocalizedName().substring(9) + (i + 1)));
		}
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack itemstack, EntityPlayer player, List list, boolean isHeld) {
		list.add(EnumChatFormatting.ITALIC + StatCollector.translateToLocal("tooltip.zss.bomb.desc.0"));
		list.add(EnumChatFormatting.ITALIC + StatCollector.translateToLocal("tooltip.zss.bomb.desc.1"));
	}
}
