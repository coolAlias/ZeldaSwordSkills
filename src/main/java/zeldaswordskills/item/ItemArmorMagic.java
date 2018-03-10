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

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import zeldaswordskills.api.item.ArmorIndex;
import zeldaswordskills.api.item.IEquipTrigger;
import zeldaswordskills.api.item.IUnenchantable;
import zeldaswordskills.creativetab.ZSSCreativeTabs;
import zeldaswordskills.entity.player.ZSSPlayerWallet;
import zeldaswordskills.network.PacketDispatcher;
import zeldaswordskills.network.client.InLiquidPacket;
import zeldaswordskills.ref.Config;
import zeldaswordskills.ref.ModInfo;

/**
 * 
 * While wearing at least the chest piece of the magic armor set, rupees will be
 * consumed regularly and each time the player takes damage. So long as the player
 * has enough rupees, they will be invulnerable.
 * <br><br>
 * If the player has no rupees while wearing the chest piece, they will be affected
 * as though wearing the Heavy Boots.
 * <br><br>
 * Each piece worn reduces the cost to negate damage when hit.
 *
 */
public class ItemArmorMagic extends ItemArmor
{
	/**
	 * Armor types as used on player: 0 boots, 1 legs, 2 chest, 3 helm
	 * Armor types as used in armor class: 0 helm, 1 chest, 2 legs, 3 boots
	 */
	public ItemArmorMagic(int renderIndex, int type) {
		super(ArmorMaterial.IRON, renderIndex, type);
		this.setCreativeTab(ZSSCreativeTabs.tabCombat);
	}

