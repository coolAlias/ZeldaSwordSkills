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

package zeldaswordskills.block.tileentity;

import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet132TileEntityData;
import net.minecraft.tileentity.TileEntity;
import zeldaswordskills.block.BlockSecretStone;
import zeldaswordskills.block.ZSSBlocks;

/**
 * 
 * A tile entity that can render as any simple block
 *
 */
public class TileEntityDungeonBlock extends TileEntity
{
	/** The block to render */
	private Block renderBlock = null;
	/** The metadata of the block to render */
	private int renderMetadata = 0;

	public TileEntityDungeonBlock() {}

	@Override
	public boolean canUpdate() {
		return false;
	}

	/** Returns the block that should render */
	public Block getRenderBlock() {
		return renderBlock;
	}

	/** Returns the metadata of the block that should render */
	public int getRenderMetadata() {
		return renderMetadata;
	}

	/**
	 * Sets the block and metadata value used for rendering
	 */
	public void setRenderBlock(Block block, int metadata) {
		this.renderBlock = block;
		this.renderMetadata = metadata;
	}

	@Override
	public Packet getDescriptionPacket() {
		NBTTagCompound tag = new NBTTagCompound();
		this.writeToNBT(tag);
		return new Packet132TileEntityData(xCoord, yCoord, zCoord, 1, tag);
	}

	@Override
	public void onDataPacket(INetworkManager net, Packet132TileEntityData packet) {
		readFromNBT(packet.data);
	}

	@Override
	public void writeToNBT(NBTTagCompound compound) {
		super.writeToNBT(compound);
		// TODO this should update world saves correctly to new format:
		if (renderBlock == null && this.getBlockType() == ZSSBlocks.dungeonCore) {
			renderBlock = Block.blocksList[BlockSecretStone.getIdFromMeta(worldObj.getBlockMetadata(xCoord, yCoord, zCoord))];
			renderMetadata = 0;
			worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, 0, 2);
		}
		compound.setInteger("renderBlock", renderBlock != null ? renderBlock.blockID : -1);
		compound.setInteger("renderMetadata", renderMetadata);
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		int blockId = (compound.hasKey("renderBlock") ? compound.getInteger("renderBlock") : -1);
		renderBlock = (blockId > -1 ? Block.blocksList[blockId] : null);
		renderMetadata = compound.getInteger("renderMetadata");
	}
}
