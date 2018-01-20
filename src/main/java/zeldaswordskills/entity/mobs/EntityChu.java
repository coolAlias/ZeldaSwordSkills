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

import java.util.List;
import java.util.UUID;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.S04PacketEntityEquipment;
import net.minecraft.util.BlockPos;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.MathHelper;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.BiomeGenBase.SpawnListEntry;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import zeldaswordskills.ClientProxy;
import zeldaswordskills.api.block.IWhipBlock.WhipType;
import zeldaswordskills.api.entity.IEntityBombEater;
import zeldaswordskills.api.entity.IEntityBombIngestible;
import zeldaswordskills.api.entity.IEntityLootable;
import zeldaswordskills.item.ItemTreasure.Treasures;
import zeldaswordskills.item.ZSSItems;
import zeldaswordskills.ref.Config;
import zeldaswordskills.ref.Sounds;
import zeldaswordskills.util.WorldUtils;

/**
 * 
 * Chus are capable of merging back together when injured, making them especially
 * challenging to defeat for the unwary.
 * 
 * Upon death, they typically drop a Chu Jelly matching the Chu's color.
 * 
 */
public abstract class EntityChu extends EntitySlime implements IEntityAdditionalSpawnData, IEntityBombEater, IEntityLootable
{
	/** Chuchu types, in order of rarity and strength */
	public static enum ChuType { RED, GREEN, BLUE, YELLOW; }

