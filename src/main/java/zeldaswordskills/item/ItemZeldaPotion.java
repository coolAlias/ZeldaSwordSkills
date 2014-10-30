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

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import zeldaswordskills.api.item.IUnenchantable;
import zeldaswordskills.creativetab.ZSSCreativeTabs;
import zeldaswordskills.entity.ZSSEntityInfo;
import zeldaswordskills.entity.buff.Buff;
import zeldaswordskills.ref.ModInfo;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemZeldaPotion extends ItemFood implements IUnenchantable
{
	/** Amount of HP to restore when consumed */
	private final float restoreHP;

	/** Id of the buff to add, if any */
	private Buff buff;
	/** Duration the buff will last */
	private int buffDuration;
	/** Amplifier of the buff, similar to vanilla potions */
	private int buffAmplifier;
	/** Probability of the set buff effect occurring */
	private float buffProbability;

	/** Creates a potion with no healing or hunger-restoring properties */
	public ItemZeldaPotion() {
		this(0, 0.0F, 0.0F);
	}

	public ItemZeldaPotion(int restoreHunger, float saturationModifier, float healAmount) {
		super(restoreHunger, saturationModifier, false);
		restoreHP = healAmount;
		setAlwaysEdible();
		setMaxStackSize(1);
		setCreativeTab(ZSSCreativeTabs.tabTools);
	}

	@Override
	public EnumAction getItemUseAction(ItemStack stack) {
		return EnumAction.drink;
	}

	@Override
	public ItemStack onEaten(ItemStack stack, World world, EntityPlayer player) {
		player.getFoodStats().func_151686_a(this, stack);
		player.heal(restoreHP);
		onFoodEaten(stack, world, player);
		if (!player.capabilities.isCreativeMode) {
			if (--stack.stackSize <= 0) {
				return new ItemStack(Items.glass_bottle);
			}
			player.inventory.addItemStackToInventory(new ItemStack(Items.glass_bottle));
		}
		return stack;
	}

	@Override
	protected void onFoodEaten(ItemStack stack, World world, EntityPlayer player) {
		super.onFoodEaten(stack, world, player);
		if (buff != null && world.rand.nextFloat() < buffProbability) {
			ZSSEntityInfo.get(player).applyBuff(buff, buffDuration, buffAmplifier);
		}
	}

	/**
	 * Sets the Buff that this potion will grant when consumed
	 */
	public ItemZeldaPotion setBuffEffect(Buff buffEnum, int duration, int amplifier, float probability) {
		buff = buffEnum;
		buffDuration = duration;
		buffAmplifier = amplifier;
		buffProbability = probability;
		return this;
	}

	@Override
	public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity entity) {
		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean hasEffect(ItemStack stack, int pass) {
		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister register) {
		itemIcon = register.registerIcon(ModInfo.ID + ":" + getUnlocalizedName().substring(9));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean isHeld) {
		list.add(EnumChatFormatting.ITALIC + StatCollector.translateToLocal("tooltip." + getUnlocalizedName().substring(5) + ".desc.0"));
	}
}
