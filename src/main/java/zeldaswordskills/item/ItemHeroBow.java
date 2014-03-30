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

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mods.battlegear2.api.PlayerEventChild.OffhandAttackEvent;
import mods.battlegear2.api.weapons.IBattlegearWeapon;
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
import zeldaswordskills.lib.Config;
import zeldaswordskills.lib.ModInfo;
import zeldaswordskills.lib.Sounds;
import zeldaswordskills.util.MerchantRecipeHelper;
import zeldaswordskills.util.TargetUtils;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;

import cpw.mods.fml.common.Optional;
import cpw.mods.fml.common.Optional.Method;
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
public class ItemHeroBow extends ItemBow implements IFairyUpgrade, IZoom, IBattlegearWeapon
{
	@SideOnly(Side.CLIENT)
	private Icon[] iconArray;

	/** Maps arrow IDs to arrow entity class */
	private static final Map<Integer, Class<? extends EntityArrow>> arrowMap = new HashMap<Integer, Class<? extends EntityArrow>>();

	/** Maps arrow IDs to bomb arrow Bomb Type */
	private static BiMap<Integer, BombType> bombArrowMap;

	/** Maps arrow IDs to elemental arrow Element Type */
	private static final Map<Integer, ElementType> elementalArrowMap = new HashMap<Integer, ElementType>();

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
	public void setLevel(ItemStack bow, int level) {
		if (!bow.hasTagCompound()) { bow.setTagCompound(new NBTTagCompound()); }
		bow.getTagCompound().setInteger("zssBowLevel", level);
	}

	/**
	 * Returns true if this bow's level is capable of shooting the given arrow id
	 */
	private boolean canShootArrow(EntityPlayer player, ItemStack bow, int arrowId) {
		if (arrowMap.containsKey(arrowId)) {
			if (elementalArrowMap.containsKey(arrowId)) {
				if (ZSSPlayerInfo.get(player).isNayruActive()) {
					return false;
				}
				int n = getLevel(bow);
				if (n < 3) {
					ElementType type = elementalArrowMap.get(arrowId);
					return (player.capabilities.isCreativeMode || (n == 2 && type != ElementType.LIGHT));
				}
			}

			return true;
		}

		return false;
	}

	/**
	 * Sets the arrow type currently in the stack
	 */
	private void setArrow(ItemStack bow, ItemStack arrow) {
		if (!bow.hasTagCompound()) { bow.setTagCompound(new NBTTagCompound()); }
		bow.getTagCompound().setInteger("nockedArrow", arrow != null ? arrow.itemID : -1);
	}

	/**
	 * Retrieves the arrow id currently nocked in the bow, or -1 for no arrow
	 */
	public int getArrow(ItemStack bow) {
		return (bow.hasTagCompound() && bow.getTagCompound().hasKey("nockedArrow") ?
				bow.getTagCompound().getInteger("nockedArrow") : -1);
	}

	/**
	 * Checks if the player has an arrow and, if so, 'nocks' it in the bow
	 * @return returns true if an arrow was 'nocked' in the stack's NBT
	 */
	private boolean nockArrow(ItemStack bow, EntityPlayer player) {
		ItemStack arrow = null;
		for (ItemStack stack : player.inventory.mainInventory) {
			if (stack != null && canShootArrow(player, bow, stack.itemID)) {
				arrow = stack;
				break;
			}
		}

		if (arrow == null && (player.capabilities.isCreativeMode ||
				EnchantmentHelper.getEnchantmentLevel(Enchantment.infinity.effectId, bow) > 0)) {
			arrow = new ItemStack(Item.arrow);
		}

		if (arrow != null && arrow.getItem() == Item.arrow && Config.enableAutoBombArrows() && player.isSneaking()) {
			ItemStack bombArrow = getAutoBombArrow(bow, player);
			arrow = (bombArrow != null ? bombArrow : arrow);
		}

		setArrow(bow, arrow);
		return arrow != null;
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
	public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean isHeld) {
		if (!isHeld && hasAutoArrow(stack)) {
			setHasAutoArrow(stack, false);
		}
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
		if (!ZSSMain.isBG2Enabled) {
			ArrowNockEvent event = new ArrowNockEvent(player, stack);
			MinecraftForge.EVENT_BUS.post(event);
			if (event.isCanceled()) {
				return event.result;
			}
		}
		if (nockArrow(stack, player)) {
			player.setItemInUse(stack, getMaxItemUseDuration(stack));
		}
		return stack;
	}

