/**
    Copyright (C) <2017> <coolAlias>

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

import java.util.Collection;
import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.block.Block;
import net.minecraft.block.BlockIce;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import zeldaswordskills.api.item.IDynamicItemBlock;
import zeldaswordskills.block.BlockDungeonStone;
import zeldaswordskills.block.BlockSecretStone;
import zeldaswordskills.block.ZSSBlocks;
import zeldaswordskills.block.tileentity.TileEntityDungeonStone;
import zeldaswordskills.client.ISwapModel;
import zeldaswordskills.client.render.item.ModelDynamicItemBlock;
import zeldaswordskills.network.PacketDispatcher;
import zeldaswordskills.network.server.HeldBlockColorPacket;
import zeldaswordskills.ref.ModInfo;

/**
 * 
 * ItemBlock for any block that provides TileEntityDungeonBlock.
 * 
 * Right-clicking any simple block while sneaking will alter the texture
 * of the itemblock to match, both while held and when placed. 
 *
 */
public class ItemDungeonBlock extends ItemBlockUnbreakable implements IDynamicItemBlock, ISwapModel
{
	/** Default block + meta for rendering in inventory without NBT compound */
	// private final Block[] defaultBlocks = new Block[2];
	private final int[] defaultMeta = new int[2];

	/**
	 * @param variants one "modid:block_name:meta_value" for each of the default item models
	 */
	public ItemDungeonBlock(Block block, String... variants) {
		super(block);
		if (variants == null || variants.length != 2) {
			throw new IllegalArgumentException("Variants must contain 2 strings");
		}
		for (int i = 0; i < variants.length; ++i) {
			String[] parts = variants[i].split(":");
			if (parts == null || parts.length != 3) {
				throw new IllegalArgumentException("Invalid format: " + variants[i] + "; format is 'mod_id:block_name:meta_value'");
			}
			/*
			// TODO
			// Regex + matches seemingly unable to work properly here: "Meta value must be an integer! Received: 1" wtf.
			else if (!parts[2].matches("\\d.\\d?")) { // only allow up to 2 digits
				throw new IllegalArgumentException("Meta value must be an integer! Received: " + parts[2]);
			}
			defaultBlocks[i] = GameRegistry.findBlock(parts[0], parts[1]);
			// Problem: returned block always null during startup
			if (!(defaultBlocks[i] instanceof Block)) {
				throw new IllegalArgumentException("Unable to find valid block for " + parts[0] + ":" + parts[1]);
			}
			 */
			defaultMeta[i] = Integer.valueOf(parts[2]);
		}
	}

	@Override
	public IBlockState getBlockStateFromStack(ItemStack stack) {
		Block block = getBlockFromStack(stack);
		return block.getStateFromMeta(getMetaFromStack(stack));
	}

	/**
	 * Returns the block used for rendering
	 */
	private Block getBlockFromStack(ItemStack stack) {
		if (stack.hasTagCompound() && stack.getTagCompound().hasKey("renderBlock", Constants.NBT.TAG_INT)) {
			return Block.getBlockById(stack.getTagCompound().getInteger("renderBlock"));
		}
		return Blocks.stone;// TODO defaultBlocks[stack.getItemDamage() > 0 ? 1 : 0];
	}

	/**
	 * Metadata value associated with the block to render
	 */
	private int getMetaFromStack(ItemStack stack) {
		return (stack.hasTagCompound() ? stack.getTagCompound().getInteger("metadata") : defaultMeta[stack.getItemDamage() > 0 ? 1 : 0]);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getColorFromItemStack(ItemStack stack, int renderPass) {
		if (stack.hasTagCompound() && stack.getTagCompound().hasKey("blockColor")) {
			return stack.getTagCompound().getInteger("blockColor");
		}
		return super.getColorFromItemStack(stack, renderPass);
	}

	@Override
	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
		return !ItemStack.areItemsEqual(oldStack, newStack);
	}

	@Override
	public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing face, float hitX, float hitY, float hitZ) {
		if (player.isSneaking()) {
			IBlockState state = world.getBlockState(pos);
			Block block = state.getBlock();
			int meta = block.getMetaFromState(state);
			if (block instanceof BlockSecretStone) {
				block = ((BlockSecretStone.EnumType) state.getValue(BlockSecretStone.VARIANT)).getDroppedBlock();
				meta = state.getBlock().getMetaFromState(state);
			} else if (block instanceof BlockDungeonStone) {
				TileEntity te = world.getTileEntity(pos);
				if (te instanceof TileEntityDungeonStone) {
					IBlockState render = ((TileEntityDungeonStone) te).getRenderState();
					if (render == null) {
						render = ((BlockDungeonStone) block).getDefaultRenderState(stack.getItemDamage() > 7);
					}
					if (render != null) {
						block = render.getBlock();
						meta = block.getMetaFromState(render);
					}
				}
			}
			if ((block.isOpaqueCube() || block instanceof BlockIce) && Item.getItemFromBlock(block) != null) {
				if (world.isRemote) { // Send block's render color to server so held block can render correctly
					PacketDispatcher.sendToServer(new HeldBlockColorPacket(block.colorMultiplier(world, pos)));
				} else {
					if (!stack.hasTagCompound()) {
						stack.setTagCompound(new NBTTagCompound());
					}
					stack.getTagCompound().setInteger("renderBlock", Block.getIdFromBlock(block));
					stack.getTagCompound().setInteger("metadata", meta);
				}
			}
			return false;
		} else {
			return super.onItemUse(stack, player, world, pos, face, hitX, hitY, hitZ);
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, EntityPlayer player, List<String> list, boolean advanced) {
		if (stack.getItemDamage() > 7) {
			list.add(EnumChatFormatting.ITALIC + StatCollector.translateToLocal("tooltip.zss.block.unbreakable.desc"));
		} else {
			list.add(EnumChatFormatting.ITALIC + StatCollector.translateToLocal("tooltip.zss.block." +
					(block == ZSSBlocks.dungeonCore ? "core" : "dungeon") + ".desc.0"));
		}
		list.add(EnumChatFormatting.ITALIC + StatCollector.translateToLocal("tooltip.zss.block.dungeon.desc.1"));
	}

	@Override
	public String[] getVariants() {
		return new String[]{ModInfo.ID + ":dungeon_block"}; // allows smart item model to be registered
	}

	/**
	 * Required or smart model will not work
	 */
	@Override
	@SideOnly(Side.CLIENT)
	public void registerResources() {
		ModelLoader.registerItemVariants(this, ModelDynamicItemBlock.resource);
		ModelLoader.setCustomMeshDefinition(this, new ItemMeshDefinition() {
			@Override
			public ModelResourceLocation getModelLocation(ItemStack stack) {
				return ModelDynamicItemBlock.resource;
			}
		});
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Collection<ModelResourceLocation> getDefaultResources() {
		return Lists.newArrayList(ModelDynamicItemBlock.resource);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Class<? extends IBakedModel> getNewModel() {
		return ModelDynamicItemBlock.class;
	}
}
