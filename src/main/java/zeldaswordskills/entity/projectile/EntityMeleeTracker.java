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

package zeldaswordskills.entity.projectile;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import zeldaswordskills.api.entity.IReflectable;
import zeldaswordskills.entity.ai.IMeleeAttacker;
import cpw.mods.fml.common.registry.IEntityAdditionalSpawnData;

/**
 * 
 * Short-lived projectile whose sole purpose is to determine if an attack hit a target,
 * but only if the thrower is an IMeleeAttacker.
 * 
 * Upon impact, the IMeleeAttacker will be notified with the MovingObjectPosition, and
 * should determine any outcomes of said impact itself.
 * 
 * If the EntityMeleeTracker expires before striking a target, IMeleeAttacker will be
 * notified of a 'miss'.
 *
 */
public class EntityMeleeTracker extends EntityMobThrowable implements IEntityAdditionalSpawnData, IReflectable
{
	/** The amount by which gravity will affect this entity */
	private float gVel = 0.0F;

	/** Number of ticks this entity is allowed to 'live', effectively limiting the range */
	private int lifespan = 3;

	/** A flag passed to IMeleeAttacker's methods, use depends on the IMeleeAttacker implementation */
	private int flag = 0;

	public EntityMeleeTracker(World world) {
		super(world);
	}

	public EntityMeleeTracker(World world, EntityLivingBase entity) {
		super(world, entity);
	}

	public EntityMeleeTracker(World world, double x, double y, double z) {
		super(world, x, y, z);
	}

	public EntityMeleeTracker(World world, EntityLivingBase shooter, EntityLivingBase target, float velocity, float wobble) {
		super(world, shooter, target, velocity, wobble);
	}

	/**
	 * Sets the amount by which this entity is affected by gravity
	 */
	public EntityMeleeTracker setGravityVelocity(float value) {
		gVel = value;
		return this;
	}

	/**
	 * Sets the lifespan of this entity, in ticks, effectively limiting its range
	 */
	public EntityMeleeTracker setLifespan(int ticks) {
		lifespan = ticks;
		return this;
	}

	/**
	 * Set the flag passed to IMeleeAttacker's methods, use depends on the IMeleeAttacker implementation
	 */
	public EntityMeleeTracker setFlag(int value) {
		flag = value;
		return this;
	}

	@Override
	public float getReflectChance(ItemStack mirrorShield, EntityPlayer player, Entity shooter) {
		return 0.0F; // cannot be reflected
	}

	@Override
	public void onReflected(ItemStack mirrorShield, EntityPlayer player, Entity shooter, Entity oldEntity) {}

	@Override
	protected float getGravityVelocity() {
		return gVel;
	}

	@Override
	protected void onImpact(MovingObjectPosition mop) {
		if (getThrower() instanceof IMeleeAttacker) {
			((IMeleeAttacker) getThrower()).onMeleeImpact(mop, flag);
		}
		if (!worldObj.isRemote) {
			setDead();
		}
	}

	@Override
	public void onUpdate() {
		if (ticksExisted > lifespan) {
			if (getThrower() instanceof IMeleeAttacker) {
				((IMeleeAttacker) getThrower()).onMeleeMiss(flag);
			}
			if (!worldObj.isRemote) {
				setDead();
			}
		} else {
			super.onUpdate();
		}
	}

	@Override
	public void writeSpawnData(ByteBuf buffer) {
		buffer.writeFloat(gVel);
		buffer.writeInt(lifespan);
		buffer.writeInt(flag);
	}

	@Override
	public void readSpawnData(ByteBuf buffer) {
		gVel = buffer.readFloat();
		lifespan = buffer.readInt();
		flag = buffer.readInt();
	}
}
