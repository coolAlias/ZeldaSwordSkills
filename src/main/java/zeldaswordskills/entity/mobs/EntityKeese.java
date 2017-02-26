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

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.EntityBat;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.BiomeGenBase.SpawnListEntry;
import zeldaswordskills.api.block.IWhipBlock.WhipType;
import zeldaswordskills.api.entity.IEntityLootable;
import zeldaswordskills.api.item.ArmorIndex;
import zeldaswordskills.entity.ZSSEntityInfo;
import zeldaswordskills.entity.buff.Buff;
import zeldaswordskills.item.ItemRupee;
import zeldaswordskills.item.ItemTreasure.Treasures;
import zeldaswordskills.item.ZSSItems;
import zeldaswordskills.ref.Config;
import zeldaswordskills.ref.Sounds;

public class EntityKeese extends EntityBat implements IMob, IEntityLootable
{
	/**
	 * Returns an EntityKeese instance appropriate to the current biome type
	 * @param world
	 * @param variance Chance for each successive appropriate Keese type to be used instead of previous
	 * @param x
	 * @param y
	 * @param z
	 * @return Null if no appropriate Keese type found for this biome
	 */
	public static EntityKeese getRandomKeeseForLocation(World world, float variance, int x, int y, int z) {
		BiomeGenBase biome = world.getBiomeGenForCoords(x, z);
		if (biome != null) {
			// Note that Keese use the AMBIENT spawn list instead of MONSTER
			List<SpawnListEntry> spawns = biome.getSpawnableList(EnumCreatureType.ambient);
			Class<?> toSpawn = null;
			for (SpawnListEntry entry : spawns) {
				if (EntityKeese.class.isAssignableFrom(entry.entityClass) && (toSpawn == null || world.rand.nextFloat() < variance)) {
					toSpawn = entry.entityClass;
				}
			}
			if (toSpawn != null) {
				try {
					return (EntityKeese) toSpawn.getConstructor(World.class).newInstance(world);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}

	/** Chunk coordinates toward which this Keese is currently heading */
	private ChunkCoordinates currentFlightTarget;

	/** Whether this Keese has spawned a swarm already */
	private boolean swarmSpawned;

	public EntityKeese(World world) {
		super(world);
		this.experienceValue = 1;
	}

	/** Create a new Keese instance of this same type for swarming */
	protected EntityKeese createInstance() {
		return new EntityKeese(this.worldObj);
	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		this.getAttributeMap().registerAttribute(SharedMonsterAttributes.attackDamage).setBaseValue(2.0D);
		this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(8.0F);
		ZSSEntityInfo.get(this).applyBuff(Buff.EVADE_UP, Integer.MAX_VALUE, 50);
	}

	/** Returns the entity's attack damage attribute value */
	protected float getDamage() {
		return (float) this.getEntityAttribute(SharedMonsterAttributes.attackDamage).getAttributeValue();
	}

	/** Whether this Keese may spawn a swarm in the near future */
	public boolean canSpawnSwarm() {
		return !swarmSpawned;
	}

	/** Disable or re-enable possibility of spawning a swarm */
	public EntityKeese setSpawnSwarm(boolean spawnSwarm) {
		this.swarmSpawned = !spawnSwarm;
		return this;
	}

	/**
	 * Returns the DamageSource this type of Keese inflicts
	 */
	protected DamageSource getDamageSource() {
		return new EntityDamageSource("mob", this);
	}

	// rarity may be 1 or 0, with 1 being more rare
	@Override
	protected void dropRareDrop(int rarity) {
		switch (rarity) {
		case 1: this.entityDropItem(new ItemStack(ZSSItems.treasure, 1, Treasures.MONSTER_CLAW.ordinal()), 0.0F); break;
		default: this.entityDropItem(this.rand.nextInt(3) == 0 ? new ItemStack(ZSSItems.rupee, 1, ItemRupee.Rupee.BLUE_RUPEE.ordinal()) : new ItemStack(ZSSItems.smallHeart), 0.0F);
		}
	}

	@Override
	public float getLootableChance(EntityPlayer player, WhipType whip) {
		return 0.2F;
	}

	@Override
	public ItemStack getEntityLoot(EntityPlayer player, WhipType whip) {
		if (this.rand.nextFloat() < (0.1F * (1 + whip.ordinal()))) {
			return new ItemStack(ZSSItems.treasure, 1, Treasures.MONSTER_CLAW.ordinal());
		}
		return (this.rand.nextInt(3) > 0 ? new ItemStack(ZSSItems.rupee, 1, ItemRupee.Rupee.GREEN_RUPEE.ordinal()) : new ItemStack(ZSSItems.smallHeart));
	}

	@Override
	public boolean onLootStolen(EntityPlayer player, boolean wasItemStolen) {
		return true;
	}

	@Override
	public boolean isHurtOnTheft(EntityPlayer player, WhipType whip) {
		return true;
	}

	/**
	 * Allows secondary effects to be applied when the player is damaged by this Chu
	 */
	protected void applySecondaryEffects(EntityPlayer player) {}

	@Override
	public void onCollideWithPlayer(EntityPlayer player) {
		if (attackTime < 1 && canEntityBeSeen(player) && getDistanceSqToEntity(player) < 2.0D
				&& (player.getCurrentArmor(ArmorIndex.WORN_HELM) == null || player.getCurrentArmor(ArmorIndex.WORN_HELM).getItem() != ZSSItems.maskSkull))
		{
			float damage = this.getDamage();
			int i = 0;
			damage += EnchantmentHelper.getEnchantmentModifierLiving(this, player);
			i += EnchantmentHelper.getKnockbackModifier(this, player);
			boolean flag = player.attackEntityFrom(this.getDamageSource(), damage);
			if (flag) {
				this.attackTime = this.rand.nextInt(20) + 20;
				playSound(Sounds.BAT_HURT, 1.0F, (rand.nextFloat() - rand.nextFloat()) * 0.2F + 1.0F);
				if (i > 0) {
					player.addVelocity((double)(-MathHelper.sin(this.rotationYaw * (float)Math.PI / 180.0F) * (float)i * 0.5F), 0.1D, (double)(MathHelper.cos(this.rotationYaw * (float)Math.PI / 180.0F) * (float)i * 0.5F));
				}
				int j = EnchantmentHelper.getFireAspectModifier(this);
				if (j > 0) {
					player.setFire(j * 4);
				}
				EnchantmentHelper.func_151384_a(player, this);
				EnchantmentHelper.func_151385_b(this, player);
				this.applySecondaryEffects(player);
			}
		}
	}

	@Override
	public void onUpdate() {
		super.onUpdate();
		if (worldObj.difficultySetting == EnumDifficulty.PEACEFUL) {
			if (!worldObj.isRemote) {
				this.setDead();
			}
		} else if (!swarmSpawned && !worldObj.isRemote) {
			swarmSpawned = true;
			if (rand.nextFloat() < Config.getKeeseSwarmChance()) {
				int n = Config.getKeeseSwarmSize() - rand.nextInt(Config.getKeeseSwarmSize());
				for (int i = 0; i < n; ++i) {
					EntityKeese k = this.createInstance();
					double x = this.posX + rand.nextFloat() * 2.0F;
					double z = this.posZ + rand.nextFloat() * 2.0F;
					k.setPosition(x, this.posY, z);
					k.swarmSpawned = true;
					worldObj.spawnEntityInWorld(k);
				}
			}
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
			}
		}
	}

	@Override
	public boolean attackEntityFrom(DamageSource source, float amount) {
		if (this.isEntityInvulnerable()) {
			return false;
		}
		if (!this.worldObj.isRemote && this.getIsBatHanging()) {
			this.setIsBatHanging(false);
		}
		return super.attackEntityFrom(source, amount);
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound compound) {
		super.readEntityFromNBT(compound);
		swarmSpawned = compound.getBoolean("SpawnedSwarm");
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound compound) {
		super.writeEntityToNBT(compound);
		compound.setBoolean("SpawnedSwarm", swarmSpawned);
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
