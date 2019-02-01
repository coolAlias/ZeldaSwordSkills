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

import java.util.List;

import net.minecraft.block.material.Material;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.BlockPos;
import net.minecraft.util.StatCollector;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import zeldaswordskills.api.item.ArmorIndex;
import zeldaswordskills.creativetab.ZSSCreativeTabs;
import zeldaswordskills.entity.ZSSEntityInfo;
import zeldaswordskills.entity.buff.Buff;
import zeldaswordskills.entity.npc.EntityGoron;
import zeldaswordskills.ref.ModInfo;
import zeldaswordskills.ref.Sounds;
import zeldaswordskills.util.MerchantRecipeHelper;
import zeldaswordskills.util.PlayerUtils;

/**
 * 
 * Link's various tunics. Despite being made out of home-spun cloth, they all provide protection
 * equivalent to chain mail. They have the same enchantibility as cloth, as well as using cloth
 * for anvil repairs.
 * 
 * Only the Hero's clothes include a full set; the other two are tunics only.
 * 
 * Hero's Tunic: standard green clothes worn by Link
 * Goron Tunic: special red tunic made from the scales of lava-dwelling Dodongos;
 * 		prevents all fire damage
 * Zora Tunic: special blue tunic allows wearer to breathe under water
 *
 */
public class ItemArmorTunic extends ItemModArmor
{
	/** Effect to add every 50 ticks */
	protected PotionEffect tickingEffect = null;

	/**
	 * Armor types as used on player: 0 boots, 1 legs, 2 chest, 3 helm
	 * Armor types as used in armor class: 0 helm, 1 chest, 2 legs, 3 boots
	 */
	public ItemArmorTunic(int renderIndex, int type) {
		super(ArmorMaterial.CHAIN, renderIndex, type);
		setCreativeTab(ZSSCreativeTabs.tabCombat);
	}

	/**
	 * Sets this piece of armor to grant the specified potion effect every 50 ticks
	 */
	public ItemArmorTunic setEffect(PotionEffect effect) {
		tickingEffect = effect;
		return this;
	}

	/**
	 * Returns this armor's ticking potion effect or null if it doesn't have one
	 */
	public PotionEffect getEffect() {
		return tickingEffect;
	}

	@Override
	public String getArmorTexture(ItemStack stack, Entity entity, int slot, String type) {
		String name = getUnlocalizedName();
		name = name.substring(name.lastIndexOf(".") + 1, name.lastIndexOf("_"));
		return String.format("%s:textures/armor/%s_layer_%d.png", ModInfo.ID, name, (slot == 2 ? 2 : 1));
	}

	@Override
	public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity entity) {
		if (!player.worldObj.isRemote && this == ZSSItems.tunicGoronChest && entity instanceof EntityGoron) {
			if (stack.getItemDamage() > 0) {
				MerchantRecipe trade = new MerchantRecipe(new ItemStack(ZSSItems.tunicGoronChest), new ItemStack(Items.emerald, 8), new ItemStack(ZSSItems.tunicGoronChest));
				MerchantRecipeHelper.addToListWithCheck(((EntityVillager) entity).getRecipes(player), trade);
				PlayerUtils.sendTranslatedChat(player, "chat.zss.trade.goron.tunic.repair");
			} else {
				PlayerUtils.sendTranslatedChat(player, "chat.zss.trade.goron.tunic.undamaged");
			}
		}
		return true;
	}

	@Override
	public void onArmorTick(World world, EntityPlayer player, ItemStack stack) {
		PotionEffect effect = getEffect();
		if (effect != null && shouldDamageArmor(world, player, stack, effect.getPotionID())) {
			player.addPotionEffect(new PotionEffect(effect));
			player.setAir(300);
			damageStack(stack, player, 1);
		}
	}

	/**
	 * Call from LivingAttackEvent when entity attacked by fire damage
	 * Checks if wearing Goron Tunic and negates damage if appropriate
	 * @return true if attack event should be canceled
	 */
	public static boolean onFireDamage(EntityLivingBase entity, float damage) {
		ItemStack stack = entity.getEquipmentInSlot(ArmorIndex.EQUIPPED_CHEST);
		if (!entity.worldObj.isRemote && stack != null && stack.getItem() == ZSSItems.tunicGoronChest) {
			if (entity.isPotionActive(Potion.fireResistance.getId())) {
				return true;
			}
			damage *= 1.0F + (ZSSEntityInfo.get(entity).getBuffAmplifier(Buff.WEAKNESS_FIRE) * 0.01F);
			damage *= 1.0F - (ZSSEntityInfo.get(entity).getBuffAmplifier(Buff.RESIST_FIRE) * 0.01F);
			if (damage < 0.1F) {
				entity.extinguish();
				return true;
			}
			if (!stack.hasTagCompound()) {
				stack.setTagCompound(new NBTTagCompound());
			}
			if (!stack.getTagCompound().hasKey("lastDamaged") || entity.worldObj.getTotalWorldTime() > (stack.getTagCompound().getLong("lastDamaged") + 20)) {
				stack.getTagCompound().setLong("lastDamaged", entity.worldObj.getTotalWorldTime());
				((ItemArmorTunic) stack.getItem()).damageStack(stack, entity, Math.max((int) damage / 4, 1));
				entity.extinguish();
			}

			return true;
		}
		return false;
	}

	/**
	 * Damages stack for amount, destroying if applicable
	 */
	private void damageStack(ItemStack stack, EntityLivingBase entity, int amount) {
		stack.damageItem(amount, entity);
		if (stack.stackSize == 0 || stack.getItemDamage() >= stack.getMaxDamage()) {
			entity.worldObj.playSoundAtEntity(entity, Sounds.ITEM_BREAK, 1.0F, 1.0F);
			entity.setCurrentItemOrArmor(EntityLiving.getArmorPosition(stack), null);
		}
	}

	/**
	 * Returns true if the armor should be damaged this tick for applying potion effect
	 */
	private boolean shouldDamageArmor(World world, EntityPlayer player, ItemStack stack, int effectID) {
		Material m = world.getBlockState(new BlockPos(player).up()).getBlock().getMaterial();
		if (effectID == Potion.waterBreathing.id) {
			if (player.isPotionActive(Potion.waterBreathing.getId())) {
				return false;
			}
			ItemStack helm = player.getCurrentArmor(ArmorIndex.WORN_HELM);
			if (helm != null && helm.getItem() == ZSSItems.maskZora) {
				return false;
			}
			int time = 50 + (50 * EnchantmentHelper.getRespiration(player)) + (helm != null && helm.getItem() == ZSSItems.tunicZoraHelm ? 100 : 0);
			return (m == Material.water && world.getTotalWorldTime() % time == 0);
		}
		return false;
	}

	@Override
	public int getItemEnchantability() {
		return ArmorMaterial.LEATHER.getEnchantability();
	}

	@Override
	public boolean getIsRepairable(ItemStack toRepair, ItemStack stack) {
		if (this == ZSSItems.tunicGoronChest) {
			// TODO return dodongo scales
			return stack.getItem() == Items.magma_cream;
		} else if (this == ZSSItems.tunicZoraChest) {
			// TODO return something interesting?
		}
		return stack.getItem() == Item.getItemFromBlock(Blocks.wool);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, EntityPlayer player, List<String> list, boolean advanced) {
		String[] tooltips = StatCollector.translateToLocal("tooltip." + getUnlocalizedName().substring(5) + ".desc").split("\\\\n");
		for (String tooltip : tooltips) {
			list.add(tooltip);
		}
	}
}
