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

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mods.battlegear2.api.PlayerEventChild;
import mods.battlegear2.api.PlayerEventChild.OffhandAttackEvent;
import mods.battlegear2.api.quiver.IArrowContainer2;
import mods.battlegear2.api.quiver.IArrowFireHandler;
import mods.battlegear2.api.quiver.QuiverArrowRegistry;
import mods.battlegear2.api.weapons.IBattlegearWeapon;
import mods.battlegear2.enchantments.BaseEnchantment;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.Icon;
import net.minecraft.util.StatCollector;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.ArrowLooseEvent;
import net.minecraftforge.event.entity.player.ArrowNockEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import zeldaswordskills.ZSSAchievements;
import zeldaswordskills.ZSSMain;
import zeldaswordskills.api.entity.BombType;
import zeldaswordskills.api.item.ArmorIndex;
import zeldaswordskills.api.item.IFairyUpgrade;
import zeldaswordskills.api.item.IZoom;
import zeldaswordskills.block.tileentity.TileEntityDungeonCore;
import zeldaswordskills.creativetab.ZSSCreativeTabs;
import zeldaswordskills.entity.ZSSPlayerInfo;
import zeldaswordskills.entity.projectile.EntityArrowBomb;
import zeldaswordskills.entity.projectile.EntityArrowCustom;
import zeldaswordskills.entity.projectile.EntityArrowElemental;
import zeldaswordskills.entity.projectile.EntityArrowElemental.ElementType;
import zeldaswordskills.handler.BattlegearEvents;
import zeldaswordskills.lib.Config;
import zeldaswordskills.lib.ModInfo;
import zeldaswordskills.lib.Sounds;
import zeldaswordskills.util.LogHelper;
import zeldaswordskills.util.MerchantRecipeHelper;
import zeldaswordskills.util.PlayerUtils;
import zeldaswordskills.util.TargetUtils;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;

import cpw.mods.fml.common.Optional;
import cpw.mods.fml.common.Optional.Method;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * 
 * The Hero's Bow can fire arrows even when the player has none if it has the infinity
 * enchantment. It is also capable of firing different types of arrows, depending on
 * its level. Standard (level 1) can fire any bomb arrow; level 2 can shoot fire and
 * ice arrows; level 3 can shoot all arrow types
 *
 */
@Optional.Interface(iface="mods.battlegear2.api.weapons.IBattlegearWeapon", modid="battlegear2", striprefs=true)
public class ItemHeroBow extends ItemBow implements ICyclableItem, IFairyUpgrade, IZoom, IBattlegearWeapon
{
	public static enum Mode {
		/** Default Hero Bow behavior searches for the first usable arrow of any kind */
		DEFAULT("", 0),
		STANDARD("minecraft:arrow", 0),
		BOMB_STANDARD(ModInfo.ID + ":zss.arrow_bomb", 1),
		BOMB_FIRE(ModInfo.ID + ":zss.arrow_bomb_fire", 1),
		BOMB_WATER(ModInfo.ID + ":zss.arrow_bomb_water", 1),
		MAGIC_FIRE(ModInfo.ID + ":zss.arrow_fire", 2),
		MAGIC_ICE(ModInfo.ID + ":zss.arrow_ice", 2),
		MAGIC_LIGHT(ModInfo.ID + ":zss.arrow_light", 3);
		private final String arrowName;
		private Item arrowItem;
		private final int level;
		private Mode(String arrowName, int level) {
			this.arrowName = arrowName;
			this.level = level;
		}
		/**
		 * Returns the arrow item required for this mode
		 */
		public Item getArrowItem() {
			if (arrowItem == null && arrowName.length() > 0) {
				String[] parts = arrowName.split(":");
				arrowItem = (parts[0].equals("minecraft") ? Item.arrow : GameRegistry.findItem(parts[0], parts[1]));
			}
			return arrowItem;
		}
		/**
		 * Returns the next Mode by ordinal position
		 */
		public Mode next() {
			return Mode.values()[(ordinal() + 1) % Mode.values().length];
		}
		/**
		 * Returns the next mode whose level does not exceed that given
		 */
		public Mode next(int level) {
			return cycle(level, true);
		}
		/**
		 * Returns the previous Mode by ordinal position
		 */
		public Mode prev() {
			return Mode.values()[((ordinal() == 0 ? Mode.values().length : ordinal()) - 1) % Mode.values().length];
		}
		/**
		 * Returns the previous mode whose level does not exceed that given
		 */
		public Mode prev(int level) {
			return cycle(level, false);
		}
		private Mode cycle(int level, boolean next) {
			Mode mode = this;
			do {
				mode = (next ? mode.next() : mode.prev());
			} while (mode != this && mode.level > level);
			return mode;
		}
	}

