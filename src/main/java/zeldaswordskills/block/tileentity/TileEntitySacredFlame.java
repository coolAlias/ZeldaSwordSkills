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

package zeldaswordskills.block.tileentity;

import java.util.Calendar;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import zeldaswordskills.lib.Config;

public class TileEntitySacredFlame extends TileEntity
{
	/** Minimum date before next spawn reset */
	private int nextResetDate = 0;

	public TileEntitySacredFlame() {}
	
	/**
	 * Called when the flame is extinguished to set the next reset date
	 */
	public void extinguish() {
		nextResetDate = Calendar.DATE + Config.getSacredFlameRefreshRate();
	}
	
	@Override
	public void updateEntity() {
		if (nextResetDate > 0 && worldObj.getWorldTime() % 256 == 0 && Calendar.DATE >= nextResetDate) {
			nextResetDate = 0;
			int meta = worldObj.getBlockMetadata(xCoord, yCoord, zCoord);
			worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, meta & ~0x8, 3);
		}
	}
	
	@Override
	public void writeToNBT(NBTTagCompound compound) {
		super.writeToNBT(compound);
		compound.setInteger("nextResetDate", nextResetDate);
	}
	
	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		nextResetDate = (compound.hasKey("nextResetDate") ? compound.getInteger("nextResetDate") : 0);
	}
}
