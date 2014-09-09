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

package zeldaswordskills.network.packet.bidirectional;

import zeldaswordskills.network.PacketDispatcher;
import zeldaswordskills.network.packet.AbstractMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;

/**
 * For messages which require different handling on each Side;
 * if the message is handled identically regardless of Side,
 * it is better to implement {@link IMessageHandler} directly
 * and register using {@link PacketDispatcher#registerBiMessage}
 */
public abstract class AbstractBiMessageHandler<T extends IMessage> extends AbstractMessageHandler<T> {

}
