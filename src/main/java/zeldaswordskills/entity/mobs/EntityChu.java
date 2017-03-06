/**
    Copyright (C) <2017> <coolAlias>

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

import java.util.List;

import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.S04PacketEntityEquipment;
import net.minecraft.util.BlockPos;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.EntityDamageSourceIndirect;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.MathHelper;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import zeldaswordskills.ClientProxy;
import zeldaswordskills.api.block.IWhipBlock.WhipType;
import zeldaswordskills.api.damage.DamageUtils.DamageSourceIce;
import zeldaswordskills.api.damage.DamageUtils.DamageSourceShock;
import zeldaswordskills.api.damage.IDamageSourceStun;
import zeldaswordskills.api.entity.IEntityBombEater;
import zeldaswordskills.api.entity.IEntityBombIngestible;
import zeldaswordskills.api.entity.IEntityLootable;
import zeldaswordskills.entity.IEntityVariant;
import zeldaswordskills.entity.ZSSEntityInfo;
import zeldaswordskills.entity.buff.Buff;
import zeldaswordskills.item.ItemTreasure.Treasures;
import zeldaswordskills.item.ZSSItems;
import zeldaswordskills.ref.Config;
import zeldaswordskills.ref.Sounds;
import zeldaswordskills.util.BiomeType;
import zeldaswordskills.util.WorldUtils;

import com.google.common.collect.Lists;

/**
 * 
 * Chuchu traits:
 * 
 * All chus are capable of merging back together when injured, making them especially
 * challenging to defeat for the unwary.
 * 
 * Red:
 * The weakest of the Chu types, but highly resistant to fire. Drops red chu jelly.
 * These are most often found in swamps.
 * 
 * Green:
 * Slightly stronger than the Red Chu, but no special resistances. Drops green chu jelly.
 * These are most often found in plains and are known to cause weakness.
 * 
 * Blue:
 * Blue Chus are the rarest type of all, dropping blue chu jelly.
 * They are known to occasionally electrify, like the Yellow Chu, are highly resistant to both
 * magic and cold, and cause cold damage when not electrified. They are most often found in taiga biomes.
 * 
 * Yellow:
 * Yellow chus are the most difficult to defeat, producing an electrical aura when they
 * sense danger or take damage. While the aura is active, they chu is immune to all damage
 * and attacking it directly will cause shock damage to the attacker. Stun effects and
 * explosions are effective in forcing the chu to drop its aura, or one can simply wait.
 * Magic damage, however, is able to penetrate their defenses no matter what.
 * Drops yellow chu jelly. These are most often found in deserts.
 *
 */
public class EntityChu extends EntitySlime implements IEntityBombEater, IEntityLootable, IEntityVariant
{
	/** Chuchu types, in order of rarity and strength */
	public static enum ChuType {
		RED(1, BiomeType.RIVER, BiomeType.FIERY),
		GREEN(2, BiomeType.PLAINS, BiomeType.FOREST),
		BLUE(3, BiomeType.TAIGA, BiomeType.COLD),
		YELLOW(4, BiomeType.ARID, BiomeType.JUNGLE);
		/** Modifier for damage, experience, and possibly other things */
		public final int modifier;
		/** Biome in which this type spawns most frequently (or possibly exclusively) */
		public final BiomeType favoredBiome;
		/** Secondary biome in which this type spawns most frequently (or possibly exclusively) */
		public final BiomeType secondBiome;
		private ChuType(int modifier, BiomeType favoredBiome, BiomeType secondBiome) {
			this.modifier = modifier;
			this.favoredBiome = favoredBiome;
			this.secondBiome = secondBiome;
		}
		/** Returns the Chu type by item damage */
		public static final ChuType fromDamage(int damage) {
			return ChuType.values()[damage % ChuType.values().length];
		}
	}

