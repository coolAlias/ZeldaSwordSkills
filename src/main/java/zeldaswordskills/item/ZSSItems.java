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

package zeldaswordskills.item;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import net.minecraft.block.BlockDispenser;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.Item.ToolMaterial;
import net.minecraft.item.ItemArmor.ArmorMaterial;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.BlockPos;
import net.minecraft.util.StatCollector;
import net.minecraft.util.WeightedRandomChestContent;
import net.minecraft.world.World;
import net.minecraftforge.common.ChestGenHooks;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.fml.common.registry.GameData;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import zeldaswordskills.ZSSMain;
import zeldaswordskills.api.block.BlockWeight;
import zeldaswordskills.api.block.IHookable;
import zeldaswordskills.api.entity.BombType;
import zeldaswordskills.api.entity.MagicType;
import zeldaswordskills.api.item.ArmorIndex;
import zeldaswordskills.block.BlockSacredFlame;
import zeldaswordskills.block.ZSSBlocks;
import zeldaswordskills.creativetab.ZSSCreativeTabs;
import zeldaswordskills.entity.ZSSEntityInfo;
import zeldaswordskills.entity.buff.Buff;
import zeldaswordskills.entity.player.ZSSPlayerInfo;
import zeldaswordskills.entity.player.ZSSPlayerSkills;
import zeldaswordskills.entity.projectile.EntitySeedShotDeku;
import zeldaswordskills.entity.projectile.EntityThrowingRock;
import zeldaswordskills.handler.TradeHandler;
import zeldaswordskills.item.ItemInstrument.Instrument;
import zeldaswordskills.item.crafting.RecipeBagToBombArrows;
import zeldaswordskills.item.crafting.RecipeCombineBombBag;
import zeldaswordskills.ref.Config;
import zeldaswordskills.ref.ModInfo;
import zeldaswordskills.ref.Sounds;
import zeldaswordskills.skills.SkillBase;
import zeldaswordskills.util.PlayerUtils;
import zeldaswordskills.world.gen.structure.LinksHouse;

