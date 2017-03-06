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

package zeldaswordskills.api.entity;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentProtection;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.server.S27PacketExplosion;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSourceIndirect;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import zeldaswordskills.api.block.IExplodable;
import zeldaswordskills.block.ZSSBlocks;
import zeldaswordskills.entity.projectile.EntityBomb;
import zeldaswordskills.ref.Config;
import zeldaswordskills.ref.Sounds;

/**
 * 
 * Custom Explosion class that allows for specifying whether or not to destroy blocks,
 * whether to inflict damage, how much and what type of damage to inflict, whether to
 * exclude liquids from the calculations, and also what particles to spawn under
 * various circumstances.
 * 
 * Extends vanilla Explosion class to allow substitution in several vanilla methods.
 * 
 * TODO allow setting custom particles to spawn
 * TODO allow setting custom sound filepath
 * 
 * @auther coolAlias
 *
 */
public class CustomExplosion extends Explosion
{
	/**
	 * Returns an explosion damage source with the thrower of the explosive, if
	 * available, set as the indirect entity.
	 */
	public static DamageSource getExplosionSource(CustomExplosion explosion) {
		if (explosion.exploder instanceof EntityThrowable) {
			return new EntityDamageSourceIndirect("explosion", explosion.exploder, ((EntityThrowable) explosion.exploder).getThrower()).setExplosion().setDifficultyScaled();
		}
		return DamageSource.setExplosionSource(explosion);
	}

	/** Type of liquids to ignore when calculating which blocks to affect */
	public static enum IgnoreLiquid{NONE, ALL, WATER, LAVA};

	// Duplicate final private values in Explosion class
	public final boolean isSmoking;
	public final boolean isFlaming;
	public final double explosionX;
	public final double explosionY;
	public final double explosionZ;
	public final Entity exploder;
	public final float explosionSize;
	// Super class field not needed as none of the super methods are called
	private final List<BlockPos> affectedBlockPositions;

	/**
	 * Creates an explosion based on the BombType given, automatically applying
	 * various characteristics. If more versatility than this is required, create a
	 * CustomExplosion object from scratch rather than using the static methods and
	 * call doExplosionA(), then doExplosionB().
	 */
	public static void createExplosion(World world, double x, double y, double z, float radius, BombType type) {
		createExplosion(new EntityBomb(world).setType(type), world, x, y, z, radius, 0.0F, true);
	}

	/**
	 * Creates an explosion based on the IEntityBomb given, automatically applying
	 * various characteristics. If more versatility than this is required, create a
	 * CustomExplosion object from scratch rather than using the static methods and
	 * call doExplosionA(), then doExplosionB().
	 * @param damage Use 0.0F for vanilla explosion damage; amounts above zero will cause a flat amount regardless of distance
	 */
	public static void createExplosion(IEntityBomb bomb, World world, double x, double y, double z, float radius, float damage, boolean canGrief) {
		CustomExplosion explosion = new CustomExplosion(world, (Entity) bomb, x, y, z, radius, canGrief, false).setDamage(damage);
		BombType type = bomb.getType();
		// TODO Adventure Mode is only set per player, not per world
		//boolean isAdventureMode = (world.getWorldInfo().getGameType() == GameType.ADVENTURE);
		boolean restrictBlocks = false; // (isAdventureMode && !bomb.canGriefAdventureMode());
		explosion.setMotionFactor(bomb.getMotionFactor());
		explosion.scalesWithDistance = (damage == 0.0F);
		explosion.targetBlock = ((restrictBlocks || Config.onlyBombSecretStone()) ? ZSSBlocks.secretStone : null);
		explosion.ignoreLiquidType = type.ignoreLiquidType;
		float f = bomb.getDestructionFactor();
		// TODO doesn't allow for any other 'hell' type dimensions...
		if (world.provider.getDimensionName().equals("Nether") && type != BombType.BOMB_FIRE) {
			f *= 0.5F;
		}
		explosion.restrictExplosionBy(f);
		explosion.doExplosionA();
		explosion.doExplosionB(true);
		if (bomb.hasPostExplosionEffect()) {
			type.postExplosionEffect(world, explosion);
		}
	}

	/** Whether or not this explosion will damage entities within the blast explosionSize */
	public boolean inflictsDamage = true;

	/** Whether the damage amount scales with distance from explosion's center, as well as chance of catching fire */
	public boolean scalesWithDistance = true;

	/** Exclude a certain type of liquid when determining which blocks are affected, usually making the explosion more devastating */
	public IgnoreLiquid ignoreLiquidType = IgnoreLiquid.NONE;

	/** Specific block to target, if any (defaults to all blocks) */
	public Block targetBlock = null;

