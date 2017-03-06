/**
    Copyright (C) <2017> <coolAlias>

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

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
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
import zeldaswordskills.api.item.IFairyUpgrade;
import zeldaswordskills.api.item.IUnenchantable;
import zeldaswordskills.api.item.IZoom;
import zeldaswordskills.block.tileentity.TileEntityDungeonCore;
import zeldaswordskills.creativetab.ZSSCreativeTabs;
import zeldaswordskills.entity.player.ZSSPlayerInfo;
import zeldaswordskills.entity.player.ZSSPlayerSkills;
import zeldaswordskills.entity.projectile.EntitySeedShot;
import zeldaswordskills.entity.projectile.EntitySeedShot.SeedType;
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
	public static enum Mode {
		/** Default Slingshot behavior searches for the first usable seed of any kind */
		DEFAULT(null),
		DEKU(SeedType.DEKU),
		BOMB(SeedType.BOMB),
		COCOA(SeedType.COCOA),
		GRASS(SeedType.GRASS),
		MELON(SeedType.MELON),
		PUMPKIN(SeedType.PUMPKIN),
		NETHERWART(SeedType.NETHERWART);
		private ItemStack seedStack;
		private final SeedType type;
		private Mode(SeedType type) {
			this.type = type;
		}
		/**
		 * Returns the seed itemstack required for this mode
		 */
		public ItemStack getSeedStack() {
			if (type != null) {
				if (ItemSlingshot.seedToType.isEmpty()) {
					ItemSlingshot.initializeSeeds();
				}
				Item item = ItemSlingshot.typeToSeed.get(type);
				if (item != null) {
					seedStack = new ItemStack(item, 1, item == Items.dye ? 3 : 0);
				}
			}
			return seedStack;
		}
		/**
		 * Returns the next Mode by ordinal position
		 */
		public Mode next() {
			return Mode.values()[(ordinal() + 1) % Mode.values().length];
		}
		/**
		 * Returns the previous Mode by ordinal position
		 */
		public Mode prev() {
			return Mode.values()[((ordinal() == 0 ? Mode.values().length : ordinal()) - 1) % Mode.values().length];
		}
	}

	/** The number of seeds this slingshot will fire per shot */
	protected final int seedsFired;

	/** The angle between each seed fragment */
	protected final float spread;

	/** Maps seed Items to seed Type (dye item must also check damage value) */
	private static final Map<Item, SeedType> seedToType = new HashMap<Item, SeedType>();

	/** Maps the seed types to seed Items for consuming seed shot */
	private static final Map<SeedType, Item> typeToSeed = new EnumMap<SeedType, Item>(SeedType.class);

	private static void initializeSeeds(){
		addSeedMapping(SeedType.BOMB, ZSSItems.bombFlowerSeed);
		addSeedMapping(SeedType.COCOA, Items.dye);
		addSeedMapping(SeedType.DEKU, ZSSItems.dekuNut);
		addSeedMapping(SeedType.GRASS, Items.wheat_seeds);
		addSeedMapping(SeedType.MELON, Items.melon_seeds);
		addSeedMapping(SeedType.NETHERWART, Items.nether_wart);
		addSeedMapping(SeedType.PUMPKIN, Items.pumpkin_seeds);
	}

	private static void addSeedMapping(SeedType type, Item item) {
		seedToType.put(item, type);
		typeToSeed.put(type, item);
	}

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

	public Mode getMode(EntityPlayer player) {
		return Mode.values()[ZSSPlayerInfo.get(player).slingshotMode % Mode.values().length];
	}

	private void setMode(EntityPlayer player, Mode mode) {
		ZSSPlayerInfo.get(player).slingshotMode = mode.ordinal();
	}

	@Override
	public void nextItemMode(ItemStack stack, EntityPlayer player) {
		if (!player.isUsingItem()) {
			setMode(player, getMode(player).next());
		}
	}

	@Override
	public void prevItemMode(ItemStack stack, EntityPlayer player) {
		if (!player.isUsingItem()) {
			setMode(player, getMode(player).prev());
		}
	}

	@Override
	public int getCurrentMode(ItemStack stack, EntityPlayer player) {
		return getMode(player).ordinal();
	}

	@Override
	public void setCurrentMode(ItemStack stack, EntityPlayer player, int mode) {
		setMode(player, Mode.values()[mode % Mode.values().length]);
	}

	@Override
	public ItemStack getRenderStackForMode(ItemStack stack, EntityPlayer player) {
		ItemStack ret = getMode(player).getSeedStack();
		if (ret != null) {
			ret.stackSize = 0;
			for (ItemStack inv : player.inventory.mainInventory) {
				if (inv != null && inv.getItem() == ret.getItem() && inv.getItemDamage() == ret.getItemDamage()) {
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
		if (hasSeeds(player)) {
			player.setItemInUse(stack, getMaxItemUseDuration(stack));
		}
		return stack;
	}

	@Override
	public void onPlayerStoppedUsing(ItemStack stack, World world, EntityPlayer player, int ticksUsed) {
		int charge = getMaxItemUseDuration(stack) - ticksUsed;
		float f = (float) charge / 20.0F;
		f = (f * f + f * 2.0F) / 3.0F;
		SeedType type = getSeedType(player);
		if (f < 0.3F || type == SeedType.NONE) {
			return;
		} else if (f > 1.0F) {
			f = 1.0F;
		}
		for (int i = 0; i < seedsFired; ++i) {
			EntitySeedShot seedShot = new EntitySeedShot(world, player, f, i + 1, spread).setType(type);
			if (f == 1.0F) {
				seedShot.setIsCritical(true);
			}
			float factor = (seedsFired == 1 ? 2.2F : seedsFired < 4 ? 1.4F : 1.0F); 
			float damage = type.getDamage();
			int k = EnchantmentHelper.getEnchantmentLevel(Enchantment.power.effectId, stack);
			if (k > 0) {
				damage += ((float)(k * 0.25F) + 0.25F);
			}
			seedShot.setDamage(damage * factor);
			int l = EnchantmentHelper.getEnchantmentLevel(Enchantment.punch.effectId, stack);
			seedShot.setKnockback(l > 0 ? l : (type == SeedType.MELON ? 1 : 0));
			if (type == SeedType.NETHERWART || EnchantmentHelper.getEnchantmentLevel(Enchantment.flame.effectId, stack) > 0) {
				seedShot.setFire(100);
			}
			if (!world.isRemote) {
				world.spawnEntityInWorld(seedShot);
			}
		}
		world.playSoundAtEntity(player, Sounds.BOW_RELEASE, 1.0F, 1.0F / (itemRand.nextFloat() * 0.4F + 1.2F) + f * 0.5F);
		if (!player.capabilities.isCreativeMode) {
			Item seed = typeToSeed.get(type);
			PlayerUtils.consumeInventoryItem(player, seed, seed == Items.dye ? 3 : 0, 1);
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
	 * Returns true if the player has any type of seed in the inventory
	 */
	protected boolean hasSeeds(EntityPlayer player) {
		if (player.capabilities.isCreativeMode) { return true; }
		return getSeedType(player) != SeedType.NONE;
	}

	/**
	 * Returns the type of seed to be shot or SeedType.NONE if no seed available
	 */
	protected SeedType getSeedType(EntityPlayer player) {
		if (seedToType.isEmpty()) {
			ItemSlingshot.initializeSeeds();
		}
		SeedType selected = getMode(player).type;
		for (ItemStack stack : player.inventory.mainInventory) {
			if (stack != null && seedToType.containsKey(stack.getItem())) {
				SeedType type = seedToType.get(stack.getItem());
				if ((type != SeedType.COCOA || stack.getItemDamage() == 3) && (selected == null || type == selected)) {
					return type;
				}
			}
		}
		return (player.capabilities.isCreativeMode ? (selected == null ? SeedType.GRASS : selected) : SeedType.NONE);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, EntityPlayer player, List<String> list, boolean advanced) {
		list.add(EnumChatFormatting.ITALIC + StatCollector.translateToLocal("tooltip.zss.slingshot.desc.0"));
		if (seedsFired > 1) {
			list.add(EnumChatFormatting.ITALIC + StatCollector.translateToLocalFormatted("tooltip.zss.slingshot.desc.1", seedsFired));
		}
		ItemStack mode = getMode(player).getSeedStack();
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
}
