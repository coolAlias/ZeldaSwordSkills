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

import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemSeeds;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.EnumPlantType;
import zeldaswordskills.api.entity.BombType;
import zeldaswordskills.api.item.IRightClickEntity;
import zeldaswordskills.block.ZSSBlocks;
import zeldaswordskills.creativetab.ZSSCreativeTabs;
import zeldaswordskills.entity.ZSSEntityInfo;
import zeldaswordskills.entity.projectile.EntityBomb;
import zeldaswordskills.ref.ModInfo;
import zeldaswordskills.util.PlayerUtils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemBombFlowerSeed extends ItemSeeds implements IRightClickEntity {

	public ItemBombFlowerSeed() {
		super(ZSSBlocks.bombFlower, Blocks.stone);
		setCreativeTab(ZSSCreativeTabs.tabTools);
	}

	@Override
	public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ) {
		if (side != 1 || !player.canPlayerEdit(x, y, z, side, stack) || !player.canPlayerEdit(x, y + 1, z, side, stack)) {
			return false;
		}
		++y; // placing it on top of the block at y
		Block plant = getPlant(world, x, y, z);
		if (plant.canPlaceBlockAt(world, x, y, z)) {
			if (!world.isRemote) {
				world.setBlock(x, y, z, plant);
			}
			--stack.stackSize;
			return true;
		}
		return false;
	}

	@Override
	public boolean onRightClickEntity(ItemStack stack, EntityPlayer player, Entity entity) {
		if (entity instanceof EntityChicken) {
			if (!player.worldObj.isRemote && PlayerUtils.consumeHeldItem(player, this, 1)) {
				// func_146082_f is setInLove
				((EntityChicken) entity).func_146082_f(player);
				int time = 60 + entity.worldObj.rand.nextInt(60);
				EntityBomb bomb = new EntityBomb(entity.worldObj).setType(BombType.BOMB_STANDARD).setTime(time);
				ZSSEntityInfo.get((EntityChicken) entity).onBombIngested(bomb);
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean hasCustomEntity(ItemStack stack) {
		return true;
	}

	@Override
	public Entity createEntity(World world, Entity entity, ItemStack stack) {
		EntityItem item = new EntityItem(world, entity.posX, entity.posY, entity.posZ, stack) {
			@Override
			public void onUpdate() {
				super.onUpdate();
				if (!worldObj.isRemote && ticksExisted > 80 && worldObj.rand.nextInt(128) == 0) {
					int i = MathHelper.floor_double(posX);
					int j = MathHelper.floor_double(posY);
					int k = MathHelper.floor_double(posZ);
					boolean flag = false;
					if (ZSSBlocks.bombFlower.canPlaceBlockAt(worldObj, i, j, k)) {
						flag = true;
					} else if (ZSSBlocks.bombFlower.canPlaceBlockAt(worldObj, i + 1, j, k)) {
						++i;
						flag = true;
					} else if (ZSSBlocks.bombFlower.canPlaceBlockAt(worldObj, i - 1, j, k)) {
						--i;
						flag = true;
					} else if (ZSSBlocks.bombFlower.canPlaceBlockAt(worldObj, i, j, k + 1)) {
						++k;
						flag = true;
					} else if (ZSSBlocks.bombFlower.canPlaceBlockAt(worldObj, i, j, k - 1)) {
						--k;
						flag = true;
					}
					if (flag) {
						worldObj.setBlock(i, j, k, ZSSBlocks.bombFlower);
						--getEntityItem().stackSize;
						if (getEntityItem().stackSize == 0) {
							setDead();
						}
					}
				}
			}
		};
		item.motionX = entity.motionX;
		item.motionY = entity.motionY;
		item.motionZ = entity.motionZ;
		item.delayBeforeCanPickup = 40;
		return item;
	}

	@Override
	public EnumPlantType getPlantType(IBlockAccess world, int x, int y, int z) {
		return EnumPlantType.Cave;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister register) {
		itemIcon = register.registerIcon(ModInfo.ID + ":" + getUnlocalizedName().substring(9));
	}
}
