/**
 * Copyright (C) <2019> <coolAlias>
 *
 * This file is part of coolAlias' Zelda Sword Skills Minecraft Mod; as such,
 * you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package zeldaswordskills.item;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

import com.google.common.collect.Maps;

import mods.battlegear2.api.IAllowItem;
import mods.battlegear2.api.ISheathed;
import mods.battlegear2.api.quiver.IArrowFireHandler;
import mods.battlegear2.api.quiver.ISpecialBow;
import mods.battlegear2.api.quiver.QuiverArrowRegistry;
import mods.battlegear2.api.quiver.QuiverArrowRegistry.DefaultArrowFire;
import mods.battlegear2.enchantments.BaseEnchantment;
import mods.battlegear2.items.ItemMBArrow;
import mods.battlegear2.items.ItemQuiver;
import mods.battlegear2.utils.BattlegearConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.ArrowLooseEvent;
import net.minecraftforge.event.entity.player.ArrowNockEvent;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.Optional.Method;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import zeldaswordskills.ZSSAchievements;
import zeldaswordskills.ZSSMain;
import zeldaswordskills.api.entity.BombType;
import zeldaswordskills.api.item.ArmorIndex;
import zeldaswordskills.api.item.ICyclableItem;
import zeldaswordskills.api.item.IFairyUpgrade;
import zeldaswordskills.api.item.IMagicArrow;
import zeldaswordskills.api.item.ISpecialAmmunition;
import zeldaswordskills.api.item.IUnenchantable;
import zeldaswordskills.api.item.IZoom;
import zeldaswordskills.api.item.ItemModeRegistry;
import zeldaswordskills.block.tileentity.TileEntityDungeonCore;
import zeldaswordskills.creativetab.ZSSCreativeTabs;
import zeldaswordskills.entity.ZSSVillagerInfo.EnumVillager;
import zeldaswordskills.entity.player.ZSSPlayerInfo;
import zeldaswordskills.entity.projectile.EntityArrowBomb;
import zeldaswordskills.entity.projectile.EntityArrowBombFire;
import zeldaswordskills.entity.projectile.EntityArrowBombWater;
import zeldaswordskills.entity.projectile.EntityArrowCustom;
import zeldaswordskills.entity.projectile.EntityArrowFire;
import zeldaswordskills.entity.projectile.EntityArrowIce;
import zeldaswordskills.entity.projectile.EntityArrowLight;
import zeldaswordskills.entity.projectile.EntityArrowSilver;
import zeldaswordskills.handler.BattlegearEvents;
import zeldaswordskills.ref.Config;
import zeldaswordskills.ref.ModInfo;
import zeldaswordskills.ref.Sounds;
import zeldaswordskills.util.MerchantRecipeHelper;
import zeldaswordskills.util.PlayerUtils;
import zeldaswordskills.util.TargetUtils;

/**
 * 
 * The Hero's Bow can fire arrows even when the player has none if it has the infinity enchantment.
 * 
 * The basic Hero's Bow (level 1) can fire any non-{@link ISpecialAmmunition}.
 * 
 * ISpecialAmmunition arrows define their own level requirements, and {@link IMagicArrow}
 * can further define magic requirements.
 *
 */
