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

package zeldaswordskills.entity.npc;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.DirtyEntityAccessor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackOnCollide;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.village.Village;
import net.minecraft.world.World;
import zeldaswordskills.api.damage.DamageUtils;
import zeldaswordskills.api.entity.IParryModifier;
import zeldaswordskills.entity.ZSSPlayerSkills;
import zeldaswordskills.item.ItemTreasure;
import zeldaswordskills.item.ItemTreasure.Treasures;
import zeldaswordskills.lib.Sounds;
import zeldaswordskills.skills.ICombo;
import zeldaswordskills.skills.SkillBase;
import zeldaswordskills.util.PlayerUtils;

/**
 * 
 * Orca will teach Link some special sword skills in exchange for Knight's Crests.
 * 
 * Spawned by naming any villager 'Orca' and interacting while holding a Knight's Crest.
 *
 */
public class EntityNpcOrca extends EntityNpcBase implements IParryModifier
{
	/** Datawatcher index to track with whom Orca is in a match (so player interaction can be prevented on client) */
	private static final int MATCH_PLAYER_ID = 18;

	/** Amount of time that must pass before Orca will spar again */
	private static final int MATCH_INTERVAL = 3000;

	/** Time at which Orca will next be ready for a sparring match */
	private long nextMatch;

	/** Timer to prevent chat from being spammed too often */
	private int chatTimer;

	/** Counter to limit number of times combat can be extended; reset to 0 each time player attacks */
	private int hitCounter;

	/** Set to true when player has parried a blow */
	private boolean parryFlag;

	public EntityNpcOrca(World world) {
		super(world);
		tasks.addTask(2, new EntityAIAttackOnCollide(this, EntityPlayer.class, 0.6D, true));
	}

	@Override
	protected void entityInit() {
		super.entityInit();
		dataWatcher.addObject(MATCH_PLAYER_ID, 0);
	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		getEntityAttribute(SharedMonsterAttributes.maxHealth).setAttribute(50.0D);
	}

	/** Whether Orca is currently in a match */
	public Entity getMatchOpponent() {
		return worldObj.getEntityByID(dataWatcher.getWatchableObjectInt(MATCH_PLAYER_ID));
	}

	private void setMatchOpponent(Entity entity) {
		dataWatcher.updateObject(MATCH_PLAYER_ID, (entity == null ? -1 : entity.entityId));
	}

	@Override
	protected String getNameTagOnSpawn() {
		return "Orca";
	}

	@Override
	protected String getLivingSound() {
		return Sounds.VILLAGER_HAGGLE;
	}

	@Override
	protected String getHurtSound() {
		return Sounds.VILLAGER_HIT;
	}

	@Override
	protected String getDeathSound() {
		return Sounds.VILLAGER_DEATH;
	}

	@Override
	public boolean isEntityInvulnerable() {
		return false; // allow Orca to be attacked for training purposes
	}

	// Orca ignores fall damage so practicing Rising Cut doesn't kill him
	@Override
	public void fall(float distance) {}

