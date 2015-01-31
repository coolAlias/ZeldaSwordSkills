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

package zeldaswordskills.client.particle;

import net.minecraft.world.World;

/**
 * Particle for Deku Leaf gust of wind effect.
 * @author Hunternif
 */
public class ParticleCyclone extends ModParticle {
	public ParticleCyclone(World world, double x, double y, double z, double velX, double velY, double velZ) {
		super(world, x, y, z, velX, velY, velZ);
		setTexturePositions(0, 0);
		this.particleMaxAge = 20;
		this.particleScale = 2;
		setFade(0, 0.5f);
	}

	@Override
	protected float scaleAtAge(float ageFraq) {
		return ageFraq < 0.5f ? 2f : (2f + 2f * (ageFraq - 0.5f));
	}
}
