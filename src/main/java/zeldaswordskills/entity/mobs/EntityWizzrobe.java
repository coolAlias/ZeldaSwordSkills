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

package zeldaswordskills.entity.mobs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import zeldaswordskills.api.block.IWhipBlock.WhipType;
import zeldaswordskills.api.entity.IEntityLootable;
import zeldaswordskills.api.entity.MagicType;
import zeldaswordskills.entity.IEntityVariant;
import zeldaswordskills.entity.ZSSEntityInfo;
import zeldaswordskills.entity.ai.EntityAINearestAttackableTargetNight;
import zeldaswordskills.entity.ai.EntityAIRangedMagic;
import zeldaswordskills.entity.ai.EntityAITeleport;
import zeldaswordskills.entity.ai.IEntityTeleport;
import zeldaswordskills.entity.ai.IMagicUser;
import zeldaswordskills.entity.buff.Buff;
import zeldaswordskills.entity.projectile.EntityMagicSpell;
import zeldaswordskills.item.ItemTreasure.Treasures;
import zeldaswordskills.item.ZSSItems;
import zeldaswordskills.ref.Config;
import zeldaswordskills.ref.Sounds;
import zeldaswordskills.util.BiomeType;
import zeldaswordskills.util.WorldUtils;

public class EntityWizzrobe extends EntityMob implements IEntityLootable, IEntityTeleport, IEntityVariant, IMagicUser
{
	/** Separate Wizzrobe enum to allow more magic types without screwing up Wizzrobe spawn eggs and such */
	public static enum WizzrobeType {
		FIRE_WIZ(MagicType.FIRE, BiomeType.FIERY),
		ICE_WIZ(MagicType.ICE, BiomeType.COLD),
		LIGHTNING_WIZ(MagicType.LIGHTNING, BiomeType.MOUNTAIN),
		WIND_WIZ(MagicType.WIND, BiomeType.FOREST);

		/** The magic type this wizzrobe uses */
		public final MagicType magicType;

		/** Biome in which this type spawns most frequently (or possibly exclusively) */
		public final BiomeType favoredBiome;

		private WizzrobeType(MagicType type, BiomeType favoredBiome) {
			this.magicType = type;
			this.favoredBiome = favoredBiome;
		}
	}

	/**
	 * Returns array of default biomes in which this entity may spawn naturally
	 */
	public static String[] getDefaultBiomes() {
		List<String> biomes = new ArrayList<String>();
		for (WizzrobeType type : WizzrobeType.values()) {
			biomes.addAll(Arrays.asList(type.favoredBiome.defaultBiomes));
		}
		biomes.addAll(Arrays.asList(BiomeType.ARID.defaultBiomes));
		biomes.addAll(Arrays.asList(BiomeType.JUNGLE.defaultBiomes));
		biomes.addAll(Arrays.asList(BiomeType.PLAINS.defaultBiomes));
		biomes.addAll(Arrays.asList(BiomeType.RIVER.defaultBiomes));
		biomes.addAll(Arrays.asList(BiomeType.TAIGA.defaultBiomes));
		return biomes.toArray(new String[biomes.size()]);
	}

	/** Data watcher index for this Wizzrobe's type */
	protected static final int TYPE_INDEX = 16;

	/** Datawatcher index for current casting time, used for rendering arm positions */
	protected static final int CASTING_TIME = 17;

	/** Datawatcher index for current maximum casting time, used for rendering arm positions */
	protected static final int MAX_CAST_TIME = 18;

	/** The magic using AI instance, used to interrupt casting when hurt */
	protected final EntityAIRangedMagic magicAI;

	/** The teleportation AI instance, used to check if can teleport when attacked */
	protected final EntityAITeleport teleportAI;

	/** Target acquisition timer */
	private int noTargetTime;