	@SideOnly(Side.CLIENT)
	private Icon[] iconArray;

	/** Maps arrow IDs to arrow entity class */
	private static final Map<Item, Class<? extends EntityArrowCustom>> arrowMap = new HashMap<Item, Class<? extends EntityArrowCustom>>();

	/** Maps arrow IDs to bomb arrow Bomb Type */
	private static BiMap<Item, BombType> bombArrowMap;

	/** Maps arrow IDs to elemental arrow Element Type */
	private static final Map<Item, ElementType> elementalArrowMap = new HashMap<Item, ElementType>();

	public ItemHeroBow(int id) {
		super(id);
		setFull3D();
		setMaxDamage(0);
		setMaxStackSize(1);
		setCreativeTab(ZSSCreativeTabs.tabCombat);
	}

	/**
	 * Returns this bow's level for purposes of determining which arrows can be used
	 * Note if the NBT tag has not yet been set, it will be created automatically here
	 */
	public int getLevel(ItemStack bow) {
		if (!bow.hasTagCompound() || !bow.getTagCompound().hasKey("zssBowLevel")) {
			setLevel(bow, 1);
		}
		return bow.getTagCompound().getInteger("zssBowLevel");
	}

	/**
	 * Sets this bow's level, creating a new NBT tag if necessary
	 */
	private void setLevel(ItemStack bow, int level) {
		if (!bow.hasTagCompound()) { bow.setTagCompound(new NBTTagCompound()); }
		bow.getTagCompound().setInteger("zssBowLevel", level);
	}

	public Mode getMode(ItemStack stack) {
		if (!stack.hasTagCompound() || !stack.getTagCompound().hasKey("zssItemMode")) {
			setMode(stack, Mode.DEFAULT);
		}
		return Mode.values()[stack.getTagCompound().getInteger("zssItemMode") % Mode.values().length];
	}

	private void setMode(ItemStack stack, Mode mode) {
		if (!stack.hasTagCompound()) { stack.setTagCompound(new NBTTagCompound()); }
		stack.getTagCompound().setInteger("zssItemMode", mode.ordinal());
	}

	@Override
	public void nextItemMode(ItemStack stack, EntityPlayer player) {
		if (!player.isUsingItem()) {
			setMode(stack, getMode(stack).next(getLevel(stack)));
		}
	}

	@Override
	public void prevItemMode(ItemStack stack, EntityPlayer player) {
		if (!player.isUsingItem()) {
			setMode(stack, getMode(stack).prev(getLevel(stack)));
		}
	}

	@Override
	public int getCurrentMode(ItemStack stack, EntityPlayer player) {
		return getMode(stack).ordinal();
	}

	@Override
	public void setCurrentMode(ItemStack stack, EntityPlayer player, int mode) {
		setMode(stack, Mode.values()[mode % Mode.values().length]);
	}

	@Override
	public ItemStack getRenderStackForMode(ItemStack stack, EntityPlayer player) {
		Item item = getMode(stack).getArrowItem();
		ItemStack ret = (item == null ? null : new ItemStack(item, 0));
		if (ret != null) {
			for (ItemStack inv : player.inventory.mainInventory) {
				if (inv != null && inv.getItem() == ret.getItem()) {
					ret.stackSize += inv.stackSize;
					if (ret.stackSize > 98) {
						ret.stackSize = 99;
						break;
					}
				}
			}
		}
		return ret;
	}

	/**
	 * Retrieves the currently nocked arrow (retrieved from player's extended properties)
	 */
	public ItemStack getArrow(EntityPlayer player) {
		return ZSSPlayerInfo.get(player).getNockedArrow();
	}

