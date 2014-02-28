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

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import zeldaswordskills.api.block.BlockWeight;
import zeldaswordskills.api.item.IArmorBreak;
import zeldaswordskills.api.item.ISmashBlock;
import zeldaswordskills.api.item.ISwingSpeed;
import zeldaswordskills.creativetab.ZSSCreativeTabs;
import zeldaswordskills.lib.ModInfo;

import com.google.common.collect.Multimap;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemHammer extends Item implements IArmorBreak, ISmashBlock, ISwingSpeed
{
	/** Max resistance that a block may have and still be smashed */
	private final BlockWeight strength;
	/** Amount of damage this hammer inflicts */
	private final float weaponDamage;
	/** Percentage of damage that ignores armor */
	private final float ignoreArmorAmount;
	
	public ItemHammer(int id, BlockWeight strength, float damage, float ignoreArmor) {
		super(id);
		this.strength = strength;
		this.weaponDamage = damage;
		this.ignoreArmorAmount = ignoreArmor;
		setFull3D();
		setMaxDamage(0);
		setMaxStackSize(1);
		setCreativeTab(ZSSCreativeTabs.tabCombat);
	}

	@Override
	public float getPercentArmorIgnored() {
		return ignoreArmorAmount;
	}

	@Override
	public BlockWeight getSmashStrength(EntityPlayer player, ItemStack stack, Block block, int meta) {
		return strength;
	}
	
	@Override
	public void onBlockSmashed(EntityPlayer player, ItemStack stack, Block block, int meta) {}
	
	@Override
	public int getSwingSpeed() {
		return 30;
	}

	@Override
	public boolean hitEntity(ItemStack stack, EntityLivingBase target, EntityLivingBase attacker) {
		// TODO play hit sound
		return true;
	}
	
	@Override
	public boolean onEntitySwing(EntityLivingBase entityLiving, ItemStack stack) {
		// TODO play swing sound
		return false;
	}
	
	@Override
	public int getItemEnchantability() {
		return 0;
	}

	@Override
	public EnumAction getItemUseAction(ItemStack stack) {
		return EnumAction.block;
	}

	@Override
	public int getMaxItemUseDuration(ItemStack stack) {
		return 72000;
	}

	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
		player.setItemInUse(stack, this.getMaxItemUseDuration(stack));
		return stack;
	}
	
	@Override
	public void onPlayerStoppedUsing(ItemStack stack, World world, EntityPlayer player, int ticksRemaining) {
		if (this == ZSSItems.hammerSkull && player.attackTime == 0) {
			int ticksInUse = getMaxItemUseDuration(stack) - ticksRemaining;
			float charge = (float) ticksInUse / 30.0F;
			charge = Math.min((charge * charge + charge * 2.0F) / 3.0F, 1.0F);
			if (charge > 0.5F) {
				player.swingItem();
				// TODO spawn particles spreading in a circle like on landing from a fall
				// TODO attack all nearby entities with stun damage
			}
		}
	}
	
	@Override
	public Multimap getItemAttributeModifiers() {
		Multimap multimap = super.getItemAttributeModifiers();
		multimap.put(SharedMonsterAttributes.attackDamage.getAttributeUnlocalizedName(), new AttributeModifier(field_111210_e, "Weapon modifier", (double) weaponDamage, 0));
		return multimap;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister register) {
		itemIcon = register.registerIcon(ModInfo.ID + ":" + getUnlocalizedName().substring(9));
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean isHeld) {
		list.add(EnumChatFormatting.ITALIC + StatCollector.translateToLocal("tooltip." + getUnlocalizedName().substring(5) + ".desc.0"));
	}
}
