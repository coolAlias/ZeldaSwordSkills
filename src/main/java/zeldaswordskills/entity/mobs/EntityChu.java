/**
    Copyright (C) <2014> <coolAlias>

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

import net.minecraft.client.particle.EntityBreakingFX;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.EntityDamageSourceIndirect;
import net.minecraft.util.MathHelper;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;
import zeldaswordskills.api.block.IWhipBlock.WhipType;
import zeldaswordskills.api.damage.DamageUtils.DamageSourceIce;
import zeldaswordskills.api.damage.DamageUtils.DamageSourceShock;
import zeldaswordskills.api.damage.IDamageSourceStun;
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
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

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
public class EntityChu extends EntityLiving implements IMob, IEntityLootable, IEntityVariant
{
	/** Chuchu types, in order of rarity and strength */
	public static enum ChuType {
		RED(BiomeType.RIVER, BiomeType.FIERY),
		GREEN(BiomeType.PLAINS, BiomeType.FOREST),
		BLUE(BiomeType.TAIGA, BiomeType.COLD),
		YELLOW(BiomeType.ARID, BiomeType.JUNGLE);

		/** Biome in which this type spawns most frequently (or possibly exclusively) */
		public final BiomeType favoredBiome;

		/** Secondary biome in which this type spawns most frequently (or possibly exclusively) */
		public final BiomeType secondBiome;

		private ChuType(BiomeType favoredBiome, BiomeType secondBiome) {
			this.favoredBiome = favoredBiome;
			this.secondBiome = secondBiome;
		}
	}

	/**
	 * Returns array of default biomes in which this entity may spawn naturally
	 */
	public static String[] getDefaultBiomes() {
		List<String> biomes = new ArrayList<String>();
		for (ChuType type : ChuType.values()) {
			biomes.addAll(Arrays.asList(type.favoredBiome.defaultBiomes));
			biomes.addAll(Arrays.asList(type.secondBiome.defaultBiomes));
		}
		biomes.addAll(Arrays.asList(BiomeType.BEACH.defaultBiomes));
		biomes.addAll(Arrays.asList(BiomeType.MOUNTAIN.defaultBiomes));
		return biomes.toArray(new String[biomes.size()]);
	}

	/** Data watcher index for this Chu's size */
	private static final int CHU_SIZE_INDEX = 16;

	/** Data watcher index for this Chu's type */
	private static final int CHU_TYPE_INDEX = 17;

	/** Data watcher index for shock time so entity can render appropriately */
	private static final int SHOCK_INDEX = 18;

	/** Slime fields */
	public float squishAmount;
	public float squishFactor;
	public float prevSquishFactor;

	/** the time between each jump of the slime */
	private int slimeJumpDelay;

	public EntityChu(World world) {
		super(world);
		yOffset = 0.0F;
		slimeJumpDelay = rand.nextInt(20) + 10;
		setType(ChuType.RED);
		setSize((1 << rand.nextInt(3)));
	}

	@Override
	protected void entityInit() {
		super.entityInit();
		dataWatcher.addObject(CHU_SIZE_INDEX, (byte) 1);
		dataWatcher.addObject(CHU_TYPE_INDEX, (byte)(ChuType.RED.ordinal()));
		dataWatcher.addObject(SHOCK_INDEX, 0);
	}

	protected EntityChu createInstance() {
		return new EntityChu(worldObj);
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
	public void setType(int type) {
		setType(ChuType.values()[type % ChuType.values().length]);
	}

	private void setTypeOnSpawn() {
		if (Config.areMobVariantsAllowed() && rand.nextFloat() < Config.getMobVariantChance()) {
			setType(rand.nextInt(ChuType.values().length));
		} else {
			BiomeGenBase biome = worldObj.getBiomeGenForCoords(MathHelper.floor_double(posX), MathHelper.floor_double(posZ));
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
		ZSSEntityInfo.get(this).removeAllBuffs();
		switch(getType()) {
		case RED:
			ZSSEntityInfo.get(this).applyBuff(Buff.RESIST_FIRE, Integer.MAX_VALUE, 75);
			break;
		case BLUE:
			ZSSEntityInfo.get(this).applyBuff(Buff.RESIST_MAGIC, Integer.MAX_VALUE, 75);
			ZSSEntityInfo.get(this).applyBuff(Buff.RESIST_COLD, Integer.MAX_VALUE, 100);
			ZSSEntityInfo.get(this).applyBuff(Buff.RESIST_SHOCK, Integer.MAX_VALUE, 50);
			break;
		case YELLOW:
			ZSSEntityInfo.get(this).applyBuff(Buff.RESIST_SHOCK, Integer.MAX_VALUE, 100);
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
		return (getSize() * worldObj.difficultySetting.getDifficultyId() * 10);
	}

	/** Random interval between shocks */
	protected int getShockInterval() {
		return (getType() == ChuType.YELLOW ? 160 : 320);
	}

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
	}

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
		} else {
			if (worldObj.difficultySetting != EnumDifficulty.PEACEFUL) {
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
			} else if (source instanceof EntityDamageSource && source.getEntity() instanceof EntityLivingBase) {
				source.getEntity().attackEntityFrom(getDamageSource(), getDamage());
				worldObj.playSoundAtEntity(this, Sounds.SHOCK, 1.0F, 1.0F / (rand.nextFloat() * 0.4F + 1.0F));
			}

			return false;
		}

		return super.attackEntityFrom(source, amount);
	}

	/** The amount of damage this chu will cause when attacking */
	protected int getDamage() {
		return getSize() + getType().ordinal();
	}

	/**
	 * Gets the type-specific damage source, taking shock time into account
	 */
	private DamageSource getDamageSource() {
		if (getShockTime() > 0) {
			return new DamageSourceShock("shock", this, getMaxStunTime(), getDamage());
		}
		switch(getType()) {
		case BLUE: return new DamageSourceIce("mob", this, 50, (getSize() > 2 ? 1 : 0));
		default: return new EntityDamageSource("mob", this);
		}
	}

	@Override
	public void onCollideWithPlayer(EntityPlayer player) {
		double d = 0.36D * (getSize() * getSize());
		if (canEntityBeSeen(player) && getDistanceSqToEntity(player) < d && player.attackEntityFrom(getDamageSource(), getDamage())) {
			playSound(Sounds.SLIME_ATTACK, 1.0F, (rand.nextFloat() - rand.nextFloat()) * 0.2F + 1.0F);
			if (rand.nextFloat() < (0.25F * getSize())) {
				applySecondaryEffects(player);
			}
		}
	}

	/**
	 * Handles any secondary effects that may occur when the player is damaged by this Chu
	 */
	private void applySecondaryEffects(EntityPlayer player) {
		switch(getType()) {
		case GREEN: ZSSEntityInfo.get(player).applyBuff(Buff.ATTACK_DOWN, 200, 50); break;
		case BLUE: ZSSEntityInfo.get(player).applyBuff(Buff.WEAKNESS_COLD, 200, 50); break;
		default:
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

		int time = getShockTime();
		if (time > 0) {
			setShockTime(time - 1);
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

		if (canChuTypeShock() && getShockTime() == 0 && !ZSSEntityInfo.get(this).isBuffActive(Buff.STUN)) {
			if (player != null && (recentlyHit > 0 || rand.nextInt(getShockInterval()) == 0)) {
				setShockTime(rand.nextInt(getSize() * 100) + (worldObj.difficultySetting.getDifficultyId() * 100));
			}
		}
		if (getShockTime() % 8 > 5 && rand.nextInt(4) == 0) {
			worldObj.playSoundAtEntity(this, Sounds.SHOCK, getSoundVolume(), 1.0F / (rand.nextFloat() * 0.4F + 1.0F));
		}
		if (onGround && getEntityData().getInteger("timesMerged") < 4) {
			attemptMerge();
		}
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
				chu.setType(getType());
				chu.setLocationAndAngles(posX + (double) f, posY + 0.5D, posZ + (double) f1, rand.nextFloat() * 360.0F, 0.0F);
				chu.getEntityData().setInteger("timesMerged", this.getEntityData().getInteger("timesMerged"));
				worldObj.spawnEntityInWorld(chu);
			}
		}

		super.setDead();
	}

	/** Amount to compress rendered entity model */
	protected void alterSquishAmount() {
		squishAmount *= 0.6F;
	}

	/** Time between jumps */
	protected int getJumpDelay() {
		return rand.nextInt(20) + 10;
	}

	private void attemptMerge() {
		int i = getSize();
		if (!worldObj.isRemote && i < 3 && getHealth() < (getMaxHealth() / 2) && rand.nextInt(16) == 0) {
			List<EntityChu> list = worldObj.getEntitiesWithinAABB(EntityChu.class, boundingBox.expand(2.0D, 1.0D, 2.0D));
			for (EntityChu chu : list) {
				if (chu != this && chu.getSize() == this.getSize() && chu.getHealth() < (chu.getMaxHealth() / 2)) {
					worldObj.playSoundAtEntity(this, Sounds.CHU_MERGE, 1.0F, 1.0F / (rand.nextFloat() * 0.4F + 1.0F));
					EntityChu newChu = createInstance();
					newChu.setSize(i * 2);
					newChu.setType(this.getType().ordinal() < chu.getType().ordinal() ? chu.getType() : this.getType());
					newChu.setLocationAndAngles((this.posX + chu.posX) / 2, posY + 0.5D, (this.posZ + chu.posZ) / 2 , rand.nextFloat() * 360.0F, 0.0F);
					newChu.getEntityData().setInteger("timesMerged", rand.nextInt(3) + this.getEntityData().getInteger("timesMerged"));
					worldObj.spawnEntityInWorld(newChu);
					chu.isDead = true;
					this.isDead = true;
					break;
				}
			}
		}
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
		compound.setInteger("Size", getSize() - 1);
		compound.setInteger("ChuType", getType().ordinal());
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound compound) {
		super.readEntityFromNBT(compound);
		setSize(compound.getInteger("Size") + 1);
		dataWatcher.updateObject(CHU_TYPE_INDEX, (byte) compound.getInteger("ChuType"));
	}
}
