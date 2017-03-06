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

package zeldaswordskills.block;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import zeldaswordskills.ZSSAchievements;
import zeldaswordskills.api.block.BlockWeight;
import zeldaswordskills.api.block.IHookable;
import zeldaswordskills.api.block.ISmashable;
import zeldaswordskills.api.block.IWhipBlock;
import zeldaswordskills.api.item.ISmashBlock;
import zeldaswordskills.creativetab.ZSSCreativeTabs;
import zeldaswordskills.entity.projectile.EntityWhip;
import zeldaswordskills.ref.Sounds;

/**
 * 
 * A block that can be smashed into the ground and eventually broken.
 * Each smash increments the meta until it reaches MAX_STATE, after which
 * any blow that is at least a level higher than the weight can destroy it.
 * 
 * If not destroyed, the peg will eventually pop back up.
 *
 */
public class BlockPeg extends Block implements IDungeonBlock, IHookable, ISmashable, IWhipBlock
{
	/** Maximum number of 'hits' before a peg is considered fully smashed down */
	private static final int MAX_HITS = 3;
	/** Current number of hits taken, 0 being fully up, 3 fully smashed into the ground */
	public static final PropertyInteger HITS_TAKEN = PropertyInteger.create("hits_taken", 0, MAX_HITS);
	/** The weight of this block, i.e. the difficulty of smashing this block */
	private final BlockWeight weight;

	public BlockPeg(Material material, BlockWeight weight) {
		super(material);
		this.weight = weight;
		disableStats();
		setTickRandomly(true);
		setBlockUnbreakable();
		setResistance(BlockWeight.IMPOSSIBLE.weight);
		setStepSound(soundTypeStone);
		setCreativeTab(ZSSCreativeTabs.tabBlocks);
		setBlockBounds(0.25F, 0.0F, 0.25F, 0.75F, 0.8125F, 0.75F);
	}

	/** Returns appropriate sound based on block material */
	private String getHitSound() {
		return blockMaterial == ZSSBlockMaterials.pegRustyMaterial ? Sounds.HIT_RUSTY : Sounds.HIT_PEG;
	}

	@Override
	public Result canGrabBlock(HookshotType type, World world, BlockPos pos, EnumFacing face) {
		if (face == EnumFacing.UP || face == EnumFacing.DOWN || ((Integer) world.getBlockState(pos).getValue(HITS_TAKEN)).intValue() > 0) {
			return Result.DENY;
		}
		return (type.getBaseType() == HookshotType.MULTI_SHOT ? Result.ALLOW : Result.DEFAULT);
	}

	@Override
	public Result canDestroyBlock(HookshotType type, World world, BlockPos pos, EnumFacing face) {
		return Result.DENY;
	}

	@Override
	public Material getHookableMaterial(HookshotType type, World world, BlockPos pos, EnumFacing face) {
		return (blockMaterial == ZSSBlockMaterials.pegWoodMaterial ? Material.wood : Material.iron);
	}

	@Override
	public BlockWeight getSmashWeight(EntityPlayer player, ItemStack stack, IBlockState state, EnumFacing face) {
		return weight;
	}

	@Override
	public Result onSmashed(World world, EntityPlayer player, ItemStack stack, BlockPos pos, IBlockState state, EnumFacing face) {
		world.playSoundEffect(pos.getX(), pos.getY(), pos.getZ(), getHitSound(), (world.rand.nextFloat() * 0.4F + 0.5F), 1.0F / (world.rand.nextFloat() * 0.4F + 0.5F));
		if (face != EnumFacing.UP) {
			return Result.DENY;
		}
		boolean flag = false;
		int hits = world.getBlockState(pos).getValue(HITS_TAKEN).intValue();
		int impact = 1 + ((ISmashBlock) stack.getItem()).getSmashStrength(player, stack, state, face).ordinal() - weight.ordinal();
		if (impact > 0) {
			flag = hits < MAX_HITS;
			hits += impact;
			if (hits >= MAX_HITS) {
				player.triggerAchievement(ZSSAchievements.hammerTime);
				if (weight.compareTo(BlockWeight.LIGHT) > 0) { // i.e. at least MEDIUM
					player.triggerAchievement(ZSSAchievements.hardHitter);
				}
			}
		}
		if (hits > MAX_HITS && impact > 1) {
			flag = true;
			world.destroyBlock(pos, false);
		} else {
			world.setBlockState(pos, state.withProperty(HITS_TAKEN, Integer.valueOf(Math.min(hits, MAX_HITS))), 3);
		}
		return (flag ? Result.ALLOW : Result.DENY);
	}

	@Override
	public boolean canBreakBlock(WhipType whip, EntityLivingBase thrower, World world, BlockPos pos, EnumFacing face) {
		return false;
	}

	@Override
	public boolean canGrabBlock(WhipType whip, EntityLivingBase thrower, World world, BlockPos pos, EnumFacing face) {
		return (face.getAxis().isHorizontal() && world.getBlockState(pos).getValue(HITS_TAKEN).intValue() < MAX_HITS);
	}

	@Override
	public Result shouldSwing(EntityWhip whip, World world, BlockPos pos, int ticksInGround) {
		if (world.getBlockState(pos).getValue(HITS_TAKEN).intValue() >= MAX_HITS) {
			whip.setDead();
			return Result.DENY;
		}
		return Result.DEFAULT;
	}

	@Override
	public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
		int hits = state.getValue(HITS_TAKEN).intValue();
		if (hits > 0) {
			world.setBlockState(pos, state.withProperty(HITS_TAKEN, Integer.valueOf(hits - 1)), 3);
		}
	}

	@Override
	public int tickRate(World world) {
		return 60;
	}

	@Override
	public boolean isOpaqueCube() {
		return false;
	}

	@Override
	public boolean isFullCube() {
		return false;
	}

	@Override
	public boolean canEntityDestroy(IBlockAccess world, BlockPos pos, Entity entity) {
		return false;
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBox(World world, BlockPos pos, IBlockState state) {
		int hits = state.getValue(HITS_TAKEN).intValue();
		if (hits == 0) {
			return new AxisAlignedBB(pos.getX() + minX, pos.getY() + minY, pos.getZ() + minZ, pos.getX() + maxX, pos.getY() + maxY + 0.5D, pos.getZ() + maxZ);
		} else if (hits >= MAX_HITS) {
			return null;
		} else {
			return super.getCollisionBoundingBox(world, pos, state);
		}
	}

	@Override
	public void setBlockBoundsBasedOnState(IBlockAccess world, BlockPos pos) {
		int hits = Math.min(world.getBlockState(pos).getValue(HITS_TAKEN).intValue(), MAX_HITS);
		setBlockBounds(0.25F, 0.0F, 0.25F, 0.75F, 0.8125F - (hits * 0.1875F), 0.75F);
	}

	@Override
	public void setBlockBoundsForItemRender() {
		setBlockBounds(0.25F, 0.0F, 0.25F, 0.75F, 0.8125F, 0.75F);
	}

	@Override
	public boolean isSameVariant(World world, BlockPos pos, IBlockState state, int meta) {
		return true;
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState().withProperty(HITS_TAKEN, Integer.valueOf(Math.min(meta, MAX_HITS)));
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(HITS_TAKEN).intValue();
	}

	@Override
	protected BlockState createBlockState() {
		return new BlockState(this, HITS_TAKEN);
	}
}
