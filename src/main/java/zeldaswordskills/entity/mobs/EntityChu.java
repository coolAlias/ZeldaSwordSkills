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

import cpw.mods.fml.common.eventhandler.Event.Result;
import cpw.mods.fml.common.registry.IEntityAdditionalSpawnData;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.particle.EntityBreakingFX;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.BiomeGenBase.SpawnListEntry;
import net.minecraft.world.chunk.Chunk;
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
public abstract class EntityChu extends EntityLiving implements IMob, IEntityAdditionalSpawnData, IEntityBombEater, IEntityLootable
{
	/** Chuchu types, in order of rarity and strength */
	public static enum ChuType { RED, GREEN, BLUE, YELLOW; }

	/**
	 * Returns an EntityChu instance appropriate to the current biome type
	 * @param world
	 * @param variance Chance for each successive appropriate Chu type to be used instead of previous
	 * @param x
	 * @param y
	 * @param z
	 * @return Null if no appropriate Chu type found for this biome
	 */
	public static EntityChu getRandomChuForLocation(World world, float variance, int x, int y, int z) {
		BiomeGenBase biome = world.getBiomeGenForCoords(x, z);
		if (biome != null) {
			List<SpawnListEntry> spawns = biome.getSpawnableList(EnumCreatureType.monster);
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

	/** Data watcher index for this Chu's size */
	private static final int CHU_SIZE_INDEX = 16;

	/** Slime fields */
	public float squishAmount;
	public float squishFactor;
	public float prevSquishFactor;

	/** the time between each jump of the slime */
	private int slimeJumpDelay;

	/** Number of times this Chu has merged */
	private int timesMerged;

	public EntityChu(World world) {
		super(world);
		yOffset = 0.0F;
		slimeJumpDelay = rand.nextInt(20) + 10;
		setSize((1 << rand.nextInt(3)));
	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		this.getAttributeMap().registerAttribute(SharedMonsterAttributes.attackDamage).setBaseValue(this.getType().ordinal());
	}

	@Override
	protected void entityInit() {
		super.entityInit();
		dataWatcher.addObject(CHU_SIZE_INDEX, (byte) 1);
	}

	/** Create a new instance of the Chu's type */
	protected abstract EntityChu createInstance();

	/**
	 * Apply traits specific to this Chu's type when spawned, e.g. any Buffs
	 */
	protected void applyTypeTraits() {}

	/**
	 * Returns this chu's size, between 1 and 4
	 */
	public int getSize() {
		return dataWatcher.getWatchableObjectByte(CHU_SIZE_INDEX);
	}

	protected void setSize(int size) {
		dataWatcher.updateObject(CHU_SIZE_INDEX, (byte) size);
		setSize(0.6F * (float) size, 0.6F * (float) size);
		setPosition(posX, posY, posZ);
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
	protected void fall(float f) {}

	@Override
	public int getTotalArmorValue() {
		return getSize() + (getType().ordinal() * 2);
	}

	@Override
	protected void dropFewItems(boolean recentlyHit, int looting) {
		if (getSize() > 1) {
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
	protected void dropRareDrop(int rarity) {
		switch(rarity) {
		case 1: entityDropItem(new ItemStack(ZSSItems.treasure,1,Treasures.JELLY_BLOB.ordinal()), 0.0F); break;
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
		return "mob.slime." + (getSize() > 1 ? "big" : "small");
	}

	@Override
	protected String getDeathSound() {
		return "mob.slime." + (getSize() > 1 ? "big" : "small");
	}

	@Override
	protected float getSoundVolume() {
		return 0.4F * (float) getSize();
	}

	@Override
	public int getVerticalFaceSpeed() {
		return 0;
	}

	/**
	 * Returns true if the slime makes a sound when it jumps (based upon the slime's size)
	 */
	protected boolean makesSoundOnJump() {
		return getSize() > 0;
	}

	/**
	 * Returns the name of the sound played when the slime jumps.
	 */
	protected String getJumpSound() {
		return "mob.slime." + (getSize() > 1 ? "big" : "small");
	}

	/**
	 * Whether this chu makes a sound when it lands
	 */
	protected boolean makesSoundOnLand() {
		return getSize() > 1;
	}

	@Override
	public boolean getCanSpawnHere() {
		if (worldObj.getWorldInfo().getTerrainType().handleSlimeSpawnReduction(rand, worldObj)) {
			return false;
		} else if (worldObj.difficultySetting != EnumDifficulty.PEACEFUL) {
			if (posY > 50.0D && rand.nextFloat() < 0.5F && rand.nextFloat() < worldObj.getCurrentMoonPhaseFactor() &&
					worldObj.getBlockLightValue(MathHelper.floor_double(posX), MathHelper.floor_double(posY), MathHelper.floor_double(posZ)) <= rand.nextInt(8)) {
				return super.getCanSpawnHere();
			}
			Chunk chunk = worldObj.getChunkFromBlockCoords(MathHelper.floor_double(posX), MathHelper.floor_double(posZ));
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
	 * Allows secondary effects to be applied when the player is damaged by this Chu
	 */
	protected void applySecondaryEffects(EntityPlayer player) {}

	@Override
	public void onCollideWithPlayer(EntityPlayer player) {
		if (this.attackTime > 0 || player.boundingBox.maxY < this.boundingBox.minY || player.boundingBox.minY > this.boundingBox.maxY) {
			return;
		}
		// Minimum reach of 1, max of 3; typical mob has a reach of 2
		double reach = MathHelper.clamp_double(1.0D + ((double) this.getSize() * 0.5D), 1.0D, 3.0D);
		float damage = this.getDamage();
		damage += EnchantmentHelper.getEnchantmentModifierLiving(this, player);
		if (this.canEntityBeSeen(player) && this.getDistanceToEntity(player) < reach) {
			boolean flag = player.attackEntityFrom(this.getDamageSource(), damage);
			if (flag) {
				this.attackTime = 20 + this.rand.nextInt(30);
				this.playSound(Sounds.SLIME_ATTACK, 1.0F, (this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F + 1.0F);
				// Copied from EntityMob
				int i = EnchantmentHelper.getKnockbackModifier(this, player);
				if (i > 0) {
					player.addVelocity((double)(-MathHelper.sin(this.rotationYaw * (float)Math.PI / 180.0F) * (float)i * 0.5F), 0.1D, (double)(MathHelper.cos(this.rotationYaw * (float)Math.PI / 180.0F) * (float)i * 0.5F));
					this.motionX *= 0.6D;
					this.motionZ *= 0.6D;
				}
				int j = EnchantmentHelper.getFireAspectModifier(this);
				if (j > 0) {
					player.setFire(j * 4);
				}
				EnchantmentHelper.func_151384_a(player, this);
				EnchantmentHelper.func_151385_b(this, player);
				// Apply additional secondary effects based on Chu's type
				this.applySecondaryEffects(player);
			}
		}
	}

	@Override
	public void onUpdate() {
		if (!worldObj.isRemote && worldObj.difficultySetting == EnumDifficulty.PEACEFUL && getSize() > 0) {
			isDead = true;
		}
		squishFactor += (squishAmount - squishFactor) * 0.5F;
		prevSquishFactor = squishFactor;
		boolean flag = onGround;
		super.onUpdate();
		int i;
		if (onGround && !flag) {
			if (worldObj.isRemote) {
				spawnParticlesOnLanding();
				if (makesSoundOnLand()) {
					playSound(getJumpSound(), getSoundVolume(), ((rand.nextFloat() - rand.nextFloat()) * 0.2F + 1.0F) / 0.8F);
				}
			}
			squishAmount = -0.5F;
		} else if (!onGround && flag) {
			squishAmount = 1.0F;
		}
		alterSquishAmount();
		if (worldObj.isRemote) {
			i = getSize();
			setSize(0.6F * (float)i, 0.6F * (float)i);
		}
	}

	@SideOnly(Side.CLIENT)
	public void spawnParticlesOnLanding() {
		int i = getSize();
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
			EntityFX particle = new EntityBreakingFX(worldObj, posX + (double) f2, boundingBox.minY, posZ + (double) f3, Items.slime_ball);
			if (particle != null) {
				particle.setRBGColorF(r, g, b);
				WorldUtils.spawnWorldParticles(worldObj, particle);
			}
		}
	}

	@Override
	protected void updateEntityActionState() {
		despawnEntity();
		EntityPlayer player = worldObj.getClosestVulnerablePlayerToEntity(this, 16.0D);
		if (player != null){
			faceEntity(player, 10.0F, 20.0F);
		}
		if (onGround && slimeJumpDelay-- <= 0) {
			slimeJumpDelay = getJumpDelay();
			if (player != null) {
				slimeJumpDelay /= 3;
			}
			isJumping = true;
			if (makesSoundOnJump()) {
				playSound(getJumpSound(), getSoundVolume(), ((rand.nextFloat() - rand.nextFloat()) * 0.2F + 1.0F) * 0.8F);
			}

			moveStrafing = 1.0F - rand.nextFloat() * 2.0F;
			moveForward = (float)(1 * getSize());
		} else {
			isJumping = false;
			if (onGround) {
				moveStrafing = moveForward = 0.0F;
			}
		}
		if (onGround && timesMerged < 4) {
			attemptMerge();
		}
	}

	/** Amount to compress rendered entity model */
	protected void alterSquishAmount() {
		squishAmount *= 0.6F;
	}

	/** Time between jumps */
	protected int getJumpDelay() {
		return rand.nextInt(20) + 10;
	}

	@Override
	public void setDead() {
		int i = getSize();
		if (!worldObj.isRemote && i > 1 && getHealth() <= 0.0F) {
			int j = 2 + rand.nextInt(3);
			for (int k = 0; k < j; ++k) {
				float f = ((float)(k % 2) - 0.5F) * (float) i / 4.0F;
				float f1 = ((float)(k / 2) - 0.5F) * (float) i / 4.0F;
				EntityChu chu = createInstance();
				chu.setSize(i / 2);
				chu.setLocationAndAngles(posX + (double) f, posY + 0.5D, posZ + (double) f1, rand.nextFloat() * 360.0F, 0.0F);
				chu.timesMerged = this.timesMerged;
				worldObj.spawnEntityInWorld(chu);
			}
		}
		super.setDead();
	}

	private void attemptMerge() {
		int i = getSize();
		if (!worldObj.isRemote && i < 3 && getHealth() < (getMaxHealth() / 2) && rand.nextInt(16) == 0) {
			List<EntityChu> list = this.worldObj.getEntitiesWithinAABB(this.getClass(), this.boundingBox.expand(2.0D, 1.0D, 2.0D));
			for (EntityChu chu : list) {
				if (chu != this && chu.isEntityAlive() && chu.getSize() == this.getSize() && chu.getHealth() < (chu.getMaxHealth() / 2)) {
					worldObj.playSoundAtEntity(this, Sounds.CHU_MERGE, 1.0F, 1.0F / (rand.nextFloat() * 0.4F + 1.0F));
					EntityChu newChu = createInstance();
					newChu.setSize(i * 2);
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
		return getSize() < 4;
	}

	@Override
	public IEntityLivingData onSpawnWithEgg(IEntityLivingData data) {
		data = super.onSpawnWithEgg(data);
		this.applyTypeTraits();
		return data;
	}

	@Override
	public void writeSpawnData(ByteBuf buffer) {}

	@Override
	public void readSpawnData(ByteBuf buffer) {
		// DataWatcher has been updated by the spawn packet, but we need to call #setSize to apply appropriate modifiers
		this.setSize(this.getSize());
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound compound) {
		super.writeEntityToNBT(compound);
		compound.setInteger("Size", getSize() - 1);
		compound.setInteger("timesMerged", timesMerged);
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound compound) {
		super.readEntityFromNBT(compound);
		setSize(compound.getInteger("Size") + 1);
		timesMerged = compound.getInteger("timesMerged");
	}
}
