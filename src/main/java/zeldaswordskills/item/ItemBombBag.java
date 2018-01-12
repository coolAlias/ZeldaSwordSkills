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
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import net.minecraft.world.World;
import zeldaswordskills.api.entity.BombType;
import zeldaswordskills.api.entity.EnumVillager;
import zeldaswordskills.api.item.IUnenchantable;
import zeldaswordskills.creativetab.ZSSCreativeTabs;
import zeldaswordskills.entity.projectile.EntityBomb;
import zeldaswordskills.ref.ModInfo;
import zeldaswordskills.util.MerchantRecipeHelper;
import zeldaswordskills.util.PlayerUtils;
import zeldaswordskills.util.WorldUtils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * 
 * A bag for holding bombs. The only means of obtaining this special bag is by finding it in
 * the world, or by purchasing one from a Priest for a hefty fee.
 * 
 * While held and so long as the bag is not full, any bombs in the player's inventory will be
 * stored automatically. Right-clicking will remove a bomb from the bag and place it in the
 * player's hands; 'b' can be pressed at any time to grab a bomb from the bag, even if it is
 * not currently being held.
 * 
 * If right-clicked while sneaking and there is another bomb bag with initial capacity
 * in the inventory that is either empty or contains the same type of bombs, the two
 * bags will combine resulting in a bag with increased carrying capacity.
 * 
 * Left-clicking on a ticking bomb with the bag will turn the bomb into an item that can be
 * picked up. Careful not to get blown up while trying!
 * 
 * NOTE: If in creative mode with full inventory and the player right-clicks while holding the
 * bag, it will simply be removed rather than dropping to the ground. This is vanilla behavior.
 * 
 */
public class ItemBombBag extends Item implements IUnenchantable
{
	private static final int BASE_CAPACITY = 10, MAX_CAPACITY = 50;

	@SideOnly(Side.CLIENT)
	private IIcon[] ones;

	@SideOnly(Side.CLIENT)
	private IIcon[] tens;

