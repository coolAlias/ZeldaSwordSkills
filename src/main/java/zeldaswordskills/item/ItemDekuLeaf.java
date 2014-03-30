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

import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import zeldaswordskills.creativetab.ZSSCreativeTabs;
import zeldaswordskills.entity.projectile.EntityCyclone;
import zeldaswordskills.lib.ModInfo;
import zeldaswordskills.lib.Sounds;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemDekuLeaf extends Item
{
	public ItemDekuLeaf(int id) {
		super(id);
		setFull3D();
		setMaxDamage(0);
		setMaxStackSize(1);
		setCreativeTab(ZSSCreativeTabs.tabTools);
	}
	
	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean isHeld) {
		EntityPlayer player = (entity instanceof EntityPlayer ? (EntityPlayer) entity : null);
		if (isHeld && entity.fallDistance > 1.0F && entity.motionY < 0) {
			if (player == null || (player.getFoodStats().getFoodLevel() > 0 && !player.capabilities.isFlying)) {
				entity.motionY = (entity.motionY < -0.05D ? -0.05D : entity.motionY);
				entity.fallDistance = 1.0F;
				if (player != null && !player.capabilities.isCreativeMode) {
					player.addExhaustion(0.15F);
				}
			}
		}
	}
	
	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
		player.swingItem();
		if (player.getFoodStats().getFoodLevel() > 0) {
			if (player.onGround) {
				player.addExhaustion(10.0F);
				if (!world.isRemote) {
					world.playSoundAtEntity(player, Sounds.WHOOSH, 1.0F, 1.0F);
					world.spawnEntityInWorld(new EntityCyclone(world, player));
				}
			} else {
				player.addExhaustion(0.3F);
				player.motionY += 0.175D;
			}
		}
		
		return stack;
	}

	@Override
	public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity entity) {
		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister register) {
		itemIcon = register.registerIcon(ModInfo.ID + ":" + getUnlocalizedName().substring(9));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack itemstack, EntityPlayer player, List list, boolean isHeld) {
		list.add(EnumChatFormatting.ITALIC + StatCollector.translateToLocal("tooltip.zss.deku_leaf.desc.0"));
	}
}
