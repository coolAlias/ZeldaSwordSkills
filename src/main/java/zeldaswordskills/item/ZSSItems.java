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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import net.minecraft.block.BlockDispenser;
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
import net.minecraft.util.WeightedRandomChestContent;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.ChestGenHooks;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.util.EnumHelper;
import zeldaswordskills.ZSSMain;
import zeldaswordskills.api.block.BlockWeight;
import zeldaswordskills.api.entity.BombType;
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
import zeldaswordskills.entity.EntityChu;
import zeldaswordskills.entity.EntityKeese;
import zeldaswordskills.entity.EntityOctorok;
import zeldaswordskills.entity.buff.Buff;
import zeldaswordskills.entity.projectile.EntityMagicSpell.MagicType;
import zeldaswordskills.handler.TradeHandler;
import zeldaswordskills.item.dispenser.BehaviorDispenseCustomMobEgg;
import zeldaswordskills.lib.Config;
import zeldaswordskills.lib.ModInfo;
import zeldaswordskills.skills.SkillBase;
import zeldaswordskills.util.LogHelper;
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
	private static Comparator<Item> itemComparator = new Comparator<Item>() {
		@Override
		public int compare(Item a, Item b) {
			if (itemList.containsKey(a) && itemList.containsKey(b)) {
				return itemList.get(a) - itemList.get(b);
			} else {
				LogHelper.warning(String.format("A mod item %s or %s is missing a comparator mapping", a.getUnlocalizedName(), b.getUnlocalizedName()));
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
	keySkeleton,
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
	dekuLeaf,
	dekuNut,
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
	pendant,
	masterOre,
	jellyChu,
	treasure,
	linksHouse;

	//================ NO TAB ================//
	public static Item
	doorLocked,
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
	tunicZoraLegs;
	//tunicZoraBoots; // flippers?

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
	swordMasterTrue;

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
	eggKeese,
	eggOctorok;

	/**
	 * Initializes mod item indices from configuration file
	 */
	public static void init(Configuration config) {
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
	}

	/**
	 * Call during FMLPreInitializationEvent to initialize and register all items.
	 * Also calls various initialization methods for special Items, registers recipes,
	 * adds loot to vanilla chests, trades, dispenser behavior, and adds drops.
	 */
	public static void init() {
		ZSSItems.initItems();
		ZSSItems.registerItems();
		ItemChuJelly.initializeJellies();
		ItemHeroBow.initializeArrows();
		ItemSlingshot.initializeSeeds();
		ZSSItems.registerRecipes();
		ZSSItems.addVanillaDungeonLoot();
		TradeHandler.registerTrades();
		ZSSItems.addGrassDrops();
		ZSSItems.addDispenserBehaviors();
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
		return true;
	}

	private static void initItems() {
		// SKILL TAB ITEMS
		skillOrb = new ItemSkillOrb().setUnlocalizedName("zss.skillorb");
		heartPiece = new ItemMiscZSS(12).setUnlocalizedName("zss.heartpiece").setCreativeTab(ZSSCreativeTabs.tabSkills);
		skillWiper = new ItemMiscZSS(0).setUnlocalizedName("zss.skill_wiper").setCreativeTab(ZSSCreativeTabs.tabSkills);

		// COMBAT TAB ITEMS
		tunicHeroHelm = new ItemArmorTunic(ZSSMain.proxy.addArmor("tunic"), ArmorIndex.TYPE_HELM).setUnlocalizedName("zss.hero_tunic_helm");
		tunicHeroChest = new ItemArmorTunic(ZSSMain.proxy.addArmor("tunic"), ArmorIndex.TYPE_CHEST).setUnlocalizedName("zss.hero_tunic_chest");
		tunicHeroLegs = new ItemArmorTunic(ZSSMain.proxy.addArmor("tunic"), ArmorIndex.TYPE_LEGS).setUnlocalizedName("zss.hero_tunic_legs");

		tunicGoronHelm = new ItemArmorTunic(ZSSMain.proxy.addArmor("tunic"), ArmorIndex.TYPE_HELM).setUnlocalizedName("zss.goron_tunic_helm");
		tunicGoronChest = new ItemArmorTunic(ZSSMain.proxy.addArmor("tunic"), ArmorIndex.TYPE_CHEST).setUnlocalizedName("zss.goron_tunic_chest");
		tunicGoronLegs = new ItemArmorTunic(ZSSMain.proxy.addArmor("tunic"), ArmorIndex.TYPE_LEGS).setUnlocalizedName("zss.goron_tunic_legs");

		tunicZoraHelm = new ItemArmorTunic(ZSSMain.proxy.addArmor("tunic"), ArmorIndex.TYPE_HELM).setUnlocalizedName("zss.zora_tunic_helm");
		tunicZoraChest = new ItemArmorTunic(ZSSMain.proxy.addArmor("tunic"), ArmorIndex.TYPE_CHEST).setEffect(new PotionEffect(Potion.waterBreathing.getId(), 90, 0)).setUnlocalizedName("zss.zora_tunic_chest");
		tunicZoraLegs = new ItemArmorTunic(ZSSMain.proxy.addArmor("tunic"), ArmorIndex.TYPE_LEGS).setUnlocalizedName("zss.zora_tunic_legs");

		tunicHeroBoots = new ItemArmorTunic(ZSSMain.proxy.addArmor("tunic"), ArmorIndex.TYPE_BOOTS).setUnlocalizedName("zss.hero_tunic_boots");
		bootsHeavy = new ItemArmorBoots(ArmorMaterial.IRON, ZSSMain.proxy.addArmor("boots")).setUnlocalizedName("zss.boots_heavy");
		bootsHover = new ItemArmorBoots(ArmorMaterial.CHAIN, ZSSMain.proxy.addArmor("boots")).setUnlocalizedName("zss.boots_hover");
		bootsPegasus = new ItemArmorBoots(ArmorMaterial.CHAIN, ZSSMain.proxy.addArmor("boots")).setUnlocalizedName("zss.boots_pegasus");
		bootsRubber = new ItemArmorBoots(ArmorMaterial.CHAIN, ZSSMain.proxy.addArmor("boots")).setUnlocalizedName("zss.boots_rubber");

		shieldDeku = new ItemZeldaShield(30, 3F, 5F).setUnlocalizedName("zss.shield_deku");
		shieldHylian = new ItemZeldaShield(18, 5F, 3.5F).setUnlocalizedName("zss.shield_hylian");
		shieldMirror = new ItemZeldaShield(24, 4F, 4F).setUnlocalizedName("zss.shield_mirror");

		swordKokiri = new ItemZeldaSword(ToolMaterial.IRON, -1.0F).setUnlocalizedName("zss.sword_kokiri").setMaxDamage(256);
		swordOrdon = new ItemZeldaSword(ToolMaterial.IRON, 1.0F).setUnlocalizedName("zss.sword_ordon").setMaxDamage(512);
		swordGiant = new ItemZeldaSword(ToolMaterial.IRON, 6.0F, true).setUnlocalizedName("zss.sword_giant").setMaxDamage(32);
		swordBiggoron = new ItemZeldaSword(ToolMaterial.IRON, 6.0F, true).setNoItemOnBreak().setUnlocalizedName("zss.sword_biggoron").setMaxDamage(0);
		swordMaster = new ItemZeldaSword(ToolMaterial.EMERALD, 2.0F).setMasterSword().setUnlocalizedName("zss.sword_master").setMaxDamage(0);
		swordTempered = new ItemZeldaSword(ToolMaterial.EMERALD, 4.0F).setMasterSword().setUnlocalizedName("zss.sword_tempered").setMaxDamage(0);
		swordGolden = new ItemZeldaSword(ToolMaterial.EMERALD, 6.0F).setMasterSword().setUnlocalizedName("zss.sword_golden").setMaxDamage(0);
		swordMasterTrue = new ItemZeldaSword(ToolMaterial.EMERALD, 8.0F).setMasterSword().setUnlocalizedName("zss.sword_master_true").setMaxDamage(0);
		swordBroken = new ItemBrokenSword().setUnlocalizedName("zss.sword_broken");

		hammer = new ItemHammer(BlockWeight.VERY_LIGHT, 8.0F, 50.0F).setUnlocalizedName("zss.hammer");
		hammerSkull = new ItemHammer(BlockWeight.MEDIUM, 12.0F, 50.0F).setUnlocalizedName("zss.hammer_skull");
		hammerMegaton = new ItemHammer(BlockWeight.VERY_HEAVY, 16.0F, 50.0F).setUnlocalizedName("zss.hammer_megaton");

		boomerang = new ItemBoomerang(4.0F, 12).setUnlocalizedName("zss.boomerang");
		boomerangMagic = new ItemBoomerang(6.0F, 24).setCaptureAll().setUnlocalizedName("zss.boomerang_magic");

		heroBow = new ItemHeroBow().setUnlocalizedName("zss.bow_hero");
		arrowBomb = new ItemZeldaArrow("arrow_bomb", false);
		arrowBombFire = new ItemZeldaArrow("arrow_bomb_fire", false);
		arrowBombWater = new ItemZeldaArrow("arrow_bomb_water", false);
		arrowFire = new ItemZeldaArrow("arrow_fire", true);
		arrowIce = new ItemZeldaArrow("arrow_ice", true);
		arrowLight = new ItemZeldaArrow("arrow_light", true);

		slingshot = new ItemSlingshot().setUnlocalizedName("zss.slingshot");
		scattershot = new ItemSlingshot(3, 30F).setUnlocalizedName("zss.scattershot");
		supershot = new ItemSlingshot(5, 15F).setUnlocalizedName("zss.supershot");

		// BLOCK TAB ITEMS
		doorLocked = new ItemDoorLocked().setUnlocalizedName("zss.doorlocked");

		// KEYS TAB ITEMS
		keyBig = new ItemKeyBig().setUnlocalizedName("zss.keybig").setFull3D();
		keySmall = new ItemMiscZSS(6).setUnlocalizedName("zss.keysmall").setFull3D().setCreativeTab(ZSSCreativeTabs.tabKeys);
		keySkeleton = new ItemMiscZSS(32).setUnlocalizedName("zss.keyskeleton").setFull3D().setMaxStackSize(1).setCreativeTab(ZSSCreativeTabs.tabKeys);

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
		potionRed = new ItemZeldaPotion(0, 0.0F, 20.0F).setUnlocalizedName("zss.potion_red");
		potionGreen = new ItemZeldaPotion(20, 40.0F, 0.0F).setUnlocalizedName("zss.potion_green");
		potionBlue = new ItemZeldaPotion(20, 40.0F, 40.0F).setUnlocalizedName("zss.potion_blue");
		potionYellow = new ItemZeldaPotion().setBuffEffect(Buff.RESIST_SHOCK, 6000, 100, 1.0F).setUnlocalizedName("zss.potion_yellow");
		rodFire = new ItemMagicRod(MagicType.FIRE, 8.0F, 8.0F).setUnlocalizedName("zss.rod_fire");
		rodIce = new ItemMagicRod(MagicType.ICE, 6.0F, 8.0F).setUnlocalizedName("zss.rod_ice");
		rodTornado = new ItemMagicRod(MagicType.WIND, 4.0F, 4.0F).setUnlocalizedName("zss.rod_tornado");
		whip = new ItemWhip().setUnlocalizedName("zss.whip");

		// MASK TAB ITEMS
		maskBlast = new ItemMask(ArmorMaterial.IRON, ZSSMain.proxy.addArmor("mask")).setUnlocalizedName("zss.mask_blast");
		maskBunny = new ItemMask(WOOD, ZSSMain.proxy.addArmor("mask")).setPrice(1, 64).setUnlocalizedName("zss.mask_bunny");
		maskCouples = new ItemMask(WOOD, ZSSMain.proxy.addArmor("mask")).setPrice(40, 32).setUnlocalizedName("zss.mask_couples");
		maskGerudo = new ItemMask(WOOD, ZSSMain.proxy.addArmor("mask")).setUnlocalizedName("zss.mask_gerudo");
		maskGiants = new ItemMask(WOOD, ZSSMain.proxy.addArmor("mask")).setUnlocalizedName("zss.mask_giants");
		maskGibdo = new ItemMask(WOOD, ZSSMain.proxy.addArmor("mask")).setUnlocalizedName("zss.mask_gibdo");
		maskHawkeye = new ItemMask(WOOD, ZSSMain.proxy.addArmor("mask")).setUnlocalizedName("zss.mask_hawkeye");
		maskKeaton = new ItemMask(WOOD, ZSSMain.proxy.addArmor("mask")).setPrice(8, 16).setUnlocalizedName("zss.mask_keaton");
		maskScents = new ItemMask(WOOD, ZSSMain.proxy.addArmor("mask")).setPrice(32, 32).setUnlocalizedName("zss.mask_scents");
		maskSkull = new ItemMask(WOOD, ZSSMain.proxy.addArmor("mask")).setPrice(20, 10).setUnlocalizedName("zss.mask_skull");
		maskSpooky = new ItemMask(WOOD, ZSSMain.proxy.addArmor("mask")).setPrice(16, 8).setUnlocalizedName("zss.mask_spooky");
		maskStone = new ItemMask(WOOD, ZSSMain.proxy.addArmor("mask")).setEffect(new PotionEffect(Potion.invisibility.getId(), 100, 0)).setUnlocalizedName("zss.mask_stone");
		maskTruth = new ItemMask(WOOD, ZSSMain.proxy.addArmor("mask")).setUnlocalizedName("zss.mask_truth");
		maskDeku = new ItemMask(WOOD, ZSSMain.proxy.addArmor("mask")).setUnlocalizedName("zss.mask_deku");
		maskGoron = new ItemMask(WOOD, ZSSMain.proxy.addArmor("mask")).setUnlocalizedName("zss.mask_goron");
		maskZora = new ItemMask(WOOD, ZSSMain.proxy.addArmor("mask")).setUnlocalizedName("zss.mask_zora");
		maskFierce = new ItemMask(WOOD, ZSSMain.proxy.addArmor("mask")).setUnlocalizedName("zss.mask_fierce");
		maskMajora = new ItemMask(WOOD, ZSSMain.proxy.addArmor("mask")).setEffect(new PotionEffect(Potion.wither.getId(), 100, 1)).setUnlocalizedName("zss.mask_majora");

		// MISCELLANEOUS TAB ITEMS
		pendant = new ItemPendant().setUnlocalizedName("zss.pendant");
		crystalSpirit = new ItemMiscZSS(0).setUnlocalizedName("zss.spirit_crystal_empty").setMaxStackSize(1).setCreativeTab(ZSSCreativeTabs.tabTools);
		masterOre = new ItemMiscZSS(24).setUnlocalizedName("zss.masterore");
		rocsFeather = new ItemMiscZSS(12).setUnlocalizedName("zss.rocs_feather").setCreativeTab(ZSSCreativeTabs.tabTools);
		dekuLeaf = new ItemDekuLeaf().setUnlocalizedName("zss.deku_leaf");
		dekuNut = new ItemMiscZSS(2).setUnlocalizedName("zss.deku_nut").setCreativeTab(ZSSCreativeTabs.tabTools);
		jellyChu = new ItemChuJelly().setUnlocalizedName("zss.jelly_chu");
		treasure = new ItemTreasure().setUnlocalizedName("zss.treasure");
		linksHouse = new ItemBuilderSeed(LinksHouse.class, "You must first clear this area of debris!", "deku_nut").setUnlocalizedName("zss.links_house");

		// ITEMS WITH NO TAB
		heldBlock = new ItemHeldBlock().setUnlocalizedName("zss.held_block");
		powerPiece = new ItemPickupOnly().setUnlocalizedName("zss.power_piece");
		smallHeart = new ItemPickupOnly().setUnlocalizedName("zss.heart");
		throwingRock = new Item().setUnlocalizedName("zss.throwing_rock").setTextureName(ModInfo.ID + ":throwing_rock").setMaxStackSize(16);

		// Custom Spawn Eggs
		eggSpawner = new ItemCustomEgg().setUnlocalizedName("zss.spawn_egg");
		eggChu = new ItemCustomVariantEgg(EntityChu.class, "chu").setUnlocalizedName("zss.eggChu");
		eggKeese = new ItemCustomVariantEgg(EntityKeese.class, "keese").setUnlocalizedName("zss.eggKeese");
		eggOctorok = new ItemCustomVariantEgg(EntityOctorok.class, "octorok").setUnlocalizedName("zss.eggOctorok");
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
						GameRegistry.registerItem(item, item.getUnlocalizedName().replace("item.", "").trim());
					}
				}
			}
		} catch(Exception e) {

		}
	}

	private static void registerRecipes() {
		if (allowGoldSmelting) {
			// func_151396_a is addSmelting()
			FurnaceRecipes.smelting().func_151396_a(Items.golden_sword, new ItemStack(Items.gold_ingot), 0.0F);
		}
		GameRegistry.addRecipe(new ItemStack(ZSSBlocks.pedestal,3,0x8), "qqq","qpq","qqq", 'q', Blocks.quartz_block, 'p', new ItemStack(ZSSBlocks.pedestal,1,0x8));
		GameRegistry.addRecipe(new ItemStack(ZSSBlocks.beamWooden), "b","b","b", 'b', Blocks.planks);
		GameRegistry.addRecipe(new ItemStack(arrowBomb), "b","a", 'b', new ItemStack(bomb,1,BombType.BOMB_STANDARD.ordinal()), 'a', Items.arrow);
		GameRegistry.addRecipe(new ItemStack(arrowBombFire), "b","a", 'b', new ItemStack(bomb,1,BombType.BOMB_FIRE.ordinal()), 'a', Items.arrow);
		GameRegistry.addRecipe(new ItemStack(arrowBombWater), "b","a", 'b', new ItemStack(bomb,1,BombType.BOMB_WATER.ordinal()), 'a', Items.arrow);
		GameRegistry.addRecipe(new ItemStack(ZSSBlocks.ceramicJar,8), "c c","c c"," c ", 'c', Items.brick);
		GameRegistry.addRecipe(new ItemStack(ZSSItems.skillOrb,1,SkillBase.bonusHeart.getId()), "HH","HH", 'H', heartPiece);
		GameRegistry.addShapelessRecipe(new ItemStack(tunicGoronHelm), tunicHeroHelm, new ItemStack(Items.dye,1,1));
		GameRegistry.addShapelessRecipe(new ItemStack(tunicGoronLegs), tunicHeroLegs, new ItemStack(Items.dye,1,1));
		GameRegistry.addShapelessRecipe(new ItemStack(tunicZoraHelm), tunicHeroHelm, new ItemStack(Items.dye,1,4));
		GameRegistry.addShapelessRecipe(new ItemStack(tunicZoraLegs), tunicHeroLegs, new ItemStack(Items.dye,1,4));
	}

	/**
	 * Adds some special loot to vanilla chests
	 */
	private static void addVanillaDungeonLoot() {
		if (enableBombLoot) {
			addLootToAll(new WeightedRandomChestContent(new ItemStack(bomb,1,BombType.BOMB_STANDARD.ordinal()), 1, 3, Config.getBombWeight()), true, true);
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
		BlockDispenser.dispenseBehaviorRegistry.putObject(eggKeese, new BehaviorDispenseCustomMobEgg());
		BlockDispenser.dispenseBehaviorRegistry.putObject(eggOctorok, new BehaviorDispenseCustomMobEgg());
	}
}