	@Override
	public void onPlayerStoppedUsing(ItemStack bow, World world, EntityPlayer player, int ticksRemaining) {
		setHasAutoArrow(bow, false);
		int ticksInUse = getMaxItemUseDuration(bow) - ticksRemaining;
		int arrowId = getArrow(bow);
		setArrow(bow, null);
		if (arrowId < 0) { return; }

		if (!ZSSMain.isBG2Enabled) {
			ArrowLooseEvent event = new ArrowLooseEvent(player, bow, ticksInUse);
			MinecraftForge.EVENT_BUS.post(event);
			if (event.isCanceled()) { return; }
			ticksInUse = event.charge;
		}

		boolean flag = player.capabilities.isCreativeMode || EnchantmentHelper.getEnchantmentLevel(Enchantment.infinity.effectId, bow) > 0;

		if (flag || player.inventory.hasItem(arrowId)) {
			float charge = (float) ticksInUse / 20.0F;
			charge = Math.min((charge * charge + charge * 2.0F) / 3.0F, 1.0F);
			if ((double) charge < 0.1D) { return; }

			EntityArrow arrow = getArrowEntity(arrowId, world, player, charge * 2.0F);
			if (arrow != null) {
				if (arrow instanceof EntityArrowCustom) {
					applyCustomArrowSettings(player, (EntityArrowCustom) arrow, bow, charge, arrowId);
				} else {
					applyArrowSettings(arrow, bow, charge);
				}
				world.playSoundAtEntity(player, "random.bow", 1.0F, 1.0F / (itemRand.nextFloat() * 0.4F + 1.2F) + charge * 0.5F);

				if (flag) {
					arrow.canBePickedUp = 2;
				} else {
					player.inventory.consumeInventoryItem(arrowId);
				}

				if (!world.isRemote) {
					world.spawnEntityInWorld(arrow);
				}
			}
		}
	}

	/**
	 * If the player is sneaking and auto-bomb arrows is enabled, this will return an
	 * appropriate bomb arrow, using the first bomb, bomb arrow or bomb bag encountered,
	 * adding a new arrow to the player's inventory while consuming both a bomb and a 
	 * regular arrow if applicable
	 */
	private ItemStack getAutoBombArrow(ItemStack bow, EntityPlayer player) {
		ItemStack arrow = null;
		boolean hasArrow = hasAutoArrow(bow);
		for (int i = 0; i < player.inventory.getSizeInventory(); ++i) {
			ItemStack stack = player.inventory.getStackInSlot(i);
			if (stack != null) {
				if (stack.getItem() == ZSSItems.arrowBomb || stack.getItem() == ZSSItems.arrowBombFire || stack.getItem() == ZSSItems.arrowBombWater) {
					arrow = stack;
					break;
				} else if (stack.getItem() == ZSSItems.bomb) {
					ItemStack bombArrow = new ItemStack(bombArrowMap.inverse().get(ItemBomb.getType(stack)),1,0);
					if (hasArrow || player.inventory.consumeInventoryItem(Item.arrow.itemID)) {
						arrow = bombArrow;
						if (!hasArrow) {
							player.inventory.setInventorySlotContents(i, bombArrow);
						}
						break;
					}
				} else if (stack.getItem() == ZSSItems.bombBag) {
					ItemBombBag bombBag = (ItemBombBag) stack.getItem();
					int bagType = bombBag.getBagBombType(stack);
					if (bagType >= 0 && bombBag.getBombsHeld(stack) > 0 && player.inventory.hasItem(Item.arrow.itemID)) {
						BombType type = BombType.values()[bagType % BombType.values().length];
						ItemStack bombArrow = new ItemStack(bombArrowMap.inverse().get(type),1,0);
						if (hasArrow || (player.inventory.addItemStackToInventory(bombArrow) && bombBag.removeBomb(stack) &&
								player.inventory.consumeInventoryItem(Item.arrow.itemID))) {
							arrow = bombArrow;
							break;
						}
					}
				}
			}
		}

		setHasAutoArrow(bow, arrow != null);
		return arrow;
	}

	/**
	 * Sets whether the bow stack has had a bomb arrow automatically loaded or not
	 */
	private void setHasAutoArrow(ItemStack bow, boolean value) {
		if (!bow.hasTagCompound()) { bow.setTagCompound(new NBTTagCompound()); }
		bow.getTagCompound().setBoolean("hasAutoArrow", value);
	}

	/**
	 * Returns true if a bomb arrow has already been automatically loaded
	 */
	private boolean hasAutoArrow(ItemStack bow) {
		return (bow.hasTagCompound() && bow.getTagCompound().hasKey("hasAutoArrow") && bow.getTagCompound().getBoolean("hasAutoArrow"));
	}

	/**
	 * Applies settings to a vanilla arrow; use custom method for custom arrows
	 * @param charge should be a value between 0.0F and 1.0F, inclusive
	 */
	public static final void applyArrowSettings(EntityArrow arrow, ItemStack bow, float charge) {
		if (charge == 1.0F) { arrow.setIsCritical(true); }

		int k = EnchantmentHelper.getEnchantmentLevel(Enchantment.power.effectId, bow);
		if (k > 0) { arrow.setDamage(arrow.getDamage() + (double) k * 0.5D + 0.5D); }

		int l = EnchantmentHelper.getEnchantmentLevel(Enchantment.punch.effectId, bow);
		if (l > 0) { arrow.setKnockbackStrength(l); }

		if (EnchantmentHelper.getEnchantmentLevel(Enchantment.flame.effectId, bow) > 0) {
			arrow.setFire(100);
		}
	}

