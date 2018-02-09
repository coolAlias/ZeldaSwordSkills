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

import java.util.Iterator;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAITasks.EntityAITaskEntry;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.BlockPos;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import zeldaswordskills.api.block.IWhipBlock.WhipType;
import zeldaswordskills.api.entity.IEntityLootable;
import zeldaswordskills.entity.ai.EntityAIPerch;
import zeldaswordskills.entity.ai.EntityAISeekPerch;
import zeldaswordskills.entity.ai.IWallPerch;
import zeldaswordskills.ref.Config;

/**
 * 
 * Skulltulas initially wait in ambush high on trees, lowering down to attack enemies from above.
 *
 */
public class EntitySkulltula extends EntitySpider implements IEntityLootable, IWallPerch
{
	/** Data watcher index tracking this entity's 'perched' status */
	private static final int PERCHED_INDEX = 17;

	public EntitySkulltula(World world) {
		super(world);
		Iterator<EntityAITaskEntry> iterator = tasks.taskEntries.iterator();
		while (iterator.hasNext()) {
			if (iterator.next().action instanceof EntityAIWander) {
				iterator.remove();
				break;
			}
		}
		this.tasks.addTask(2, new EntityAIPerch(this));
		this.tasks.addTask(5, new EntityAISeekPerch(this, 0.8D));
	}

	@Override
	protected void entityInit() {
		super.entityInit();
		dataWatcher.addObject(PERCHED_INDEX, (byte) 0);
	}

	@Override
	public boolean isPerched() {
		return (dataWatcher.getWatchableObjectByte(PERCHED_INDEX) != 0);
	}

	@Override
	public boolean canPerch() {
		BlockPos pos = new BlockPos(this);
		for (EnumFacing facing : EnumFacing.HORIZONTALS) {
			if (worldObj.isSideSolid(pos.offset(facing), facing.getOpposite())) {
				return distanceToGround() > 2;
			}
		}
		return false;
	}

	@Override
	public void setPerched(boolean isPerched) {
		dataWatcher.updateObject(PERCHED_INDEX, (isPerched ? (byte) 1 : (byte) 0));
	}

	private int distanceToGround() {
		int i = 0;
		BlockPos pos = new BlockPos(this).down();
		while (!worldObj.isSideSolid(pos, EnumFacing.UP) && pos.getY() > 5) {
			pos = pos.down();
			++i;
		}
		return i;
	}

	@Override
	public float getLootableChance(EntityPlayer player, WhipType whip) {
		return 0.2F;
	}

	@Override
	public ItemStack getEntityLoot(EntityPlayer player, WhipType whip) {
		return new ItemStack(Items.emerald);
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
	public int getMaxFallHeight() {
		return 10;
	}

	@Override
	public void fall(float distance, float damageMultiplier) {}

	@Override
	public int getTotalArmorValue() {
		return super.getTotalArmorValue() + 4;
	}

	@Override
	protected void damageEntity(DamageSource source, float amount) {
		super.damageEntity(source, amount);
		setPerched(false);
	}

	@Override
	public void onLivingUpdate() {
		super.onLivingUpdate();
		if (worldObj.isRemote) {
			return;
		}
		if (this.isPerched()) {
			if (this.hurtResistantTime > 0) {
				this.setPerched(false);
			} else if (this.getAttackTarget() != null || this.getAITarget() != null) {
				EntityLivingBase target = (this.getAttackTarget() == null ? this.getAITarget() : this.getAttackTarget());
				// Distance check ignoring differences on Y axis to 'leap' at current target
				BlockPos pos = new BlockPos(target.getPosition().getX(), this.getPosition().getY(), target.getPosition().getZ());
				if (this.getDistanceSq(pos) < 10.0D) {
					this.setPerched(false);
				}
			}
			// If still perched, try to move upward
			if (this.isPerched()) {
				boolean moveUp = (this.shouldMoveUpWhilePerched() && (this.distanceToGround() < 3 || this.rand.nextFloat() < 0.1F));
				this.motionY = (moveUp ? 0.1D : 0.0D);
			}
		}
	}

	/**
	 * Returns true if the Skulltula could potentially move further up the wall while perched 
	 */
	protected boolean shouldMoveUpWhilePerched() {
		BlockPos up = new BlockPos(this).up();
		boolean hasFace = false;
		for (EnumFacing facing : EnumFacing.HORIZONTALS) {
			if (this.worldObj.isSideSolid(up.offset(facing), facing.getOpposite())) {
				hasFace = true;
				break;
			}
		}
		return (hasFace && !this.worldObj.isSideSolid(up, EnumFacing.DOWN));
	}

	@Override
	public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, IEntityLivingData data) {
		// Avoid calling super due to possibility of spawning skeleton (so instead, copy most of EntitySpider's code)
		getEntityAttribute(SharedMonsterAttributes.followRange).applyModifier(new AttributeModifier("Random spawn bonus", rand.nextGaussian() * 0.05D, 1));
		if (data == null) {
			data = new EntitySpider.GroupData();
			if (worldObj.getDifficulty() == EnumDifficulty.HARD && worldObj.rand.nextFloat() < 0.1F * difficulty.getClampedAdditionalDifficulty()) {
				((EntitySpider.GroupData) data).func_111104_a(worldObj.rand);
			}
		}
		if (data instanceof EntitySpider.GroupData) {
			int i = ((EntitySpider.GroupData) data).potionEffectId;
			if (i > 0 && Potion.potionTypes[i] != null) {
				this.addPotionEffect(new PotionEffect(i, Integer.MAX_VALUE));
			}
		}
		return data;
	}

	@Override
	public float getBlockPathWeight(BlockPos pos) {
		float f = 0.0F;
		boolean has_face = false;
		for (EnumFacing facing : EnumFacing.HORIZONTALS) {
			if (worldObj.isSideSolid(pos.offset(facing), facing.getOpposite())) {
				f += (has_face ? -0.1F : 0.1F);
				has_face = true;
			} else if (has_face) {
				f += 0.1F;
			}
		}
		if (!has_face) {
			return super.getBlockPathWeight(pos);
		}
		for (int i = 1; i < 3; ++i) {
			if (worldObj.isSideSolid(pos.down(i), EnumFacing.UP)) {
				return f;
			} else if (worldObj.getBlockState(pos.down(i)).getBlock().getMaterial().isLiquid()) {
				return super.getBlockPathWeight(pos);
			}
			f += (0.1F * i);
		}
		return f;
	}

	@Override
	public void writeToNBT(NBTTagCompound compound) {
		super.writeToNBT(compound);
		compound.setBoolean("IsPerched", isPerched());
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		setPerched(compound.getBoolean("IsPerched"));
	}
}
