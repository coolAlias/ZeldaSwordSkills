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
import net.minecraft.item.ItemBlockWithMetadata;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.MinecraftForge;
import zeldaswordskills.api.block.BlockWeight;
import zeldaswordskills.block.BlockChestInvisible.TileEntityChestInvisible;
import zeldaswordskills.block.tileentity.TileEntityCeramicJar;
import zeldaswordskills.block.tileentity.TileEntityChestLocked;
import zeldaswordskills.block.tileentity.TileEntityDungeonBlock;
import zeldaswordskills.block.tileentity.TileEntityDungeonCore;
import zeldaswordskills.block.tileentity.TileEntityGossipStone;
import zeldaswordskills.block.tileentity.TileEntityInscription;
import zeldaswordskills.block.tileentity.TileEntityPedestal;
import zeldaswordskills.block.tileentity.TileEntitySacredFlame;
import zeldaswordskills.client.render.block.RenderCeramicJar;
import zeldaswordskills.client.render.block.RenderChestLocked;
import zeldaswordskills.client.render.block.RenderGiantLever;
import zeldaswordskills.client.render.block.RenderSacredFlame;
import zeldaswordskills.client.render.block.RenderSpecialCrop;
import zeldaswordskills.client.render.block.RenderTileDungeonBlock;
import zeldaswordskills.client.render.block.RenderTileEntityCeramicJar;
import zeldaswordskills.client.render.block.RenderTileEntityChestLocked;
import zeldaswordskills.client.render.block.RenderTileEntityPedestal;
import zeldaswordskills.item.ItemBlockTime;
import zeldaswordskills.item.ItemCeramicJar;
import zeldaswordskills.item.ItemDungeonBlock;
import zeldaswordskills.item.ItemGossipStone;
import zeldaswordskills.item.ItemSacredFlame;
import zeldaswordskills.item.ItemSecretStone;
import zeldaswordskills.item.ItemWarpStone;
import zeldaswordskills.item.ZSSItems;
import zeldaswordskills.util.BlockRotationData;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ZSSBlocks
{
	private static int modBlockIndex, modBlockSecondIndex;
	private static final int MOD_BLOCK_INDEX_DEFAULT = 4080;
	private static final int MOD_BLOCK_INDEX_SECOND = 4050;

	public static Block
	barrierLight,
	barrierHeavy,
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
	// the following have a real Item, not an ItemBlock:
	doorLocked,
	doorLockedSmall,
	dungeonCore,
	dungeonStone;

	/**
	 * Initialize block index and other variables from config
	 */
	public static void initConfig(Configuration config) {
		modBlockIndex = config.getBlock("modBlockIndex", MOD_BLOCK_INDEX_DEFAULT).getInt();
		modBlockSecondIndex = config.getBlock("Secondary block index used if the first index runs out of IDs; must be lower than modBlockIndex by at least 10", MOD_BLOCK_INDEX_SECOND).getInt();
		if (modBlockSecondIndex + 9 > modBlockIndex) {
			throw new IllegalArgumentException("Second mod block index must be at least 10 lower than the original mod block index!");
		}
	}

	/**
	 * Call during FMLPreInitializationEvent to initialize and register all blocks
	 */
	public static void preInit() {
		secretStone = new BlockSecretStone(getNextBlockId(), Material.rock).setUnlocalizedName("zss.secretstone");
		dungeonCore = new BlockDungeonCore(getNextBlockId(), Material.rock).setUnlocalizedName("zss.dungeoncore");
		pedestal = new BlockPedestal(getNextBlockId()).setUnlocalizedName("zss.pedestal");
		chestLocked = new BlockChestLocked(getNextBlockId()).setUnlocalizedName("zss.chest_locked");
		doorLocked = new BlockDoorBoss(getNextBlockId(), Material.iron).setUnlocalizedName("zss.door_locked");
		sacredFlame = new BlockSacredFlame(getNextBlockId()).setUnlocalizedName("zss.sacredflame");
		ceramicJar = new BlockCeramicJar(getNextBlockId()).setUnlocalizedName("zss.ceramic_jar");
		barrierLight = new BlockHeavy(getNextBlockId(), Material.rock, BlockWeight.MEDIUM).setUnlocalizedName("zss.barrier_light");
		barrierHeavy = new BlockHeavy(getNextBlockId(), Material.rock, BlockWeight.VERY_HEAVY).setUnlocalizedName("zss.barrier_heavy");
		pegWooden = new BlockPeg(getNextBlockId(), ZSSBlockMaterials.pegWoodMaterial, BlockWeight.VERY_LIGHT).setUnlocalizedName("zss.peg_wooden");
		pegRusty = new BlockPeg(getNextBlockId(), ZSSBlockMaterials.pegRustyMaterial, BlockWeight.MEDIUM).setUnlocalizedName("zss.peg_rusty");
		dungeonStone = new BlockDungeonStone(getNextBlockId(), Material.rock).setUnlocalizedName("zss.dungeonstone");
		beamWooden = new BlockBar(getNextBlockId(), Material.wood).setUnlocalizedName("zss.beam_wooden");
		hookTarget = new BlockTargetDirectional(getNextBlockId(), Material.rock).setUnlocalizedName("zss.hook_target");
		hookTargetAll = new BlockTarget(getNextBlockId(), Material.rock).setUnlocalizedName("zss.hook_target_all");
		leverGiant = new BlockGiantLever(getNextBlockId()).setUnlocalizedName("zss.lever_giant");
		doorLockedSmall = new BlockDoorLocked(getNextBlockId(), Material.iron).setUnlocalizedName("zss.door_locked_small");
		gossipStone = new BlockGossipStone(getNextBlockId()).setUnlocalizedName("zss.gossip_stone");
		chestInvisible = new BlockChestInvisible(getNextBlockId()).setUnlocalizedName("zss.chest_invisible");
		bombFlower = new BlockBombFlower(getNextBlockId()).setUnlocalizedName("zss.bomb_flower");
		timeBlock = new BlockTime(getNextBlockId()).setUnlocalizedName("zss.time_block");
		inscription = new BlockSongInscription(getNextBlockId()).setUnlocalizedName("zss.inscription");
		warpStone = new BlockWarpStone(getNextBlockId()).setUnlocalizedName("zss.warp_stone");

		register();
	}

	private static int getNextBlockId() {
		if (modBlockIndex < 4095) {
			return modBlockIndex++;
		}
		return modBlockSecondIndex++;
	}

	/**
	 * Registers all custom Item renderers
	 */
	@SideOnly(Side.CLIENT)
	public static void registerRenderers() {
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityCeramicJar.class, new RenderTileEntityCeramicJar());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityChestLocked.class, new RenderTileEntityChestLocked());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityPedestal.class, new RenderTileEntityPedestal());
		RenderingRegistry.registerBlockHandler(new RenderTileDungeonBlock());
		RenderingRegistry.registerBlockHandler(new RenderCeramicJar());
		RenderingRegistry.registerBlockHandler(new RenderChestLocked());
		RenderingRegistry.registerBlockHandler(new RenderGiantLever());
		RenderingRegistry.registerBlockHandler(new RenderSacredFlame());
		RenderingRegistry.registerBlockHandler(new RenderSpecialCrop());
	}

	private static void register() {
		GameRegistry.registerBlock(barrierLight, barrierLight.getUnlocalizedName());
		GameRegistry.registerBlock(barrierHeavy, barrierHeavy.getUnlocalizedName());
		GameRegistry.registerBlock(pegWooden, pegWooden.getUnlocalizedName());
		GameRegistry.registerBlock(pegRusty, pegRusty.getUnlocalizedName());
		GameRegistry.registerBlock(ceramicJar, ItemCeramicJar.class, ceramicJar.getUnlocalizedName());
		GameRegistry.registerBlock(chestLocked, chestLocked.getUnlocalizedName());
		GameRegistry.registerBlock(chestInvisible, chestInvisible.getUnlocalizedName());
		GameRegistry.registerBlock(doorLocked, doorLocked.getUnlocalizedName());
		GameRegistry.registerBlock(doorLockedSmall, doorLockedSmall.getUnlocalizedName());
		GameRegistry.registerBlock(dungeonCore, ItemDungeonBlock.class, dungeonCore.getUnlocalizedName());
		GameRegistry.registerBlock(dungeonStone, ItemDungeonBlock.class, dungeonStone.getUnlocalizedName());
		GameRegistry.registerBlock(beamWooden, beamWooden.getUnlocalizedName());
		GameRegistry.registerBlock(hookTarget, hookTarget.getUnlocalizedName());
		MinecraftForge.setBlockHarvestLevel(hookTarget, "pickaxe", 1);
		GameRegistry.registerBlock(hookTargetAll, hookTargetAll.getUnlocalizedName());
		MinecraftForge.setBlockHarvestLevel(hookTargetAll, "pickaxe", 1);
		GameRegistry.registerBlock(leverGiant, leverGiant.getUnlocalizedName());
		GameRegistry.registerBlock(pedestal, ItemBlockWithMetadata.class, pedestal.getUnlocalizedName());
		GameRegistry.registerBlock(secretStone, ItemSecretStone.class, secretStone.getUnlocalizedName());
		GameRegistry.registerBlock(sacredFlame, ItemSacredFlame.class, sacredFlame.getUnlocalizedName());
		GameRegistry.registerBlock(timeBlock, ItemBlockTime.class, timeBlock.getUnlocalizedName());
		GameRegistry.registerBlock(inscription, inscription.getUnlocalizedName());
		GameRegistry.registerBlock(warpStone, ItemWarpStone.class, warpStone.getUnlocalizedName());
		GameRegistry.registerBlock(gossipStone, ItemGossipStone.class, gossipStone.getUnlocalizedName());
		MinecraftForge.setBlockHarvestLevel(gossipStone, "pickaxe", 2);
		GameRegistry.registerBlock(bombFlower, bombFlower.getUnlocalizedName());

		GameRegistry.registerTileEntity(TileEntityCeramicJar.class, "tileEntityCeramicJar");
		GameRegistry.registerTileEntity(TileEntityChestLocked.class, "tileEntityChestLocked");
		GameRegistry.registerTileEntity(TileEntityChestInvisible.class, "tileEntityChestInvisible");
		GameRegistry.registerTileEntity(TileEntityDungeonBlock.class, "tileEntityDungeonBlock");
		GameRegistry.registerTileEntity(TileEntityDungeonCore.class, "tileEntityDungeonCore");
		GameRegistry.registerTileEntity(TileEntityPedestal.class, "tileEntityPedestal");
		GameRegistry.registerTileEntity(TileEntityGossipStone.class, "tileEntityGossipStone");
		GameRegistry.registerTileEntity(TileEntityInscription.class, "tileEntityInscription");
		GameRegistry.registerTileEntity(TileEntitySacredFlame.class, "tileEntitySacredFlame");

		// register item blocks for comparator sorting:
		try {
			for (Field f: ZSSBlocks.class.getFields()) {
				if (Block.class.isAssignableFrom(f.getType())) {
					Block block = (Block) f.get(null);
					if (block != null) {
						ZSSItems.registerItemBlock(new ItemStack(block).getItem());
					}
				}
			}
		} catch(Exception e) {

		}

		// Register rotation types for custom blocks
		BlockRotationData.registerCustomBlockRotation(chestLocked, BlockRotationData.Rotation.PISTON_CONTAINER);
		BlockRotationData.registerCustomBlockRotation(chestInvisible, BlockRotationData.Rotation.PISTON_CONTAINER);
		BlockRotationData.registerCustomBlockRotation(inscription, BlockRotationData.Rotation.PISTON_CONTAINER);
		BlockRotationData.registerCustomBlockRotation(leverGiant, BlockRotationData.Rotation.LEVER);
	}
}