public class ZSSItems
{
	/** Map Item to internal ID index for Creative Tab comparator sorting to force even old saves to have correct order */
	private static final Map<Item, Integer> itemList = new HashMap<Item, Integer>(256);
	private static int sortId = 0;
	/** List of items added by other mods that are scheduled to have comparator mappings added */
	private static final List<Item> addonItems = new ArrayList<Item>();
	private static Comparator<Item> itemComparator = new Comparator<Item>() {
		@Override
		public int compare(Item a, Item b) {
			if (itemList.containsKey(a) && itemList.containsKey(b)) {
				return itemList.get(a) - itemList.get(b);
			} else {
				ZSSMain.logger.warn("A mod item " + a.getUnlocalizedName() + " or " + b.getUnlocalizedName() + " is missing a comparator mapping");
				return GameData.getItemRegistry().getId(a) - GameData.getItemRegistry().getId(b);
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
	/** Enable crafting recipe to make copies of the Book of Mudora */
	private static boolean enableCraftingMudora;
	/** Enable crafting throwing rocks from cobblestone and back */
	private static boolean enableCraftingThrowingRock;

	/** List of potential extra drops from tall grass when cut with a sword */
	private static final List<ItemStack> grassDrops = new ArrayList<ItemStack>();

	/** Material used for masks */
	public static final ArmorMaterial WOOD = EnumHelper.addArmorMaterial("Wood", "FakeTexture", 5, new int[] {1,3,2,1}, 5);

	/* Creative Tabs are sorted in the order that Items are declared */
	//================ SKILLS TAB ================//
	public static Item
	skillWiper,
	skillOrb,
	heartPiece;

	//================ KEYS TAB ================//
	public static Item
	keySkeleton,
	keyBig,
	keySmall;

	//================ TOOLS TAB ================//
	public static Item
	bomb,
	bombBag,
	magicMirror,
	crystalSpirit,
	crystalDin,
	crystalFarore,
	crystalNayru,
	medallion,
	dekuLeaf,
	dekuNut,
	bombFlowerSeed,
	gauntletsSilver,
	gauntletsGolden,
	hookshot,
	hookshotUpgrade,
	whip,
	rodFire,
	rodIce,
	rodTornado,
	fairyBottle,
	potionRed,
	potionGreen,
	potionBlue,
	potionYellow,
	potionPurple,
	lonlonMilk,
	lonlonSpecial,
	magicJar,
	magicJarBig,
	magicContainer,
	rocsFeather;

	//================ TREASURES TAB ================//
	public static Item
	instrument,
	bookMudora,
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
	arrowLight,
	arrowSilver;

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
	public static Item eggSpawner; // egg item for all ZSS spawnable entities

	/**
	 * Initializes mod item indices from configuration file
	 */
	public static void initConfig(Configuration config) {
		/*================== GRASS DROPS =====================*/
		//TODO add keys when Drops is added to ConfigGui
		enableGrassArrowDrop = config.getBoolean("Grass Can Drop Arrows", "drops", true, "Enable arrow drops from grass (must use sword)", "config.zss.drops.enable_grass_arrow_drop");
		enableGrassBombDrop = config.getBoolean("Grass Can Drop Bombs", "drops", false, "Enable bomb drops from grass (must use sword)", "config.zss.drops.enable_grass_bomb_drop");
		enableGrassEmeraldDrop = config.getBoolean("Bombs Can Drop Emeralds", "drops", true, "Enable emerald drops from grass (must use sword)", "config.zss.drops.enable_grass_emerald_drop");

		/*================== LOOT IN VANILLA CHESTS =====================*/
		enableBombLoot = config.getBoolean("Bombs in Chests", "loot", false, "Enable bombs in vanilla chests", "config.zss.loot.enable_bomb_loot");
		enableBombBagLoot = config.getBoolean("Bomb Bags in Chests", "loot", false, "Enable bomb bags in vanilla chests", "config.zss.loot.enable_bomb_bag_loot");
		enableHeartLoot = config.getBoolean("Heart Pieces in Chests", "loot", false, "Enable heart pieces in vanilla chests", "config.zss.loot.enable_heart_loot");

		/*================== RECIPES =====================*/
		allowGoldSmelting = config.getBoolean("Can Smelt Pigmen Swords", "recipes", false, "Smelt all those disarmed pigmen swords into gold ingots", "config.zss.recipes.allow_gold_smelting");
		enableCraftingHammer = config.getBoolean("Can Craft the Wooden Hammer", "recipes", true, "Enable crafting of the Wooden Hammer used to bypass wooden pegs", "config.zss.recipes.enable_crafting_hammer");
		enableCraftingMudora = config.getBoolean("Can Copy Book of Mudora by Crafting", "recipes", true, "Enable crafting recipe to make copies of the Book of Mudora", "config.zss.recipes.enable_crafting_mudora");
		enableCraftingThrowingRock = config.getBoolean("Can Craft Throwing Rocks", "recipes", false, "Enable crafting throwing rocks from cobblestone and back", "config.zss.recipes.enable_crafting_throwing_rock");
	}

	/**
	 * Call during FMLPreInitializationEvent to initialize and register all items.
	 */
	public static void preInit() {
		ZSSItems.initItems();
		ZSSItems.registerItems();
	}

	/**
	 * Call during FMLInitializationEvent to register all crafting recipes.
	 */
	public static void init() {
		ZSSItems.registerRecipes();
		ItemHeroBow.initializeArrows();
		ItemSlingshot.initializeSeeds();
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
		// Register mappings for all addon items now, so ZSS items always appear first
		for (Item item : addonItems) {
			ZSSItems.registerItemComparatorMapping(item);
		}
	}

	private static void addGrassDrops() {
		for (int i = 0; i < 10; ++i) {
			grassDrops.add(new ItemStack(smallHeart));
			if (enableGrassArrowDrop && i % 3 == 2) {
				grassDrops.add(new ItemStack(Items.arrow));
			}
			if (i % 3 == 0) {
				grassDrops.add(new ItemStack(dekuNut));
			}
		}
		if (enableGrassBombDrop) {
			grassDrops.add(new ItemStack(bomb));
		}
		if (enableGrassEmeraldDrop) {
			grassDrops.add(new ItemStack(Items.emerald));
		}
	}

	/** Returns a random stack from within the grass drops list */
	public static ItemStack getRandomGrassDrop(Random rand) {
		return grassDrops.get(rand.nextInt(grassDrops.size())).copy();
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
			navi.setStackDisplayName("Navi");
			player.inventory.addItemStackToInventory(navi);

		}
		return true;
	}

	private static void initItems() {
		//===================== SKILL TAB =====================//
		skillOrb = new ItemSkillOrb().setUnlocalizedName("skillorb");
		heartPiece = new ItemMiscZSS(12).setUnlocalizedName("heart_piece").setCreativeTab(ZSSCreativeTabs.tabSkills);
		skillWiper = (new ItemMiscZSS(0) {
			@Override
			public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
				if (!world.isRemote) {
					PlayerUtils.sendTranslatedChat(player, "chat.zss.skill.reset");
					ZSSPlayerSkills.get(player).resetSkills();
				}
				return stack;
			}
		}).setUnlocalizedName("skill_wiper").setCreativeTab(ZSSCreativeTabs.tabSkills);

		//===================== COMBAT TAB =====================//
		// ARMOR SETS
		tunicHeroHelm = new ItemArmorTunic(ZSSMain.proxy.addArmor("tunic"), ArmorIndex.TYPE_HELM).setUnlocalizedName("hero_tunic_helm");
		tunicHeroChest = new ItemArmorTunic(ZSSMain.proxy.addArmor("tunic"), ArmorIndex.TYPE_CHEST).setUnlocalizedName("hero_tunic_chest");
		tunicHeroLegs = new ItemArmorTunic(ZSSMain.proxy.addArmor("tunic"), ArmorIndex.TYPE_LEGS).setUnlocalizedName("hero_tunic_legs");
		tunicHeroBoots = new ItemArmorTunic(ZSSMain.proxy.addArmor("tunic"), ArmorIndex.TYPE_BOOTS).setUnlocalizedName("hero_tunic_boots");

		tunicGoronHelm = new ItemArmorTunic(ZSSMain.proxy.addArmor("tunic"), ArmorIndex.TYPE_HELM).setUnlocalizedName("goron_tunic_helm");
		tunicGoronChest = new ItemArmorTunic(ZSSMain.proxy.addArmor("tunic"), ArmorIndex.TYPE_CHEST).setUnlocalizedName("goron_tunic_chest");
		tunicGoronLegs = new ItemArmorTunic(ZSSMain.proxy.addArmor("tunic"), ArmorIndex.TYPE_LEGS).setUnlocalizedName("goron_tunic_legs");

		tunicZoraHelm = (new ItemArmorTunic(ZSSMain.proxy.addArmor("tunic"), ArmorIndex.TYPE_HELM) {
			@Override
			@SideOnly(Side.CLIENT)
			public void getSubItems(Item item, CreativeTabs tab, List<ItemStack> list) {
				ItemStack helm = new ItemStack(item);
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
		}).setUnlocalizedName("zora_tunic_helm");
		tunicZoraChest = new ItemArmorTunic(ZSSMain.proxy.addArmor("tunic"), ArmorIndex.TYPE_CHEST).setEffect(new PotionEffect(Potion.waterBreathing.getId(), 90, 0)).setUnlocalizedName("zora_tunic_chest");
		tunicZoraLegs = new ItemArmorTunic(ZSSMain.proxy.addArmor("tunic"), ArmorIndex.TYPE_LEGS).setUnlocalizedName("zora_tunic_legs");
		tunicZoraBoots = (new ItemArmorTunic(ZSSMain.proxy.addArmor("tunic"), ArmorIndex.TYPE_BOOTS) {
			@Override
			public void onArmorTick(World world, EntityPlayer player, ItemStack stack) {
				Material m = world.getBlockState(new BlockPos(player).up()).getBlock().getMaterial();
				if (m.isLiquid() && m != Material.lava && !player.onGround && !player.capabilities.isFlying) {
					if (((player.motionX * player.motionX) + (player.motionZ * player.motionZ)) < 1.5D) {
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
		}).setUnlocalizedName("zora_tunic_boots");

		// BOOTS
		bootsHeavy = new ItemArmorBoots.ItemHeavyBoots(ArmorMaterial.IRON, ZSSMain.proxy.addArmor("boots"), "minecraft:textures/models/armor/iron_layer_1.png").setUnlocalizedName("boots_heavy");
		bootsHover = new ItemArmorBoots.ItemHoverBoots(ArmorMaterial.CHAIN, ZSSMain.proxy.addArmor("boots"), ModInfo.ID + ":textures/armor/mask_hawkeye_layer_1.png").setUnlocalizedName("boots_hover");
		bootsPegasus = new ItemArmorBoots.ItemPegasusBoots(ArmorMaterial.CHAIN, ZSSMain.proxy.addArmor("boots"), ModInfo.ID + ":textures/armor/hero_tunic_layer_1.png").setUnlocalizedName("boots_pegasus");
		bootsRubber = new ItemArmorBoots.ItemRubberBoots(ArmorMaterial.CHAIN, ZSSMain.proxy.addArmor("boots"), ModInfo.ID + ":textures/armor/boots_rubber_layer_1.png").setUnlocalizedName("boots_rubber");

		// SHIELDS
		shieldDeku = new ItemZeldaShield(ToolMaterial.WOOD, 0.25F, 30, 3F, 5F).setUnlocalizedName("shield_deku");
		shieldHylian = new ItemZeldaShield(ToolMaterial.IRON, 0.5F, 18, 5F, 3.5F).setUnlocalizedName("shield_hylian");
		shieldMirror = new ItemZeldaShield(ToolMaterial.EMERALD, 0.75F, 24, 4F, 4F).setUnlocalizedName("shield_mirror");

		// SWORDS
		swordKokiri = new ItemZeldaSword(ToolMaterial.IRON, -1.0F).setUnlocalizedName("sword_kokiri").setMaxDamage(256);
		swordOrdon = new ItemZeldaSword(ToolMaterial.IRON, 1.0F).setUnlocalizedName("sword_ordon").setMaxDamage(512);
		swordGiant = new ItemZeldaSword(ToolMaterial.IRON, 6.0F, true).setUnlocalizedName("sword_giant").setMaxDamage(32);
		swordBiggoron = new ItemZeldaSword(ToolMaterial.IRON, 6.0F, true).setNoItemOnBreak().setUnlocalizedName("sword_biggoron").setMaxDamage(0);
		swordMaster = new ItemZeldaSword(ToolMaterial.EMERALD, 2.0F).setMasterSword().setUnlocalizedName("sword_master").setMaxDamage(0);
		swordTempered = new ItemZeldaSword(ToolMaterial.EMERALD, 4.0F).setMasterSword().setUnlocalizedName("sword_tempered").setMaxDamage(0);
		swordGolden = new ItemZeldaSword(ToolMaterial.EMERALD, 6.0F).setMasterSword().setUnlocalizedName("sword_golden").setMaxDamage(0);
		swordMasterTrue = new ItemZeldaSword(ToolMaterial.EMERALD, 8.0F).setMasterSword().setUnlocalizedName("sword_master_true").setMaxDamage(0);
		swordDarknut = new ItemZeldaSword(ToolMaterial.IRON, 1.0F, true, 20, 0.5F).setUnlocalizedName("sword_darknut").setMaxDamage(768);
		swordBroken = new ItemBrokenSword().setUnlocalizedName("sword_broken");

		// HAMMERS
		hammer = new ItemHammer(BlockWeight.VERY_LIGHT, 8.0F, 50.0F).setUnlocalizedName("hammer");
		hammerSkull = new ItemHammer(BlockWeight.MEDIUM, 12.0F, 50.0F).setUnlocalizedName("hammer_skull");
		hammerMegaton = new ItemHammer(BlockWeight.VERY_HEAVY, 16.0F, 50.0F).setUnlocalizedName("hammer_megaton");

		// BOOMERANGS
		boomerang = new ItemBoomerang(4.0F, 12).setUnlocalizedName("boomerang");
		boomerangMagic = new ItemBoomerang(6.0F, 24).setCaptureAll().setUnlocalizedName("boomerang_magic");

		// BOWS & ARROWS
		heroBow = new ItemHeroBow().setUnlocalizedName("bow_hero");
		arrowBomb = new ItemZeldaArrow("arrow_bomb", 1);
		arrowBombFire = new ItemZeldaArrow("arrow_bomb_fire", 1);
		arrowBombWater = new ItemZeldaArrow("arrow_bomb_water", 1);
		arrowFire = new ItemZeldaArrow.ItemMagicArrow("arrow_fire", 2, 2.5F);
		arrowIce = new ItemZeldaArrow.ItemMagicArrow("arrow_ice", 2, 2.5F);
		arrowLight = new ItemZeldaArrow.ItemMagicArrow("arrow_light", 3, 5.0F);
		arrowSilver = new ItemZeldaArrow("arrow_silver", 3);

		// SLINGSHOTS
		slingshot = new ItemSlingshot().setUnlocalizedName("slingshot");
		scattershot = new ItemSlingshot(3, 30F).setUnlocalizedName("scattershot");
		supershot = new ItemSlingshot(5, 15F).setUnlocalizedName("supershot");

		//===================== KEYS TAB =====================//
		keyBig = new ItemKeyBig().setUnlocalizedName("key_big").setFull3D();
		keySmall = new ItemMiscZSS(6).setUnlocalizedName("key_small").setFull3D().setCreativeTab(ZSSCreativeTabs.tabKeys);
		keySkeleton = new ItemMiscZSS(32).setUnlocalizedName("key_skeleton").setFull3D().setMaxStackSize(1).setMaxDamage(Config.getNumSkelKeyUses()).setCreativeTab(ZSSCreativeTabs.tabKeys);

		//===================== TOOLS TAB =====================//
		hookshot = new ItemHookShot().setUnlocalizedName("hookshot");
		hookshotUpgrade = new ItemHookShotUpgrade().setUnlocalizedName("hookshot_upgrade");
		bombBag = new ItemBombBag().setUnlocalizedName("bomb_bag");
		bomb = new ItemBomb().setUnlocalizedName("bomb");
		crystalSpirit = new ItemMiscZSS(0).setUnlocalizedName("spirit_crystal_empty").setMaxStackSize(1).setCreativeTab(ZSSCreativeTabs.tabTools);
		crystalDin = new ItemSpiritCrystal(BlockSacredFlame.EnumType.DIN, 8, 16).setUnlocalizedName("spirit_crystal_din");
		crystalFarore = new ItemSpiritCrystal(BlockSacredFlame.EnumType.FARORE, 8, 70).setUnlocalizedName("spirit_crystal_farore");
		crystalNayru = new ItemSpiritCrystal(BlockSacredFlame.EnumType.NAYRU, 16, 0).setUnlocalizedName("spirit_crystal_nayru");
		dekuLeaf = new ItemDekuLeaf().setUnlocalizedName("deku_leaf");
		dekuNut = (new ItemMiscZSS(2) {
			@Override
			public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
				EntitySeedShotDeku seedShot = new EntitySeedShotDeku(world, player, 0.5F, 1, 0);
				seedShot.setDamage(2.5F);
				if (!player.capabilities.isCreativeMode) {
					--stack.stackSize;
				}
				if (!world.isRemote) {
					world.spawnEntityInWorld(seedShot);
				}
				return stack;
			}
		}).setUnlocalizedName("deku_nut").setCreativeTab(ZSSCreativeTabs.tabTools);
		gauntletsSilver = new ItemPowerGauntlets(BlockWeight.MEDIUM).setUnlocalizedName("gauntlets_silver");
		gauntletsGolden = new ItemPowerGauntlets(BlockWeight.VERY_HEAVY).setUnlocalizedName("gauntlets_golden");
		magicMirror = new ItemMagicMirror().setUnlocalizedName("magic_mirror");
		fairyBottle = new ItemFairyBottle().setUnlocalizedName("fairy_bottle");
		rocsFeather = new ItemMiscZSS(12).setUnlocalizedName("rocs_feather").setCreativeTab(ZSSCreativeTabs.tabTools);
		potionRed = new ItemZeldaPotion("potion_red", 20.0F, 0.0F);
		potionGreen = new ItemZeldaPotion("potion_green", 0.0F, 100.0F);
		potionBlue = new ItemZeldaPotion("potion_blue", 40.0F, 100.0F);
		potionYellow = new ItemZeldaPotion("potion_yellow").setBuffEffect(Buff.RESIST_SHOCK, 6000, 100, 1.0F);
		potionPurple = new ItemPotionPurple("potion_purple", 20, 40.0F);
		lonlonMilk = new ItemLonLonMilk("lon_lon_milk", 2, 10.0F);
		lonlonSpecial = new ItemDrinkable.ItemLonLonSpecial("lon_lon_special");
		magicJar = new ItemPickupOnly.ItemMagicJar(10).setUnlocalizedName("magic_jar");
		magicJarBig = new ItemPickupOnly.ItemMagicJar(250).setUnlocalizedName("magic_jar_big");
		magicContainer = (new ItemDrinkable("magic_container") {
			@Override
			public ItemStack onItemUseFinish(ItemStack stack, World world, EntityPlayer player) {
				ZSSPlayerInfo info = ZSSPlayerInfo.get(player);
				float max = info.getMaxMagic();
				if (max < Config.getMaxMagicPoints() || info.getCurrentMagic() < max) {
					world.playSoundAtEntity(player, "random.burp", 0.5F, world.rand.nextFloat() * 0.1F + 0.9F);
					info.setMaxMagic(max + 50.0F);
					info.setCurrentMagic(info.getMaxMagic());
					if (!player.capabilities.isCreativeMode) {
						--stack.stackSize;
					}
				}
				return super.onItemUseFinish(stack, world, player);
			}
			@Override
			@SideOnly(Side.CLIENT)
			public void addInformation(ItemStack stack, EntityPlayer player, List<String> list, boolean advanced) {
				list.add(StatCollector.translateToLocal("tooltip.zss.magic_container.desc.0"));
				list.add(StatCollector.translateToLocal("tooltip.zss.magic_container.desc.1"));
			}
		}).setCreativeTab(ZSSCreativeTabs.tabTools);
		medallion = new ItemMedallion().setUnlocalizedName("medallion");
		rodFire = new ItemMagicRod(MagicType.FIRE, 8.0F, 10.0F).setUnlocalizedName("rod_fire");
		rodIce = new ItemMagicRod(MagicType.ICE, 6.0F, 10.0F).setUnlocalizedName("rod_ice");
		rodTornado = new ItemMagicRod(MagicType.WIND, 4.0F, 10.0F).setUnlocalizedName("rod_tornado");
		whip = new ItemWhip().setUnlocalizedName("whip");

		//===================== MASK TAB =====================//
		maskBlast = new ItemMask.ItemMaskBlast(ArmorMaterial.IRON, ZSSMain.proxy.addArmor("mask")).setUnlocalizedName("mask_blast");
		maskBunny = new ItemMask.ItemMaskBunny(WOOD, ZSSMain.proxy.addArmor("mask")).setPrice(1, 64).setUnlocalizedName("mask_bunny");
		maskCouples = new ItemMask.ItemMaskCouples(WOOD, ZSSMain.proxy.addArmor("mask")).setPrice(40, 32).setUnlocalizedName("mask_couples");
		maskGerudo = new ItemMask(WOOD, ZSSMain.proxy.addArmor("mask")).setUnlocalizedName("mask_gerudo");
		maskGiants = new ItemMask.ItemMaskGiants(WOOD, ZSSMain.proxy.addArmor("mask")).setUnlocalizedName("mask_giants");
		maskGibdo = new ItemMask(WOOD, ZSSMain.proxy.addArmor("mask")).setUnlocalizedName("mask_gibdo");
		maskHawkeye = new ItemMask.ItemMaskHawkeye(WOOD, ZSSMain.proxy.addArmor("mask")).setUnlocalizedName("mask_hawkeye");
		maskKeaton = new ItemMask(WOOD, ZSSMain.proxy.addArmor("mask")).setPrice(8, 16).setUnlocalizedName("mask_keaton");
		maskScents = new ItemMask(WOOD, ZSSMain.proxy.addArmor("mask")).setPrice(32, 32).setUnlocalizedName("mask_scents");
		maskSkull = new ItemMask(WOOD, ZSSMain.proxy.addArmor("mask")).setPrice(20, 10).setUnlocalizedName("mask_skull");
		maskSpooky = new ItemMask(WOOD, ZSSMain.proxy.addArmor("mask")).setPrice(16, 8).setUnlocalizedName("mask_spooky");
		maskStone = new ItemMask(WOOD, ZSSMain.proxy.addArmor("mask")).setEffect(new PotionEffect(Potion.invisibility.getId(), 100, 0)).setUnlocalizedName("mask_stone");
		maskTruth = new ItemMask(WOOD, ZSSMain.proxy.addArmor("mask")).setUnlocalizedName("mask_truth");
		maskDeku = new ItemMask(WOOD, ZSSMain.proxy.addArmor("mask")).setUnlocalizedName("mask_deku");
		maskGoron = new ItemMask.ItemMaskGoron(WOOD, ZSSMain.proxy.addArmor("mask")).setUnlocalizedName("mask_goron");
		maskZora = new ItemMaskZora(WOOD, ZSSMain.proxy.addArmor("mask")).setUnlocalizedName("mask_zora");
		maskFierce = new ItemMask(WOOD, ZSSMain.proxy.addArmor("mask")).setUnlocalizedName("mask_fierce");
		maskMajora = new ItemMask.ItemMaskMajora(WOOD, ZSSMain.proxy.addArmor("mask")).setEffect(new PotionEffect(Potion.wither.getId(), 100, 1)).setUnlocalizedName("mask_majora");

		//===================== MISCELLANEOUS TAB =====================//
		pendant = new ItemPendant().setUnlocalizedName("pendant");
		masterOre = new ItemMasterOre(24).setUnlocalizedName("master_ore");
		jellyChu = new ItemChuJelly().setUnlocalizedName("jelly_chu");
		treasure = new ItemTreasure().setUnlocalizedName("treasure");
		skulltulaToken = new ItemSkulltulaToken().setUnlocalizedName("skulltula_token");
		linksHouse = new ItemBuilderSeed(LinksHouse.class, "chat.zss.links_house.fail").setUnlocalizedName("links_house");
		instrument = new ItemInstrument();
		bombFlowerSeed = new ItemBombFlowerSeed().setUnlocalizedName("seed_bomb_flower");
		bookMudora = new BaseModItem().setUnlocalizedName("book_mudora").setMaxDamage(0).setCreativeTab(ZSSCreativeTabs.tabMisc);

		//===================== NO TAB =====================//
		heldBlock = new ItemHeldBlock().setUnlocalizedName("held_block");
		powerPiece = (new ItemPickupOnly() {
			@Override
			public boolean onPickupItem(ItemStack stack, EntityPlayer player) {
				PlayerUtils.playSound(player, Sounds.SUCCESS_MAGIC, 0.6F, 1.0F);
				ZSSEntityInfo buffs = ZSSEntityInfo.get(player);
				buffs.applyBuff(Buff.ATTACK_UP, 600, 100);
				buffs.applyBuff(Buff.DEFENSE_UP, 600, 25);
				buffs.applyBuff(Buff.EVADE_UP, 600, 25);
				buffs.applyBuff(Buff.RESIST_STUN, 600, 100);
				--stack.stackSize;
				return true;
			}
		}).setUnlocalizedName("power_piece");
		smallHeart = (new ItemPickupOnly() {
			@Override
			public boolean onPickupItem(ItemStack stack, EntityPlayer player) {
				if (player.getHealth() < player.getMaxHealth() || Config.alwaysPickupHearts()) {
					player.heal(1.0F);
					--stack.stackSize;
					return true;
				}
				return false;
			}
		}).setUnlocalizedName("heart_small");
		throwingRock = (new BaseModItem() {
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
		}).setUnlocalizedName("throwing_rock").setMaxStackSize(18);

		//===================== SPAWN EGGS TAB =====================//
		eggSpawner = new ItemCustomEgg().setUnlocalizedName("spawn_egg");
	}

	/**
	 * Adds a comparator mapping for a non-ZSS item
	 */
	public static void addItemComparatorMapping(Item item) {
		addonItems.add(item);
	}

	/**
	 * Actually adds the item comparator mapping
	 */
	private static void registerItemComparatorMapping(Item item) {
		if (itemList.containsKey(item)) {
			ZSSMain.logger.warn("Item already has a comparator mapping: " + (item == null ? "NULL" : item.getUnlocalizedName()));
		} else {
			itemList.put(item, sortId++);
		}
	}

	/**
	 * Registers an ItemBlock to the item sorter for creative tabs sorting
	 */
	public static void registerItemBlock(Item block) {
		if (block instanceof ItemBlock) {
			ZSSItems.registerItemComparatorMapping(block);
		} else {
			ZSSMain.logger.warn("Tried to register a non-ItemBlock item for " + (block == null ? "NULL" : block.getUnlocalizedName()));
		}
	}

	private static void registerItems() {
		try {
			for (Field f: ZSSItems.class.getFields()) {
				if (Item.class.isAssignableFrom(f.getType())) {
					Item item = (Item) f.get(null);
					if (item != null) {
						ZSSItems.registerItemComparatorMapping(item);
						String name = item.getUnlocalizedName();
						GameRegistry.registerItem(item, name.substring(name.lastIndexOf(".") + 1));
						if (item instanceof ICustomDispenserBehavior) {
							BlockDispenser.dispenseBehaviorRegistry.putObject(item, ((ICustomDispenserBehavior) item).getNewDispenserBehavior());
						}
					}
				}
			}
		} catch(Exception e) {
			ZSSMain.logger.warn("Caught exception while registering items: " + e.toString());
			e.printStackTrace();
		}
	}

	private static void registerRecipes() {
		if (allowGoldSmelting) {
			FurnaceRecipes.instance().addSmelting(Items.golden_sword, new ItemStack(Items.gold_ingot), 0.0F);
		}
		GameRegistry.addRecipe(new ItemStack(ZSSItems.instrument,1,ItemInstrument.Instrument.OCARINA_FAIRY.ordinal()), " c ","crc", 'c', Items.clay_ball, 'r', Items.reeds);
		if (enableCraftingHammer) {
			GameRegistry.addRecipe(new ItemStack(hammer), "lll"," s "," s ", 'l', Blocks.log, 's', Items.stick);
			GameRegistry.addRecipe(new ItemStack(hammer), "lll"," s "," s ", 'l', Blocks.log2, 's', Items.stick);
		}
		GameRegistry.addShapelessRecipe(new ItemStack(hookshot, 1, IHookable.HookshotType.WOOD_SHOT_EXT.ordinal()), new ItemStack(hookshot, 1, IHookable.HookshotType.WOOD_SHOT.ordinal()), new ItemStack(hookshotUpgrade, 1, ItemHookShotUpgrade.UpgradeType.EXTENDER.ordinal()));
		GameRegistry.addShapelessRecipe(new ItemStack(hookshot, 1, IHookable.HookshotType.CLAW_SHOT.ordinal()), new ItemStack(hookshot, 1, IHookable.HookshotType.WOOD_SHOT.ordinal()), new ItemStack(hookshotUpgrade, 1, ItemHookShotUpgrade.UpgradeType.CLAW.ordinal()));
		GameRegistry.addShapelessRecipe(new ItemStack(hookshot, 1, IHookable.HookshotType.CLAW_SHOT_EXT.ordinal()), new ItemStack(hookshot, 1, IHookable.HookshotType.WOOD_SHOT_EXT.ordinal()), new ItemStack(hookshotUpgrade, 1, ItemHookShotUpgrade.UpgradeType.CLAW.ordinal()));
		GameRegistry.addShapelessRecipe(new ItemStack(hookshot, 1, IHookable.HookshotType.CLAW_SHOT_EXT.ordinal()), new ItemStack(hookshot, 1, IHookable.HookshotType.CLAW_SHOT.ordinal()), new ItemStack(hookshotUpgrade, 1, ItemHookShotUpgrade.UpgradeType.EXTENDER.ordinal()));
		GameRegistry.addShapelessRecipe(new ItemStack(hookshot, 1, IHookable.HookshotType.MULTI_SHOT.ordinal()), new ItemStack(hookshot, 1, IHookable.HookshotType.CLAW_SHOT.ordinal()), new ItemStack(hookshotUpgrade, 1, ItemHookShotUpgrade.UpgradeType.MULTI.ordinal()));
		GameRegistry.addShapelessRecipe(new ItemStack(hookshot, 1, IHookable.HookshotType.MULTI_SHOT_EXT.ordinal()), new ItemStack(hookshot, 1, IHookable.HookshotType.CLAW_SHOT_EXT.ordinal()), new ItemStack(hookshotUpgrade, 1, ItemHookShotUpgrade.UpgradeType.MULTI.ordinal()));
		GameRegistry.addShapelessRecipe(new ItemStack(hookshot, 1, IHookable.HookshotType.MULTI_SHOT_EXT.ordinal()), new ItemStack(hookshot, 1, IHookable.HookshotType.MULTI_SHOT.ordinal()), new ItemStack(hookshotUpgrade, 1, ItemHookShotUpgrade.UpgradeType.EXTENDER.ordinal()));
		if (enableCraftingMudora) {
			GameRegistry.addShapelessRecipe(new ItemStack(bookMudora, 2), bookMudora, Items.book, Items.feather, new ItemStack(Items.dye, 1, EnumDyeColor.BLACK.getDyeDamage()));
		}
		if (enableCraftingThrowingRock) {
			GameRegistry.addShapelessRecipe(new ItemStack(throwingRock, 9), Blocks.cobblestone);
			GameRegistry.addRecipe(new ItemStack(Blocks.cobblestone), "rrr", "rrr", "rrr", 'r', throwingRock);
		}
		GameRegistry.addRecipe(new RecipeBagToBombArrows());
		GameRegistry.addRecipe(new RecipeCombineBombBag());
		GameRegistry.addRecipe(new ItemStack(ZSSBlocks.pedestal,3,0x8), "qqq", "qpq", "qqq", 'q', Blocks.quartz_block, 'p', new ItemStack(ZSSBlocks.pedestal,1,0x8));
		GameRegistry.addRecipe(new ItemStack(ZSSBlocks.beamWooden), "b", "b", "b", 'b', Blocks.planks);
		GameRegistry.addRecipe(new ItemStack(ZSSBlocks.gossipStone), " s ", "sos", " s ", 's', Blocks.stone, 'o', new ItemStack(ZSSItems.instrument, 1, Instrument.OCARINA_FAIRY.ordinal()));
		GameRegistry.addRecipe(new ItemStack(ZSSBlocks.hookTarget), " c ", "bab", " b ", 'a', Items.redstone, 'b', Blocks.stone, 'c', Blocks.iron_bars);
		GameRegistry.addRecipe(new ItemStack(ZSSBlocks.hookTargetAll), "bcb", "cac", "bcb", 'a', Items.redstone, 'b', Blocks.stone, 'c', Blocks.iron_bars);
		GameRegistry.addShapelessRecipe(new ItemStack(arrowBomb), new ItemStack(bomb,1,BombType.BOMB_STANDARD.ordinal()), Items.arrow);
		GameRegistry.addShapelessRecipe(new ItemStack(arrowBombFire), new ItemStack(bomb,1,BombType.BOMB_FIRE.ordinal()), Items.arrow);
		GameRegistry.addShapelessRecipe(new ItemStack(arrowBombWater), new ItemStack(bomb,1,BombType.BOMB_WATER.ordinal()), Items.arrow);
		GameRegistry.addRecipe(new ItemStack(ZSSBlocks.ceramicJar,8), "c c","c c"," c ", 'c', Items.brick);
		GameRegistry.addRecipe(new ItemStack(ZSSItems.skillOrb,1,SkillBase.bonusHeart.getId()), "HH", "HH", 'H', heartPiece);
		GameRegistry.addRecipe(new ItemStack(ZSSItems.instrument,1,ItemInstrument.Instrument.OCARINA_FAIRY.ordinal()), " c ", "crc", 'c', Items.clay_ball, 'r', Items.reeds);
		GameRegistry.addShapelessRecipe(new ItemStack(tunicGoronLegs), tunicHeroLegs, new ItemStack(Items.dye,1, EnumDyeColor.RED.getDyeDamage()));
		GameRegistry.addShapelessRecipe(new ItemStack(tunicGoronLegs), tunicZoraLegs, new ItemStack(Items.dye,1, EnumDyeColor.RED.getDyeDamage()));
		GameRegistry.addShapelessRecipe(new ItemStack(tunicZoraLegs), tunicHeroLegs, new ItemStack(Items.dye,1, EnumDyeColor.BLUE.getDyeDamage()));
		GameRegistry.addShapelessRecipe(new ItemStack(tunicZoraLegs), tunicGoronLegs, new ItemStack(Items.dye,1, EnumDyeColor.BLUE.getDyeDamage()));
		GameRegistry.addShapelessRecipe(new ItemStack(tunicHeroLegs), tunicGoronLegs, new ItemStack(Items.dye, 1, EnumDyeColor.GREEN.getDyeDamage()));
		GameRegistry.addShapelessRecipe(new ItemStack(tunicHeroLegs), tunicZoraLegs, new ItemStack(Items.dye, 1, EnumDyeColor.GREEN.getDyeDamage()));
	}

	/**
	 * Adds some special loot to vanilla chests
	 */
	private static void addVanillaDungeonLoot() {
		ChestGenHooks.getInfo(ChestGenHooks.VILLAGE_BLACKSMITH).addItem(new WeightedRandomChestContent(new ItemStack(bookMudora), 1, 1, 2));
		ChestGenHooks.getInfo(ChestGenHooks.STRONGHOLD_LIBRARY).addItem(new WeightedRandomChestContent(new ItemStack(bookMudora), 1, 1, 2));
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
}