	/**
	 * Stores the currently nocked arrow in the player's extended properties
	 */
	private void setArrow(EntityPlayer player, ItemStack arrow) {
		ZSSPlayerInfo.get(player).setNockedArrow(arrow);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public float getMaxZoomTime() {
		return 20.0F;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public float getZoomFactor() {
		return 0.15F;
	}

	@Override
	public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity entity) {
		if (Config.areArrowTradesEnabled() && entity instanceof EntityVillager && !player.worldObj.isRemote) {
			EntityVillager villager = (EntityVillager) entity;
			MerchantRecipeList trades = villager.getRecipes(player);
			if (villager.getProfession() == 2 && trades != null && trades.size() >= Config.getFriendTradesRequired()) {
				int level = getLevel(stack);
				MerchantRecipe trade = null;
				if (level > 1) {
					trade = new MerchantRecipe(new ItemStack(ZSSItems.arrowFire, 4), new ItemStack(Item.emerald, 16));
					if (MerchantRecipeHelper.doesListContain(trades, trade)) {
						trade = new MerchantRecipe(new ItemStack(ZSSItems.arrowIce, 4), new ItemStack(Item.emerald, 20));
						if (level > 2 && MerchantRecipeHelper.doesListContain(trades, trade)) {
							trade = new MerchantRecipe(new ItemStack(ZSSItems.arrowLight, 4), new ItemStack(Item.emerald, 40));
						}
					}
				}
				if (trade != null && MerchantRecipeHelper.addToListWithCheck(trades, trade)) {
					player.addChatMessage(StatCollector.translateToLocal("chat.zss.trade.arrow"));
				} else {
					player.addChatMessage(StatCollector.translateToLocal("chat.zss.trade.generic.sorry.1"));
				}
			} else {
				player.addChatMessage(StatCollector.translateToLocal("chat.zss.trade.generic.sorry.0"));
			}
		}

		return true;
	}

	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
		ArrowNockEvent event = new ArrowNockEvent(player, stack);
		if (ZSSMain.isBG2Enabled) { // 'fake' event:
			event.setCanceled(BattlegearEvents.preArrowNock(event));
		} else {
			MinecraftForge.EVENT_BUS.post(event);
		}
		if (event.isCanceled()) {
			// May not be needed: make sure bow does not have an arrow 'nocked' in NBT if no longer in use
			if (player.getItemInUse() == null && getArrow(player) != null) {
				//LogHelper.warning("Removing arrow from bow when not in use after nock event");
				setArrow(player, null);
			}
			return event.result;
		}
		// This can only be reached if BG2 is not installed
		if (nockArrowFromInventory(stack, player)) {
			int duration = getMaxItemUseDuration(stack);
			if (ZSSMain.isBG2Enabled) {
				duration -= (EnchantmentHelper.getEnchantmentLevel(BaseEnchantment.bowCharge.effectId, stack) * 20000);
			}
			player.setItemInUse(stack, duration);
		}
		return stack;
	}

	@Override
	public void onPlayerStoppedUsing(ItemStack bow, World world, EntityPlayer player, int ticksRemaining) {
		ZSSPlayerInfo.get(player).hasAutoBombArrow = false;
		int ticksInUse = getMaxItemUseDuration(bow) - ticksRemaining;
		ArrowLooseEvent event = new ArrowLooseEvent(player, bow, ticksInUse);
		if (ZSSMain.isBG2Enabled) { // 'fake' event:
			event.setCanceled(BattlegearEvents.preArrowLoose(event));
		} else {
			MinecraftForge.EVENT_BUS.post(event);
		}
		if (event.isCanceled()) {
			if (getArrow(player) != null) {
				setArrow(player, null); // nocked from inventory from empty quiver slot, then hot-swapped
			}
			return;
		}
		// This code can only be reached if not using a quiver
		ItemStack arrowStack = getArrow(player);
		if (arrowStack != null) { // can be null when hot-swapping to an empty quiver slot
			if (canShootArrow(player, bow, arrowStack)) {
				fireArrow(event, arrowStack, bow, player);
			}
			setArrow(player, null);
		}
	}

