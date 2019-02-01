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

package zeldaswordskills.client.model;

import net.minecraft.client.model.ModelRenderer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import zeldaswordskills.client.render.entity.RenderGenericLiving;

/**
 * 
 * Interface for entities using {@link RenderGenericLiving} that need to render
 * equipped items in the same manner that ModelBiped does.
 * 
 * Currently renders held item and equipped helmet.
 *
 */
public interface IModelBiped {

	/**
	 * Return the head portion of the model for rendering the helmet layer
	 */
	@SideOnly(Side.CLIENT)
	public ModelRenderer getHeadModel();

	/**
	 * Should call {@link ModelRenderer#postRender} for the model's right or left arm
	 * @param isRight	True when rendering the right arm (seems to ALWAYS be true...)
	 */
	@SideOnly(Side.CLIENT)
	public void postRenderArm(boolean isRight, float scale);

	/**
	 * Sets whether the model should be rendered holding an item in the right
	 * or left hand, and if that item is a block.
	 * Typically stored in fields named 'heldItemRight' and 'heldItemLeft'
	 * @param isRight	True when setting the item held in the right hand
	 * @param heldValue	0 for no item, 1 for an item
	 */
	@Deprecated // no longer called, unless a custom LayerArmorBase is also added
	@SideOnly(Side.CLIENT)
	public void setHeldItemValue(boolean isRight, int heldValue);

}
