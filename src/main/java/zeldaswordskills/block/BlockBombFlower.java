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
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.EnumPlantType;
import zeldaswordskills.api.block.IBoomerangBlock;
import zeldaswordskills.api.block.IExplodable;
import zeldaswordskills.api.block.IQuakeBlock;
import zeldaswordskills.api.block.IWhipBlock;
import zeldaswordskills.api.entity.BombType;
import zeldaswordskills.api.entity.CustomExplosion;
import zeldaswordskills.api.entity.IEntityBomb;
import zeldaswordskills.client.render.block.RenderSpecialCrop;
import zeldaswordskills.entity.projectile.EntityBomb;
import zeldaswordskills.entity.projectile.EntityBoomerang;
import zeldaswordskills.entity.projectile.EntityWhip;
import zeldaswordskills.item.ZSSItems;
import zeldaswordskills.ref.ModInfo;
import zeldaswordskills.util.PlayerUtils;
import zeldaswordskills.util.SideHit;
import zeldaswordskills.util.TargetUtils;
import cpw.mods.fml.common.eventhandler.Event.Result;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockBombFlower extends BlockCrops implements IBoomerangBlock, IExplodable, IQuakeBlock, IWhipBlock
{
	@SideOnly(Side.CLIENT)
	private IIcon[] iconArray;

	public BlockBombFlower() {
		super();
		setCreativeTab(null); // no Creative Tab
	}

	@Override
	public EnumPlantType getPlantType(IBlockAccess world, int x, int y, int z) {
		return EnumPlantType.Cave;
	}

	/**
	 * Item dropped when crop not yet mature
	 */
	@Override
	protected Item func_149866_i() {
		return ZSSItems.bombFlowerSeed;
	}

	/**
	 * Item harvested when crop fully mature
	 */
	@Override
	protected Item func_149865_P() {
		return ZSSItems.bombFlowerSeed;
	}

	@Override
	public boolean canDropFromExplosion(Explosion explosion) {
		return false;
	}

	@Override
	protected boolean canPlaceBlockOn(Block block) {
		return block.getMaterial() == Material.rock;
	}

	@Override
	public boolean canBlockStay(World world, int x, int y, int z) {
		Block soil = world.getBlock(x, y - 1, z);
		return canPlaceBlockOn(soil) && hasLava(world, x, y - 1, z);
	}

	private boolean hasLava(World world, int x, int y, int z) {
		boolean hasLava = (
				world.getBlock(x - 1, y, z).getMaterial() == Material.lava ||
				world.getBlock(x + 1, y, z).getMaterial() == Material.lava ||
				world.getBlock(x, y, z - 1).getMaterial() == Material.lava ||
				world.getBlock(x, y, z + 1).getMaterial() == Material.lava);
		return hasLava;
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
		if (player.getHeldItem() != null || world.getBlockMetadata(x, y, z) != 7) {
			return false; // this lets bonemeal do its thing
		} else if (!world.isRemote) {
			player.setCurrentItemOrArmor(0, new ItemStack(ZSSItems.bomb,1,BombType.BOMB_FLOWER.ordinal()));
			world.setBlockMetadataWithNotify(x, y, z, 0, 2);
		}
		return true;
	}

	@Override
	public void onBlockClicked(World world, int x, int y, int z, EntityPlayer player) {
		if (world.getBlockMetadata(x, y, z) == 7) {
			if (PlayerUtils.isHoldingWeapon(player)) {
				createExplosion(world, x, y, z, true);
			} else {
				disperseSeeds(world, x, y, z, true);
			}
		}
	}

	@Override
	public void onBlockExploded(World world, int x, int y, int z, Explosion explosion) {
		int meta = world.getBlockMetadata(x, y, z);
		if (meta == 7) {
			// full growth stage (0x7) for rendering, plus bit 8 to make the block explode next update tick
			world.setBlockMetadataWithNotify(x, y, z, 15, 2);
			world.scheduleBlockUpdate(x, y, z, this, 5);
		} else if (meta < 7) {
			Entity exploder = (explosion instanceof CustomExplosion ? ((CustomExplosion) explosion).exploder : null);
			if (!(exploder instanceof IEntityBomb) || ((IEntityBomb) exploder).getType() != BombType.BOMB_FLOWER) {
				super.onBlockExploded(world, x, y, z, explosion);
			}
		}
		// meta 8+ do nothing, these will explode on their own next update tick
	}

	@Override
	public boolean onBoomerangCollided(World world, int x, int y, int z, EntityBoomerang boomerang) {
		if (!world.isRemote && world.getBlockMetadata(x, y, z) == 7) {
			boolean captured = false;
			world.setBlockMetadataWithNotify(x, y, z, 0, 2);
			EntityItem bomb = new EntityItem(world, x + 0.5D, y + 0.5D, z + 0.5D, new ItemStack(ZSSItems.bomb, 1, BombType.BOMB_FLOWER.ordinal()));
			world.spawnEntityInWorld(bomb);
			if (boomerang.captureItem(bomb)) {
				captured = true; // stop explosion from happening
			} else {
				bomb.setDead();
			}
			if (!captured) {
				createExplosion(world, x, y, z, true);
			}
		}
		return false;
	}

	@Override
	public void onEntityCollidedWithBlock(World world, int x, int y, int z, Entity entity) {
		if (world.getBlockMetadata(x, y, z) == 7 && entity instanceof IProjectile) {
			createExplosion(world, x, y, z, true);
		}
	}

	@Override
	public void handleQuakeEffect(World world, int x, int y, int z, EntityPlayer player) {
		if (world.getBlockMetadata(x, y, z) == 7) {
			// TODO call disperseSeeds instead? schedule an update tick?
			createExplosion(world, x, y, z, true);
		}
	}

	@Override
	public boolean canBreakBlock(WhipType whip, EntityLivingBase thrower, World world, int x, int y, int z, int side) {
		return getGrowthStage(world.getBlockMetadata(x, y, z)) < 2;
	}

	@Override
	public boolean canGrabBlock(WhipType whip, EntityLivingBase thrower, World world, int x, int y, int z, int side) {
		return (side != SideHit.BOTTOM && world.getBlockMetadata(x, y, z) == 7);
	}

	@Override
	public Result shouldSwing(EntityWhip whip, World world, int x, int y, int z, int ticksInGround) {
		if (ticksInGround > 30 && world.getBlockMetadata(x, y, z) == 7) {
			EntityLivingBase thrower = whip.getThrower();
			EntityItem bomb = new EntityItem(world, whip.posX, whip.posY + 1, whip.posZ, new ItemStack(ZSSItems.bomb, 1, BombType.BOMB_FLOWER.ordinal()));
			double dx = thrower.posX - bomb.posX;
			double dy = (thrower.posY + thrower.getEyeHeight()) - bomb.posY;
			double dz = thrower.posZ - bomb.posZ;
			TargetUtils.setEntityHeading(bomb, dx, dy * 1.5D, dz, 1.0F, 0.0F, true);
			if (!world.isRemote) {
				world.spawnEntityInWorld(bomb);
			}
			world.setBlockToAir(x, y, z);
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
	public void updateTick(World world, int x, int y, int z, Random rand) {
		checkAndDropBlock(world, x, y, z);
		int meta = world.getBlockMetadata(x, y, z);
		if (meta > 7) {
			createExplosion(world, x, y, z, true);
		} else if (meta < 7 && rand.nextInt(6) == 0) {
			world.setBlockMetadataWithNotify(x, y, z, meta + 1, 2);
		} else if (meta == 7 && rand.nextInt(16) == 0) {
			disperseSeeds(world, x, y, z, false);
		}
	}

	/**
	 * Creates an immediate explosion at the block's coordinates, optionally setting the block to air
	 */
	private void createExplosion(World world, int x, int y, int z, boolean toAir) {
		if (!world.isRemote) {
			if (toAir) {
				world.setBlockToAir(x, y, z);
			}
			CustomExplosion.createExplosion(world, x, y, z, 3.0F, BombType.BOMB_FLOWER);
		}
	}

	/**
	 * Spawns a bomb entity at the block's position and sets the growth stage back to zero
	 */
	private void disperseSeeds(World world, int x, int y, int z, boolean isGriefing) {
		if (!world.isRemote) {
			world.setBlockMetadataWithNotify(x, y, z, 0, 2);
			EntityBomb bomb = new EntityBomb(world, x + 0.5D, y + 0.5D, z + 0.5D).setType(BombType.BOMB_FLOWER).setFuseTime(64);
			if (!isGriefing) {
				bomb.setNoGrief();
			}
			world.spawnEntityInWorld(bomb);
		}
	}

	@Override
	public int getRenderType() {
		return RenderSpecialCrop.renderId;
	}

	/**
	 * Returns the growth stage from meta, i.e. 0 (seedling), 1, 2, or 3 (fully mature)
	 */
	private int getGrowthStage(int meta) {
		meta &= 0x7; // only care about the first 7 bits
		return meta == 6 ? 2 : meta >> 1;
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
		int stage = getGrowthStage(world.getBlockMetadata(x, y, z));
		if (stage == 0) {
			return null;
		}
		return AxisAlignedBB.getBoundingBox(x + minX, y + minY, z + minZ, x + maxX, y + maxY, z + maxZ);
	}

	@Override
	public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z) {
		int stage = getGrowthStage(world.getBlockMetadata(x, y, z));
		setBlockBounds(0.1F, 0.0F, 0.1F, 0.9F, 0.2F + (stage * 0.15F), 0.9F);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int meta) {
		return iconArray[getGrowthStage(meta)];
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister register) {
		String s = ModInfo.ID + ":" + getUnlocalizedName().substring(9) + "_stage_";
		iconArray = new IIcon[4];
		for (int i = 0; i < iconArray.length; ++i) {
			iconArray[i] = register.registerIcon(s + i);
		}
	}
}