	/** Type of DamageSource that this explosion will cause; leave null for vanilla explosion damage */
	protected DamageSource source = null;

	/** Amount of damage to inflict; if 0.0F, vanilla explosion damage amount will be used instead */
	protected float damage = 0.0F;

	/** Amount of time for which affected entities will be set on fire; only if explosion isFlaming */
	protected int burnTime = 0;

	/** Factor by which affected entity's motion will be multiplied */
	protected float motionFactor = 1.0F;

	/** Maximum explosionSize within which blocks can be affected, regardless of explosion size */
	protected static final int MAX_RADIUS = 16;

	/** Restricts (or expands) the blast radius for destroying blocks by this factor */
	protected float restrictExplosion = 1.0F;

	/** Originally private Random from super class */
	protected final Random rand = new Random();

	/** Originally private World object from super class */
	protected final World worldObj;

	/** Maps players to the knockback vector applied by the explosion, to send to the client; private in original class */
	protected Map<EntityPlayer, Vec3> playerKnockbackMap = new HashMap<EntityPlayer, Vec3>();

	@SideOnly(Side.CLIENT)
	public CustomExplosion(World world, Entity exploder, double x, double y, double z, float explosionSize, boolean isSmoking, boolean isFlaming, List<BlockPos> affectedBlocks) {
		this(world, exploder, x, y, z, explosionSize, isSmoking, isFlaming);
		this.affectedBlockPositions.addAll(affectedBlocks);
	}

	/**
	 * Creates a custom explosion; isSmoking is true by default, and isFlaming is false by default
	 */
	public CustomExplosion(World world, Entity exploder, double x, double y, double z, float explosionSize) {
		this(world, exploder, x, y, z, explosionSize, true, false);
	}

	public CustomExplosion(World world, Entity exploder, double x, double y, double z, float explosionSize, boolean isSmoking, boolean isFlaming) {
		super(world, exploder, x, y, z, explosionSize, isSmoking, isFlaming);
		this.worldObj = world;
		this.isSmoking = isSmoking;
		this.isFlaming = isFlaming;
		this.explosionX = x;
		this.explosionY = y;
		this.explosionZ = z;
		this.explosionSize = explosionSize;
		this.exploder = exploder;
		this.affectedBlockPositions = Lists.newArrayList();
	}

	/**
	 * Sets the DamageSource this explosion will cause; returns itself for convenience
	 */
	public CustomExplosion setSource(DamageSource source) {
		this.source = source;
		return this;
	}

	/**
	 * Returns the damage source to use; if source is null, default explosion-type DamageSource is used
	 */
	protected DamageSource getDamageSource() {
		return (source != null ? source : CustomExplosion.getExplosionSource(this));
	}

	/**
	 * Sets amount of damage this explosion will cause; returns itself for convenience
	 */
	public CustomExplosion setDamage(float amount) {
		damage = amount;
		return this;
	}

	/**
	 * Sets amount of time for which affected entities will burn; returns itself for convenience
	 */
	public CustomExplosion setBurnTime(int ticks) {
		burnTime = ticks;
		return this;
	}

	/**
	 * Sets amount by which entity's motion will be multiplied
	 */
	public CustomExplosion setMotionFactor(float amount) {
		motionFactor = amount;
		return this;
	}

	/**
	 * Sets the factor by which to restrict block destruction (smaller factor means less destruction; 1.0F is normal size);
	 * returns itself for convenience
	 */
	public CustomExplosion restrictExplosionBy(float factor) {
		restrictExplosion = factor;
		return this;
	}

	/**
	 * Does the first part of the explosion (destroy blocks)
	 */
	@Override
	public void doExplosionA() {
		if (isSmoking && restrictExplosion > 0) {
			populateAffectedBlocksList();
		}
		if (inflictsDamage) {
			affectEntitiesWithin();
		}
	}

	/**
	 * Does the second part of the explosion (sound, particles, drop spawn)
	 */
	@Override
	public void doExplosionB(boolean spawnExtraParticles) {
		worldObj.playSoundEffect(explosionX, explosionY, explosionZ, Sounds.EXPLOSION, 4.0F, (1.0F + (worldObj.rand.nextFloat() - worldObj.rand.nextFloat()) * 0.2F) * 0.7F);
		if (explosionSize >= 2.0F && isSmoking) {
			worldObj.spawnParticle(EnumParticleTypes.EXPLOSION_HUGE, explosionX, explosionY, explosionZ, 1.0D, 0.0D, 0.0D);
		} else {
			worldObj.spawnParticle(EnumParticleTypes.EXPLOSION_LARGE, explosionX, explosionY, explosionZ, 1.0D, 0.0D, 0.0D);
		}

		Iterator<BlockPos> iterator;
		BlockPos blockpos;
		Block block;
		if (isSmoking) {
			iterator = affectedBlockPositions.iterator();
			while (iterator.hasNext()) {
				blockpos = iterator.next();
				block = worldObj.getBlockState(blockpos).getBlock();
				explodeBlockAt(block, blockpos, spawnExtraParticles);
			}
		}

		if (isFlaming) {
			iterator = affectedBlockPositions.iterator();
			while (iterator.hasNext()) {
				blockpos = iterator.next();
				block = worldObj.getBlockState(blockpos).getBlock();
				if (block == Blocks.air && worldObj.getBlockState(blockpos.down()).getBlock().isFullBlock() && rand.nextInt(3) == 0) {
					worldObj.setBlockState(blockpos, Blocks.fire.getDefaultState());
				}
			}
		}

		notifyClients();
	}

