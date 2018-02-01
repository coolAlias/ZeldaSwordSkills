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

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import zeldaswordskills.ZSSAchievements;
import zeldaswordskills.ZSSMain;
import zeldaswordskills.api.item.ICyclableItem;
import zeldaswordskills.api.item.IFairyUpgrade;
import zeldaswordskills.api.item.IUnenchantable;
import zeldaswordskills.api.item.IZoom;
import zeldaswordskills.api.item.ItemModeRegistry;
import zeldaswordskills.block.tileentity.TileEntityDungeonCore;
import zeldaswordskills.creativetab.ZSSCreativeTabs;
import zeldaswordskills.entity.player.ZSSPlayerSkills;
import zeldaswordskills.entity.projectile.EntitySeedShot;
import zeldaswordskills.entity.projectile.EntitySeedShotBomb;
import zeldaswordskills.entity.projectile.EntitySeedShotCocoa;
import zeldaswordskills.entity.projectile.EntitySeedShotDeku;
import zeldaswordskills.entity.projectile.EntitySeedShotMelon;
import zeldaswordskills.entity.projectile.EntitySeedShotNetherwart;
import zeldaswordskills.entity.projectile.EntitySeedShotPumpkin;
import zeldaswordskills.ref.Config;
import zeldaswordskills.ref.Sounds;
import zeldaswordskills.skills.SkillBase;
import zeldaswordskills.util.MerchantRecipeHelper;
import zeldaswordskills.util.PlayerUtils;
import zeldaswordskills.util.WorldUtils;

/**
 * 
 * The slingshot can shoot any type of seed to inflict a small amount of damage.
 * 
 * Each type of seed will have a different effect, and some, such as the Deku Nut,
 * require a special type of slingshot (upgraded) to shoot properly.
 *
 */
public class ItemSlingshot extends BaseModItem implements ICyclableItem, IFairyUpgrade, IUnenchantable, IZoom
{
	/** The number of seeds this slingshot will fire per shot */
	protected final int seedsFired;

	/** The angle between each seed fragment */
	protected final float spread;

	public ItemSlingshot() {
		this(1, 0);
	}

