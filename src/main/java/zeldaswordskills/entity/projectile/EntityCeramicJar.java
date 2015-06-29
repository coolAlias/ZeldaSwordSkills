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

package zeldaswordskills.entity.projectile;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.common.ChestGenHooks;
import zeldaswordskills.block.ZSSBlocks;
import zeldaswordskills.lib.Config;
import zeldaswordskills.lib.Sounds;
import zeldaswordskills.util.WorldUtils;
import zeldaswordskills.world.gen.DungeonLootLists;

public class EntityCeramicJar extends EntityThrowable
{
	/** The stack to drop upon impact, if any */
	private ItemStack stack = null;

	public EntityCeramicJar(World world) {
		super(world);
	}

	public EntityCeramicJar(World world, EntityLivingBase entity) {
		super(world, entity);
	}

	public EntityCeramicJar(World world, double x, double y, double z) {
		super(world, x, y, z);
	}

	/**
	 * Sets the stack that will drop upon impact
	 */
	public EntityCeramicJar setStack(ItemStack stack) {
		this.stack = stack.copy();
		return this;
	}

	@Override
	protected float func_70182_d() {
		return 1.0F;
	}

	@Override
	protected float getGravityVelocity() {
		return 0.1F;
	}

	@Override
	protected void onImpact(MovingObjectPosition mop) {
		if (mop.entityHit != null) {
			mop.entityHit.attackEntityFrom(DamageSource.causeThrownDamage(this, getThrower()), 2.0F);
		}
		for (int i = 0; i < 20; ++i) {
			worldObj.spawnParticle("tilecrack_" + ZSSBlocks.ceramicJar.blockID + "_0", posX, posY, posZ, motionX + rand.nextGaussian(), 0.01D, motionZ + rand.nextGaussian());
		}

		WorldUtils.playSoundAtEntity(this, Sounds.BREAK_JAR, 0.4F, 0.5F);

		if (stack == null && rand.nextFloat() < Config.getJarDropChance()) {
			stack = ChestGenHooks.getInfo(DungeonLootLists.JAR_DROPS).getOneItem(rand);
		}
		if (stack != null) {
			WorldUtils.spawnItemWithRandom(worldObj, stack, (int) posX, (int) posY + 1, (int) posZ);
		}
		if (!worldObj.isRemote) {
			setDead();
		}
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound compound) {
		super.writeEntityToNBT(compound);
		if (stack != null) {
			NBTTagCompound item = new NBTTagCompound();
			stack.writeToNBT(item);
			compound.setTag("jarItem", item);
		}
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound compound) {
		super.readEntityFromNBT(compound);
		if (compound.hasKey("jarItem")) {
			stack = ItemStack.loadItemStackFromNBT(compound.getCompoundTag("jarItem"));
		}
	}
}