	/**
	 * Actually explodes the block and spawns particles if allowed
	 */
	private void explodeBlockAt(Block block, BlockPos blockpos, boolean spawnExtraParticles) {
		if (spawnExtraParticles) {
			double d0 = (double)((float) blockpos.getX() + worldObj.rand.nextFloat());
			double d1 = (double)((float) blockpos.getY() + worldObj.rand.nextFloat());
			double d2 = (double)((float) blockpos.getZ() + worldObj.rand.nextFloat());
			double d3 = d0 - explosionX;
			double d4 = d1 - explosionY;
			double d5 = d2 - explosionZ;
			double d6 = (double) MathHelper.sqrt_double(d3 * d3 + d4 * d4 + d5 * d5);
			d3 /= d6;
			d4 /= d6;
			d5 /= d6;
			double d7 = 0.5D / (d6 / (double) explosionSize + 0.1D);
			d7 *= (double)(worldObj.rand.nextFloat() * worldObj.rand.nextFloat() + 0.3F);
			d3 *= d7;
			d4 *= d7;
			d5 *= d7;
			worldObj.spawnParticle(EnumParticleTypes.EXPLOSION_NORMAL, (d0 + explosionX * 1.0D) / 2.0D, (d1 + explosionY * 1.0D) / 2.0D, (d2 + explosionZ * 1.0D) / 2.0D, d3, d4, d5);
			worldObj.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, d0, d1, d2, d3, d4, d5);
		}
		if (block.getMaterial() != Material.air) {
			if (block.canDropFromExplosion(this)) {
				block.dropBlockAsItemWithChance(worldObj, blockpos, worldObj.getBlockState(blockpos), 1.0F /  explosionSize, 0);
			}
			block.onBlockExploded(worldObj, blockpos, this);
		}
	}

	/**
	 * Populates the affectedBlocksList with any blocks that should be affected by this explosion
	 */
	protected void populateAffectedBlocksList() {
		HashSet<BlockPos> hashset = Sets.newHashSet();
		float radius = Math.min(explosionSize * restrictExplosion, 16.0F);
		for (int i = 0; i < MAX_RADIUS; ++i) {
			for (int j = 0; j < MAX_RADIUS; ++j) {
				for (int k = 0; k < MAX_RADIUS; ++k) {
					if (i == 0 || i == MAX_RADIUS - 1 || j == 0 || j == MAX_RADIUS - 1 || k == 0 || k == MAX_RADIUS - 1)
					{
						double d3 = (double)((float)i / ((float) MAX_RADIUS - 1.0F) * 2.0F - 1.0F);
						double d4 = (double)((float)j / ((float) MAX_RADIUS - 1.0F) * 2.0F - 1.0F);
						double d5 = (double)((float)k / ((float) MAX_RADIUS - 1.0F) * 2.0F - 1.0F);
						double d6 = Math.sqrt(d3 * d3 + d4 * d4 + d5 * d5);
						d3 /= d6;
						d4 /= d6;
						d5 /= d6;
						float f1 = radius * (0.7F + worldObj.rand.nextFloat() * 0.6F);
						double d0 = explosionX;
						double d1 = explosionY;
						double d2 = explosionZ;

						for (float f2 = 0.3F; f1 > 0.0F; f1 -= 0.22500001F)
						{
							int l = MathHelper.floor_double(d0);
							int i1 = MathHelper.floor_double(d1);
							int j1 = MathHelper.floor_double(d2);
							BlockPos blockpos = new BlockPos(l, i1, j1);
							IBlockState iblockstate = worldObj.getBlockState(blockpos);
							Block block = iblockstate.getBlock();
							Material material = block.getMaterial();
							if (material != Material.air) {
								// True if block resistance should reduce the explosion radius
								boolean flag = !material.isLiquid() || ignoreLiquidType == IgnoreLiquid.NONE || 
										(ignoreLiquidType == IgnoreLiquid.WATER && material != Material.water) ||
										(ignoreLiquidType == IgnoreLiquid.LAVA && material != Material.lava);
								if (flag) {
									float f3 = exploder != null ? exploder.getExplosionResistance(this, worldObj, blockpos, iblockstate) : block.getExplosionResistance(worldObj, blockpos, null, this);
									f1 -= (f3 + 0.3F) * 0.3F;
								}
							}
							if (f1 > 0.0F && (targetBlock == null || block == targetBlock || block instanceof IExplodable) &&
									(exploder == null || exploder.verifyExplosion(this, worldObj, blockpos, iblockstate, f1)))
							{
								hashset.add(blockpos);
							}

							d0 += d3 * (double)f2;
							d1 += d4 * (double)f2;
							d2 += d5 * (double)f2;
						}
					}
				}
			}
		}

		affectedBlockPositions.addAll(hashset);
	}

	/**
	 * Affects all entities within the explosion, causing damage if flagged to do so
	 */
	protected void affectEntitiesWithin() {
		float diameter = explosionSize * 2.0F;
		int i1 = MathHelper.floor_double(explosionX - (double) explosionSize - 1.0D);
		int j1 = MathHelper.floor_double(explosionY - (double) explosionSize - 1.0D);
		int k1 = MathHelper.floor_double(explosionZ - (double) explosionSize - 1.0D);
		int i2 = MathHelper.floor_double(explosionX + (double) explosionSize + 1.0D);
		int j2 = MathHelper.floor_double(explosionY + (double) explosionSize + 1.0D);
		int k2 = MathHelper.floor_double(explosionZ + (double) explosionSize + 1.0D);
		List<Entity> list = worldObj.getEntitiesWithinAABBExcludingEntity(exploder, new AxisAlignedBB((double)i1, (double)j1, (double)k1, (double)i2, (double)j2, (double)k2));
		net.minecraftforge.event.ForgeEventFactory.onExplosionDetonate(worldObj, this, list, diameter);
		Vec3 vec3 = new Vec3(explosionX, explosionY, explosionZ);
		for (int n = 0; n < list.size(); ++n) {
			Entity entity = list.get(n);
			if (entity.isImmuneToExplosions()) {
				continue;
			}
			double d7 = (scalesWithDistance ? entity.getDistance(explosionX, explosionY, explosionZ) / (double) diameter : 0.0D);
			if (d7 <= 1.0D) {
				double d0 = entity.posX - explosionX;
				double d1 = entity.posY + (double) entity.getEyeHeight() - explosionY;
				double d2 = entity.posZ - explosionZ;
				double d8 = (double) MathHelper.sqrt_double(d0 * d0 + d1 * d1 + d2 * d2);
				if (d8 != 0.0D) {
					d0 /= d8;
					d1 /= d8;
					d2 /= d8;
					double d9 = (double) worldObj.getBlockDensity(vec3, entity.getEntityBoundingBox());
					double d10 = (1.0D - d7) * d9;
					float amount = (damage == 0.0F ? (float)((int)((d10 * d10 + d10) / 2.0D * 8.0D * diameter + 1.0D)) : damage * (float) d10);
					if (entity.attackEntityFrom(getDamageSource(), amount) && isFlaming && !entity.isImmuneToFire()) {
						if (!scalesWithDistance || rand.nextFloat() < d10) {
							entity.setFire(burnTime);
						}
					}
					double d11 = EnchantmentProtection.func_92092_a(entity, d10);
					entity.motionX += d0 * d11 * motionFactor;
					entity.motionY += d1 * d11 * motionFactor;
					entity.motionZ += d2 * d11 * motionFactor;
					if (entity instanceof EntityPlayer) {
						playerKnockbackMap.put((EntityPlayer) entity, new Vec3(d0 * d10, d1 * d10, d2 * d10));
					}
				}
			}
		}
	}

	/** Returns map of affected players */
	@Override
	public Map<EntityPlayer, Vec3> getPlayerKnockbackMap() { return playerKnockbackMap; }

	protected void notifyClients() {
		if (!worldObj.isRemote) {
			Iterator<EntityPlayer> iterator = worldObj.playerEntities.iterator();
			while (iterator.hasNext()) {
				EntityPlayer player = iterator.next();
				if (player.getDistanceSq(explosionX, explosionY, explosionZ) < 4096.0D) {
					((EntityPlayerMP) player).playerNetServerHandler.sendPacket(new S27PacketExplosion(explosionX, explosionY, explosionZ, explosionSize, affectedBlockPositions, (Vec3) this.getPlayerKnockbackMap().get(player)));
				}
			}
		}
	}
}