@Optional.InterfaceList(value={
		@Optional.Interface(iface="mods.battlegear2.api.IAllowItem", modid="battlegear2", striprefs=true),
		@Optional.Interface(iface="mods.battlegear2.api.ISheathed", modid="battlegear2", striprefs=true),
		@Optional.Interface(iface="mods.battlegear2.api.quiver.ISpecialBow", modid="battlegear2", striprefs=true)
})
public class ItemHeroBow extends ItemBow implements ICyclableItem, IFairyUpgrade, IModItem, IUnenchantable, IZoom,
IAllowItem, ISheathed, ISpecialBow
{
	@SideOnly(Side.CLIENT)
	private List<ModelResourceLocation> models;

	/** Maps Bomb Type to correct bomb arrow items */
	private static EnumMap<BombType, Item> bombArrowMap = Maps.newEnumMap(BombType.class);

	/** Static list to store fire handlers; can't set type without requiring BG2 */
	private static final List<IArrowFireHandler> fireHandlers = new ArrayList<IArrowFireHandler>();

	public ItemHeroBow() {
		super();
		setFull3D();
		setMaxDamage(0);
		setMaxStackSize(1);
		setCreativeTab(ZSSCreativeTabs.tabCombat);
	}

	/**
	 * Returns "item.zss.unlocalized_name" for translation purposes
	 */
	@Override
	public String getUnlocalizedName() {
		return super.getUnlocalizedName().replaceFirst("item.", "item.zss.");
	}

	@Override
	public String getUnlocalizedName(ItemStack stack) {
		return getUnlocalizedName();
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

	public int getMode(ItemStack stack) {
		if (!stack.hasTagCompound() || !stack.getTagCompound().hasKey("zssItemMode")) {
			this.setMode(stack, 0);
		}
		return stack.getTagCompound().getInteger("zssItemMode");
	}

	private void setMode(ItemStack stack, int index) {
		if (!stack.hasTagCompound()) { stack.setTagCompound(new NBTTagCompound()); }
		stack.getTagCompound().setInteger("zssItemMode", index);
	}

	@Override
	public void nextItemMode(ItemStack stack, EntityPlayer player) {
		if (!player.isUsingItem()) {
			int level = (player.capabilities.isCreativeMode ? 3 : this.getLevel(stack));
			int current = this.getMode(stack);
			int mode = current;
			do {
				mode = ItemModeRegistry.ARROW_MODES.next(mode, level);
			} while (mode != current && this.skipMode(mode, player));
			this.setMode(stack, mode);
		}
	}

	@Override
	public void prevItemMode(ItemStack stack, EntityPlayer player) {
		if (!player.isUsingItem()) {
			int level = (player.capabilities.isCreativeMode ? 3 : this.getLevel(stack));
			int current = this.getMode(stack);
			int mode = current;
			do {
				mode = ItemModeRegistry.ARROW_MODES.prev(mode, level);
			} while (mode != current && this.skipMode(mode, player));
			this.setMode(stack, mode);
		}
	}

	private boolean skipMode(int mode, EntityPlayer player) {
		if (player.capabilities.isCreativeMode) {
			return false;
		}
		ItemStack stack = ItemModeRegistry.ARROW_MODES.getStack(mode);
		return stack != null && !PlayerUtils.hasItem(player, stack);
	}

	@Override
	public int getCurrentMode(ItemStack stack, EntityPlayer player) {
		return this.getMode(stack);
	}

	@Override
	public void setCurrentMode(ItemStack stack, EntityPlayer player, int mode) {
		this.setMode(stack, mode);
	}

	@Override
	public ItemStack getRenderStackForMode(ItemStack stack, EntityPlayer player) {
		ItemStack render = ItemModeRegistry.ARROW_MODES.getStack(this.getMode(stack));
		if (render != null) {
			render.stackSize = 0;
			for (ItemStack inv : player.inventory.mainInventory) {
				if (inv != null && inv.getItem() == render.getItem() && (inv.getItemDamage() == render.getItemDamage() || !render.getHasSubtypes())) {
					render.stackSize += inv.stackSize;
					if (render.stackSize > 98) {
						render.stackSize = 99;
						break;
					}
				}
			}
		}
		return render;
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
			if (EnumVillager.PRIEST.is(villager) && trades != null) {
				int level = getLevel(stack);
				MerchantRecipe trade = null;
				if (level > 1) {
					trade = new MerchantRecipe(new ItemStack(Items.emerald, 16), new ItemStack(ZSSItems.arrowFire, 4));
					if (MerchantRecipeHelper.doesListContain(trades, trade)) {
						trade = new MerchantRecipe(new ItemStack(Items.emerald, 20), new ItemStack(ZSSItems.arrowIce, 4));
						if (level > 2 && MerchantRecipeHelper.doesListContain(trades, trade)) {
							trade = new MerchantRecipe(new ItemStack(Items.emerald, 40), new ItemStack(ZSSItems.arrowLight, 4));
						}
					}
				}
				if (trade != null && MerchantRecipeHelper.addToListWithCheck(trades, trade)) {
					PlayerUtils.sendTranslatedChat(player, "chat.zss.trade.arrow");
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
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
		ArrowNockEvent event = new ArrowNockEvent(player, stack);
		MinecraftForge.EVENT_BUS.post(event);
		if (event.isCanceled()) {
			return event.result;
		}
		// This can only be reached if BG2 is not installed or no arrow was found in the quiver
		if (nockArrowFromInventory(stack, player)) {
			int duration = getMaxItemUseDuration(stack);
			if (ZSSMain.isBG2Enabled) {
				duration -= (EnchantmentHelper.getEnchantmentLevel(BaseEnchantment.bowCharge.get().effectId, stack) * 20000);
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
		MinecraftForge.EVENT_BUS.post(event);
		if (event.isCanceled()) {
			if (getArrow(player) != null) {
				setArrow(player, null); // nocked from inventory from empty quiver slot, then hot-swapped
			}
			return;
		}
		// Default behavior found usable arrow in standard player inventory
		ItemStack arrowStack = getArrow(player);
		if (arrowStack != null) { // can be null when hot-swapping to an empty quiver slot
			if (canShootArrow(player, bow, arrowStack)) {
				fireArrow(event, arrowStack, bow, player);
			}
			setArrow(player, null);
		}
	}

	/**
	 * Called if ArrowLooseEvent was not canceled, so either the player is not using a quiver,
	 * or Battlegear2 is not loaded.
	 * Constructs and fires the arrow, if possible, and may consume the appropriate inventory item.
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
			if (arrowEntity != null && confirmArrowShot(arrowStack, player)) {
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
	 * Returns true if the player truly can fire this arrow; used to consume MP for magic arrows
	 */
	private boolean confirmArrowShot(ItemStack arrow, EntityPlayer player) {
		if (arrow != null && arrow.getItem() instanceof IMagicArrow) {
			float mp = ((IMagicArrow) arrow.getItem()).getMagicCost(arrow, player);
			return ZSSPlayerInfo.get(player).consumeMagic(mp);
		}
		return true;
	}

	/**
	 * Returns true if this bow's level is capable of shooting the given arrow
	 */
	public boolean canShootArrow(EntityPlayer player, ItemStack bow, ItemStack arrowStack) {
		Item arrowItem = (arrowStack == null ? null : arrowStack.getItem());
		if (ItemModeRegistry.ARROW_MODES.contains(arrowStack)) {
			if (player.capabilities.isCreativeMode) {
				return true;
			}
			if (arrowItem instanceof ISpecialAmmunition && ((ISpecialAmmunition) arrowItem).getRequiredLevelForAmmo(arrowStack) > this.getLevel(bow)) {
				return false;
			}
			if (arrowItem instanceof IMagicArrow) {
				return ZSSPlayerInfo.get(player).canUseMagic() && ZSSPlayerInfo.get(player).getCurrentMagic() >= ((IMagicArrow) arrowItem).getMagicCost(arrowStack, player);
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
		ItemStack modeArrow = ItemModeRegistry.ARROW_MODES.getStack(this.getMode(bow));
		ItemStack arrow = null;
		if (modeArrow == null && Config.enableAutoBombArrows() && player.isSneaking()) {
			arrow = getAutoBombArrow(bow, player);
		}
		// Search specifically for the selected arrow type
		if (modeArrow != null && canShootArrow(player, bow, modeArrow)) {
			if (player.capabilities.isCreativeMode || PlayerUtils.hasItem(player, modeArrow)) {
				arrow = modeArrow;
			}
		}
		// No mode selected or arrow could not be shot - search inventory for shootable arrow
		if (arrow == null) {
			for (ItemStack stack : player.inventory.mainInventory) {
				if (stack != null && canShootArrow(player, bow, stack)) {
					arrow = stack;
					break;
				}
			}
		}
		// If still no arrow and player is in Creative Mode, nock default arrow
		if (arrow == null && player.capabilities.isCreativeMode) {
			arrow = new ItemStack(Items.arrow);
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
		boolean hasArrow = (player.capabilities.isCreativeMode || player.inventory.hasItem(Items.arrow));
		// Flag whether the bomb arrow was already determined and items need not be consumed
		boolean hasAutoArrow = ZSSPlayerInfo.get(player).hasAutoBombArrow;
		for (int i = 0; i < player.inventory.getSizeInventory() && arrow == null; ++i) {
			ItemStack stack = player.inventory.getStackInSlot(i);
			if (stack != null) {
				if (stack.getItem() == ZSSItems.arrowBomb || stack.getItem() == ZSSItems.arrowBombFire || stack.getItem() == ZSSItems.arrowBombWater) {
					arrow = stack;
				} else if (stack.getItem() instanceof ItemBombBag) {
					ItemBombBag bombBag = (ItemBombBag) stack.getItem();
					ItemStack bomb = bombBag.removeBomb(player, stack);
					if (bomb != null) {
						BombType type = ItemBomb.getType(bomb);
						ItemStack bombArrow = new ItemStack(bombArrowMap.get(type), 1, 0);
						if (player.capabilities.isCreativeMode) {
							arrow = bombArrow;
						} else if (hasAutoArrow) {
							arrow = bombArrow;
							bombBag.addBombs(stack, bomb);
						} else if (hasArrow && player.inventory.addItemStackToInventory(bombArrow)) {
							if (player.inventory.consumeInventoryItem(Items.arrow)) {
								arrow = bombArrow;
							} else {
								bombBag.addBombs(stack, bomb);
								PlayerUtils.consumeInventoryItem(player, bombArrow, 1);
							}
						}
					}
				} else if (stack.getItem() == ZSSItems.bomb) {
					ItemStack bombArrow = new ItemStack(bombArrowMap.get(ItemBomb.getType(stack)), 1, 0);
					if (hasAutoArrow || player.capabilities.isCreativeMode) {
						arrow = bombArrow;
					} else if (hasArrow && player.inventory.consumeInventoryItem(Items.arrow)) {
						arrow = bombArrow;
						player.inventory.setInventorySlotContents(i, bombArrow);
					}
				}
			}
		}
		if (arrow == null && player.capabilities.isCreativeMode) {
			arrow = new ItemStack(ZSSItems.arrowBomb);
		}
		ZSSPlayerInfo.get(player).hasAutoBombArrow = (arrow != null);
		return arrow;
	}

	@Override
	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
		return !ItemStack.areItemsEqual(oldStack, newStack);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public ModelResourceLocation getModel(ItemStack stack, EntityPlayer player, int ticksRemaining) {
		if (!player.isUsingItem()) {
			return models.get(0);
		}
		int ticks = stack.getMaxItemUseDuration() - ticksRemaining;
		int i = (ticks > 17 ? 3 : ticks > 13 ? 2 : ticks > 0 ? 1 : 0);
		ItemStack arrowStack = ZSSPlayerInfo.get(player).getNockedArrow();
		if (i > 0 && arrowStack != null) {
			// TODO this works, but is probably not ideal
			Minecraft mc = Minecraft.getMinecraft();
			GlStateManager.pushMatrix();
			boolean firstPerson = (player == mc.thePlayer && mc.getRenderManager().options.thirdPersonView == 0);
			if (firstPerson) {
				GlStateManager.rotate(45.0F, 0.0F, 1.0F, 0.0F);
				GlStateManager.rotate(-25.0F, 0.0F, 0.0F, 1.0F);
				GlStateManager.translate(-0.4D, 0.4D, 0.0D);
				GlStateManager.scale(1.325F, 1.325F, 1.325F);
			} else {
				GlStateManager.rotate(87.5F, 0.0F, 1.0F, 0.0F);
				GlStateManager.rotate(45F, 0.0F, 0.0F, 1.0F);
				GlStateManager.rotate(-10.0F, 1.0F, 0.0F, 0.0F);
				GlStateManager.translate(0.125D, 0.125D, 0.125D);
				GlStateManager.scale(0.625F, 0.625F, 0.625F);
			}
			GlStateManager.translate(-(-3F+i)/16F, -(-3F+i)/16F, 0.5F/16F);
			mc.getRenderItem().renderItem(arrowStack, ItemCameraTransforms.TransformType.FIXED);
			GlStateManager.popMatrix();
		}
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
	public void registerResources() {
		String[] variants = getVariants();
		models = new ArrayList<ModelResourceLocation>(variants.length);
		for (int i = 0; i < variants.length; ++i) {
			models.add(new ModelResourceLocation(variants[i], "inventory"));
		}
		ModelLoader.registerItemVariants(this, models.toArray(new ModelResourceLocation[0]));
		ModelLoader.setCustomMeshDefinition(this, new ItemMeshDefinition() {
			@Override
			public ModelResourceLocation getModelLocation(ItemStack stack) {
				return models.get(0);
			}
		});
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(Item item, CreativeTabs tab, List<ItemStack> list) {
		for (int i = 1; i < 4; ++i) {
			ItemStack stack = new ItemStack(item);
			setLevel(stack, i);
			list.add(stack);
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, EntityPlayer player, List<String> list, boolean advanced) {
		list.add(StatCollector.translateToLocal("tooltip." + getUnlocalizedName().substring(5) + ".desc.0"));
		list.add(StatCollector.translateToLocalFormatted("tooltip." + getUnlocalizedName().substring(5) + ".desc.1", getLevel(stack)));
		ItemStack mode = ItemModeRegistry.ARROW_MODES.getStack(this.getMode(stack));
		if (mode != null) {
			list.add(EnumChatFormatting.YELLOW + StatCollector.translateToLocalFormatted("tooltip.zss.mode", mode.getDisplayName()));
		}
	}

	@Override
	public void handleFairyUpgrade(EntityItem item, EntityPlayer player, TileEntityDungeonCore core) {
		ItemStack stack = item.getEntityItem();
		int n = getLevel(stack);
		BlockPos pos = core.getPos();
		if (n < 3 && core.consumeRupees((n + 1) * Config.getHeroBowUpgradeCost())) {
			setLevel(stack, ++n);
			core.getWorld().playSoundEffect(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, Sounds.SECRET_MEDLEY, 1.0F, 1.0F);
			player.triggerAchievement(ZSSAchievements.fairyBow);
			if (n == 3) {
				player.triggerAchievement(ZSSAchievements.fairyBowMax);
			}
		} else {
			core.getWorld().playSoundEffect(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, Sounds.FAIRY_LAUGH, 1.0F, 1.0F);
			PlayerUtils.sendTranslatedChat(player, "chat.zss.fairy.laugh.unworthy");
		}
	}

	@Override
	public boolean hasFairyUpgrade(ItemStack stack) {
		return getLevel(stack) < 3;
	}

	@Method(modid="battlegear2")
	@Override
	public boolean allowOffhand(ItemStack main, ItemStack offhand, EntityPlayer player) {
		return offhand == null || offhand.getItem() instanceof ItemQuiver;
	}

	@Method(modid="battlegear2")
	@Override
	public boolean sheatheOnBack(ItemStack stack) {
		return true;
	}

	@Method(modid="battlegear2")
	@Override
	public List<IArrowFireHandler> getFireHandlers(ItemStack arrow, ItemStack bow, EntityPlayer player) {
		return fireHandlers;
	}

	@Method(modid="battlegear2")
	@Override
	public Result canDrawBow(ItemStack bow, EntityPlayer player) {
		ItemStack arrow = BattlegearEvents.getQuiverArrow(bow, player);
		return (canShootArrow(player, bow, arrow) ? Result.ALLOW : Result.DENY);
	}

	/**
	 * Applies vanilla arrow enchantments and sets critical if applicable
	 * NOTE: These settings are already added by BattleGear2 when shot from a quiver
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
	 * @param charge A value between 0.0F and 1.0F, inclusive
	 * @param arrowStack Determines arrow type (fire, ice, etc.) and the item that the arrow entity will drop
	 */
	private static final void applyCustomArrowSettings(EntityPlayer player, ItemStack bow, ItemStack arrowStack, EntityArrowCustom arrowEntity, float charge) {
		Item arrowItem = arrowStack.getItem();
		arrowEntity.setArrowItem(arrowItem);
		if (player.getCurrentArmor(ArmorIndex.WORN_HELM) != null && player.getCurrentArmor(ArmorIndex.WORN_HELM).getItem() == ZSSItems.maskHawkeye) {
			// Config.preferTargetingMobs() is a per-client setting; Hawkeye Mask will just assume it prefers enemies
			EntityLivingBase target = TargetUtils.acquireLookTarget(player, 64, 1.0F, false, IMob.class);
			if (target != null) {
				arrowEntity.setHomingArrow(true);
				arrowEntity.setTarget(target);
			}
		}
	}

	/**
	 * Returns the entity arrow appropriate for the id given, using the
	 * shooter and charge provided during construction
	 */
	@SuppressWarnings("finally")
	public static final EntityArrow getArrowEntity(ItemStack arrowStack, World world, EntityLivingBase shooter, float charge) {
		Class<? extends EntityArrow> clazz = ItemModeRegistry.ARROW_MODES.getEntityClass(arrowStack);
		if (clazz != null) {
			EntityArrow arrow = null;
			try {
				try {
					arrow = clazz.getConstructor(World.class, EntityLivingBase.class, float.class).newInstance(world, shooter, charge);
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

	/**
	 * Initialize arrow modes and bomb -> arrow item mappings; call any time after pre-init
	 */
	public static void initializeArrows() {
		// Bomb type to arrow mappings
		ItemHeroBow.bombArrowMap.put(BombType.BOMB_STANDARD, ZSSItems.arrowBomb);
		ItemHeroBow.bombArrowMap.put(BombType.BOMB_FIRE, ZSSItems.arrowBombFire);
		ItemHeroBow.bombArrowMap.put(BombType.BOMB_WATER, ZSSItems.arrowBombWater);
		// Arrow mode registry
		if (ItemModeRegistry.ARROW_MODES.size() > 0) {
			ZSSMain.logger.error("ItemHeroBow item modes were registered prior to the default ZSS modes; only register item modes during Post Init or later");
		}
		ItemModeRegistry.ARROW_MODES.register(null, null); // default 'no item selected' mode
		ItemModeRegistry.ARROW_MODES.register(new ItemStack(Items.arrow), EntityArrowCustom.class);
		ItemModeRegistry.ARROW_MODES.register(new ItemStack(ZSSItems.arrowBomb), EntityArrowBomb.class);
		ItemModeRegistry.ARROW_MODES.register(new ItemStack(ZSSItems.arrowBombFire), EntityArrowBombFire.class);
		ItemModeRegistry.ARROW_MODES.register(new ItemStack(ZSSItems.arrowBombWater), EntityArrowBombWater.class);
		ItemModeRegistry.ARROW_MODES.register(new ItemStack(ZSSItems.arrowFire), EntityArrowFire.class);
		ItemModeRegistry.ARROW_MODES.register(new ItemStack(ZSSItems.arrowIce), EntityArrowIce.class);
		ItemModeRegistry.ARROW_MODES.register(new ItemStack(ZSSItems.arrowLight), EntityArrowLight.class);
		ItemModeRegistry.ARROW_MODES.register(new ItemStack(ZSSItems.arrowSilver), EntityArrowSilver.class);
		// Add BG2 arrows
		if (ZSSMain.isBG2Enabled) {
			for (int i = 0; i < ItemMBArrow.names.length; ++i) {
				ItemModeRegistry.ARROW_MODES.register(new ItemStack(BattlegearConfig.MbArrows, 1, i), ItemMBArrow.arrows[i]);
			}
		}
	}

	/**
	 * Registers {@link HeroBowFireHandler} and all arrows required for use with
	 * Battlegear2's quiver system
	 */
	@Method(modid="battlegear2")
	public static void registerBG2() {
		fireHandlers.add(new HeroBowFireHandler());
		fireHandlers.add(new DefaultArrowFire());
		QuiverArrowRegistry.addArrowFireHandler(new HeroBowFireHandler());
		// registering as null prevents default fire handler from handling these arrows:
		QuiverArrowRegistry.addArrowToRegistry(ZSSItems.arrowBomb, null);
		QuiverArrowRegistry.addArrowToRegistry(ZSSItems.arrowBombFire, null);
		QuiverArrowRegistry.addArrowToRegistry(ZSSItems.arrowBombWater, null);
		QuiverArrowRegistry.addArrowToRegistry(ZSSItems.arrowFire, null);
		QuiverArrowRegistry.addArrowToRegistry(ZSSItems.arrowIce, null);
		QuiverArrowRegistry.addArrowToRegistry(ZSSItems.arrowLight, null);
		QuiverArrowRegistry.addArrowToRegistry(ZSSItems.arrowSilver, null);
	}

	/** 
	 * Handler for building ZSS arrows fired from a Battlegear2 quiver, including
	 * customized versions of the vanilla arrow.
	 * 
	 * ZSS arrows require an appropriately-leveled ItemHeroBow, or creative mode.
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
				EntityArrow arrowEntity = ItemHeroBow.getArrowEntity(arrow, world, player, charge);
				if (arrowEntity != null && (!(bow.getItem() instanceof ItemHeroBow) || ((ItemHeroBow) bow.getItem()).confirmArrowShot(arrow, player))) {
					// vanilla arrow settings will be applied by BG2's arrow loose event
					if (arrowEntity instanceof EntityArrowCustom) {
						ItemHeroBow.applyCustomArrowSettings(player, bow, arrow, (EntityArrowCustom) arrowEntity, charge);
					}
				}
				return arrowEntity;
			}
			return null;
		}
	}
}
