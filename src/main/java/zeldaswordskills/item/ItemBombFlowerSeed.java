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
import net.minecraft.client.renderer.ItemModelMesher;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemSeeds;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.EnumPlantType;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import zeldaswordskills.api.entity.BombType;
import zeldaswordskills.api.item.IRightClickEntity;
import zeldaswordskills.block.ZSSBlocks;
import zeldaswordskills.creativetab.ZSSCreativeTabs;
import zeldaswordskills.entity.ZSSEntityInfo;
import zeldaswordskills.entity.projectile.EntityBomb;
import zeldaswordskills.ref.ModInfo;
import zeldaswordskills.util.PlayerUtils;

public class ItemBombFlowerSeed extends ItemSeeds implements IModItem, IRightClickEntity {

	public ItemBombFlowerSeed() {
		super(ZSSBlocks.bombFlower, Blocks.stone);
		setCreativeTab(ZSSCreativeTabs.tabTools);
	}

	/**
	 * Returns "item.zss.unlocalized_name" for translation purposes
	 */
	@Override
	public String getUnlocalizedName() {
		return super.getUnlocalizedName().replaceFirst("item.", "item.zss.");
	}

	@Override
	public String getUnlocalizedName(ItemStack stack) {
		return getUnlocalizedName();
	}

	@Override
	public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing face, float hitX, float hitY, float hitZ) {
		if (face == EnumFacing.DOWN || !player.canPlayerEdit(pos, face, stack) || !player.canPlayerEdit(pos.up(), face, stack)) {
			return false;
		}
		pos = pos.up(); // placing it on top of the block at y
		Block plant = getPlant(world, pos).getBlock();
		if (plant.canPlaceBlockAt(world, pos)) {
			if (!world.isRemote) {
				world.setBlockState(pos, plant.getDefaultState());
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
				((EntityChicken) entity).setInLove(player);
				int time = 60 + entity.worldObj.rand.nextInt(60);
				EntityBomb bomb = new EntityBomb(entity.worldObj).setType(BombType.BOMB_STANDARD).setFuseTime(time);
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
					BlockPos pos = new BlockPos(this);
					boolean flag = false;
					if (ZSSBlocks.bombFlower.canPlaceBlockAt(worldObj, pos)) {
						flag = true;
					} else if (ZSSBlocks.bombFlower.canPlaceBlockAt(worldObj, pos.north())) {
						pos = pos.north();
						flag = true;
					} else if (ZSSBlocks.bombFlower.canPlaceBlockAt(worldObj, pos.south())) {
						pos = pos.south();
						flag = true;
					} else if (ZSSBlocks.bombFlower.canPlaceBlockAt(worldObj, pos.east())) {
						pos = pos.east();
						flag = true;
					} else if (ZSSBlocks.bombFlower.canPlaceBlockAt(worldObj, pos.west())) {
						pos = pos.west();
						flag = true;
					}
					if (flag) {
						worldObj.setBlockState(pos, ZSSBlocks.bombFlower.getDefaultState());
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
		item.setPickupDelay(40);
		return item;
	}

	@Override
	public EnumPlantType getPlantType(IBlockAccess world, BlockPos pos) {
		return EnumPlantType.Cave;
	}

	/**
	 * Default behavior returns NULL to not register any variants
	 */
	@Override
	public String[] getVariants() {
		return null;
	}

	/**
	 * Default implementation suggested by {@link IModItem#registerVariants()}
	 */
	@Override
	@SideOnly(Side.CLIENT)
	public void registerVariants() {
		String[] variants = getVariants();
		if (variants != null) {
			ModelBakery.addVariantName(this, variants);
		}
	}

	/**
	 * Register all of this Item's renderers here, including for any subtypes.
	 * Default behavior registers a single inventory-based mesher for each variant
	 * returned by {@link #getVariants() getVariants}.
	 * If no variants are available, "mod_id:" plus the item's unlocalized name is used.
	 */
	@Override
	@SideOnly(Side.CLIENT)
	public void registerRenderers(ItemModelMesher mesher) {
		String[] variants = getVariants();
		if (variants == null || variants.length < 1) {
			String name = getUnlocalizedName();
			variants = new String[]{ModInfo.ID + ":" + name.substring(name.lastIndexOf(".") + 1)};
		}
		for (int i = 0; i < variants.length; ++i) {
			mesher.register(this, i, new ModelResourceLocation(variants[i], "inventory"));
		}
	}
}
