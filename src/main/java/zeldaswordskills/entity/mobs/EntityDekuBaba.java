/**
    Copyright (C) <2016> <coolAlias>

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import zeldaswordskills.api.damage.EnumDamageType;
import zeldaswordskills.api.damage.IPostDamageEffect;
import zeldaswordskills.api.entity.IEntityBombEater;
import zeldaswordskills.api.entity.IEntityBombIngestible;
import zeldaswordskills.api.entity.IEntityCustomTarget;
import zeldaswordskills.api.entity.ai.EntityAIDynamicAction;
import zeldaswordskills.api.entity.ai.EntityAIDynamicProne;
import zeldaswordskills.api.entity.ai.EntityAITargetBombs;
import zeldaswordskills.api.entity.ai.EntityAction;
import zeldaswordskills.api.entity.ai.IEntityDynamic;
import zeldaswordskills.api.entity.ai.IEntityDynamicAI;
import zeldaswordskills.entity.ZSSEntityInfo;
import zeldaswordskills.item.ZSSItems;
import zeldaswordskills.ref.Sounds;
import zeldaswordskills.util.BiomeType;
import zeldaswordskills.util.PlayerUtils;
import zeldaswordskills.util.TargetUtils;

public class EntityDekuBaba extends EntityDekuBase implements IEntityBombEater, IEntityDynamic, IEntityCustomTarget
{
	/**
	 * Returns array of default biomes in which this entity may spawn naturally
	 */
	public static String[] getDefaultBiomes() {
		return BiomeType.getBiomeArray(null, BiomeType.FOREST, BiomeType.JUNGLE, BiomeType.PLAINS, BiomeType.RIVER);
	}

	/** Health update flag signaling that a bomb was ingested */
	public static final byte BOMB_INGESTED = EntityDekuBase.flag_index++;
	public static final EntityAction ACTION_SPROUT = new EntityAction(EntityDekuBase.flag_index++, 11, 5);
	public static final EntityAction ACTION_READY = new EntityAction(EntityDekuBase.flag_index++, 0, 5);
	public static final EntityAction ACTION_ATTACK = new EntityAction(EntityDekuBase.flag_index++, 16, 7);
	public static final EntityAction ACTION_BOMB = new EntityAction(EntityDekuBase.flag_index++, ACTION_ATTACK.duration, ACTION_ATTACK.action_frame);
	public static final EntityAction ACTION_PRONE = new EntityAction(EntityDekuBase.flag_index++, 60, 0);
	private static final Map<Integer, EntityAction> actionMap = new HashMap<Integer, EntityAction>();
	protected static void registerAction(EntityAction action) {
		actionMap.put(action.id, action);
	}
	static {
		registerAction(ACTION_SPROUT);
		registerAction(ACTION_READY);
		registerAction(ACTION_ATTACK);
		registerAction(ACTION_BOMB);
		registerAction(ACTION_PRONE);
	}

	/** DataWatcher index for alertness level */
	public static final int ALERTNESS_INDEX = 17;

	/** Datawatcher index for current target entity's ID */
	public static final int TARGET_INDEX = 18;

	/** Datawatcher index for current custom target entity's ID */
	public static final int CUSTOM_TARGET_INDEX = 19;

	/** Datawatcher index for current difficulty ID since client doesn't normally know about that */
	public static final int DIFFICULTY_INDEX = 20;

	/** Datawatcher index for custom state, e.g. confused */
	public static final int STATUS_INDEX = 21;

	/** Value for 'confused' status */
	public static final int STATUS_BOMB = 1;

	/** Value for 'confused' status */
	public static final int STATUS_CONFUSED = 2;

	/** Maximum alertness level */
	public static final int MAX_ALERTNESS = 50;

	/** Time it takes for any ingested bomb to explode */
	public static final int FUSE_TIME = 20;

	/** Current action should never be null and should always be the same on both client and server */
	protected EntityAction action = ACTION_READY;

	protected final List<EntityAction> actionList = new ArrayList<EntityAction>();

	/** Current action timer */
	protected int action_timer;

	/** Set to true if struck while prone */
	protected boolean prone;

	/** Ingested bomb 'timer' used for flashing effect */
	protected int status_timer;

	/** Possibly non-living target entity */
	protected Entity target;

	public EntityDekuBaba(World world) {
		super(world);
		actionList.add(action); // add base action so can use #set later
	}

	@Override
	protected void addAITasks() {
		this.tasks.addTask(1, new EntityAIDynamicProne(this, ACTION_PRONE, 63));
		this.tasks.addTask(2, new EntityAIDynamicAction(this, ACTION_ATTACK, 4.0F, true));
		this.tasks.addTask(4, new EntityAITargetBombs(this, ACTION_BOMB, 3.0F, true));
		this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, true));
		this.targetTasks.addTask(2, new EntityAINearestAttackableTarget(this, EntityPlayer.class, 0, true, false, null));
	}

	@Override
	public void entityInit() {
		super.entityInit();
		dataWatcher.addObject(ALERTNESS_INDEX, 0);
		dataWatcher.addObject(TARGET_INDEX, -1);
		dataWatcher.addObject(CUSTOM_TARGET_INDEX, -1);
		dataWatcher.addObject(DIFFICULTY_INDEX, worldObj.getDifficulty().getDifficultyId());
		dataWatcher.addObject(STATUS_INDEX, 0);
	}

	@Override
	protected String getLivingSound() {
		return isFullyAlert() ? Sounds.LEAF_RUSTLE : null;
	}

	@Override
	protected float getSoundVolume() {
		return 0.75F + rand.nextFloat() * 0.25F;
	}

	@Override
	protected float getSoundPitch() {
		return 0.5F;
	}

	/**
	 * Alertness level of this Deku Baba; 0 is fully retracted, {@code ACTION_SPROUT#duration} is fully extended and ready to act.
	 * Value may be up to {@value #MAX_ALERTNESS}.
	 */
	protected int getAlertness() {
		return dataWatcher.getWatchableObjectInt(ALERTNESS_INDEX);
	}

	/**
	 * Returns true if the Deku Baba is fully alert, i.e. it can perform actions and take damage
	 */
	public boolean isFullyAlert() {
		return isEntityAlive() && getActionTime(ACTION_SPROUT.id) > ACTION_SPROUT.getDuration(getActionSpeed(ACTION_SPROUT.id));
	}

	private void updateAlertness() {
		if (action == ACTION_PRONE) {
			return;
		}
		int alertness = getAlertness();
		int i = (this.getCurrentTarget() == null ? -1 : 1);
		if (this.getCurrentTarget() != null && !this.canEntityBeSeen(this.getCurrentTarget())) {
			i = (this.rand.nextInt(4) == 0 ? -1 : 0);
		}
		int modified = MathHelper.clamp_int(alertness + i, 0, MAX_ALERTNESS);
		if (modified != alertness) {
			dataWatcher.updateObject(ALERTNESS_INDEX, modified);
		}
	}

	/**
	 * Returns true if this baba is confused
	 */
	public boolean isConfused() {
		return dataWatcher.getWatchableObjectInt(STATUS_INDEX) == STATUS_CONFUSED;
	}

	/**
	 * Sets the confused state of this baba
	 */
	public void setConfused(boolean confused) {
		dataWatcher.updateObject(STATUS_INDEX, confused ? STATUS_CONFUSED : 0);
		status_timer = (confused && status_timer == 0 ? rand.nextInt(80) + rand.nextInt(80) + 142 : 0);
	}

	/**
	 * Whether the action ID is considered an 'attack'
	 */
	public boolean isAttack(int action_id) {
		return action_id == ACTION_ATTACK.id || action_id == ACTION_BOMB.id;
	}

	@Override
	public List<EntityAction> getActiveActions() {
		return actionList;
	}

	@Override
	public int getActionTime(int action_id) {
		if (action_id == ACTION_SPROUT.id) {
			return getAlertness();
		}
		// action timer starts at max and decrements to 0, but needs to be returned as starting at 0 and increment to max
		EntityAction a = getActionById(action_id);
		if (a == action && action_timer > 0) {
			return a.getDuration(getActionSpeed(action_id)) - action_timer;
		}
		return 0;
	}

	@Override
	public float getActionSpeed(int action_id) {
		int i = getDifficultyModifier() - 2;
		if (isAttack(action_id)) {
			return 0.7F + (i * 0.15F);
		} else if (action_id == ACTION_PRONE.id) {
			return 1.0F + (i * 0.25F);
		}
		return 1.0F;
	}

	@Override
	public boolean canExecute(int action_id, IEntityDynamicAI ai) {
		if (isAttack(action_id)) {
			Entity entity = getCurrentTarget();
			return action_timer == 0 && entity != null && canAttack() && TargetUtils.isTargetInFrontOf(this, entity, 15F);
		} else if (action_id == ACTION_PRONE.id) {
			return prone;
		}
		return true;
	}

	@Override
	public void beginAction(int action_id, IEntityDynamicAI ai) {
		if (isAttack(action_id)) {
			this.playLivingSound();
		}
		setActionState(action_id);
	}

	@Override
	public void endAction(int action_id, IEntityDynamicAI ai) {
		if (action_id == ACTION_PRONE.id) {
			prone = false;
		}
		if (action_id == action.id) { 
			setActionState(ACTION_READY.id);
		}
	}

	@Override
	public void performAction(int action_id, IEntityDynamicAI ai) {
		Entity target = getCurrentTarget();
		if (isConfused()) {
			// do nothing
		} else if (action_id == ACTION_ATTACK.id) {
			if (target instanceof EntityLivingBase && canAttack() && TargetUtils.isTargetInFrontOf(this, target, 15F)) {
				attackEntityAsMob(target);
			}
		} else if (action_id == ACTION_BOMB.id) {
			if (target instanceof IEntityBombIngestible && canAttack() && TargetUtils.isTargetInFrontOf(this, target, 15F)) {
				IEntityBombIngestible bomb = (IEntityBombIngestible) getCurrentTarget();
				if (ZSSEntityInfo.get(this).onBombIngested(bomb)) {
					dataWatcher.updateObject(STATUS_INDEX, STATUS_BOMB);
					worldObj.setEntityState(this, BOMB_INGESTED);
					double damage = this.getAttributeMap().getAttributeInstance(SharedMonsterAttributes.attackDamage).getAttributeValue();
					bomb.setExplosionDamage((float) damage * worldObj.getDifficulty().getDifficultyId());
					bomb.setFuseTime(FUSE_TIME);
					ZSSEntityInfo.get(this).refreshFuseTime();
				}
			}
		}
	}

	/** Returns the world difficulty setting ID, between 0 and 3 */
	public int getDifficultyModifier() {
		return dataWatcher.getWatchableObjectInt(DIFFICULTY_INDEX);
	}

	/** Counts up to {@value #FUSE_TIME} after ingesting a bomb */
	public int getBombTimer() {
		return (status_timer > 0 && dataWatcher.getWatchableObjectInt(STATUS_INDEX) == STATUS_BOMB ? FUSE_TIME - status_timer : 0);
	}

	@Override
	public Result ingestBomb(IEntityBombIngestible bomb) {
		return Result.DENY;
	}

	@Override
	public boolean onBombIndigestion(IEntityBombIngestible bomb) {
		prone = true; // allows full damage to be applied and results in receiving Deku Nuts
		return true;
	}

	@Override
	public boolean doesIngestedBombExplode(IEntityBombIngestible bomb) {
		return true;
	}

	@Override
	public boolean isIngestedBombFatal(IEntityBombIngestible bomb) {
		return true;
	}

	@Override
	public Entity getCurrentTarget() {
		// prioritize custom target when available
		Entity entity = getCustomTarget();
		if (entity == null) {
			entity = getAttackTarget();
		}
		return entity;
	}

	@Override
	public Entity getCustomTarget() {
		if (target == null && dataWatcher.getWatchableObjectInt(CUSTOM_TARGET_INDEX) > -1) {
			target = worldObj.getEntityByID(dataWatcher.getWatchableObjectInt(CUSTOM_TARGET_INDEX));
			if (target == null) {
				dataWatcher.updateObject(CUSTOM_TARGET_INDEX, -1);
			}
		}
		return target;
	}

	@Override
	public void setCustomTarget(Entity entity) {
		this.target = entity;
		dataWatcher.updateObject(CUSTOM_TARGET_INDEX, (entity == null ? -1 : entity.getEntityId()));
	}

	@Override
	public EntityLivingBase getAttackTarget() {
		if (super.getAttackTarget() == null && dataWatcher.getWatchableObjectInt(TARGET_INDEX) > -1) {
			Entity target = worldObj.getEntityByID(dataWatcher.getWatchableObjectInt(TARGET_INDEX));
			if (target instanceof EntityLivingBase) {
				setAttackTarget((EntityLivingBase) target);
			} else {
				dataWatcher.updateObject(TARGET_INDEX, -1);
			}
		}
		return super.getAttackTarget();
	}

	@Override
	public void setAttackTarget(EntityLivingBase entity) {
		super.setAttackTarget(entity);
		dataWatcher.updateObject(TARGET_INDEX, (entity == null ? -1 : entity.getEntityId()));
	}

	@Override
	protected boolean canAttack() {
		return isFullyAlert() && action != ACTION_PRONE;
	}

	@Override
	public boolean attackEntityAsMob(Entity entity) {
		boolean blocking = false; // item will no longer be in use after block: store current state
		if (entity instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer) entity;
			if (PlayerUtils.isBlocking(player) && PlayerUtils.isShield(player.getHeldItem())) {
				blocking = true;
			}
		}
		boolean flag = super.attackEntityAsMob(entity);
		if (blocking) {
			prone = true;
		}
		return flag;
	}

	@Override
	public boolean attackEntityFrom(DamageSource source, float amount) {
		if (!isFullyAlert() && source.getSourceOfDamage() != null) {
			return false;
		} else if (isSourceFatal(source)) {
			// only give deku nuts if hit while already prone or attacking
			prone ^= (!isAttack(action.id));
			return super.attackEntityFrom(source, getMaxHealth());
		} else if (prone) {
			if (super.attackEntityFrom(source, getSlashDamage(source, amount))) {
				this.onProneAttack(source, amount);
				return true;
			}
			return false;
		} else if (didAttackCauseProne(source, amount)) {
			return true;
		} else if (isDamageEffective(source)) {
			return super.attackEntityFrom(source, amount);
		}
		return false;
	}

	/**
	 * Called after a successful attack while prone
	 * @param amount The original damage amount, not necessarily the actual amount inflicted
	 */
	protected void onProneAttack(DamageSource source, float amount) {
		if (this.getHealth() > 0.0F) {
			this.setActionState(ACTION_READY.id);
			action_timer = 10; // minor delay before next action
		}
	}

	/**
	 * Returns true if the attack damaged the entity, thereby causing it to become prone
	 */
	protected boolean didAttackCauseProne(DamageSource source, float amount) {
		boolean flag = false;
		// Prevent DoT type effects from causing prone by requiring #getEntity() to be non-null
		if (prone || amount < 0.5F || source.getEntity() == null) {
			return false;
		} else if (source.isExplosion()) {
			// explosions can always cause baba to go prone and cause decent damage
			flag = super.attackEntityFrom(source, Math.max(0.5F, amount * 0.25F));
		} else if (source instanceof IPostDamageEffect && ((IPostDamageEffect) source).getDuration(EnumDamageType.STUN) > 0) {
			// stun attacks do minimal damage, but can always cause baba to go prone
			amount = Math.max(amount * 0.25F, 0.5F);
			flag = super.attackEntityFrom(source, amount);
		} else if (isAttack(action.id) && getActionTime(action.id) > worldObj.getDifficulty().getDifficultyId()) {
			flag = super.attackEntityFrom(source, getSlashDamage(source, amount));
		}
		if (flag && this.getHealth() > 0.0F) { // only set to prone if still alive
			if (!isConfused() && source.getSourceOfDamage() instanceof EntityPlayer && ((EntityPlayer) source.getSourceOfDamage()).getHeldItem() == null) {
				this.setConfused(true);
			} else {
				this.setConfused(false);
				this.prone = true; // flag for prone AI to execute
			}
		}
		return flag;
	}

	/**
	 * Return the amount of damage to inflict upon striking the baba either while it is attacking or while it is prone
	 * @param source Check {@link #isSlashing(DamageSource)} to see if it is a slashing weapon
	 * @param amount The original damage amount
	 */
	protected float getSlashDamage(DamageSource source, float amount) {
		return isSlashing(source) ? (prone ? getMaxHealth() : getMaxHealth() / 2.0F) : amount;
	}

	protected boolean isDamageEffective(DamageSource source) {
		if (source.isFireDamage() || source.isExplosion() || source.isMagicDamage() || source == DamageSource.inWall || source == DamageSource.outOfWorld) {
			return true;
		}
		return false;
	}

	protected boolean isTargetValid(EntityLivingBase entity) {
		if (!entity.isEntityAlive()) {
			return false;
		} else if (entity instanceof EntityPlayer && ((EntityPlayer) entity).capabilities.disableDamage) {
			return false;
		}
		return entity.getDistanceSqToEntity(this) < 100.0D;
	}

	@Override
	public void onUpdate() {
		super.onUpdate();
		if (!this.isEntityAlive()) {
			return; // reduces indentation below
		}
		// Set targets to null on both server and client, otherwise model freaks out in Creative
		if (this.target != null && !this.target.isEntityAlive()) {
			this.setCustomTarget(null);
		}
		if (this.getAttackTarget() != null && !isTargetValid(getAttackTarget())) {
			this.setAttackTarget(null);
		}
		updateAlertness();
		if (!worldObj.isRemote) {
			if (worldObj.getDifficulty().getDifficultyId() != getDifficultyModifier()) {
				dataWatcher.updateObject(DIFFICULTY_INDEX, worldObj.getDifficulty().getDifficultyId());
			}
		} else if (isConfused() && ticksExisted % 10 == 0) {
			Vec3 look = this.getLookVec();
			for (int i = 0; i < 1; ++i) {
				worldObj.spawnParticle(EnumParticleTypes.SPELL,
						posX - look.xCoord * 0.5D + 0.1D * rand.nextGaussian(),
						this.getEntityBoundingBox().maxY + 0.25D + 0.15D * rand.nextGaussian(),
						posZ - look.zCoord * 0.5D + 0.1D * rand.nextGaussian(),
						0.0D, 0.0D, 0.0D);
			}
		}
		if (status_timer > 0) {
			--status_timer;
			if (status_timer == 0 && !worldObj.isRemote) {
				dataWatcher.updateObject(STATUS_INDEX, 0);
			}
		}
		if (action_timer > 0 && isEntityAlive()) {
			--action_timer;
			if (action_timer == 0) {
				setActionState(ACTION_READY.id);
			}
		}
		if (getActionTime(ACTION_SPROUT.id) > ACTION_SPROUT.getActionFrame(1.0F)) {
			Entity target = this.getCurrentTarget();
			if (target != null && action != ACTION_PRONE) {
				this.getLookHelper().setLookPositionWithEntity(target, 30.0F, 30.0F);
				// Manually update rotation yaw since the Deku Baba does not move
				double dx = target.posX - this.posX;
				double dz = target.posZ - this.posZ;
				double dy = target.posY - MathHelper.floor_double(this.getEntityBoundingBox().minY + 0.5D);
				double d = dx * dx + dy * dy + dz * dz;
				if (d >= 2.500000277905201E-7D) {
					float f = (float)(Math.atan2(dz, dx) * 180.0D / Math.PI) - 90.0F;
					this.rotationYaw = this.limitAngle(this.rotationYaw, f, 30.0F);
				}
			}
		}
	}

	@Override
	protected byte getCustomDeathFlag(DamageSource source) {
		if (prone || isSourceFatal(source)) {
			return super.getCustomDeathFlag(source);
		}
		return 0;
	}

	private float limitAngle(float angle, float target, float max) {
		target = MathHelper.clamp_float(MathHelper.wrapAngleTo180_float(target - angle), -max, max);
		return angle + target;
	}

	/**
	 * Return an EntityAction based on an integer ID; may return null
	 */
	public EntityAction getActionById(int id) {
		return EntityDekuBaba.actionMap.get(id);
	}

	/**
	 * Sets current action based on the given ID, return false if the ID is not a valid action
	 */
	protected boolean setActionState(int id) {
		EntityAction a = getActionById(id);
		if (a != null) {
			action = a;
			prone = (action == ACTION_PRONE);
			action_timer = action.getDuration(getActionSpeed(action.id));
			if (!worldObj.isRemote) {
				worldObj.setEntityState(this, (byte) action.id);
			}
			actionList.set(0, action);
			return true;
		}
		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void handleHealthUpdate(byte flag) {
		if (flag == BOMB_INGESTED) {
			status_timer = FUSE_TIME;
		} else if (!setActionState(flag)) {
			super.handleHealthUpdate(flag);
		}
	}

	@Override
	protected Item getDropItem() {
		return (prone ? Items.stick : ZSSItems.dekuNut);
	}

	@Override
	public void writeToNBT(NBTTagCompound compound) {
		super.writeToNBT(compound);
		compound.setInteger("statusIndex", dataWatcher.getWatchableObjectInt(STATUS_INDEX));
		compound.setInteger("statusTimer", status_timer);
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		dataWatcher.updateObject(STATUS_INDEX, compound.getInteger("statusIndex"));
		status_timer = compound.getInteger("statusTimer");
	}
}
