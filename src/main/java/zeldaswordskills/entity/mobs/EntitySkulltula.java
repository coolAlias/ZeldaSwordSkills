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
import java.util.Iterator;
import java.util.List;

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
import zeldaswordskills.entity.IEntityVariant;
import zeldaswordskills.entity.ai.EntityAIPerch;
import zeldaswordskills.entity.ai.EntityAISeekPerch;
import zeldaswordskills.entity.ai.IWallPerch;
import zeldaswordskills.item.ZSSItems;
import zeldaswordskills.ref.Config;
import zeldaswordskills.util.BiomeType;

/**
 * 
 * Skulltulas initially wait in ambush high on trees, lowering down to attack enemies from above.
 *
 */
public class EntitySkulltula extends EntitySpider implements IEntityLootable, IEntityVariant, IWallPerch
{
	/**
	 * Returns array of default biomes in which this entity may spawn naturally
	 */
	public static String[] getDefaultBiomes() {
		List<String> biomes = new ArrayList<String>();
		biomes.addAll(Arrays.asList(BiomeType.FOREST.defaultBiomes));
		biomes.addAll(Arrays.asList(BiomeType.JUNGLE.defaultBiomes));
		biomes.addAll(Arrays.asList(BiomeType.TAIGA.defaultBiomes));
		return biomes.toArray(new String[biomes.size()]);
	}

	/** Data watcher index for this entity's variant (type) */
	private static final int TYPE_INDEX = 17;
	/** Data watcher index tracking this entity's 'perched' status */
	private static final int PERCHED_INDEX = 18;

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
		dataWatcher.addObject(TYPE_INDEX, (byte) 0);
		dataWatcher.addObject(PERCHED_INDEX, (byte) 0);
	}

	@Override
	public IEntityVariant setType(int type) {
		dataWatcher.updateObject(TYPE_INDEX, (type > 0 ? (byte) 1 : (byte) 0));
		return this;
	}

	/**
	 * Returns true if this is a Golden Skulltula
	 */
	public boolean isGolden() {
		return (dataWatcher.getWatchableObjectByte(TYPE_INDEX) != 0);
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
		return new ItemStack(isGolden() ? ZSSItems.skulltulaToken : Items.emerald);
	}

	@Override
	public boolean onLootStolen(EntityPlayer player, boolean wasItemStolen) {
		return wasItemStolen || !isGolden();
	}

	@Override
	public boolean isHurtOnTheft(EntityPlayer player, WhipType whip) {
		return Config.getHurtOnSteal();
	}

	@Override
	protected void dropFewItems(boolean recentlyHit, int lootingLevel) {
		super.dropFewItems(recentlyHit, lootingLevel);
		if (isGolden()) {
			entityDropItem(new ItemStack(ZSSItems.skulltulaToken), 0.0F);
		}
	}

	@Override
	public int getMaxFallHeight() {
		return 10;
	}

	@Override
	public void fall(float distance, float damageMultiplier) {}

	@Override
	public int getTotalArmorValue() {
		return super.getTotalArmorValue() + (isGolden() ? 6 : 4);
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
		if (hurtResistantTime == 0 && (getAttackTarget() == null || getAITarget() == null)) {
			if (isPerched() && getBrightness(1.0F) < 0.5F) {
				motionY = (worldObj.isSideSolid(new BlockPos(this).up(), EnumFacing.DOWN) || distanceToGround() > 3 ? 0.0D : 0.1D);
				EntityPlayer target = worldObj.getClosestPlayerToEntity(this, 8.0D);
				if (target != null && !((EntityPlayer) target).capabilities.disableDamage) {
					setAttackTarget(target);
					setPerched(false);
				}
			}
		} else {
			setPerched(false);
		}
	}

	@Override
	public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, IEntityLivingData data) {
		// Avoid calling super due to possibility of spawning skeleton (so instead, copy most of EntitySpider's code)
		getEntityAttribute(SharedMonsterAttributes.followRange).applyModifier(new AttributeModifier("Random spawn bonus", rand.nextGaussian() * 0.05D, 1));
		if (worldObj.rand.nextInt(100) == 0) {
			setType(1); // Golden Skulltula
		}
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
		compound.setByte("SkulltulaType", dataWatcher.getWatchableObjectByte(TYPE_INDEX));
		compound.setBoolean("IsPerched", isPerched());
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		dataWatcher.updateObject(TYPE_INDEX, compound.getByte("SkulltulaType"));
		setPerched(compound.getBoolean("IsPerched"));
	}
}
