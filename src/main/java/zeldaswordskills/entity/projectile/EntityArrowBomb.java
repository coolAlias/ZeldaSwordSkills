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

package zeldaswordskills.entity.projectile;

import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSourceIndirect;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import zeldaswordskills.api.entity.BombType;
import zeldaswordskills.api.entity.CustomExplosion;
import zeldaswordskills.api.entity.IEntityBomb;
import zeldaswordskills.item.ItemBomb;
import zeldaswordskills.lib.Config;

public class EntityArrowBomb extends EntityArrowCustom implements IEntityBomb
{
	/** Watchable object index for bomb's type */
	private static final int BOMBTYPE_DATAWATCHER_INDEX = 25;

	/** Uses ItemBomb's radius if this value is zero */
	private float radius = 0.0F;

	/** Whether this bomb is capable of destroying blocks */
	private boolean canGrief = true;

	public EntityArrowBomb(World world) {
		super(world);
	}

	public EntityArrowBomb(World world, double x, double y, double z) {
		super(world, x, y, z);
	}

	public EntityArrowBomb(World world, EntityLivingBase shooter, float velocity) {
		super(world, shooter, velocity);
	}

	public EntityArrowBomb(World world, EntityLivingBase shooter, EntityLivingBase target, float velocity, float wobble) {
		super(world, shooter, target, velocity, wobble);
	}

	@Override
	public void entityInit() {
		super.entityInit();
		dataWatcher.addObject(BOMBTYPE_DATAWATCHER_INDEX, BombType.BOMB_STANDARD.ordinal());
	}

	@Override
	protected DamageSource getDamageSource(Entity entity) {
		return new EntityDamageSourceIndirect("bomb arrow", this, shootingEntity).setProjectile().setExplosion();
	}

	/**
	 * Sets this bomb's radius
	 */
	public EntityArrowBomb setRadius(float radius) {
		this.radius = radius;
		return this;
	}

	// TODO @Override
	public boolean canGriefAdventureMode() {
		return Config.canGriefAdventure();
	}

	/**
	 * Sets this bomb to not destroy blocks, but still cause damage 
	 */
	public EntityArrowBomb setNoGrief() {
		canGrief = false;
		return this;
	}

	@Override
	public BombType getType() {
		return BombType.values()[dataWatcher.getWatchableObjectInt(BOMBTYPE_DATAWATCHER_INDEX)];
	}

	/**
	 * Set this bomb's {@link BombType}
	 */
	public EntityArrowBomb setType(BombType type) {
		dataWatcher.updateObject(BOMBTYPE_DATAWATCHER_INDEX, type.ordinal());
		return this;
	}

	@Override
	public float getMotionFactor() {
		return 1.0F;
	}

	@Override
	public float getDestructionFactor() {
		return 1.0F;
	}

	@Override
	public boolean hasPostExplosionEffect() {
		return false;
	}

	@Override
	protected float getVelocityFactor() {
		return 1.0F;
	}

	@Override
	protected float getGravityVelocity() {
		return 0.065F;
	}

	@Override
	protected String getParticleName() {
		return "smoke";
	}

	@Override
	protected boolean shouldSpawnParticles() {
		return true;
	}

	@Override
	public void onUpdate() {
		super.onUpdate();
		if (worldObj.provider.isHellWorld && ticksInAir > 0 && !worldObj.isRemote && (getType() == BombType.BOMB_STANDARD || getType() == BombType.BOMB_FLOWER)) {
			CustomExplosion.createExplosion(this, worldObj, posX, posY, posZ, (radius == 0.0F ? ItemBomb.getRadius(getType()) : radius), (float) getDamage(), canGrief);
			setDead();
		}
	}

	@Override
	protected void onImpact(MovingObjectPosition mop) {
		Material material = worldObj.getBlockMaterial(MathHelper.floor_double(posX), MathHelper.floor_double(posY), MathHelper.floor_double(posZ));
		boolean inFire = isBurning() || (material == Material.lava || material == Material.fire) || worldObj.isBoundingBoxBurning(boundingBox);
		if (!isDud(inFire)) {
			if (!worldObj.isRemote) {
				CustomExplosion.createExplosion(this, worldObj, posX, posY, posZ, (radius == 0.0F ? ItemBomb.getRadius(getType()) : radius), (float) getDamage(), canGrief);
				setDead();
			}
		} else {
			super.onImpact(mop);
		}
	}

	/**
	 * Returns true if the bomb is a dud: in the water or a water bomb in the nether
	 * @param inFire whether this bomb is in fire, lava, or currently burning
	 */
	private boolean isDud(boolean inFire) {
		switch(getType()) {
		case BOMB_WATER:  return inFire || worldObj.provider.isHellWorld;
		default: return (worldObj.getBlockMaterial((int) posX, (int) posY, (int) posZ) == Material.water);
		}
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound compound) {
		super.writeEntityToNBT(compound);
		compound.setInteger("arrowType", getType().ordinal());
		compound.setFloat("bombRadius", radius);
		compound.setBoolean("canGrief", canGrief);
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound compound) {
		super.readEntityFromNBT(compound);
		setType(BombType.values()[compound.getInteger("arrowType") % BombType.values().length]);
		radius = compound.getFloat("bombRadius");
		canGrief = compound.getBoolean("canGrief");
	}
}
