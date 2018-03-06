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

package net.minecraft.entity;

import java.lang.reflect.Field;

import cpw.mods.fml.relauncher.ReflectionHelper;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.village.Village;
import zeldaswordskills.ZSSMain;

public class DirtyEntityAccessor
{
	/** Accessible reference to {@code EntityThrowable#thrower */
	private static Field throwableThrower;

	/** Accessible reference to {@code EntityVillager#villageObj */
	private static Field villageObject;

	/** Damages the target for the amount of damage using the vanilla method; posts LivingHurtEvent */
	public static void damageEntity(EntityLivingBase target, DamageSource source, float amount) {
		target.damageEntity(source, amount);
	}

	/**
	 * Returns the amount of damage the entity will receive after armor and potions are taken into account
	 */
	public static float getModifiedDamage(EntityLivingBase entity, DamageSource source, float amount) {
		// Don't want to actually damage the entity's armor at this point, so
		// reproduce parts of EntityLivingBase#applyArmorCalculations here:
		if (!source.isUnblockable()) {
			int armor = 25 - entity.getTotalArmorValue();
			amount = (amount * (float) armor) / 25.0F;
		}
		amount = entity.applyPotionDamageCalculations(source, amount);
		return Math.max(amount - entity.getAbsorptionAmount(), 0.0F);
	}

	/** Sets or adds to the amount of xp the entity will drop when killed */
	public static void setLivingXp(EntityLiving entity, int xp, boolean add) {
		entity.experienceValue = (add ? entity.experienceValue + xp : xp);
	}

	/**
	 * Sets an entity's size; stores original size in entity's extended data
	 * @param width stored as "origWidth"
	 * @param height stored as "origHeight"
	 */
	public static void setSize(Entity entity, float width, float height) {
		NBTTagCompound compound = entity.getEntityData();
		compound.setFloat("origWidth", entity.width);
		compound.setFloat("origHeight", entity.height);
		entity.setSize(width, height);
		if (entity instanceof EntityPlayer) {
			((EntityPlayer) entity).eyeHeight = 0.85F * height;
		}
	}

	/**
	 * Restores entity to original size; must have first called {@link #setSize(Entity, float, float) setSize}
	 */
	public static void restoreOriginalSize(Entity entity) {
		NBTTagCompound compound = entity.getEntityData();
		if (compound.hasKey("origWidth") && compound.hasKey("origHeight")) {
			entity.setSize(compound.getFloat("origWidth"), compound.getFloat("origHeight"));
			if (entity instanceof EntityPlayer) {
				((EntityPlayer) entity).eyeHeight = ((EntityPlayer) entity).getDefaultEyeHeight();
			}
		} else {
			ZSSMain.logger.warn("Attempted to restore original size without any available data");
		}
	}

	/**
	 * Sets the value of the entity returned by {@link EntityThrowable#getThrower()}
	 */
	public static void setThrowableThrower(EntityThrowable throwable, EntityLivingBase thrower) {
		if (throwableThrower == null) {
			throwableThrower = ReflectionHelper.findField(EntityThrowable.class, "field_70192_c", "thrower");
		}
		try {
			throwableThrower.set(throwable, thrower);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Returns the villager's village object if possible
	 */
	public static Village getVillageObject(EntityVillager villager) {
		if (villageObject == null) {
			villageObject = ReflectionHelper.findField(EntityVillager.class, "field_70954_d", "villageObj");
		}
		try {
			return (Village) villageObject.get(villager);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
