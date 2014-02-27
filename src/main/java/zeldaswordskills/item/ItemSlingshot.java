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

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemSeeds;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import net.minecraft.world.World;
import zeldaswordskills.api.item.IFairyUpgrade;
import zeldaswordskills.block.tileentity.TileEntityDungeonCore;
import zeldaswordskills.creativetab.ZSSCreativeTabs;
import zeldaswordskills.entity.ZSSPlayerInfo;
import zeldaswordskills.entity.projectile.EntitySeedShot;
import zeldaswordskills.entity.projectile.EntitySeedShot.SeedType;
import zeldaswordskills.lib.Config;
import zeldaswordskills.lib.ModInfo;
import zeldaswordskills.skills.SkillBase;
import zeldaswordskills.util.MerchantRecipeHelper;
import zeldaswordskills.util.PlayerUtils;
import zeldaswordskills.util.WorldUtils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * 
 * The slingshot can shoot any type of seed to inflict a small amount of damage.
 * 
 * Each type of seed will have a different effect, and some, such as the Deku Nut,
 * require a special type of slingshot (upgraded) to shoot properly.
 *
 */
public class ItemSlingshot extends Item implements IFairyUpgrade
{
	/** The number of seeds this slingshot will fire per shot */
	protected final int seedsFired;
	
	/** The angle between each seed fragment */
	protected final float spread;
	
	/** Maps the seed types to seed Items for consuming seed shot */
	private static final Map<SeedType, Integer> typeToSeed = new EnumMap(SeedType.class);
	
	public static void initializeSeeds(){
		typeToSeed.put(SeedType.COCOA, Item.dyePowder.itemID);
		typeToSeed.put(SeedType.DEKU, ZSSItems.dekuNut.itemID);
		typeToSeed.put(SeedType.GRASS, Item.seeds.itemID);
		typeToSeed.put(SeedType.MELON, Item.melonSeeds.itemID);
		typeToSeed.put(SeedType.NETHERWART, Item.netherStalkSeeds.itemID);
		typeToSeed.put(SeedType.PUMPKIN, Item.pumpkinSeeds.itemID);
	}

	public ItemSlingshot(int id) {
		this(id, 1, 0);
	}
	
	public ItemSlingshot(int id, int seedsFired, float spread) {
		super(id);
		this.seedsFired = seedsFired;
		this.spread = spread;
		setFull3D();
		setMaxDamage(0);
		setMaxStackSize(1);
		setCreativeTab(ZSSCreativeTabs.tabCombat);
	}
	
	@Override
	public boolean isItemTool(ItemStack stack) { return true; }

	@Override
	public int getItemEnchantability() { return 0; }

	@Override
	public int getMaxItemUseDuration(ItemStack stack) { return 72000; }

	@Override
	public EnumAction getItemUseAction(ItemStack stack) { return EnumAction.bow; }

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
			float damage = (type == SeedType.GRASS ? 1.0F : type == SeedType.NETHERWART || type == SeedType.DEKU ? 1.5F : 1.25F);
			
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
		