	/**
	 * Returns array of default biomes in which this entity may spawn naturally
	 */
	public static String[] getDefaultBiomes() {
		List<BiomeType> biomes = Lists.newArrayList(BiomeType.BEACH, BiomeType.MOUNTAIN);
		for (ChuType type : ChuType.values()) {
			biomes.add(type.favoredBiome);
			biomes.add(type.secondBiome);
		}
		return BiomeType.getBiomeArray(null, biomes.toArray(new BiomeType[biomes.size()]));
	}

	/** Data watcher index for this Chu's type */
	private static final int CHU_TYPE_INDEX = 17;

	/** Data watcher index for shock time so entity can render appropriately */
	private static final int SHOCK_INDEX = 18;

	/** Number of times this Chu has merged */
	private int timesMerged;

	public EntityChu(World world) {
		super(world);
		setType(ChuType.RED);
	}

	@Override
	protected void entityInit() {
		super.entityInit();
		dataWatcher.addObject(CHU_TYPE_INDEX, (byte)(ChuType.RED.ordinal()));
		dataWatcher.addObject(SHOCK_INDEX, 0);
	}

	@Override
	protected EntityChu createInstance() {
		EntityChu chu = new EntityChu(worldObj);
		chu.setType(getType());
		chu.timesMerged = this.timesMerged;
		return chu;
	}

	@Override
	public float getEyeHeight() {
		return 0.625F * height;
	}

	/** Returns this Chu's type */
	public ChuType getType() {
		return ChuType.values()[dataWatcher.getWatchableObjectByte(CHU_TYPE_INDEX)];
	}

	/** Sets this Chu's type */
	public void setType(ChuType type) {
		dataWatcher.updateObject(CHU_TYPE_INDEX, (byte)(type.ordinal()));
		applyTypeTraits();
	}

	@Override
	public EntityChu setType(int type) {
		setType(ChuType.values()[type % ChuType.values().length]);
		return this;
	}

	private void setTypeOnSpawn() {
		if (Config.areMobVariantsAllowed() && rand.nextFloat() < Config.getMobVariantChance()) {
			setType(rand.nextInt(ChuType.values().length));
		} else {
			BiomeGenBase biome = worldObj.getBiomeGenForCoords(new BlockPos(this));
			BiomeType biomeType = BiomeType.getBiomeTypeFor(biome);
			for (ChuType t : ChuType.values()) {
				if (t.favoredBiome == biomeType || t.secondBiome == biomeType) {
					setType(t);
					return;
				}
			}
		}
	}

	/**
	 * Applies traits based on Chu's type
	 */
	private void applyTypeTraits() {
		ZSSEntityInfo info = ZSSEntityInfo.get(this);
		info.removeAllBuffs();
		switch(getType()) {
		case RED:
			info.applyBuff(Buff.RESIST_FIRE, Integer.MAX_VALUE, 75);
			break;
		case BLUE:
			info.applyBuff(Buff.RESIST_MAGIC, Integer.MAX_VALUE, 75);
			info.applyBuff(Buff.RESIST_COLD, Integer.MAX_VALUE, 100);
			info.applyBuff(Buff.RESIST_SHOCK, Integer.MAX_VALUE, 50);
			break;
		case YELLOW:
			info.applyBuff(Buff.RESIST_SHOCK, Integer.MAX_VALUE, 100);
			break;
		default:
		}
	}

	/** Whether this chu type can shock; always true for Yellow, sometimes true for Blue */
	protected boolean canChuTypeShock() {
		return (getType() == ChuType.YELLOW || (getType() == ChuType.BLUE && rand.nextInt(80) == 0));
	}

	/** Returns the amount of time remaining for which this Chu is electrified */
	public int getShockTime() {
		return dataWatcher.getWatchableObjectInt(SHOCK_INDEX);
	}

	/** Sets the amount of time this Chu will remain electrified */
	public void setShockTime(int time) {
		dataWatcher.updateObject(SHOCK_INDEX, time);
	}

