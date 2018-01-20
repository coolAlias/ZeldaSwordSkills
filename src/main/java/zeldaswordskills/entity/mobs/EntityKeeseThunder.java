/**
    Copyright (C) <2018> <coolAlias>

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

package zeldaswordskills.entity.mobs;

import net.minecraft.entity.SharedMonsterAttributes;
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

public class EntityKeeseThunder extends EntityKeese
{
	/** Data watcher index for shock time so entity can render appropriately */
	protected static final int SHOCK_INDEX = 18;

	public EntityKeeseThunder(World world) {
		super(world);
		this.experienceValue = 5;
	}

	@Override
	protected EntityKeeseThunder createInstance() {
		return new EntityKeeseThunder(this.worldObj);
	}

	@Override
	protected void entityInit() {
		super.entityInit();
		dataWatcher.addObject(EntityKeeseThunder.SHOCK_INDEX, 0);
	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(12.0F);
		ZSSEntityInfo.get(this).applyBuff(Buff.RESIST_SHOCK, Integer.MAX_VALUE, 100);
	}

	/** Returns the amount of time remaining for which this Keese is electrified */
	public int getShockTime() {
		return this.dataWatcher.getWatchableObjectInt(EntityKeeseThunder.SHOCK_INDEX);
	}

	/** Sets the amount of time this Keese will remain electrified */
	public void setShockTime(int time) {
		this.dataWatcher.updateObject(EntityKeeseThunder.SHOCK_INDEX, time);
	}

	@Override
	protected DamageSource getDamageSource() {
		if (this.getShockTime() > 0) {
			return new DamageSourceShock("shock", this, this.worldObj.getDifficulty().getDifficultyId() * 50, 1.0F);
		}
		return super.getDamageSource();
	}

	@Override
	protected void applySecondaryEffects(EntityPlayer player) {
		// Not a secondary effect, per se, but shock time is reduced each time Thunder Keese attacks
		int time = this.getShockTime();
		if (time > 0) {
			this.setShockTime(Math.max(0, time - this.rand.nextInt(50) - 25));
		}
	}

	@Override
	public boolean attackEntityFrom(DamageSource source, float amount) {
		if (this.isEntityInvulnerable(source)) {
			return false;
		}
		if (!this.worldObj.isRemote && this.getIsBatHanging()) {
			this.setIsBatHanging(false);
		}
		if (this.getShockTime() > 0) {
			if (source instanceof EntityDamageSourceIndirect) {
				if (source.isMagicDamage()) {
					return super.attackEntityFrom(source, amount);
				} else if (source.isExplosion()) {
					ZSSEntityInfo.get(this).stun(20 + this.rand.nextInt((int)(amount * 5) + 1));
					this.setShockTime(0);
				} else if (source instanceof IDamageSourceStun) {
					this.setShockTime(0);
				}
				// Hack to prevent infinite loop when attacked by other electrified mobs (other keese, chus, etc)
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
	public void onUpdate() {
		super.onUpdate();
		int time = this.getShockTime();
		if (time > 0) {
			this.setShockTime(time - 1);
			if (time % 8 > 6 && this.rand.nextInt(4) == 0) {
				this.worldObj.playSoundAtEntity(this, Sounds.SHOCK, this.getSoundVolume(), 1.0F / (this.rand.nextFloat() * 0.4F + 1.0F));
			}
		}
	}

	@Override
	protected void updateAITasks() {
		super.updateAITasks();
		if (ZSSEntityInfo.get(this).isBuffActive(Buff.STUN) || this.getIsBatHanging()) {
			// because Keese get moved twice per tick due to inherited EntityBat methods
			return;
		}
		if (this.getShockTime() == 0 && !ZSSEntityInfo.get(this).isBuffActive(Buff.STUN)) {
			if (this.attackingPlayer != null && ((this.recentlyHit > 0 && this.rand.nextInt(20) == 0) || this.rand.nextInt(300) == 0)) {
				this.setShockTime(this.rand.nextInt(50) + (this.worldObj.getDifficulty().getDifficultyId() * (this.rand.nextInt(20) + 10)));
			}
		}
	}
}
