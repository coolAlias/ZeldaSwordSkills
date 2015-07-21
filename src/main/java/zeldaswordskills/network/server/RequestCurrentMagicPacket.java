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

package zeldaswordskills.network.server;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.relauncher.Side;
import zeldaswordskills.network.AbstractMessage.AbstractServerMessage;
import zeldaswordskills.network.PacketDispatcher;
import zeldaswordskills.network.client.SyncCurrentMagicPacket;

/**
 * 
 * SharedMonsterAttribute is synchronized too slowly from server->client, such that the
 * player's maximum magic points are not correct at the time of receiving the sync player
 * info packet upon joining the world, causing current magic to potentially be truncated.
 *
 */
public class RequestCurrentMagicPacket extends AbstractServerMessage<RequestCurrentMagicPacket>
{
	public RequestCurrentMagicPacket() {}

	@Override
	protected void read(PacketBuffer buffer) throws IOException {}

	@Override
	protected void write(PacketBuffer buffer) throws IOException {}

	@Override
	protected void process(EntityPlayer player, Side side) {
		if (player instanceof EntityPlayerMP) {
			PacketDispatcher.sendTo(new SyncCurrentMagicPacket(player, true), (EntityPlayerMP) player);
		}
	}
}
