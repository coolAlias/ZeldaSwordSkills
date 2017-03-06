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

import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import zeldaswordskills.api.item.IUnenchantable;
import zeldaswordskills.creativetab.ZSSCreativeTabs;
import zeldaswordskills.entity.ZSSEntityInfo;
import zeldaswordskills.entity.buff.Buff;
import zeldaswordskills.entity.buff.BuffBase;
import zeldaswordskills.entity.player.ZSSPlayerInfo;

public class ItemZeldaPotion extends ItemDrinkable implements IUnenchantable
{
	/** Amount of HP to restore when consumed */
	private final float restoreHP;

	/** Amount of MP to restore when consumed */
	private final float restoreMP;

	/** The buff to add, if any */
	private BuffBase buff;

	/** Duration in minutes, with any remainder in seconds */
	private int minutes, seconds;

	/** Probability of the set buff effect occurring */
	private float buffProbability;

	/** Creates a potion with no healing or hunger-restoring properties */
	public ItemZeldaPotion(String name) {
		this(name, 0.0F, 0.0F);
	}

	/**
	 * @param restoreHP amount of HP drinking this potion immediately restores
	 * @param restoreMP amount of MP drinking this potion immediately restores
	 */
	public ItemZeldaPotion(String name, float restoreHP, float restoreMP) {
		super(name);
		this.restoreHP = restoreHP;
		this.restoreMP = restoreMP;
		setMaxStackSize(1);
		setCreativeTab(ZSSCreativeTabs.tabTools);
	}

	@Override
	public ItemStack onItemUseFinish(ItemStack stack, World world, EntityPlayer player) {
		if (!player.capabilities.isCreativeMode) {
			--stack.stackSize;
		}
		world.playSoundAtEntity(player, "random.burp", 0.5F, world.rand.nextFloat() * 0.1F + 0.9F);
		player.heal(restoreHP);
		ZSSPlayerInfo.get(player).restoreMagic(restoreMP);
		if (buff != null && world.rand.nextFloat() < buffProbability) {
			ZSSEntityInfo.get(player).applyBuff(new BuffBase(buff));
		}
		return super.onItemUseFinish(stack, world, player);
	}

	/**
	 * Sets the Buff that this potion will grant when consumed
	 */
	public ItemZeldaPotion setBuffEffect(Buff buffEnum, int duration, int amplifier, float probability) {
		buff = new BuffBase(buffEnum, duration, amplifier);
		minutes = duration / 1200;
		seconds = duration % 1200;
		buffProbability = probability;
		return this;
	}

	@Override
	public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity entity) {
		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean hasEffect(ItemStack stack) {
		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, EntityPlayer player, List<String> list, boolean advanced) {
		if (restoreHP > 0) {
			list.add(EnumChatFormatting.YELLOW + StatCollector.translateToLocalFormatted("tooltip.zss.restore_hp", String.format("%.0f", restoreHP / 2.0F)));
		}
		if (restoreMP > 0) {
			list.add(EnumChatFormatting.GREEN + StatCollector.translateToLocalFormatted("tooltip.zss.restore_mp", MathHelper.floor_float(restoreMP)));
		}
		if (buff != null) {
			list.add(EnumChatFormatting.GRAY + StatCollector.translateToLocalFormatted("tooltip.zss.buff", buff.getBuff().getName(), minutes, String.format("%02d", seconds)));
			if (buffProbability < 1.0F) {
				list.add(EnumChatFormatting.GREEN + StatCollector.translateToLocalFormatted("tooltip.zss.buff_chance", String.format("%.1f", buffProbability * 100)));
			}
			list.add(EnumChatFormatting.YELLOW + StatCollector.translateToLocalFormatted("tooltip.zss.buff_amplifier", buff.getAmplifier()));
		}
	}
}