	/** Returns max time affected entities will be stunned when shocked */
	protected int getMaxStunTime() {
		return (getSlimeSize() * worldObj.getDifficulty().getDifficultyId() * 10);
	}

	/** Random interval between shocks */
	protected int getShockInterval() {
		return (getType() == ChuType.YELLOW ? 160 : 320);
	}

	@Override
	protected void setSlimeSize(int size) {
		super.setSlimeSize(size);
		getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue((double)((size + 1) * (size + 1)));
		setHealth(getMaxHealth());
		experienceValue += getType().modifier + 1;
	}

	@Override
	public boolean canBreatheUnderwater() {
		return true;
	}

	@Override
	public void fall(float distance, float damageMultiplier) {}

	@Override
	public int getTotalArmorValue() {
		return getSlimeSize() + (getType().ordinal() * 2);
	}

	@Override
	protected void dropFewItems(boolean recentlyHit, int looting) {
		if (getSlimeSize() > 1) {
			int k = rand.nextInt(4) - 2;
			if (looting > 0) {
				k += rand.nextInt(looting + 1);
			}
			for (int l = 0; l < k; ++l) {
				entityDropItem(new ItemStack(ZSSItems.jellyChu, 1, getType().ordinal()), 0.0F);
			}
		}
	}

	@Override
	protected void addRandomDrop() {
		switch(rand.nextInt(8)) {
		case 1: entityDropItem(new ItemStack(ZSSItems.treasure, 1, Treasures.JELLY_BLOB.ordinal()), 0.0F); break;
		default: entityDropItem(new ItemStack(rand.nextInt(3) == 1 ? Items.emerald : ZSSItems.smallHeart), 0.0F);
		}
	}

	@Override
	public float getLootableChance(EntityPlayer player, WhipType whip) {
		return 0.2F;
	}

	@Override
	public ItemStack getEntityLoot(EntityPlayer player, WhipType whip) {
		if (rand.nextFloat() < (0.1F * (1 + whip.ordinal()))) {
			return new ItemStack(ZSSItems.treasure,1,Treasures.JELLY_BLOB.ordinal());
		}
		return new ItemStack(ZSSItems.jellyChu, 1, getType().ordinal());
	}

	@Override
	public boolean onLootStolen(EntityPlayer player, boolean wasItemStolen) {
		return true;
	}

	@Override
	public boolean isHurtOnTheft(EntityPlayer player, WhipType whip) {
		return Config.getHurtOnSteal();
	}

	@Override
	protected String getHurtSound() {
		return "mob.slime." + (getSlimeSize() > 1 ? "big" : "small");
	}

	@Override
	protected String getDeathSound() {
		return "mob.slime." + (getSlimeSize() > 1 ? "big" : "small");
	}

	@Override
	protected float getSoundVolume() {
		return 0.4F * (float) getSlimeSize();
	}

	@Override
	public int getVerticalFaceSpeed() {
		return 0;
	}

	/**
	 * Returns true if the slime makes a sound when it jumps (based upon the slime's size)
	 */
	protected boolean makesSoundOnJump() {
		return getSlimeSize() > 0;
	}

	/**
	 * Returns the name of the sound played when the slime jumps.
	 */
	protected String getJumpSound() {
		return "mob.slime." + (getSlimeSize() > 1 ? "big" : "small");
	}

	/**
	 * Whether this chu makes a sound when it lands
	 */
	protected boolean makesSoundOnLand() {
		return getSlimeSize() > 1;
	}

