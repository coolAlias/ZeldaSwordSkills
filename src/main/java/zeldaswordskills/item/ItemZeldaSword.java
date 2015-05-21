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

import mods.battlegear2.api.PlayerEventChild.OffhandAttackEvent;
import mods.battlegear2.api.weapons.IBattlegearWeapon;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import zeldaswordskills.ZSSAchievements;
import zeldaswordskills.api.entity.IParryModifier;
import zeldaswordskills.api.item.IFairyUpgrade;
import zeldaswordskills.api.item.ISacredFlame;
import zeldaswordskills.api.item.ISwingSpeed;
import zeldaswordskills.api.item.IUnenchantable;
import zeldaswordskills.block.BlockSacredFlame;
import zeldaswordskills.block.tileentity.TileEntityDungeonCore;
import zeldaswordskills.creativetab.ZSSCreativeTabs;
import zeldaswordskills.ref.Config;
import zeldaswordskills.ref.ModInfo;
import zeldaswordskills.ref.Sounds;
import zeldaswordskills.util.PlayerUtils;
import zeldaswordskills.util.WorldUtils;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import cpw.mods.fml.common.Optional;
import cpw.mods.fml.common.Optional.Method;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * 
 * Base class for all ZSS Swords.
 * 
 * These require an anvil to be repaired, and if broken only a skilled blacksmith
 * is able to fix it.
 *
 */
@Optional.Interface(iface="mods.battlegear2.api.weapons.IBattlegearWeapon", modid="battlegear2", striprefs=true)
public class ItemZeldaSword extends ItemSword implements IBattlegearWeapon, IFairyUpgrade, IParryModifier, ISacredFlame, ISwingSpeed, IUnenchantable
{
	/** Original ItemSword's field is private, but this has the same functionality */
	protected final float weaponDamage;

	/** Original ItemSword's field is private, so store tool material in case it's needed */
	protected final ToolMaterial toolMaterial;

	/** Whether this sword is considered a 'master' sword for purposes of skills and such*/
	protected boolean isMaster = false;

	/** Whether this sword requires two hands */
	protected final boolean twoHanded;

	/** Additional swing time */
	protected final int swingSpeed;

	/** Additional exhaustion added each swing */
	protected final float exhaustion;

	/** Icon for the broken version of this sword */
	@SideOnly(Side.CLIENT)
	protected IIcon brokenIcon;

	/** Whether this sword will give the 'broken' version when it breaks */
	protected boolean givesBrokenItem = true;

	/**
	 * Default constructor for single-handed weapons with no swing speed or exhaustion penalties 
	 */
	public ItemZeldaSword(ToolMaterial material, float bonusDamage) {
		this(material, bonusDamage, false, 0, 0.0F);
	}

	/**
	 * Default constructor for two-handed weapons; if two-handed, default values of
	 * 15 and 0.3F are used for swing speed and exhaustion, respectively.
	 */
	public ItemZeldaSword(ToolMaterial material, float bonusDamage, boolean twoHanded) {
		this(material, bonusDamage, twoHanded, (twoHanded ? 15 : 0), (twoHanded ? 0.3F : 0.0F));
	}

	public ItemZeldaSword(ToolMaterial material, float bonusDamage, boolean twoHanded, int swingSpeed, float exhaustion) {
		super(material);
		this.setNoRepair();
		this.toolMaterial = material;
		this.weaponDamage = 4.0F + bonusDamage + material.getDamageVsEntity();
		this.twoHanded = twoHanded;
		this.swingSpeed = Math.max(0, swingSpeed);
		this.exhaustion = Math.max(0.0F, exhaustion);
		setCreativeTab(ZSSCreativeTabs.tabCombat);
	}

	/**
	 * Flags this sword as a 'master' sword, which also sets no item on break to true
	 */
	public ItemZeldaSword setMasterSword() {
		setNoItemOnBreak();
		isMaster = true;
		return this;
	}

	/** Whether this sword is considered a 'master' sword for purposes of skills and such*/
	public boolean isMasterSword() {
		return isMaster;
	}

	/**
	 * Sets this sword to not give the broken item version when the sword breaks
	 */
	public ItemZeldaSword setNoItemOnBreak() {
		givesBrokenItem = false;
		return this;
	}

	@Override
	public float getOffensiveModifier(EntityLivingBase entity, ItemStack stack) {
		return (twoHanded ? 0.25F : 0.0F);
	}

	@Override
	public float getDefensiveModifier(EntityLivingBase entity, ItemStack stack) {
		return 0;
	}

	@Override
	public float getExhaustion() {
		return exhaustion;
	}

	@Override
	public int getSwingSpeed() {
		return swingSpeed;
	}

	@Override
	public int getItemEnchantability() {
		return (isMaster ? 0 : super.getItemEnchantability());
	}

