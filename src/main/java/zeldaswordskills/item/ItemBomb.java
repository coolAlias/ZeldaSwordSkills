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

import java.util.Arrays;
import java.util.List;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.INpc;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import zeldaswordskills.api.entity.BombType;
import zeldaswordskills.api.entity.CustomExplosion;
import zeldaswordskills.api.item.IHandlePickup;
import zeldaswordskills.api.item.IHandleToss;
import zeldaswordskills.api.item.INbtComparable;
import zeldaswordskills.api.item.IUnenchantable;
import zeldaswordskills.creativetab.ZSSCreativeTabs;
import zeldaswordskills.entity.npc.EntityNpcBarnes;
import zeldaswordskills.entity.projectile.EntityBomb;
import zeldaswordskills.network.PacketDispatcher;
import zeldaswordskills.network.server.BombTickPacket;
import zeldaswordskills.ref.Config;
import zeldaswordskills.ref.ModInfo;
import zeldaswordskills.ref.Sounds;
import zeldaswordskills.util.PlayerUtils;

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
public class ItemBomb extends Item implements IHandlePickup, IHandleToss, INbtComparable, IRupeeValue.IMetaRupeeValue, IUnenchantable
{
	@SideOnly(Side.CLIENT)
	private IIcon[] iconArray;

	/** Blast radius for bombs (Creeper is 3.0F) */
	public static final float RADIUS = 3.0F;