	/**
	 * Call from ArrowLooseEvent when using Battlegear2's quiver system to prevent handling by
	 * the default fire handler, which allows any bow to fire any registered arrow (e.g. the
	 * vanilla bow could fire Light Arrows) and also has first priority, preventing vanilla
	 * arrows from being handled by Hero's Bow.
	 * 
	 * This will not be necessary if pull request to quiver API is accepted.
	 * 
	 * @param quiverStack	The IArrowContainer2-containing stack, i.e. a quiver
	 * @param arrowStack	The arrow stack as retrieved from the quiver
	 */
	@Method(modid="battlegear2")
	public void bg2FireArrow(ArrowLooseEvent event, ItemStack quiverStack, ItemStack arrowStack) {
		if (!canShootArrow(event.entityPlayer, event.bow, arrowStack)) {
			return; // prevents hot-swap firing, since we are bypassing the fire handlers
		}
		float charge = new PlayerEventChild.QuiverArrowEvent.ChargeCalculations(event).getCharge();
		if (charge < 0.1F) {
			return;
		}
		World world = event.entityPlayer.worldObj;
		IArrowContainer2 quiver = (IArrowContainer2) quiverStack.getItem();
		EntityArrow arrowEntity = getArrowEntity(arrowStack, world, event.entityPlayer, charge * 2.0F);
		if (arrowEntity == null) { // try to construct BG2 arrow
			arrowEntity = QuiverArrowRegistry.getArrowType(arrowStack, world, event.entityPlayer, charge * 2.0F);
		}
		if (arrowEntity != null) {
			if (arrowEntity instanceof EntityArrowCustom) {
				applyCustomArrowSettings(event.entityPlayer, event.bow, arrowStack, (EntityArrowCustom) arrowEntity, charge);
			}
			// replicate BG2's ArrowLooseEvent handling here:
			PlayerEventChild.QuiverArrowEvent.Firing arrowEvent = new PlayerEventChild.QuiverArrowEvent.Firing(event, quiverStack, arrowEntity);
			quiver.onPreArrowFired(arrowEvent);
			if (!MinecraftForge.EVENT_BUS.post(arrowEvent)) {
				if (arrowEvent.isCritical || charge == 1.0F) {
					arrowEntity.setIsCritical(true);
				}
				if (arrowEvent.addEnchantments) { // applyArrowSettings also sets critical
					applyArrowSettings(arrowEntity, event.bow, charge);
				}
				if (event.entityPlayer.capabilities.isCreativeMode || EnchantmentHelper.getEnchantmentLevel(Enchantment.infinity.effectId, event.bow) > 0) {
					arrowEntity.canBePickedUp = 2;
				}
				if (arrowEvent.bowSoundVolume > 0) {
					world.playSoundAtEntity(event.entityPlayer, Sounds.BOW_RELEASE, arrowEvent.bowSoundVolume, 1.0F / (itemRand.nextFloat() * 0.4F + 1.2F) + charge * 0.5F);
				}
				if (!world.isRemote) {
					world.spawnEntityInWorld(arrowEntity);
				}
				quiver.onArrowFired(world, event.entityPlayer, quiverStack, event.bow, arrowEntity);
			}
		}
	}

	/**
	 * Called if ArrowLooseEvent was not canceled, so either the player is not using a quiver,
	 * or Battlegear2 is not loaded.
	 * Constructs and fires the arrow, if possible, and may consume the appropriate inventory item.
	 * @param arrowStack	The stack retrieved from {@link ItemHeroBow#getArrow}, i.e. from the stack's NBT
	 */
	private void fireArrow(ArrowLooseEvent event, ItemStack arrowStack, ItemStack bow, EntityPlayer player) {
		boolean flag = (player.capabilities.isCreativeMode || EnchantmentHelper.getEnchantmentLevel(Enchantment.infinity.effectId, bow) > 0);
		if (flag || PlayerUtils.hasItem(player, arrowStack)) {
			float charge = (float) event.charge / 20.0F;
			charge = Math.min((charge * charge + charge * 2.0F) / 3.0F, 1.0F);
			if ((double) charge < 0.1D) {
				return;
			}

			EntityArrow arrowEntity = getArrowEntity(arrowStack, player.worldObj, player, charge * 2.0F);
			if (arrowEntity == null && ZSSMain.isBG2Enabled) { // try to construct BG2 arrow
				arrowEntity = QuiverArrowRegistry.getArrowType(arrowStack, player.worldObj, player, charge * 2.0F);
			}
			if (arrowEntity != null) {
				LogHelper.finer("Created arrow entity: " + arrowEntity);
				applyArrowSettings(arrowEntity, bow, charge);
				if (arrowEntity instanceof EntityArrowCustom) {
					applyCustomArrowSettings(event.entityPlayer, event.bow, arrowStack, (EntityArrowCustom) arrowEntity, charge);
				}
				player.worldObj.playSoundAtEntity(player, Sounds.BOW_RELEASE, 1.0F, 1.0F / (itemRand.nextFloat() * 0.4F + 1.2F) + charge * 0.5F);

				if (flag) {
					arrowEntity.canBePickedUp = 2;
				} else {
					PlayerUtils.consumeInventoryItem(player, arrowStack, 1);
				}

				if (!player.worldObj.isRemote) {
					player.worldObj.spawnEntityInWorld(arrowEntity);
				}
			}
		}
	}

