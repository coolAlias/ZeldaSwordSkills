/**
 * Copyright (C) <2019> <coolAlias>
 *
 * This file is part of coolAlias' Zelda Sword Skills Minecraft Mod; as such,
 * you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package zeldaswordskills.entity.mobs;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import zeldaswordskills.api.block.IWhipBlock.WhipType;
import zeldaswordskills.api.entity.BombType;
import zeldaswordskills.entity.projectile.EntityBomb;
import zeldaswordskills.item.ItemTreasure.Treasures;
import zeldaswordskills.item.ZSSItems;

public class EntityOctorokPink extends EntityOctorok
{
	public EntityOctorokPink(World world) {
		super(world);
	}

	@Override
	protected Entity getProjectile(EntityLivingBase target, float damage) {
		int difficulty = worldObj.getDifficulty().getDifficultyId();
		return new EntityBomb(worldObj, this, target, 0.2F + (difficulty * 0.1F), (float)(14 - difficulty * 4))
				.setType(BombType.BOMB_WATER)
				.setFuseTime(-1) // explode on impact
				.setMotionFactor(0.25F)
				.setNoGrief()
				.setIgnoreWater()
				.setGravityVelocity(0.01F)
				.setDamage(damage * 2.0F * difficulty);
	}

	@Override
	protected Item getDropItem() {
		return Items.gunpowder;
	}

	@Override
	protected void addRandomDrop() {
		switch (rand.nextInt(8)) {
		case 1: this.entityDropItem(new ItemStack(ZSSItems.treasure, 1, Treasures.TENTACLE.ordinal()), 0.0F);
		default: this.entityDropItem(new ItemStack(ZSSItems.bomb, 1, BombType.BOMB_WATER.ordinal()), 0.0F);
		}
	}

	@Override
	public ItemStack getEntityLoot(EntityPlayer player, WhipType whip) {
		float chance = 0.1F * (1 + whip.ordinal());
		if (this.rand.nextFloat() < chance) {
			return new ItemStack(ZSSItems.treasure,1,Treasures.TENTACLE.ordinal());
		} else if (this.rand.nextFloat() < chance) {
			return new ItemStack(ZSSItems.bomb, 1, BombType.BOMB_WATER.ordinal());
		}
		return new ItemStack(this.getDropItem(), 1, 0);
	}
}