	@Override
	public boolean getCanSpawnHere() {
		if (worldObj.getWorldInfo().getTerrainType().handleSlimeSpawnReduction(rand, worldObj)) {
			return false;
		} else {
			BlockPos pos = new BlockPos(this);
			if (worldObj.getDifficulty() == EnumDifficulty.PEACEFUL || worldObj.getLightFor(EnumSkyBlock.SKY, pos) > rand.nextInt(32)) {
				return false;
			}
			if (posY > 50.0D && rand.nextFloat() < 0.5F && rand.nextFloat() < worldObj.getCurrentMoonPhaseFactor()) {
				int light = worldObj.getLightFromNeighbors(pos);
				if (worldObj.isThundering()) {
					int j = worldObj.getSkylightSubtracted();
					worldObj.setSkylightSubtracted(10);
					light = worldObj.getLightFromNeighbors(pos);
					worldObj.setSkylightSubtracted(j);
				}
				return light <= rand.nextInt(8) && super.getCanSpawnHere();
			}
			Chunk chunk = worldObj.getChunkFromBlockCoords(pos);
			if (rand.nextInt(10) == 0 && chunk.getRandomWithSeed(432191789L).nextInt(10) == 0 && posY < 50.0D) {
				return super.getCanSpawnHere();
			}

			return false;
		}
	}

	@Override
	public boolean attackEntityFrom(DamageSource source, float amount) {
		if (source == DamageSource.inWall) {
			return false;
		} else if (getShockTime() > 0) {
			if (source instanceof EntityDamageSourceIndirect) {
				if (source.isMagicDamage()) {
					return super.attackEntityFrom(source, amount);
				} else if (source.isExplosion()) {
					ZSSEntityInfo.get(this).stun(20 + rand.nextInt((int)(amount * 5) + 1));
					setShockTime(0);
				} else if (source instanceof IDamageSourceStun) {
					setShockTime(0);
				}
				// Hack to prevent infinite loop when attacked by other electrified mobs (other chus, keese, etc)
			} else if (source instanceof EntityDamageSource && source.getEntity() instanceof EntityPlayer && !source.damageType.equals("thorns")) {
				source.getEntity().attackEntityFrom(getDamageSource(), getAttackStrength());
				worldObj.playSoundAtEntity(this, Sounds.SHOCK, 1.0F, 1.0F / (rand.nextFloat() * 0.4F + 1.0F));
			}

			return false;
		}

		return super.attackEntityFrom(source, amount);
	}

	/** The amount of damage this chu will cause when attacking */
	@Override
	protected int getAttackStrength() {
		return super.getAttackStrength() + getType().modifier;
	}

	/**
	 * Gets the type-specific damage source, taking shock time into account
	 */
	private DamageSource getDamageSource() {
		if (getShockTime() > 0) {
			return new DamageSourceShock("shock", this, getMaxStunTime(), getAttackStrength());
		}
		switch(getType()) {
		case BLUE: return new DamageSourceIce("mob", this, 50, (getSlimeSize() > 2 ? 1 : 0));
		default: return new EntityDamageSource("mob", this);
		}
	}

	/**
	 * Called when slime collides with player
	 */
	@Override
	protected void func_175451_e(EntityLivingBase target) {
		int size = getSlimeSize();
		double min_distance = 0.6D * 0.6D * size * size;
		if (canEntityBeSeen(target) && getDistanceSqToEntity(target) < min_distance && target.attackEntityFrom(getDamageSource(), getAttackStrength())) {
			playSound(Sounds.SLIME_ATTACK, 1.0F, (rand.nextFloat() - rand.nextFloat()) * 0.2F + 1.0F);
			if (rand.nextFloat() < (0.25F * getSlimeSize())) {
				applySecondaryEffects(target);
			}
			int t = getShockTime();
			if (t > 0) {
				setShockTime(Math.max(0, t - rand.nextInt(100) - 50));
			}
			applyEnchantments(this, target);
		}
	}

	/**
	 * Handles any secondary effects that may occur when the target is damaged by this Chu
	 */
	private void applySecondaryEffects(EntityLivingBase target) {
		switch(getType()) {
		case GREEN: ZSSEntityInfo.get(target).applyBuff(Buff.ATTACK_DOWN, 200, 50); break;
		case BLUE: ZSSEntityInfo.get(target).applyBuff(Buff.WEAKNESS_COLD, 200, 50); break;
		default:
		}
	}

