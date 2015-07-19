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

import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.EntityBat;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.EntityDamageSourceIndirect;
import net.minecraft.util.MathHelper;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import zeldaswordskills.api.block.IWhipBlock.WhipType;
import zeldaswordskills.api.damage.DamageUtils.DamageSourceIce;
import zeldaswordskills.api.damage.DamageUtils.DamageSourceShock;
import zeldaswordskills.api.damage.IDamageSourceStun;
import zeldaswordskills.api.entity.IEntityLootable;
import zeldaswordskills.api.item.ArmorIndex;
import zeldaswordskills.entity.IEntityVariant;
import zeldaswordskills.entity.ZSSEntityInfo;
import zeldaswordskills.entity.buff.Buff;
import zeldaswordskills.item.ItemTreasure.Treasures;
import zeldaswordskills.item.ZSSItems;
import zeldaswordskills.ref.Config;
import zeldaswordskills.ref.Sounds;
import zeldaswordskills.util.BiomeType;

public class EntityKeese extends EntityBat implements IMob, IEntityLootable, IEntityVariant
{
	/** The different varieties of Keese */
	public static enum KeeseType {
		NORMAL(8.0F, BiomeType.PLAINS, BiomeType.FOREST),
		FIRE(12.0F, BiomeType.FIERY, BiomeType.JUNGLE),
		ICE(12.0F, BiomeType.COLD, BiomeType.TAIGA),
		THUNDER(12.0F, BiomeType.ARID, BiomeType.BEACH),
		CURSED(16.0F, null, null); // special spawn chance

		/** Default max health for this Keese Type */
		public final float maxHealth;

		/** Biome in which this type spawns most frequently (or possibly exclusively) */
		public final BiomeType favoredBiome;

		/** Secondary biome, if any, in which this type spawns most frequently (or possibly exclusively) */
		public final BiomeType secondBiome;

		private KeeseType(float maxHealth, BiomeType favoredBiome, BiomeType secondBiome) {
			this.maxHealth = maxHealth;
			this.favoredBiome = favoredBiome;
			this.secondBiome = secondBiome;
		}
	}

	/**
	 * Returns array of default biomes in which this entity may spawn naturally
	 */
	public static String[] getDefaultBiomes() {
		List<String> biomes = new ArrayList<String>();
		for (KeeseType type : KeeseType.values()) {
			if (type.favoredBiome != null) {
				biomes.addAll(Arrays.asList(type.favoredBiome.defaultBiomes));
			}
			if (type.secondBiome != null) {
				biomes.addAll(Arrays.asList(type.secondBiome.defaultBiomes));
			}
		}
		biomes.addAll(Arrays.asList(BiomeType.RIVER.defaultBiomes));
		biomes.addAll(Arrays.asList(BiomeType.MOUNTAIN.defaultBiomes));
		return biomes.toArray(new String[biomes.size()]);
	}

	/** Chunk coordinates toward which this Keese is currently heading */
	private ChunkCoordinates currentFlightTarget;

	/** Data watcher index for this Keese's type */
	private static final int TYPE_INDEX = 17;

	/** Data watcher index for shock time so entity can render appropriately */
	private static final int SHOCK_INDEX = 18;

	/** Whether this Keese has spawned a swarm already */
	private boolean swarmSpawned;

	public EntityKeese(World world) {
		super(world);
		setSize(0.5F, 0.9F);
		setIsBatHanging(true);
		setType(KeeseType.NORMAL);
	}

	@Override
	protected void entityInit() {
		super.entityInit();
		dataWatcher.addObject(TYPE_INDEX, (byte)(KeeseType.NORMAL.ordinal()));
		dataWatcher.addObject(SHOCK_INDEX, 0);
	}

	/** Returns this Keese's type */
	public KeeseType getType() {
		return KeeseType.values()[dataWatcher.getWatchableObjectByte(TYPE_INDEX) % KeeseType.values().length];
	}

	/** Sets this Keese's type */
	public void setType(KeeseType type) {
		dataWatcher.updateObject(TYPE_INDEX, (byte)(type.ordinal()));
		applyTypeTraits();
		getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(getType().maxHealth);
		setHealth(getMaxHealth());
	}

