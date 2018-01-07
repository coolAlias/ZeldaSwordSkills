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
import java.util.UUID;

import net.minecraft.block.Block;
import net.minecraft.block.BlockSoulSand;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.StatCollector;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import zeldaswordskills.api.item.ArmorIndex;
import zeldaswordskills.api.item.IUnenchantable;
import zeldaswordskills.creativetab.ZSSCreativeTabs;
import zeldaswordskills.entity.ZSSEntityInfo;
import zeldaswordskills.entity.ZSSVillagerInfo.EnumVillager;
import zeldaswordskills.entity.buff.Buff;
import zeldaswordskills.entity.player.ZSSPlayerInfo;
import zeldaswordskills.network.PacketDispatcher;
import zeldaswordskills.network.client.InLiquidPacket;
import zeldaswordskills.util.MerchantRecipeHelper;
import zeldaswordskills.util.PlayerUtils;
import zeldaswordskills.util.WorldUtils;

/**
 * 
 * Various boots Link may find during his adventures. Each pair has the unique attribute
 * of being totally indestructible, but also unenchantable.
 * 
 * Heavy Boots: Made of heavy iron, these allow Link to move more quickly in liquids,
 * 				but more slowly on land. The great weight provides great resistance
 * 				to knockback effects. Wearing Heavy Boots allows Link to pull other
 * 				entities using the Hookshot.
 * 
 * Hover Boots: Allow wearer to dash across chasms, liquids, and other things.
 * 
 * Pegasus Boots: These greatly increase the hero's speed and evasion.
 * 
 * Rubber Boots: Handy for reducing the wearer's conductivity.
 *
 */
public class ItemArmorBoots extends ItemModArmor implements IUnenchantable
{
	/** Armor model texture resource location */
	private final String resourceLocation;

	/**
	 * Armor types as used on player: 0 boots, 1 legs, 2 chest, 3 helm
	 * Armor types as used in armor class: 0 helm, 1 chest, 2 legs, 3 boots
	 */
	public ItemArmorBoots(ArmorMaterial material, int renderIndex, String resourceLocation) {
		super(material, renderIndex, ArmorIndex.TYPE_BOOTS);
		this.resourceLocation = resourceLocation;
		setMaxDamage(0);
		setCreativeTab(ZSSCreativeTabs.tabCombat);
	}

	@Override
	public int getItemEnchantability() {
		return 0;
	}

	@Override
	public boolean getIsRepairable(ItemStack stack1, ItemStack stack2) {
		return false;
	}

