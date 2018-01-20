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
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;
import zeldaswordskills.api.block.IWhipBlock.WhipType;
import zeldaswordskills.entity.ZSSEntityInfo;
import zeldaswordskills.entity.buff.Buff;
import zeldaswordskills.item.ItemTreasure.Treasures;
import zeldaswordskills.item.ZSSItems;

public class EntityKeeseCursed extends EntityKeese
{
	public EntityKeeseCursed(World world) {
		super(world);
		this.isImmuneToFire = true;
		this.experienceValue = 7;
	}

	@Override
	protected EntityKeeseCursed createInstance() {
		return new EntityKeeseCursed(this.worldObj);
	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(16.0F);
		ZSSEntityInfo.get(this).applyBuff(Buff.RESIST_FIRE, Integer.MAX_VALUE, 100);
		ZSSEntityInfo.get(this).applyBuff(Buff.WEAKNESS_HOLY, Integer.MAX_VALUE, 100);
	}

	@Override
	protected void applySecondaryEffects(EntityPlayer player) {
		switch (this.rand.nextInt(16)) {
		case 0: ZSSEntityInfo.get(player).applyBuff(Buff.ATTACK_DOWN, this.rand.nextInt(500) + 100, this.rand.nextInt(51) + 50); break;
		case 1: ZSSEntityInfo.get(player).applyBuff(Buff.DEFENSE_DOWN, this.rand.nextInt(500) + 100, this.rand.nextInt(26) + 25); break;
		case 2: ZSSEntityInfo.get(player).applyBuff(Buff.EVADE_DOWN, this.rand.nextInt(500) + 100, this.rand.nextInt(51) + 50); break;
		case 3: ZSSEntityInfo.get(player).applyBuff(Buff.WEAKNESS_COLD, this.rand.nextInt(500) + 100, this.rand.nextInt(51) + 50); break;
		case 4: ZSSEntityInfo.get(player).applyBuff(Buff.WEAKNESS_FIRE, this.rand.nextInt(500) + 100, this.rand.nextInt(51) + 50); break;
		case 5: ZSSEntityInfo.get(player).applyBuff(Buff.WEAKNESS_MAGIC, this.rand.nextInt(500) + 100, this.rand.nextInt(51) + 50); break;
		case 6: ZSSEntityInfo.get(player).applyBuff(Buff.WEAKNESS_SHOCK, this.rand.nextInt(500) + 100, this.rand.nextInt(51) + 50); break;
		case 7: player.addPotionEffect(new PotionEffect(Potion.confusion.id, this.rand.nextInt(500) + 100, 1)); break;
		case 8: player.addPotionEffect(new PotionEffect(Potion.blindness.id, this.rand.nextInt(500) + 100, 1)); break;
		case 9: player.addPotionEffect(new PotionEffect(Potion.poison.id, this.rand.nextInt(100) + 50, this.rand.nextInt(9) / 8)); break;
		case 10: player.addPotionEffect(new PotionEffect(Potion.harm.id, 1, this.rand.nextInt(9) / 8)); break;
		default:
		}
	}

	@Override
	protected void dropRareDrop(int rarity) {
		switch (rarity) {
		case 1: this.entityDropItem(new ItemStack(ZSSItems.treasure, 1, Treasures.EVIL_CRYSTAL.ordinal()), 0.0F); break;
		default: this.entityDropItem(new ItemStack(this.rand.nextInt(8) == 0 ? ZSSItems.heartPiece : ZSSItems.smallHeart), 0.0F);
		}
	}

	@Override
	public ItemStack getEntityLoot(EntityPlayer player, WhipType whip) {
		if (this.rand.nextFloat() < (0.1F * (1 + whip.ordinal()))) {
			return new ItemStack(ZSSItems.treasure, 1, Treasures.EVIL_CRYSTAL.ordinal());
		}
		return new ItemStack(rand.nextInt(3) > 0 ? Items.emerald : ZSSItems.smallHeart);
	}
}
