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

package zeldaswordskills.entity.ai;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;

public class EntityAIPowerAttack extends EntityAIBase
{
	/** Owner of the AI */
	private final EntityLiving attacker;

	/** IPowerAttacker instance of attacker */
	private final IPowerAttacker powerAttacker;

	/** Current AI target */
	private EntityLivingBase target;

	/** Base reach distance */
	private double range;

	/** Maximum distance squared from target at which attacker is able to attack */
	private double rangeSq;

	/** Number of ticks until next attack is allowed */
	private int attackTimer;

	/** Number of ticks remaining until power attack triggers */
	private int chargeTimer;

	/**
	 * @param range		Attacker's reach with this attack
	 */
	public <T extends EntityLiving & IPowerAttacker> EntityAIPowerAttack(T attacker, double range) {
		this.attacker = attacker;
		this.powerAttacker = attacker;
		this.range = range;
		this.rangeSq = (range * range);
		// since attackOnCollide is always executing, must use mutex 0 for now
		setMutexBits(0);
	}

	@Override
	public boolean shouldExecute() {
		target = attacker.getAttackTarget();
		if (attacker.attackTime > 0) {
			return false;
		} else if (attackTimer > 0) {
			--attackTimer;
			return false;
		} else if (target == null || !target.isEntityAlive()) {
			return false;
		} else if (attacker.getDistanceSqToEntity(target) > (rangeSq * 2.25F)) {
			return false;
		}
		return attacker.onGround;
	}

	@Override
	public void startExecuting() {
		setMutexBits(3); // prevent EntityAIAttackOnCollide from executing
		chargeTimer = powerAttacker.getChargeTime();
		powerAttacker.beginPowerAttack();
	}

	@Override
	public boolean continueExecuting() {
		return (target != null && target.isEntityAlive() && chargeTimer > 0 && attacker.onGround && attacker.getDistanceSqToEntity(target) < (rangeSq * 2.25F));
	}

	@Override
	public void updateTask() {
		attacker.getLookHelper().setLookPositionWithEntity(target, 30.0F, 30.0F);
		// helps attacker actually rotate towards target, as well as continue to slowly close distance
		attacker.getMoveHelper().setMoveTo(target.posX, target.posY, target.posZ, 1.0D / 3.0D);
		if (chargeTimer > 0 && --chargeTimer == 0) {
			attackTimer = 20 + attacker.worldObj.rand.nextInt(20) + attacker.worldObj.rand.nextInt(20);
			double d = attacker.getDistanceSq(target.posX, target.boundingBox.minY, target.posZ);
			double width = (attacker.width + target.width) / 2.0F;
			double reach = (range + width) * (range + width);
			if (d <= reach && attacker.getEntitySenses().canSee(target)) {
				powerAttacker.performPowerAttack(target);
			} else {
				powerAttacker.onAttackMissed();
			}
		}
	}

	@Override
	public void resetTask() {
		setMutexBits(0); // re-allow other AIs to execute
		chargeTimer = 0;
		target = null;
	}
}
