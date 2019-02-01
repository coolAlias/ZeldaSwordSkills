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

package zeldaswordskills.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import zeldaswordskills.api.block.BlockWeight;
import zeldaswordskills.creativetab.ZSSCreativeTabs;
import zeldaswordskills.item.ZSSItems;
import zeldaswordskills.ref.Sounds;
import zeldaswordskills.util.PlayerUtils;

/**
 * 
 * Like normal doors, these always come in a two-block pair, but may only be removed by
 * using the matching Big Key.
 * 
 * Metadata 0x0 to 0x7 are the key type required to open the door, and 0x8 flags top or bottom.
 *
 */
public class BlockDoorLocked extends Block implements IDungeonBlock
{
	/** Upper (bit 8) or lower (default) half of door */
	public static final PropertyEnum<BlockDoor.EnumDoorHalf> HALF = PropertyEnum.create("half", BlockDoor.EnumDoorHalf.class);

	public BlockDoorLocked(Material material) {
		super(material);
		setBlockUnbreakable();
		setResistance(BlockWeight.IMPOSSIBLE.weight);
		setStepSound(soundTypeMetal);
		setCreativeTab(ZSSCreativeTabs.tabKeys);
		setDefaultState(blockState.getBaseState().withProperty(HALF, BlockDoor.EnumDoorHalf.LOWER));
	}

	@Override
	public int getMobilityFlag() {
		return 2;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public EnumWorldBlockLayer getBlockLayer() {
		return EnumWorldBlockLayer.CUTOUT_MIPPED;
	}

	@Override
	public boolean isOpaqueCube() {
		return false;
	}

	@Override
	public boolean canPlaceBlockAt(World world, BlockPos pos) {
		return pos.getY() >= 255 ? false : World.doesBlockHaveSolidTopSurface(world, pos.down()) && super.canPlaceBlockAt(world, pos) && super.canPlaceBlockAt(world, pos.up());
	}

	/**
	 * Return true if the player's held item was succesfully used to unlock this door
	 */
	protected boolean canUnlock(EntityPlayer player, IBlockState state) {
		ItemStack key = player.getHeldItem();
		if (key != null) {
			if (key.getItem() == ZSSItems.keySmall) {
				return PlayerUtils.consumeHeldItem(player, ZSSItems.keySmall, 1);
			} else if (key.getItem() == ZSSItems.keySkeleton) {
				key.damageItem(1, player);
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumFacing face, float hitX, float hitY, float hitZ) {
		if (!world.isRemote) {
			if (canUnlock(player, state)) {
				world.playSoundAtEntity(player, Sounds.LOCK_DOOR, 0.25F, 1.0F / (world.rand.nextFloat() * 0.4F + 0.5F));
				world.setBlockToAir(pos);
			} else {
				world.playSoundAtEntity(player, Sounds.LOCK_RATTLE, 0.25F, 1.0F / (world.rand.nextFloat() * 0.4F + 0.5F));
			}
		}
		return false; // returning true here prevents any held item from processing onItemUse
	}

	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {
		pos = (state.getValue(HALF) == BlockDoor.EnumDoorHalf.LOWER ? pos.up() : pos.down());
		if (isSameVariant(world, pos, world.getBlockState(pos), state.getBlock().getMetaFromState(state))) {
			world.setBlockToAir(pos);
		}
	}

	@Override
	public boolean isSameVariant(World world, BlockPos pos, IBlockState state, int meta) {
		return (state.getBlock() == this);
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		BlockDoor.EnumDoorHalf half = ((meta & 8) > 0) ? BlockDoor.EnumDoorHalf.UPPER : BlockDoor.EnumDoorHalf.LOWER;
		return getDefaultState().withProperty(HALF, half);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return (state.getValue(HALF) == BlockDoor.EnumDoorHalf.UPPER) ? 8 : 0;
	}

	@Override
	protected BlockState createBlockState() {
		return new BlockState(this, HALF);
	}
}
