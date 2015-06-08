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

package zeldaswordskills.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * 
 * @author credits go to UltimateSpartan for this model
 *
 */
@SideOnly(Side.CLIENT)
public class ModelHookShot extends ModelBase
{
	ModelRenderer handletop;
	ModelRenderer handlemiddle;
	ModelRenderer handlebottom;
	ModelRenderer gearbox;
	ModelRenderer bracetop;
	ModelRenderer bracebottom;
	ModelRenderer supporttop;
	ModelRenderer supportright;
	ModelRenderer supportleft;
	ModelRenderer supportbottom;
	ModelRenderer hookrod;
	ModelRenderer hookbody;
	ModelRenderer hookfintop;
	ModelRenderer hookfinright;
	ModelRenderer hookfinleft;

	public ModelHookShot()
	{
		textureWidth = 64;
		textureHeight = 32;

		handletop = new ModelRenderer(this, 16, 0);
		handletop.addBox(-0.5F, -0.5F, -4F, 1, 1, 4);
		handletop.setRotationPoint(-6F, -0.6F, -8.7F);
		handletop.setTextureSize(64, 32);
		handletop.mirror = true;
		setRotation(handletop, 0.2617994F, 0F, 0F);
		handlemiddle = new ModelRenderer(this, 12, 0);
		handlemiddle.addBox(-0.5F, -1F, -2F, 1, 6, 1);
		handlemiddle.setRotationPoint(-6F, 0F, -7F);
		handlemiddle.setTextureSize(64, 32);
		handlemiddle.mirror = true;
		setRotation(handlemiddle, 0F, 0F, 0F);
		handlebottom = new ModelRenderer(this, 16, 0);
		handlebottom.addBox(-0.5F, -0.5F, -4F, 1, 1, 4);
		handlebottom.setRotationPoint(-6F, 4.6F, -8.7F);
		handlebottom.setTextureSize(64, 32);
		handlebottom.mirror = true;
		setRotation(handlebottom, -0.2617994F, 0F, 0F);
		gearbox = new ModelRenderer(this, 0, 0);
		gearbox.addBox(-0.5F, -1F, -2F, 3, 3, 3);
		gearbox.setRotationPoint(-7F, 1.5F, -12F);
		gearbox.setTextureSize(64, 32);
		gearbox.mirror = true;
		setRotation(gearbox, 0F, 0F, 0F);
		bracetop = new ModelRenderer(this, 26, 0);
		bracetop.addBox(-0.5F, -0.5F, -4F, 1, 1, 4);
		bracetop.setRotationPoint(-6F, 0.4F, -12F);
		bracetop.setTextureSize(64, 32);
		bracetop.mirror = true;
		setRotation(bracetop, 0F, 0F, 0F);
		bracebottom = new ModelRenderer(this, 26, 0);
		bracebottom.addBox(-0.5F, -0.5F, -4F, 1, 1, 4);
		bracebottom.setRotationPoint(-6F, 3.6F, -12F);
		bracebottom.setTextureSize(64, 32);
		bracebottom.mirror = true;
		setRotation(bracebottom, 0F, 0F, 0F);
		supporttop = new ModelRenderer(this, 40, 0);
		supporttop.addBox(-0.5F, -1F, -2F, 1, 1, 1);
		supporttop.setRotationPoint(-6F, 1.5F, -13.5F);
		supporttop.setTextureSize(64, 32);
		supporttop.mirror = true;
		setRotation(supporttop, 0F, 0F, 0F);
		supportright = new ModelRenderer(this, 36, 0);
		supportright.addBox(-0.5F, -1F, -2F, 1, 3, 1);
		supportright.setRotationPoint(-7F, 1.5F, -13.5F);
		supportright.setTextureSize(64, 32);
		supportright.mirror = true;
		setRotation(supportright, 0F, 0F, 0F);
		supportleft = new ModelRenderer(this, 36, 0);
		supportleft.addBox(-0.5F, -1F, -2F, 1, 3, 1);
		supportleft.setRotationPoint(-5F, 1.5F, -13.5F);
		supportleft.setTextureSize(64, 32);
		supportleft.mirror = true;
		setRotation(supportleft, 0F, 0F, 0F);
		supportbottom = new ModelRenderer(this, 40, 0);
		supportbottom.addBox(-0.5F, -1F, -2F, 1, 1, 1);
		supportbottom.setRotationPoint(-6F, 3.5F, -13.5F);
		supportbottom.setTextureSize(64, 32);
		supportbottom.mirror = true;
		setRotation(supportbottom, 0F, 0F, 0F);
		hookrod = new ModelRenderer(this, 0, 10);
		hookrod.addBox(-0.5F, -0.5F, -4F, 1, 1, 6);
		hookrod.setRotationPoint(-6F, 2F, -15F);
		hookrod.setTextureSize(64, 32);
		hookrod.mirror = true;
		setRotation(hookrod, 0F, 0F, 0F);
		hookbody = new ModelRenderer(this, 14, 10);
		hookbody.addBox(-0.5F, -0.5F, -4F, 2, 2, 2);
		hookbody.setRotationPoint(-6.5F, 1.5F, -14F);
		hookbody.setTextureSize(64, 32);
		hookbody.mirror = true;
		setRotation(hookbody, 0F, 0F, 0F);
		hookfintop = new ModelRenderer(this, 22, 10);
		hookfintop.addBox(-0.5F, -2.5F, -0.5F, 1, 3, 1);
		hookfintop.setRotationPoint(-6F, 2F, -16.8F);
		hookfintop.setTextureSize(64, 32);
		hookfintop.mirror = true;
		setRotation(hookfintop, 0.3490659F, 0F, 0F);
		hookfinright = new ModelRenderer(this, 22, 10);
		hookfinright.addBox(-0.5F, -0.5F, -0.5F, 1, 3, 1);
		hookfinright.setRotationPoint(-6F, 1.8F, -16.8F);
		hookfinright.setTextureSize(64, 32);
		hookfinright.mirror = true;
		setRotation(hookfinright, -0.3490659F, 0F, 0.9250245F);
		hookfinleft = new ModelRenderer(this, 22, 10);
		hookfinleft.addBox(-0.5F, -0.5F, -0.5F, 1, 3, 1);
		hookfinleft.setRotationPoint(-6F, 1.8F, -16.8F);
		hookfinleft.setTextureSize(64, 32);
		hookfinleft.mirror = true;
		setRotation(hookfinleft, -0.3490659F, 0F, -0.9250245F);
	}

	public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5)
	{
		super.render(entity, f, f1, f2, f3, f4, f5);
		setRotationAngles(f, f1, f2, f3, f4, f5, entity);
		handletop.render(f5);
		handlemiddle.render(f5);
		handlebottom.render(f5);
		gearbox.render(f5);
		bracetop.render(f5);
		bracebottom.render(f5);
		supporttop.render(f5);
		supportright.render(f5);
		supportleft.render(f5);
		supportbottom.render(f5);
		hookrod.render(f5);
		hookbody.render(f5);
		hookfintop.render(f5);
		hookfinright.render(f5);
		hookfinleft.render(f5);
	}

	private void setRotation(ModelRenderer model, float x, float y, float z) {
		model.rotateAngleX = x;
		model.rotateAngleY = y;
		model.rotateAngleZ = z;
	}

	public void setRotationAngles(float f, float f1, float f2, float f3, float f4, float f5, Entity entity) {
		super.setRotationAngles(f, f1, f2, f3, f4, f5, entity);
	}
}
