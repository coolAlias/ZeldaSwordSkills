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

package zeldaswordskills.entity.projectile;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumMovingObjectType;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;
import zeldaswordskills.client.particle.FXCycloneRing;
import zeldaswordskills.lib.Config;
import zeldaswordskills.lib.ModInfo;
import zeldaswordskills.util.SideHit;
import zeldaswordskills.util.WorldUtils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class EntityCyclone extends EntityMobThrowable
{
	// Particle effect stuff:
	private static float maxPitch = 0.4f;
	private float yaw = 0;
	private float yawVelocity = 0.1f;
	private float pitch = 0;
	private static float pitchVelocity = 0;
	
	/** ItemStack version of captured drops is more efficient for NBT storage */
	private List<ItemStack> capturedItems = new ArrayList<ItemStack>();
	
	public EntityCyclone(World world) {
		super(world);
		setSize(1.0F, 2.0F);
	}

	public EntityCyclone(World world, EntityLivingBase entity) {
		super(world, entity);
		setSize(1.0F, 2.0F);
		posX -= motionX * 2;
		posZ -= motionZ * 2;
	}

	public EntityCyclone(World world, double x, double y, double z) {
		super(world, x, y, z);
		setSize(1.0F, 2.0F);
	}

	public EntityCyclone(World world, EntityLivingBase shooter, EntityLivingBase target, float velocity, float wobble) {
		super(world, shooter, target, velocity, wobble);
		setSize(1.0F, 2.0F);
	}
	
	@Override
	public void applyEntityCollision(Entity entity) {}
	
	@Override
	public boolean handleWaterMovement() { return false; }
	
	@Override
	public boolean handleLavaMovement() { return false; }
	
	@Override
	protected float getGravityVelocity() { return 0.0F; }
	
	@Override
	protected float func_70182_d() { return 0.75F; }
	
	@Override
	public void onUpdate() {
		super.onUpdate();
		motionY += 0.01D;
		if (!worldObj.isRemote) {
			captureDrops();
			destroyLeaves();
			if (ticksExisted > 40) {
				setDead();
				releaseDrops();
			} else if (ticksExisted % 6 == 5) {
				worldObj.playSoundAtEntity(this, ModInfo.SOUND_WHIRLWIND, 0.6F, 1.0F);
			}
		} else {
			spawnParticleRing();
		}
	}

	@Override
	protected void onImpact(MovingObjectPosition mop) {
		if (mop.typeOfHit == EnumMovingObjectType.TILE) {
			Material m = worldObj.getBlockMaterial(mop.blockX, mop.blockY, mop.blockZ);
			if (m == Material.leaves) {
				if (Config.canDekuDenude()) {
					worldObj.destroyBlock(mop.blockX, mop.blockY, mop.blockZ, true);
				}
			} else if (m.blocksMovement()) {
				if (mop.sideHit == SideHit.TOP) {
					posY = mop.blockY + 1;
					rotationPitch = 0.0F;
					motionY = 0.0D;
				} else if (!worldObj.isRemote) {
					setDead();
					releaseDrops();
				}
			}
		} else if (mop.entityHit != null) {
			if (!(mop.entityHit instanceof EntityLivingBase) || rand.nextFloat() > ((EntityLivingBase) mop.entityHit).getAttributeMap().getAttributeInstance(SharedMonsterAttributes.knockbackResistance).getAttributeValue()) {
				mop.entityHit.motionX = this.motionX * 1.8D;
				mop.entityHit.motionY = this.motionY + 0.5D;
				mop.entityHit.motionZ = this.motionZ * 1.8D;
				mop.entityHit.rotationYaw += 30.0F * this.ticksExisted;
			}
		}
	}
	
	/**
	 * Scans for and captures nearby EntityItems
	 */
	private void captureDrops() {
		if (!isDead) {
			List<EntityItem> items = worldObj.getEntitiesWithinAABB(EntityItem.class, boundingBox.expand(1.0D, 1.0D, 1.0D));
			for (EntityItem item : items) {
				capturedItems.add(item.getEntityItem());
				item.setDead();
			}
		}
	}
	
	/**
	 * Releases all captured drops into the world as dropped items
	 */
	private void releaseDrops() {
		for (ItemStack stack : capturedItems) {
			WorldUtils.spawnItemWithRandom(worldObj, stack, posX, posY, posZ);
		}
	}
	
	/**
	 * Checks for and destroys leaves each update tick
	 */
	private void destroyLeaves() {
		List affectedBlockPositions = new ArrayList(WorldUtils.getAffectedBlocksList(worldObj, rand, 2.0F, posX, posY, posZ, -1));
		Iterator iterator;
		ChunkPosition chunkposition;
		int i, j, k;
		iterator = affectedBlockPositions.iterator();
		while (iterator.hasNext()) {
			chunkposition = (ChunkPosition)iterator.next();
			i = chunkposition.x;
			j = chunkposition.y;
			k = chunkposition.z;
			Material m = worldObj.getBlockMaterial(i, j, k);
			if ((m == Material.leaves && Config.canDekuDenude()) || m == Material.plants || m == Material.vine || m == Material.web) {
				worldObj.destroyBlock(i, j, k, true);
			}
		}
	}
	
	/** Updates the cyclone swirling angles and spawns a new ring of particles. */
	@SideOnly(Side.CLIENT)
	private void spawnParticleRing() {
		yaw += yawVelocity;
		if (yaw > 2*Math.PI)
			yaw -= 2*Math.PI;
		
		if (Math.random() < 0.1) {
			//if (pitchVelocity < 0.01)
				pitchVelocity = 0.2f;
		}
		pitch += pitchVelocity;
		if (pitch > maxPitch)
			pitch = maxPitch;
		if (pitchVelocity > 0) {
			pitchVelocity -= 0.05f;
		} else {
			pitchVelocity = 0;
		}
		if (pitch > 0) {
			pitch -= 0.07f;
		} else {
			pitch = 0;
		}
		
		// This was left from when Cyclone had a predetermined duration in Dota 2 Items:
		/*if (duration - elapsed < 0.5f && alpha > 0) {
			alpha -= 0.05f;
		}*/
		//TODO: when destroying the cyclone, set the particles to start fading
		
		EffectRenderer effectRenderer = Minecraft.getMinecraft().effectRenderer;
		FXCycloneRing ring = new FXCycloneRing(worldObj, posX, posY + 0.1D, posZ, motionX, motionY, motionZ, yaw, pitch, 0.7f, effectRenderer);
		effectRenderer.addEffect(ring);
	}
	
	@Override
	public void writeEntityToNBT(NBTTagCompound compound) {
		super.writeEntityToNBT(compound);
		NBTTagList items = new NBTTagList();
		for (ItemStack stack : capturedItems) {
			NBTTagCompound dropNBT = new NBTTagCompound();
			stack.writeToNBT(dropNBT);
			items.appendTag(dropNBT);
		}
		compound.setTag("items", items);
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound compound) {
		super.readEntityFromNBT(compound);
		NBTTagList items = compound.getTagList("items");
		for (int i = 0; i < items.tagCount(); ++i) {
			capturedItems.add(ItemStack.loadItemStackFromNBT((NBTTagCompound) items.tagAt(i)));
		}
	}
}
