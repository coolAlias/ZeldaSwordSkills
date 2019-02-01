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

package zeldaswordskills.api.entity;

import java.util.Random;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import zeldaswordskills.api.damage.EnumDamageType;
import zeldaswordskills.ref.Sounds;

public enum MagicType {
	/** Causes fire damage, melts ice, ignites blocks */
	FIRE("fire", EnumDamageType.FIRE, true, "textures/blocks/lava_still.png", Sounds.MAGIC_FIRE, EnumParticleTypes.FLAME),
	/** Causes cold damage, freezes targets, extinguishes flames and lava */
	ICE("ice", EnumDamageType.COLD, true, "textures/blocks/ice.png", Sounds.MAGIC_ICE, EnumParticleTypes.SNOW_SHOVEL),
	/** Inflicts shock damage */
	LIGHTNING("lightning", EnumDamageType.SHOCK, false, "textures/blocks/gold_block.png", Sounds.SHOCK, EnumParticleTypes.CLOUD),
	/** Not currently used */
	WATER("water", EnumDamageType.WATER, false, "textures/blocks/water_still.png", Sounds.SPLASH, EnumParticleTypes.WATER_SPLASH),
	/** Currently no special effects; used only to give Tornado Rod a dummy magic type */
	WIND("wind", EnumDamageType.WIND, false, "textures/blocks/emerald_block.png", Sounds.WHIRLWIND, EnumParticleTypes.CLOUD);

	private final String unlocalizedName;

	private final EnumDamageType damageType;

	private final boolean affectsBlocks;

	private final ResourceLocation texture;

	private final String moveSound;

	// TODO what about non-vanilla particle strings?
	private final EnumParticleTypes trailingParticle;

	private MagicType(String name, EnumDamageType damageType, boolean affectsBlocks, String texture, String moveSound, EnumParticleTypes trailingParticle) {
		this.unlocalizedName = name;
		this.damageType = damageType;
		this.affectsBlocks = affectsBlocks;
		this.texture = new ResourceLocation(texture);
		this.moveSound = moveSound;
		this.trailingParticle = trailingParticle;
	}

	@Override
	public String toString() {
		return StatCollector.translateToLocal("magic." + unlocalizedName + ".name");
	}

	/**
	 * Returns if the magic type and caster combination can affect blocks,
	 * depending on the current game rule settings for mob griefing
	 */
	public boolean affectsBlocks(World world, EntityLivingBase caster) {
		return affectsBlocks && (caster instanceof EntityPlayer || world.getGameRules().getBoolean("mobGriefing"));
	}

	public EnumDamageType getDamageType() {
		return damageType;
	}

	/**
	 * Returns the texture to render on the model cubes of the spell entity
	 */
	public ResourceLocation getEntityTexture() {
		return texture;
	}

	/** Returns sound to play every few ticks while spell travels */
	public String getMovingSound() {
		return moveSound;
	}

	/** Returns how frequently the moving sound should play */
	public int getSoundFrequency() {
		switch(this) {
		case FIRE: return 12;
		case ICE: return 4;
		case WIND: return 5;
		default: return 6;
		}
	}

	/** Returns sound volume for moving sound */
	public float getSoundVolume(Random rand) {
		switch(this) {
		case ICE: return 0.5F + rand.nextFloat();
		case WIND: return 0.6F;
		default: return 1.0F + rand.nextFloat();
		}
	}

	/** Returns the pitch for the moving sound */
	public float getSoundPitch(Random rand) {
		switch(this) {
		case ICE: return rand.nextFloat() * 0.4F + 0.8F;
		case WIND: return 1.0F;
		default: return rand.nextFloat() * 0.7F + 0.3F;
		}
	}

	/** Returns particle to spawn behind spell as it travels */
	public EnumParticleTypes getTrailingParticle() {
		return trailingParticle;
	}
}