	/**
	 * Returns true if this bow's level is capable of shooting the given arrow
	 */
	public boolean canShootArrow(EntityPlayer player, ItemStack bow, ItemStack arrowStack) {
		Item arrowItem = (arrowStack == null ? null : arrowStack.getItem());
		if (arrowMap.containsKey(arrowItem)) {
			if (player.capabilities.isCreativeMode) {
				return true;
			}
			if (elementalArrowMap.containsKey(arrowItem)) {
				if (ZSSPlayerInfo.get(player).isNayruActive()) {
					return false;
				}
				int n = getLevel(bow);
				if (n < 3) {
					return (n == 2 && elementalArrowMap.get(arrowItem) != ElementType.LIGHT);
				}
			}

			return true;
		}
		// allow hero's bow to fire arrows registered with BG2
		return ZSSMain.isBG2Enabled && QuiverArrowRegistry.isKnownArrow(arrowStack);
	}

	/**
	 * Returns null or a valid arrow stack from the player's inventory, valid
	 * meaning that {@link #canShootArrow} returned true
	 */
	private ItemStack getArrowFromInventory(ItemStack bow, EntityPlayer player) {
		Item modeArrow = getMode(bow).getArrowItem();
		ItemStack arrow = null;
		if (modeArrow == null && Config.enableAutoBombArrows() && player.isSneaking()) {
			arrow = getAutoBombArrow(bow, player);
		}
		// Search specifically for the selected arrow type:
		if (modeArrow != null && canShootArrow(player, bow, new ItemStack(modeArrow))) {
			for (ItemStack stack : player.inventory.mainInventory) {
				if (stack != null && stack.getItem() == modeArrow) {
					arrow = stack;
					break;
				}
			}
		} else if (arrow == null) { // otherwise use default behavior
			for (ItemStack stack : player.inventory.mainInventory) {
				if (stack != null && canShootArrow(player, bow, stack)) {
					arrow = stack;
					break;
				}
			}
		}

		if (arrow == null && player.capabilities.isCreativeMode) {
			arrow = new ItemStack(modeArrow == null ? Item.arrow : modeArrow);
		}

		return arrow;
	}

	/**
	 * Returns true if a suitable arrow to fire was found in the inventory
	 */
	public boolean nockArrowFromInventory(ItemStack bow, EntityPlayer player) {
		setArrow(player, getArrowFromInventory(bow, player));
		return getArrow(player) != null;
	}

