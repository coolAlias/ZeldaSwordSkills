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

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.statemap.IStateMapper;
import net.minecraft.client.renderer.block.statemap.StateMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.EnumPlantType;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import zeldaswordskills.api.block.IBoomerangBlock;
import zeldaswordskills.api.block.IExplodable;
import zeldaswordskills.api.block.IQuakeBlock;
import zeldaswordskills.api.block.IWhipBlock;
import zeldaswordskills.api.entity.BombType;
import zeldaswordskills.api.entity.CustomExplosion;
import zeldaswordskills.api.entity.IEntityBomb;
import zeldaswordskills.entity.projectile.EntityBomb;
import zeldaswordskills.entity.projectile.EntityBoomerang;
import zeldaswordskills.entity.projectile.EntityWhip;
import zeldaswordskills.item.ZSSItems;
import zeldaswordskills.util.PlayerUtils;
import zeldaswordskills.util.TargetUtils;

public class BlockBombFlower extends BlockCrops implements IBoomerangBlock, ICustomStateMapper, IExplodable, IQuakeBlock, IWhipBlock
{
	/** Uses bit 8 (true) to flag this block for an explosion next update tick */
	public static final PropertyBool EXPLODE = PropertyBool.create("explode");

	public BlockBombFlower() {
		super();
		setCreativeTab(null); // no Creative Tab
		setDefaultState(blockState.getBaseState().withProperty(AGE, Integer.valueOf(0)).withProperty(EXPLODE, Boolean.valueOf(false)));
	}

	@Override
	public EnumPlantType getPlantType(IBlockAccess world, BlockPos pos) {
		return EnumPlantType.Cave;
	}

	@Override
	protected Item getSeed() {
		return ZSSItems.bombFlowerSeed;
	}

	@Override
	protected Item getCrop() {
		return ZSSItems.bombFlowerSeed;
	}

	@Override
	public boolean canDropFromExplosion(Explosion explosion) {
		return false;
	}

	@Override
	public boolean canPlaceBlockAt(World world, BlockPos pos) {
		return super.canPlaceBlockAt(world, pos) && canBlockStay(world, pos, world.getBlockState(pos));
	}

	@Override
	protected boolean canPlaceBlockOn(Block block) {
		return block.getMaterial() == Material.rock;
	}

	@Override
	public boolean canBlockStay(World world, BlockPos pos, IBlockState state) {
		Block soil = world.getBlockState(pos.down()).getBlock();
		return canPlaceBlockOn(soil) && hasLava(world, pos.down());
	}

	private boolean hasLava(World world, BlockPos pos) {
		boolean hasLava = (
				world.getBlockState(pos.north()).getBlock().getMaterial() == Material.lava ||
				world.getBlockState(pos.south()).getBlock().getMaterial() == Material.lava ||
				world.getBlockState(pos.east()).getBlock().getMaterial() == Material.lava ||
				world.getBlockState(pos.west()).getBlock().getMaterial() == Material.lava);
		return hasLava;
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumFacing face, float hitX, float hitY, float hitZ) {
		if (player.getHeldItem() != null || ((Integer) state.getValue(AGE)).intValue() != 7) {
			return false; // this lets bonemeal do its thing
		} else if (!world.isRemote) {
			player.setCurrentItemOrArmor(0, new ItemStack(ZSSItems.bomb,1,BombType.BOMB_FLOWER.ordinal()));
			world.setBlockState(pos, getDefaultState());
		}
		return true;
	}

	@Override
	public void onBlockClicked(World world, BlockPos pos, EntityPlayer player) {
		if (((Integer) world.getBlockState(pos).getValue(AGE)).intValue() == 7) {
			if (PlayerUtils.isHoldingWeapon(player)) {
				createExplosion(world, pos, true);
			} else {
				disperseSeeds(world, pos, true);
			}
		}
	}

	@Override
	public void onBlockExploded(World world, BlockPos pos, Explosion explosion) {
		IBlockState state = world.getBlockState(pos);
		if (((Boolean) state.getValue(EXPLODE)).booleanValue()) {
			// do nothing, these will explode on their own next update tick
		} else if (((Integer) state.getValue(AGE)).intValue() == 7) {
			world.setBlockState(pos, state.withProperty(EXPLODE, Boolean.valueOf(true)), 2);
			world.scheduleUpdate(pos, this, 5);
		} else {
			Entity exploder = (explosion instanceof CustomExplosion ? ((CustomExplosion) explosion).exploder : null);
			if (!(exploder instanceof IEntityBomb) || ((IEntityBomb) exploder).getType() != BombType.BOMB_FLOWER) {
				super.onBlockExploded(world, pos, explosion);
			}
		}
	}

	@Override
	public boolean onBoomerangCollided(World world, BlockPos pos, IBlockState state, EntityBoomerang boomerang) {
		if (!world.isRemote && ((Integer) state.getValue(AGE)).intValue() == 7) {
			boolean captured = false;
			world.setBlockState(pos, getDefaultState());
			EntityItem bomb = new EntityItem(world, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, new ItemStack(ZSSItems.bomb, 1, BombType.BOMB_FLOWER.ordinal()));
			world.spawnEntityInWorld(bomb);
			if (boomerang.captureItem(bomb)) {
				captured = true; // stop explosion from happening
			} else {
				bomb.setDead();
			}
			if (!captured) {
				createExplosion(world, pos, true);
			}
		}
		return false;
	}

	@Override
	public void onEntityCollidedWithBlock(World world, BlockPos pos, Entity entity) {
		this.onEntityCollidedWithBlock(world, pos, world.getBlockState(pos), entity);
	}