	protected boolean wasOnGround;

	@Override
	public void onUpdate() {
		if (!worldObj.isRemote && worldObj.getDifficulty() == EnumDifficulty.PEACEFUL && getSlimeSize() > 0) {
			isDead = true;
		}
		squishFactor += (squishAmount - squishFactor) * 0.5F;
		prevSquishFactor = squishFactor;
		// Hack to bypass slime's onUpdate:
		if (net.minecraftforge.common.ForgeHooks.onLivingUpdate(this)) return;
		entityLivingBaseUpdate();
		entityLivingUpdate();
		super.onEntityUpdate();
		// End hack, start copy/pasta:
		if (onGround && !wasOnGround && worldObj.isRemote) {
			spawnParticlesOnLanding();
			/*
			int i = this.getSlimeSize();

			for (int j = 0; j < i * 8; ++j)
			{
				float f = this.rand.nextFloat() * (float)Math.PI * 2.0F;
				float f1 = this.rand.nextFloat() * 0.5F + 0.5F;
				float f2 = MathHelper.sin(f) * (float)i * 0.5F * f1;
				float f3 = MathHelper.cos(f) * (float)i * 0.5F * f1;
				World world = this.worldObj;
				EnumParticleTypes enumparticletypes = this.func_180487_n();
				double d0 = this.posX + (double)f2;
				double d1 = this.posZ + (double)f3;
				world.spawnParticle(enumparticletypes, d0, this.getEntityBoundingBox().minY, d1, 0.0D, 0.0D, 0.0D, new int[0]);
			}
			 */
			if (makesSoundOnLand()) {
				playSound(getJumpSound(), getSoundVolume(), ((rand.nextFloat() - rand.nextFloat()) * 0.2F + 1.0F) / 0.8F);
			}
			squishAmount = -0.5F;
		} else if (!onGround && wasOnGround) {
			squishAmount = 1.0F;
		}
		wasOnGround = onGround;
		alterSquishAmount();
		if (canChuTypeShock()) {
			updateShockState();
		}
		if (onGround && timesMerged < 4) {
			attemptMerge();
		}
	}

