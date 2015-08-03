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

import java.util.List;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import zeldaswordskills.api.item.IUnenchantable;
import zeldaswordskills.creativetab.ZSSCreativeTabs;
import zeldaswordskills.entity.player.ZSSPlayerInfo;
import zeldaswordskills.entity.projectile.EntityCyclone;
import zeldaswordskills.ref.ModInfo;
import zeldaswordskills.ref.Sounds;
import zeldaswordskills.util.WorldUtils;

public class ItemDekuLeaf extends Item implements IUnenchantable
{
	public ItemDekuLeaf() {
		super();
		setFull3D();
		setMaxDamage(0);
		setMaxStackSize(1);
		setCreativeTab(ZSSCreativeTabs.tabTools);
	}

	/**
	 * Returns the next time this stack may be used
	 */
	private long getNextUseTime(ItemStack stack) {
		return (stack.hasTagCompound() ? stack.getTagCompound().getLong("next_use") : 0);
	}

	/**
	 * Sets the next time this stack may be used to the current world time plus a number of ticks
	 */
	private void setNextUseTime(ItemStack stack, World world, int ticks) {
		if (!stack.hasTagCompound()) { stack.setTagCompound(new NBTTagCompound()); }
		stack.getTagCompound().setLong("next_use", (world.getTotalWorldTime() + ticks));
	}

	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean isHeld) {
		if (isHeld && entity instanceof EntityPlayer && entity.fallDistance > 1.0F && entity.motionY < 0) {
			EntityPlayer player = (EntityPlayer) entity;
			if (!player.capabilities.isFlying && ZSSPlayerInfo.get(player).getCurrentMagic() > 0) {
				player.motionY = (player.motionY < -0.05D ? -0.05D : player.motionY);
				player.fallDistance = 1.0F;
				if (world.getWorldTime() % 4 == 0) { // 1 MP per 2 seconds
					ZSSPlayerInfo.get(player).consumeMagic(0.1F);
				}
			}
		}
	}

	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
		player.swingItem();
		if (player.onGround) {
			if (world.getTotalWorldTime() > getNextUseTime(stack) && ZSSPlayerInfo.get(player).useMagic(10.0F)) {
				if (!world.isRemote) {
					WorldUtils.playSoundAtEntity(player, Sounds.WHOOSH, 0.4F, 0.5F);
					world.spawnEntityInWorld(new EntityCyclone(world, player));
					if (!player.capabilities.isCreativeMode) {
						setNextUseTime(stack, world, 20);
					}
				}
			} else if (!world.isRemote) {
				WorldUtils.playSoundAtEntity(player, Sounds.GRUNT, 0.3F, 0.8F);
			}
		} else if (ZSSPlayerInfo.get(player).useMagic(0.25F)) {
			player.motionY += 0.175D;
		}
		return stack;
	}

	@Override
	public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity entity) {
		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister register) {
		itemIcon = register.registerIcon(ModInfo.ID + ":" + getUnlocalizedName().substring(9));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack itemstack, EntityPlayer player, List list, boolean isHeld) {
		list.add(EnumChatFormatting.ITALIC + StatCollector.translateToLocal("tooltip.zss.deku_leaf.desc.0"));
	}
}
