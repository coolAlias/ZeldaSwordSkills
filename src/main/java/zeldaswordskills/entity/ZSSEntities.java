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

package zeldaswordskills.entity;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelSquid;
import net.minecraft.client.renderer.entity.RenderSnowball;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.monster.EntityBlaze;
import net.minecraft.entity.monster.EntityCaveSpider;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.EntityGhast;
import net.minecraft.entity.monster.EntityIronGolem;
import net.minecraft.entity.monster.EntityMagmaCube;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.monster.EntitySnowman;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.entity.monster.EntityWitch;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.config.Configuration;
import zeldaswordskills.ZSSMain;
import zeldaswordskills.api.entity.LootableEntityRegistry;
import zeldaswordskills.client.model.ModelDarknut;
import zeldaswordskills.client.model.ModelGoron;
import zeldaswordskills.client.model.ModelMaskSalesman;
import zeldaswordskills.client.model.ModelWizzrobe;
import zeldaswordskills.client.render.RenderNothing;
import zeldaswordskills.client.render.entity.RenderCustomArrow;
import zeldaswordskills.client.render.entity.RenderEntityBomb;
import zeldaswordskills.client.render.entity.RenderEntityBoomerang;
import zeldaswordskills.client.render.entity.RenderEntityChu;
import zeldaswordskills.client.render.entity.RenderEntityFairy;
import zeldaswordskills.client.render.entity.RenderEntityHookShot;
import zeldaswordskills.client.render.entity.RenderEntityJar;
import zeldaswordskills.client.render.entity.RenderEntityKeese;
import zeldaswordskills.client.render.entity.RenderEntityMagicSpell;
import zeldaswordskills.client.render.entity.RenderEntityOctorok;
import zeldaswordskills.client.render.entity.RenderEntitySwordBeam;
import zeldaswordskills.client.render.entity.RenderEntityWhip;
import zeldaswordskills.client.render.entity.RenderEntityWizzrobe;
import zeldaswordskills.client.render.entity.RenderGenericLiving;
import zeldaswordskills.entity.mobs.EntityBlackKnight;
import zeldaswordskills.entity.mobs.EntityChu;
import zeldaswordskills.entity.mobs.EntityDarknut;
import zeldaswordskills.entity.mobs.EntityGrandWizzrobe;
import zeldaswordskills.entity.mobs.EntityKeese;
import zeldaswordskills.entity.mobs.EntityOctorok;
import zeldaswordskills.entity.mobs.EntityWizzrobe;
import zeldaswordskills.entity.npc.EntityGoron;
import zeldaswordskills.entity.npc.EntityNpcBarnes;
import zeldaswordskills.entity.npc.EntityNpcMaskTrader;
import zeldaswordskills.entity.npc.EntityNpcOrca;
import zeldaswordskills.entity.projectile.EntityArrowBomb;
import zeldaswordskills.entity.projectile.EntityArrowCustom;
import zeldaswordskills.entity.projectile.EntityArrowElemental;
import zeldaswordskills.entity.projectile.EntityBomb;
import zeldaswordskills.entity.projectile.EntityBoomerang;
import zeldaswordskills.entity.projectile.EntityCeramicJar;
import zeldaswordskills.entity.projectile.EntityCyclone;
import zeldaswordskills.entity.projectile.EntityHookShot;
import zeldaswordskills.entity.projectile.EntityLeapingBlow;
import zeldaswordskills.entity.projectile.EntityMagicSpell;
import zeldaswordskills.entity.projectile.EntitySeedShot;
import zeldaswordskills.entity.projectile.EntitySwordBeam;
import zeldaswordskills.entity.projectile.EntityThrowingRock;
import zeldaswordskills.entity.projectile.EntityWhip;
import zeldaswordskills.item.ZSSItems;
import zeldaswordskills.ref.Config;
import zeldaswordskills.ref.LibPotionID;
import zeldaswordskills.ref.ModInfo;
import zeldaswordskills.util.BiomeType;
import zeldaswordskills.util.SpawnableEntityData;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ZSSEntities
{
	/** Spawn rates */
	private static int spawnGoron;

	public static int getGoronRatio() { return spawnGoron; }

	/** Array of default biomes each mob is allowed to spawn in */
	private static final Map<Class<? extends EntityLiving>, String[]> defaultSpawnLists = new HashMap<Class<? extends EntityLiving>, String[]>();

	/** Map of SpawnableEntityData for each entity class that can spawn naturally */
	private static final Map<Class<? extends EntityLiving>, SpawnableEntityData> spawnableEntityData = new HashMap<Class<? extends EntityLiving>, SpawnableEntityData>();

	/**
	 * Registers all entities, entity eggs, and populates default spawn biome lists
	 */
	public static void preInit() {
		registerEntities();
		addSpawnLocations(EntityChu.class, EntityChu.getDefaultBiomes());
		addSpawnLocations(EntityDarknut.class, EntityDarknut.getDefaultBiomes());
		addSpawnLocations(EntityFairy.class, BiomeType.RIVER.defaultBiomes);
		addSpawnLocations(EntityKeese.class, EntityKeese.getDefaultBiomes());
		addSpawnLocations(EntityWizzrobe.class, EntityWizzrobe.getDefaultBiomes());
		addSpawnLocations(EntityOctorok.class, BiomeType.OCEAN.defaultBiomes);
	}

	/**
	 * Initializes entity spawn rates, spawn locations, and adds spawns.
	 */
	public static void postInit(Configuration config) {
		// REGISTER ENTITY SPAWN DATA
		int rate = config.get("Mob Spawns", "[Spawn Rate] Chuchu spawn rate (0 to disable)[0+]", 10).getInt();
		addSpawnableEntityData(EntityChu.class, EnumCreatureType.monster, 4, 4, rate);
		rate = config.get("Mob Spawns", "[Spawn Rate] Darknut spawn rate (0 to disable)[0+]", 5).getInt();
		addSpawnableEntityData(EntityDarknut.class, EnumCreatureType.monster, 1, 1, rate);
		rate = config.get("Mob Spawns", "[Spawn Rate] Fairy (wild) spawn rate (0 to disable)[0+]", 1).getInt();
		addSpawnableEntityData(EntityFairy.class, EnumCreatureType.ambient, 1, 3, rate);
		// Gorons are an exception, as they are not spawned using vanilla mechanics
		spawnGoron = config.get("Mob Spawns", "[Spawn Rate] Goron spawn rate, as a ratio of regular villagers to Gorons (0 to disable)[0+]", 4).getInt();
		rate = config.get("Mob Spawns", "[Spawn Rate] Keese spawn rate (0 to disable)[0+]", 1).getInt();
		addSpawnableEntityData(EntityKeese.class, EnumCreatureType.ambient, 4, 4, rate); // TODO should use monster type???
		rate = config.get("Mob Spawns", "[Spawn Rate] Octorok spawn rate (0 to disable)[0+]", 8).getInt();
		addSpawnableEntityData(EntityOctorok.class, EnumCreatureType.waterCreature, 2, 4, rate);
		rate = config.get("Mob Spawns", "[Spawn Rate] Wizzrobe spawn rate (0 to disable)[0+]", 10).getInt();
		addSpawnableEntityData(EntityWizzrobe.class, EnumCreatureType.monster, 1, 1, rate);

		// ALLOWED BIOMES
		for (Class<? extends EntityLiving> entity : defaultSpawnLists.keySet()) {
			String[] defaultBiomes = defaultSpawnLists.get(entity);
			SpawnableEntityData spawnData = spawnableEntityData.get(entity);
			if (defaultBiomes != null && spawnData != null && spawnData.spawnRate > 0) {
				String[] biomes = config.get("Mob Spawns", String.format("[Spawn Biomes] List of biomes in which %s are allowed to spawn", entity.getName().substring(entity.getName().lastIndexOf(".") + 1)), defaultBiomes).getStringList();
				if (biomes != null) {
					addSpawns(entity, biomes, spawnData);
				}
			}
		}

		// VANILLA LOOTABLE ENTITIES
		float f = Config.getVanillaWhipLootChance();
		if (f > 0) {
			// won't override entries if added elsewhere first, which gives other mods a chance to register special loot for vanilla mobs
			LootableEntityRegistry.addLootableEntity(EntityBlaze.class, f, new ItemStack(Items.blaze_rod));
			LootableEntityRegistry.addLootableEntity(EntityCaveSpider.class, f, new ItemStack(Items.spider_eye), new ItemStack(Items.string));
			LootableEntityRegistry.addLootableEntity(EntityCreeper.class, f, new ItemStack(Items.gunpowder));
			LootableEntityRegistry.addLootableEntity(EntityEnderman.class, f, new ItemStack(Items.ender_pearl));
			LootableEntityRegistry.addLootableEntity(EntityGhast.class, f, new ItemStack(Items.ghast_tear), new ItemStack(Items.gunpowder), new ItemStack(Items.gunpowder));
			LootableEntityRegistry.addLootableEntity(EntityIronGolem.class, f, new ItemStack(Items.iron_ingot));
			LootableEntityRegistry.addLootableEntity(EntityMagmaCube.class, f, new ItemStack(Items.magma_cream));
			LootableEntityRegistry.addLootableEntity(EntityPigZombie.class, f, new ItemStack(Items.gold_nugget), new ItemStack(Items.gold_nugget), new ItemStack(Items.gold_ingot));
			LootableEntityRegistry.addLootableEntity(EntitySkeleton.class, f, new ItemStack(Items.arrow), new ItemStack(Items.bone), new ItemStack(Items.flint));
			LootableEntityRegistry.addLootableEntity(EntitySlime.class, f, new ItemStack(Items.slime_ball));
			LootableEntityRegistry.addLootableEntity(EntitySnowman.class, f, new ItemStack(Items.snowball));
			LootableEntityRegistry.addLootableEntity(EntitySpider.class, f, new ItemStack(Items.spider_eye), new ItemStack(Items.string));
			LootableEntityRegistry.addLootableEntity(EntityWitch.class, f, new ItemStack(Items.potionitem,1,LibPotionID.HEALING.id), new ItemStack(Items.potionitem,1,LibPotionID.SWIFTNESS.id), new ItemStack(Items.potionitem,1,LibPotionID.FIRERESIST.id), new ItemStack(Items.potionitem,1,LibPotionID.WATER_BREATHING.id));
			LootableEntityRegistry.addLootableEntity(EntityZombie.class, f, new ItemStack(Items.iron_ingot), new ItemStack(Items.carrot), new ItemStack(Items.potato));
		}
	}

	private static void addSpawns(Class<? extends EntityLiving> entity, String[] biomes, SpawnableEntityData spawnData) {
		for (String name : biomes) {
			BiomeGenBase biome = getBiomeByName(name);
			if (biome != null) {
				EntityRegistry.addSpawn(entity, spawnData.spawnRate, spawnData.min, spawnData.max, spawnData.creatureType, biome);
			} else {
				ZSSMain.logger.warn(String.format("Unable to find matching biome for %s while adding spawns for %s!", name, entity.getName().substring(entity.getName().lastIndexOf(".") + 1)));
			}
		}
	}

	/**
	 * Retrives the BiomeGenBase associated with the string given, or null if it was not found
	 */
	private static BiomeGenBase getBiomeByName(String name) {
		for (BiomeGenBase biome : BiomeGenBase.getBiomeGenArray()) {
			if (biome != null && biome.biomeName != null && biome.biomeName.toLowerCase().replace(" ", "").equals(name.toLowerCase().replace(" ", ""))) {
				return biome;
			}
		}
		return null;
	}

	private static void registerEntities() {
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
		EntityRegistry.registerModEntity(EntityMagicSpell.class, "magicspell", ++modEntityIndex, ZSSMain.instance, 64, 10, true);
		EntityRegistry.registerModEntity(EntityWhip.class, "whip", ++modEntityIndex, ZSSMain.instance, 64, 10, true);

		// NATURALLY SPAWNING MOBS
		registerEntity(EntityFairy.class, "fairy", ++modEntityIndex, 80, 0xADFF2F, 0xFFFF00);
		EntityRegistry.registerModEntity(EntityNavi.class, "navi", ++modEntityIndex, ZSSMain.instance, 80, 3, true);

		EntityRegistry.registerModEntity(EntityChu.class, "chu", ++modEntityIndex, ZSSMain.instance, 80, 3, true);
		CustomEntityList.addMapping(EntityChu.class, "chu", 0x008000, 0xDC143C, 0x008000, 0x00EE00, 0x008000, 0x3A5FCD, 0x008000, 0xFFFF00);

		EntityRegistry.registerModEntity(EntityDarknut.class, "darknut", ++modEntityIndex, ZSSMain.instance, 80, 3, true);
		CustomEntityList.addMapping(EntityDarknut.class, "darknut", 0x1E1E1E, 0x8B2500, 0x1E1E1E, 0xFB2500);

		EntityRegistry.registerModEntity(EntityKeese.class, "keese", ++modEntityIndex, ZSSMain.instance, 80, 3, true);
		CustomEntityList.addMapping(EntityKeese.class, "keese", 0x000000, 0x555555, 0x000000, 0xFF4500, 0x000000, 0x40E0D0, 0x000000, 0xFFD700, 0x000000, 0x800080);

		EntityRegistry.registerModEntity(EntityOctorok.class, "octorok", ++modEntityIndex, ZSSMain.instance, 80, 3, true);
		CustomEntityList.addMapping(EntityOctorok.class, "octorok", 0x68228B, 0xBA55D3, 0x68228B, 0xFF00FF);

		EntityRegistry.registerModEntity(EntityWizzrobe.class, "wizzrobe", ++modEntityIndex, ZSSMain.instance, 80, 3, true);
		CustomEntityList.addMapping(EntityWizzrobe.class, "wizzrobe", 0x8B2500, 0xFF0000, 0x8B2500, 0x00B2EE, 0x8B2500, 0xEEEE00, 0x8B2500, 0x00EE76);

		// BOSSES
		registerEntity(EntityGrandWizzrobe.class, "wizzrobe_grand", ++modEntityIndex, 80, 0x8B2500, 0x1E1E1E);
		registerEntity(EntityBlackKnight.class, "darknut_boss", ++modEntityIndex, 80, 0x1E1E1E, 0x000000);

		// NPCS
		registerEntity(EntityGoron.class, "goron", ++modEntityIndex, 80, 0xB8860B, 0x8B5A00);
		registerEntity(EntityNpcBarnes.class, "npc.barnes", ++modEntityIndex, 80, 0x8B8378, 0xED9121);
		registerEntity(EntityNpcMaskTrader.class, "npc.mask_trader", ++modEntityIndex, 80, 0x0000EE, 0x00C957);
		registerEntity(EntityNpcOrca.class, "npc.orca", ++modEntityIndex, 80, 0x0000EE, 0x9A32CD);
	}

	@SideOnly(Side.CLIENT) 
	public static void registerRenderers() {
		RenderingRegistry.registerEntityRenderingHandler(EntityArrowCustom.class, new RenderCustomArrow());
		RenderingRegistry.registerEntityRenderingHandler(EntityBomb.class, new RenderEntityBomb());
		RenderingRegistry.registerEntityRenderingHandler(EntityBoomerang.class, new RenderEntityBoomerang());
		RenderingRegistry.registerEntityRenderingHandler(EntityCeramicJar.class, new RenderEntityJar());
		RenderingRegistry.registerEntityRenderingHandler(EntityChu.class, new RenderEntityChu());
		RenderingRegistry.registerEntityRenderingHandler(EntityCyclone.class, new RenderNothing());
		RenderingRegistry.registerEntityRenderingHandler(EntityDarknut.class, new RenderGenericLiving(
				new ModelDarknut(), 0.5F, 1.5F, ModInfo.ID + ":textures/entity/darknut_standard.png"));
		RenderingRegistry.registerEntityRenderingHandler(EntityBlackKnight.class, new RenderGenericLiving(
				new ModelDarknut(), 0.5F, 1.8F, ModInfo.ID + ":textures/entity/darknut_standard.png"));
		RenderingRegistry.registerEntityRenderingHandler(EntityFairy.class, new RenderEntityFairy());
		RenderingRegistry.registerEntityRenderingHandler(EntityNavi.class, new RenderEntityFairy());
		RenderingRegistry.registerEntityRenderingHandler(EntityGoron.class, new RenderGenericLiving(
				new ModelGoron(), 0.5F, 1.5F, ModInfo.ID + ":textures/entity/goron.png"));
		RenderingRegistry.registerEntityRenderingHandler(EntityKeese.class, new RenderEntityKeese());
		RenderingRegistry.registerEntityRenderingHandler(EntityHookShot.class, new RenderEntityHookShot());
		RenderingRegistry.registerEntityRenderingHandler(EntityLeapingBlow.class, new RenderNothing());
		RenderingRegistry.registerEntityRenderingHandler(EntityMagicSpell.class, new RenderEntityMagicSpell());
		RenderingRegistry.registerEntityRenderingHandler(EntityNpcBarnes.class, new RenderGenericLiving(
				new ModelBiped(0.0F, 0.0F, 64, 64), 0.5F, 1.0F, ModInfo.ID + ":textures/entity/npc_barnes.png"));
		RenderingRegistry.registerEntityRenderingHandler(EntityNpcMaskTrader.class, new RenderGenericLiving(
				new ModelMaskSalesman(), 0.5F, 1.0F, ModInfo.ID + ":textures/entity/npc_mask_salesman.png"));
		RenderingRegistry.registerEntityRenderingHandler(EntityNpcOrca.class, new RenderGenericLiving(
				new ModelBiped(0.0F, 0.0F, 64, 64), 0.5F, 1.0F, ModInfo.ID + ":textures/entity/npc_orca.png"));
		RenderingRegistry.registerEntityRenderingHandler(EntityOctorok.class, new RenderEntityOctorok(new ModelSquid(), 0.7F));
		RenderingRegistry.registerEntityRenderingHandler(EntitySeedShot.class, new RenderSnowball(ZSSItems.dekuNut));
		RenderingRegistry.registerEntityRenderingHandler(EntitySwordBeam.class, new RenderEntitySwordBeam());
		RenderingRegistry.registerEntityRenderingHandler(EntityThrowingRock.class, new RenderSnowball(ZSSItems.throwingRock));
		RenderingRegistry.registerEntityRenderingHandler(EntityWhip.class, new RenderEntityWhip());
		RenderingRegistry.registerEntityRenderingHandler(EntityWizzrobe.class, new RenderEntityWizzrobe(new ModelWizzrobe(), 1.0F));
		RenderingRegistry.registerEntityRenderingHandler(EntityGrandWizzrobe.class, new RenderEntityWizzrobe(new ModelWizzrobe(), 1.5F));
	}

	/**
	 * Registers a tracked entity with only one variety using the given colors for the spawn egg
	 */
	public static void registerEntity(Class<? extends EntityLiving> entityClass, String name, int modEntityIndex, int trackingRange, int primaryColor, int secondaryColor) {
		EntityRegistry.registerModEntity(entityClass, name, modEntityIndex, ZSSMain.instance, trackingRange, 3, true);
		CustomEntityList.addMapping(entityClass, name, primaryColor, secondaryColor);
	}

	/**
	 * Register an entity as a spawnable entity
	 */
	private static void addSpawnableEntityData(Class<? extends EntityLiving> entity, EnumCreatureType creatureType, int min, int max, int spawnRate) {
		if (spawnableEntityData.containsKey(entity)) {
			ZSSMain.logger.warn("Spawnable entity " + entity.getName().substring(entity.getName().lastIndexOf(".") + 1) + " has already been registered!");
		} else {
			spawnableEntityData.put(entity, new SpawnableEntityData(creatureType, min, max, spawnRate));
		}
	}

	/**
	 * Adds default biomes in which the entity is allowed to spawn, if any
	 */
	private static void addSpawnLocations(Class<? extends EntityLiving> entity, String... biomes) {
		if (biomes != null && biomes.length > 0) {
			if (defaultSpawnLists.containsKey(entity)) {
				ZSSMain.logger.warn(entity.getName().substring(entity.getName().lastIndexOf(".") + 1) + " already has an array of default spawn locations!");
			} else {
				defaultSpawnLists.put(entity, biomes);
			}
		}
	}
}
