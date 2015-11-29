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

package zeldaswordskills.entity;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.DamageSource;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import zeldaswordskills.api.damage.DamageUtils.DamageSourceBaseIndirect;
import zeldaswordskills.api.damage.EnumDamageType;

/**
 * 
 * Custom lightning bolt spawned by Ether Medallion targets a single entity only
 *
 */
public class EntityEtherLightning extends EntityLightningBolt
{
	/** Copies of EntityLightningBolt's private fields */
	private int lightningState, boltLivingTime;

	/** Entity responsible for spawning the lightning bolt */
	protected final EntityLivingBase thrower;

	/** Entity targeted by the lightning bolt; set to null after first strike */
	protected EntityLivingBase target;

	public EntityEtherLightning(World world, EntityLivingBase thrower, EntityLivingBase target, double posX, double posY, double posZ) {
		super(world, posX, posY, posZ);
		this.thrower = thrower;
		this.target = target;
		this.setLocationAndAngles(posX, posY, posZ, 0.0F, 0.0F);
		this.lightningState = 2;
		this.boltVertex = this.rand.nextLong();
		this.boltLivingTime = this.rand.nextInt(3) + 1;
		if (!world.isRemote && world.getGameRules().getGameRuleBooleanValue("doFireTick") && (world.getDifficulty() == EnumDifficulty.NORMAL || world.getDifficulty() == EnumDifficulty.HARD) && world.isAreaLoaded(new BlockPos(this), 10)) {
			extinguishFire(world);
		}
	}

	@Override
	public void onUpdate() {
		super.onEntityUpdate(); // bypass super.onUpdate
		if (lightningState == 2) {
			worldObj.playSoundEffect(posX, posY, posZ, "ambient.weather.thunder", 10000.0F, 0.8F + rand.nextFloat() * 0.2F);
			worldObj.playSoundEffect(posX, posY, posZ, "random.explode", 2.0F, 0.5F + rand.nextFloat() * 0.2F);
		}
		--lightningState;
		if (lightningState < 0) {
			if (boltLivingTime == 0) {
				setDead();
			} else if (lightningState < -rand.nextInt(10)) {
				--boltLivingTime;
				lightningState = 1;
				boltVertex = rand.nextLong();
			}
		}
		if (lightningState >= 0) {
			if (worldObj.isRemote) {
				worldObj.setLastLightningBolt(2);
			} else if (target != null) {
				int strength = (worldObj.isRaining() ? 20 : 0) + (worldObj.isThundering() ? 30 : 0);
				// 15 to 35 damage, or 1/3 that for entities on the ground
				float damage = 15.0F + rand.nextInt(11) + (strength * 0.2F);
				// TODO many ground-based targets are not on the ground... should
				// max damage be restricted to actual registered flying entities only?
				if (target.onGround) {
					damage /= 3.0F;
				}
				int time = 600 + (strength > 0 || !target.onGround ? rand.nextInt(10) + rand.nextInt(Math.max(1, strength)) + 10 : 0);
				DamageSource source = new DamageSourceBaseIndirect(DamageSource.lightningBolt.damageType, this, this.thrower, true, EnumDamageType.SHOCK).setMagicDamage().setDamageBypassesArmor();
				if (time > 0) {
					((DamageSourceBaseIndirect) source).setStunDamage(time, 1, true);
				}
				target.attackEntityFrom(source, damage);
				if (!net.minecraftforge.event.ForgeEventFactory.onEntityStruckByLightning(target, this)) {
					target.onStruckByLightning(this);
				}
				target = null; // prevent processing again, since lightning can apparently strike 2-3 times per entity
			}
		}
	}

	/**
	 * Hack to put out any fires caused by EntityLightningBolt constructor
	 */
	private void extinguishFire(World world) {
		BlockPos centerPos = new BlockPos(this);
		for (int i = -1; i < 2; ++i) {
			for (int j = -1; j < 2; ++j) {
				for (int k = -1; k < 2; ++k) {
					BlockPos pos = centerPos.add(i, j, k);
					if (world.getBlockState(pos).getBlock() == Blocks.fire) {
						world.setBlockToAir(pos);
					}
				}
			}
		}
	}
}