	/**
	 * Returns an EntityChu instance appropriate to the current biome type
	 * @param world
	 * @param variance Chance for each successive appropriate Chu type to be used instead of previous
	 * @param pos
	 * @return Null if no appropriate Chu type found for this biome
	 */
	public static EntityChu getRandomChuForLocation(World world, float variance, BlockPos pos) {
		BiomeGenBase biome = world.getBiomeGenForCoords(pos);
		if (biome != null) {
			List<SpawnListEntry> spawns = biome.getSpawnableList(EnumCreatureType.MONSTER);
			Class<?> toSpawn = null;
			for (SpawnListEntry entry : spawns) {
				if (EntityChu.class.isAssignableFrom(entry.entityClass) && (toSpawn == null || world.rand.nextFloat() < variance)) {
					toSpawn = entry.entityClass;
				}
			}
			if (toSpawn != null) {
				try {
					return (EntityChu) toSpawn.getConstructor(World.class).newInstance(world);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}

	/** UUID for size-based damage modifier */
	protected static final UUID sizeDamageModiferUUID = UUID.fromString("28FCAB28-E297-4C07-A130-5302B4ED4E3C");

	/** Damage modifier based on current size */
	protected static final AttributeModifier sizeDamageModifier = (new AttributeModifier(sizeDamageModiferUUID, "Size Damage Modifier", 1.0D, 0)).setSaved(true);

	/** Number of times this Chu has merged */
	private int timesMerged;

	public EntityChu(World world) {
		super(world);
		setSlimeSize((1 << rand.nextInt(3)));
	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		this.getAttributeMap().registerAttribute(SharedMonsterAttributes.attackDamage).setBaseValue(this.getType().ordinal());
	}

	@Override
	public float getEyeHeight() {
		return 0.625F * height;
	}

	/** Create a new instance of the Chu's type */
	protected abstract EntityChu createInstance();

	protected void applyTypeTraits() {}

	@Override
	protected void setSlimeSize(int size) {
		super.setSlimeSize(size);
		getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue((double)((size + 1) * (size + 1)));
		setHealth(getMaxHealth());
		experienceValue = size + (getType().ordinal() + 1);
		IAttributeInstance damageAttribute = this.getEntityAttribute(SharedMonsterAttributes.attackDamage);
		damageAttribute.removeModifier(sizeDamageModifier);
		AttributeModifier newModifier = (new AttributeModifier(sizeDamageModiferUUID, "Size Damage Modifier", size, 0)).setSaved(true);
		damageAttribute.applyModifier(newModifier);
	}

	/** Returns this Chu's type */
	public abstract ChuType getType();

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
			return new ItemStack(ZSSItems.treasure, 1, Treasures.JELLY_BLOB.ordinal());
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
		}
		return false;
	}

	@Override
	public boolean attackEntityFrom(DamageSource source, float amount) {
		if (source == DamageSource.inWall) {
			return false;
		}
		return super.attackEntityFrom(source, amount);
	}

	@Override
	protected int getAttackStrength() {
		return MathHelper.floor_float(this.getDamage());
	}

	/** Returns the Chu's attack damage attribute value */
	protected float getDamage() {
		return (float) this.getEntityAttribute(SharedMonsterAttributes.attackDamage).getAttributeValue();
	}

	/**
	 * Gets the type-specific damage source
	 */
	protected DamageSource getDamageSource() {
		return new EntityDamageSource("mob", this);
	}

	/**
	 * Allows secondary effects to be applied when an entity is damaged by this Chu
	 */
	protected void applySecondaryEffects(EntityLivingBase target) {}

	/**
	 * Called when slime collides with player
	 */
	@Override
	protected void func_175451_e(EntityLivingBase target) {
		// Minimum reach of 1, max of 3; typical mob has a reach of 2
		double reach = MathHelper.clamp_double(1.0D + ((double) this.getSlimeSize() * 0.5D), 1.0D, 3.0D);
		float damage = this.getDamage();
		damage += EnchantmentHelper.getModifierForCreature(this.getHeldItem(), target.getCreatureAttribute());
		if (this.canEntityBeSeen(target) && this.getDistanceToEntity(target) < reach) {
			boolean flag = target.attackEntityFrom(this.getDamageSource(), damage);
			if (flag) {
				this.playSound(Sounds.SLIME_ATTACK, 1.0F, (this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F + 1.0F);
				// Copied from EntityMob
				int i = EnchantmentHelper.getKnockbackModifier(this);
				if (i > 0) {
					target.addVelocity((double)(-MathHelper.sin(this.rotationYaw * (float)Math.PI / 180.0F) * (float)i * 0.5F), 0.1D, (double)(MathHelper.cos(this.rotationYaw * (float)Math.PI / 180.0F) * (float)i * 0.5F));
					this.motionX *= 0.6D;
					this.motionZ *= 0.6D;
				}
				int j = EnchantmentHelper.getFireAspectModifier(this);
				if (j > 0) {
					target.setFire(j * 4);
				}
				this.applyEnchantments(this, target);
				// Apply additional secondary effects based on Chu's type
				this.applySecondaryEffects(target);
			}
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
			if (makesSoundOnLand()) {
				playSound(getJumpSound(), getSoundVolume(), ((rand.nextFloat() - rand.nextFloat()) * 0.2F + 1.0F) / 0.8F);
			}
			squishAmount = -0.5F;
		} else if (!onGround && wasOnGround) {
			squishAmount = 1.0F;
		}
		wasOnGround = onGround;
		alterSquishAmount();
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

	protected void entityLivingUpdate() {
		if (!worldObj.isRemote) {
			updateLeashedState();
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

	@Override
	public void setDead() {
		int i = getSlimeSize();
		if (!worldObj.isRemote && i > 1 && getHealth() <= 0.0F) {
			int j = 2 + rand.nextInt(3);
			for (int k = 0; k < j; ++k) {
				float f = ((float)(k % 2) - 0.5F) * (float) i / 4.0F;
				float f1 = ((float)(k / 2) - 0.5F) * (float) i / 4.0F;
				EntityChu chu = createInstance();
				chu.setSlimeSize(i / 2);
				chu.setLocationAndAngles(posX + (double) f, posY + 0.5D, posZ + (double) f1, rand.nextFloat() * 360.0F, 0.0F);
				chu.timesMerged = this.timesMerged;
				worldObj.spawnEntityInWorld(chu);
			}
		}
		super.setDead();
	}

	private void attemptMerge() {
		int i = getSlimeSize();
		if (!worldObj.isRemote && i < 3 && getHealth() < (getMaxHealth() / 2) && rand.nextInt(16) == 0) {
			List<EntityChu> list = this.worldObj.getEntitiesWithinAABB(this.getClass(), this.getEntityBoundingBox().expand(2.0D, 1.0D, 2.0D));
			for (EntityChu chu : list) {
				if (chu != this && chu.isEntityAlive() && chu.getSlimeSize() == this.getSlimeSize() && chu.getHealth() < (chu.getMaxHealth() / 2)) {
					worldObj.playSoundAtEntity(this, Sounds.CHU_MERGE, 1.0F, 1.0F / (rand.nextFloat() * 0.4F + 1.0F));
					EntityChu newChu = createInstance();
					newChu.setSlimeSize(i * 2);
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
		this.applyTypeTraits();
		return data;
	}

	@Override
	public void writeSpawnData(ByteBuf buffer) {}

	@Override
	public void readSpawnData(ByteBuf buffer) {
		// DataWatcher has been updated by the spawn packet, but we need to call #setSize to apply appropriate modifiers
		this.setSlimeSize(this.getSlimeSize());
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound compound) {
		super.writeEntityToNBT(compound);
		compound.setInteger("Size", getSlimeSize() - 1);
		compound.setInteger("timesMerged", timesMerged);
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound compound) {
		super.readEntityFromNBT(compound);
		setSlimeSize(compound.getInteger("Size") + 1);
		timesMerged = compound.getInteger("timesMerged");
	}
}
