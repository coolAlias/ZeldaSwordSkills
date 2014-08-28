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

package zeldaswordskills.item;

import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingData;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import zeldaswordskills.entity.CustomEntityList;
import zeldaswordskills.entity.IEntityVariant;
import zeldaswordskills.util.LogHelper;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * 
 * Spawn Eggs for custom entities with subtypes and implementing {@link IEntityVariant};
 * entities with only one variety should use a more vanilla-like style of spawner Item.
 * Unlike the vanilla spawn eggs, the Entity class to be spawned is stored as a field in the
 * Item instance, and each Entity has its own Item; item damage is used as the Entity sub-type. 
 * Uses the vanilla egg icons so that the egg styles always match, regardless of resource pack.
 *
 */
public class ItemCustomVariantEgg extends ItemCustomEgg
{
	/** The class of Entity that will be spawned */
	private final Class<? extends Entity> classToSpawn;

	/** The unlocalized entity name, retrieved as "entity.{entityName}.name" and suffixed with ".n", where 'n' is the subtype index */
	private final String entityName;

	public ItemCustomVariantEgg(int id, Class<? extends Entity> classToSpawn, String entityName) {
		super(id);
		this.classToSpawn = classToSpawn;
		this.entityName = entityName;
	}

	@Override
	public String getItemDisplayName(ItemStack stack) {
		String s = ("" + StatCollector.translateToLocal("item.zss.spawn_egg.name")).trim();
		if (entityName != null) {
			s = s + " " + StatCollector.translateToLocal("entity." + entityName + ".name." + stack.getItemDamage());
		}
		return s;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getColorFromItemStack(ItemStack stack, int renderPass) {
		List<Integer> colors = CustomEntityList.entityEggs.get(classToSpawn);
		int colorIndex = stack.getItemDamage() * 2;
		return colors != null && colors.size() > colorIndex + 1 ? colors.get((renderPass == 0 ? colorIndex : colorIndex + 1)) : 16777215;
	}

	@Override
	public Entity spawnCreature(World world, int subtype, double x, double y, double z) {
		Entity entity = null;

		if (CustomEntityList.entityEggs.containsKey(classToSpawn)) {
			entity = CustomEntityList.createEntity(classToSpawn, world);
			if (entity instanceof EntityLiving) {
				EntityLiving entityliving = (EntityLiving) entity;
				entity.setLocationAndAngles(x, y, z, MathHelper.wrapAngleTo180_float(world.rand.nextFloat() * 360.0F), 0.0F);
				entityliving.rotationYawHead = entityliving.rotationYaw;
				entityliving.renderYawOffset = entityliving.rotationYaw;
				entityliving.onSpawnWithEgg((EntityLivingData) null);
				if (entity instanceof IEntityVariant) {
					((IEntityVariant) entity).setType(subtype);
				}
				world.spawnEntityInWorld(entity);
				entityliving.playLivingSound();
			}
		}

		return entity;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(int item, CreativeTabs tab, List itemList) {
		List<Integer> colors = CustomEntityList.entityEggs.get(classToSpawn);
		if (colors.isEmpty()) {
			LogHelper.warning("Custom entity egg has an empty color list");
		} else if (colors.size() % 2 != 0) {
			LogHelper.warning("Custom entity egg has an odd number of colors");
		}
		for (int i = 0; i < (colors.size() / 2); ++i) {
			itemList.add(new ItemStack(item, 1, i));
		}
	}
}
