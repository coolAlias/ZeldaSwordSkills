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

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item.ToolMaterial;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.EntityDamageSourceIndirect;
import net.minecraft.world.World;
import zeldaswordskills.api.damage.DamageUtils.DamageSourceShock;
import zeldaswordskills.api.damage.IDamageSourceStun;
import zeldaswordskills.entity.ZSSEntityInfo;
import zeldaswordskills.entity.buff.Buff;
import zeldaswordskills.ref.Sounds;

/**
 * 
 * Some Chus produce a protective electrical field when they sense danger or take damage.
 * While the field is active, the Chu is immune to most damage and attackers may be shocked.
 * Explosions and stuns can force the Chu to drop its electric field, or one can simply wait.
 * Magic and wooden weapons are both able to penetrate the electric field.
 *
 */
public abstract class EntityChuElectric extends EntityChu
{
	/** Data watcher index for shock time so entity can render appropriately */
	protected static final int SHOCK_INDEX = 18;

	public EntityChuElectric(World world) {
		super(world);
	}

	@Override
	protected void entityInit() {
		super.entityInit();
		this.dataWatcher.addObject(EntityChuElectric.SHOCK_INDEX, 0);
	}

	/** Returns the amount of time remaining for which this Chu is electrified */
	public int getShockTime() {
		return this.dataWatcher.getWatchableObjectInt(EntityChuElectric.SHOCK_INDEX);
	}

	/** Sets the amount of time this Chu will remain electrified */
	public void setShockTime(int time) {
		this.dataWatcher.updateObject(EntityChuElectric.SHOCK_INDEX, time);
	}

	/** Returns max time affected entities will be stunned when shocked */
	protected int getMaxStunTime() {
		return (this.getSlimeSize() * this.worldObj.getDifficulty().getDifficultyId() * 10);
	}

	/** Random interval between shocks */
	protected int getShockInterval() {
		return 160;
	}

	@Override
	protected DamageSource getDamageSource() {
		if (this.getShockTime() > 0) {
			return new DamageSourceShock("shock", this, this.getMaxStunTime(), this.getDamage());
		}
		return super.getDamageSource();
	}

	@Override
	public boolean attackEntityFrom(DamageSource source, float amount) {
		if (source == DamageSource.inWall) {
			return false;
		} else if (this.getShockTime() > 0) {
			if (source instanceof EntityDamageSourceIndirect) {
				if (source.isMagicDamage()) {
					return super.attackEntityFrom(source, amount);
				} else if (source.isExplosion()) {
					ZSSEntityInfo.get(this).stun(20 + this.rand.nextInt((int)(amount * 5) + 1));
					this.setShockTime(0);
				} else if (source instanceof IDamageSourceStun) {
					this.setShockTime(0);
				}
				// Hack to prevent infinite loop when attacked by other electrified mobs (other chus, keese, etc)
			} else if (source instanceof EntityDamageSource && source.getEntity() instanceof EntityPlayer && !source.damageType.equals("thorns")) {
				boolean isWood = false;
				ItemStack stack = ((EntityPlayer) source.getEntity()).getHeldItem();
				if (stack != null && stack.getItem() instanceof ItemTool) {
					isWood = ((ItemTool) stack.getItem()).getToolMaterial() == ToolMaterial.WOOD;
				} else if (stack != null && stack.getItem() instanceof ItemSword) {
					isWood = ((ItemSword) stack.getItem()).getToolMaterialName().equals(ToolMaterial.WOOD.toString());
				}
				if (!isWood) {
					source.getEntity().attackEntityFrom(this.getDamageSource(), this.getDamage());
					this.worldObj.playSoundAtEntity(this, Sounds.SHOCK, 1.0F, 1.0F / (this.rand.nextFloat() * 0.4F + 1.0F));
				}
			}
			return false;
		}
		return super.attackEntityFrom(source, amount);
	}

	@Override
	protected void applySecondaryEffects(EntityLivingBase target) {
		int time = this.getShockTime();
		if (time > 0) {
			this.setShockTime(Math.max(0, time - this.rand.nextInt(100) - 50));
		}
	}

	@Override
	public void onUpdate() {
		super.onUpdate();
		this.updateShockState();
	}

	/**
	 * Updates the Electric Chu's shock status
	 */
	protected void updateShockState() {
		if (this.getShockTime() == 0 && !ZSSEntityInfo.get(this).isBuffActive(Buff.STUN)) {
			EntityPlayer player = this.worldObj.getClosestPlayerToEntity(this, 16.0D);
			if (player != null && (this.recentlyHit > 0 || this.rand.nextInt(this.getShockInterval()) == 0)) {
				this.setShockTime(this.rand.nextInt(this.getSlimeSize() * 50) + (this.worldObj.getDifficulty().getDifficultyId() * (this.rand.nextInt(20) + 10)));
			}
		}
		if (this.getShockTime() % 8 > 5 && this.rand.nextInt(4) == 0) {
			this.worldObj.playSoundAtEntity(this, Sounds.SHOCK, this.getSoundVolume(), 1.0F / (this.rand.nextFloat() * 0.4F + 1.0F));
		}
		int time = this.getShockTime();
		if (time > 0) {
			this.setShockTime(time - 1);
		}
	}
}
