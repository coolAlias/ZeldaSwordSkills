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

package zeldaswordskills.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.Icon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.event.Event.Result;
import zeldaswordskills.api.block.IWhipBlock;
import zeldaswordskills.creativetab.ZSSCreativeTabs;
import zeldaswordskills.entity.projectile.EntityWhip;
import zeldaswordskills.lib.ModInfo;
import zeldaswordskills.util.SideHit;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockBar extends Block implements IWhipBlock
{
	@SideOnly(Side.CLIENT)
	private Icon iconHorizontal;
	@SideOnly(Side.CLIENT)
	private Icon iconVertical;

	public BlockBar(int id, Material material) {
		super(id, material);
		setHardness(2.0F);
		setResistance(5.0F);
		setStepSound(soundWoodFootstep);
		setCreativeTab(ZSSCreativeTabs.tabBlocks);
		setBlockBounds(0.0F, 0.25F, 0.25F, 1.0F, 0.75F, 0.75F);
	}

	@Override
	public boolean canBreakBlock(WhipType whip, EntityLivingBase thrower, World world, int x, int y, int z, int side) {
		return false;
	}

	@Override
	public boolean canGrabBlock(WhipType whip, EntityLivingBase thrower, World world, int x, int y, int z, int side) {
		switch(world.getBlockMetadata(x, y, z) % 3) {
		case 0:	return (side != 4 && side != 5); // east/west
		case 1:	return (side != 2 && side != 3); // north/south
		case 2:	return (side != 0 && side != 1); // up/down
		}
		return false;
	}

	@Override
	public Result shouldSwing(EntityWhip whip, World world, int x, int y, int z, int ticksInGround) {
		return Result.DEFAULT;
	}

	@Override
	public boolean renderAsNormalBlock() {
		return false;
	}

	@Override
	public boolean isOpaqueCube() {
		return false;
	}

	@Override
	public int onBlockPlaced(World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ, int meta) {
		// 2 = NORTH (face of block), 3 = SOUTH, 4 = WEST, 5 = EAST, 0 = BOTTOM, 1 = TOP
		switch(side) {
		case 2:
		case 3: return 1;
		case 4:
		case 5: return 0;
		case SideHit.TOP:
		case SideHit.BOTTOM: return 2;
		}
		return meta;
	}

	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, int neighbor) {
		boolean drop = false;
		switch(world.getBlockMetadata(x, y, z) % 3) {
		case 0:	// east/west
			drop = (!world.getBlockMaterial(x + 1, y, z).blocksMovement() && !world.getBlockMaterial(x - 1, y, z).blocksMovement());
			break;
		case 1:	// north/south
			drop = (!world.getBlockMaterial(x, y, z + 1).blocksMovement() && !world.getBlockMaterial(x, y, z - 1).blocksMovement());
			break;
		case 2:	// up/down
			drop = (!world.getBlockMaterial(x, y + 1, z).blocksMovement() && !world.getBlockMaterial(x, y - 1, z).blocksMovement());
			break;
		}
		if (drop && !world.isRemote) {
			dropBlockAsItem(world, x, y, z, 0, 0);
			world.setBlockToAir(x, y, z);
		}
	}

	@Override
	public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z) {
		switch(world.getBlockMetadata(x, y, z) % 3) {
		case 0:	// east/west
			setBlockBounds(0.0F, 0.25F, 0.25F, 1.0F, 0.75F, 0.75F);
			break;
		case 1:	// north/south
			setBlockBounds(0.25F, 0.25F, 0.0F, 0.75F, 0.75F, 1.0F);
			break;
		case 2:	// up/down
			setBlockBounds(0.25F, 0.0F, 0.25F, 0.75F, 1.0F, 0.75F);
			break;
		}
	}

	@Override
	public void setBlockBoundsForItemRender() {
		setBlockBounds(0.0F, 0.25F, 0.25F, 1.0F, 0.75F, 0.75F);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Icon getIcon(int side, int meta) {
		switch(meta % 3) {
		case 0: // east/west
			return (side == 4 || side == 5 ? blockIcon : iconHorizontal);
		case 1: // north/south
			return (side == 2 || side == 3 ? blockIcon : (side == 4 || side == 5 ? iconHorizontal : iconVertical));
		case 2: // up/down
			return (side == 0 || side == 1 ? blockIcon : iconVertical);
		}
		return (side == 0 || side == 1 ? blockIcon : iconVertical);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister register) {
		blockIcon = register.registerIcon(ModInfo.ID + ":" + getUnlocalizedName().substring(9) + "_end");
		iconHorizontal = register.registerIcon(ModInfo.ID + ":" + getUnlocalizedName().substring(9) + "_horizontal");
		iconVertical = register.registerIcon(ModInfo.ID + ":" + getUnlocalizedName().substring(9) + "_vertical");
	}
}