	public ItemBomb() {
		super();
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
			int fuseTime = (Config.getBombFuseTime() > 0 ? Config.getBombFuseTime() : 56);
			// subtract any ticks which passed while held
			fuseTime -= (stack.hasTagCompound() ? stack.getTagCompound().getInteger("time") : 0);
			world.spawnEntityInWorld(new EntityBomb(world, player).setType(getType(stack)).addTime(fuseTime));
		}
		player.destroyCurrentEquippedItem();
		return stack;
	}

	@Override
	public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity entity) {
		if (entity instanceof EntityNpcBarnes) {
			if (!player.worldObj.isRemote) {
				if (((EntityNpcBarnes) entity).addBombBagTrade()) {
					PlayerUtils.sendTranslatedChat(player, "chat.zss.trade.bomb.add");
				} else {
					PlayerUtils.sendTranslatedChat(player, "chat.zss.trade.bomb.careful");
				}
			}
			return true;
		} else if (entity instanceof INpc) {
			if (!player.worldObj.isRemote) {
				PlayerUtils.sendTranslatedChat(player, "chat.zss.trade.bomb.careful");
			}
			return true;
		}
		return super.onLeftClickEntity(stack, player, entity);
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
		stack = this.setDefaultNBT(stack);
		if (isHeld || getType(stack) == BombType.BOMB_FLOWER) {
			if (entity instanceof EntityPlayer) {
				if (world.isRemote && Minecraft.getMinecraft().currentScreen == null) {
					PacketDispatcher.sendToServer(new BombTickPacket(slot));
				}
			} else {
				tickBomb(stack, world, entity, slot);
			}
		} else {
			stack.getTagCompound().setInteger("time", 0);
			stack.getTagCompound().setBoolean("inWater", false);
		}
	}

	/**
	 * Compares the two stacks' tags ignoring the "time" and "inWater" entries
	 */
	@Override
	public boolean areTagsEquivalent(ItemStack a, ItemStack b) {
		NBTTagCompound x = a.getTagCompound();
		NBTTagCompound y = b.getTagCompound();
		if (x == null || y == null) {
			return true;
		}
		x = removeDefaultTags((NBTTagCompound) x.copy());
		y = removeDefaultTags((NBTTagCompound) y.copy());
		return x.equals(y);
	}

	private NBTTagCompound removeDefaultTags(NBTTagCompound tag) {
		tag.removeTag("time");
		tag.removeTag("inWater");
		return tag;
	}

	/**
	 * Adds the required NBT tags to the stack's NBT, setting a new tag compound if necessary 
	 */
	public ItemStack setDefaultNBT(ItemStack stack) {
		if (!stack.hasTagCompound()) {
			stack.setTagCompound(new NBTTagCompound());
		}
		NBTTagCompound tag = stack.getTagCompound();
		if (!tag.hasKey("time")) {
			tag.setInteger("time", 0);
		}
		if (!tag.hasKey("inWater")) {
			tag.setBoolean("inWater", false);
		}
		return stack;
	}

	@Override
	public int getDefaultRupeeValue(ItemStack stack) {
		return getType(stack).defaultRupeeValue;
	}

	@Override
	public List<ItemStack> getRupeeValueSubItems() {
		return Arrays.asList(new ItemStack[]{
				new ItemStack(this, 1, BombType.BOMB_STANDARD.ordinal()),
				new ItemStack(this, 1, BombType.BOMB_WATER.ordinal()),
				new ItemStack(this, 1, BombType.BOMB_FIRE.ordinal())
				// BOMB_FLOWER not available as a trade
		});
	}

	/**
	 * Increments bomb's timer and causes explosion if time is out; stack must be held by entity
	 */
	public void tickBomb(ItemStack stack, World world, Entity entity, int slot) {
		if (!stack.hasTagCompound()) {
			stack.setTagCompound(new NBTTagCompound());
			stack.getTagCompound().setInteger("time", 0);
			stack.getTagCompound().setBoolean("inWater", false);
		}
		BombType type = getType(stack);
		if (type != BombType.BOMB_WATER && world.getBlock((int) entity.posX, (int) entity.posY + 1, (int) entity.posZ).getMaterial() == Material.water) {
			stack.getTagCompound().setInteger("time", 0);
			stack.getTagCompound().setBoolean("inWater", true);
		} else if (stack.getTagCompound().getBoolean("inWater")) {
			stack.getTagCompound().setBoolean("inWater", false);
		}
		if (canTick(world, type, stack.getTagCompound().getBoolean("inWater"))) {
			int time = stack.getTagCompound().getInteger("time");
			if (time % 20 == 0) {
				world.playSoundAtEntity(entity, Sounds.BOMB_FUSE, 1.0F, 2.0F + entity.worldObj.rand.nextFloat() * 0.4F);
			}
			boolean flag = world.provider.isHellWorld && (type == BombType.BOMB_STANDARD || type == BombType.BOMB_FLOWER);
			stack.getTagCompound().setInteger("time", flag ? Config.getBombFuseTime() : ++time);
			int fuse = Config.getBombFuseTime();
			if (fuse == 0 && type == BombType.BOMB_FLOWER) {
				fuse = 56;
			}
			if (time == fuse && !world.isRemote) {
				EntityBomb bomb = null;
				if (entity instanceof EntityPlayer) {
					((EntityPlayer) entity).inventory.setInventorySlotContents(slot, null);
					bomb = new EntityBomb(world, (EntityPlayer) entity).setType(type);
				} else {
					entity.setCurrentItemOrArmor(slot, null);
					bomb = new EntityBomb(world).setType(type);
				}
				CustomExplosion.createExplosion(bomb, world, entity.posX, entity.posY, entity.posZ, getRadius(type), 0.0F, true);
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
	public IIcon getIconFromDamage(int type) {
		return iconArray[getType(type).ordinal()];
	}

	@Override
	public String getUnlocalizedName(ItemStack stack) {
		return getUnlocalizedName() + "." + getType(stack).unlocalizedName;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(Item item, CreativeTabs tab, List list) {
		for (int i = 0; i < BombType.values().length; ++i) {
			list.add(new ItemStack(item, 1, i));
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister register) {
		iconArray = new IIcon[BombType.values().length];
		String s = ModInfo.ID + ":" + getUnlocalizedName().substring(9) + "_";
		for (int i = 0; i < BombType.values().length; ++i) {
			iconArray[i] = register.registerIcon(s + getType(i).unlocalizedName);
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack itemstack, EntityPlayer player, List list, boolean isHeld) {
		list.add(StatCollector.translateToLocal("tooltip.zss.bomb.desc.0"));
		list.add(StatCollector.translateToLocal("tooltip.zss.bomb.desc.1"));
	}
}
