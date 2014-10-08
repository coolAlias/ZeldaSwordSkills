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

package zeldaswordskills.ref;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;
import scala.actors.threadpool.Arrays;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public enum ZeldaSong {
	SONG_OF_TIME("song_time"),
	ZELDAS_LULLABY("song_lullaby");

	private final String soundFile;

	private ZeldaSong(String soundFile) {
		this.soundFile = ModInfo.ID + ":" + soundFile;
	}

	/** Map of key combos used to trigger each song */
	// Must be client-side only due to KeyBindings, so cannot be initialized here
	@SideOnly(Side.CLIENT)
	private static Map<ZeldaSong, List<KeyBinding>> songKeys;

	/**
	 * Call from ClientProxy during mod startup to initialize the song keys
	 * @param mc	Pass in Minecraft instance for easy access to vanilla keybindings, if needed
	 */
	// i.e. in a ClientProxy method: ZeldaSong.init(Minecraft.getMinecraft());
	@SideOnly(Side.CLIENT)
	public static void init(Minecraft mc) {
		songKeys = new EnumMap<ZeldaSong, List<KeyBinding>>(ZeldaSong.class);
		// use simple combos at first for testing
		SONG_OF_TIME.addKeyMapping(mc.gameSettings.keyBindRight, mc.gameSettings.keyBindJump, mc.gameSettings.keyBindBack);
		ZELDAS_LULLABY.addKeyMapping(mc.gameSettings.keyBindLeft, mc.gameSettings.keyBindForward, mc.gameSettings.keyBindRight);
	}

	/**
	 * Helper method to more easily add key mappings
	 */
	@SideOnly(Side.CLIENT)
	private void addKeyMapping(KeyBinding... keys) {
		songKeys.put(this, Arrays.asList(keys));
	}

	/**
	 * Returns a song if the keys pressed match, or null if they don't
	 */
	@SideOnly(Side.CLIENT)
	public static ZeldaSong getSongFromKeys(List<KeyBinding> keys) {
		for (ZeldaSong song : ZeldaSong.values()) {
			List<KeyBinding> combo = songKeys.get(song);
			// quick size comparison saves time and is also a 'safety' check
			if (combo != null && combo.size() == keys.size()) {
				for (int i = 0; i < keys.size(); ++i) {
					if (keys.get(i) != combo.get(i)) {
						break;
					}
				}
				// made it through the loop without breaking, which means we found our song!
				return song;
			}
		}
		return null;
	}

	/**
	 * Plays the song and performs any effects
	 */
	public void playSong(EntityPlayer player) {
		if (player.worldObj.isRemote) {
			// client side style:
			player.playSound(soundFile, 1.0F, 1.0F);
			// TODO perform client-side effects, such as adding sparkles...
		} else {
			// server side style:
			player.worldObj.playSoundAtEntity(player, soundFile, 1.0F, 1.0F);

			// perform server-side effects:
			switch(this) {
			case SONG_OF_TIME:
				// copied from CommandTime :P
				for (int i = 0; i < MinecraftServer.getServer().worldServers.length; ++i) {
					WorldServer worldserver = MinecraftServer.getServer().worldServers[i];
					worldserver.setWorldTime(worldserver.getWorldTime() + (long) 12000); // adds half a day
				}
				break;
			case ZELDAS_LULLABY:
				// TODO activate nearby special blocks
				break;
			}
		}
	}

	/*
	// In the GUI
	/** Stores the key combination pressed
	private final List<KeyBinding> pressedKeys = new ArrayList<KeyBinding>();

	@Override
	protected void keyTyped(char c, int key) {
		// Change to use your own KeyBindings, of course
		if (key == mc.gameSettings.keyBindForward.getKeyCode()) {
			pressedKeys.add(mc.gameSettings.keyBindForward);
		} else if (key == mc.gameSettings.keyBindBack.getKeyCode()) {
			pressedKeys.add(mc.gameSettings.keyBindBack);
		} else if (key == mc.gameSettings.keyBindLeft.getKeyCode()) {
			pressedKeys.add(mc.gameSettings.keyBindLeft);
		} else if (key == mc.gameSettings.keyBindRight.getKeyCode()) {
			pressedKeys.add(mc.gameSettings.keyBindRight);
		} else if (key == mc.gameSettings.keyBindJump.getKeyCode()) {
			pressedKeys.add(mc.gameSettings.keyBindJump);
		} else if (key == ??? ) { 
			pressedKeys.clear(); // clears current song
		} else {
			super.keyTyped(c, key);
		}
		// Now check if the current list of pressed keys is a valid song
		ZeldaSongs song = ZeldaSongs.getSongFromKeys(pressedKeys);
		if (song != null) {
			// send a packet to the server with the song to play
			PacketDispatcher.sendToServer(new ZeldaSongPacket(song));
			// play here if you just want the song to be heard by the player only
			// or play on the server if everyone should hear it
			song.play(mc.thePlayer);
		}
	}

	 */
}