	@Override
	public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity entity) {
		return true;
	}

	/**
	 * Call when the player is hurt to check if they are wearing Magic Armor and,
	 * if so, use any rupees they may have to negate or mitigate the damage taken.
	 * @param player
	 * @param damage Damage amount from LivingHurtEvent
	 * @return damage amount modified based on rupees spent if magic armor is worn
	 */
	public static float onPlayerHurt(EntityPlayer player, float damage) {
		int pieces = ItemArmorMagic.getPiecesWorn(player);
		if (pieces > 0) {
			ZSSPlayerWallet wallet = ZSSPlayerWallet.get(player);
			int rupees = wallet.getRupees();
			if (rupees < 1) {
				return damage;
			}
			int cost = Config.getMagicArmorShieldCost();
			// Amount to lower cost per piece of additional set armor worn (min 1, max 100)
			int tenth = MathHelper.clamp_int(((cost + 9) / 10), 1, 100);
			cost = Math.max(1, (cost - ((pieces - 1) * tenth)));
			// Being hit without enough rupees depletes all remaining rupees
			if (rupees < cost) {
				wallet.setRupees(0);
			} else if (Config.doesMagicArmorCostScale()) { 
				while (damage > 0.0F && wallet.spendRupees(cost, false)) {
					damage -= 1.0F;
				}
				wallet.sync();
			} else if (wallet.spendRupees(cost)) {
				damage = 0.0F;
			}
		}
		return damage;
	}

	/**
	 * Returns the number of items in the set worn by the player, or 0 if
	 * they are not wearing at least the Magic Armor chest piece.
	 */
	public static int getPiecesWorn(EntityPlayer player) {
		int pieces = 0;
		for (int i = 0; i < 4; i++) {
			ItemStack stack = player.getCurrentArmor(i);
			if (stack != null && stack.getItem() instanceof ItemArmorMagic) {
				pieces++;
			} else if (i == ArmorIndex.EQUIPPED_CHEST) {
				return 0;
			}
		}
		return pieces;
	}

	@Override
	public String getArmorTexture(ItemStack stack, Entity entity, int slot, String type) {
		String name = this.getUnlocalizedName().substring(9, this.getUnlocalizedName().lastIndexOf("_"));
		if (this == ZSSItems.armorMagicChest) {
			int i = (entity.ticksExisted % 100) / 20;
			switch (i) {
			case 0: break;
			case 1: name = "hero_tunic"; break;
			case 2: name = "goron_tunic"; break;
			case 3: name = "hero_tunic"; break;
			case 4: name = "zora_tunic"; break;
			}
		}
		return String.format("%s:textures/armor/%s_layer_%d.png", ModInfo.ID, name, (slot == 2 ? 2 : 1));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister register) {
		this.itemIcon = register.registerIcon(ModInfo.ID + ":" + this.getUnlocalizedName().substring(9));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack,	EntityPlayer player, List list, boolean advanced) {
		list.add(StatCollector.translateToLocal("tooltip.zss." + this.getUnlocalizedName().substring(9) + ".desc.0"));
	}

	/**
	 * 
	 * Chest piece is responsible for deducting rupees each tick and
	 * must be worn for the player to be invincible.
	 * <br><br>
	 * It is also the only indestructible and unenchantable piece.
	 *
	 */
	public static class Chest extends ItemArmorMagic implements IEquipTrigger, IUnenchantable
	{
		private static final UUID pennilessMovePenaltyUUID = UUID.fromString("A6C8CCB6-AE7B-4F14-908A-2F41BDB4D720");
		private static final AttributeModifier pennilessMovePenalty = (new AttributeModifier(pennilessMovePenaltyUUID, "Magic Armor Movement penalty", -0.6D, 1)).setSaved(false);

		public Chest(int renderIndex) {
			super(renderIndex, ArmorIndex.TYPE_CHEST);
			this.setMaxDamage(0); // cannot be damaged
		}

		@Override
		public int getItemEnchantability() {
			return 0;
		}

		@Override
		public boolean getIsRepairable(ItemStack toRepair, ItemStack stack) {
			return false;
		}

		@Override
		public void onArmorEquipped(ItemStack stack, EntityPlayer player, int equipSlot) {
			if (ZSSPlayerWallet.get(player).getRupees() < Config.getMagicArmorTickCost()) {
				player.getEntityAttribute(SharedMonsterAttributes.movementSpeed).applyModifier(pennilessMovePenalty);
			}
		}

		@Override
		public void onArmorUnequipped(ItemStack stack, EntityPlayer player, int equipSlot) {
			IAttributeInstance speed = player.getEntityAttribute(SharedMonsterAttributes.movementSpeed);
			if (speed.getModifier(pennilessMovePenaltyUUID) != null) {
				speed.removeModifier(pennilessMovePenalty);
			}
		}

		@Override
		public void onArmorTick(World world, EntityPlayer player, ItemStack stack) {
			if (!world.isRemote) {
				ZSSPlayerWallet wallet = ZSSPlayerWallet.get(player);
				int cost = MathHelper.clamp_int(wallet.getRupees(), 1, Config.getMagicArmorTickCost());
				if (wallet.getRupees() > 0 && (world.getTotalWorldTime() % 20) == 0) {
					wallet.spendRupees(Math.min(cost, wallet.getRupees()));
				}
				// Update state each tick in case player gains or loses rupees by other means, such as taking damage
				IAttributeInstance speed = player.getEntityAttribute(SharedMonsterAttributes.movementSpeed);
				if (wallet.getRupees() < cost) {
					if (speed.getModifier(pennilessMovePenaltyUUID) == null) {
						speed.applyModifier(pennilessMovePenalty);
					}
					// Replicate sinking effect of wearing Heavy Boots
					int i = MathHelper.floor_double(player.posX);
					int j = MathHelper.floor_double(player.boundingBox.minY);
					int k = MathHelper.floor_double(player.posZ);
					Material m = world.getBlock(i, j, k).getMaterial();
					if (m.isLiquid()) {
						PacketDispatcher.sendTo(new InLiquidPacket(m == Material.lava), (EntityPlayerMP) player);
					}
				} else if (speed.getModifier(pennilessMovePenaltyUUID) != null) {
					speed.removeModifier(pennilessMovePenalty);
				}
			}
		}

		@Override
		@SideOnly(Side.CLIENT)
		public void addInformation(ItemStack stack,	EntityPlayer player, List list, boolean advanced) {
			list.add(StatCollector.translateToLocal("tooltip.zss.magic_armor_chest.desc.0"));
			list.add(StatCollector.translateToLocal("tooltip.zss.magic_armor_chest.desc.1"));
		}
	}
}