	@Override
	public void onEntityCollidedWithBlock(World world, BlockPos pos, IBlockState state, Entity entity) {
		if (((Integer) state.getValue(AGE)).intValue() == 7 && entity instanceof IProjectile) {
			createExplosion(world, pos, true);
		}
	}

	@Override
	public void handleQuakeEffect(World world, BlockPos pos, IBlockState state, EntityPlayer player) {
		if (((Integer) state.getValue(AGE)).intValue() == 7) {
			// TODO call disperseSeeds instead? schedule an update tick?
			createExplosion(world, pos, true);
		}
	}

	@Override
	public boolean canBreakBlock(WhipType whip, EntityLivingBase thrower, World world, BlockPos pos, EnumFacing face) {
		return getGrowthStage(((Integer) world.getBlockState(pos).getValue(AGE)).intValue()) < 2;
	}

	@Override
	public boolean canGrabBlock(WhipType whip, EntityLivingBase thrower, World world, BlockPos pos, EnumFacing face) {
		return ((Integer) world.getBlockState(pos).getValue(AGE)).intValue() == 7;
	}

	@Override
	public Result shouldSwing(EntityWhip whip, World world, BlockPos pos, int ticksInGround) {
		if (ticksInGround > 30 && ((Integer) world.getBlockState(pos).getValue(AGE)).intValue() == 7) {
			EntityLivingBase thrower = whip.getThrower();
			EntityItem bomb = new EntityItem(world, whip.posX, whip.posY + 1, whip.posZ, new ItemStack(ZSSItems.bomb, 1, BombType.BOMB_FLOWER.ordinal()));
			double dx = thrower.posX - bomb.posX;
			double dy = (thrower.posY + thrower.getEyeHeight()) - (bomb.posY - 1);
			double dz = thrower.posZ - bomb.posZ;
			TargetUtils.setEntityHeading(bomb, dx, dy + 0.5D, dz, 1.0F, 0.0F, true);
			if (!world.isRemote) {
				world.spawnEntityInWorld(bomb);
			}
			world.setBlockToAir(pos);
			whip.setDead();
		}
		return Result.DENY;
	}

	/**
	 * Override updateTick because bomb flowers don't care about fertile soil or water
	 * Don't call super, so make sure to call checkAndDropBlock from BlockBush
	 * Note that updateTick is only ever called on the server
	 */
	@Override
	public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
		checkAndDropBlock(world, pos, state);
		int meta = getMetaFromState(state);
		if (meta > 7) {
			createExplosion(world, pos, true);
		} else if (meta < 7 && rand.nextInt(6) == 0) {
			world.setBlockState(pos, state.withProperty(AGE, Integer.valueOf(meta + 1)), 2);
		} else if (meta == 7 && rand.nextInt(16) == 0) {
			disperseSeeds(world, pos, false);
		}
	}

	/**
	 * Creates an immediate explosion at the block's coordinates, optionally setting the block to air
	 */
	private void createExplosion(World world, BlockPos pos, boolean toAir) {
		if (!world.isRemote) {
			if (toAir) {
				world.setBlockToAir(pos);
			}
			CustomExplosion.createExplosion(world, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, 3.0F, BombType.BOMB_FLOWER);
		}
	}

	/**
	 * Spawns a bomb entity at the block's position and sets the growth stage back to zero
	 */
	private void disperseSeeds(World world, BlockPos pos, boolean isGriefing) {
		if (!world.isRemote) {
			world.setBlockState(pos, getDefaultState());
			EntityBomb bomb = new EntityBomb(world, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D).setType(BombType.BOMB_FLOWER).setFuseTime(64);
			if (!isGriefing) {
				bomb.setNoGrief();
			}
			world.spawnEntityInWorld(bomb);
		}
	}

	/**
	 * Returns the growth stage from meta, i.e. 0 (seedling), 1, 2, or 3 (fully mature)
	 */
	private int getGrowthStage(int meta) {
		meta &= 0x7; // only care about the first 7 bits
		return meta == 6 ? 2 : meta >> 1;
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBox(World world, BlockPos pos, IBlockState state) {
		int stage = getGrowthStage(((Integer) state.getValue(AGE)).intValue());
		if (stage == 0) {
			return null;
		}
		return new AxisAlignedBB(pos.getX() + minX, pos.getY() + minY, pos.getZ() + minZ, pos.getX() + maxX, pos.getY() + maxY, pos.getZ() + maxZ);
	}

	@Override
	public void setBlockBoundsBasedOnState(IBlockAccess world, BlockPos pos) {
		int stage = getGrowthStage(((Integer) world.getBlockState(pos).getValue(AGE)).intValue());
		setBlockBounds(0.1F, 0.0F, 0.1F, 0.9F, 0.2F + (stage * 0.15F), 0.9F);
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		Boolean explode = Boolean.valueOf((meta & 0x8) > 0);
		return this.getDefaultState().withProperty(AGE, Integer.valueOf(meta & 0x7)).withProperty(EXPLODE, explode);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		int i = ((Integer) state.getValue(AGE)).intValue();
		if (((Boolean) state.getValue(EXPLODE)).booleanValue()) {
			i |= 0x8;
		}
		return i;
	}

	@Override
	protected BlockState createBlockState() {
		return new BlockState(this, AGE, EXPLODE);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IStateMapper getCustomStateMap() {
		return (new StateMap.Builder()).ignore(EXPLODE).build();
	}
}
