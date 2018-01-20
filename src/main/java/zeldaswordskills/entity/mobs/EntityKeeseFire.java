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

import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.world.World;
import zeldaswordskills.entity.ZSSEntityInfo;
import zeldaswordskills.entity.buff.Buff;

public class EntityKeeseFire extends EntityKeese
{
	public EntityKeeseFire(World world) {
		super(world);
		this.isImmuneToFire = true;
		this.experienceValue = 3;
	}

	@Override
	protected EntityKeeseFire createInstance() {
		return new EntityKeeseFire(this.worldObj);
	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(12.0F);
		ZSSEntityInfo.get(this).applyBuff(Buff.RESIST_FIRE, Integer.MAX_VALUE, 100);
		ZSSEntityInfo.get(this).applyBuff(Buff.WEAKNESS_COLD, Integer.MAX_VALUE, 100);
	}

	@Override
	protected DamageSource getDamageSource() {
		return new EntityDamageSource("mob", this).setFireDamage();
	}

	@Override
	protected void applySecondaryEffects(EntityPlayer player) {
		if (this.rand.nextFloat() < 0.5F) {
			player.setFire(this.rand.nextInt(4) + 4);
		}
	}
}
