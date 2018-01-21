/**
    Copyright (C) <2018> <coolAlias>

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

package zeldaswordskills.client.model;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.model.ModelRenderer;
import zeldaswordskills.entity.mobs.EntityDarknut;
import zeldaswordskills.entity.mobs.EntityDarknutMighty;

/**
 * 
 * @author Model concept by TheRedMajora, re-coded and animated by coolAlias
 *
 */
@SideOnly(Side.CLIENT)
public class ModelDarknutMighty extends ModelDarknut implements IModelBiped
{
	private ModelRenderer cape;

	public ModelDarknutMighty() {
		super();
		this.cape = new ModelRenderer(this, 32, 45);
		this.cape.addBox(-5F, 0F, 0F, 10, 16, 1);
		this.cape.setRotationPoint(0F, 0F, 3F);
		this.cape.setTextureSize(this.textureWidth, this.textureHeight);
	}

	@Override
	protected void renderArmor(EntityDarknut entity, float f, float f1, float f2, float f3, float f4, float f5) {
		if (entity instanceof EntityDarknutMighty && ((EntityDarknutMighty) entity).isWearingCape()) {
			this.cape.render(f5);
		}
	}
}