	@Override
	public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity entity) {
		if (entity instanceof EntityVillager && !player.worldObj.isRemote) {
			EntityVillager villager = (EntityVillager) entity;
			MerchantRecipeList trades = villager.getRecipes(player);
			if (EnumVillager.BLACKSMITH.is(villager) && trades != null) {
				MerchantRecipe trade = new MerchantRecipe(stack.copy(), new ItemStack(Items.emerald, 16));
				if (player.worldObj.rand.nextFloat() < 0.2F && MerchantRecipeHelper.addToListWithCheck(trades, trade)) {
					PlayerUtils.sendTranslatedChat(player, "chat.zss.trade.generic.sell.1");
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
	public void onArmorTick(World world, EntityPlayer player, ItemStack stack) {
		ZSSPlayerInfo info = ZSSPlayerInfo.get(player);
		if (!info.getFlag(ZSSPlayerInfo.IS_WEARING_BOOTS)) {
			info.setWearingBoots(stack);
		}
	}

	/**
	 * Applies modifiers for boots when equipped, calling {@link #removeModifiers} followed by {@link #applyCustomModifiers}
	 */
	public final void applyModifiers(ItemStack stack, EntityPlayer player) {
		removeModifiers(stack, player);
		applyCustomModifiers(stack, player);
	}

	/**
	 * Called from {@link #applyModifiers} to add any special modifiers when equipped
	 */
	protected void applyCustomModifiers(ItemStack stack, EntityPlayer player) {}

	/**
	 * Called when a pair of boots is unequipped to remove any modifiers
	 */
	public void removeModifiers(ItemStack stack, EntityPlayer player) {}

	@Override
	public String getArmorTexture(ItemStack stack, Entity entity, int slot, String type) {
		return resourceLocation;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, EntityPlayer player, List<String> list, boolean advanced) {
		list.add(EnumChatFormatting.ITALIC + StatCollector.translateToLocal("tooltip." + getUnlocalizedName().substring(5) + ".desc.0"));
	}
	// Multimap modifiers are applied both while wearing and while holding an item
	/*
	@Override
	public Multimap getItemAttributeModifiers() {
		Multimap multimap = super.getItemAttributeModifiers();
		if (this == ZSSItems.bootsHeavy) {
			multimap.put(SharedMonsterAttributes.knockbackResistance.getAttributeUnlocalizedName(), new AttributeModifier(heavyBootsKnockbackModifierUUID, "Heavy Boots Knockback Resistance", 0.75D, 0));
			multimap.put(SharedMonsterAttributes.movementSpeed.getAttributeUnlocalizedName(), new AttributeModifier(heavyBootsMovePenaltyUUID, "Heavy Boots Movement penalty", -0.6D, 1));
		}
		return multimap;
	}
	 */

	//======================================================================//
	//	Static inner classes for Masks with custom behaviors				//
	//======================================================================//
	public static class ItemHeavyBoots extends ItemArmorBoots
	{
		/** Bonus to knockback resistance when wearing Heavy Boots */
		private static final UUID heavyBootsKnockbackModifierUUID = UUID.fromString("71AF0F88-82E5-49DE-B9CC-844048E33D69");
		private static final AttributeModifier heavyBootsKnockbackModifier = (new AttributeModifier(heavyBootsKnockbackModifierUUID, "Heavy Boots Knockback Resistance", 1.0D, 0)).setSaved(false);

		/** Movement penalty for wearing Heavy Boots applies at all times */
		private static final UUID heavyBootsMovePenaltyUUID = UUID.fromString("B6C8CCB6-AE7B-4F14-908A-2F41BDB4D720");
		private static final AttributeModifier heavyBootsMovePenalty = (new AttributeModifier(heavyBootsMovePenaltyUUID, "Heavy Boots Movement penalty", -0.6D, 1)).setSaved(false);

		public ItemHeavyBoots(ArmorMaterial material, int renderIndex, String resourceLocation) {
			super(material, renderIndex, resourceLocation);
		}
		@Override
		public void onArmorTick(World world, EntityPlayer player, ItemStack stack) {
			super.onArmorTick(world, player, stack);
			WorldUtils.reverseMaterialAcceleration(world, player.getEntityBoundingBox().expand(0.0D, -0.4000000059604645D, 0.0D).contract(0.001D, 0.001D, 0.001D), Material.water, player);
			if (!world.isRemote) {
				BlockPos pos = new BlockPos(player);
				Material m = world.getBlockState(pos).getBlock().getMaterial();
				if (m.isLiquid()) {
					PacketDispatcher.sendTo(new InLiquidPacket(m == Material.lava), (EntityPlayerMP) player);
				}
				Material m1 = world.getBlockState(pos.down()).getBlock().getMaterial();
				if ((m1 == Material.glass || m1 == Material.ice) && world.getTotalWorldTime() % 2 == 0) {
					if ((!player.isSneaking() && world.rand.nextFloat() < 0.15F) || world.rand.nextFloat() < 0.01F) {
						world.destroyBlock(pos.down(), false);
					}
				}
			}
		}
		@Override
		protected void applyCustomModifiers(ItemStack stack, EntityPlayer player) {
			player.getEntityAttribute(SharedMonsterAttributes.knockbackResistance).applyModifier(heavyBootsKnockbackModifier);
			player.getEntityAttribute(SharedMonsterAttributes.movementSpeed).applyModifier(heavyBootsMovePenalty);
			ZSSEntityInfo.get(player).applyBuff(Buff.EVADE_DOWN, Integer.MAX_VALUE, 50);
		}
		@Override
		public void removeModifiers(ItemStack stack, EntityPlayer player) {
			player.getEntityAttribute(SharedMonsterAttributes.knockbackResistance).removeModifier(heavyBootsKnockbackModifier);
			player.getEntityAttribute(SharedMonsterAttributes.movementSpeed).removeModifier(heavyBootsMovePenalty);
			ZSSEntityInfo.get(player).removeBuff(Buff.EVADE_DOWN);
		}
	}
	public static class ItemHoverBoots extends ItemArmorBoots {
		public ItemHoverBoots(ArmorMaterial material, int renderIndex, String resourceLocation) {
			super(material, renderIndex, resourceLocation);
		}
		@Override
		public void onArmorTick(World world, EntityPlayer player, ItemStack stack) {
			super.onArmorTick(world, player, stack);
			ZSSPlayerInfo info = ZSSPlayerInfo.get(player);
			Block block = world.getBlockState(new BlockPos(player).down()).getBlock();
			boolean flag = !block.getMaterial().blocksMovement() || block.slipperiness > 0.6F || block instanceof BlockSoulSand;
			if (flag && player.isSprinting() && player.motionY < 0.0D && ++info.hoverTime < 40 ) {
				player.posY += -player.motionY;
				player.motionY = 0.0D;
				player.fallDistance = 0.0F;
				if (info.hoverTime % 3 == 0) {
					world.spawnParticle(EnumParticleTypes.CLOUD, player.posX, player.posY - 1, player.posZ, -player.motionX, player.motionY, -player.motionZ);
				}
			} else if (info.hoverTime > 0) {
				info.hoverTime = 0;
				player.setSprinting(false);
				if (world.isRemote && Minecraft.getMinecraft().gameSettings.keyBindSprint.isKeyDown()) {
					KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindSprint.getKeyCode(), false);
				}
			}
		}
	}
	public static class ItemPegasusBoots extends ItemArmorBoots
	{
		/** Movement bonus for wearing Pegasus Boots */
		private static final UUID pegasusBootsMoveBonusUUID = UUID.fromString("36A0FC05-50EB-460B-8961-615633A6D813");
		private static final AttributeModifier pegasusBootsMoveBonus = (new AttributeModifier(pegasusBootsMoveBonusUUID, "Pegasus Boots Speed Bonus", 0.3D, 2)).setSaved(false);

		public ItemPegasusBoots(ArmorMaterial material, int renderIndex, String resourceLocation) {
			super(material, renderIndex, resourceLocation);
		}
		@Override
		protected void applyCustomModifiers(ItemStack stack, EntityPlayer player) {
			player.getEntityAttribute(SharedMonsterAttributes.movementSpeed).applyModifier(pegasusBootsMoveBonus);
			ZSSPlayerInfo.get(player).setFlag(ZSSPlayerInfo.MOBILITY, true);
			ZSSEntityInfo.get(player).applyBuff(Buff.EVADE_UP, Integer.MAX_VALUE, 25);
		}
		@Override
		public void removeModifiers(ItemStack stack, EntityPlayer player) {
			player.getEntityAttribute(SharedMonsterAttributes.movementSpeed).removeModifier(pegasusBootsMoveBonus);
			ZSSPlayerInfo.get(player).setFlag(ZSSPlayerInfo.MOBILITY, false);
			ZSSEntityInfo.get(player).removeBuff(Buff.EVADE_UP);
		}
	}
	public static class ItemRubberBoots extends ItemArmorBoots {
		public ItemRubberBoots(ArmorMaterial material, int renderIndex, String resourceLocation) {
			super(material, renderIndex, resourceLocation);
		}
		@Override
		protected void applyCustomModifiers(ItemStack stack, EntityPlayer player) {
			ZSSEntityInfo.get(player).applyBuff(Buff.RESIST_SHOCK, Integer.MAX_VALUE, 50);
		}
		@Override
		public void removeModifiers(ItemStack stack, EntityPlayer player) {
			ZSSEntityInfo.get(player).removeBuff(Buff.RESIST_SHOCK);
		}
	}
}