	@Override
	public boolean hitEntity(ItemStack stack, EntityLivingBase target, EntityLivingBase attacker) {
		stack.damageItem(1, attacker);
		onStackDamaged(stack, attacker);
		return true;
	}

	@Override
	public boolean onBlockDestroyed(ItemStack stack, World world, Block block, int x, int y, int z, EntityLivingBase entity) {
		if ((double) block.getBlockHardness(world, x, y, z) != 0.0D) {
			stack.damageItem((stack.getItem() == ZSSItems.swordGiant ? stack.getMaxDamage() + 1 : 2), entity);
			onStackDamaged(stack, entity);
		}
		return true;
	}

	/**
	 * Called when the stack is damaged; if stack size is 0, gives appropriate broken sword item
	 */
	protected void onStackDamaged(ItemStack stack, EntityLivingBase entity) {
		if (stack.stackSize == 0 && givesBrokenItem && entity instanceof EntityPlayer) {
			PlayerUtils.addItemToInventory((EntityPlayer) entity, new ItemStack(ZSSItems.swordBroken, 1, Item.getIdFromItem(this)));
		}
	}

	/**
	 * Override to add custom weapon damage field rather than vanilla ItemSword's field
	 */
	@Override
	public Multimap getItemAttributeModifiers() {
		Multimap multimap = HashMultimap.create();
		multimap.put(SharedMonsterAttributes.attackDamage.getAttributeUnlocalizedName(), new AttributeModifier(field_111210_e, "Weapon modifier", (double) weaponDamage, 0));
		return multimap;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIconFromDamage(int damage) {
		return (damage == -1 ? brokenIcon : itemIcon);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister register) {
		itemIcon = register.registerIcon(ModInfo.ID + ":" + getUnlocalizedName().substring(9));
		if (givesBrokenItem) {
			brokenIcon = register.registerIcon(ModInfo.ID + ":broken_" + getUnlocalizedName().substring(9));
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean isHeld) {
		list.add(EnumChatFormatting.ITALIC + StatCollector.translateToLocal("tooltip." + getUnlocalizedName().substring(5) + ".desc.0"));
		if (stack.getItem() == ZSSItems.swordTempered) {
			if (stack.hasTagCompound() && stack.getTagCompound().hasKey("zssHitCount")) {
				list.add(EnumChatFormatting.ITALIC + StatCollector.translateToLocalFormatted("tooltip.zss.sword_tempered.desc.1",stack.getTagCompound().getInteger("zssHitCount")));
			}
		} else if (stack.getItem() == ZSSItems.swordGolden) {
			if (stack.hasTagCompound() && stack.getTagCompound().hasKey("SacredFlames")) {
				int level = stack.getTagCompound().getInteger("SacredFlames");
				for (int i = 1; i < 5; ++i) {
					if (i != 3 && (level & i) != 0) {
						list.add(EnumChatFormatting.ITALIC + StatCollector.translateToLocalFormatted("tooltip.zss.sword_golden.desc.1", StatCollector.translateToLocal("misc.zss.sacred_flame.name." + i)));
					}
				}
			}
		}
	}

	@Override
	public void handleFairyUpgrade(EntityItem item, EntityPlayer player, TileEntityDungeonCore core) {
		ItemStack stack = item.getEntityItem();
		if (stack.hasTagCompound() && stack.getTagCompound().hasKey("zssHitCount") && stack.getTagCompound().getInteger("zssHitCount") > Config.getRequiredKills()) {
			item.setDead();
			WorldUtils.spawnItemWithRandom(core.getWorldObj(), new ItemStack(ZSSItems.swordGolden), core.xCoord, core.yCoord + 2, core.zCoord);
			core.getWorldObj().playSoundEffect(core.xCoord + 0.5D, core.yCoord + 1, core.zCoord + 0.5D, Sounds.FAIRY_BLESSING, 1.0F, 1.0F);
			PlayerUtils.sendTranslatedChat(player, "chat.zss.sword.blessing");
			player.triggerAchievement(ZSSAchievements.swordGolden);
		} else {
			core.getWorldObj().playSoundEffect(core.xCoord + 0.5D, core.yCoord + 1, core.zCoord + 0.5D, Sounds.FAIRY_LAUGH, 1.0F, 1.0F);
			PlayerUtils.sendTranslatedChat(player, "chat.zss.fairy.laugh.unworthy");
		}
	}

	@Override
	public boolean hasFairyUpgrade(ItemStack stack) {
		return this == ZSSItems.swordTempered;
	}

	/**
	 * Call when a player kills a mob with the Tempered Sword to increment the foes slain count
	 * There is no need to check if the held item is correct, as that is done here
	 */
	public static void onKilledMob(EntityPlayer player, IMob mob) {
		if (!player.worldObj.isRemote && player.getHeldItem() != null && player.getHeldItem().getItem() == ZSSItems.swordTempered) {
			ItemStack stack = player.getHeldItem();
			NBTTagCompound tag = stack.getTagCompound();
			if (tag == null) { tag = new NBTTagCompound(); }
			tag.setInteger("zssHitCount", tag.getInteger("zssHitCount") + 1);
			stack.setTagCompound(tag);
			if (tag.getInteger("zssHitCount") > Config.getRequiredKills()) {
				player.triggerAchievement(ZSSAchievements.swordEvil);
			}
		}
	}

	@Override
	public boolean onActivatedSacredFlame(ItemStack stack, World world, EntityPlayer player, int type, boolean isActive) {
		return false;
	}

	@Override
	public boolean onClickedSacredFlame(ItemStack stack, World world, EntityPlayer player, int type, boolean isActive) {
		if (world.isRemote) {
			return false;
		} else if (this == ZSSItems.swordGolden && isActive) {
			NBTTagCompound tag = stack.getTagCompound();
			if (tag == null) { tag = new NBTTagCompound(); }
			if ((tag.getInteger("SacredFlames") & type) == 0) {
				tag.setInteger("SacredFlames", tag.getInteger("SacredFlames") | type);
				stack.setTagCompound(tag);
				world.playSoundAtEntity(player, Sounds.FLAME_ABSORB, 1.0F, 1.0F);
				PlayerUtils.sendFormattedChat(player, "chat.zss.sacred_flame.new",
						getItemStackDisplayName(stack), StatCollector.translateToLocal("misc.zss.sacred_flame.name." + type));
				player.triggerAchievement(ZSSAchievements.swordFlame);
				addSacredFlameEnchantments(stack, type);
				return true;
			} else {
				PlayerUtils.sendFormattedChat(player, "chat.zss.sacred_flame.old.same", getItemStackDisplayName(stack));
			}
		} else {
			if (isActive) {
				PlayerUtils.sendTranslatedChat(player, "chat.zss.sacred_flame.incorrect.sword");
			} else {
				PlayerUtils.sendTranslatedChat(player, "chat.zss.sacred_flame.inactive");
			}
		}
		WorldUtils.playSoundAtEntity(player, Sounds.SWORD_MISS, 0.4F, 0.5F);
		return false;
	}

	/**
	 * Adds appropriate enchantments to Golden Sword when bathing in one of the Sacred Flames
	 * @param type metadata value of the Sacred Flame
	 */
	private void addSacredFlameEnchantments(ItemStack stack, int type) {
		switch(type) {
		case BlockSacredFlame.DIN: stack.addEnchantment(Enchantment.fireAspect, 2); break;
		case BlockSacredFlame.FARORE: stack.addEnchantment(Enchantment.knockback, 2); break;
		case BlockSacredFlame.NAYRU: stack.addEnchantment(Enchantment.looting, 3); break;
		}
		boolean flag = false;
		NBTTagList enchList = stack.getTagCompound().getTagList("ench", Constants.NBT.TAG_COMPOUND);
		for (int i = 0; i < enchList.tagCount(); ++i) {
			NBTTagCompound compound = enchList.getCompoundTagAt(i);
			if (compound.getShort("id") == Enchantment.sharpness.effectId) {
				short lvl = compound.getShort("lvl");
				if (lvl < Enchantment.sharpness.getMaxLevel()) {
					enchList.removeTag(i);
					stack.addEnchantment(Enchantment.sharpness, lvl + 1);
				}
				flag = true;
				break;
			}
		}
		if (!flag) {
			stack.addEnchantment(Enchantment.sharpness, 1);
		}
	}

	@Method(modid="battlegear2")
	@Override
	public boolean sheatheOnBack(ItemStack stack) {
		return true;
	}

	@Method(modid="battlegear2")
	@Override
	public boolean isOffhandHandDual(ItemStack stack) {
		return (Config.allowOffhandMaster() || !isMaster) && !twoHanded;
	}

	@Method(modid="battlegear2")
	@Override
	public boolean offhandAttackEntity(OffhandAttackEvent event, ItemStack main, ItemStack offhand) {
		return true;
	}

	@Method(modid="battlegear2")
	@Override
	public boolean offhandClickAir(PlayerInteractEvent event, ItemStack main, ItemStack offhand) {
		return true;
	}

	@Method(modid="battlegear2")
	@Override
	public boolean offhandClickBlock(PlayerInteractEvent event, ItemStack main, ItemStack offhand) {
		return true;
	}

	@Method(modid="battlegear2")
	@Override
	public void performPassiveEffects(Side side, ItemStack main, ItemStack offhand) {}

	@Method(modid="battlegear2")
	@Override
	public boolean allowOffhand(ItemStack main, ItemStack offhand) {
		return !twoHanded;
	}
}