		world.playSoundAtEntity(player, "random.bow", 1.0F, 1.0F / (itemRand.nextFloat() * 0.4F + 1.2F) + f * 0.5F);
		if (!player.capabilities.isCreativeMode) {
			int seedId = typeToSeed.get(type);
			PlayerUtils.consumeInventoryItem(player, seedId, seedId == Item.dyePowder.itemID ? 3 : 0);
		}
	}
	
	@Override
	public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity entity) {
		if (!player.worldObj.isRemote && entity instanceof EntityVillager) {
			EntityVillager villager = (EntityVillager) entity;
			MerchantRecipeList trades = villager.getRecipes(player);
			if (trades != null) {
				MerchantRecipe trade = new MerchantRecipe(stack.copy(), new ItemStack(Item.emerald, 6 + (2 * seedsFired)));
				if (player.worldObj.rand.nextFloat() < 0.2F && MerchantRecipeHelper.addToListWithCheck(trades, trade)) {
					player.addChatMessage(StatCollector.translateToLocal("chat.zss.trade.generic.sell.0"));
				} else {
					player.addChatMessage(StatCollector.translateToLocal("chat.zss.trade.generic.sorry.1"));
				}
			} else {
				player.addChatMessage(StatCollector.translateToLocal("chat.zss.trade.generic.sorry.0"));
			}
		}
		return true;
	}
	
	/**
	 * Returns true if the player has any type of seed in the inventory
	 */
	protected boolean hasSeeds(EntityPlayer player) {
		if (player.capabilities.isCreativeMode) { return true; }
		for (ItemStack stack : player.inventory.mainInventory) {
			if (stack != null) {
				if (stack.getItem() instanceof ItemSeeds || stack.getItem() == ZSSItems.dekuNut) {
					return true;
				} else if (stack.getItem() == Item.dyePowder && stack.getItemDamage() == 3) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Returns the type of seed to be shot or NONE if no seed available
	 */
	protected SeedType getSeedType(EntityPlayer player) {
		for (ItemStack stack : player.inventory.mainInventory) {
			if (stack != null) {
				if (stack.getItem() == Item.seeds) {
					return SeedType.GRASS;
				} else if (stack.getItem() == Item.melonSeeds) {
					return SeedType.MELON;
				} else if (stack.getItem() == Item.netherStalkSeeds) {
					return SeedType.NETHERWART;
				} else if (stack.getItem() == Item.pumpkinSeeds) {
					return SeedType.PUMPKIN;
				} else if (stack.getItem() == Item.dyePowder && stack.getItemDamage() == 3) {
					return SeedType.COCOA;
				} else if (stack.getItem() == ZSSItems.dekuNut) {
					return SeedType.DEKU;
				}
			}
		}
		return (player.capabilities.isCreativeMode ? SeedType.GRASS : SeedType.NONE);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister register) {
		itemIcon = register.registerIcon(ModInfo.ID + ":" + getUnlocalizedName().substring(9));
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack itemstack, EntityPlayer player, List list, boolean isHeld) {
		list.add(EnumChatFormatting.ITALIC + StatCollector.translateToLocal("tooltip.zss.slingshot.desc.0"));
		if (seedsFired > 1) {
			list.add(EnumChatFormatting.ITALIC + StatCollector.translateToLocalFormatted("tooltip.zss.slingshot.desc.1", new Object[]{seedsFired}));
		}
	}
	
	@Override
	public void handleFairyUpgrade(EntityItem item, EntityPlayer player, TileEntityDungeonCore core) {
		ItemStack stack = item.getEntityItem();
		if (stack.getItem() == ZSSItems.slingshot && core.consumeRupees(Config.getSlingshotCostOne())) {
			item.setDead();
			WorldUtils.spawnItemWithRandom(core.getWorldObj(), new ItemStack(ZSSItems.scattershot), core.xCoord, core.yCoord + 2, core.zCoord);
			core.getWorldObj().playSoundEffect(core.xCoord + 0.5D, core.yCoord + 1, core.zCoord + 0.5D, ModInfo.SOUND_SECRET_MEDLEY, 1.0F, 1.0F);
		} else if (stack.getItem() == ZSSItems.scattershot && core.consumeRupees(Config.getSlingshotCostTwo())) {
			item.setDead();
			WorldUtils.spawnItemWithRandom(core.getWorldObj(), new ItemStack(ZSSItems.supershot), core.xCoord, core.yCoord + 2, core.zCoord);
			core.getWorldObj().playSoundEffect(core.xCoord + 0.5D, core.yCoord + 1, core.zCoord + 0.5D, ModInfo.SOUND_SECRET_MEDLEY, 1.0F, 1.0F);
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
		int hearts = ZSSPlayerInfo.get(player).getSkillLevel(SkillBase.bonusHeart);
		int divisor = (seedsFired == 1 ? 5 : seedsFired < 4 ? 7 : 10);
		int newLvl = Math.min((hearts / divisor), Enchantment.power.getMaxLevel());
		int lvl = newLvl;
		boolean flag = false;
		boolean playSound = false;

		if (stack.hasTagCompound() && stack.getTagCompound().hasKey("ench")) {
			NBTTagList enchList = (NBTTagList) stack.getTagCompound().getTag("ench");
			for (int i = 0; i < enchList.tagCount(); ++i) {
				NBTTagCompound compound = (NBTTagCompound) enchList.tagAt(i);
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
		if (playSound) {
			core.getWorldObj().playSoundEffect(core.xCoord + 0.5D, core.yCoord + 1, core.zCoord + 0.5D, ModInfo.SOUND_FAIRY_BLESSING, 1.0F, 1.0F);
		}
	}
}
