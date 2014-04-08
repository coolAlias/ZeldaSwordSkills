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

package zeldaswordskills.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.ItemBlockWithMetadata;
import net.minecraftforge.common.Configuration;
import zeldaswordskills.api.block.BlockWeight;
import zeldaswordskills.block.tileentity.TileEntityCeramicJar;
import zeldaswordskills.block.tileentity.TileEntityChestLocked;
import zeldaswordskills.block.tileentity.TileEntityDungeonBlock;
import zeldaswordskills.block.tileentity.TileEntityDungeonCore;
import zeldaswordskills.block.tileentity.TileEntityPedestal;
import zeldaswordskills.block.tileentity.TileEntitySacredFlame;
import zeldaswordskills.client.render.block.RenderCeramicJar;
import zeldaswordskills.client.render.block.RenderChestLocked;
import zeldaswordskills.client.render.block.RenderSacredFlame;
import zeldaswordskills.client.render.block.RenderTileDungeonBlock;
import zeldaswordskills.client.render.block.RenderTileEntityCeramicJar;
import zeldaswordskills.client.render.block.RenderTileEntityChestLocked;
import zeldaswordskills.client.render.block.RenderTileEntityPedestal;
import zeldaswordskills.item.ItemCeramicJar;
import zeldaswordskills.item.ItemDungeonBlock;
import zeldaswordskills.item.ItemSacredFlame;
import zeldaswordskills.item.ItemSecretStone;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ZSSBlocks
{
	private static int modBlockIndex;
	private static final int MOD_BLOCK_INDEX_DEFAULT = 4080;

	public static Block
	barrierLight,
	barrierHeavy,
	ceramicJar,
	chestLocked,
	doorLocked,
	dungeonCore,
	dungeonStone,
	pedestal,
	pegWooden,
	pegRusty,
	secretStone,
	sacredFlame;

	/**
	 * Initialize block index and other variables from config
	 */
	public static void init(Configuration config) {
		modBlockIndex = config.getBlock("modBlockIndex", MOD_BLOCK_INDEX_DEFAULT).getInt();
	}

	/**
	 * Initializes and registers all blocks
	 */
	public static void load() {
		secretStone = new BlockSecretStone(modBlockIndex++, Material.rock).setUnlocalizedName("zss.secretstone");
		dungeonCore = new BlockDungeonCore(modBlockIndex++, Material.rock).setUnlocalizedName("zss.dungeoncore");
		pedestal = new BlockPedestal(modBlockIndex++).setUnlocalizedName("zss.pedestal");
		chestLocked = new BlockChestLocked(modBlockIndex++).setUnlocalizedName("zss.chest_locked");
		doorLocked = new BlockDoorLocked(modBlockIndex++, Material.iron).setUnlocalizedName("zss.door_locked");
		sacredFlame = new BlockSacredFlame(modBlockIndex++).setUnlocalizedName("zss.sacredflame");
		ceramicJar = new BlockCeramicJar(modBlockIndex++).setUnlocalizedName("zss.ceramic_jar");
		barrierLight = new BlockHeavy(modBlockIndex++, Material.rock, BlockWeight.MEDIUM).setUnlocalizedName("zss.barrier_light");
		barrierHeavy = new BlockHeavy(modBlockIndex++, Material.rock, BlockWeight.VERY_HEAVY).setUnlocalizedName("zss.barrier_heavy");
		pegWooden = new BlockPeg(modBlockIndex++, BlockPeg.pegWoodMaterial, BlockWeight.VERY_LIGHT).setUnlocalizedName("zss.peg_wooden");
		pegRusty = new BlockPeg(modBlockIndex++, BlockPeg.pegRustyMaterial, BlockWeight.MEDIUM).setUnlocalizedName("zss.peg_rusty");
		dungeonStone = new BlockDungeonStone(modBlockIndex++, Material.rock).setUnlocalizedName("zss.dungeonstone");

		register();
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
		RenderingRegistry.registerBlockHandler(new RenderSacredFlame());
	}

	private static void register() {
		GameRegistry.registerBlock(barrierLight, barrierLight.getUnlocalizedName());
		GameRegistry.registerBlock(barrierHeavy, barrierHeavy.getUnlocalizedName());
		GameRegistry.registerBlock(pegWooden, pegWooden.getUnlocalizedName());
		GameRegistry.registerBlock(pegRusty, pegRusty.getUnlocalizedName());
		GameRegistry.registerBlock(ceramicJar, ItemCeramicJar.class, ceramicJar.getUnlocalizedName());
		GameRegistry.registerBlock(chestLocked, chestLocked.getUnlocalizedName());
		GameRegistry.registerBlock(doorLocked, doorLocked.getUnlocalizedName());
		GameRegistry.registerBlock(dungeonCore, ItemDungeonBlock.class, dungeonCore.getUnlocalizedName());
		GameRegistry.registerBlock(dungeonStone, ItemDungeonBlock.class, dungeonStone.getUnlocalizedName());
		GameRegistry.registerBlock(pedestal, ItemBlockWithMetadata.class, pedestal.getUnlocalizedName());
		GameRegistry.registerBlock(secretStone, ItemSecretStone.class, secretStone.getUnlocalizedName());
		GameRegistry.registerBlock(sacredFlame, ItemSacredFlame.class, sacredFlame.getUnlocalizedName());

		GameRegistry.registerTileEntity(TileEntityCeramicJar.class, "tileEntityCeramicJar");
		GameRegistry.registerTileEntity(TileEntityChestLocked.class, "tileEntityChestLocked");
		GameRegistry.registerTileEntity(TileEntityDungeonBlock.class, "tileEntityDungeonBlock");
		GameRegistry.registerTileEntity(TileEntityDungeonCore.class, "tileEntityDungeonCore");
		GameRegistry.registerTileEntity(TileEntityPedestal.class, "tileEntityPedestal");
		GameRegistry.registerTileEntity(TileEntitySacredFlame.class, "tileEntitySacredFlame");
	}
}
