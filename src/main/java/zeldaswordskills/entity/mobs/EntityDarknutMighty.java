/**
 * Copyright (C) <2019> <coolAlias>
 *
 * This file is part of coolAlias' Zelda Sword Skills Minecraft Mod; as such,
 * you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package zeldaswordskills.entity.mobs;

import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import zeldaswordskills.api.item.ArmorIndex;
import zeldaswordskills.entity.ZSSEntityInfo;
import zeldaswordskills.entity.buff.Buff;

public class EntityDarknutMighty extends EntityDarknut
{
	/** DataWatcher for cape */
	private final static int CAPE_INDEX = 18;

	public EntityDarknutMighty(World world) {
		super(world);
		this.experienceValue = 20;
	}

	@Override
	public void entityInit() {
		super.entityInit();
		this.dataWatcher.addObject(EntityDarknutMighty.CAPE_INDEX, (byte) 60);
	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		ZSSEntityInfo.get(this).applyBuff(Buff.RESIST_STUN, Integer.MAX_VALUE, 50);
		this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(100.0D);
		this.getEntityAttribute(SharedMonsterAttributes.attackDamage).setBaseValue(7.0D);
	}

	public boolean isWearingCape() {
		return (this.dataWatcher.getWatchableObjectByte(EntityDarknutMighty.CAPE_INDEX) > (byte) 0);
	}

	/**
	 * Grants the Darknut a cape with the given amount for health (i.e. ticks of fire damage required to burn through it)
	 */
	protected void setWearingCape(byte ticksRequired) {
		this.dataWatcher.updateObject(EntityDarknutMighty.CAPE_INDEX, ticksRequired);
	}

	/**
	 * Damages the cape by 1 point
	 */
	private void damageCape() {
		byte b = this.dataWatcher.getWatchableObjectByte(EntityDarknutMighty.CAPE_INDEX);
		if (b > 0) {
			this.setWearingCape((byte)(b - 1));
			if (b == (byte) 1) {
				this.extinguish();
			}
		}
	}

	@Override
	protected boolean canAttackArmor(DamageSource source, float amount) {
		if (this.isWearingCape()) {
			if (source.isFireDamage()) {
				this.setFire(3);
			}
			return false;
		}
		return true;
	}

	@Override
	protected void onArmorDestroyed() {
		this.setCurrentItemOrArmor(ArmorIndex.EQUIPPED_CHEST, new ItemStack(Items.chainmail_chestplate));
		this.applyArmorAttributeModifiers(false);
	}

	@Override
	public void onLivingUpdate() {
		if (this.isWearingCape() && this.isBurning()) {
			this.damageCape();
		}
		super.onLivingUpdate();
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound compound) {
		super.writeEntityToNBT(compound);
		compound.setByte("CapeHealth", this.dataWatcher.getWatchableObjectByte(EntityDarknutMighty.CAPE_INDEX));
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound compound) {
		super.readEntityFromNBT(compound);
		this.dataWatcher.updateObject(EntityDarknutMighty.CAPE_INDEX, compound.getByte("CapeHealth"));
	}
}