	@Override
	public boolean attackEntityFrom(DamageSource source, float damage) {
		if (source.getEntity() instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer) source.getEntity();
			Entity opponent = getMatchOpponent();
			if (worldObj.getWorldTime() < nextMatch) {
				sendTranslatedChat(player, "chat.zss.npc.orca.match.damage.resting", true);
				return false;
			} else if (opponent != null && opponent != player) {
				sendTranslatedChat(player, "chat.zss.npc.orca.match.in_match", true);
				return false;
			} else if (source.getSourceOfDamage() != player && !source.getDamageType().equals(DamageUtils.INDIRECT_SWORD) && !source.getDamageType().equals(DamageUtils.INDIRECT_COMBO)) {
				sendTranslatedChat(player, "chat.zss.npc.orca.match.damage.melee_only", false);
				return false;
			} else if (player.getHealth() < 2.0F) {
				sendTranslatedChat(player, "chat.zss.npc.orca.match.damage.low_health", true);
				return false;
			} else if (getHeldItem() == null) {
				sendTranslatedChat(player, "chat.zss.npc.orca.match.damage.unarmed." + worldObj.rand.nextInt(3), true);
				setCurrentItemOrArmor(0, new ItemStack(Item.swordWood));
				return false;
				// Damage source for armor break not set until damage actually inflicted, so check if skill is active instead
			} else if (!PlayerUtils.isHoldingWeapon(player)) {
				sendTranslatedChat(player, "chat.zss.npc.orca.match.damage.player_no_weapon", true);
				return false;
			} else if (ZSSPlayerSkills.get(player).isSkillActive(SkillBase.armorBreak)) {
				sendTranslatedChat(player, "chat.zss.npc.orca.match.damage.armor_break", false);
			} else if (ZSSPlayerSkills.get(player).isSkillActive(SkillBase.endingBlow)) {
				sendTranslatedChat(player, "chat.zss.npc.orca.match.damage.ending_blow", false);
			} else if (ZSSPlayerSkills.get(player).isSkillActive(SkillBase.risingCut)) {
				sendTranslatedChat(player, "chat.zss.npc.orca.match.damage.rising_cut", false);	
			} else if (source.getDamageType().equals(DamageUtils.INDIRECT_SWORD) || source.getDamageType().equals(DamageUtils.INDIRECT_COMBO)) {
				sendTranslatedChat(player, "chat.zss.npc.orca.match.damage.ranged_sword", false);
			} else if (source.getDamageType().equals(DamageUtils.NON_SWORD)) {
				sendTranslatedChat(player, "chat.zss.npc.orca.match.damage.non_sword", false);
			} else {
				ZSSPlayerSkills skills = ZSSPlayerSkills.get(player);
				if (skills.getTargetingSkill() == null) {
					sendTranslatedChat(player, "chat.zss.npc.orca.match.damage.unskilled." + worldObj.rand.nextInt(3), false);
				} else if (skills.getTargetingSkill().getCurrentTarget() == this) {
					sendTranslatedChat(player, "chat.zss.npc.orca.match.damage.hit." + worldObj.rand.nextInt(4), false);
				} else {
					sendTranslatedChat(player, "chat.zss.npc.orca.match.damage.wrong_target", false);
				}
			}
		}
		return super.attackEntityFrom(source, damage);
	}

	@Override
	protected void damageEntity(DamageSource source, float amount) {
		if (source.getEntity() instanceof EntityPlayer) {
			// Post hurt event with proper damage amount to allow triggering of skills
			amount = DirtyEntityAccessor.getModifiedDamage(this, source, amount);
			net.minecraftforge.common.ForgeHooks.onLivingHurt(this, source, amount);
			hitCounter = 0; // reset consecutive hit counter
		} else {
			super.damageEntity(source, amount);
		}
	}

	@Override
	public boolean attackEntityAsMob(Entity entity) {
		if (entity instanceof EntityPlayer && ((EntityPlayer) entity).getHealth() < 2.0F) {
			setRevengeTarget(null);
			sendTranslatedChat((EntityPlayer) entity, "chat.zss.npc.orca.match.victory." + worldObj.rand.nextInt(4), true);
		} else if (entity.attackEntityFrom(DamageSource.causeMobDamage(this), 1F)) {
			if (entity instanceof EntityPlayer) {
				EntityPlayer player = (EntityPlayer) entity;
				boolean isBlocking = PlayerUtils.isBlocking(player);
				if (!isBlocking && ++hitCounter > 2) { // hit player 3 times, player loses
					setRevengeTarget(null);
					sendTranslatedChat(player, "chat.zss.npc.orca.match.victory." + worldObj.rand.nextInt(4), true);
				} else {
					setRevengeTarget(player);
					if (isBlocking) {
						sendTranslatedChat(player, "chat.zss.npc.orca.match.attack.blocked." + worldObj.rand.nextInt(2), false);
					} else {
						sendTranslatedChat(player, "chat.zss.npc.orca.match.attack." + worldObj.rand.nextInt(4), false);
					}
				}
				// End player combo each time a blow is landed and not blocked, regardless of damage: 
				ICombo combo = ZSSPlayerSkills.get(player).getComboSkill();
				if (!isBlocking && combo != null && combo.isComboInProgress()) {
					combo.getCombo().endCombo(player);
				}
			}
			int knockback = 0;
			if (entity instanceof EntityLivingBase) {
				knockback += EnchantmentHelper.getKnockbackModifier(this, ((EntityLivingBase) entity));
				if (knockback < 1 && entity instanceof EntityPlayer) {
					knockback = 1;
				}
			}
			if (knockback > 0) {
				float f = (float) knockback * 0.5F;
				double dx = -MathHelper.sin(rotationYaw * (float) Math.PI / 180.0F) * f;
				double dz = MathHelper.cos(rotationYaw * (float) Math.PI / 180.0F) * f;
				entity.addVelocity(dx, 0.1D, dz);
				motionX *= 0.6D;
				motionZ *= 0.6D;
			}
			return true;
		} else if (entity instanceof EntityPlayer && ZSSPlayerSkills.get((EntityPlayer) entity).isSkillActive(SkillBase.dodge)) {
			sendTranslatedChat((EntityPlayer) entity, "chat.zss.npc.orca.match.attack.dodged." + worldObj.rand.nextInt(2), false);
		}
		return false;
	}

	@Override
	public void setRevengeTarget(EntityLivingBase entity) {
		EntityLivingBase prevTarget = getAttackTarget();
		Village tmp = villageObj;
		villageObj = null; // prevent player from losing village rep
		super.setRevengeTarget(entity);
		setAttackTarget(entity); // needed for attack AI to work
		attackingPlayer = (entity instanceof EntityPlayer ? (EntityPlayer) entity : null);
		setMatchOpponent(attackingPlayer);
		villageObj = tmp;
		if (entity == null && prevTarget instanceof EntityPlayer) {
			nextMatch = worldObj.getWorldTime() + MATCH_INTERVAL;
			// func_142015_aE() is getRevengeTimer()
			if ((ticksExisted - func_142015_aE()) > 99) { // match timed out
				sendTranslatedChat((EntityPlayer) prevTarget, "chat.zss.npc.orca.match.timeout." + worldObj.rand.nextInt(3), true);
			}
		}
	}

	@Override
	public void onLivingUpdate() {
		updateArmSwingProgress();
		if (chatTimer > 0) {
			--chatTimer;
		}
		if (getAttackTarget() instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer) getAttackTarget();
			ICombo combo = ZSSPlayerSkills.get(player).getComboSkill();
			if (parryFlag) {
				parryFlag = false;
				if (getHeldItem() == null) {
					setRevengeTarget(null);
					sendTranslatedChat(player, "chat.zss.npc.orca.match.defeat.disarmed." + worldObj.rand.nextInt(3), true);
				} else {
					sendTranslatedChat(player, "chat.zss.npc.orca.match.disarm_attempt." + worldObj.rand.nextInt(3), false);
				}
			} else if (combo != null && combo.isComboInProgress() && combo.getCombo().getConsecutiveHits() > 9) {
				setRevengeTarget(null);
				sendTranslatedChat(player, "chat.zss.npc.orca.match.defeat.combo." + worldObj.rand.nextInt(3), true);
			} else if (getHeldItem() == null) { // weapon was probably destroyed via Sword Break
				setRevengeTarget(null);
				sendTranslatedChat(player, "chat.zss.npc.orca.match.defeat.disarmed." + worldObj.rand.nextInt(3), true);
			} else if (recentlyHit < 60 && !PlayerUtils.isHoldingWeapon(player)) {
				setRevengeTarget(null);
				sendTranslatedChat(player, "chat.zss.npc.orca.match.quit." + worldObj.rand.nextInt(4), true);
			} else if (recentlyHit < 10) {
				sendTranslatedChat(player, "chat.zss.npc.orca.match.player_idle." + worldObj.rand.nextInt(3), false);
				recentlyHit = 60; // reset counter so idle chat message doesn't get spammed
			}
		}
		super.onLivingUpdate();
	}

	/**
	 * Method to use for sending chats during combat to prevent player getting spammed with chat
	 */
	private void sendTranslatedChat(EntityPlayer player, String chat, boolean alwaysSend) {
		if (!worldObj.isRemote && (alwaysSend || rand.nextInt(Math.max(1, chatTimer)) == 0)) {
			PlayerUtils.sendTranslatedChat(player, chat);
			chatTimer = worldObj.rand.nextInt(20) + worldObj.rand.nextInt(20) + 20;
		}
	}

	@Override
	public boolean interact(EntityPlayer player) {
		Entity opponent = getMatchOpponent();
		if (opponent != null) {
			if (opponent != player) {
				PlayerUtils.sendTranslatedChat(player, "chat.zss.npc.orca.match.in_match");
			}
			return false;
		}
		if (!player.worldObj.isRemote) {
			ItemStack stack = player.getHeldItem();
			if (ZSSPlayerSkills.get(player).completedCrests()) {
				PlayerUtils.sendTranslatedChat(player, "chat.zss.npc.orca.master." + player.worldObj.rand.nextInt(4));
			} else if (stack != null && stack.getItem() instanceof ItemTreasure && stack.getItemDamage() == Treasures.KNIGHTS_CREST.ordinal()) {
				ZSSPlayerSkills.get(player).giveCrest();
			} else {
				PlayerUtils.sendTranslatedChat(player, "chat.zss.npc.orca.idle." + rand.nextInt(5));
			}
		}
		return true;
	}

	@Override
	public EntityLivingData onSpawnWithEgg(EntityLivingData data) {
		data = super.onSpawnWithEgg(data);
		setCurrentItemOrArmor(0, new ItemStack(Item.swordWood));
		return data;
	}

	@Override
	public float getOffensiveModifier(EntityLivingBase entity, ItemStack stack) {
		parryFlag = true;
		return 0.5F; // impossible to disarm unless player has bonuses other than from Parry skill level
	}

	@Override
	public float getDefensiveModifier(EntityLivingBase entity, ItemStack stack) {
		return 0;
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound compound) {
		super.writeEntityToNBT(compound);
		compound.setLong("nextMatch", nextMatch);
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound compound) {
		super.readEntityFromNBT(compound);
		nextMatch = compound.getLong("nextMatch");
	}
}