	// If only EntitySlime was more inheritance-friendly, e.g. call 'onLanded' to spawn particles, etc.
	private ItemStack[] previousEquipment = new ItemStack[5];
	private void entityLivingBaseUpdate() {
		if (!this.worldObj.isRemote) {
			int i = this.getArrowCountInEntity();
			if (i > 0) {
				if (this.arrowHitTimer <= 0) {
					this.arrowHitTimer = 20 * (30 - i);
				}
				--this.arrowHitTimer;
				if (this.arrowHitTimer <= 0) {
					this.setArrowCountInEntity(i - 1);
				}
			}
			for (int j = 0; j < 5; ++j) {
				ItemStack itemstack = this.previousEquipment[j];
				ItemStack itemstack1 = this.getEquipmentInSlot(j);
				if (!ItemStack.areItemStacksEqual(itemstack1, itemstack)) {
					((WorldServer) worldObj).getEntityTracker().sendToAllTrackingEntity(this, new S04PacketEntityEquipment(this.getEntityId(), j, itemstack1));
					if (itemstack != null) {
						getAttributeMap().removeAttributeModifiers(itemstack.getAttributeModifiers());
					}
					if (itemstack1 != null) {
						getAttributeMap().applyAttributeModifiers(itemstack1.getAttributeModifiers());
					}
					this.previousEquipment[j] = itemstack1 == null ? null : itemstack1.copy();
				}
			}
			if (ticksExisted % 20 == 0) {
				getCombatTracker().reset();
			}
		}
		this.onLivingUpdate();
		double d0 = this.posX - this.prevPosX;
		double d1 = this.posZ - this.prevPosZ;
		float f = (float)(d0 * d0 + d1 * d1);
		float f1 = this.renderYawOffset;
		float f2 = 0.0F;
		this.prevOnGroundSpeedFactor = this.onGroundSpeedFactor;
		float f3 = 0.0F;
		if (f > 0.0025000002F) {
			f3 = 1.0F;
			f2 = (float)Math.sqrt((double)f) * 3.0F;
			f1 = (float)Math.atan2(d1, d0) * 180.0F / (float)Math.PI - 90.0F;
		}
		if (this.swingProgress > 0.0F) {
			f1 = this.rotationYaw;
		}
		if (!this.onGround) {
			f3 = 0.0F;
		}
		this.onGroundSpeedFactor += (f3 - this.onGroundSpeedFactor) * 0.3F;
		this.worldObj.theProfiler.startSection("headTurn");
		f2 = this.updateDistance(f1, f2);
		this.worldObj.theProfiler.endSection();
		this.worldObj.theProfiler.startSection("rangeChecks");
		while (this.rotationYaw - this.prevRotationYaw < -180.0F) {
			this.prevRotationYaw -= 360.0F;
		}
		while (this.rotationYaw - this.prevRotationYaw >= 180.0F) {
			this.prevRotationYaw += 360.0F;
		}
		while (this.renderYawOffset - this.prevRenderYawOffset < -180.0F) {
			this.prevRenderYawOffset -= 360.0F;
		}
		while (this.renderYawOffset - this.prevRenderYawOffset >= 180.0F) {
			this.prevRenderYawOffset += 360.0F;
		}
		while (this.rotationPitch - this.prevRotationPitch < -180.0F) {
			this.prevRotationPitch -= 360.0F;
		}
		while (this.rotationPitch - this.prevRotationPitch >= 180.0F) {
			this.prevRotationPitch += 360.0F;
		}
		while (this.rotationYawHead - this.prevRotationYawHead < -180.0F) {
			this.prevRotationYawHead -= 360.0F;
		}
		while (this.rotationYawHead - this.prevRotationYawHead >= 180.0F) {
			this.prevRotationYawHead += 360.0F;
		}
		this.worldObj.theProfiler.endSection();
		this.movedDistance += f2;
	}

	private void entityLivingUpdate() {
		if (!worldObj.isRemote) {
			updateLeashedState();
		}
	}

	/*
	@Override
	public void onUpdate() {
		super.onUpdate();
		if (canChuTypeShock()) {
			updateShockState();
		}
		if (onGround && getEntityData().getInteger("timesMerged") < 4) {
			attemptMerge();
		}
	}
	 */

	/**
	 * Updates the Chu's shock status; only called if canChuTypeShock() returns true
	 */
	protected void updateShockState() {
		if (getShockTime() == 0 && !ZSSEntityInfo.get(this).isBuffActive(Buff.STUN)) {
			EntityPlayer player = worldObj.getClosestPlayerToEntity(this, 16.0D);
			if (player != null && (recentlyHit > 0 || rand.nextInt(getShockInterval()) == 0)) {
				setShockTime(rand.nextInt(getSlimeSize() * 50) + (worldObj.getDifficulty().getDifficultyId() * (rand.nextInt(20) + 10)));
			}
		}
		if (getShockTime() % 8 > 5 && rand.nextInt(4) == 0) {
			worldObj.playSoundAtEntity(this, Sounds.SHOCK, getSoundVolume(), 1.0F / (rand.nextFloat() * 0.4F + 1.0F));
		}
		int time = getShockTime();
		if (time > 0) {
			setShockTime(time - 1);
		}
	}

	/**
	 * Particle to spawn upon landing
	 * TODO - return custom particle to avoid the stupid onUpdate hack above
	 */
	@Override
	protected EnumParticleTypes getParticleType() {
		return super.getParticleType();
	}

