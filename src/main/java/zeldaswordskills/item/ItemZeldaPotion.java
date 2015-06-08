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

package zeldaswordskills.item;

import java.util.List;

import net.minecraft.client.renderer.ItemModelMesher;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import zeldaswordskills.api.item.IUnenchantable;
import zeldaswordskills.creativetab.ZSSCreativeTabs;
import zeldaswordskills.entity.ZSSEntityInfo;
import zeldaswordskills.entity.buff.Buff;
import zeldaswordskills.ref.ModInfo;

public class ItemZeldaPotion extends ItemFood implements IModItem, IUnenchantable
{
	/** Amount of HP to restore when consumed */
	private final float restoreHP;
	/** Id of the buff to add, if any */
	private Buff buff;
	/** Duration the buff will last */
	private int buffDuration;
	/** Amplifier of the buff, similar to vanilla potions */
	private int buffAmplifier;
	/** Probability of the set buff effect occurring */
	private float buffProbability;

	/** Creates a potion with no healing or hunger-restoring properties */
	public ItemZeldaPotion() {
		this(0, 0.0F, 0.0F);
	}

	public ItemZeldaPotion(int restoreHunger, float saturationModifier, float healAmount) {
		super(restoreHunger, saturationModifier, false);
		restoreHP = healAmount;
		setAlwaysEdible();
		setMaxStackSize(1);
		setCreativeTab(ZSSCreativeTabs.tabTools);
	}

	/**
	 * Returns "item.zss.unlocalized_name" for translation purposes
	 */
	@Override
	public String getUnlocalizedName() {
		return super.getUnlocalizedName().replaceFirst("item.", "item.zss.");
	}

	@Override
	public String getUnlocalizedName(ItemStack stack) {
		return getUnlocalizedName();
	}

	@Override
	public EnumAction getItemUseAction(ItemStack stack) {
		return EnumAction.DRINK;
	}

	@Override
	public ItemStack onItemUseFinish(ItemStack stack, World world, EntityPlayer player) {
		// Copy but avoid calling super method in order to allow addition of glass bottle to inventory
		player.getFoodStats().addStats(this, stack);
		world.playSoundAtEntity(player, "random.burp", 0.5F, world.rand.nextFloat() * 0.1F + 0.9F);
		player.heal(restoreHP);
		onFoodEaten(stack, world, player);
		if (!player.capabilities.isCreativeMode) {
			if (--stack.stackSize <= 0) {
				return new ItemStack(Items.glass_bottle);
			}
			player.inventory.addItemStackToInventory(new ItemStack(Items.glass_bottle));
		}
		return stack;
	}

	@Override
	protected void onFoodEaten(ItemStack stack, World world, EntityPlayer player) {
		super.onFoodEaten(stack, world, player);
		if (buff != null && world.rand.nextFloat() < buffProbability) {
			ZSSEntityInfo.get(player).applyBuff(buff, buffDuration, buffAmplifier);
		}
	}

	/**
	 * Sets the Buff that this potion will grant when consumed
	 */
	public ItemZeldaPotion setBuffEffect(Buff buffEnum, int duration, int amplifier, float probability) {
		buff = buffEnum;
		buffDuration = duration;
		buffAmplifier = amplifier;
		buffProbability = probability;
		return this;
	}

	@Override
	public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity entity) {
		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean hasEffect(ItemStack stack) {
		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean isHeld) {
		list.add(EnumChatFormatting.ITALIC + StatCollector.translateToLocal("tooltip." + getUnlocalizedName().substring(5) + ".desc.0"));
	}

	/**
	 * Default behavior returns NULL to not register any variants
	 */
	@Override
	public String[] getVariants() {
		return null;
	}

	/**
	 * Default implementation suggested by {@link IModItem#registerVariants()}
	 */
	@Override
	@SideOnly(Side.CLIENT)
	public void registerVariants() {
		String[] variants = getVariants();
		if (variants != null) {
			ModelBakery.addVariantName(this, variants);
		}
	}

	/**
	 * Register all of this Item's renderers here, including for any subtypes.
	 * Default behavior registers a single inventory-based mesher for each variant
	 * returned by {@link #getVariants() getVariants}.
	 * If no variants are available, "mod_id:" plus the item's unlocalized name is used.
	 */
	@Override
	@SideOnly(Side.CLIENT)
	public void registerRenderers(ItemModelMesher mesher) {
		String[] variants = getVariants();
		if (variants == null || variants.length < 1) {
			String name = getUnlocalizedName();
			variants = new String[]{ModInfo.ID + ":" + name.substring(name.lastIndexOf(".") + 1)};
		}
		for (int i = 0; i < variants.length; ++i) {
			mesher.register(this, i, new ModelResourceLocation(variants[i], "inventory"));
		}
	}
}
