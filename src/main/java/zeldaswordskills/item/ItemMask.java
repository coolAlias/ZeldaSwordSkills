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

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import net.minecraft.entity.DirtyEntityAccessor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.INpc;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import zeldaswordskills.ZSSAchievements;
import zeldaswordskills.api.entity.CustomExplosion;
import zeldaswordskills.api.item.ArmorIndex;
import zeldaswordskills.api.item.IUnenchantable;
import zeldaswordskills.api.item.IZoomHelper;
import zeldaswordskills.creativetab.ZSSCreativeTabs;
import zeldaswordskills.entity.ZSSEntityInfo;
import zeldaswordskills.entity.ZSSPlayerInfo;
import zeldaswordskills.entity.ZSSVillagerInfo;
import zeldaswordskills.entity.ZSSVillagerInfo.EnumVillager;
import zeldaswordskills.entity.buff.Buff;
import zeldaswordskills.entity.npc.EntityNpcMaskTrader;
import zeldaswordskills.entity.projectile.EntityBomb;
import zeldaswordskills.ref.Sounds;
import zeldaswordskills.util.PlayerUtils;
import zeldaswordskills.util.TimedChatDialogue;

/**
 * 
 * All types of Masks should use this class
 *
 */
public class ItemMask extends ItemModArmor implements IUnenchantable, IZoomHelper
{
	/** Effect to add every 50 ticks */
	protected PotionEffect tickingEffect = null;

	/** Number of rupees to pay back to the Happy Mask Salesman */
	private int buyPrice;

	/** Number of rupees a villager will pay for this mask */
	private int sellPrice;

	/** Movement bonus for wearing the Bunny Hood */
	public static final UUID bunnyHoodMoveBonusUUID = UUID.fromString("8412C9F7-9645-4C24-8FD1-6EFB8282E822");
	public static final AttributeModifier bunnyHoodMoveBonus = (new AttributeModifier(bunnyHoodMoveBonusUUID, "Bunny Hood Speed Bonus", 0.3D, 2)).setSaved(false);

	/**
	 * Armor types as used on player: 0 boots, 1 legs, 2 chest, 3 helm
	 * Armor types as used in armor class: 0 helm, 1 chest, 2 legs, 3 boots
	 */
	public ItemMask(ArmorMaterial material, int renderIndex) {
		super(material, renderIndex, ArmorIndex.TYPE_HELM);
		setMaxDamage(0);
		setCreativeTab(ZSSCreativeTabs.tabMasks);
	}

	/**
	 * Sets the buy and sell price for this mask; only for masks in the trading sequence
	 * @param buy price the player will pay for the mask
	 * @param sell price the player will receive for selling the mask
	 */
	public ItemMask setPrice(int buy, int sell) {
		buyPrice = buy;
		sellPrice = sell;
		return this;
	}

	/** The number of rupees to pay back to the Happy Mask Salesman */
	public int getBuyPrice() {
		return buyPrice > 0 ? buyPrice : 16;
	}

	/** The number of rupees a villager will pay for this mask */
	public int getSellPrice() {
		return sellPrice > 0 ? sellPrice : 16;
	}

	/**
	 * Sets this mask to grant the specified potion effect every 50 ticks
	 */
	public ItemMask setEffect(PotionEffect effect) {
		tickingEffect = effect;
		return this;
	}

	/**
	 * Used by the Blast Mask to cause an explosion; sets a short cooldown on the stack
	 */
	public void explode(EntityPlayer player, ItemStack stack, World world, double x, double y, double z) {
		if (this == ZSSItems.maskBlast) {
			if (!player.capabilities.isCreativeMode && isCooling(world, stack)) {
				world.playSoundEffect(x, y, z, Sounds.CLICK, 0.3F, 0.6F);
			} else {
				CustomExplosion.createExplosion(new EntityBomb(world), world, x, y, z, 3.0F, 10.0F, false);
				setNextUse(world, stack, 40);
			}
		}
	}

	/** Returns true if this Mask is cooling down */
	private boolean isCooling(World world, ItemStack stack) {
		return (stack.hasTagCompound() && world.getWorldTime() < stack.getTagCompound().getInteger("nextUse"));
	}

	/**
	 * Sets the time, in ticks, which must pass before the stack may be used again
	 */
	private void setNextUse(World world, ItemStack stack, int time) {
		if (!stack.hasTagCompound()) { stack.setTagCompound(new NBTTagCompound()); }
		stack.getTagCompound().setLong("nextUse", world.getWorldTime() + time);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public float getMagnificationFactor() {
		return (this == ZSSItems.maskHawkeye ? 3.0F : 0.0F);
	}

	@Override
	public void onArmorTick(World world, EntityPlayer player, ItemStack stack) {
		ZSSPlayerInfo info = ZSSPlayerInfo.get(player);
		if (!info.getFlag(ZSSPlayerInfo.IS_WEARING_HELM)) {
			info.setWearingHelm();
		}
		if (tickingEffect != null && world.getWorldTime() % 50 == 0) {
			player.addPotionEffect(new PotionEffect(tickingEffect));
		}
	}

	@Override
	public int getItemEnchantability() {
		return 0;
	}

	@Override
	public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity entity) {
		if (!player.worldObj.isRemote && entity instanceof EntityVillager) {
			EntityVillager villager = (EntityVillager) entity;
			if (ZSSVillagerInfo.get(villager).getMaskDesired() == this) {
				// TODO ? villager.setCurrentItemOrArmor(ArmorIndex.EQUIPPED_HELM, new ItemStack(this));
				ZSSVillagerInfo.get(villager).onMaskTrade();
				ZSSPlayerInfo.get(player).completeCurrentMaskStage();
				player.setCurrentItemOrArmor(0, new ItemStack(Items.emerald, getSellPrice()));
				PlayerUtils.playSound(player, Sounds.CASH_SALE, 1.0F, 1.0F);
				PlayerUtils.sendTranslatedChat(player, "chat.zss.mask.sold." + player.worldObj.rand.nextInt(4));
				player.triggerAchievement(ZSSAchievements.maskSold);
			}
		}
		return true;
	}