	/**
	 * Applies custom arrow settings
	 * @param charge should be a value between 0.0F and 1.0F, inclusive
	 */
	private void applyCustomArrowSettings(EntityPlayer player, EntityArrowCustom arrow, ItemStack bow, float charge, int arrowId) {
		if (charge == 1.0F) { arrow.setIsCritical(true); }
		arrow.setArrowItem(arrowId);
		arrow.setShooter(player);

		if (player.getCurrentArmor(ArmorIndex.WORN_HELM) != null && player.getCurrentArmor(ArmorIndex.WORN_HELM).getItem() == ZSSItems.maskHawkeye) {
			EntityLivingBase target = TargetUtils.acquireLookTarget(player, 64, 1.0F);
			if (target != null) {
				arrow.setHomingArrow(true);
				arrow.setTarget(target);
			}
		}

		if (arrow instanceof EntityArrowBomb && bombArrowMap.containsKey(arrowId)) {
			((EntityArrowBomb) arrow).setType(bombArrowMap.get(arrowId));
			arrow.setDamage(0.0F);
		} else if (arrow instanceof EntityArrowElemental && elementalArrowMap.containsKey(arrowId)) {
			((EntityArrowElemental) arrow).setType(elementalArrowMap.get(arrowId));
		}

		int k = EnchantmentHelper.getEnchantmentLevel(Enchantment.power.effectId, bow);
		double d = arrow.getDamage();
		if (k > 0 && d > 0) { arrow.setDamage(d + (double) k * 0.5D + 0.5D); }

		int l = EnchantmentHelper.getEnchantmentLevel(Enchantment.punch.effectId, bow);
		if (l > 0) { arrow.setKnockbackStrength(l); }

		if (EnchantmentHelper.getEnchantmentLevel(Enchantment.flame.effectId, bow) > 0) {
			arrow.setFire(100);
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Icon getIcon(ItemStack stack, int renderPass, EntityPlayer player, ItemStack usingItem, int useRemaining) {
		if (usingItem == null) { return itemIcon; }
		int ticksInUse = stack.getMaxItemUseDuration() - useRemaining;
		if (ticksInUse > 17) {
			return iconArray[2];
		} else if (ticksInUse > 13) {
			return iconArray[1];
		} else if (ticksInUse > 0) {
			return iconArray[0];
		} else {
			return itemIcon;
		}
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
		list.add(EnumChatFormatting.ITALIC + StatCollector.translateToLocalFormatted("tooltip." + getUnlocalizedName().substring(5) + ".desc.1", new Object[]{getLevel(stack)}));
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

	public static void initializeArrows() {
		arrowMap.put(Item.arrow.itemID, EntityArrowCustom.class);
		arrowMap.put(ZSSItems.arrowBomb.itemID, EntityArrowBomb.class);
		arrowMap.put(ZSSItems.arrowBombFire.itemID, EntityArrowBomb.class);
		arrowMap.put(ZSSItems.arrowBombWater.itemID, EntityArrowBomb.class);
		arrowMap.put(ZSSItems.arrowFire.itemID, EntityArrowElemental.class);
		arrowMap.put(ZSSItems.arrowIce.itemID, EntityArrowElemental.class);
		arrowMap.put(ZSSItems.arrowLight.itemID, EntityArrowElemental.class);

		ImmutableBiMap.Builder<Integer, BombType> builder = ImmutableBiMap.builder();
		builder.put(ZSSItems.arrowBomb.itemID, BombType.BOMB_STANDARD);
		builder.put(ZSSItems.arrowBombFire.itemID, BombType.BOMB_FIRE);
		builder.put(ZSSItems.arrowBombWater.itemID, BombType.BOMB_WATER);
		bombArrowMap = builder.build();

		elementalArrowMap.put(ZSSItems.arrowFire.itemID, ElementType.FIRE);
		elementalArrowMap.put(ZSSItems.arrowIce.itemID, ElementType.ICE);
		elementalArrowMap.put(ZSSItems.arrowLight.itemID, ElementType.LIGHT);
	}

	/**
	 * Returns the entity arrow appropriate for the id given, using the
	 * shooter and charge provided during construction
	 */
	@SuppressWarnings("finally")
	public static EntityArrow getArrowEntity(int arrowId, World world, EntityLivingBase shooter, float charge) {
		if (arrowMap.containsKey(arrowId)) {
			EntityArrow arrow = null;
			try {
				try {
					arrow = arrowMap.get(arrowId).getConstructor(World.class, EntityLivingBase.class, float.class).newInstance(world, shooter, charge);
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
}
