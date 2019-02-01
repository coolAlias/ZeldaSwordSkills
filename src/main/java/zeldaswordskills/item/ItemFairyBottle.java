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

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import zeldaswordskills.api.item.IUnenchantable;
import zeldaswordskills.block.tileentity.TileEntityDungeonCore;
import zeldaswordskills.creativetab.ZSSCreativeTabs;
import zeldaswordskills.entity.passive.EntityFairy;
import zeldaswordskills.entity.passive.EntityNavi;
import zeldaswordskills.ref.Sounds;
import zeldaswordskills.util.WorldUtils;

/**
 * 
 * A fairy in a bottle; use to restore full health, or leave in the hotbar to grant 5 hearts
 * when Link would otherwise die.
 *
 */
public class ItemFairyBottle extends BaseModItem implements IUnenchantable
{
	public ItemFairyBottle() {
		super();
		setMaxDamage(0);
		setMaxStackSize(1);
		setCreativeTab(ZSSCreativeTabs.tabTools);
	}

	/**
	 * Checks player's action bar for a fairy bottle and uses it if present, restoring 5 hearts
	 * Call from LivingDeathEvent and return true if event should be canceled
	 */
	public static boolean onDeath(EntityPlayer player) {
		for (int i = 0; i < InventoryPlayer.getHotbarSize(); ++i) {
			ItemStack stack = player.inventory.getStackInSlot(i);
			if (stack != null && stack.getItem() instanceof ItemFairyBottle && !stack.hasDisplayName()) {
				WorldUtils.playSoundAtEntity(player, Sounds.FAIRY_LAUGH, 0.4F, 0.5F);
				player.setHealth(10F);
				player.inventory.setInventorySlotContents(i, new ItemStack(Items.glass_bottle));
				return true;
			}
		}
		return false;
	}

	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
		boolean used = false;
		if (stack.hasDisplayName()) {
			if (player.isSneaking()) {
				used = true;
				if (!world.isRemote) {
					Vec3 vec3 = player.getLookVec();
					EntityNavi navi = new EntityNavi(world);
					navi.setOwner(player);
					navi.setCustomNameTag(stack.getDisplayName());
					navi.setPosition(player.posX + (vec3.xCoord * 2D), player.posY + 1.6D, player.posZ + (vec3.zCoord * 2D));
					world.spawnEntityInWorld(navi);
				}
			}
		} else if (player.isSneaking()) {
			used = true;
			if (!world.isRemote) {
				Vec3 vec3 = player.getLookVec();
				EntityFairy fairy = new EntityFairy(world);
				fairy.setPosition(player.posX + (vec3.xCoord * 2D), player.posY + 1.6D, player.posZ + (vec3.zCoord * 2D));
				List<TileEntityDungeonCore> list = WorldUtils.getTileEntitiesWithinAABB(world, TileEntityDungeonCore.class, fairy.getEntityBoundingBox().expand(4.0D, 3.0D, 4.0D));
				for (TileEntityDungeonCore core : list) {
					if (core.isSpawner()) {
						fairy.setFairyHome(core.getPos().up());
						break;
					}
				}
				fairy.onReleased();
				world.spawnEntityInWorld(fairy);
			}
		} else if (player.getHealth() < player.getMaxHealth()) {
			used = true;
			if (!world.isRemote) {
				player.heal(player.getMaxHealth());
			}
		}
		if (used) {
			WorldUtils.playSoundAtEntity(player, Sounds.CORK, 0.4F, 1.0F);
			if (!player.capabilities.isCreativeMode || stack.hasDisplayName()) {
				--stack.stackSize;
				if (stack.stackSize <= 0) {
					return new ItemStack(Items.glass_bottle);
				} else {
					player.inventory.addItemStackToInventory(new ItemStack(Items.glass_bottle));
				}
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
	public boolean hasEffect(ItemStack stack) {
		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, EntityPlayer player, List<String> list, boolean advanced) {
		if (stack.hasDisplayName()) {
			list.add(StatCollector.translateToLocalFormatted("tooltip." + getUnlocalizedName().substring(5) + ".navi.desc.0", stack.getDisplayName()));
		} else {
			for (int i = 0; i < 4; ++i) {
				list.add(StatCollector.translateToLocal("tooltip." + getUnlocalizedName().substring(5) + ".desc." + i));
			}
		}
	}
}
