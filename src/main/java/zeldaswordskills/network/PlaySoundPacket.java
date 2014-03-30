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

package zeldaswordskills.network;

import java.io.IOException;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

import cpw.mods.fml.relauncher.Side;

/**
 * 
 * Plays a sound on the client or server side
 *
 */
public class PlaySoundPacket extends CustomPacket
{
	private String sound;

	private float volume;

	private float pitch;

	/** Coordinates at which to play the sound; used on the server side */
	private double x, y, z;

	public PlaySoundPacket() {}

	public PlaySoundPacket(String sound, float volume, float pitch, double x, double y, double z) {
		this.sound = sound;
		this.volume = volume;
		this.pitch = pitch;
		this.x = x;
		this.y = y;
		this.z = z;
	}

	/**
	 * @param entity entity can be null when sending packet to client; sound played at player's position
	 */
	public PlaySoundPacket(String sound, float volume, float pitch, Entity entity) {
		this(sound, volume, pitch, (entity != null ? entity.posX : 0),
				(entity != null ? entity.posY : 0), (entity != null ? entity.posZ : 0));
	}

	public PlaySoundPacket(String sound, float volume, float pitch) {
		this(sound, volume, pitch, null);
	}

	@Override
	public void write(ByteArrayDataOutput out) throws IOException {
		out.writeUTF(sound);
		out.writeFloat(volume);
		out.writeFloat(pitch);
		out.writeDouble(x);
		out.writeDouble(y);
		out.writeDouble(z);
	}

	@Override
	public void read(ByteArrayDataInput in) throws IOException {
		sound = in.readUTF();
		volume = in.readFloat();
		pitch = in.readFloat();
		x = in.readDouble();
		y = in.readDouble();
		z = in.readDouble();
	}

	@Override
	public void execute(EntityPlayer player, Side side) throws ProtocolException {
		if (side.isClient()) {
			player.playSound(sound, volume, pitch);
		} else {
			player.worldObj.playSoundEffect(x, y, z, sound, volume, pitch);
		}
	}
}
