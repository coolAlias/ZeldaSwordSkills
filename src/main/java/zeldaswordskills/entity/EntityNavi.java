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

import java.util.UUID;

import net.minecraft.entity.Entity;
import net.minecraft.entity.IEntityOwnable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import zeldaswordskills.block.IDungeonBlock;
import zeldaswordskills.ref.Config;
import zeldaswordskills.ref.Sounds;
import zeldaswordskills.util.PlayerUtils;

public class EntityNavi extends EntityFairy implements IEntityOwnable
{
	/** Datawatcher index for owner's UUID */
	private static final int OWNER_INDEX = 17;

	private EntityPlayer owner;

	public EntityNavi(World world) {
		super(world);
		func_110163_bv(); // sets persistence required to true, meaning will not despawn
	}

	@Override
	protected void entityInit() {
		super.entityInit();
		dataWatcher.addObject(OWNER_INDEX, "");
	}

	@Override
	public void setFairyHome(double x, double y, double z) {
		super.setFairyHome(x, y, z);
		home = null;
	}

	@Override
	public boolean canDespawn() {
		return false;
	}

	@Override
	protected void collideWithEntity(Entity entity) {}

	@Override
	public void onUpdate() {
		super.onUpdate();
		if (!worldObj.isRemote) {
			if (getOwner() == null && ticksExisted > 10) {
				setDead();
			} else if (Config.getNaviRange() > 0 && ticksExisted % 50 == 49) {
				detectDungeons();
			}
		}
	}

	@Override
	protected void updateAITasks() {
		if (getOwner() != null && (ticksExisted % 30) == 0) {
			Vec3 look = owner.getLookVec();
			int x = MathHelper.floor_double(owner.posX - look.xCoord);
			int y = MathHelper.floor_double(owner.posY + owner.getEyeHeight());
			int z = MathHelper.floor_double(owner.posZ - look.zCoord);
			home = new int[]{x, y + 1, z};
			if (owner.getDistanceSqToEntity(this) > 144.0D) {
				setLocationAndAngles(owner.posX + look.xCoord, owner.posY + owner.getEyeHeight(), owner.posZ + look.zCoord, rotationYaw, rotationPitch);
			}
		}
		super.updateAITasks();
	}

	/**
	 * getOwnerUUID(); retrieves from datawatcher
	 */
	@Override
	public String func_152113_b() {
		return dataWatcher.getWatchableObjectString(OWNER_INDEX);
	}

	/**
	 * Updates datawatcher with the owner's UUID
	 */
	public void setOwnerUUID(String uuid) {
		this.dataWatcher.updateObject(OWNER_INDEX, uuid);
	}

	@Override
	public EntityPlayer getOwner() {
		if (owner != null) {
			return owner;
		}
		try {
			UUID uuid = UUID.fromString(func_152113_b());
			if (uuid != null) {
				owner = worldObj.func_152378_a(uuid);
			}
			return owner;
		} catch (IllegalArgumentException e) {
			return null;
		}
	}

	/**
	 * Sets the owner of this fairy; without an owner, Navi will perish...
	 */
	public void setOwner(EntityPlayer player) {
		this.owner = player;
		setOwnerUUID(player.getUniqueID().toString());
	}

	/**
	 * Checks nearby for any secret rooms; requires owner to be valid
	 */
	private void detectDungeons() {
		int range = Config.getNaviRange();
		double r2 = (range * range);
		int r = range + (range / 2);
		int ry = MathHelper.ceiling_float_int((float) r / 2.0F);
		int x = MathHelper.floor_double(owner.posX + 0.5D);
		int y = MathHelper.floor_double(owner.posY + owner.getEyeHeight());
		int z = MathHelper.floor_double(owner.posZ + 0.5D);
		boolean search = true;
		for (int i = x - r; i <= x + r && search; ++i) {
			for (int j = y - ry; j <= y + ry && search; ++j) {
				for (int k = z - r; k <= z + r && search; ++k) {
					if (worldObj.getBlock(i, j, k) instanceof IDungeonBlock) {
						double d = owner.getDistanceSq((double) i + 0.5D, (double) j + 0.5D, (double) k + 0.5D);
						if (d <= r2) {
							float f = (float)(Math.sqrt(d) / (double) r);
							// play sound only on the client side - not everyone needs to hear each other's annoying fairies
							PlayerUtils.playSound(owner, Sounds.FAIRY_LAUGH, 0.6F - (f / 2.0F), 1.0F);
							search = false;
						}
					}
				}
			}
		}
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound compound) {
		super.writeEntityToNBT(compound);
		String uuid = func_152113_b();
		if (uuid != null && uuid.length() > 0) {
			compound.setString("OwnerUUID", uuid);
		}
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound compound) {
		super.readEntityFromNBT(compound);
		String s = (compound.hasKey("OwnerUUID", Constants.NBT.TAG_STRING) ? compound.getString("OwnerUUID") : "");
		if (s.length() > 0) {
			setOwnerUUID(s);
		}
	}
}
