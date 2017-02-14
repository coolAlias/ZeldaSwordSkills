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

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import zeldaswordskills.ZSSMain;

public class DirtyEntityAccessor {

	/** Accessible reference to {@code EntityLivingBase#damageEntity} */
	private static Method damageEntity;
	/** Accessible reference to {@code EntityLivingBase#applyPotionDamageCalculations */
	private static Method applyPotionDamageCalculations;
	/** Accessible reference to {@code Entity#setSize */
	private static Method setSize;
	/** Accessible reference to {@code EntityLiving#experienceValue */
	private static Field experienceValue;

	/** Damages the target for the amount of damage using the vanilla method; posts LivingHurtEvent */
	public static void damageEntity(EntityLivingBase target, DamageSource source, float amount) {
		if (damageEntity == null) {
			damageEntity = ReflectionHelper.findMethod(EntityLivingBase.class, target, new String[]{"func_70665_d","damageEntity"}, DamageSource.class, float.class);
		}
		try {
			damageEntity.invoke(target, source, amount);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Returns the amount of damage the entity will receive after armor and potions are taken into account
	 */
	public static float getModifiedDamage(EntityLivingBase entity, DamageSource source, float amount) {
		if (applyPotionDamageCalculations == null) {
			applyPotionDamageCalculations = ReflectionHelper.findMethod(EntityLivingBase.class, entity, new String[]{"func_70672_c","applyPotionDamageCalculations"}, DamageSource.class, float.class);
		}
		// Don't want to actually damage the entity's armor at this point, so
		// reproduce parts of EntityLivingBase#applyArmorCalculations here:
		if (!source.isUnblockable()) {
			int armor = 25 - entity.getTotalArmorValue();
			amount = (amount * (float) armor) / 25.0F;
		}
		try {
			applyPotionDamageCalculations.invoke(entity, source, amount);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return Math.max(amount - entity.getAbsorptionAmount(), 0.0F);
	}

	/** Sets or adds to the amount of xp the entity will drop when killed */
	public static void setLivingXp(EntityLiving entity, int xp, boolean add) {
		if (experienceValue == null) {
			experienceValue = ReflectionHelper.findField(EntityLiving.class, "field_70728_aV", "experienceValue");
		}
		try {
			int value = (int) experienceValue.get(entity);
			experienceValue.set(entity, (add ? value + xp : xp));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Sets an entity's size; stores original size in entity's extended data
	 * @param width stored as "origWidth"
	 * @param height stored as "origHeight"
	 */
	public static void setSize(Entity entity, float width, float height) {
		if (setSize == null) {
			setSize = ReflectionHelper.findMethod(Entity.class, entity, new String[]{"func_70105_a","setSize"}, float.class, float.class);
		}
		NBTTagCompound compound = entity.getEntityData();
		compound.setFloat("origWidth", entity.width);
		compound.setFloat("origHeight", entity.height);
		try {
			setSize.invoke(entity, width, height);
		} catch (Exception e) {
			e.printStackTrace();
		}
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
			if (setSize == null) {
				setSize = ReflectionHelper.findMethod(Entity.class, entity, new String[]{"func_70105_a","setSize"}, float.class, float.class);
			}
			try {
				setSize.invoke(entity, compound.getFloat("origWidth"), compound.getFloat("origHeight"));
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (entity instanceof EntityPlayer) {
				((EntityPlayer) entity).eyeHeight = ((EntityPlayer) entity).getDefaultEyeHeight();
			}
		} else {
			ZSSMain.logger.warn("Attempted to restore original size without any available data");
		}
	}
}