	/**
	 * Called when the player right-clicks on an entity while wearing a mask
	 * @return returning true will cancel the interact event (preventing trade
	 * gui from opening for villagers, for example)
	 */
	public boolean onInteract(ItemStack stack, EntityPlayer player, Entity entity) {
		if (!player.worldObj.isRemote) {
			if (entity instanceof EntityVillager) {
				EntityVillager villager = (EntityVillager) entity;
				if (entity.getClass().isAssignableFrom(EntityVillager.class) && this == ZSSVillagerInfo.get(villager).getMaskDesired()) {
					new TimedChatDialogue(player, Arrays.asList(StatCollector.translateToLocal("chat.zss.mask.desired.0"),
							StatCollector.translateToLocalFormatted("chat.zss.mask.desired.1", getSellPrice())));
				} else {
					// Custom villager professions all use the same chat message
					int p = villager.getProfession();
					String s = (p < 0 || p > EnumVillager.values().length) ? "custom" : String.valueOf(p);
					PlayerUtils.sendTranslatedChat(player, "chat." + getUnlocalizedName().substring(5) + "." + s);
				}
			} else if (entity instanceof EntityNpcMaskTrader) {
				PlayerUtils.sendTranslatedChat(player, "chat." + getUnlocalizedName().substring(5) + ".salesman");
			} else if (entity instanceof INpc) {
				PlayerUtils.sendTranslatedChat(player, "chat." + getUnlocalizedName().substring(5) + ".custom");
			} else {
				return false;
			}
		}
		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack,	EntityPlayer player, List list, boolean par4) {
		list.add(EnumChatFormatting.ITALIC + StatCollector.translateToLocal("tooltip." + getUnlocalizedName().substring(5) + ".desc.0"));
	}

	/**
	 * Applies or removes attribute modifiers for masks when equipped or unequipped
	 */
	public static void applyAttributeModifiers(ItemStack stack, EntityPlayer player) {
		if (stack != null && ZSSPlayerInfo.get(player).getFlag(ZSSPlayerInfo.IS_WEARING_HELM)) {
			ItemMask.removeAttributeModifiers(stack, player);
			if (((ItemMask) stack.getItem()).tickingEffect != null) {
				player.addPotionEffect(new PotionEffect(((ItemMask) stack.getItem()).tickingEffect));
			}
			if (stack.getItem() == ZSSItems.maskBunny) {
				player.getEntityAttribute(SharedMonsterAttributes.movementSpeed).applyModifier(bunnyHoodMoveBonus);
				ZSSPlayerInfo.get(player).setFlag(ZSSPlayerInfo.MOBILITY, true);
				ZSSEntityInfo.get(player).applyBuff(Buff.EVADE_UP, Integer.MAX_VALUE, 25);
			} else if (stack.getItem() == ZSSItems.maskGiants) {
				/*
				// TODO
				DirtyEntityAccessor.setSize(player, player.width * 3.0F, player.height * 3.0F);
				if (player.worldObj.isRemote) {
					player.stepHeight += 1.0F;
				}
				 */
			} else if (stack.getItem() == ZSSItems.maskGoron) {
				ZSSEntityInfo.get(player).applyBuff(Buff.RESIST_FIRE, Integer.MAX_VALUE, 100);
			} else if (stack.getItem() == ZSSItems.maskMajora) {
				ZSSEntityInfo.get(player).applyBuff(Buff.ATTACK_UP, Integer.MAX_VALUE, 100);
			}
		}
	}

	/**
	 * Remove modifiers provided by the given stack
	 * @param stack ItemMask being unequipped
	 */
	public static void removeAttributeModifiers(ItemStack stack, EntityPlayer player) {
		if (stack == null) { return; }
		if (stack.getItem() == ZSSItems.maskBunny) {
			player.getEntityAttribute(SharedMonsterAttributes.movementSpeed).removeModifier(bunnyHoodMoveBonus);
			ZSSPlayerInfo.get(player).setFlag(ZSSPlayerInfo.MOBILITY, true);
			ZSSEntityInfo.get(player).removeBuff(Buff.EVADE_UP);
		} else if (stack.getItem() == ZSSItems.maskGiants) {
			if (player.getEntityData().hasKey("origWidth")) {
				DirtyEntityAccessor.restoreOriginalSize(player);
				if (player.worldObj.isRemote) {
					player.stepHeight -= 1.0F;
				}
			}
		} else if (stack.getItem() == ZSSItems.maskGoron) {
			ZSSEntityInfo.get(player).removeBuff(Buff.RESIST_FIRE);
		} else if (stack.getItem() == ZSSItems.maskMajora) {
			ZSSEntityInfo.get(player).removeBuff(Buff.ATTACK_UP);
		}
	}
}
