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

package zeldaswordskills.api.item;

public enum HookshotType {
	/** Can grapple wood and break glass */
	WOOD_SHOT,
	/** Extended version of WOOD_SHOT */
	WOOD_SHOT_EXT,
	/** Can grapple stone and iron grates, breaks glass and wood */
	CLAW_SHOT,
	/** Extended version of CLAW_SHOT */
	CLAW_SHOT_EXT,
	/** Can grapple a wide variety of materials, only breaks glass */
	MULTI_SHOT,
	/** Extended version of MULTI_SHOT */
	MULTI_SHOT_EXT;
}