	@Override
	public EntityKeese setType(int type) {
		setType(KeeseType.values()[type % KeeseType.values().length]);
		return this;
	}

	/**
	 * Sets the Keese's type when spawned
	 */
	private void setTypeOnSpawn() {
		if (worldObj.provider.isHellWorld && rand.nextFloat() < Config.getKeeseCursedChance()) {
			setType(KeeseType.CURSED);
		} else if (rand.nextFloat() < Config.getKeeseCursedChance()) { // second chance for both Hell and everywhere else
			setType(KeeseType.CURSED);
		} else if (Config.areMobVariantsAllowed() && rand.nextFloat() < Config.getMobVariantChance()) {
			setType(rand.nextInt(KeeseType.values().length));
		} else {
			BiomeGenBase biome = worldObj.getBiomeGenForCoords(MathHelper.floor_double(posX), MathHelper.floor_double(posZ));
			BiomeType biomeType = BiomeType.getBiomeTypeFor(biome);
			for (KeeseType t : KeeseType.values()) {
				if (t.favoredBiome == biomeType || t.secondBiome == biomeType) {
					setType(t);
					return;
				}
			}
		}
	}

	/**
	 * Applies traits based on Keese's type
	 */
	private void applyTypeTraits() {
		ZSSEntityInfo info = ZSSEntityInfo.get(this);
		info.removeAllBuffs();
		info.applyBuff(Buff.EVADE_UP, Integer.MAX_VALUE, 50);
		switch(getType()) {
		case CURSED:
			info.applyBuff(Buff.RESIST_FIRE, Integer.MAX_VALUE, 100);
			info.applyBuff(Buff.WEAKNESS_HOLY, Integer.MAX_VALUE, 100);
			experienceValue = 7;
			break;
		case FIRE:
			info.applyBuff(Buff.RESIST_FIRE, Integer.MAX_VALUE, 100);
			info.applyBuff(Buff.WEAKNESS_COLD, Integer.MAX_VALUE, 100);
			isImmuneToFire = true;
			experienceValue = 3;
			break;
		case ICE:
			info.applyBuff(Buff.RESIST_COLD, Integer.MAX_VALUE, 100);
			info.applyBuff(Buff.WEAKNESS_FIRE, Integer.MAX_VALUE, 100);
			experienceValue = 3;
			break;
		case THUNDER:
			info.applyBuff(Buff.RESIST_SHOCK, Integer.MAX_VALUE, 100);
			experienceValue = 5;
			break;
		default: experienceValue = 1;
		}
	}

	/** Whether this Keese type can shock */
	protected boolean canShock() {
		return (getType() == KeeseType.THUNDER);
	}

	/** Returns the amount of time remaining for which this Keese is electrified */
	public int getShockTime() {
		return dataWatcher.getWatchableObjectInt(SHOCK_INDEX);
	}

	/** Sets the amount of time this Keese will remain electrified */
	public void setShockTime(int time) {
		dataWatcher.updateObject(SHOCK_INDEX, time);
	}

	/** Whether this Keese may spawn a swarm in the near future */
	public boolean getSpawnSwarm() {
		return !swarmSpawned;
	}

	/** Disable or re-enable possibility of spawning a swarm */
	public EntityKeese setSpawnSwarm(boolean spawnSwarm) {
		this.swarmSpawned = !spawnSwarm;
		return this;
	}

	/**
	 * Returns amount of damage this type of Keese inflicts
	 */
	private float getDamage() {
		return 2.0F;
	}

	/**
	 * Returns the DamageSource this type of Keese inflicts
	 */
	private DamageSource getDamageSource() {
		if (getShockTime() > 0) {
			return new DamageSourceShock("shock", this, worldObj.difficultySetting.getDifficultyId() * 50, 1.0F);
		}
		switch(getType()) {
		case FIRE: return new EntityDamageSource("mob", this).setFireDamage();
		case ICE: return new DamageSourceIce("mob", this, 100, 0);
		default: return new EntityDamageSource("mob", this);
		}
	}