	public ItemBombBag() {
		super();
		setMaxDamage(0);
		setMaxStackSize(1);
		setCreativeTab(ZSSCreativeTabs.tabTools);
	}

	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
		ItemStack bomb = this.removeBomb(player, stack);
		if (bomb != null) {
			if (!player.inventory.addItemStackToInventory(stack)) {
				player.dropPlayerItemWithRandomChoice(stack, false);
			}
			return bomb;
		}
		return stack;
	}

	@Override
	public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity entity) {
		if (entity instanceof EntityBomb) {
			return ((EntityBomb) entity).disarm(entity.worldObj);
		} else if (entity instanceof EntityVillager && !player.worldObj.isRemote) {
			EntityVillager villager = (EntityVillager) entity;
			MerchantRecipeList trades = villager.getRecipes(player);
			if (EnumVillager.LIBRARIAN.is(villager) || trades == null) {
				PlayerUtils.sendTranslatedChat(player, "chat.zss.trade.generic.sorry.0");
			} else {
				MerchantRecipe trade = new MerchantRecipe(stack.copy(), new ItemStack(Items.emerald, 16));
				if (player.worldObj.rand.nextFloat() < 0.2F && MerchantRecipeHelper.addToListWithCheck(trades, trade)) {
					PlayerUtils.sendTranslatedChat(player, "chat.zss.trade.generic.sell.1");
				} else {
					PlayerUtils.sendTranslatedChat(player, "chat.zss.trade.generic.sorry.1");
				}
			}
		}
		return true;
	}

	/**
	 * While held, the bomb bag will scan the player's inventory for bombs and attempt to store them
	 * @param slot inventory slot at which the item resides
	 * @param isHeld true if the item is currently held
	 */
	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean isHeld) {
		if (!world.isRemote && isHeld && getBombsHeld(stack) < getCapacity(stack) && entity instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer) entity;
			for (int i = 0; i < player.inventory.getSizeInventory(); ++i) {
				ItemStack invStack = player.inventory.getStackInSlot(i);
				if (invStack != null && areMatchingTypes(stack, invStack, true)) {
					if (addBombs(stack, invStack) < 1) {
						player.inventory.setInventorySlotContents(i, null);
						if (getBombsHeld(stack) == getCapacity(stack)) {
							return;
						}
					}
				}
			}
		}
	}

	@Override
	public IIcon getIcon(ItemStack stack, int pass) {
		int bombsHeld = getBombsHeld(stack);
		switch(pass) {
		case 0: return itemIcon;
		case 1: return ones[bombsHeld % 10];
		case 2: return tens[(bombsHeld / 10) % 10];
		default: return itemIcon;
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister register) {
		itemIcon = register.registerIcon(ModInfo.ID + ":" + getUnlocalizedName().substring(9));
		ones = new IIcon[10];
		tens = new IIcon[10];
		for (int i = 0; i < 10; ++i) {
			ones[i] = register.registerIcon(ModInfo.ID + ":digits/" + (i == 0 ? "" : "00") + i);
			tens[i] = register.registerIcon(ModInfo.ID + ":digits/0" + i + (i == 0 ? "" : "0"));
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean isHeld) {
		list.add(StatCollector.translateToLocal("tooltip.zss.bombbag.desc.0"));
		list.add(StatCollector.translateToLocal("tooltip.zss.bombbag.desc.1"));
		list.add(StatCollector.translateToLocal("tooltip.zss.bombbag.desc.2"));
		int held = getBombsHeld(stack);
		int i = getBagBombType(stack);
		BombType type = (held > 0 && i > 0) ? BombType.values()[i % BombType.values().length] : BombType.BOMB_STANDARD;
		String bombName = StatCollector.translateToLocal("item.zss.bomb." + type.unlocalizedName + ".name");
		list.add(EnumChatFormatting.BOLD + StatCollector.translateToLocalFormatted("tooltip.zss.bombbag.desc.bombs", bombName, held, getCapacity(stack)));
	}

	/**
	 * Attempts to add an amount of bombs to the bag, returning any that wouldn't fit
	 * @param amount can be either positive or negative
	 * @return the number of bombs that could not be added / removed, if any
	 */
	public int addBombs(ItemStack stack, int amount) {
		int bombs = getBombsHeld(stack) + amount;
		if (amount < 0) {
			stack.getTagCompound().setInteger("bombs", Math.max(0, bombs));
			if (bombs < 1) {
				setBagBombType(stack, -1);
			}
			return -Math.min(0, bombs);
		}
		int capacity = getCapacity(stack);
		stack.getTagCompound().setInteger("bombs", Math.min(bombs, capacity));
		return -Math.min(0, capacity - bombs);
	}

	/**
	 * ItemStack sensitive version for setting bag's type when adding bombs
	 * @return the number of bombs that wouldn't fit, if any (usually returns a negative value)
	 */
	public int addBombs(ItemStack bag, ItemStack bombs) {
		if (ItemBomb.getType(bombs) == BombType.BOMB_FLOWER) {
			return bombs.stackSize; // can't put bomb flowers in bomb bags
		} else if (areMatchingTypes(bag, bombs, true)) {
			int remaining = addBombs(bag, bombs.stackSize);
			setBagBombType(bag, ItemBomb.getType(bombs).ordinal());
			return remaining;
		} else {
			return bombs.stackSize;
		}
	}

	/**
	 * Returns the removed bomb ItemStack or null if no bombs were available
	 * @param player If the player is in Creative mode, a bomb is always returned without removing any
	 */
	public ItemStack removeBomb(EntityPlayer player, ItemStack stack) {
		int type = this.getBagBombType(stack);
		if (player.capabilities.isCreativeMode || this.addBombs(stack, -1) == 0) {
			return new ItemStack(ZSSItems.bomb, 1, type);
		}
		return null;
	}

	/**
	 * Empties the entire contents of the bomb bag into the player's inventory
	 * or onto the ground if there is no more room
	 */
	public void emptyBag(ItemStack stack, EntityPlayer player) {
		int n = getBombsHeld(stack);
		int type = getBagBombType(stack);
		if (type < 0 || n < 1) { return; }
		ItemStack newBag = new ItemStack(ZSSItems.bombBag);
		setCapacity(newBag, getCapacity(stack));
		if (player.inventory.addItemStackToInventory(newBag)) {
			player.setCurrentItemOrArmor(0, null);
			while (n-- > 0) {
				ItemStack bomb = new ItemStack(ZSSItems.bomb, 1, type);
				if (!player.inventory.addItemStackToInventory(bomb)) {
					WorldUtils.spawnItemWithRandom(player.worldObj, bomb, player.posX, player.posY, player.posZ);
				}
			}
		}
	}

	/**
	 * Returns max storage capacity for this bag
	 */
	public int getCapacity(ItemStack stack) {
		return getCapacity(stack, false);
	}

	/**
	 * Returns either the true NBT capacity for this bag, or the adjusted max capacity
	 */
	public int getCapacity(ItemStack stack, boolean trueCapacity) {
		int capacity = (stack.hasTagCompound() ? Math.max(BASE_CAPACITY, stack.getTagCompound().getInteger("capacity")) : BASE_CAPACITY);
		int type = getBagBombType(stack);
		return (trueCapacity || type == -1 || type == BombType.BOMB_STANDARD.ordinal()) ? capacity : capacity / 2;
	}

	/**
	 * Set's the stacks capacity to 'size' or MAX_CAPACITY, whichever is smaller
	 */
	public void setCapacity(ItemStack stack, int size) {
		verifyNBT(stack);
		stack.getTagCompound().setInteger("capacity", Math.min(size, MAX_CAPACITY));
	}

	/**
	 * Returns number of bombs held in this bag
	 */
	public int getBombsHeld(ItemStack stack) {
		verifyNBT(stack); // fixes bags from creative tab not having nbt tag
		// fix for reports of ArrayIndexOutOfBoundsException from bombs held < 0
		int bombsHeld = (stack.hasTagCompound() ? stack.getTagCompound().getInteger("bombs") : 0);
		if (bombsHeld < 0) {
			stack.getTagCompound().setInteger("bombs", 0);
		}
		return (bombsHeld < 0 ? 0 : bombsHeld);
	}

	/**
	 * Returns the ordinal value of the type of bomb held, or -1 if no current type
	 */
	public int getBagBombType(ItemStack stack) {
		return (stack.hasTagCompound() ? stack.getTagCompound().getInteger("type") : -1);
	}

	/**
	 * Sets the bags current type
	 */
	public void setBagBombType(ItemStack bag, int type) {
		verifyNBT(bag);
		bag.getTagCompound().setInteger("type", type);
	}

	/**
	 * Returns true if the stack is a bomb or a bomb bag and its type matches the
	 * type currently stored in the bag, or true if no bombs are currently stored
	 * @param isBomb true if searching for a bomb and not a bomb bag
	 */
	public boolean areMatchingTypes(ItemStack bag, ItemStack stack, boolean isBomb) {
		int type = getBagBombType(bag);
		if (isBomb && stack.getItem() instanceof ItemBomb) {
			return getBombsHeld(bag) == 0 || type == -1 || type == ItemBomb.getType(stack).ordinal();
		} else if (!isBomb && stack.getItem() instanceof ItemBombBag) {
			int type2 = getBagBombType(stack);
			return getBombsHeld(bag) == 0 || type == -1 || getBombsHeld(stack) == 0 || type == type2 || type2 == -1;
		} else {
			return false;
		}
	}

	/**
	 * Returns true if the two stacks can be combined into one bomb bag with increased capacity
	 */
	public boolean canCombine(ItemStack bag, ItemStack stack) {
		if (stack == null || stack == bag || !areMatchingTypes(bag, stack, false)) {
			return false;
		}
		return (getCapacity(bag, true) + getCapacity(stack, true)) <= MAX_CAPACITY;
	}

	/**
	 * Ensures stack has correctly formatted NBT tag; if not, one is created
	 */
	private void verifyNBT(ItemStack stack) {
		if (!stack.hasTagCompound()) {
			stack.setTagCompound(new NBTTagCompound());
			stack.getTagCompound().setInteger("bombs", 0);
			stack.getTagCompound().setInteger("type", -1);
			stack.getTagCompound().setInteger("capacity", BASE_CAPACITY);
		}
	}
}
