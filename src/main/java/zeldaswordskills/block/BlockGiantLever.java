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

package zeldaswordskills.block;

import net.minecraft.block.BlockLever;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import zeldaswordskills.api.block.BlockWeight;
import zeldaswordskills.api.block.IWhipBlock;
import zeldaswordskills.client.render.block.RenderGiantLever;
import zeldaswordskills.creativetab.ZSSCreativeTabs;
import zeldaswordskills.entity.projectile.EntityWhip;
import zeldaswordskills.lib.ModInfo;
import zeldaswordskills.util.WorldUtils;
import cpw.mods.fml.common.eventhandler.Event.Result;

/**
 * 
 * Lever that may only be switched on by using a whip.
 * Unbreakable unless it is generating power.
 *
 */
public class BlockGiantLever extends BlockLever implements IWhipBlock
{
	public BlockGiantLever() {
		super();
		setHardness(1.0F);
		setStepSound(soundTypeWood);
		setBlockTextureName(ModInfo.ID + ":lever_giant");
		setCreativeTab(ZSSCreativeTabs.tabBlocks);
		setBlockBounds(0.125F, 0.0F, 0.125F, 0.875F, 0.8F, 0.875F);
	}

	@Override
	public boolean canBreakBlock(WhipType whip, EntityLivingBase thrower, World world, int x, int y, int z, int side) {
		return false;
	}

	@Override
	public boolean canGrabBlock(WhipType whip, EntityLivingBase thrower, World world, int x, int y, int z, int side) {
		return true;
	}

	@Override
	public Result shouldSwing(EntityWhip whip, World world, int x, int y, int z, int ticksInGround) {
		if (ticksInGround > (40 - (whip.getType().ordinal() * 5))) {
			WorldUtils.activateButton(world, this, x, y, z);
			whip.setDead();
		}
		return Result.DENY;
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
		switch(world.getBlockMetadata(x, y, z) & 7) {
		case 0: return AxisAlignedBB.getBoundingBox(x + 0.125F, y + 0.625F, z + 0.2F, x + 0.875F, y + 1.0F, z + 0.8F);
		case 1: return AxisAlignedBB.getBoundingBox(x + 0.0F, y + 0.125F, z + 0.2F, x + 0.375F, y + 0.875F, z + 0.8F);
		case 2: return AxisAlignedBB.getBoundingBox(x + 0.625F, y + 0.125F, z + 0.2F, x + 1.0F, y + 0.875F, z + 0.8F);
		case 3: return AxisAlignedBB.getBoundingBox(x + 0.2F, y + 0.125F, z + 0.0F, x + 0.8F, y + 0.875F, z + 0.375F);
		case 4: return AxisAlignedBB.getBoundingBox(x + 0.2F, y + 0.125F, z + 0.625F, x + 0.8F, y + 0.875F, z + 1.0F);
		case 5: return AxisAlignedBB.getBoundingBox(x + 0.2F, y + 0.0F, z + 0.125F, x + 0.8F, y + 0.375F, z + 0.875F);
		case 6: return AxisAlignedBB.getBoundingBox(x + 0.125F, y + 0.0F, z + 0.2F, x + 0.875F, y + 0.375F, z + 0.8F);
		case 7: return AxisAlignedBB.getBoundingBox(x + 0.2F, y + 0.625F, z + 0.125F, x + 0.8F, y + 1.0F, z + 0.875F);
		}
		return null;
	}

	@Override
	public int getRenderType() {
		return RenderGiantLever.renderId;
	}

	@Override
	public int getMobilityFlag() {
		return 2;
	}

	/**
	 * For the hit-box used for right-click activation and collision detection, but not for preventing movement
	 */
	@Override
	public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z) {
		switch(world.getBlockMetadata(x, y, z) & 7) {
		case 0: setBlockBounds(0.125F, 0.2F, 0.2F, 0.875F, 1.0F, 0.8F); break;
		case 1: setBlockBounds(0.0F, 0.125F, 0.2F, 0.8F, 0.875F, 0.8F); break;
		case 2: setBlockBounds(0.2F, 0.125F, 0.2F, 1.0F, 0.875F, 0.8F); break;
		case 3: setBlockBounds(0.2F, 0.125F, 0.0F, 0.8F, 0.875F, 0.8F); break;
		case 4: setBlockBounds(0.2F, 0.125F, 0.2F, 0.8F, 0.875F, 1.0F); break;
		case 5: setBlockBounds(0.2F, 0.0F, 0.125F, 0.8F, 0.8F, 0.875F); break;
		case 6: setBlockBounds(0.125F, 0.0F, 0.2F, 0.875F, 0.8F, 0.8F); break;
		case 7: setBlockBounds(0.2F, 0.2F, 0.125F, 0.8F, 1.0F, 0.875F); break;
		}
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
		// cannot be activated normally
		return false;
	}

	@Override
	public float getBlockHardness(World world, int x, int y, int z) {
		return (world.getBlockMetadata(x, y, z) > 0x7 ? blockHardness : -1);
	}

	@Override
	public boolean canEntityDestroy(IBlockAccess world, int x, int y, int z, Entity entity) {
		return world.getBlockMetadata(x, y, z) > 0x7;
	}

	@Override
	public float getExplosionResistance(Entity entity, World world, int x, int y, int z, double explosionX, double explosionY, double explosionZ) {
		return (world.getBlockMetadata(x, y, z) > 0x7 ? getExplosionResistance(entity) : BlockWeight.getMaxResistance());
	}
}
