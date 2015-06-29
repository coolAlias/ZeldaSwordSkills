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

package zeldaswordskills.network.client;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import zeldaswordskills.lib.Config;
import zeldaswordskills.network.CustomPacket.CustomClientPacket;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

import cpw.mods.fml.relauncher.Side;

/**
 * 
 * Sent to each player as they log in to synchronize certain configuration settings.
 *
 */
public class SyncConfigPacket extends CustomClientPacket
{
	/** Processing calls a static method in Config, so use this field to indicate that it is a valid packet */
	private boolean isValid;
	/** Bit 1 (shift 0) */
	public boolean enableOffhandMaster;
	/** Bit 2 (shift 1) */
	public boolean enableStunPlayer;
	/** Bit 3 (shift 2) */
	public boolean enableSwingSpeed;
	/** Bit 4 (shift 3) */
	public boolean enableVanillaLift;
	/** Bit 5 (shift 4) */
	public boolean enableVanillaSmash;
	/** Bit 6 (shift 5) */
	public boolean disableAllUnenchantables;
	/** Bit 7 (shift 6) */
	public boolean enableHookableOnly;
	/** Bit 8 (shift 7) */
	public boolean requireFullHealth;
	/** Bit 9 (shift 8) */
	public boolean allMasterSwordsProvidePower;
	/** Bit 10 (shift 9) */
	public boolean enableSecretStoneLift;
	public int baseSwingSpeed;
	public int bombFuseTime;
	public int hookshotRange;
	public int whipRange;

	public SyncConfigPacket() {}

	/**
	 * Returns whether packet is valid
	 */
	public final boolean isMessageValid() {
		return isValid;
	}

	@Override
	public void read(ByteArrayDataInput buffer) throws IOException {
		short bits = buffer.readShort();
		this.enableOffhandMaster = (bits & 0x1) > 0;
		this.enableStunPlayer = (bits & (0x1 << 1)) > 0;
		this.enableSwingSpeed = (bits & (0x1 << 2)) > 0;
		this.enableVanillaLift = (bits & (0x1 << 3)) > 0;
		this.enableVanillaSmash = (bits & (0x1 << 4)) > 0;
		this.disableAllUnenchantables = (bits & (0x1 << 5)) > 0;
		this.enableHookableOnly = (bits & (0x1 << 6)) > 0;
		this.requireFullHealth = (bits & (0x1 << 7)) > 0;
		this.allMasterSwordsProvidePower = (bits & (0x1 << 8)) > 0;
		this.enableSecretStoneLift = (bits & (0x1 << 9)) > 0;
		this.baseSwingSpeed = buffer.readInt();
		this.bombFuseTime = buffer.readInt();
		this.hookshotRange = buffer.readInt();
		this.whipRange = buffer.readInt();
		this.isValid = true;
	}

	@Override
	public void write(ByteArrayDataOutput buffer) throws IOException {
		short bits = 0;
		bits |= (Config.allowOffhandMaster() ? 0x1 : 0x0);
		bits |= (Config.canPlayersBeStunned() ? (0x1 << 1) : 0x0);
		bits |= (Config.affectAllSwings() ? (0x1 << 2) : 0x0);
		bits |= (Config.canLiftVanilla() ? (0x1 << 3) : 0x0);
		bits |= (Config.canSmashVanilla() ? (0x1 << 4) : 0x0);
		bits |= (Config.areUnenchantablesDisabled() ? (0x1 << 5) : 0x0);
		bits |= (Config.allowHookableOnly() ? (0x1 << 6) : 0x0);
		bits |= (Config.getHealthAllowance(1) == 0.0F ? (0x1 << 7) : 0x0);
		bits |= (Config.getMasterSwordsProvidePower() ? (0x1 << 8) : 0x0);
		bits |= (Config.canLiftSecretStone() ? (0x1 << 9) : 0x0);
		buffer.writeShort(bits);
		buffer.writeInt(Config.getBaseSwingSpeed());
		buffer.writeInt(Config.getBombFuseTime());
		buffer.writeInt(Config.getHookshotRange());
		buffer.writeInt(Config.getWhipRange());
	}

	@Override
	public void execute(EntityPlayer player, Side side) throws ProtocolException {
		Config.syncClientSettings(this);
	}
}
