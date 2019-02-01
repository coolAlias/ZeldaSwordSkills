/**
 * Copyright (C) <2019> <coolAlias>
 *
 * This file is part of coolAlias' Zelda Sword Skills Minecraft Mod; as such,
 * you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package zeldaswordskills.api.client.animation;

import net.minecraft.client.model.ModelRenderer;

/**
 * 
 * Helper class for ModelRenderers that can modify or return the value of the
 * model renderer's rotateAngle, rotationPoint (base position of the box), or
 * offset (box position relative to the rotation point) for the given axis.
 *
 */
public enum RotationAxis
{
	X,
	Y,
	Z;

	/**
	 * Returns the current rotation value for the model part on this axis
	 */
	public float getRotation(ModelRenderer part) {
		switch (this) {
		case X: return part.rotateAngleX;
		case Y: return part.rotateAngleY;
		case Z: return part.rotateAngleZ;
		default: return 0.0F;
		}
	}

	/**
	 * Adds the amount to the current rotation value of the model part on this axis
	 */
	public void addRotation(ModelRenderer part, float amount) {
		switch (this) {
		case X: part.rotateAngleX += amount; break;
		case Y: part.rotateAngleY += amount; break;
		case Z: part.rotateAngleZ += amount; break;
		}
	}

	/**
	 * Returns the current rotation point for the model part on this axis
	 */
	public float getRotationPoint(ModelRenderer part) {
		switch (this) {
		case X: return part.rotationPointX;
		case Y: return part.rotationPointY;
		case Z: return part.rotationPointZ;
		default: return 0.0F;
		}
	}

	/**
	 * Adds the amount to the current rotation point value of the model part on this axis
	 */
	public void addRotationPoint(ModelRenderer part, float amount) {
		switch (this) {
		case X: part.rotationPointX += amount; break;
		case Y: part.rotationPointY += amount; break;
		case Z: part.rotationPointZ += amount; break;
		}
	}

	/**
	 * Returns the current offset value (i.e. box position relative to the rotation point) for the model part on this axis
	 */
	public float getOffset(ModelRenderer part) {
		switch (this) {
		case X: return part.offsetX;
		case Y: return part.offsetY;
		case Z: return part.offsetZ;
		default: return 0.0F;
		}
	}

	/**
	 * Adds the amount to the current offset value (i.e. box position relative to the rotation point) of the model part on this axis
	 */
	public void addOffset(ModelRenderer part, float amount) {
		switch (this) {
		case X: part.offsetX += amount; break;
		case Y: part.offsetY += amount; break;
		case Z: part.offsetZ += amount; break;
		}
	}
}
