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

package zeldaswordskills.block;

import java.lang.reflect.Field;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;
import zeldaswordskills.ZSSMain;
import zeldaswordskills.api.block.BlockWeight;
import zeldaswordskills.block.tileentity.TileEntityCeramicJar;
import zeldaswordskills.block.tileentity.TileEntityChestLocked;
import zeldaswordskills.block.tileentity.TileEntityDungeonCore;
import zeldaswordskills.block.tileentity.TileEntityDungeonStone;
import zeldaswordskills.block.tileentity.TileEntityGossipStone;
import zeldaswordskills.block.tileentity.TileEntityInscription;
import zeldaswordskills.block.tileentity.TileEntityPedestal;
import zeldaswordskills.block.tileentity.TileEntitySacredFlame;
import zeldaswordskills.item.ItemBlockPedestal;
import zeldaswordskills.item.ItemBlockUnbreakable;
import zeldaswordskills.item.ItemCeramicJar;
import zeldaswordskills.item.ItemDoorBoss;
import zeldaswordskills.item.ItemDoorLocked;
import zeldaswordskills.item.ItemDungeonBlock;
import zeldaswordskills.item.ItemGossipStone;
import zeldaswordskills.item.ItemMetadataBlock;
import zeldaswordskills.item.ItemModBlock;
import zeldaswordskills.item.ItemSacredFlame;
import zeldaswordskills.item.ItemWarpStone;
import zeldaswordskills.item.ZSSItems;
import zeldaswordskills.ref.ModInfo;
import zeldaswordskills.util.BlockRotationData;

public class ZSSBlocks
{
	public static Block
	heavyBlock,
	timeBlock,
	bombFlower,
	ceramicJar,
	chestLocked,
	chestInvisible,
	pedestal,
	pegWooden,
	pegRusty,
	leverGiant,
	beamWooden,
	hookTarget,
	hookTargetAll,
	gossipStone,
	inscription,
	warpStone,
	secretStone,
	sacredFlame,
	doorBoss,
	doorLocked,
	dungeonCore,
	dungeonStone;

