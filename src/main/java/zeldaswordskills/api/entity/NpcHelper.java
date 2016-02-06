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

package zeldaswordskills.api.entity;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.IMerchant;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import zeldaswordskills.ZSSMain;
import zeldaswordskills.entity.npc.EntityNpcBarnes;
import zeldaswordskills.entity.npc.EntityNpcMaskTrader;
import zeldaswordskills.entity.npc.EntityNpcOrca;

import com.google.common.collect.Maps;

/**
 *
 * Helper class for handling Npcs that are spawned by converting existing, named villagers.
 * Adding a name/class mapping will cause the {@link INpcVillager} methods to be called
 * for villagers with that name at the designated interaction points.
 *
 */
public class NpcHelper
{
	private static final Map<String, Class<? extends Entity>> nameToClassMap = Maps.newHashMap();

	static {
		nameToClassMap.put("Barnes", EntityNpcBarnes.class);
		nameToClassMap.put("Happy Mask Salesman", EntityNpcMaskTrader.class);
		nameToClassMap.put("Mask Salesman", EntityNpcMaskTrader.class);
		nameToClassMap.put("Orca", EntityNpcOrca.class);
	}

	/**
	 * Adds a mapping for the specified Npc name to Npc class; when interacting with
	 * an EntityVillager with this exact name, a conversion will be attempted.
	 * @param name     Entity's name tag must match exactly in order to attempt conversion
	 * @param npcClass Must implement INpcVillager in addition to Entity
	 */
	public static void addVillagerConversion(String name, Class<? extends Entity> npcClass) {
		if (!npcClass.isAssignableFrom(INpcVillager.class)) {
			throw new IllegalArgumentException("Entity class must implement INpcVillager to add a villager conversion mapping");
		} else if (nameToClassMap.containsKey(name)) {
			ZSSMain.logger.warn("Failed to add villager conversion for " + name + ": entry already exists - " + nameToClassMap.get(name));
		}
		nameToClassMap.put(name, npcClass);
	}

	/**
	 * Attempts to convert a named villager into an NPC; if successful, the villager
	 * is destroyed and the NPC spawned in its place.
	 * @param rightClick True if converting from right-click interact event, or false if from left-click
	 * @return DEFAULT to allow the event or calling method to continue processing
	 *         ALLOW   to cancel any further processing and signify that the villager was successfully converted
	 *         DENY    to cancel any further processing and signify that the villager was NOT converted
	 */
	public static Result convertVillager(EntityPlayer player, EntityVillager villager, boolean rightClick) {
		Result result = Result.DEFAULT;
		String name = villager.getCustomNameTag();
		for (String match : nameToClassMap.keySet()) {
			if (match.equals(name)) {
				Entity npc = getNpcForName(name, villager.worldObj);
				npc.setLocationAndAngles(villager.posX, villager.posY + 0.2F, villager.posZ, villager.rotationYaw, villager.rotationPitch);
				if (npc instanceof INpcVillager) {
					result = (rightClick ? ((INpcVillager) npc).canInteractConvert(player, villager) : ((INpcVillager) npc).canLeftClickConvert(player, villager));
					if (result == Result.ALLOW && !villager.worldObj.isRemote) { 
						if (npc instanceof IMerchant) {
							((IMerchant) npc).setRecipes(villager.getRecipes(player));
						}
						if (npc instanceof EntityLiving) {
							((EntityLiving) npc).onInitialSpawn(npc.worldObj.getDifficultyForLocation(new BlockPos(npc)), null);
						}
						npc.setCustomNameTag(name);
						villager.setDead();
						villager.worldObj.spawnEntityInWorld(npc);
						((INpcVillager) npc).onConverted(player);
					}
				}
				break;
			}
		}
		return result;
	}

	private static Entity getNpcForName(String name, World world) {
		Entity npc = null;
		try {
			npc = (Entity) nameToClassMap.get(name).getConstructor(World.class).newInstance(world);
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		}
		return npc;
	}
}