	public ItemSlingshot(int seedsFired, float spread) {
		super();
		this.seedsFired = seedsFired;
		this.spread = spread;
		setFull3D();
		setMaxDamage(0);
		setMaxStackSize(1);
		setCreativeTab(ZSSCreativeTabs.tabCombat);
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
			int current = this.getMode(stack);
			int mode = current;
			do {
				mode = ItemModeRegistry.SEED_MODES.next(mode);
			} while (mode != current && this.skipMode(mode, player));
			this.setMode(stack, mode);
		}
	}

	@Override
	public void prevItemMode(ItemStack stack, EntityPlayer player) {
		if (!player.isUsingItem()) {
			int current = this.getMode(stack);
			int mode = current;
			do {
				mode = ItemModeRegistry.SEED_MODES.prev(mode);
			} while (mode != current && this.skipMode(mode, player));
			this.setMode(stack, mode);
		}
	}

	private boolean skipMode(int mode, EntityPlayer player) {
		if (player.capabilities.isCreativeMode) {
			return false;
		}
		ItemStack stack = ItemModeRegistry.SEED_MODES.getStack(mode);
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
		ItemStack render = ItemModeRegistry.SEED_MODES.getStack(this.getMode(stack));
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
	public int getItemEnchantability() {
		return 0;
	}

	@Override
	public int getMaxItemUseDuration(ItemStack stack) {
		return 72000;
	}

	@Override
	public EnumAction getItemUseAction(ItemStack stack) {
		return EnumAction.BOW;
	}

	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
		if (this.getSeedsFromInventory(stack, player) != null) {
			player.setItemInUse(stack, getMaxItemUseDuration(stack));
		}
		return stack;
	}

	@Override
	public void onPlayerStoppedUsing(ItemStack stack, World world, EntityPlayer player, int ticksUsed) {
		ItemStack shot = this.getSeedsFromInventory(stack, player);
		int charge = getMaxItemUseDuration(stack) - ticksUsed;
		float f = (float) charge / 20.0F;
		f = (f * f + f * 2.0F) / 3.0F;
		if (f < 0.3F || shot == null) {
			return;
		} else if (f > 1.0F) {
			f = 1.0F;
		}
		for (int i = 0; i < this.seedsFired; ++i) {
			EntitySeedShot seedShot = ItemSlingshot.getShotEntity(shot, world, player, f, i + 1, this.spread);
			if (seedShot == null) {
				if (i > 0) {
					continue;
				} else {
					return;
				}
			}
			if (f == 1.0F) {
				seedShot.setIsCritical(true);
			}
			// Damage
			float factor = (this.seedsFired == 1 ? 2.2F : this.seedsFired < 4 ? 1.4F : 1.0F); 
			float damage = seedShot.getDamage();
			int k = EnchantmentHelper.getEnchantmentLevel(Enchantment.power.effectId, stack);
			if (k > 0) {
				damage += ((float)(k * 0.25F) + 0.25F);
			}
			seedShot.setDamage(damage * factor);
			// Knockback
			int l = EnchantmentHelper.getEnchantmentLevel(Enchantment.punch.effectId, stack);
			seedShot.setKnockback(l + seedShot.getKnockback());
			// Flaming
			if (EnchantmentHelper.getEnchantmentLevel(Enchantment.flame.effectId, stack) > 0) {
				seedShot.setFire(100);
			}
			if (!world.isRemote) {
				world.spawnEntityInWorld(seedShot);
			}
		}
		world.playSoundAtEntity(player, Sounds.BOW_RELEASE, 1.0F, 1.0F / (itemRand.nextFloat() * 0.4F + 1.2F) + f * 0.5F);
		if (!player.capabilities.isCreativeMode) {
			PlayerUtils.consumeInventoryItem(player, shot, 1);
		}
	}

	@Override
	public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity entity) {
		if (!player.worldObj.isRemote && entity instanceof EntityVillager) {
			EntityVillager villager = (EntityVillager) entity;
			MerchantRecipeList trades = villager.getRecipes(player);
			if (trades != null) {
				MerchantRecipe trade = new MerchantRecipe(stack.copy(), new ItemStack(Items.emerald, 6 + (2 * seedsFired)));
				if (player.worldObj.rand.nextFloat() < 0.2F && MerchantRecipeHelper.addToListWithCheck(trades, trade)) {
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

	/**
	 * Returns the first available seed shot from the player's inventory, with preference for selected mode
	 */
	private ItemStack getSeedsFromInventory(ItemStack stack, EntityPlayer player) {
		ItemStack selected = ItemModeRegistry.SEED_MODES.getStack(this.getMode(stack));
		// Search specifically for the selected shot type
		if (selected != null) {
			if (player.capabilities.isCreativeMode || PlayerUtils.hasItem(player, selected)) {
				return selected;
			}
		}
		// No mode selected or seed could not be shot - search inventory for usable seed shot
		for (ItemStack invStack : player.inventory.mainInventory) {
			if (invStack != null && ItemModeRegistry.SEED_MODES.contains(invStack)) {
				return invStack;
			}
		}
		// No seed shot found - return default grass seed shot if in Creative
		return (player.capabilities.isCreativeMode ? new ItemStack(Items.wheat_seeds) : null);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, EntityPlayer player, List<String> list, boolean advanced) {
		list.add(EnumChatFormatting.ITALIC + StatCollector.translateToLocal("tooltip.zss.slingshot.desc.0"));
		if (seedsFired > 1) {
			list.add(EnumChatFormatting.ITALIC + StatCollector.translateToLocalFormatted("tooltip.zss.slingshot.desc.1", seedsFired));
		}
		ItemStack mode = ItemModeRegistry.SEED_MODES.getStack(this.getMode(stack));
		if (mode != null) {
			list.add(EnumChatFormatting.YELLOW + StatCollector.translateToLocalFormatted("tooltip.zss.mode", mode.getDisplayName()));
		}
	}

	@Override
	public void handleFairyUpgrade(EntityItem item, EntityPlayer player, TileEntityDungeonCore core) {
		ItemStack stack = item.getEntityItem();
		BlockPos pos = core.getPos();
		if (stack.getItem() == ZSSItems.slingshot && core.consumeRupees(Config.getSlingshotCostOne())) {
			item.setDead();
			player.triggerAchievement(ZSSAchievements.fairySlingshot);
			WorldUtils.spawnItemWithRandom(core.getWorld(), new ItemStack(ZSSItems.scattershot), pos.getX(), pos.getY() + 2, pos.getZ());
			core.getWorld().playSoundEffect(pos.getX() + 0.5D, pos.getY() + 1, pos.getZ() + 0.5D, Sounds.SECRET_MEDLEY, 1.0F, 1.0F);
		} else if (stack.getItem() == ZSSItems.scattershot && core.consumeRupees(Config.getSlingshotCostTwo())) {
			item.setDead();
			player.triggerAchievement(ZSSAchievements.fairySupershot);
			WorldUtils.spawnItemWithRandom(core.getWorld(), new ItemStack(ZSSItems.supershot), pos.getX(), pos.getY() + 2, pos.getZ());
			core.getWorld().playSoundEffect(pos.getX() + 0.5D, pos.getY() + 1, pos.getZ() + 0.5D, Sounds.SECRET_MEDLEY, 1.0F, 1.0F);
		} else {
			addFairyEnchantments(stack, player, core);
		}
	}

	@Override
	public boolean hasFairyUpgrade(ItemStack stack) {
		return true;
	}

	/**
	 * Checks for and adds any applicable fairy enchantments to the slingshot
	 */
	private void addFairyEnchantments(ItemStack stack, EntityPlayer player, TileEntityDungeonCore core) {
		int hearts = ZSSPlayerSkills.get(player).getSkillLevel(SkillBase.bonusHeart);
		int divisor = (seedsFired == 1 ? 5 : seedsFired < 4 ? 7 : 10);
		int newLvl = Math.min((hearts / divisor), Enchantment.power.getMaxLevel());
		int lvl = newLvl;
		boolean flag = false;
		boolean playSound = false;
		if (stack.hasTagCompound() && stack.getTagCompound().hasKey("ench")) {
			NBTTagList enchList = (NBTTagList) stack.getTagCompound().getTag("ench");
			for (int i = 0; i < enchList.tagCount(); ++i) {
				NBTTagCompound compound = (NBTTagCompound) enchList.getCompoundTagAt(i);
				if (compound.getShort("id") == Enchantment.power.effectId) {
					int oldLvl = compound.getShort("lvl");
					lvl = newLvl - oldLvl;
					while (lvl > 0 && core.consumeRupees(divisor * 2)) {
						--lvl;
					}
					newLvl -= lvl;
					if (newLvl > oldLvl) {
						enchList.removeTag(i);
						stack.addEnchantment(Enchantment.power, newLvl);
						playSound = true;
					}
					flag = true;
					break;
				}
			}
		}
		if (!flag) {
			while (lvl > 0 && core.consumeRupees(divisor * 2)) {
				--lvl;
			}
			newLvl -= lvl;
			if (newLvl > 0) {
				playSound = true;
				stack.addEnchantment(Enchantment.power, newLvl);
			}
		}
		BlockPos pos = core.getPos();
		if (playSound) {
			player.triggerAchievement(ZSSAchievements.fairyEnchantment);
			core.getWorld().playSoundEffect(pos.getX() + 0.5D, pos.getY() + 1, pos.getZ() + 0.5D, Sounds.FAIRY_BLESSING, 1.0F, 1.0F);
		} else {
			core.getWorld().playSoundEffect(pos.getX() + 0.5D, pos.getY() + 1, pos.getZ() + 0.5D, Sounds.FAIRY_LAUGH, 1.0F, 1.0F);
			PlayerUtils.sendTranslatedChat(player, "chat.zss.fairy.laugh.unworthy");
		}
	}

	public static void initializeSeeds() {
		if (ItemModeRegistry.SEED_MODES.size() > 0) {
			ZSSMain.logger.error("Slingshot item modes were registered prior to the default ZSS modes; only register item modes during Post Init or later");
		}
		ItemModeRegistry.SEED_MODES.register(null, null); // default 'no item selected' mode
		ItemModeRegistry.SEED_MODES.register(new ItemStack(Items.wheat_seeds), EntitySeedShot.class);
		ItemModeRegistry.SEED_MODES.register(new ItemStack(ZSSItems.bombFlowerSeed), EntitySeedShotBomb.class);
		ItemModeRegistry.SEED_MODES.register(new ItemStack(Items.dye, 1, 3), EntitySeedShotCocoa.class);
		ItemModeRegistry.SEED_MODES.register(new ItemStack(ZSSItems.dekuNut), EntitySeedShotDeku.class);
		ItemModeRegistry.SEED_MODES.register(new ItemStack(Items.melon_seeds), EntitySeedShotMelon.class);
		ItemModeRegistry.SEED_MODES.register(new ItemStack(Items.nether_wart), EntitySeedShotNetherwart.class);
		ItemModeRegistry.SEED_MODES.register(new ItemStack(Items.pumpkin_seeds), EntitySeedShotPumpkin.class);
	}

	/**
	 * Returns the entity arrow appropriate for the id given, using the
	 * shooter and charge provided during construction
	 */
	@SuppressWarnings("finally")
	public static final EntitySeedShot getShotEntity(ItemStack stack, World world, EntityLivingBase shooter, float charge, int n, float spread) {
		Class<? extends EntitySeedShot> clazz = ItemModeRegistry.SEED_MODES.getEntityClass(stack);
		if (clazz != null) {
			EntitySeedShot shot = null;
			try {
				try {
					shot = clazz.getConstructor(World.class, EntityLivingBase.class, float.class, int.class, float.class).newInstance(world, shooter, charge, n, spread);
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
				return shot;
			}
		} else {
			return null;
		}
	}
}