	@SideOnly(Side.CLIENT)
	private void spawnParticlesOnLanding() {
		int i = getSlimeSize();
		float r = 1.0F, g = 1.0F, b = 1.0F;
		switch(getType()) {
		case RED: r = 0.65F; g = 0.25F; b = 0.3F; break;
		case BLUE: r = 0.25F; g = 0.4F; b = 0.75F; break;
		case YELLOW: g = 0.65F; b = 0.0F; break;
		default:
		}
		for (int j = 0; j < i * 8; ++j) {
			float f = rand.nextFloat() * (float) Math.PI * 2.0F;
			float f1 = rand.nextFloat() * 0.5F + 0.5F;
			float f2 = MathHelper.sin(f) * (float) i * 0.5F * f1;
			float f3 = MathHelper.cos(f) * (float) i * 0.5F * f1;
			// Need to use a factory to return the particle without automatically adding it to the effect renderer
			IParticleFactory factory = ClientProxy.particleFactoryMap.get(EnumParticleTypes.SLIME.getParticleID());
			// Alternate option: EntityBreakingFX.SlimeFactory factory = new EntityBreakingFX.SlimeFactory();
			if (factory != null) {
				EntityFX particle = factory.getEntityFX(EnumParticleTypes.SLIME.getParticleID(), worldObj, posX + (double) f2, getEntityBoundingBox().minY, posZ + (double) f3, 0, 0, 0);
				if (particle != null) {
					particle.setRBGColorF(r, g, b);
					WorldUtils.spawnWorldParticles(worldObj, particle);
				}
			}
		}
	}

	private void attemptMerge() {
		int i = getSlimeSize();
		if (!worldObj.isRemote && i < 3 && getHealth() < (getMaxHealth() / 2) && rand.nextInt(16) == 0) {
			List<EntityChu> list = worldObj.getEntitiesWithinAABB(EntityChu.class, getEntityBoundingBox().expand(2.0D, 1.0D, 2.0D));
			for (EntityChu chu : list) {
				if (chu != this && chu.getSlimeSize() == this.getSlimeSize() && chu.getHealth() < (chu.getMaxHealth() / 2)) {
					worldObj.playSoundAtEntity(this, Sounds.CHU_MERGE, 1.0F, 1.0F / (rand.nextFloat() * 0.4F + 1.0F));
					EntityChu newChu = createInstance();
					newChu.setSlimeSize(i * 2);
					newChu.setType(this.getType().ordinal() < chu.getType().ordinal() ? chu.getType() : this.getType());
					newChu.setLocationAndAngles((this.posX + chu.posX) / 2, posY + 0.5D, (this.posZ + chu.posZ) / 2 , rand.nextFloat() * 360.0F, 0.0F);
					newChu.timesMerged = rand.nextInt(4) + 1 + this.timesMerged;
					worldObj.spawnEntityInWorld(newChu);
					chu.isDead = true;
					this.isDead = true;
					break;
				}
			}
		}
	}

	@Override
	public Result ingestBomb(IEntityBombIngestible bomb) {
		worldObj.playSoundAtEntity(this, Sounds.CHU_MERGE, 1.0F, 1.0F / (rand.nextFloat() * 0.4F + 1.0F));
		return Result.DEFAULT;
	}

	@Override
	public boolean onBombIndigestion(IEntityBombIngestible bomb) {
		return true;
	}

	@Override
	public boolean doesIngestedBombExplode(IEntityBombIngestible bomb) {
		return true;
	}

	@Override
	public boolean isIngestedBombFatal(IEntityBombIngestible bomb) {
		return getSlimeSize() < 4;
	}

	@Override
	public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, IEntityLivingData data) {
		data = super.onInitialSpawn(difficulty, data);
		setTypeOnSpawn();
		return data;
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound compound) {
		super.writeEntityToNBT(compound);
		compound.setInteger("ChuType", getType().ordinal());
		compound.setInteger("timesMerged", timesMerged);
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound compound) {
		super.readEntityFromNBT(compound);
		dataWatcher.updateObject(CHU_TYPE_INDEX, (byte) compound.getInteger("ChuType"));
		timesMerged = compound.getInteger("timesMerged");
	}
}
