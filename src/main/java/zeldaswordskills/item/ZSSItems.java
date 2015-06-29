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

package zeldaswordskills.item;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDispenser;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumArmorMaterial;
import net.minecraft.item.EnumToolMaterial;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.MathHelper;
import net.minecraft.util.WeightedRandomChestContent;
import net.minecraft.world.World;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.ChestGenHooks;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.EnumHelper;
import zeldaswordskills.ZSSMain;
import zeldaswordskills.api.block.BlockWeight;
import zeldaswordskills.api.entity.BombType;
import zeldaswordskills.api.entity.MagicType;
import zeldaswordskills.api.item.ArmorIndex;
import zeldaswordskills.block.BlockSacredFlame;
import zeldaswordskills.block.ZSSBlocks;
import zeldaswordskills.client.render.item.RenderBigItem;
import zeldaswordskills.client.render.item.RenderHeldItemBlock;
import zeldaswordskills.client.render.item.RenderItemBomb;
import zeldaswordskills.client.render.item.RenderItemBombBag;
import zeldaswordskills.client.render.item.RenderItemCustomBow;
import zeldaswordskills.client.render.item.RenderItemDungeonBlock;
import zeldaswordskills.client.render.item.RenderItemShield;
import zeldaswordskills.creativetab.ZSSCreativeTabs;
import zeldaswordskills.entity.ZSSPlayerSkills;
import zeldaswordskills.entity.ZSSVillagerInfo;
import zeldaswordskills.entity.buff.Buff;
import zeldaswordskills.entity.mobs.EntityChu;
import zeldaswordskills.entity.mobs.EntityDarknut;
import zeldaswordskills.entity.mobs.EntityKeese;
import zeldaswordskills.entity.mobs.EntityOctorok;
import zeldaswordskills.entity.mobs.EntityWizzrobe;
import zeldaswordskills.entity.projectile.EntitySeedShot;
import zeldaswordskills.entity.projectile.EntitySeedShot.SeedType;
import zeldaswordskills.entity.projectile.EntityThrowingRock;
import zeldaswordskills.handler.TradeHandler;
import zeldaswordskills.item.ItemInstrument.Instrument;
import zeldaswordskills.item.dispenser.BehaviorDispenseCustomMobEgg;
import zeldaswordskills.lib.Config;
import zeldaswordskills.lib.ModInfo;
import zeldaswordskills.skills.SkillBase;
import zeldaswordskills.util.LogHelper;
import zeldaswordskills.util.PlayerUtils;
import zeldaswordskills.world.gen.structure.LinksHouse;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ZSSItems
{
	/** Map Item to internal ID index for Creative Tab comparator sorting to force even old saves to have correct order */
	private static final Map<Item, Integer> itemList = new HashMap<Item, Integer>(256);
	private static int sortId = 0;
	private static Comparator<Item> itemComparator = new Comparator<Item>() {
		@Override
		public int compare(Item a, Item b) {
			if (itemList.containsKey(a) && itemList.containsKey(b)) {
				return itemList.get(a) - itemList.get(b);
			} else {
				LogHelper.warning(String.format("A mod item %s or %s is missing a comparator mapping", a.getUnlocalizedName(), b.getUnlocalizedName()));
				return a.itemID - b.itemID;
			}
		}
	};
	public static Comparator<ItemStack> itemstackComparator = new Comparator<ItemStack>() {
		@Override
		public int compare(ItemStack a, ItemStack b) {
			if (a.getItem() == b.getItem()) {
				// hack for Bonus Heart ordering:
				if (a.getItem() == skillOrb && (a.getItemDamage() == SkillBase.bonusHeart.getId() || b.getItemDamage() == SkillBase.bonusHeart.getId())) {
					return (a.getItemDamage() == SkillBase.bonusHeart.getId() ? Byte.MAX_VALUE : Byte.MIN_VALUE);
				}
				return a.getItemDamage() - b.getItemDamage();
			} else {
				return itemComparator.compare(a.getItem(), b.getItem());
			}
		}
	};

	private static int modItemIndex;
	private static final int MOD_ITEM_INDEX_DEFAULT = 27653;

	/*================== GRASS DROPS =====================*/
	/** Whether special drops from grass are enabled and if so, which ones */
	private static boolean
	enableGrassArrowDrop,
	enableGrassBombDrop,
	enableGrassEmeraldDrop;

	/*================== LOOT IN VANILLA CHESTS =====================*/
	/** Random dungeon loot enable/disable (for vanilla chests only) */
	private static boolean
	enableBombLoot,
	enableBombBagLoot,
	enableHeartLoot;

	/*================== RECIPES =====================*/
	/** Whether smelting gold swords into ingots is allowed */
	private static boolean allowGoldSmelting;
	/** Enable crafting of the Wooden Hammer used to bypass wooden pegs */
	private static boolean enableCraftingHammer;
	/** Enable crafting throwing rocks from cobblestone and back */
	private static boolean enableCraftingThrowingRock;

	/** List of potential extra drops from tall grass when cut with a sword */
	private static final List<ItemStack> grassDrops = new ArrayList<ItemStack>();

	/** Material used for masks */
	public static final EnumArmorMaterial WOOD = EnumHelper.addArmorMaterial("Wood", 5, new int[] {1,3,2,1}, 5);

	/* Creative Tabs are sorted in the order that Items are declared */

	//================ SKILLS TAB ================//
	public static Item
	skillWiper,
	skillOrb,
	heartPiece;

	//================ KEYS TAB ================//
	public static Item
	keyBig,
	keySmall,
	keySkeleton;

	//================ BLOCKS TAB ================//
	public static Item doorLocked, doorLockedSmall;

	//================ TOOLS TAB ================//
	public static Item
	bomb,
	bombBag,
	magicMirror,
	crystalSpirit,
	crystalDin,
	crystalFarore,
	crystalNayru,
	dekuLeaf,
	dekuNut,
	bombFlowerSeed,
	gauntletsSilver,
	gauntletsGolden,
	hookshot,
	hookshotAddon,
	whip,
	rodFire,
	rodIce,
	rodTornado,
	fairyBottle,
	potionRed,
	potionGreen,
	potionBlue,
	potionYellow,
	rocsFeather;

	//================ TREASURES TAB ================//
	public static Item
	instrument,
	pendant,
	masterOre,
	jellyChu,
	treasure,
	skulltulaToken,
	linksHouse;

	//================ NO TAB ================//
	public static Item
	heldBlock,
	powerPiece,
	smallHeart,
	throwingRock;

	//================ COMBAT TAB ================//
	/** ZSS Armor Sets */
	public static Item
	tunicHeroHelm,
	tunicHeroChest,
	tunicHeroLegs,
	tunicHeroBoots,

	tunicGoronHelm,
	tunicGoronChest,
	tunicGoronLegs,
	//tunicGoronBoots,

	tunicZoraHelm,
	tunicZoraChest,
	tunicZoraLegs,
	tunicZoraBoots;

	/** Special Boots */
	public static Item
	bootsHeavy,
	bootsHover,
	bootsPegasus,
	bootsRubber;

	/** Zelda Shields */
	public static Item
	shieldDeku,
	shieldHylian,
	shieldMirror;

	/** Zelda Swords */
	public static Item
	swordBroken,
	swordKokiri,
	swordOrdon,
	swordGiant,
	swordBiggoron,
	swordMaster,
	swordTempered,
	swordGolden,
	swordMasterTrue,
	swordDarknut;

	/** Other Melee Weapons */
	public static Item
	hammer,
	hammerSkull,
	hammerMegaton;

	/** Ranged Weapons */
	public static Item
	boomerang,
	boomerangMagic,
	slingshot,
	scattershot,
	supershot;

	/** Hero's Bow and Arrows */
	public static Item
	heroBow,
	arrowBomb,
	arrowBombWater,
	arrowBombFire,
	arrowFire,
	arrowIce,
	arrowLight;

	//================ MASKS TAB ================//
	public static Item
	maskBlast,
	maskBunny,
	maskCouples,
	maskGerudo,
	maskGiants,
	maskGibdo,
	maskHawkeye,
	maskKeaton,
	maskScents,
	maskSkull,
	maskSpooky,
	maskStone,
	maskTruth,
	maskDeku,
	maskGoron,
	maskZora,
	maskFierce,
	maskMajora;

	//================ SPAWN EGGS TAB ================//
	public static Item
	eggSpawner, // for all Entities with only one type
	eggChu,
	eggDarknut,
	eggKeese,
	eggOctorok,
	eggWizzrobe;

	/**
	 * Initializes mod item indices from configuration file
	 */
	public static void initConfig(Configuration config) {
		modItemIndex = config.getItem("modItemIndex", MOD_ITEM_INDEX_DEFAULT).getInt() - 256;

		/*================== GRASS DROPS =====================*/
		enableGrassArrowDrop = config.get("Drops", "Enable arrow drops from grass (must use sword)", true).getBoolean(true);
		enableGrassBombDrop = config.get("Drops", "Enable bomb drops from grass (must use sword)", false).getBoolean(false);
		enableGrassEmeraldDrop = config.get("Drops", "Enable emerald drops from grass (must use sword)", true).getBoolean(true);

		/*================== LOOT IN VANILLA CHESTS =====================*/
		enableBombLoot = config.get("Loot", "Enable bombs in vanilla chests", false).getBoolean(false);
		enableBombBagLoot = config.get("Loot", "Enable bomb bags in vanilla chests", false).getBoolean(false);
		enableHeartLoot = config.get("Loot", "Enable heart pieces in vanilla chests", false).getBoolean(false);

		/*================== RECIPES =====================*/
		allowGoldSmelting = config.get("Recipes", "Smelt all those disarmed pigmen swords into gold ingots", false).getBoolean(false);
		enableCraftingHammer = config.get("Recipes", "Enable crafting of the Wooden Hammer used to bypass wooden pegs", true).getBoolean(true);
		enableCraftingThrowingRock = config.get("Recipes", "Enable crafting throwing rocks from cobblestone and back", false).getBoolean(false);
	}

	/**
	 * Call during FMLPreInitializationEvent to initialize and register all items.
	 */
	public static void preInit() {
		ZSSItems.initItems();
		ZSSItems.registerItems();
		ItemChuJelly.initializeJellies();
		ItemHeroBow.initializeArrows();
		ItemSlingshot.initializeSeeds();
		ZSSItems.addDispenserBehaviors();
	}

	/**
	 * Call during FMLInitializationEvent to register all crafting recipes.
	 */
	public static void init() {
		ZSSItems.registerRecipes();
	}

	/**
	 * Call during FMLServerStartingEvent to register trades and add loot.
	 * Delaying this until server start ensures that any block / item ID conflicts
	 * caused by other mods being added or removed will have been resolved.
	 */
	public static void onServerStarting() {
		ZSSItems.addGrassDrops();
		ZSSItems.addVanillaDungeonLoot();
		TradeHandler.registerTrades();
	}

	/**
	 * Registers all custom Item renderers
	 */
	@SideOnly(Side.CLIENT)
	public static void registerRenderers() {
		MinecraftForgeClient.registerItemRenderer(ZSSItems.bomb.itemID, new RenderItemBomb());
		MinecraftForgeClient.registerItemRenderer(ZSSItems.bombBag.itemID, new RenderItemBombBag());
		MinecraftForgeClient.registerItemRenderer(ZSSItems.hammer.itemID, new RenderBigItem(1.0F));
		MinecraftForgeClient.registerItemRenderer(ZSSItems.hammerMegaton.itemID, new RenderBigItem(1.0F));
		MinecraftForgeClient.registerItemRenderer(ZSSItems.hammerSkull.itemID, new RenderBigItem(1.0F));
		MinecraftForgeClient.registerItemRenderer(ZSSItems.swordBiggoron.itemID, new RenderBigItem(0.75F));
		MinecraftForgeClient.registerItemRenderer(ZSSItems.swordGiant.itemID, new RenderBigItem(0.75F));
		MinecraftForgeClient.registerItemRenderer(ZSSItems.swordDarknut.itemID, new RenderBigItem(0.9F));
		MinecraftForgeClient.registerItemRenderer(ZSSItems.heroBow.itemID, new RenderItemCustomBow());
		MinecraftForgeClient.registerItemRenderer(ZSSItems.shieldDeku.itemID, new RenderItemShield());
		MinecraftForgeClient.registerItemRenderer(ZSSItems.shieldHylian.itemID, new RenderItemShield());
		MinecraftForgeClient.registerItemRenderer(ZSSItems.shieldMirror.itemID, new RenderItemShield());
		//MinecraftForgeClient.registerItemRenderer(ZSSItems.hookshot.itemID, new RenderItemHookShot());

		// BLOCK ITEMS
		MinecraftForgeClient.registerItemRenderer(ZSSItems.heldBlock.itemID, new RenderHeldItemBlock());
		MinecraftForgeClient.registerItemRenderer(ZSSBlocks.dungeonCore.blockID, new RenderItemDungeonBlock());
		MinecraftForgeClient.registerItemRenderer(ZSSBlocks.dungeonStone.blockID, new RenderItemDungeonBlock());
	}

	private static void addGrassDrops() {
		for (int i = 0; i < 10; ++i) {
			grassDrops.add(new ItemStack(smallHeart));
			if (enableGrassArrowDrop && i % 3 == 2) {
				grassDrops.add(new ItemStack(Item.arrow));
			}
			if (i % 3 == 0) {
				grassDrops.add(new ItemStack(dekuNut));
			}
		}
		if (enableGrassBombDrop) {
			grassDrops.add(new ItemStack(bomb));
		}
		if (enableGrassEmeraldDrop) {
			grassDrops.add(new ItemStack(Item.emerald));
		}
	}

	/** Returns a random stack from within the grass drops list */
	public static ItemStack getRandomGrassDrop(Random rand) {
		return grassDrops.get(rand.nextInt(grassDrops.size()));
	}

	/**
	 * Gives player appropriate starting gear or returns false
	 */
	public static boolean grantBonusGear(EntityPlayer player) {
		if (!Config.enableStartingGear) {
			return false;
		}

		if (Config.enableLinksHouse) {
			player.inventory.addItemStackToInventory(new ItemStack(linksHouse));
		} else {
			if (Config.enableSword) {
				player.inventory.addItemStackToInventory(new ItemStack(swordKokiri));
			}
			if (Config.enableOrb) {
				player.inventory.addItemStackToInventory(new ItemStack(skillOrb,1,SkillBase.swordBasic.getId()));
			}
		}
		if (Config.enableFullSet) {
			ItemStack[] set = { new ItemStack(tunicHeroBoots),new ItemStack(tunicHeroLegs),
					new ItemStack(tunicHeroChest),new ItemStack(tunicHeroHelm)};
			for (int i = 0; i < set.length; ++i) {
				if (Config.enableAutoEquip && player.getCurrentArmor(i) == null) {
					player.setCurrentItemOrArmor(i + 1, set[i]);
				} else {
					player.inventory.addItemStackToInventory(set[i]);
				}
			}
		} else if (Config.enableTunic) {
			if (Config.enableAutoEquip && player.getCurrentArmor(3) == null) {
				player.setCurrentItemOrArmor(3, new ItemStack(tunicHeroChest));
			} else {
				player.inventory.addItemStackToInventory(new ItemStack(tunicHeroChest));
			}
		}
		if (Config.enableNavi) {
			ItemStack navi = new ItemStack(fairyBottle);
			navi.setItemName("Navi");
			player.inventory.addItemStackToInventory(navi);

		}
		return true;
	}

	private static void initItems() {
		// SKILL TAB ITEMS
		skillOrb = new ItemSkillOrb(modItemIndex++).setUnlocalizedName("zss.skillorb");
		heartPiece = new ItemMiscZSS(modItemIndex++,12).setUnlocalizedName("zss.heartpiece").setCreativeTab(ZSSCreativeTabs.tabSkills);

		// COMBAT TAB ITEMS
		tunicHeroHelm = new ItemArmorTunic(modItemIndex++, ZSSMain.proxy.addArmor("tunic"), ArmorIndex.TYPE_HELM).setUnlocalizedName("zss.hero_tunic_helm");
		tunicHeroChest = new ItemArmorTunic(modItemIndex++, ZSSMain.proxy.addArmor("tunic"), ArmorIndex.TYPE_CHEST).setUnlocalizedName("zss.hero_tunic_chest");
		tunicHeroLegs = new ItemArmorTunic(modItemIndex++, ZSSMain.proxy.addArmor("tunic"), ArmorIndex.TYPE_LEGS).setUnlocalizedName("zss.hero_tunic_legs");

		tunicGoronChest = new ItemArmorTunic(modItemIndex++, ZSSMain.proxy.addArmor("tunic"), ArmorIndex.TYPE_CHEST).setUnlocalizedName("zss.goron_tunic_chest");
		tunicGoronHelm = new ItemArmorTunic(modItemIndex++, ZSSMain.proxy.addArmor("tunic"), ArmorIndex.TYPE_HELM).setUnlocalizedName("zss.goron_tunic_helm");
		tunicGoronLegs = new ItemArmorTunic(modItemIndex++, ZSSMain.proxy.addArmor("tunic"), ArmorIndex.TYPE_LEGS).setUnlocalizedName("zss.goron_tunic_legs");

		tunicZoraChest = new ItemArmorTunic(modItemIndex++, ZSSMain.proxy.addArmor("tunic"), ArmorIndex.TYPE_CHEST).setEffect(new PotionEffect(Potion.waterBreathing.id, 90, 0)).setUnlocalizedName("zss.zora_tunic_chest");
		tunicZoraHelm = (new ItemArmorTunic(modItemIndex++, ZSSMain.proxy.addArmor("tunic"), ArmorIndex.TYPE_HELM) {
			@Override
			@SideOnly(Side.CLIENT)
			public void getSubItems(int item, CreativeTabs tab, List list) {
				ItemStack helm = new ItemStack(item, 1, 0);
				helm.addEnchantment(Enchantment.respiration, 3);
				list.add(helm);
			}
			@Override
			public WeightedRandomChestContent getChestGenBase(ChestGenHooks chest, Random rnd, WeightedRandomChestContent original) {
				ItemStack helm = new ItemStack(this);
				helm.addEnchantment(Enchantment.respiration, 3);
				original.theItemId = helm;
				return original;
			}
		}).setUnlocalizedName("zss.zora_tunic_helm");
		tunicZoraLegs = new ItemArmorTunic(modItemIndex++, ZSSMain.proxy.addArmor("tunic"), ArmorIndex.TYPE_LEGS).setUnlocalizedName("zss.zora_tunic_legs");

		tunicHeroBoots = new ItemArmorTunic(modItemIndex++, ZSSMain.proxy.addArmor("tunic"), ArmorIndex.TYPE_BOOTS).setUnlocalizedName("zss.hero_tunic_boots");
		bootsHeavy = new ItemArmorBoots(modItemIndex++, EnumArmorMaterial.IRON, ZSSMain.proxy.addArmor("boots")).setUnlocalizedName("zss.boots_heavy");
		bootsPegasus = new ItemArmorBoots(modItemIndex++, EnumArmorMaterial.CHAIN, ZSSMain.proxy.addArmor("boots")).setUnlocalizedName("zss.boots_pegasus");

		swordKokiri = new ItemZeldaSword(modItemIndex++, EnumToolMaterial.IRON, -1.0F).setUnlocalizedName("zss.sword_kokiri").setMaxDamage(256);
		swordOrdon = new ItemZeldaSword(modItemIndex++, EnumToolMaterial.IRON, 1.0F).setUnlocalizedName("zss.sword_ordon").setMaxDamage(512);
		swordGiant = new ItemZeldaSword(modItemIndex++, EnumToolMaterial.IRON, 6.0F, true).setUnlocalizedName("zss.sword_giant").setMaxDamage(32);
		swordBiggoron = new ItemZeldaSword(modItemIndex++, EnumToolMaterial.IRON, 6.0F, true).setNoItemOnBreak().setUnlocalizedName("zss.sword_biggoron").setMaxDamage(0);
		swordMaster = new ItemZeldaSword(modItemIndex++, EnumToolMaterial.EMERALD, 2.0F).setMasterSword().setUnlocalizedName("zss.sword_master").setMaxDamage(0);
		swordTempered = new ItemZeldaSword(modItemIndex++, EnumToolMaterial.EMERALD, 4.0F).setMasterSword().setUnlocalizedName("zss.sword_tempered").setMaxDamage(0);
		swordGolden = new ItemZeldaSword(modItemIndex++, EnumToolMaterial.EMERALD, 6.0F).setMasterSword().setUnlocalizedName("zss.sword_golden").setMaxDamage(0);
		swordMasterTrue = new ItemZeldaSword(modItemIndex++, EnumToolMaterial.EMERALD, 8.0F).setMasterSword().setUnlocalizedName("zss.sword_master_true").setMaxDamage(0);
		swordBroken = new ItemBrokenSword(modItemIndex++).setUnlocalizedName("zss.sword_broken");

		slingshot = new ItemSlingshot(modItemIndex++).setUnlocalizedName("zss.slingshot");
		scattershot = new ItemSlingshot(modItemIndex++, 3, 30F).setUnlocalizedName("zss.scattershot");
		supershot = new ItemSlingshot(modItemIndex++, 5, 15F).setUnlocalizedName("zss.supershot");

		// BLOCK TAB ITEMS
		doorLocked = new ItemDoorBoss(modItemIndex++).setUnlocalizedName("zss.doorlocked");

		// MISCELLANEOUS TAB ITEMS
		hookshot = new ItemHookShot(modItemIndex++).setUnlocalizedName("zss.hookshot");
		hookshotAddon = new ItemHookShotUpgrade(modItemIndex++).setUnlocalizedName("zss.hookshot.upgrade");
		bombBag = new ItemBombBag(modItemIndex++).setUnlocalizedName("zss.bombbag");
		bomb = new ItemBomb(modItemIndex++).setUnlocalizedName("zss.bomb");
		pendant = new ItemPendant(modItemIndex++).setUnlocalizedName("zss.pendant");
		masterOre = new ItemMasterOre(modItemIndex++,24).setUnlocalizedName("zss.masterore");
		keySmall = new ItemMiscZSS(modItemIndex++,6).setUnlocalizedName("zss.keysmall").setFull3D().setCreativeTab(ZSSCreativeTabs.tabKeys);
		keyBig = new ItemKeyBig(modItemIndex++).setUnlocalizedName("zss.keybig").setFull3D();
		keySkeleton = new ItemMiscZSS(modItemIndex++,32).setUnlocalizedName("zss.keyskeleton").setFull3D().setMaxStackSize(1).setMaxDamage(Config.getNumSkelKeyUses()).setCreativeTab(ZSSCreativeTabs.tabKeys);
		magicMirror = new ItemMagicMirror(modItemIndex++).setUnlocalizedName("zss.magicmirror");
		fairyBottle = new ItemFairyBottle(modItemIndex++).setUnlocalizedName("zss.fairybottle");
		rocsFeather = new ItemMiscZSS(modItemIndex++,12).setUnlocalizedName("zss.rocs_feather").setCreativeTab(ZSSCreativeTabs.tabTools);

		// ITEMS WITH NO TAB
		smallHeart = new ItemPickupOnly(modItemIndex++).setUnlocalizedName("zss.heart");
		throwingRock = (new Item(modItemIndex++) {
			@Override
			public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
				if (!player.capabilities.isCreativeMode) {
					--stack.stackSize;
				}
				if (!world.isRemote) {
					world.spawnEntityInWorld(new EntityThrowingRock(world, player));
				}
				return stack;
			}
		}).setUnlocalizedName("zss.throwing_rock").setTextureName(ModInfo.ID + ":throwing_rock").setMaxStackSize(18);

		// 0.5.16 NEW ITEMS
		heroBow = new ItemHeroBow(modItemIndex++).setUnlocalizedName("zss.bow_hero");
		arrowBomb = new ItemZeldaArrow(modItemIndex++, "arrow_bomb", false);
		arrowBombFire = new ItemZeldaArrow(modItemIndex++, "arrow_bomb_fire", false);
		arrowBombWater = new ItemZeldaArrow(modItemIndex++, "arrow_bomb_water", false);
		arrowFire = new ItemZeldaArrow(modItemIndex++, "arrow_fire", true);
		arrowIce = new ItemZeldaArrow(modItemIndex++, "arrow_ice", true);
		arrowLight = new ItemZeldaArrow(modItemIndex++, "arrow_light", true);

		dekuNut = (new ItemMiscZSS(modItemIndex++, 2) {
			@Override
			public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
				EntitySeedShot seedShot = new EntitySeedShot(world, player, 0.5F, 1, 0).setType(SeedType.DEKU);
				seedShot.setDamage(2.5F);
				if (!player.capabilities.isCreativeMode) {
					--stack.stackSize;
				}
				if (!world.isRemote) {
					world.spawnEntityInWorld(seedShot);
				}
				return stack;
			}
		}).setUnlocalizedName("zss.deku_nut").setCreativeTab(ZSSCreativeTabs.tabTools);

		maskHawkeye = new ItemMask(modItemIndex++, WOOD, ZSSMain.proxy.addArmor("mask")).setUnlocalizedName("zss.mask_hawkeye");
		crystalSpirit = new ItemMiscZSS(modItemIndex++, 0).setUnlocalizedName("zss.spirit_crystal_empty").setMaxStackSize(1).setCreativeTab(ZSSCreativeTabs.tabTools);
		crystalDin = new ItemSpiritCrystal(modItemIndex++, BlockSacredFlame.DIN, 8, 16).setUnlocalizedName("zss.spirit_crystal_din");
		crystalFarore = new ItemSpiritCrystal(modItemIndex++, BlockSacredFlame.FARORE, 8, 70).setUnlocalizedName("zss.spirit_crystal_farore");
		crystalNayru = new ItemSpiritCrystal(modItemIndex++, BlockSacredFlame.NAYRU, 16, 0).setUnlocalizedName("zss.spirit_crystal_nayru");

		potionRed = new ItemZeldaPotion(modItemIndex++, 0, 0.0F, 20.0F).setUnlocalizedName("zss.potion_red");
		potionGreen = new ItemZeldaPotion(modItemIndex++, 20, 40.0F, 0.0F).setUnlocalizedName("zss.potion_green");
		potionBlue = new ItemZeldaPotion(modItemIndex++, 20, 40.0F, 40.0F).setUnlocalizedName("zss.potion_blue");
		potionYellow = new ItemZeldaPotion(modItemIndex++).setBuffEffect(Buff.RESIST_SHOCK, 6000, 100, 1.0F).setUnlocalizedName("zss.potion_yellow");
		jellyChu = new ItemChuJelly(modItemIndex++).setUnlocalizedName("zss.jelly_chu");
		powerPiece = new ItemPickupOnly(modItemIndex++).setUnlocalizedName("zss.power_piece");
		bootsRubber = new ItemArmorBoots(modItemIndex++, EnumArmorMaterial.CHAIN, ZSSMain.proxy.addArmor("boots")).setUnlocalizedName("zss.boots_rubber");
		treasure = new ItemTreasure(modItemIndex++).setUnlocalizedName("zss.treasure");
		bootsHover = new ItemArmorBoots(modItemIndex++, EnumArmorMaterial.CHAIN, ZSSMain.proxy.addArmor("boots")).setUnlocalizedName("zss.boots_hover");
		dekuLeaf = new ItemDekuLeaf(modItemIndex++).setUnlocalizedName("zss.deku_leaf");
		boomerang = new ItemBoomerang(modItemIndex++, 4.0F, 12).setUnlocalizedName("zss.boomerang");
		boomerangMagic = new ItemBoomerang(modItemIndex++, 6.0F, 24).setCaptureAll().setUnlocalizedName("zss.boomerang_magic");

		// 0.5.17 NEW ITEMS:
		heldBlock = new ItemHeldBlock(modItemIndex++).setUnlocalizedName("zss.held_block");
		gauntletsSilver = new ItemPowerGauntlets(modItemIndex++, BlockWeight.MEDIUM).setUnlocalizedName("zss.gauntlets_silver");
		gauntletsGolden = new ItemPowerGauntlets(modItemIndex++, BlockWeight.VERY_HEAVY).setUnlocalizedName("zss.gauntlets_golden");

		// MASK TAB ITEMS
		maskBlast = new ItemMask(modItemIndex++, EnumArmorMaterial.IRON, ZSSMain.proxy.addArmor("mask")).setUnlocalizedName("zss.mask_blast");
		// can't use CLOTH as it expects an overlay and crashes when rendering
		maskBunny = new ItemMask(modItemIndex++, WOOD, ZSSMain.proxy.addArmor("mask")).setPrice(1, 64).setUnlocalizedName("zss.mask_bunny");
		maskCouples = (new ItemMask(modItemIndex++, WOOD, ZSSMain.proxy.addArmor("mask")) {
			@Override
			public void onArmorTickUpdate(World world, EntityPlayer player, ItemStack stack) {
				super.onArmorTickUpdate(world, player, stack);
				if (world.getWorldTime() % 64 == 0) {
					List<EntityVillager> villagers = world.getEntitiesWithinAABB(EntityVillager.class, player.boundingBox.expand(8.0D, 3.0D, 8.0D));
					for (EntityVillager villager : villagers) {
						if (world.rand.nextFloat() < 0.5F) {
							ZSSVillagerInfo.get(villager).setMating();
						}
					}
				}
			}
		}).setPrice(40, 32).setUnlocalizedName("zss.mask_couples");
		maskGerudo = new ItemMask(modItemIndex++, WOOD, ZSSMain.proxy.addArmor("mask")).setUnlocalizedName("zss.mask_gerudo");
		maskGiants = new ItemMask(modItemIndex++, WOOD, ZSSMain.proxy.addArmor("mask")).setUnlocalizedName("zss.mask_giants");
		maskGibdo = new ItemMask(modItemIndex++, WOOD, ZSSMain.proxy.addArmor("mask")).setUnlocalizedName("zss.mask_gibdo");
		maskKeaton = new ItemMask(modItemIndex++, WOOD, ZSSMain.proxy.addArmor("mask")).setPrice(8, 16).setUnlocalizedName("zss.mask_keaton");
		maskScents = new ItemMask(modItemIndex++, WOOD, ZSSMain.proxy.addArmor("mask")).setPrice(32, 32).setUnlocalizedName("zss.mask_scents");
		maskSkull = new ItemMask(modItemIndex++, WOOD, ZSSMain.proxy.addArmor("mask")).setPrice(20, 10).setUnlocalizedName("zss.mask_skull");
		maskSpooky = new ItemMask(modItemIndex++, WOOD, ZSSMain.proxy.addArmor("mask")).setPrice(16, 8).setUnlocalizedName("zss.mask_spooky");
		maskStone = new ItemMask(modItemIndex++, WOOD, ZSSMain.proxy.addArmor("mask")).setEffect(new PotionEffect(Potion.invisibility.id, 100, 0)).setUnlocalizedName("zss.mask_stone");
		maskTruth = new ItemMask(modItemIndex++, WOOD, ZSSMain.proxy.addArmor("mask")).setUnlocalizedName("zss.mask_truth");
		maskDeku = new ItemMask(modItemIndex++, WOOD, ZSSMain.proxy.addArmor("mask")).setUnlocalizedName("zss.mask_deku");
		maskGoron = new ItemMask(modItemIndex++, WOOD, ZSSMain.proxy.addArmor("mask")).setUnlocalizedName("zss.mask_goron");
		maskZora = new ItemMaskZora(modItemIndex++, WOOD, ZSSMain.proxy.addArmor("mask")).setUnlocalizedName("zss.mask_zora");
		maskFierce = new ItemMask(modItemIndex++, WOOD, ZSSMain.proxy.addArmor("mask")).setUnlocalizedName("zss.mask_fierce");

		hammer = new ItemHammer(modItemIndex++, BlockWeight.VERY_LIGHT, 8.0F, 50.0F).setUnlocalizedName("zss.hammer");
		hammerSkull = new ItemHammer(modItemIndex++, BlockWeight.MEDIUM, 12.0F, 50.0F).setUnlocalizedName("zss.hammer_skull");
		hammerMegaton = new ItemHammer(modItemIndex++, BlockWeight.VERY_HEAVY, 16.0F, 50.0F).setUnlocalizedName("zss.hammer_megaton");

		skillWiper = (new ItemMiscZSS(modItemIndex++, 0) {
			@Override
			public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
				if (!world.isRemote) {
					PlayerUtils.sendTranslatedChat(player, "chat.zss.skill.reset");
					ZSSPlayerSkills.get(player).resetSkills();
				}
				return stack;
			}
		}).setUnlocalizedName("zss.skill_wiper").setCreativeTab(ZSSCreativeTabs.tabSkills);

		// 0.6.1 new items
		shieldDeku = new ItemZeldaShield(modItemIndex++, EnumToolMaterial.WOOD, 0.25F, 30, 3F, 5F).setUnlocalizedName("zss.shield_deku");
		shieldHylian = new ItemZeldaShield(modItemIndex++, EnumToolMaterial.IRON, 0.5F, 18, 5F, 3.5F).setUnlocalizedName("zss.shield_hylian");
		shieldMirror = new ItemZeldaShield(modItemIndex++, EnumToolMaterial.EMERALD, 0.75F, 24, 4F, 4F).setUnlocalizedName("zss.shield_mirror");

		maskMajora = new ItemMask(modItemIndex++, WOOD, ZSSMain.proxy.addArmor("mask")).setEffect(new PotionEffect(Potion.wither.id, 100, 1)).setUnlocalizedName("zss.mask_majora");

		// 0.6.5 new items
		rodFire = new ItemMagicRod(modItemIndex++, MagicType.FIRE, 8.0F, 8.0F).setUnlocalizedName("zss.rod_fire");
		rodIce = new ItemMagicRod(modItemIndex++, MagicType.ICE, 6.0F, 8.0F).setUnlocalizedName("zss.rod_ice");
		rodTornado = new ItemMagicRod(modItemIndex++, MagicType.WIND, 4.0F, 4.0F).setUnlocalizedName("zss.rod_tornado");

		// Custom Spawn Eggs
		eggSpawner = new ItemCustomEgg(modItemIndex++).setUnlocalizedName("zss.spawn_egg");
		eggChu = new ItemCustomVariantEgg(modItemIndex++, EntityChu.class, "chu").setUnlocalizedName("zss.eggChu");
		eggKeese = new ItemCustomVariantEgg(modItemIndex++, EntityKeese.class, "keese").setUnlocalizedName("zss.eggKeese");
		eggOctorok = new ItemCustomVariantEgg(modItemIndex++, EntityOctorok.class, "octorok").setUnlocalizedName("zss.eggOctorok");

		// Post v1.0 items:
		whip = new ItemWhip(modItemIndex++).setUnlocalizedName("zss.whip");
		tunicZoraBoots = (new ItemArmorTunic(modItemIndex++, ZSSMain.proxy.addArmor("tunic"), ArmorIndex.TYPE_BOOTS) {
			@Override
			public void onArmorTickUpdate(World world, EntityPlayer player, ItemStack stack) {
				int i = MathHelper.floor_double(player.posX);
				int j = MathHelper.floor_double(player.boundingBox.minY);
				int k = MathHelper.floor_double(player.posZ);
				Material m = world.getBlockMaterial(i, j + 1, k);
				if (m.isLiquid() && m != Material.lava && !player.onGround && !player.capabilities.isFlying) {
					if (((player.motionX * player.motionX) + (player.motionZ * player.motionZ)) < 1.65D) {
						player.motionX *= 1.115D;
						player.motionZ *= 1.115D;
					}
				} else if (!m.isLiquid() && player.onGround && !player.capabilities.isFlying) {
					player.motionX *= 0.125D;
					player.motionZ *= 0.125D;
					if (player.isSprinting()) {
						player.setSprinting(false);
					}
				}
			}
		}).setUnlocalizedName("zss.zora_tunic_boots");
		doorLockedSmall = new ItemDoorLocked(modItemIndex++, ZSSBlocks.doorLockedSmall).setUnlocalizedName("zss.door_locked_small");
		linksHouse = new ItemBuilderSeed(modItemIndex++, LinksHouse.class, "You must first clear this area of debris!", "deku_nut").setUnlocalizedName("zss.links_house");
		bombFlowerSeed = new ItemBombFlowerSeed(modItemIndex++).setUnlocalizedName("zss.seed_bomb_flower");
		swordDarknut = new ItemZeldaSword(modItemIndex++, EnumToolMaterial.IRON, 1.0F, true, 20, 0.5F).setUnlocalizedName("zss.sword_darknut").setMaxDamage(768);
		eggDarknut = new ItemCustomVariantEgg(modItemIndex++, EntityDarknut.class, "darknut").setUnlocalizedName("zss.eggDarknut");
		eggWizzrobe = new ItemCustomVariantEgg(modItemIndex++, EntityWizzrobe.class, "wizzrobe").setUnlocalizedName("zss.eggWizzrobe");
		instrument = new ItemInstrument(modItemIndex++);
		skulltulaToken = (new ItemMiscZSS(modItemIndex++, 20) {
			@Override
			protected void handleTrade(ItemStack stack, EntityPlayer player, EntityVillager villager) {
				ZSSVillagerInfo villagerInfo = ZSSVillagerInfo.get(villager);
				if (villager.getClass().isAssignableFrom(EntityVillager.class) && ("Cursed Man").equals(villager.getCustomNameTag())) {
					villagerInfo.handleSkulltulaTrade(stack, player);
				} else if (villager.isChild()) {
					PlayerUtils.sendTranslatedChat(player, "chat.zss.trade.generic.child");
				} else if (villagerInfo.isHunter()) {
					villagerInfo.addHunterTrade(player, new ItemStack(this), sellPrice);
				} else {
					int i = villager.getProfession();
					PlayerUtils.sendTranslatedChat(player, "chat.zss.skulltula_token.villager." + (i > 4 ? "custom" : i));
				}
			}
		}).setUnlocalizedName("zss.skulltula_token");
	}

	/**
	 * Registers an ItemBlock to the item sorter for creative tabs sorting
	 */
	public static void registerItemBlock(Item block) {
		if (block instanceof ItemBlock) {
			itemList.put(block, sortId++);
		} else {
			LogHelper.warning("Tried to register a non-ItemBlock item for " + block.getUnlocalizedName());
		}
	}

	private static void registerItems() {
		try {
			for (Field f: ZSSItems.class.getFields()) {
				if (Item.class.isAssignableFrom(f.getType())) {
					Item item = (Item) f.get(null);
					if (item != null) {
						itemList.put(item, sortId++);
						GameRegistry.registerItem(item, item.getUnlocalizedName().substring(5).trim());
					}
				}
			}
		} catch (Exception e) {

		}
	}

	private static void registerRecipes() {
		if (allowGoldSmelting) {
			FurnaceRecipes.smelting().addSmelting(Item.swordGold.itemID, new ItemStack(Item.ingotGold), 0.0F);
		}
		if (enableCraftingHammer) {
			GameRegistry.addRecipe(new ItemStack(hammer), "lll"," s "," s ", 'l', Block.wood, 's', Item.stick);
		}
		if (enableCraftingThrowingRock) {
			GameRegistry.addShapelessRecipe(new ItemStack(throwingRock, 9), Block.cobblestone);
			GameRegistry.addRecipe(new ItemStack(Block.cobblestone), "rrr", "rrr", "rrr", 'r', throwingRock);
		}
		GameRegistry.addRecipe(new ItemStack(ZSSBlocks.pedestal,3,0x8), "qqq","qpq","qqq", 'q', Block.blockNetherQuartz, 'p', new ItemStack(ZSSBlocks.pedestal,1,0x8));
		GameRegistry.addRecipe(new ItemStack(ZSSBlocks.beamWooden), "b","b","b", 'b', Block.planks);
		GameRegistry.addRecipe(new ItemStack(ZSSBlocks.gossipStone), " s ","sos"," s ", 's', Block.stone, 'o', new ItemStack(ZSSItems.instrument, 1, Instrument.OCARINA_FAIRY.ordinal()));
		GameRegistry.addRecipe(new ItemStack(ZSSBlocks.hookTarget), " c ","bab"," b ", 'a', Item.redstone, 'b', Block.stone, 'c', Block.fenceIron);
		GameRegistry.addRecipe(new ItemStack(ZSSBlocks.hookTargetAll), "bcb", "cac", "bcb", 'a', Item.redstone, 'b', Block.stone, 'c', Block.fenceIron);
		GameRegistry.addShapelessRecipe(new ItemStack(arrowBomb), new ItemStack(bomb, 1, BombType.BOMB_STANDARD.ordinal()), Item.arrow);
		GameRegistry.addShapelessRecipe(new ItemStack(arrowBombFire), new ItemStack(bomb, 1, BombType.BOMB_FIRE.ordinal()), Item.arrow);
		GameRegistry.addShapelessRecipe(new ItemStack(arrowBombWater), new ItemStack(bomb, 1, BombType.BOMB_WATER.ordinal()), Item.arrow);
		GameRegistry.addRecipe(new ItemStack(ZSSBlocks.ceramicJar,8), "c c","c c"," c ", 'c', Item.brick);
		GameRegistry.addRecipe(new ItemStack(ZSSItems.skillOrb, 1, SkillBase.bonusHeart.getId()), "HH","HH", 'H', heartPiece);
		GameRegistry.addRecipe(new ItemStack(ZSSItems.instrument, 1, ItemInstrument.Instrument.OCARINA_FAIRY.ordinal()), " c ","crc", 'c', Item.clay, 'r', Item.reed);
		GameRegistry.addShapelessRecipe(new ItemStack(tunicGoronLegs), tunicHeroLegs, new ItemStack(Item.dyePowder, 1, 1));
		GameRegistry.addShapelessRecipe(new ItemStack(tunicGoronLegs), tunicZoraLegs, new ItemStack(Item.dyePowder, 1, 1));
		GameRegistry.addShapelessRecipe(new ItemStack(tunicHeroLegs), tunicGoronLegs, new ItemStack(Item.dyePowder, 1, 2));
		GameRegistry.addShapelessRecipe(new ItemStack(tunicHeroLegs), tunicZoraLegs, new ItemStack(Item.dyePowder, 1, 2));
		GameRegistry.addShapelessRecipe(new ItemStack(tunicZoraLegs), tunicGoronLegs, new ItemStack(Item.dyePowder, 1, 4));
		GameRegistry.addShapelessRecipe(new ItemStack(tunicZoraLegs), tunicHeroLegs, new ItemStack(Item.dyePowder, 1, 4));
	}

	/**
	 * Adds some special loot to vanilla chests
	 */
	private static void addVanillaDungeonLoot() {
		if (enableBombLoot) {
			addLootToAll(new WeightedRandomChestContent(new ItemStack(bomb, 1, BombType.BOMB_STANDARD.ordinal()), 1, 3, Config.getBombWeight()), true, true);
		}
		if (enableBombBagLoot) {
			addLootToAll(new WeightedRandomChestContent(new ItemStack(bombBag), 1, 1, Config.getBombBagWeight()), true, false);
		}
		if (enableHeartLoot) {
			addLootToAll(new WeightedRandomChestContent(new ItemStack(skillOrb, 1, SkillBase.bonusHeart.getId()), 1, 1, Config.getHeartWeight()), false, false);
		}
	}

	/**
	 * Adds weighted chest contents to all ChestGenHooks, with possible exception of blacksmith and Bonus Chest
	 */
	private static void addLootToAll(WeightedRandomChestContent loot, boolean smith, boolean bonus) {
		ChestGenHooks.getInfo(ChestGenHooks.MINESHAFT_CORRIDOR).addItem(loot);
		ChestGenHooks.getInfo(ChestGenHooks.PYRAMID_DESERT_CHEST).addItem(loot);
		ChestGenHooks.getInfo(ChestGenHooks.PYRAMID_JUNGLE_CHEST).addItem(loot);
		ChestGenHooks.getInfo(ChestGenHooks.STRONGHOLD_CORRIDOR).addItem(loot);
		ChestGenHooks.getInfo(ChestGenHooks.STRONGHOLD_LIBRARY).addItem(loot);
		ChestGenHooks.getInfo(ChestGenHooks.STRONGHOLD_CROSSING).addItem(loot);
		ChestGenHooks.getInfo(ChestGenHooks.DUNGEON_CHEST).addItem(loot);
		if (smith) {
			ChestGenHooks.getInfo(ChestGenHooks.VILLAGE_BLACKSMITH).addItem(loot);
		}
		if (bonus) {
			ChestGenHooks.getInfo(ChestGenHooks.BONUS_CHEST).addItem(loot);
		}
	}

	private static void addDispenserBehaviors() {
		BlockDispenser.dispenseBehaviorRegistry.putObject(eggSpawner, new BehaviorDispenseCustomMobEgg());
		BlockDispenser.dispenseBehaviorRegistry.putObject(eggChu, new BehaviorDispenseCustomMobEgg());
		BlockDispenser.dispenseBehaviorRegistry.putObject(eggDarknut, new BehaviorDispenseCustomMobEgg());
		BlockDispenser.dispenseBehaviorRegistry.putObject(eggKeese, new BehaviorDispenseCustomMobEgg());
		BlockDispenser.dispenseBehaviorRegistry.putObject(eggOctorok, new BehaviorDispenseCustomMobEgg());
		BlockDispenser.dispenseBehaviorRegistry.putObject(eggWizzrobe, new BehaviorDispenseCustomMobEgg());
	}
}