	/**
	 * Call during FMLPreInitializationEvent to initialize and register all blocks
	 */
	public static void preInit() {
		// NOTE: pass getUnlocalizedString WITHOUT 'tile.' or blockstate=>model will get confused
		// NOTE: new Object[]{args...} is required for vararg constructor invocation via Reflection
		beamWooden = new BlockBar(Material.wood).setUnlocalizedName("beam_wooden");
		GameRegistry.registerBlock(beamWooden, ItemModBlock.class, beamWooden.getUnlocalizedName().substring(5));
		bombFlower = new BlockBombFlower().setUnlocalizedName("bomb_flower");
		GameRegistry.registerBlock(bombFlower, null, bombFlower.getUnlocalizedName().substring(5));
		ceramicJar = new BlockCeramicJar().setUnlocalizedName("ceramic_jar");
		GameRegistry.registerBlock(ceramicJar, ItemCeramicJar.class, ceramicJar.getUnlocalizedName().substring(5));
		GameRegistry.registerTileEntity(TileEntityCeramicJar.class, ModInfo.ID + ":tileEntityCeramicJar");
		chestInvisible = new BlockChestInvisible().setUnlocalizedName("chest_invisible");
		GameRegistry.registerBlock(chestInvisible, ItemModBlock.class, chestInvisible.getUnlocalizedName().substring(5));
		GameRegistry.registerTileEntity(TileEntityChestLocked.class, ModInfo.ID + ":tileEntityChestInvisible");
		chestLocked = new BlockChestLocked().setUnlocalizedName("chest_locked");
		GameRegistry.registerBlock(chestLocked, ItemModBlock.class, chestLocked.getUnlocalizedName().substring(5));
		GameRegistry.registerTileEntity(TileEntityChestLocked.class, ModInfo.ID + ":tileEntityChestLocked");
		doorBoss = new BlockDoorBoss(Material.iron).setUnlocalizedName("door_boss");
		GameRegistry.registerBlock(doorBoss, ItemDoorBoss.class, doorBoss.getUnlocalizedName().substring(5));
		doorLocked = new BlockDoorLocked(Material.iron).setUnlocalizedName("door_locked");
		GameRegistry.registerBlock(doorLocked, ItemDoorLocked.class, doorLocked.getUnlocalizedName().substring(5));
		dungeonCore = new BlockDungeonCore(Material.rock).setUnlocalizedName("dungeon_core");
		GameRegistry.registerBlock(dungeonCore, ItemDungeonBlock.class, dungeonCore.getUnlocalizedName().substring(5), new Object[]{new String[]{"minecraft:stone:1", "minecraft:stone:2"}});
		GameRegistry.registerTileEntity(TileEntityDungeonCore.class, ModInfo.ID + ":tileEntityDungeonCore");
		dungeonStone = new BlockDungeonStone(Material.rock).setUnlocalizedName("dungeon_stone");
		GameRegistry.registerBlock(dungeonStone, ItemDungeonBlock.class, dungeonStone.getUnlocalizedName().substring(5), new Object[]{new String[]{"minecraft:stone:5", "minecraft:stone:6"}});
		GameRegistry.registerTileEntity(TileEntityDungeonStone.class, ModInfo.ID + ":tileEntityDungeonStone");
		gossipStone = new BlockGossipStone().setUnlocalizedName("gossip_stone");
		GameRegistry.registerBlock(gossipStone, ItemGossipStone.class, gossipStone.getUnlocalizedName().substring(5));
		GameRegistry.registerTileEntity(TileEntityGossipStone.class, ModInfo.ID + ":tileEntityGossipStone");
		heavyBlock = new BlockHeavy(Material.rock).setUnlocalizedName("heavy_block");
		GameRegistry.registerBlock(heavyBlock, ItemMetadataBlock.class, heavyBlock.getUnlocalizedName().substring(5), new Object[]{new String[]{BlockHeavy.EnumType.LIGHT.getName(), BlockHeavy.EnumType.HEAVY.getName()}});
		hookTarget = new BlockTargetDirectional(Material.rock).setUnlocalizedName("hook_target");
		GameRegistry.registerBlock(hookTarget, ItemModBlock.class, hookTarget.getUnlocalizedName().substring(5));
		hookTargetAll = new BlockTarget(Material.rock).setUnlocalizedName("hook_target_all");
		GameRegistry.registerBlock(hookTargetAll, ItemModBlock.class, hookTargetAll.getUnlocalizedName().substring(5));
		inscription = new BlockSongInscription().setUnlocalizedName("inscription");
		GameRegistry.registerBlock(inscription, ItemModBlock.class, inscription.getUnlocalizedName().substring(5));
		GameRegistry.registerTileEntity(TileEntityInscription.class, ModInfo.ID + ":tileEntityInscription");
		leverGiant = new BlockGiantLever().setUnlocalizedName("lever_giant");
		GameRegistry.registerBlock(leverGiant, ItemModBlock.class, leverGiant.getUnlocalizedName().substring(5));
		pegWooden = new BlockPeg(ZSSBlockMaterials.pegWoodMaterial, BlockWeight.VERY_LIGHT).setUnlocalizedName("peg_wooden");
		GameRegistry.registerBlock(pegWooden, ItemModBlock.class, pegWooden.getUnlocalizedName().substring(5));
		pegRusty = new BlockPeg(ZSSBlockMaterials.pegRustyMaterial, BlockWeight.MEDIUM).setUnlocalizedName("peg_rusty");
		GameRegistry.registerBlock(pegRusty, ItemModBlock.class, pegRusty.getUnlocalizedName().substring(5));
		pedestal = new BlockPedestal().setUnlocalizedName("pedestal");
		GameRegistry.registerBlock(pedestal, ItemBlockPedestal.class, pedestal.getUnlocalizedName().substring(5));
		GameRegistry.registerTileEntity(TileEntityPedestal.class, ModInfo.ID + ":tileEntityPedestal");
		sacredFlame = new BlockSacredFlame().setUnlocalizedName("sacred_flame");
		GameRegistry.registerBlock(sacredFlame, ItemSacredFlame.class, sacredFlame.getUnlocalizedName().substring(5));
		GameRegistry.registerTileEntity(TileEntitySacredFlame.class, ModInfo.ID + ":tileEntitySacredFlame");
		secretStone = new BlockSecretStone(Material.rock).setUnlocalizedName("secret_stone");
		GameRegistry.registerBlock(secretStone, ItemBlockUnbreakable.class, secretStone.getUnlocalizedName().substring(5));
		timeBlock = new BlockTime().setUnlocalizedName("time_block");
		GameRegistry.registerBlock(timeBlock, ItemMetadataBlock.class, timeBlock.getUnlocalizedName().substring(5), new Object[]{new String[]{BlockTime.EnumType.TIME.getName(), BlockTime.EnumType.ROYAL.getName()}});
		warpStone = new BlockWarpStone().setUnlocalizedName("warp_stone");
		GameRegistry.registerBlock(warpStone, ItemWarpStone.class, warpStone.getUnlocalizedName().substring(5));
		// register block items for creative tab comparator sorting:
		try {
			for (Field f: ZSSBlocks.class.getFields()) {
				if (Block.class.isAssignableFrom(f.getType())) {
					Block block = (Block) f.get(null);
					if (block != null) {
						ItemStack stack = new ItemStack(block);
						if (stack != null && stack.getItem() != null) {
							ZSSItems.registerItemBlock(stack.getItem());
						}
						if (block instanceof IVanillaRotation) {
							ZSSMain.logger.debug("Registering custom rotation for " + block.getUnlocalizedName());
							BlockRotationData.registerCustomBlockRotation(block, ((IVanillaRotation) block).getRotationPattern());
						}
					}
				}
			}
		} catch (Exception e) {
			ZSSMain.logger.warn("Caught exception while registering block ItemBlocks: " + e.toString());
			e.printStackTrace();
		}
	}
}
