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

package zeldaswordskills.item;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
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
import net.minecraft.item.Item;
import net.minecraft.item.Item.ToolMaterial;
import net.minecraft.item.ItemArmor.ArmorMaterial;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StatCollector;
import net.minecraft.util.WeightedRandomChestContent;
import net.minecraft.world.World;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.ChestGenHooks;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.util.EnumHelper;
import zeldaswordskills.ZSSMain;
import zeldaswordskills.api.block.BlockWeight;
import zeldaswordskills.api.block.IHookable;
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
import zeldaswordskills.entity.ZSSEntityInfo;
import zeldaswordskills.entity.ZSSVillagerInfo;
import zeldaswordskills.entity.buff.Buff;
import zeldaswordskills.entity.player.ZSSPlayerInfo;
import zeldaswordskills.entity.player.ZSSPlayerSkills;
import zeldaswordskills.entity.projectile.EntitySeedShot;
import zeldaswordskills.entity.projectile.EntitySeedShotDeku;
import zeldaswordskills.entity.projectile.EntityThrowingRock;
import zeldaswordskills.item.IRupeeValue.IMetaRupeeValue;
import zeldaswordskills.item.ItemInstrument.Instrument;
import zeldaswordskills.item.crafting.RecipeCombineBombBag;
import zeldaswordskills.item.dispenser.BehaviorDispenseCustomMobEgg;
import zeldaswordskills.ref.Config;
import zeldaswordskills.ref.ModInfo;
import zeldaswordskills.ref.Sounds;
import zeldaswordskills.skills.SkillBase;
import zeldaswordskills.util.PlayerUtils;
import zeldaswordskills.world.gen.structure.LinksHouse;
import cpw.mods.fml.common.registry.GameData;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

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
	enableGrassBombDrop;

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
	/** Number of rupees required to craft a single emerald (0 to disable) [0-64] */
	private static int rupeesToEmeralds;


	/** List of potential extra drops from tall grass when cut with a sword */
	private static final List<ItemStack> grassDrops = new ArrayList<ItemStack>();

	/** Material used for masks */
	public static final ArmorMaterial WOOD = EnumHelper.addArmorMaterial("Wood", 5, new int[] {1,3,2,1}, 5);

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
	potionPurple,
	lonlonMilk,
	lonlonSpecial,
	magicJar,
	magicJarBig,
	magicContainer,
	rocsFeather,
	walletUpgrade;

	//================ TREASURES TAB ================//
	public static Item
	rupee,
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
	doorLocked,
	doorLockedSmall,
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
	public static Item eggSpawner; // same item for all custom entities with spawn eggs

	/**
	 * Initializes mod item indices from configuration file
	 */
	public static void initConfig(Configuration config) {
		/*================== LOOT SETTINGS =====================*/
		enableGrassArrowDrop = config.get("Loot", "Enable arrow drops from grass (must use sword)", true).getBoolean(true);
		enableGrassBombDrop = config.get("Loot", "Enable bomb drops from grass (must use sword)", false).getBoolean(false);
		enableBombLoot = config.get("Loot", "Enable bombs in vanilla chests", false).getBoolean(false);
		enableBombBagLoot = config.get("Loot", "Enable bomb bags in vanilla chests", false).getBoolean(false);
		enableHeartLoot = config.get("Loot", "Enable heart pieces in vanilla chests", false).getBoolean(false);
		/*================== RECIPES =====================*/
		allowGoldSmelting = config.get("Recipes", "Smelt all those disarmed pigmen swords into gold ingots", false).getBoolean(false);
		enableCraftingHammer = config.get("Recipes", "Enable crafting of the Wooden Hammer used to bypass wooden pegs", true).getBoolean(true);
		enableCraftingMudora = config.get("Recipes", "Enable crafting recipe to make copies of the Book of Mudora", true).getBoolean(true);
		enableCraftingThrowingRock = config.get("Recipes", "Enable crafting throwing rocks from cobblestone and back", false).getBoolean(false);
		rupeesToEmeralds = MathHelper.clamp_int(config.get("Recipes", "Number of rupees required to craft a single emerald (0 to disable) [0-64]", 0).getInt(), 0, 64);
	}

	/**
	 * Call during FMLPreInitializationEvent to initialize and register all items.
	 */
	public static void preInit() {
		ZSSItems.initItems();
		ZSSItems.registerItems();
		ZSSItems.addDispenserBehaviors();
	}

	/**
	 * Call during FMLInitializationEvent to register all crafting recipes.
	 */
	public static void init() {
		ZSSItems.registerRecipes();
		ItemChuJelly.initializeJellies();
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
		ZSSVillagerInfo.initTrades();	
		// Register mappings for all addon items now, so ZSS items always appear first
		for (Item item : addonItems) {
			ZSSItems.registerItemComparatorMapping(item);
		}
	}

	/**
	 * Registers all custom Item renderers
	 */
	@SideOnly(Side.CLIENT)
	public static void registerRenderers() {
		MinecraftForgeClient.registerItemRenderer(ZSSItems.bomb, new RenderItemBomb());
		MinecraftForgeClient.registerItemRenderer(ZSSItems.bombBag, new RenderItemBombBag());
		MinecraftForgeClient.registerItemRenderer(ZSSItems.hammer, new RenderBigItem(1.0F));
		MinecraftForgeClient.registerItemRenderer(ZSSItems.hammerMegaton, new RenderBigItem(1.0F));
		MinecraftForgeClient.registerItemRenderer(ZSSItems.hammerSkull, new RenderBigItem(1.0F));
		MinecraftForgeClient.registerItemRenderer(ZSSItems.swordBiggoron, new RenderBigItem(0.75F));
		MinecraftForgeClient.registerItemRenderer(ZSSItems.swordGiant, new RenderBigItem(0.75F));
		MinecraftForgeClient.registerItemRenderer(ZSSItems.swordDarknut, new RenderBigItem(0.9F));
		MinecraftForgeClient.registerItemRenderer(ZSSItems.heroBow, new RenderItemCustomBow());
		MinecraftForgeClient.registerItemRenderer(ZSSItems.shieldDeku, new RenderItemShield());
		MinecraftForgeClient.registerItemRenderer(ZSSItems.shieldHylian, new RenderItemShield());
		MinecraftForgeClient.registerItemRenderer(ZSSItems.shieldMirror, new RenderItemShield());
		//MinecraftForgeClient.registerItemRenderer(ZSSItems.hookshot.itemID, new RenderItemHookShot());

		// BLOCK ITEMS
		MinecraftForgeClient.registerItemRenderer(ZSSItems.heldBlock, new RenderHeldItemBlock());
		MinecraftForgeClient.registerItemRenderer(Item.getItemFromBlock(ZSSBlocks.dungeonCore), new RenderItemDungeonBlock());
		MinecraftForgeClient.registerItemRenderer(Item.getItemFromBlock(ZSSBlocks.dungeonStone), new RenderItemDungeonBlock());
	}

	private static void addGrassDrops() {
		for (int i = 0; i < 10; ++i) {
			grassDrops.add(new ItemStack(smallHeart));
			if (i % 2 == 0) {
				grassDrops.add(new ItemStack(rupee, 1, ItemRupee.Rupee.GREEN_RUPEE.ordinal()));
			}
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
		// SKILL TAB ITEMS
		skillOrb = new ItemSkillOrb().setUnlocalizedName("zss.skillorb");
		heartPiece = new ItemMiscZSS(12).setUnlocalizedName("zss.heartpiece").setCreativeTab(ZSSCreativeTabs.tabSkills);
		skillWiper = (new ItemMiscZSS(0) {
			@Override
			public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
				if (!world.isRemote) {
					PlayerUtils.sendTranslatedChat(player, "chat.zss.skill.reset");
					ZSSPlayerSkills.get(player).resetSkills();
				}
				return stack;
			}
		}).setUnlocalizedName("zss.skill_wiper").setCreativeTab(ZSSCreativeTabs.tabSkills);

		// COMBAT TAB ITEMS
		tunicHeroHelm = new ItemArmorTunic(ZSSMain.proxy.addArmor("tunic"), ArmorIndex.TYPE_HELM).setUnlocalizedName("zss.hero_tunic_helm");
		tunicHeroChest = new ItemArmorTunic(ZSSMain.proxy.addArmor("tunic"), ArmorIndex.TYPE_CHEST).setUnlocalizedName("zss.hero_tunic_chest");
		tunicHeroLegs = new ItemArmorTunic(ZSSMain.proxy.addArmor("tunic"), ArmorIndex.TYPE_LEGS).setUnlocalizedName("zss.hero_tunic_legs");
		tunicHeroBoots = new ItemArmorTunic(ZSSMain.proxy.addArmor("tunic"), ArmorIndex.TYPE_BOOTS).setUnlocalizedName("zss.hero_tunic_boots");

		tunicGoronHelm = new ItemArmorTunic(ZSSMain.proxy.addArmor("tunic"), ArmorIndex.TYPE_HELM).setUnlocalizedName("zss.goron_tunic_helm");
		tunicGoronChest = new ItemArmorTunic(ZSSMain.proxy.addArmor("tunic"), ArmorIndex.TYPE_CHEST).setUnlocalizedName("zss.goron_tunic_chest");
		tunicGoronLegs = new ItemArmorTunic(ZSSMain.proxy.addArmor("tunic"), ArmorIndex.TYPE_LEGS).setUnlocalizedName("zss.goron_tunic_legs");

		tunicZoraHelm = (new ItemArmorTunic(ZSSMain.proxy.addArmor("tunic"), ArmorIndex.TYPE_HELM) {
			@Override
			@SideOnly(Side.CLIENT)
			public void getSubItems(Item item, CreativeTabs tab, List list) {
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
		}).setUnlocalizedName("zss.zora_tunic_helm");
		tunicZoraChest = new ItemArmorTunic(ZSSMain.proxy.addArmor("tunic"), ArmorIndex.TYPE_CHEST).setEffect(new PotionEffect(Potion.waterBreathing.getId(), 90, 0)).setUnlocalizedName("zss.zora_tunic_chest");
		tunicZoraLegs = new ItemArmorTunic(ZSSMain.proxy.addArmor("tunic"), ArmorIndex.TYPE_LEGS).setUnlocalizedName("zss.zora_tunic_legs");
		tunicZoraBoots = (new ItemArmorTunic(ZSSMain.proxy.addArmor("tunic"), ArmorIndex.TYPE_BOOTS) {
			@Override
			public void onArmorTick(World world, EntityPlayer player, ItemStack stack) {
				int i = MathHelper.floor_double(player.posX);
				int j = MathHelper.floor_double(player.boundingBox.minY);
				int k = MathHelper.floor_double(player.posZ);
				Material m = world.getBlock(i, j + 1, k).getMaterial();
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

		bootsHeavy = new ItemArmorBoots.ItemHeavyBoots(ArmorMaterial.IRON, ZSSMain.proxy.addArmor("boots"), "minecraft:textures/models/armor/iron_layer_1.png").setUnlocalizedName("zss.boots_heavy");
		bootsHover = new ItemArmorBoots.ItemHoverBoots(ArmorMaterial.CHAIN, ZSSMain.proxy.addArmor("boots"), ModInfo.ID + ":textures/armor/mask_hawkeye_layer_1.png").setUnlocalizedName("zss.boots_hover");
		bootsPegasus = new ItemArmorBoots.ItemPegasusBoots(ArmorMaterial.CHAIN, ZSSMain.proxy.addArmor("boots"), ModInfo.ID + ":textures/armor/hero_tunic_layer_1.png").setUnlocalizedName("zss.boots_pegasus");
		bootsRubber = new ItemArmorBoots.ItemRubberBoots(ArmorMaterial.CHAIN, ZSSMain.proxy.addArmor("boots"), ModInfo.ID + ":textures/armor/boots_rubber_layer_1.png").setUnlocalizedName("zss.boots_rubber");

		shieldDeku = new ItemZeldaShield(ToolMaterial.WOOD, 0.25F, 30, 3F, 5F).setUnlocalizedName("zss.shield_deku");
		shieldHylian = new ItemZeldaShield(ToolMaterial.IRON, 0.5F, 18, 5F, 3.5F).setUnlocalizedName("zss.shield_hylian");
		shieldMirror = new ItemZeldaShield(ToolMaterial.EMERALD, 0.75F, 24, 4F, 4F).setUnlocalizedName("zss.shield_mirror");

		swordKokiri = new ItemZeldaSword(ToolMaterial.IRON, -1.0F).setUnlocalizedName("zss.sword_kokiri").setMaxDamage(256);
		swordOrdon = new ItemZeldaSword(ToolMaterial.IRON, 1.0F).setUnlocalizedName("zss.sword_ordon").setMaxDamage(512);
		swordGiant = new ItemZeldaSword(ToolMaterial.IRON, 6.0F, true).setUnlocalizedName("zss.sword_giant").setMaxDamage(32);
		swordBiggoron = new ItemZeldaSword(ToolMaterial.IRON, 6.0F, true).setNoItemOnBreak().setUnlocalizedName("zss.sword_biggoron").setMaxDamage(0);
		swordMaster = new ItemZeldaSword(ToolMaterial.EMERALD, 2.0F).setMasterSword().setUnlocalizedName("zss.sword_master").setMaxDamage(0);
		swordTempered = new ItemZeldaSword(ToolMaterial.EMERALD, 4.0F).setMasterSword().setUnlocalizedName("zss.sword_tempered").setMaxDamage(0);
		swordGolden = new ItemZeldaSword(ToolMaterial.EMERALD, 6.0F).setMasterSword().setUnlocalizedName("zss.sword_golden").setMaxDamage(0);
		swordMasterTrue = new ItemZeldaSword(ToolMaterial.EMERALD, 8.0F).setMasterSword().setUnlocalizedName("zss.sword_master_true").setMaxDamage(0);
		swordBroken = new ItemBrokenSword().setUnlocalizedName("zss.sword_broken");
		swordDarknut = new ItemZeldaSword(ToolMaterial.IRON, 1.0F, true, 20, 0.5F).setUnlocalizedName("zss.sword_darknut").setMaxDamage(768);

		hammer = new ItemHammer(BlockWeight.VERY_LIGHT, 8.0F, 50.0F).setUnlocalizedName("zss.hammer");
		hammerSkull = new ItemHammer(BlockWeight.MEDIUM, 12.0F, 50.0F).setUnlocalizedName("zss.hammer_skull");
		hammerMegaton = new ItemHammer(BlockWeight.VERY_HEAVY, 16.0F, 50.0F).setUnlocalizedName("zss.hammer_megaton");

		boomerang = new ItemBoomerang(4.0F, 12).setUnlocalizedName("zss.boomerang");
		boomerangMagic = new ItemBoomerang(6.0F, 24).setCaptureAll().setUnlocalizedName("zss.boomerang_magic");

		heroBow = new ItemHeroBow().setUnlocalizedName("zss.bow_hero");
		arrowBomb = new ItemZeldaArrow("arrow_bomb", 8, 1);
		arrowBombFire = new ItemZeldaArrow("arrow_bomb_fire", 10, 1);
		arrowBombWater = new ItemZeldaArrow("arrow_bomb_water", 12, 1);
		arrowFire = new ItemZeldaArrow.ItemMagicArrow("arrow_fire", 20, 2, 2.5F);
		arrowIce = new ItemZeldaArrow.ItemMagicArrow("arrow_ice", 20, 2, 2.5F);
		arrowLight = new ItemZeldaArrow.ItemMagicArrow("arrow_light", 50, 3, 5.0F);
		arrowSilver = new ItemZeldaArrow("arrow_silver", 40, 3);

		slingshot = new ItemSlingshot().setUnlocalizedName("zss.slingshot");
		scattershot = new ItemSlingshot(3, 30F).setUnlocalizedName("zss.scattershot");
		supershot = new ItemSlingshot(5, 15F).setUnlocalizedName("zss.supershot");

		// BLOCK TAB ITEMS
		doorLocked = new ItemDoorBoss().setUnlocalizedName("zss.doorlocked");
		doorLockedSmall = new ItemDoorLocked(ZSSBlocks.doorLockedSmall).setUnlocalizedName("zss.door_locked_small");

		// KEYS TAB ITEMS
		keyBig = new ItemKeyBig().setUnlocalizedName("zss.keybig").setFull3D();
		keySmall = new ItemMiscZSS(6).setUnlocalizedName("zss.keysmall").setFull3D().setCreativeTab(ZSSCreativeTabs.tabKeys);
		keySkeleton = new ItemMiscZSS(32).setUnlocalizedName("zss.keyskeleton").setFull3D().setMaxStackSize(1).setMaxDamage(Config.getNumSkelKeyUses()).setCreativeTab(ZSSCreativeTabs.tabKeys);

		// TOOLS TAB ITEMS
		hookshot = new ItemHookShot().setUnlocalizedName("zss.hookshot");
		hookshotAddon = new ItemHookShotUpgrade().setUnlocalizedName("zss.hookshot.upgrade");
		bombBag = new ItemBombBag().setUnlocalizedName("zss.bombbag");
		bomb = new ItemBomb().setUnlocalizedName("zss.bomb");
		crystalDin = new ItemSpiritCrystal(BlockSacredFlame.DIN, 8, 16).setUnlocalizedName("zss.spirit_crystal_din");
		crystalFarore = new ItemSpiritCrystal(BlockSacredFlame.FARORE, 8, 70).setUnlocalizedName("zss.spirit_crystal_farore");
		crystalNayru = new ItemSpiritCrystal(BlockSacredFlame.NAYRU, 16, 0).setUnlocalizedName("zss.spirit_crystal_nayru");
		gauntletsSilver = new ItemPowerGauntlets(BlockWeight.MEDIUM).setUnlocalizedName("zss.gauntlets_silver");
		gauntletsGolden = new ItemPowerGauntlets(BlockWeight.VERY_HEAVY).setUnlocalizedName("zss.gauntlets_golden");
		magicMirror = new ItemMagicMirror().setUnlocalizedName("zss.magicmirror");
		fairyBottle = new ItemFairyBottle().setUnlocalizedName("zss.fairybottle");
		potionRed = new ItemZeldaPotion("potion_red", 10, 20.0F, 0.0F);
		potionGreen = new ItemZeldaPotion("potion_green", 20, 0.0F, 100.0F);
		potionBlue = new ItemZeldaPotion("potion_blue", 60, 40.0F, 100.0F);
		potionYellow = new ItemZeldaPotion("potion_yellow", 40).setBuffEffect(Buff.RESIST_SHOCK, 6000, 100, 1.0F);
		potionPurple = new ItemDrinkable.ItemPotionPurple("potion_purple", 20, 40.0F);
		lonlonMilk = new ItemLonLonMilk("lon_lon_milk", 2, 10.0F);
		lonlonSpecial = new ItemDrinkable.ItemLonLonSpecial("lon_lon_special");
		magicJar = new ItemPickupOnly.ItemMagicJar("magic_jar", 10);
		magicJarBig = new ItemPickupOnly.ItemMagicJar("magic_jar_big", 250);
		magicContainer = (new ItemDrinkable("magic_container") {
			@Override
			public ItemStack onEaten(ItemStack stack, World world, EntityPlayer player) {
				ZSSPlayerInfo info = ZSSPlayerInfo.get(player);
				float max = info.getMaxMagic();
				if (max < Config.getMaxMagicPoints() || info.getCurrentMagic() < max) {
					info.setMaxMagic(max + 50.0F);
					info.setCurrentMagic(info.getMaxMagic());
					if (!player.capabilities.isCreativeMode) {
						--stack.stackSize;
					}
				}
				return super.onEaten(stack, world, player);
			}
			@Override
			@SideOnly(Side.CLIENT)
			public void addInformation(ItemStack stack,	EntityPlayer player, List list, boolean isHeld) {
				list.add(StatCollector.translateToLocal("tooltip.zss.magic_container.desc.0"));
				list.add(StatCollector.translateToLocal("tooltip.zss.magic_container.desc.1"));
			}
		}).setCreativeTab(ZSSCreativeTabs.tabTools);
		rodFire = new ItemMagicRod(MagicType.FIRE, 8.0F, 10.0F).setUnlocalizedName("zss.rod_fire");
		rodIce = new ItemMagicRod(MagicType.ICE, 6.0F, 10.0F).setUnlocalizedName("zss.rod_ice");
		rodTornado = new ItemMagicRod(MagicType.WIND, 4.0F, 10.0F).setUnlocalizedName("zss.rod_tornado");
		whip = new ItemWhip().setUnlocalizedName("zss.whip");

		// MASK TAB ITEMS
		maskBlast = new ItemMask.ItemMaskBlast(ArmorMaterial.IRON, ZSSMain.proxy.addArmor("mask")).setUnlocalizedName("zss.mask_blast");
		maskBunny = new ItemMask.ItemMaskBunny(WOOD, ZSSMain.proxy.addArmor("mask")).setPrice(1, 64).setUnlocalizedName("zss.mask_bunny");
		maskCouples = new ItemMask.ItemMaskCouples(WOOD, ZSSMain.proxy.addArmor("mask")).setPrice(40, 32).setUnlocalizedName("zss.mask_couples");
		maskGerudo = new ItemMask(WOOD, ZSSMain.proxy.addArmor("mask")).setUnlocalizedName("zss.mask_gerudo");
		maskGiants = new ItemMask.ItemMaskGiants(WOOD, ZSSMain.proxy.addArmor("mask")).setUnlocalizedName("zss.mask_giants");
		maskGibdo = new ItemMask(WOOD, ZSSMain.proxy.addArmor("mask")).setUnlocalizedName("zss.mask_gibdo");
		maskHawkeye = new ItemMask.ItemMaskHawkeye(WOOD, ZSSMain.proxy.addArmor("mask")).setUnlocalizedName("zss.mask_hawkeye");
		maskKeaton = new ItemMask(WOOD, ZSSMain.proxy.addArmor("mask")).setPrice(8, 16).setUnlocalizedName("zss.mask_keaton");
		maskScents = new ItemMask(WOOD, ZSSMain.proxy.addArmor("mask")).setPrice(32, 32).setUnlocalizedName("zss.mask_scents");
		maskSkull = new ItemMask(WOOD, ZSSMain.proxy.addArmor("mask")).setPrice(20, 10).setUnlocalizedName("zss.mask_skull");
		maskSpooky = new ItemMask(WOOD, ZSSMain.proxy.addArmor("mask")).setPrice(16, 8).setUnlocalizedName("zss.mask_spooky");
		maskStone = new ItemMask(WOOD, ZSSMain.proxy.addArmor("mask")).setEffect(new PotionEffect(Potion.invisibility.getId(), 100, 0)).setUnlocalizedName("zss.mask_stone");
		maskTruth = new ItemMask(WOOD, ZSSMain.proxy.addArmor("mask")).setUnlocalizedName("zss.mask_truth");
		maskDeku = new ItemMask(WOOD, ZSSMain.proxy.addArmor("mask")).setUnlocalizedName("zss.mask_deku");
		maskGoron = new ItemMask.ItemMaskGoron(WOOD, ZSSMain.proxy.addArmor("mask")).setUnlocalizedName("zss.mask_goron");
		maskZora = new ItemMaskZora(WOOD, ZSSMain.proxy.addArmor("mask")).setUnlocalizedName("zss.mask_zora");
		maskFierce = new ItemMask(WOOD, ZSSMain.proxy.addArmor("mask")).setUnlocalizedName("zss.mask_fierce");
		maskMajora = new ItemMask.ItemMaskMajora(WOOD, ZSSMain.proxy.addArmor("mask")).setEffect(new PotionEffect(Potion.wither.getId(), 100, 1)).setUnlocalizedName("zss.mask_majora");

		// MISCELLANEOUS TAB ITEMS
		pendant = new ItemPendant().setUnlocalizedName("zss.pendant");
		crystalSpirit = new ItemMiscZSS(0).setUnlocalizedName("zss.spirit_crystal_empty").setMaxStackSize(1).setCreativeTab(ZSSCreativeTabs.tabTools);
		masterOre = new ItemMasterOre(24).setUnlocalizedName("zss.masterore");
		rocsFeather = new ItemMiscZSS(12).setUnlocalizedName("zss.rocs_feather").setCreativeTab(ZSSCreativeTabs.tabTools);
		dekuLeaf = new ItemDekuLeaf().setUnlocalizedName("zss.deku_leaf");
		dekuNut = (new ItemMiscZSS(2) {
			@Override
			public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
				EntitySeedShot seedShot = new EntitySeedShotDeku(world, player, 0.5F, 1, 0);
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
		jellyChu = new ItemChuJelly().setUnlocalizedName("zss.jelly_chu");
		treasure = new ItemTreasure().setUnlocalizedName("zss.treasure");
		skulltulaToken = new ItemSkulltulaToken().setUnlocalizedName("zss.skulltula_token");
		linksHouse = new ItemBuilderSeed(LinksHouse.class, "chat.zss.links_house.fail", "deku_nut").setUnlocalizedName("zss.links_house");
		instrument = new ItemInstrument();
		bombFlowerSeed = new ItemBombFlowerSeed().setUnlocalizedName("zss.seed_bomb_flower");

		// ITEMS WITH NO TAB
		heldBlock = new ItemHeldBlock().setUnlocalizedName("zss.held_block");
		powerPiece = (new ItemPickupOnly("power_piece") {
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
		});
		smallHeart = (new ItemPickupOnly("heart") {
			@Override
			public boolean onPickupItem(ItemStack stack, EntityPlayer player) {
				if (player.getHealth() < player.getMaxHealth() || Config.alwaysPickupHearts()) {
					player.heal(1.0F);
					--stack.stackSize;
					return true;
				}
				return false;
			}
		});
		throwingRock = (new Item() {
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

		// Custom Spawn Eggs
		eggSpawner = new ItemCustomEgg().setUnlocalizedName("zss.spawn_egg");

		// NEW ITEMS
		bookMudora = new Item().setUnlocalizedName("zss.book_mudora").setTextureName(ModInfo.ID + ":book_mudora").setMaxDamage(0).setCreativeTab(ZSSCreativeTabs.tabMisc);
		medallion = new ItemMedallion().setUnlocalizedName("zss.medallion");
		rupee = new ItemRupee();
		walletUpgrade = new ItemWalletUpgrade();
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
						GameRegistry.registerItem(item, item.getUnlocalizedName().replace("item.", "").trim());
					}
				}
			}
		} catch(Exception e) {
			ZSSMain.logger.error("Error registering items:");
			e.printStackTrace();
		}
	}

	public static String[] getDefaultRupeeValues() {
		try {
			List<String> defaults = new ArrayList<String>();
			for (Field f: ZSSItems.class.getFields()) {
				if (Item.class.isAssignableFrom(f.getType())) {
					Item item = (Item) f.get(null);
					if (item instanceof IRupeeValue) {
						List<String> s = getRupeeValueString(item, (IRupeeValue) item);
						if (s != null && !s.isEmpty()) {
							defaults.addAll(s);
						}
					}
				}
			}
			return defaults.toArray(new String[defaults.size()]);
		} catch(Exception e) {
			ZSSMain.logger.error("Error loading default rupee values:");
			e.printStackTrace();
		}
		return null;
	}

	private static List<String> getRupeeValueString(Item item, IRupeeValue value) {
		String name = Item.itemRegistry.getNameForObject(item);
		if (item.getHasSubtypes() && item instanceof IMetaRupeeValue) {
			List<ItemStack> stacks = ((IMetaRupeeValue) item).getRupeeValueSubItems();
			if (stacks != null && !stacks.isEmpty()) {
				List<String> entries = new ArrayList<String>();
				for (ItemStack stack : stacks) {
					entries.add(String.format("%s@%d=%d", name, stack.getItemDamage(), value.getDefaultRupeeValue(stack)));
				}
				return entries;
			}
		}
		return Arrays.asList(name + "=" + value.getDefaultRupeeValue(new ItemStack(item)));
	}

	private static void registerRecipes() {
		if (allowGoldSmelting) {
			// func_151396_a is addSmelting()
			FurnaceRecipes.smelting().func_151396_a(Items.golden_sword, new ItemStack(Items.gold_ingot), 0.0F);
		}
		if (enableCraftingHammer) {
			GameRegistry.addRecipe(new ItemStack(hammer), "lll"," s "," s ", 'l', Blocks.log, 's', Items.stick);
			GameRegistry.addRecipe(new ItemStack(hammer), "lll"," s "," s ", 'l', Blocks.log2, 's', Items.stick);
		}
		GameRegistry.addShapelessRecipe(new ItemStack(hookshot, 1, IHookable.HookshotType.WOOD_SHOT_EXT.ordinal()), new ItemStack(hookshot, 1, IHookable.HookshotType.WOOD_SHOT.ordinal()), new ItemStack(hookshotAddon, 1, ItemHookShotUpgrade.AddonType.EXTENSION.ordinal()));
		GameRegistry.addShapelessRecipe(new ItemStack(hookshot, 1, IHookable.HookshotType.CLAW_SHOT.ordinal()), new ItemStack(hookshot, 1, IHookable.HookshotType.WOOD_SHOT.ordinal()), new ItemStack(hookshotAddon, 1, ItemHookShotUpgrade.AddonType.STONECLAW.ordinal()));
		GameRegistry.addShapelessRecipe(new ItemStack(hookshot, 1, IHookable.HookshotType.CLAW_SHOT_EXT.ordinal()), new ItemStack(hookshot, 1, IHookable.HookshotType.WOOD_SHOT_EXT.ordinal()), new ItemStack(hookshotAddon, 1, ItemHookShotUpgrade.AddonType.STONECLAW.ordinal()));
		GameRegistry.addShapelessRecipe(new ItemStack(hookshot, 1, IHookable.HookshotType.CLAW_SHOT_EXT.ordinal()), new ItemStack(hookshot, 1, IHookable.HookshotType.CLAW_SHOT.ordinal()), new ItemStack(hookshotAddon, 1, ItemHookShotUpgrade.AddonType.EXTENSION.ordinal()));
		GameRegistry.addShapelessRecipe(new ItemStack(hookshot, 1, IHookable.HookshotType.MULTI_SHOT.ordinal()), new ItemStack(hookshot, 1, IHookable.HookshotType.CLAW_SHOT.ordinal()), new ItemStack(hookshotAddon, 1, ItemHookShotUpgrade.AddonType.MULTI.ordinal()));
		GameRegistry.addShapelessRecipe(new ItemStack(hookshot, 1, IHookable.HookshotType.MULTI_SHOT_EXT.ordinal()), new ItemStack(hookshot, 1, IHookable.HookshotType.CLAW_SHOT_EXT.ordinal()), new ItemStack(hookshotAddon, 1, ItemHookShotUpgrade.AddonType.MULTI.ordinal()));
		GameRegistry.addShapelessRecipe(new ItemStack(hookshot, 1, IHookable.HookshotType.MULTI_SHOT_EXT.ordinal()), new ItemStack(hookshot, 1, IHookable.HookshotType.MULTI_SHOT.ordinal()), new ItemStack(hookshotAddon, 1, ItemHookShotUpgrade.AddonType.EXTENSION.ordinal()));
		if (enableCraftingMudora) {
			GameRegistry.addShapelessRecipe(new ItemStack(bookMudora, 2), bookMudora, Items.book, Items.feather, new ItemStack(Items.dye, 1, 0));
		}
		if (enableCraftingThrowingRock) {
			GameRegistry.addShapelessRecipe(new ItemStack(throwingRock, 9), Blocks.cobblestone);
			GameRegistry.addRecipe(new ItemStack(Blocks.cobblestone), "rrr", "rrr", "rrr", 'r', throwingRock);
		}
		GameRegistry.addRecipe(new RecipeCombineBombBag());
		//RecipeSorter.register(ModInfo.ID + ":combinebombbag", RecipeCombineBombBag.class, RecipeSorter.Category.SHAPELESS, "");
		GameRegistry.addRecipe(new ItemStack(ZSSBlocks.pedestal,3,0x8), "qqq","qpq","qqq", 'q', Blocks.quartz_block, 'p', new ItemStack(ZSSBlocks.pedestal,1,0x8));
		GameRegistry.addRecipe(new ItemStack(ZSSBlocks.beamWooden), "b","b","b", 'b', Blocks.planks);
		GameRegistry.addRecipe(new ItemStack(ZSSBlocks.gossipStone), " s ","sos"," s ", 's', Blocks.stone, 'o', new ItemStack(ZSSItems.instrument, 1, Instrument.OCARINA_FAIRY.ordinal()));
		GameRegistry.addRecipe(new ItemStack(ZSSBlocks.hookTarget), " c ","bab"," b ", 'a', Items.redstone, 'b', Blocks.stone, 'c', Blocks.iron_bars);
		GameRegistry.addRecipe(new ItemStack(ZSSBlocks.hookTargetAll), "bcb", "cac", "bcb", 'a', Items.redstone, 'b', Blocks.stone, 'c', Blocks.iron_bars);
		GameRegistry.addShapelessRecipe(new ItemStack(arrowBomb), new ItemStack(bomb, 1, BombType.BOMB_STANDARD.ordinal()), Items.arrow);
		GameRegistry.addShapelessRecipe(new ItemStack(arrowBombFire), new ItemStack(bomb, 1, BombType.BOMB_FIRE.ordinal()), Items.arrow);
		GameRegistry.addShapelessRecipe(new ItemStack(arrowBombWater), new ItemStack(bomb, 1, BombType.BOMB_WATER.ordinal()), Items.arrow);
		GameRegistry.addRecipe(new ItemStack(ZSSBlocks.ceramicJar,8), "c c","c c"," c ", 'c', Items.brick);
		GameRegistry.addRecipe(new ItemStack(ZSSItems.skillOrb, 1, SkillBase.bonusHeart.getId()), "HH","HH", 'H', heartPiece);
		GameRegistry.addRecipe(new ItemStack(ZSSItems.instrument, 1, ItemInstrument.Instrument.OCARINA_FAIRY.ordinal()), " c ","crc", 'c', Items.clay_ball, 'r', Items.reeds);
		GameRegistry.addShapelessRecipe(new ItemStack(tunicGoronLegs), tunicHeroLegs, new ItemStack(Items.dye, 1, 1));
		GameRegistry.addShapelessRecipe(new ItemStack(tunicGoronLegs), tunicZoraLegs, new ItemStack(Items.dye, 1, 1));
		GameRegistry.addShapelessRecipe(new ItemStack(tunicHeroLegs), tunicGoronLegs, new ItemStack(Items.dye, 1, 2));
		GameRegistry.addShapelessRecipe(new ItemStack(tunicHeroLegs), tunicZoraLegs, new ItemStack(Items.dye, 1, 2));
		GameRegistry.addShapelessRecipe(new ItemStack(tunicZoraLegs), tunicGoronLegs, new ItemStack(Items.dye, 1, 4));
		GameRegistry.addShapelessRecipe(new ItemStack(tunicZoraLegs), tunicHeroLegs, new ItemStack(Items.dye, 1, 4));
		// RUPEE CONVERSIONS: Small -> Big
		GameRegistry.addShapelessRecipe(new ItemStack(rupee, 1, ItemRupee.Rupee.BLUE_RUPEE.ordinal()), new ItemStack(rupee, 1, ItemRupee.Rupee.GREEN_RUPEE.ordinal()), new ItemStack(rupee, 1, ItemRupee.Rupee.GREEN_RUPEE.ordinal()), new ItemStack(rupee, 1, ItemRupee.Rupee.GREEN_RUPEE.ordinal()), new ItemStack(rupee, 1, ItemRupee.Rupee.GREEN_RUPEE.ordinal()), new ItemStack(rupee, 1, ItemRupee.Rupee.GREEN_RUPEE.ordinal()));
		GameRegistry.addShapelessRecipe(new ItemStack(rupee, 1, ItemRupee.Rupee.YELLOW_RUPEE.ordinal()), new ItemStack(rupee, 1, ItemRupee.Rupee.BLUE_RUPEE.ordinal()), new ItemStack(rupee, 1, ItemRupee.Rupee.BLUE_RUPEE.ordinal()));
		GameRegistry.addShapelessRecipe(new ItemStack(rupee, 1, ItemRupee.Rupee.RED_RUPEE.ordinal()), new ItemStack(rupee, 1, ItemRupee.Rupee.BLUE_RUPEE.ordinal()), new ItemStack(rupee, 1, ItemRupee.Rupee.BLUE_RUPEE.ordinal()), new ItemStack(rupee, 1, ItemRupee.Rupee.BLUE_RUPEE.ordinal()), new ItemStack(rupee, 1, ItemRupee.Rupee.BLUE_RUPEE.ordinal()));
		GameRegistry.addShapelessRecipe(new ItemStack(rupee, 1, ItemRupee.Rupee.RED_RUPEE.ordinal()), new ItemStack(rupee, 1, ItemRupee.Rupee.YELLOW_RUPEE.ordinal()), new ItemStack(rupee, 1, ItemRupee.Rupee.YELLOW_RUPEE.ordinal()));
		GameRegistry.addShapelessRecipe(new ItemStack(rupee, 1, ItemRupee.Rupee.PURPLE_RUPEE.ordinal()), new ItemStack(rupee, 1, ItemRupee.Rupee.YELLOW_RUPEE.ordinal()), new ItemStack(rupee, 1, ItemRupee.Rupee.YELLOW_RUPEE.ordinal()), new ItemStack(rupee, 1, ItemRupee.Rupee.YELLOW_RUPEE.ordinal()), new ItemStack(rupee, 1, ItemRupee.Rupee.YELLOW_RUPEE.ordinal()), new ItemStack(rupee, 1, ItemRupee.Rupee.YELLOW_RUPEE.ordinal()));
		GameRegistry.addShapelessRecipe(new ItemStack(rupee, 1, ItemRupee.Rupee.SILVER_RUPEE.ordinal()), new ItemStack(rupee, 1, ItemRupee.Rupee.RED_RUPEE.ordinal()), new ItemStack(rupee, 1, ItemRupee.Rupee.RED_RUPEE.ordinal()), new ItemStack(rupee, 1, ItemRupee.Rupee.RED_RUPEE.ordinal()), new ItemStack(rupee, 1, ItemRupee.Rupee.RED_RUPEE.ordinal()), new ItemStack(rupee, 1, ItemRupee.Rupee.RED_RUPEE.ordinal()));
		GameRegistry.addShapelessRecipe(new ItemStack(rupee, 1, ItemRupee.Rupee.SILVER_RUPEE.ordinal()), new ItemStack(rupee, 1, ItemRupee.Rupee.PURPLE_RUPEE.ordinal()), new ItemStack(rupee, 1, ItemRupee.Rupee.PURPLE_RUPEE.ordinal()));
		GameRegistry.addShapelessRecipe(new ItemStack(rupee, 1, ItemRupee.Rupee.GOLD_RUPEE.ordinal()), new ItemStack(rupee, 1, ItemRupee.Rupee.PURPLE_RUPEE.ordinal()), new ItemStack(rupee, 1, ItemRupee.Rupee.PURPLE_RUPEE.ordinal()), new ItemStack(rupee, 1, ItemRupee.Rupee.PURPLE_RUPEE.ordinal()), new ItemStack(rupee, 1, ItemRupee.Rupee.PURPLE_RUPEE.ordinal()));
		GameRegistry.addShapelessRecipe(new ItemStack(rupee, 1, ItemRupee.Rupee.GOLD_RUPEE.ordinal()), new ItemStack(rupee, 1, ItemRupee.Rupee.SILVER_RUPEE.ordinal()), new ItemStack(rupee, 1, ItemRupee.Rupee.SILVER_RUPEE.ordinal()));
		// RUPEE CONVERSIONS: Big -> Small
		GameRegistry.addShapelessRecipe(new ItemStack(rupee, ItemRupee.Rupee.BLUE_RUPEE.value, ItemRupee.Rupee.GREEN_RUPEE.ordinal()), new ItemStack(rupee, 1, ItemRupee.Rupee.BLUE_RUPEE.ordinal()));
		GameRegistry.addShapelessRecipe(new ItemStack(rupee, ItemRupee.Rupee.YELLOW_RUPEE.value/ItemRupee.Rupee.BLUE_RUPEE.value, ItemRupee.Rupee.BLUE_RUPEE.ordinal()), new ItemStack(rupee, 1, ItemRupee.Rupee.YELLOW_RUPEE.ordinal()));
		GameRegistry.addShapelessRecipe(new ItemStack(rupee, ItemRupee.Rupee.RED_RUPEE.value/ItemRupee.Rupee.YELLOW_RUPEE.value, ItemRupee.Rupee.YELLOW_RUPEE.ordinal()), new ItemStack(rupee, 1, ItemRupee.Rupee.RED_RUPEE.ordinal()));
		GameRegistry.addShapelessRecipe(new ItemStack(rupee, ItemRupee.Rupee.PURPLE_RUPEE.value/ItemRupee.Rupee.YELLOW_RUPEE.value, ItemRupee.Rupee.YELLOW_RUPEE.ordinal()), new ItemStack(rupee, 1, ItemRupee.Rupee.PURPLE_RUPEE.ordinal()));
		GameRegistry.addShapelessRecipe(new ItemStack(rupee, ItemRupee.Rupee.SILVER_RUPEE.value/ItemRupee.Rupee.PURPLE_RUPEE.value, ItemRupee.Rupee.PURPLE_RUPEE.ordinal()), new ItemStack(rupee, 1, ItemRupee.Rupee.SILVER_RUPEE.ordinal()));
		GameRegistry.addShapelessRecipe(new ItemStack(rupee, ItemRupee.Rupee.GOLD_RUPEE.value/ItemRupee.Rupee.SILVER_RUPEE.value, ItemRupee.Rupee.SILVER_RUPEE.ordinal()), new ItemStack(rupee, 1, ItemRupee.Rupee.GOLD_RUPEE.ordinal()));
		if (rupeesToEmeralds > 0) {
			GameRegistry.addShapelessRecipe(new ItemStack(rupee, rupeesToEmeralds, ItemRupee.Rupee.GREEN_RUPEE.ordinal()), new ItemStack(Items.emerald));
			GameRegistry.addShapelessRecipe(new ItemStack(Items.emerald), new ItemStack(rupee, rupeesToEmeralds, ItemRupee.Rupee.GREEN_RUPEE.ordinal()));
		}
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
	}
}
