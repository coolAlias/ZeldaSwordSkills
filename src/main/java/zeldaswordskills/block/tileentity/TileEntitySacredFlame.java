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

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.gui.IUpdatePlayerListBox;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.Constants;
import zeldaswordskills.block.BlockSacredFlame;
import zeldaswordskills.ref.Config;

public class TileEntitySacredFlame extends TileEntity implements IUpdatePlayerListBox
{
	/** Minimum date before next spawn reset */
	private long nextResetDate = 0;

	public TileEntitySacredFlame() {}

	/**
	 * Call when the flame is extinguished to set the next reset date
	 */
	public void extinguish() {
		nextResetDate = worldObj.getWorldTime() + (24000 * Config.getSacredFlameRefreshRate());
	}

	@Override
	public void update() {
		if (nextResetDate > 0 && worldObj.getWorldTime() > nextResetDate) {
			nextResetDate = 0;
			worldObj.setBlockState(pos, worldObj.getBlockState(pos).withProperty(BlockSacredFlame.EXTINGUISHED, Boolean.valueOf(false)), 3);
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound compound) {
		super.writeToNBT(compound);
		compound.setLong("nextResetDate", nextResetDate);
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		nextResetDate = (compound.hasKey("nextResetDate") && compound.getTag("nextResetDate").getId() == Constants.NBT.TAG_LONG ? compound.getLong("nextResetDate") : 0);
	}
}
