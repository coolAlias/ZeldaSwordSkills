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

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import zeldaswordskills.api.damage.DamageUtils.DamageSourceIce;
import zeldaswordskills.api.damage.DamageUtils.DamageSourceShock;
import zeldaswordskills.entity.ZSSEntityInfo;
import zeldaswordskills.entity.buff.Buff;

/**
 * 
 * Blue Chus are the rarest type of all. Like the Yellow Chu, they become electrified as a defense
 * mechanism and are also highly resistant to both magic and cold. Their attacks are imbued with cold.
 *
 */
public class EntityChuBlue extends EntityChuElectric
{
	public EntityChuBlue(World world) {
		super(world);
	}

	@Override
	protected EntityChuBlue createInstance() {
		return new EntityChuBlue(this.worldObj);
	}

	@Override
	protected void applyTypeTraits() {
		ZSSEntityInfo info = ZSSEntityInfo.get(this);
		info.applyBuff(Buff.RESIST_MAGIC, Integer.MAX_VALUE, 75);
		info.applyBuff(Buff.RESIST_COLD, Integer.MAX_VALUE, 100);
		info.applyBuff(Buff.RESIST_SHOCK, Integer.MAX_VALUE, 50);
	}

	@Override
	public ChuType getType() {
		return ChuType.BLUE;
	}

	@Override
	protected DamageSource getDamageSource() {
		if (this.getShockTime() > 0) {
			return new DamageSourceShock("shock", this, this.getMaxStunTime(), this.getDamage());
		}
		return new DamageSourceIce("mob", this, 50, (this.getSlimeSize() > 2 ? 1 : 0));
	}

	@Override
	protected void applySecondaryEffects(EntityLivingBase target) {
		if (this.rand.nextFloat() < (0.25F * this.getSlimeSize())) {
			ZSSEntityInfo.get(target).applyBuff(Buff.WEAKNESS_COLD, 200, 50);
		}
	}
}
