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

package zeldaswordskills.entity;

import java.util.Arrays;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackOnCollide;
import net.minecraft.entity.ai.EntityAIFollowGolem;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookAtTradePlayer;
import net.minecraft.entity.ai.EntityAIMoveThroughVillage;
import net.minecraft.entity.ai.EntityAIMoveTowardsRestriction;
import net.minecraft.entity.ai.EntityAIMoveTowardsTarget;
import net.minecraft.entity.ai.EntityAIPlay;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAITradePlayer;
import net.minecraft.entity.ai.EntityAIVillagerMate;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.ai.EntityAIWatchClosest2;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StatCollector;
import net.minecraft.village.Village;
import net.minecraft.world.World;
import zeldaswordskills.api.entity.ISongEntity;
import zeldaswordskills.entity.ai.GenericAIDefendVillage;
import zeldaswordskills.entity.ai.IVillageDefender;
import zeldaswordskills.entity.buff.Buff;
import zeldaswordskills.item.ZSSItems;
import zeldaswordskills.ref.Sounds;
import zeldaswordskills.ref.ZeldaSong;
import zeldaswordskills.util.PlayerUtils;
import zeldaswordskills.util.TimedAddItem;
import zeldaswordskills.util.TimedChatDialogue;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class EntityGoron extends EntityVillager implements IVillageDefender, ISongEntity
{
	/** The Goron's village, since EntityVillager.villageObj cannot be accessed */
	protected Village village;

	/** Flag for handling attack timer during client-side health update */
	private static final Byte ATTACK_FLAG = (byte) 4;

	/** Timer for health regeneration, similar to players when satiated */
	private int regenTimer;

	/** Flag to allow attributes to update to adult status */
	private boolean wasChild;

	public EntityGoron(World world) {
		this(world, 0);
	}

	public EntityGoron(World world, int profession) {
		super(world, profession);
		setSize(1.5F, 2.8F);
		//setCanPickUpLoot(true);
		tasks.taskEntries.clear();
		tasks.addTask(0, new EntityAISwimming(this));
		//tasks.addTask(1, new EntityAIAvoidEntity(this, EntityZombie.class, 8.0F, 0.6D, 0.6D));
		tasks.addTask(1, new EntityAITradePlayer(this));
		tasks.addTask(1, new EntityAILookAtTradePlayer(this));
		tasks.addTask(2, new EntityAIAttackOnCollide(this, EntityPlayer.class, 1.0D, false));
		tasks.addTask(2, new EntityAIAttackOnCollide(this, IMob.class, 1.0D, false));
		tasks.addTask(3, new EntityAIMoveTowardsTarget(this, getEntityAttribute(SharedMonsterAttributes.movementSpeed).getAttributeValue(), 16.0F));
		tasks.addTask(4, new EntityAIMoveThroughVillage(this, 0.6D, true));
		//tasks.addTask(2, new EntityAIMoveIndoors(this));
		//tasks.addTask(3, new EntityAIRestrictOpenDoor(this));
		//tasks.addTask(4, new EntityAIOpenDoor(this, true));
		tasks.addTask(5, new EntityAIMoveTowardsRestriction(this, 1.0D));
		tasks.addTask(6, new EntityAIVillagerMate(this));
		tasks.addTask(7, new EntityAIFollowGolem(this));
		tasks.addTask(8, new EntityAIPlay(this, 0.32D));
		tasks.addTask(9, new EntityAIWatchClosest2(this, EntityPlayer.class, 3.0F, 1.0F));
		tasks.addTask(9, new EntityAIWatchClosest2(this, EntityVillager.class, 5.0F, 0.02F));
		tasks.addTask(9, new EntityAIWander(this, 0.6D));
		tasks.addTask(10, new EntityAIWatchClosest(this, EntityLiving.class, 8.0F));
		targetTasks.addTask(1, new GenericAIDefendVillage(this));
		targetTasks.addTask(2, new EntityAIHurtByTarget(this, true));
		//targetTasks.addTask(3, new EntityAINearestAttackableTarget(this, EntityLiving.class, 0, false, true, IMob.mobSelector));
	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(100.0D);
		getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(0.3D);
		getEntityAttribute(SharedMonsterAttributes.knockbackResistance).setBaseValue(0.75D);
		getAttributeMap().registerAttribute(SharedMonsterAttributes.attackDamage).setBaseValue(2.0D);
		isImmuneToFire = true;
		ZSSEntityInfo.get(this).applyBuff(Buff.RESIST_FIRE, Integer.MAX_VALUE, 100);
	}

	/**
	 * Updates Goron attributes (maxHealth, etc.) as appropriate for growing age;
	 * necessary because NBT is read AFTER applyEntityAttributes(), as well as for
	 * updating after transition from childhood to adulthood
	 * @notes Passes return value of {@link #isChild()} to {@link #updateEntityAttributes(boolean)}
	 */
	private void updateEntityAttributes() {
		updateEntityAttributes(isChild());
	}

	/**
	 * As {@link #updateEntityAttributes} with parameter to force child values for use
	 * when spawning child entities, as their growing age is not yet set.
	 * @param isChild Whether the entity is a child; wasChild is set to this value
	 */
	private void updateEntityAttributes(boolean isChild) {
		getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue((isChild ? 40.0D : 100.0D));
		getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue((isChild ? 0.5D : 0.3D));
		getEntityAttribute(SharedMonsterAttributes.knockbackResistance).setBaseValue((isChild ? 0.25D : 0.75D));
		getEntityAttribute(SharedMonsterAttributes.attackDamage).setBaseValue((isChild ? 2.0D : 8.0D));
		wasChild = isChild;
	}

	@Override
	public void onLivingUpdate() {
		updateArmSwingProgress();
		super.onLivingUpdate();
		if (getAITarget() instanceof EntityPlayer || getAttackTarget() instanceof EntityPlayer) {
			// func_142015_aE() returns the revenge timer:
			if (ticksExisted > func_142015_aE() + 30 || recentlyHit < 70) {
				setRevengeTarget(null);
				setAttackTarget(null);
				attackingPlayer = null;
			}
		}
		if (!worldObj.isRemote) {
			updateHealth();
		}
	}

	@Override
	public void updateAITick() {
		super.updateAITick();
		if (!hasHome()) {
			village = null;
		} else if (village == null) {
			ChunkCoordinates cc = getHomePosition();
			if (cc != null) {
				village = worldObj.villageCollectionObj.findNearestVillage(cc.posX, cc.posY, cc.posZ, 32);
			}
		}
	}

	/**
	 * Checks if Goron should regenerate some health
	 */
	protected void updateHealth() {
		if (getHealth() < getMaxHealth()) {
			if (++regenTimer > 399) {
				heal(1.0F);
				regenTimer = 0;
			}
		}
		if (wasChild && !isChild()) {
			updateEntityAttributes();
		}
	}

	@Override
	public boolean attackEntityFrom(DamageSource source, float amount) {
		if (isEntityInvulnerable()) {
			return false;
		} else if (super.attackEntityFrom(source, amount)) {
			Entity entity = source.getEntity();
			if (riddenByEntity != entity && ridingEntity != entity) {
				if (entity != this) {
					entityToAttack = entity;
				}
				return true;
			} else {
				return true;
			}
		} else {
			return false;
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void handleHealthUpdate(byte flag) {
		if (flag == ATTACK_FLAG) {
			// matches golem's value for rendering; not the same as value on server
			attackTime = 10;
		} else {
			super.handleHealthUpdate(flag);
		}
	}

	@Override
	public boolean attackEntityAsMob(Entity entity) {
		if (attackTime > 0) {
			return false;
		}
		float amount = (float) getEntityAttribute(SharedMonsterAttributes.attackDamage).getAttributeValue();
		int knockback = 0;
		attackTime = 20; // set to 20 in EntityMob#attackEntity, but seems to be unnecessary due to AI
		worldObj.setEntityState(this, ATTACK_FLAG);

		if (entity instanceof EntityLivingBase) {
			amount += EnchantmentHelper.getEnchantmentModifierLiving(this, (EntityLivingBase) entity);
			knockback += EnchantmentHelper.getKnockbackModifier(this, (EntityLivingBase) entity);
		}

		boolean flag = entity.attackEntityFrom(DamageSource.causeMobDamage(this), amount);
		if (flag) {
			if (knockback > 0) {
				float f = (float) knockback * 0.5F;
				double dx = -MathHelper.sin(rotationYaw * (float) Math.PI / 180.0F) * f;
				double dz = MathHelper.cos(rotationYaw * (float) Math.PI / 180.0F) * f;
				entity.addVelocity(dx, 0.1D, dz);
				motionX *= 0.6D;
				motionZ *= 0.6D;
			}

			int fire = EnchantmentHelper.getFireAspectModifier(this);
			if (fire > 0) {
				entity.setFire(fire * 4);
			}

			if (entity instanceof EntityLivingBase) {
				EnchantmentHelper.func_151384_a((EntityLivingBase) entity, this);
			}
			EnchantmentHelper.func_151385_b(this, entity);
		}

		return flag;
	}

	@Override
	public boolean canBreatheUnderwater() {
		return true;
	}

	@Override
	protected void collideWithEntity(Entity entity) {
		if (entity instanceof IMob && canAttackClass(entity.getClass()) && getRNG().nextInt(20) == 0) {
			setAttackTarget((EntityLivingBase) entity);
		}
		super.collideWithEntity(entity);
	}

	@Override
	public int getTotalArmorValue() {
		return super.getTotalArmorValue() + (isChild() ? 4 : 10);
	}

	// TODO update sounds to Goron-specific sounds
	@Override
	protected String getLivingSound() {
		return isTrading() ? Sounds.VILLAGER_HAGGLE : Sounds.VILLAGER_IDLE;
	}

	@Override
	protected String getHurtSound() {
		return Sounds.VILLAGER_HIT;
	}

	@Override
	protected String getDeathSound() {
		return Sounds.VILLAGER_DEATH;
	}

	/**
	 * Creates a child entity for {@link EntityAgeable#createChild(EntityAgeable)}
	 * @return class-specific instance, rather than generic EntityAgeable
	 */
	@Override
	public EntityGoron createChild(EntityAgeable entity) {
		EntityGoron goron = new EntityGoron(worldObj);
		goron.onSpawnWithEgg(null);
		goron.updateEntityAttributes(true);
		return goron;
	}

	@Override
	public Village getVillageToDefend() {
		return village;
	}

	@Override
	public boolean onSongPlayed(EntityPlayer player, ZeldaSong song, int power, int affected) {
		if (song == ZeldaSong.SARIAS_SONG) {
			playLivingSound();
			if (("Darunia").equals(getCustomNameTag())) {
				if (ZSSPlayerSongs.get(player).hasCuredNpc("Darunia")) {
					PlayerUtils.sendChat(player, StatCollector.translateToLocal("chat.zss.song.saria.darunia.thanks"));
				} else if (power < 5) {
					PlayerUtils.sendChat(player, StatCollector.translateToLocal("chat.zss.song.saria.darunia.weak"));
				} else if (ZSSPlayerSongs.get(player).onCuredNpc("Darunia")) {
					ItemStack gift = new ItemStack(ZSSItems.gauntletsSilver);
					new TimedChatDialogue(player, Arrays.asList(
							StatCollector.translateToLocal("chat.zss.song.saria.darunia.0"),
							StatCollector.translateToLocal("chat.zss.song.saria.darunia.1"),
							StatCollector.translateToLocalFormatted("chat.zss.song.saria.darunia.2", gift.getDisplayName())), 0, 1500);
					new TimedAddItem(player, gift, 3000, Sounds.SUCCESS);
				} else {
					PlayerUtils.sendChat(player, StatCollector.translateToLocal("chat.zss.song.saria.darunia.thanks"));
				}
			} else if (affected == 0) {
				PlayerUtils.sendChat(player, StatCollector.translateToLocal("chat.zss.song.saria.goron"));
			}
			return true;
		}
		return false;
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound compound) {
		super.readEntityFromNBT(compound);
		updateEntityAttributes();
	}

	/**
	 * Attempts to spawn a goron on the coat-tails of a villager joining the world
	 * Sets the 'zssFirstJoinFlag' in the villager's NBT data to prevent processing
	 * again when the game is restarted.
	 * @param villager The villager currently spawning
	 * @result Depending on the current ration of villagers to gorons, a goron will be spawned near the villager
	 */
	public static void doVillagerSpawn(EntityVillager villager, World world) {
		NBTTagCompound compound = villager.getEntityData();
		if (!compound.hasKey("zssFirstJoinFlag")) {
			compound.setBoolean("zssFirstJoinFlag", true);
			int ratio = ZSSEntities.getGoronRatio();
			if (ratio > 0 && world.rand.nextInt(ratio) == 0) {
				int x = MathHelper.floor_double(villager.posX);
				int y = MathHelper.floor_double(villager.posY);
				int z = MathHelper.floor_double(villager.posZ);
				try {
					if (world.villageCollectionObj.getVillageList().isEmpty() || world.villageCollectionObj.findNearestVillage(x, y, z, 32) != null) {
						EntityGoron goron = new EntityGoron(world, world.rand.nextInt(5));
						double posX = villager.posX + world.rand.nextInt(8) - 4;
						double posZ = villager.posZ + world.rand.nextInt(8) - 4;
						goron.setLocationAndAngles(posX, villager.posY + 1, posZ, villager.rotationYaw, villager.rotationPitch);
						if (goron.getCanSpawnHere()) {
							world.spawnEntityInWorld(goron);
						}
					}
				} catch (NullPointerException e) {
					; // catches null pointer from block not found during world load (super-flat only?)
				}
			}
		}
	}
}