	/**
	 * If the player is sneaking and auto-bomb arrows is enabled, this will return an
	 * appropriate bomb arrow, using the first bomb, bomb arrow or bomb bag encountered,
	 * adding a new arrow to the player's inventory while consuming both a bomb and a 
	 * regular arrow if applicable
	 */
	private ItemStack getAutoBombArrow(ItemStack bow, EntityPlayer player) {
		ItemStack arrow = null;
		// Player must have a standard arrow to construct bomb arrow from bomb or bomb bag
		boolean hasArrow = (player.capabilities.isCreativeMode || player.inventory.hasItem(Item.arrow.itemID));
		// Flag whether the bomb arrow was already determined and items need not be consumed
		boolean hasAutoArrow = ZSSPlayerInfo.get(player).hasAutoBombArrow;

		for (int i = 0; i < player.inventory.getSizeInventory() && arrow == null; ++i) {
			ItemStack stack = player.inventory.getStackInSlot(i);
			if (stack != null) {
				if (stack.getItem() == ZSSItems.arrowBomb || stack.getItem() == ZSSItems.arrowBombFire || stack.getItem() == ZSSItems.arrowBombWater) {
					arrow = stack;
				} else if (stack.getItem() == ZSSItems.bombBag) {
					ItemBombBag bombBag = (ItemBombBag) stack.getItem();
					int bagType = bombBag.getBagBombType(stack);
					if (bagType >= 0 && bombBag.getBombsHeld(stack) > 0) {
						BombType type = BombType.values()[bagType % BombType.values().length];
						ItemStack bombArrow = new ItemStack(bombArrowMap.inverse().get(type),1,0);
						if (hasAutoArrow || player.capabilities.isCreativeMode) {
							arrow = bombArrow;
						} else if (hasArrow && player.inventory.addItemStackToInventory(bombArrow)) {
							if (bombBag.removeBomb(stack)) {
								if (player.inventory.consumeInventoryItem(Item.arrow.itemID)) {
									arrow = bombArrow;
								} else {
									bombBag.addBombs(stack, bombArrow);
								}
							} else {
								PlayerUtils.consumeInventoryItem(player, bombArrow, 1);
							}
						}
					}
				} else if (stack.getItem() == ZSSItems.bomb) {
					ItemStack bombArrow = new ItemStack(bombArrowMap.inverse().get(ItemBomb.getType(stack)),1,0);
					if (hasAutoArrow || player.capabilities.isCreativeMode) {
						arrow = bombArrow;
					} else if (hasArrow && player.inventory.consumeInventoryItem(Item.arrow.itemID)) {
						arrow = bombArrow;
						player.inventory.setInventorySlotContents(i, bombArrow);
					}
				}
			}
		}

		ZSSPlayerInfo.get(player).hasAutoBombArrow = (arrow != null);
		return arrow;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Icon getIcon(ItemStack stack, int renderPass, EntityPlayer player, ItemStack usingItem, int useRemaining) {
		if (usingItem == null) { return itemIcon; }
		int t = stack.getMaxItemUseDuration() - useRemaining;
		return (t > 17 ? iconArray[2] : (t > 13 ? iconArray[1] : (t > 0 ? iconArray[0] : itemIcon)));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister register) {
		itemIcon = register.registerIcon(ModInfo.ID + ":" + getUnlocalizedName().substring(9) + "_standard_0");
		iconArray = new Icon[3];
		for (int i = 0; i < iconArray.length; ++i) {
			iconArray[i] = register.registerIcon(ModInfo.ID + ":" + getUnlocalizedName().substring(9) + "_standard_" + (i + 1));
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack,	EntityPlayer player, List list, boolean par4) {
		list.add(EnumChatFormatting.ITALIC + StatCollector.translateToLocal("tooltip." + getUnlocalizedName().substring(5) + ".desc.0"));
		list.add(EnumChatFormatting.ITALIC + StatCollector.translateToLocalFormatted("tooltip." + getUnlocalizedName().substring(5) + ".desc.1", getLevel(stack)));
	}

	@Override
	public void handleFairyUpgrade(EntityItem item, EntityPlayer player, TileEntityDungeonCore core) {
		ItemStack stack = item.getEntityItem();
		int n = getLevel(stack);
		if (n < 3 && core.consumeRupees((n + 1) * Config.getHeroBowUpgradeCost())) {
			setLevel(stack, ++n);
			core.worldObj.playSoundEffect(core.xCoord + 0.5D, core.yCoord + 1, core.zCoord + 0.5D, Sounds.SECRET_MEDLEY, 1.0F, 1.0F);
			player.triggerAchievement(ZSSAchievements.fairyBow);
			if (n == 3) {
				player.triggerAchievement(ZSSAchievements.fairyBowMax);
			}
		} else {
			core.worldObj.playSoundEffect(core.xCoord + 0.5D, core.yCoord + 1, core.zCoord + 0.5D, Sounds.FAIRY_LAUGH, 1.0F, 1.0F);
			player.addChatMessage(StatCollector.translateToLocal("chat.zss.fairy.laugh.unworthy"));
		}
	}

	@Override
	public boolean hasFairyUpgrade(ItemStack stack) {
		return getLevel(stack) < 3;
	}

	@Method(modid="battlegear2")
	@Override
	public boolean sheatheOnBack(ItemStack stack) {
		return true;
	}

	@Method(modid="battlegear2")
	@Override
	public boolean isOffhandHandDual(ItemStack stack) {
		return false;
	}

	@Method(modid="battlegear2")
	@Override
	public boolean offhandAttackEntity(OffhandAttackEvent event, ItemStack main, ItemStack offhand) {
		return false;
	}

	@Method(modid="battlegear2")
	@Override
	public boolean offhandClickAir(PlayerInteractEvent event, ItemStack main, ItemStack offhand) {
		return false;
	}

	@Method(modid="battlegear2")
	@Override
	public boolean offhandClickBlock(PlayerInteractEvent event, ItemStack main, ItemStack offhand) {
		return false;
	}

	@Method(modid="battlegear2")
	@Override
	public void performPassiveEffects(Side side, ItemStack main, ItemStack offhand) {}

	@Method(modid="battlegear2")
	@Override
	public boolean allowOffhand(ItemStack main, ItemStack offhand) {
		return false;
	}

	/**
	 * Applies vanilla arrow settings, such as enchantments and critical
	 * NOTE: These settings would be added by BattleGear2 when using a quiver,
	 * except it is not allowed to occur due to the current API limitations
	 * @param charge should be a value between 0.0F and 1.0F, inclusive
	 */
	public static final void applyArrowSettings(EntityArrow arrow, ItemStack bow, float charge) {
		if (charge == 1.0F) {
			arrow.setIsCritical(true);
		}

		int k = EnchantmentHelper.getEnchantmentLevel(Enchantment.power.effectId, bow);
		if (k > 0) {
			arrow.setDamage(arrow.getDamage() + (double) k * 0.5D + 0.5D);
		}

		int l = EnchantmentHelper.getEnchantmentLevel(Enchantment.punch.effectId, bow);
		if (l > 0) {
			arrow.setKnockbackStrength(l);
		}

		if (EnchantmentHelper.getEnchantmentLevel(Enchantment.flame.effectId, bow) > 0) {
			arrow.setFire(100);
		}
	}

	/**
	 * Applies custom arrow settings
	 * @param charge should be a value between 0.0F and 1.0F, inclusive
	 * @param arrowStack Arrow must be an ItemZeldaArrow; determines arrow type (fire, ice, etc.) and the item that the arrow entity will drop
	 */
	private static void applyCustomArrowSettings(EntityPlayer player, ItemStack bow, ItemStack arrowStack, EntityArrowCustom arrowEntity, float charge) {
		Item arrowItem = arrowStack.getItem();
		arrowEntity.setArrowItem(arrowItem.itemID);

		if (player.getCurrentArmor(ArmorIndex.WORN_HELM) != null && player.getCurrentArmor(ArmorIndex.WORN_HELM).getItem() == ZSSItems.maskHawkeye) {
			EntityLivingBase target = TargetUtils.acquireLookTarget(player, 64, 1.0F);
			if (target != null) {
				arrowEntity.setHomingArrow(true);
				arrowEntity.setTarget(target);
			}
		}

		if (arrowEntity instanceof EntityArrowBomb && bombArrowMap.containsKey(arrowItem)) {
			((EntityArrowBomb) arrowEntity).setType(bombArrowMap.get(arrowItem));
			arrowEntity.setDamage(0.0F);
		} else if (arrowEntity instanceof EntityArrowElemental && elementalArrowMap.containsKey(arrowItem)) {
			((EntityArrowElemental) arrowEntity).setType(elementalArrowMap.get(arrowItem));
		}
	}

	/**
	 * Returns the entity arrow appropriate for the id given, using the
	 * shooter and charge provided during construction
	 */
	@SuppressWarnings("finally")
	public static final EntityArrowCustom getArrowEntity(ItemStack arrowStack, World world, EntityLivingBase shooter, float charge) {
		Item arrowItem = (arrowStack == null ? null : arrowStack.getItem());
		if (arrowMap.containsKey(arrowItem)) {
			EntityArrowCustom arrow = null;
			try {
				try {
					arrow = arrowMap.get(arrowItem).getConstructor(World.class, EntityLivingBase.class, float.class).newInstance(world, shooter, charge);
				} catch (InstantiationException e) {
					e.printStackTrace();
					return null;
				} catch (IllegalAccessException e) {
					e.printStackTrace();
					return null;
				} catch (InvocationTargetException e) {
					e.printStackTrace();
					return null;
				}
			} finally {
				return arrow;
			}
		} else {
			return null;
		}
	}

	public static void initializeArrows() {
		arrowMap.put(Item.arrow, EntityArrowCustom.class);
		arrowMap.put(ZSSItems.arrowBomb, EntityArrowBomb.class);
		arrowMap.put(ZSSItems.arrowBombFire, EntityArrowBomb.class);
		arrowMap.put(ZSSItems.arrowBombWater, EntityArrowBomb.class);
		arrowMap.put(ZSSItems.arrowFire, EntityArrowElemental.class);
		arrowMap.put(ZSSItems.arrowIce, EntityArrowElemental.class);
		arrowMap.put(ZSSItems.arrowLight, EntityArrowElemental.class);

		ImmutableBiMap.Builder<Item, BombType> builder = ImmutableBiMap.builder();
		builder.put(ZSSItems.arrowBomb, BombType.BOMB_STANDARD);
		builder.put(ZSSItems.arrowBombFire, BombType.BOMB_FIRE);
		builder.put(ZSSItems.arrowBombWater, BombType.BOMB_WATER);
		bombArrowMap = builder.build();

		elementalArrowMap.put(ZSSItems.arrowFire, ElementType.FIRE);
		elementalArrowMap.put(ZSSItems.arrowIce, ElementType.ICE);
		elementalArrowMap.put(ZSSItems.arrowLight, ElementType.LIGHT);
	}

	/**
	 * Registers {@link HeroBowFireHandler} and all arrows required for use with
	 * Battlegear2's quiver system
	 */
	@Method(modid="battlegear2")
	public static void registerBG2() {
		QuiverArrowRegistry.addArrowFireHandler(new HeroBowFireHandler());
		registerQuiverArrow(ZSSItems.arrowBomb, EntityArrowBomb.class);
		registerQuiverArrow(ZSSItems.arrowBombFire, EntityArrowBomb.class);
		registerQuiverArrow(ZSSItems.arrowBombWater, EntityArrowBomb.class);
		registerQuiverArrow(ZSSItems.arrowFire, EntityArrowElemental.class);
		registerQuiverArrow(ZSSItems.arrowIce, EntityArrowElemental.class);
		registerQuiverArrow(ZSSItems.arrowLight, EntityArrowElemental.class);
	}

	@Method(modid="battlegear2")
	public static void registerQuiverArrow(Item item, Class<? extends EntityArrow> clazz) {
		// registering entity as null allows arrows to be crafted with quiver, and forces
		// the vanilla bow to use the custom fire handler rather than the default one
		QuiverArrowRegistry.addArrowToRegistry(new ItemStack(item), null);
	}

	/**
	 * 
	 * Handler for building arrows shot with Hero's Bow from Battlegear2 quivers.
	 * With the current API, however, this gets called after the default handler,
	 * meaning it will not work for customizing vanilla arrows; thus, the Hero
	 * Bow must handle itself internally, only passing off to the handlers for
	 * BG2 arrows. This requires bypassing BG2 handling entirely, which is done by
	 * not posting the ArrowNock and ArrowLooseEvents and using fake ones instead
	 * (which was made necessary due to BG2 receiving canceled on those two events)
	 * 
	 * This fire handler is still used by the vanilla bow, though, for firing Zelda
	 * arrows (or preventing them from being fired, as the case may  be).
	 *
	 */
	@Optional.Interface(iface="mods.battlegear2.api.quiver.IArrowFireHandler", modid="battlegear2", striprefs=true)
	public static class HeroBowFireHandler implements IArrowFireHandler {
		@Method(modid="battlegear2")
		@Override
		public boolean canFireArrow(ItemStack arrow, World world, EntityPlayer player, float charge) {
			ItemStack bow = player.getHeldItem();
			if (bow != null) { // allow creative players to shoot custom Zelda arrows using any bow
				return (bow.getItem() instanceof ItemHeroBow ? ((ItemHeroBow) bow.getItem()).canShootArrow(player, bow, arrow) : player.capabilities.isCreativeMode);
			}
			return false;
		}

		@Method(modid="battlegear2")
		@Override
		public EntityArrow getFiredArrow(ItemStack arrow, World world, EntityPlayer player, float charge) {
			ItemStack bow = player.getHeldItem();
			if (bow != null && (bow.getItem() instanceof ItemHeroBow || player.capabilities.isCreativeMode)) {
				EntityArrowCustom arrowEntity = ItemHeroBow.getArrowEntity(arrow, world, player, charge);
				if (arrowEntity != null) {
					// Must apply vanilla settings as well, since BG2's ArrowLooseEvent is not triggered
					ItemHeroBow.applyArrowSettings(arrowEntity, bow, charge);
					ItemHeroBow.applyCustomArrowSettings(player, bow, arrow, arrowEntity, charge);
				}
				return arrowEntity;
			}
			return null;
		}
	}
}
