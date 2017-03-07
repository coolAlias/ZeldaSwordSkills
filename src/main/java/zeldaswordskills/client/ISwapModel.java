/**
    Copyright (C) <2017> <coolAlias>

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

package zeldaswordskills.client;

import java.util.Collection;

import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * 
 * Allows automating the {@link ModelBakeEvent} for both Blocks and Items
 *
 */
public interface ISwapModel {

	/**
	 * Return the default resource locations used to retrieve this object from the model registry
	 */
	@SideOnly(Side.CLIENT)
	Collection<ModelResourceLocation> getDefaultResources();

	/**
	 * Return the class used for the new model
	 * The class must have a constructor that takes a single IBakedModel argument
	 */
	@SideOnly(Side.CLIENT)
	Class<? extends IBakedModel> getNewModel();

}
