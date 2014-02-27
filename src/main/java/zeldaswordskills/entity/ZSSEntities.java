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

import net.minecraft.client.model.ModelSquid;
import net.minecraft.client.renderer.entity.RenderSnowball;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityEggInfo;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.Configuration;
import zeldaswordskills.ZSSMain;
import zeldaswordskills.api.entity.EntityArrowCustom;
import zeldaswordskills.client.render.RenderNothing;
import zeldaswordskills.client.render.entity.RenderCustomArrow;
import zeldaswordskills.client.render.entity.RenderEntityBomb;
import zeldaswordskills.client.render.entity.RenderEntityBoomerang;
import zeldaswordskills.client.render.entity.RenderEntityChu;
import zeldaswordskills.client.render.entity.RenderEntityFairy;
import zeldaswordskills.client.render.entity.RenderEntityHookShot;
import zeldaswordskills.client.render.entity.RenderEntityJar;
import zeldaswordskills.client.render.entity.RenderEntityKeese;
import zeldaswordskills.client.render.entity.RenderEntitySwordBeam;
import zeldaswordskills.client.render.entity.RenderOctorok;
import zeldaswordskills.entity.projectile.EntityArrowBomb;
import zeldaswordskills.entity.projectile.EntityArrowElemental;
import zeldaswordskills.entity.projectile.EntityBomb;
import zeldaswordskills.entity.projectile.EntityBoomerang;
import zeldaswordskills.entity.projectile.EntityCeramicJar;
import zeldaswordskills.entity.projectile.EntityCyclone;
import zeldaswordskills.entity.projectile.EntityHookShot;
import zeldaswordskills.entity.projectile.EntityLeapingBlow;
import zeldaswordskills.entity.projectile.EntitySeedShot;
import zeldaswordskills.entity.projectile.EntitySwordBeam;
import zeldaswordskills.entity.projectile.EntityThrowingRock;
import zeldaswordskills.item.ZSSItems;
import zeldaswordskills.lib.Config;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ZSSEntities
{
	/** Spawn rates */
	private static int spawnChu, spawnFairy, spawnKeese, spawnOctorok;
	
	/**
	 * Initializes entity spawn rates 
	 */
	public static void init(Configuration config) {
		// SPAWN RATES
		spawnChu = config.get("Spawn Rates", "Chuchu spawn rate (0 to disable)[0+]", 1).getInt();
		spawnFairy = config.get("Spawn Rates", "Fairy (wild) spawn rate (0 to disable)[0+]", 1).getInt();
		spawnKeese = config.get("Spawn Rates", "Keese spawn rate (0 to disable)[0+]", 1).getInt();
		spawnOctorok = config.get("Spawn Rates", "Octorok spawn rate (0 to disable)[0+]", 8).getInt();
	}
	
	/**
	 * Registers all entities, entity eggs, and adds spawns
	 */
	public static void load() {
		registerEntities();
		registerEggs();
		addSpawns();
	}
	
	protected static void registerEntities() {
		int modEntityIndex = 0;
		EntityRegistry.registerModEntity(EntityLeapingBlow.class, "leapingblow", ++modEntityIndex, ZSSMain.instance, 64, 10, true);
		EntityRegistry.registerModEntity(EntitySwordBeam.class, "swordbeam", ++modEntityIndex, ZSSMain.instance, 64, 10, true);
		EntityRegistry.registerModEntity(EntityBomb.class, "bomb", ++modEntityIndex, ZSSMain.instance, 64, 10, true);
		EntityRegistry.registerModEntity(EntityBoomerang.class, "boomerang", ++modEntityIndex, ZSSMain.instance, 64, 10, true);
		EntityRegistry.registerModEntity(EntityCyclone.class, "cyclone", ++modEntityIndex, ZSSMain.instance, 64, 10, true);
		EntityRegistry.registerModEntity(EntityCeramicJar.class, "jar", ++modEntityIndex, ZSSMain.instance, 64, 10, true);
		EntityRegistry.registerModEntity(EntityHookShot.class, "hookshot", ++modEntityIndex, ZSSMain.instance, 64, 10, true);
		EntityRegistry.registerModEntity(EntitySeedShot.class, "seedshot", ++modEntityIndex, ZSSMain.instance, 64, 10, true);
		EntityRegistry.registerModEntity(EntityThrowingRock.class, "rock", ++modEntityIndex, ZSSMain.instance, 64, 10, true);
		EntityRegistry.registerModEntity(EntityArrowBomb.class, "arrowbomb", ++modEntityIndex, ZSSMain.instance, 64, 20, true);
		EntityRegistry.registerModEntity(EntityArrowCustom.class, "arrowcustom", ++modEntityIndex, ZSSMain.instance, 64, 20, true);
		EntityRegistry.registerModEntity(EntityArrowElemental.class, "arrowelemental", ++modEntityIndex, ZSSMain.instance, 64, 20, true);
		
		// MOBS
		EntityRegistry.registerModEntity(EntityChu.class, "chu", ++modEntityIndex, ZSSMain.instance, 80, 3, false);
		EntityRegistry.registerModEntity(EntityFairy.class, "fairy", ++modEntityIndex, ZSSMain.instance, 80, 3, false);
		EntityRegistry.registerModEntity(EntityKeese.class, "keese", ++modEntityIndex, ZSSMain.instance, 80, 3, false);
		EntityRegistry.registerModEntity(EntityOctorok.class, "octorok", ++modEntityIndex, ZSSMain.instance, 80, 3, false);
	}
	
	@SideOnly(Side.CLIENT) 
	public static void registerRenderers() {
		RenderingRegistry.registerEntityRenderingHandler(EntityArrowCustom.class, new RenderCustomArrow());
		RenderingRegistry.registerEntityRenderingHandler(EntityBomb.class, new RenderEntityBomb());
		RenderingRegistry.registerEntityRenderingHandler(EntityBoomerang.class, new RenderEntityBoomerang());
		RenderingRegistry.registerEntityRenderingHandler(EntityCeramicJar.class, new RenderEntityJar());
		RenderingRegistry.registerEntityRenderingHandler(EntityChu.class, new RenderEntityChu());
		RenderingRegistry.registerEntityRenderingHandler(EntityFairy.class, new RenderEntityFairy());
		RenderingRegistry.registerEntityRenderingHandler(EntityKeese.class, new RenderEntityKeese());
		RenderingRegistry.registerEntityRenderingHandler(EntityHookShot.class, new RenderEntityHookShot());
		RenderingRegistry.registerEntityRenderingHandler(EntityLeapingBlow.class, new RenderNothing());
		RenderingRegistry.registerEntityRenderingHandler(EntityOctorok.class, new RenderOctorok(new ModelSquid(), 0.7F));
		RenderingRegistry.registerEntityRenderingHandler(EntitySeedShot.class, new RenderSnowball(ZSSItems.dekuNut));
		RenderingRegistry.registerEntityRenderingHandler(EntitySwordBeam.class, new RenderEntitySwordBeam());
		RenderingRegistry.registerEntityRenderingHandler(EntityThrowingRock.class, new RenderSnowball(ZSSItems.throwingRock));
		RenderingRegistry.registerEntityRenderingHandler(EntityCyclone.class, new RenderNothing());
	}
	
	protected static void registerEggs() {
		int id = Config.getSpawnEggStartId();
		registerEntityEgg(EntityFairy.class, (id == 0 ? getNextEggId() : id++), 0xADFF2F, 0xFFFF00);
		registerEntityEgg(EntityChu.class, (id == 0 ? getNextEggId() : id++), 0xEE2C2C, 0x00CED1);
		registerEntityEgg(EntityKeese.class, (id == 0 ? getNextEggId() : id++), 0x555555, 0x000000);
		registerEntityEgg(EntityOctorok.class, (id == 0 ? getNextEggId() : id++), 0x68228B, 0xBA55D3);
	}
	
	protected static void registerEntityEgg(Class<? extends Entity> entity, int id, int primaryColor, int secondaryColor){
		EntityList.IDtoClassMapping.put(id, entity);
		EntityList.entityEggs.put(id, new EntityEggInfo(id, primaryColor, secondaryColor));
	}
	
	/**
	 * Returns the next available egg id
	 */
	protected static int getNextEggId() {
		int i = 0;
		while (EntityList.entityEggs.containsKey(i)) {
			++i;
		}
		return i;
	}
	
	protected static void addSpawns() {
		if (spawnFairy > 0) {
			EntityRegistry.addSpawn(EntityFairy.class, spawnFairy, 1, 3, EnumCreatureType.ambient, BiomeGenBase.swampland);
		}
		for (BiomeGenBase biome : BiomeGenBase.biomeList) {
			if (biome != null) {
				if (spawnChu > 0) {
					EntityRegistry.addSpawn(EntityChu.class, spawnChu, 4, 4, EnumCreatureType.monster, biome);
				}
				if (spawnKeese > 0) {
					EntityRegistry.addSpawn(EntityKeese.class, spawnKeese, 4, 4, EnumCreatureType.ambient, biome);
				}
			}
		}
		if (spawnOctorok > 0) {
			EntityRegistry.addSpawn(EntityOctorok.class, spawnOctorok, 2, 4, EnumCreatureType.waterCreature, BiomeGenBase.ocean, BiomeGenBase.river, BiomeGenBase.swampland);
		}
	}
}