	// rarity may be 1 or 0, with 1 being more rare
	@Override
	protected void dropRareDrop(int rarity) {
		if (getType() == KeeseType.CURSED) {
			switch(rarity) {
			case 1: entityDropItem(new ItemStack(ZSSItems.treasure,1,Treasures.EVIL_CRYSTAL.ordinal()), 0.0F); break;
			default: entityDropItem(new ItemStack(rand.nextInt(8) == 0 ? ZSSItems.smallHeart : ZSSItems.heartPiece), 0.0F);
			}
		} else {
			switch(rarity) {
			case 1: entityDropItem(new ItemStack(ZSSItems.treasure,1,Treasures.MONSTER_CLAW.ordinal()), 0.0F); break;
			default: entityDropItem(new ItemStack(rand.nextInt(3) == 1 ? Items.emerald : ZSSItems.smallHeart), 0.0F);
			}
		}
	}

	@Override
	public float getLootableChance(EntityPlayer player, WhipType whip) {
		return 0.2F;
	}

	@Override
	public ItemStack getEntityLoot(EntityPlayer player, WhipType whip) {
		if (rand.nextFloat() < (0.1F * (1 + whip.ordinal()))) {
			return new ItemStack(ZSSItems.treasure,1, (getType() == KeeseType.CURSED ? Treasures.EVIL_CRYSTAL.ordinal() : Treasures.MONSTER_CLAW.ordinal()));
		}
		return new ItemStack(rand.nextInt(3) > 0 ? Items.emerald : ZSSItems.smallHeart);
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
	public void onCollideWithPlayer(EntityPlayer player) {
		if (attackTime == 0 && canEntityBeSeen(player) && getDistanceSqToEntity(player) < 1.5D
				&& (player.getCurrentArmor(ArmorIndex.WORN_HELM) == null || player.getCurrentArmor(ArmorIndex.WORN_HELM).getItem() != ZSSItems.maskSkull)
				&& player.attackEntityFrom(getDamageSource(), getDamage()))
		{
			attackTime = rand.nextInt(20) + 20;
			playSound(Sounds.BAT_HURT, 1.0F, (rand.nextFloat() - rand.nextFloat()) * 0.2F + 1.0F);
			int t = getShockTime();
			if (t > 0) {
				setShockTime(Math.max(0, t - rand.nextInt(50) - 25));
			}
			switch(getType()) {
			case CURSED:
				applyRandomCurse(player);
				break;
			case FIRE:
				if (rand.nextFloat() < 0.5F) {
					player.setFire(rand.nextInt(4) + 4);
				}
				break;
			default:
			}
		}
	}

	/**
	 * Applies a random negative effect to the player, or possibly no effect
	 */
	private void applyRandomCurse(EntityPlayer player) {
		switch(rand.nextInt(16)) {
		case 0: ZSSEntityInfo.get(player).applyBuff(Buff.ATTACK_DOWN, rand.nextInt(500) + 100, rand.nextInt(51) + 50); break;
		case 1: ZSSEntityInfo.get(player).applyBuff(Buff.DEFENSE_DOWN, rand.nextInt(500) + 100, rand.nextInt(26) + 25); break;
		case 2: ZSSEntityInfo.get(player).applyBuff(Buff.EVADE_DOWN, rand.nextInt(500) + 100, rand.nextInt(51) + 50); break;
		case 3: ZSSEntityInfo.get(player).applyBuff(Buff.WEAKNESS_COLD, rand.nextInt(500) + 100, rand.nextInt(51) + 50); break;
		case 4: ZSSEntityInfo.get(player).applyBuff(Buff.WEAKNESS_FIRE, rand.nextInt(500) + 100, rand.nextInt(51) + 50); break;
		case 5: ZSSEntityInfo.get(player).applyBuff(Buff.WEAKNESS_MAGIC, rand.nextInt(500) + 100, rand.nextInt(51) + 50); break;
		case 6: ZSSEntityInfo.get(player).applyBuff(Buff.WEAKNESS_SHOCK, rand.nextInt(500) + 100, rand.nextInt(51) + 50); break;
		case 7: player.addPotionEffect(new PotionEffect(Potion.confusion.id, rand.nextInt(500) + 100, 1)); break;
		case 8: player.addPotionEffect(new PotionEffect(Potion.blindness.id, rand.nextInt(500) + 100, 1)); break;
		case 9: player.addPotionEffect(new PotionEffect(Potion.poison.id, rand.nextInt(100) + 50, rand.nextInt(9) / 8)); break;
		case 10: player.addPotionEffect(new PotionEffect(Potion.harm.id, 1, rand.nextInt(9) / 8)); break;
		default:
		}
	}

	@Override
	public void onUpdate() {
		super.onUpdate();
		if (!swarmSpawned && !worldObj.isRemote) {
			swarmSpawned = true;
			if (rand.nextFloat() < Config.getKeeseSwarmChance()) {
				int n = Config.getKeeseSwarmSize() - rand.nextInt(Config.getKeeseSwarmSize());
				for (int i = 0; i < n; ++i) {
					EntityKeese k = new EntityKeese(worldObj);
					double x = this.posX + rand.nextFloat() * 2.0F;
					double z = this.posZ + rand.nextFloat() * 2.0F;
					k.setPosition(x, this.posY, z);
					k.setTypeOnSpawn();
					k.swarmSpawned = true;
					worldObj.spawnEntityInWorld(k);
				}
			}
		}
		int time = getShockTime();
		if (time > 0) {
			setShockTime(time - 1);
			if (time % 8 > 6 && rand.nextInt(4) == 0) {
				worldObj.playSoundAtEntity(this, Sounds.SHOCK, getSoundVolume(), 1.0F / (rand.nextFloat() * 0.4F + 1.0F));
			}
		}
		if (!worldObj.isRemote && worldObj.difficultySetting == EnumDifficulty.PEACEFUL) {
			this.setDead();
		}
	}

	@Override
	protected void updateAITasks() {
		if (ZSSEntityInfo.get(this).isBuffActive(Buff.STUN)) {
			// because Keese get moved twice per tick due to inherited EntityBat methods
			return;
		}
		super.updateAITasks();
		if (!getIsBatHanging()) {
			if (currentFlightTarget != null && (!worldObj.isAirBlock(currentFlightTarget.posX, currentFlightTarget.posY, currentFlightTarget.posZ) || currentFlightTarget.posY < 1)) {
				currentFlightTarget = null;
			}

			if (currentFlightTarget == null || rand.nextInt(30) == 0 || currentFlightTarget.getDistanceSquared((int) posX, (int) posY, (int) posZ) < (attackingPlayer != null ? 1.0F : 4.0F)) {
				attackingPlayer = getLastAttacker() instanceof EntityPlayer ? (EntityPlayer) getLastAttacker() : worldObj.getClosestPlayerToEntity(this, 8.0D);
				if (attackingPlayer != null && !attackingPlayer.capabilities.isCreativeMode &&
						(attackingPlayer.getCurrentArmor(ArmorIndex.WORN_HELM) == null || attackingPlayer.getCurrentArmor(ArmorIndex.WORN_HELM).getItem() != ZSSItems.maskSkull))
				{
					currentFlightTarget = new ChunkCoordinates((int) attackingPlayer.posX, (int) attackingPlayer.posY + 1, (int) attackingPlayer.posZ);
					worldObj.playAuxSFXAtEntity(attackingPlayer, 1015, (int) posX, (int) posY, (int) posZ, 0);
				} else {
					currentFlightTarget = new ChunkCoordinates((int) posX + rand.nextInt(7) - rand.nextInt(7), (int) posY + rand.nextInt(6) - 2, (int) posZ + rand.nextInt(7) - rand.nextInt(7));
				}
			}

			double d0 = (double) currentFlightTarget.posX + 0.5D - posX;
			double d1 = (double) currentFlightTarget.posY + 0.1D - posY;
			double d2 = (double) currentFlightTarget.posZ + 0.5D - posZ;
			motionX += (Math.signum(d0) * 0.5D - motionX) * 0.10000000149011612D;
			motionY += (Math.signum(d1) * 0.699999988079071D - motionY) * 0.10000000149011612D;
			motionZ += (Math.signum(d2) * 0.5D - motionZ) * 0.10000000149011612D;
			float f = (float)(Math.atan2(motionZ, motionX) * 180.0D / Math.PI) - 90.0F;
			float f1 = MathHelper.wrapAngleTo180_float(f - rotationYaw);
			moveForward = 0.5F;
			rotationYaw += f1;

			if (attackingPlayer == null && rand.nextInt(100) == 0 && worldObj.getBlock(MathHelper.floor_double(posX), (int) posY + 1, MathHelper.floor_double(posZ)).isNormalCube()) {
				setIsBatHanging(true);
			} else if (canShock() && getShockTime() == 0 && !ZSSEntityInfo.get(this).isBuffActive(Buff.STUN)) {
				if (attackingPlayer != null && ((recentlyHit > 0 && rand.nextInt(20) == 0) || rand.nextInt(300) == 0)) {
					setShockTime(rand.nextInt(50) + (worldObj.difficultySetting.getDifficultyId() * (rand.nextInt(20) + 10)));
				}
			}
		}
	}

	@Override
	public boolean attackEntityFrom(DamageSource source, float amount) {
		if (isEntityInvulnerable()) {
			return false;
		} else {
			if (!worldObj.isRemote && getIsBatHanging()) {
				setIsBatHanging(false);
			}
			if (getShockTime() > 0) {
				if (source instanceof EntityDamageSourceIndirect) {
					if (source.isMagicDamage()) {
						return super.attackEntityFrom(source, amount);
					} else if (source.isExplosion()) {
						ZSSEntityInfo.get(this).stun(20 + rand.nextInt((int)(amount * 5) + 1));
						setShockTime(0);
					} else if (source instanceof IDamageSourceStun) {
						setShockTime(0);
					}
					// Hack to prevent infinite loop when attacked by other electrified mobs (other keese, chus, etc)
				} else if (source instanceof EntityDamageSource && source.getEntity() instanceof EntityPlayer && !source.damageType.equals("thorns")) {
					source.getEntity().attackEntityFrom(getDamageSource(), getDamage());
					worldObj.playSoundAtEntity(this, Sounds.SHOCK, 1.0F, 1.0F / (rand.nextFloat() * 0.4F + 1.0F));
				}

				return false;
			}

			return super.attackEntityFrom(source, amount);
		}
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound compound) {
		super.readEntityFromNBT(compound);
		swarmSpawned = compound.getBoolean("SpawnedSwarm");
		dataWatcher.updateObject(TYPE_INDEX, compound.getByte("KeeseType"));
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound compound) {
		super.writeEntityToNBT(compound);
		compound.setBoolean("SpawnedSwarm", swarmSpawned);
		compound.setByte("KeeseType", dataWatcher.getWatchableObjectByte(TYPE_INDEX));
	}

	@Override
	public IEntityLivingData onSpawnWithEgg(IEntityLivingData data) {
		data = super.onSpawnWithEgg(data);
		setTypeOnSpawn();
		return data;
	}

	@Override
	public boolean getCanSpawnHere() {
		return (worldObj.difficultySetting != EnumDifficulty.PEACEFUL && (posY < 64.0D || rand.nextInt(16) > 13) && isValidLightLevel() && !worldObj.isAnyLiquid(boundingBox));
	}

	/**
	 * Copied from EntityMob
	 */
	protected boolean isValidLightLevel() {
		int i = MathHelper.floor_double(posX);
		int j = MathHelper.floor_double(boundingBox.minY);
		int k = MathHelper.floor_double(posZ);
		if (worldObj.getSavedLightValue(EnumSkyBlock.Sky, i, j, k) > rand.nextInt(32)) {
			return false;
		}
		int l = worldObj.getBlockLightValue(i, j, k);
		if (worldObj.isThundering()) {
			int i1 = worldObj.skylightSubtracted;
			worldObj.skylightSubtracted = 10;
			l = worldObj.getBlockLightValue(i, j, k);
			worldObj.skylightSubtracted = i1;
		}
		return l <= this.rand.nextInt(8);
	}
}