	public EntityWizzrobe(World world) {
		super(world);
		magicAI = getMagicAI();
		teleportAI = getNewTeleportAI();
		tasks.addTask(1, new EntityAISwimming(this));
		tasks.addTask(2, magicAI);
		tasks.addTask(3, teleportAI);
		tasks.addTask(4, new EntityAIWander(this, 1.0D));
		tasks.addTask(5, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
		tasks.addTask(5, new EntityAILookIdle(this));
		targetTasks.addTask(1, new EntityAIHurtByTarget(this, false));
		targetTasks.addTask(2, new EntityAINearestAttackableTargetNight(this, EntityPlayer.class, 0, true, 0.5F));
		experienceValue = 8; // normal mobs are 5
		setSize(0.6F, 1.8F);
		setType(WizzrobeType.WIND_WIZ);
	}

	@Override
	public EntityAITeleport getTeleportAI() {
		return teleportAI;
	}

	/**
	 * Returns the teleportation AI this Wizzrobe should use when being constructed
	 */
	protected EntityAITeleport getNewTeleportAI() {
		return new EntityAITeleport(this, 16.0D, 60, true, true, true, true, true);
	}

	/**
	 * Returns the magic AI this Wizzrobe should use
	 */
	protected EntityAIRangedMagic getMagicAI() {
		return new EntityAIRangedMagic(this, 20, 60, 16.0D);
	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(30.0D);
		getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(0.25D);
		getEntityAttribute(SharedMonsterAttributes.knockbackResistance).setBaseValue(1.0D);
		getEntityAttribute(SharedMonsterAttributes.followRange).setBaseValue(40.0D);
	}

	@Override
	protected void entityInit() {
		super.entityInit();
		dataWatcher.addObject(TYPE_INDEX, (byte)(WizzrobeType.WIND_WIZ.ordinal()));
		dataWatcher.addObject(CASTING_TIME, 0);
		dataWatcher.addObject(MAX_CAST_TIME, 0);
	}

	/** Returns the Wizzrobe's Magic Type; this determines which spell will be cast */
	public MagicType getMagicType() {
		return getWizzrobeType().magicType;
	}

	/** Returns this Wizzrobe's type */
	public WizzrobeType getWizzrobeType() {
		return WizzrobeType.values()[dataWatcher.getWatchableObjectByte(TYPE_INDEX) % WizzrobeType.values().length];
	}

	/** Sets the Wizzrobe's type; this determines the Wizzrobe's Magic Type */
	public void setType(WizzrobeType type) {
		dataWatcher.updateObject(TYPE_INDEX, (byte)(type.ordinal()));
		applyTypeTraits();
	}

	@Override
	public EntityWizzrobe setType(int type) {
		setType(WizzrobeType.values()[type % WizzrobeType.values().length]);
		return this;
	}

	private void setTypeOnSpawn() {
		if (Config.areMobVariantsAllowed() && rand.nextFloat() < Config.getMobVariantChance()) {
			setType(rand.nextInt(WizzrobeType.values().length));
		} else {
			BiomeGenBase biome = worldObj.getBiomeGenForCoords(MathHelper.floor_double(posX), MathHelper.floor_double(posZ));
			BiomeType biomeType = BiomeType.getBiomeTypeFor(biome);
			for (WizzrobeType t : WizzrobeType.values()) {
				if (t.favoredBiome == biomeType) {
					setType(t);
					return;
				}
			}
			// no favored biome was found, set based on remaining biome types
			if (biomeType != null) {
				switch(biomeType) {
				case ARID:
					setType(rand.nextFloat() < 0.5F ? WizzrobeType.LIGHTNING_WIZ : WizzrobeType.FIRE_WIZ);
					break;
				case RIVER: // swamps and rivers are same as Jungle
				case JUNGLE:
					setType(rand.nextFloat() < 0.5F ? WizzrobeType.WIND_WIZ : WizzrobeType.LIGHTNING_WIZ);
					break;
				case TAIGA:
					setType(rand.nextFloat() < 0.5F ? WizzrobeType.WIND_WIZ : WizzrobeType.ICE_WIZ);
					break;
				default:
				}
			}
		}
	}

	protected void applyTypeTraits() {
		ZSSEntityInfo info = ZSSEntityInfo.get(this);
		info.removeAllBuffs();
		info.applyBuff(Buff.RESIST_MAGIC, Integer.MAX_VALUE, 50);
		info.applyBuff(Buff.RESIST_STUN, Integer.MAX_VALUE, 50);
		switch(getMagicType()) {
		case FIRE:
			info.applyBuff(Buff.RESIST_FIRE, Integer.MAX_VALUE, 50);
			info.applyBuff(Buff.WEAKNESS_COLD, Integer.MAX_VALUE, 100);
			break;
		case ICE:
			info.applyBuff(Buff.RESIST_COLD, Integer.MAX_VALUE, 50);
			info.applyBuff(Buff.WEAKNESS_FIRE, Integer.MAX_VALUE, 100);
			break;
		case LIGHTNING:
			info.applyBuff(Buff.RESIST_SHOCK, Integer.MAX_VALUE, 50);
			break;
		case WIND:
			break;
		default:
		}
	}

	/** The base time required to cast a spell */
	protected int getBaseCastingTime() {
		return 80;
	}

	/** Returns the current casting time for entity animations */
	public int getCurrentCastingTime() {
		return dataWatcher.getWatchableObjectInt(CASTING_TIME);
	}

	private void setCurrentCastingTime(int time) {
		if (!worldObj.isRemote) {
			dataWatcher.updateObject(CASTING_TIME, Math.max(0, time));
		}
	}

	/** Returns the current maximum casting time for entity animations */
	public int getMaxCastingTime() {
		return dataWatcher.getWatchableObjectInt(MAX_CAST_TIME);
	}

	private void setMaxCastingTime(int time) {
		if (!worldObj.isRemote) {
			dataWatcher.updateObject(MAX_CAST_TIME, Math.max(0, time));
		}
	}

	/**
	 * Sets teleportation boundary using {@link EntityAITeleport#setTeleBounds}
	 */
	public final void setTeleBounds(AxisAlignedBB newBounds) {
		teleportAI.setTeleBounds(newBounds);
	}

	@Override
	public boolean isAIEnabled() {
		return true;
	}

	@Override
	protected float getSoundVolume() {
		return 0.4F;
	}

	@Override
	protected boolean canTriggerWalking() {
		return false;
	}

	@Override
	public boolean attackEntityAsMob(Entity target) {
		return false; // no damage on contact
	}

	@Override
	public boolean attackEntityFrom(DamageSource source, float amount) {
		if (!worldObj.isRemote) {
			boolean wasReflected = false;
			if (source.getSourceOfDamage() instanceof EntityMagicSpell) {
				EntityMagicSpell spell = (EntityMagicSpell) source.getSourceOfDamage();
				wasReflected = spell.getEntityData().getBoolean("isReflected");
				if (spell.getThrower() == this && !wasReflected) {
					return false;
				}
			}
			if (!wasReflected && source.getEntity() != null) {
				if (canTelevade() && teleportAI.teleportRandomly()) {
					return false;
				}
			}
		}
		return super.attackEntityFrom(source, amount);
	}

	@Override
	protected void damageEntity(DamageSource source, float amount) {
		if (getCurrentCastingTime() > 0 && !isEntityInvulnerable() && amount >= getMinInterruptDamage()) {
			float interruptChance = 1.0F - ((getMaxInterruptDamage() - amount) / getMaxInterruptDamage());
			if (rand.nextFloat() < interruptChance) {
				magicAI.interruptCasting();
				teleportAI.scheduleNextTeleport(2); // teleport right away - no second attacks!
			}
		}
		super.damageEntity(source, amount);
	}

	@Override
	protected Entity findPlayerToAttack() {
		return (getBrightness(1.0F) < 0.5F ? worldObj.getClosestVulnerablePlayerToEntity(this, 32.0D) : null);
	}

	/**
	 * Base spell damage
	 */
	protected float getBaseSpellDamage() {
		return 4.0F;
	}

	/**
	 * Spell area of effect
	 */
	protected float getSpellAoE() {
		return 1.25F;
	}

	/**
	 * Chance spell will be reflected when blocked by Mirror Shield
	 * @return negative values use default EntityMagicSpell handling
	 */
	protected float getReflectChance() {
		return -1.0F; // use default handling
	}

	/**
	 * Minimum damage that can possibly interrupt spell-casting
	 */
	protected float getMinInterruptDamage() {
		return 4.0F;
	}

	/**
	 * Amount of damage at which spell interruption is guaranteed
	 */
	protected float getMaxInterruptDamage() {
		return 16.0F; // 4 damage has 25% interrupt chance
	}

	/**
	 * Returns true if Wizzrobe can attempt to teleport out of harm's way
	 */
	private boolean canTelevade() {
		if (getCurrentCastingTime() > 0) {
			return false;
		} else if (teleportAI.canTeleport()) {
			return true;
		}
		return (rand.nextFloat() < getTelevadeChance());
	}

	/**
	 * Chance that Wizzrobe can teleport out of harm's way even if teleport AI cannot teleport
	 */
	protected float getTelevadeChance() {
		return 0.5F;
	}

	@Override
	public boolean canContinueCasting() {
		ZSSEntityInfo info = ZSSEntityInfo.get(this);
		if (info.isBuffActive(Buff.STUN)) {
			if (worldObj.rand.nextInt(50) > info.getBuffAmplifier(Buff.STUN)) {
				info.removeBuff(Buff.STUN);
			}
			return false;
		}
		return true;
	}

	@Override
	public int beginSpellCasting(EntityLivingBase target) {
		if (target == null) {
			return 0;
		}
		int castTime = getBaseCastingTime() - (worldObj.difficultySetting.getDifficultyId() * 10);
		castTime += (rand.nextInt(castTime) - rand.nextInt(castTime)) / 2;
		setMaxCastingTime(castTime);
		setCurrentCastingTime(castTime);
		return castTime;
	}

	@Override
	public void castPassiveSpell() {}

	@Override
	public void castRangedSpell(EntityLivingBase target, float range) {
		float difficulty = (float) worldObj.difficultySetting.getDifficultyId();
		EntityMagicSpell spell = new EntityMagicSpell(worldObj, this, target, 0.8F + (0.25F * difficulty), (float)(14 - worldObj.difficultySetting.getDifficultyId() * 4));
		spell.setType(getMagicType());
		spell.setArea(getSpellAoE());
		spell.setDamageBypassesArmor();
		spell.setDamage(getBaseSpellDamage() * difficulty);
		spell.setReflectChance(getReflectChance());
		WorldUtils.playSoundAtEntity(this, Sounds.WHOOSH, 0.4F, 0.5F);
		if (!worldObj.isRemote) {
			worldObj.spawnEntityInWorld(spell);
		}
		teleportAI.scheduleNextTeleport(rand.nextInt(5) + rand.nextInt(5) + 6);
	}

	@Override
	public void stopCasting() {
		setCurrentCastingTime(0);
	}

	@Override
	public void onLivingUpdate() {
		super.onLivingUpdate();
		// update casting time for rendering and sound effects
		int castTime = getCurrentCastingTime();
		if (!worldObj.isRemote && castTime > 0) {
			--castTime;
			setCurrentCastingTime(castTime);
			MagicType type = getMagicType();
			if (castTime % type.getSoundFrequency() == 0) {
				worldObj.playSoundAtEntity(this, type.getMovingSound(), type.getSoundVolume(rand) * 0.5F, type.getSoundPitch(rand));
			}
		}
		// spawn some Enderman-like particles
		for (int i = 0; i < 2; ++i) {
			worldObj.spawnParticle("portal",
					posX + (rand.nextDouble() - 0.5D) * (double) width,
					posY + rand.nextDouble() * (double) height - 0.25D,
					posZ + (rand.nextDouble() - 0.5D) * (double) width,
					(rand.nextDouble() - 0.5D) * 2.0D,
					-rand.nextDouble(),
					(rand.nextDouble() - 0.5D) * 2.0D);
		}
		if (worldObj.isDaytime() && ticksExisted % 20 == 0 && !isValidLightLevel()) {
			despawnEntity();
		}
	}

	@Override
	public void onUpdate() {
		super.onUpdate();
		teleportAI.invalidateBounds(256.0D);
		if (!onGround && motionY < 0.0D) {
			motionY *= 0.6D;
		}
		if (isBurning() && getMagicType() == MagicType.FIRE) {
			extinguish(); // immune to burning, but not all fire damage
		}
		if (teleportAI.getTeleBounds() == null && this.getEntityToAttack() == null && ++noTargetTime > 400) {
			noTargetTime = 0;
			Entity player = findPlayerToAttack();
			if (player instanceof EntityPlayer && !worldObj.isRemote && canEntityBeSeen(player) && !((EntityPlayer) player).capabilities.disableDamage) {
				setTarget(player);
				for (int i = 0; i < 64; ++i) {
					if (EntityAITeleport.teleportToEntity(worldObj, this, player, null, teleportAI.isGrounded)) {
						break;
					}
				}
			}
		}
	}

	@Override
	protected void fall(float distance) {}

	@Override
	protected Item getDropItem() {
		return Items.ender_pearl;
	}

	@Override
	protected void dropRareDrop(int rarity) {
		entityDropItem(getRareDrop(rarity, 4), 0.0F);
	}

	/**
	 * @param rarity	Typically 0 or 1
	 * @param modifier	Applied to book enchantment level
	 */
	private ItemStack getRareDrop(int rarity, int modifier) {
		if (rarity > 0) {
			return new ItemStack(ZSSItems.treasure, 1, Treasures.EVIL_CRYSTAL.ordinal());
		}
		if (rand.nextInt(8) == 0) {
			ItemStack book = new ItemStack(Items.book);
			EnchantmentHelper.addRandomEnchantment(rand, book, rand.nextInt(8) + rand.nextInt(8) + modifier);
			return book;
		}
		switch(getWizzrobeType()) {
		case FIRE_WIZ: return new ItemStack(ZSSItems.arrowFire);
		case ICE_WIZ: return new ItemStack(ZSSItems.arrowIce);
		case LIGHTNING_WIZ: return new ItemStack(ZSSItems.arrowLight);
		default: return null;
		}
	}

	@Override
	public float getLootableChance(EntityPlayer player, WhipType whip) {
		return 0.2F;
	}

	@Override
	public ItemStack getEntityLoot(EntityPlayer player, WhipType whip) {
		if (rand.nextInt(10 - whip.ordinal()) == 0) {
			return new ItemStack(ZSSItems.treasure, 1, Treasures.EVIL_CRYSTAL.ordinal());
		}
		return getRareDrop(0, (3 * (whip.ordinal() + 1)));
	}

	@Override
	public boolean onLootStolen(EntityPlayer player, boolean wasItemStolen) {
		return true;
	}

	@Override
	public boolean isHurtOnTheft(EntityPlayer player, WhipType whip) {
		return true;
	}

	@Override
	public boolean getCanSpawnHere() {
		return !worldObj.isDaytime() && super.getCanSpawnHere() && worldObj.getWorldTime() > Config.getTimeToSpawnWizzrobe();
	}

	@Override
	public IEntityLivingData onSpawnWithEgg(IEntityLivingData data) {
		data = super.onSpawnWithEgg(data);
		setTypeOnSpawn();
		return data;
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound compound) {
		super.writeEntityToNBT(compound);
		compound.setByte("WizzrobeType", dataWatcher.getWatchableObjectByte(TYPE_INDEX));
		AxisAlignedBB box = teleportAI.getTeleBounds();
		if (box != null) {
			NBTTagCompound bounds = new NBTTagCompound();
			bounds.setDouble("minX", box.minX);
			bounds.setDouble("maxX", box.maxX);
			bounds.setDouble("minY", box.minY);
			bounds.setDouble("maxY", box.maxY);
			bounds.setDouble("minZ", box.minZ);
			bounds.setDouble("maxZ", box.maxZ);
			compound.setTag("teleBounds", bounds);
		}
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound compound) {
		super.readEntityFromNBT(compound);
		dataWatcher.updateObject(TYPE_INDEX, compound.getByte("WizzrobeType"));
		if (compound.hasKey("teleBounds")) {
			NBTTagCompound bounds = compound.getCompoundTag("teleBounds");
			double minX = bounds.getDouble("minX");
			double maxX = bounds.getDouble("maxX");
			double minY = bounds.getDouble("minY");
			double maxY = bounds.getDouble("maxY");
			double minZ = bounds.getDouble("minZ");
			double maxZ = bounds.getDouble("maxZ");
			setTeleBounds(AxisAlignedBB.getBoundingBox(minX, minY, minZ, maxX, maxY, maxZ));
		}
	}
}
