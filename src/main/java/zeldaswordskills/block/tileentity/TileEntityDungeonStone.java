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

package zeldaswordskills.block.tileentity;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;

/**
 * 
 * A tile entity that can render as any simple block
 *
 */
public class TileEntityDungeonStone extends TileEntityBase
{
	/** The block to render */
	private Block renderBlock = null;
	/** The metadata of the block to render */
	private int renderMetadata = 0;

	public TileEntityDungeonStone() {}

	/**
	 * Returns the full state of the block that should render; may return null
	 */
	public IBlockState getRenderState() {
		return (renderBlock == null ? null : renderBlock.getStateFromMeta(renderMetadata));
	}

	/**
	 * Sets the block state used for rendering with default color multiplier
	 */
	public void setRenderState(IBlockState state) {
		this.renderBlock = state.getBlock();
		this.renderMetadata = state.getBlock().getMetaFromState(state);
	}

	@Override
	public Packet<?> getDescriptionPacket() {
		NBTTagCompound tag = new NBTTagCompound();
		this.writeToNBT(tag);
		return new S35PacketUpdateTileEntity(pos, 1, tag);
	}

	@Override
	public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity packet) {
		readFromNBT(packet.getNbtCompound());
	}

	@Override
	public void writeToNBT(NBTTagCompound compound) {
		super.writeToNBT(compound);
		compound.setInteger("renderBlock", renderBlock != null ? Block.getIdFromBlock(renderBlock) : -1);
		compound.setInteger("renderMetadata", renderMetadata);
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		int blockId = (compound.hasKey("renderBlock") ? compound.getInteger("renderBlock") : -1);
		renderBlock = (blockId > -1 ? Block.getBlockById(blockId) : null);
		renderMetadata = compound.getInteger("renderMetadata");
	}
}
