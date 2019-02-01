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

package zeldaswordskills.item;

import java.util.List;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import mods.battlegear2.api.weapons.IBattlegearWeapon;
import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.Optional.Method;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import zeldaswordskills.ZSSAchievements;
import zeldaswordskills.api.entity.IParryModifier;
import zeldaswordskills.api.item.IFairyUpgrade;
import zeldaswordskills.api.item.ISacredFlame;
import zeldaswordskills.api.item.ISwingSpeed;
import zeldaswordskills.api.item.IUnenchantable;
import zeldaswordskills.api.item.IWeapon;
import zeldaswordskills.block.BlockSacredFlame;
import zeldaswordskills.block.tileentity.TileEntityDungeonCore;
import zeldaswordskills.creativetab.ZSSCreativeTabs;
import zeldaswordskills.ref.Config;
import zeldaswordskills.ref.Sounds;
import zeldaswordskills.util.PlayerUtils;
import zeldaswordskills.util.WorldUtils;

/**
 * 
 * Base class for all ZSS Swords.
 * 
 * These require an anvil to be repaired, and if broken only a skilled blacksmith
 * is able to fix it.
 *
 */
@Optional.Interface(iface="mods.battlegear2.api.weapons.IBattlegearWeapon", modid="battlegear2", striprefs=true)
public class ItemZeldaSword extends BaseModItemSword implements IBattlegearWeapon, IFairyUpgrade, IParryModifier, ISacredFlame, ISwingSpeed, IUnenchantable, IWeapon
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
	public boolean onBlockDestroyed(ItemStack stack, World world, Block block, BlockPos pos, EntityLivingBase entity) {
		if ((double) block.getBlockHardness(world, pos) != 0.0D) {
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
			ItemStack broken = ItemBrokenSword.getBrokenSwordFor(this);
			if (broken != null) {
				PlayerUtils.addItemToInventory((EntityPlayer) entity, broken);
			}
		}
	}

	/**
	 * Override to add custom weapon damage field rather than vanilla ItemSword's field
	 */
	@Override
	public Multimap<String, AttributeModifier> getItemAttributeModifiers() {
		Multimap<String, AttributeModifier> multimap = HashMultimap.create();
		multimap.put(SharedMonsterAttributes.attackDamage.getAttributeUnlocalizedName(), new AttributeModifier(itemModifierUUID, "Weapon modifier", (double) weaponDamage, 0));
		return multimap;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, EntityPlayer player, List<String> list, boolean advanced) {
		list.add(StatCollector.translateToLocal("tooltip." + getUnlocalizedName().substring(5) + ".desc.0"));
		if (stack.getItem() == ZSSItems.swordTempered) {
			if (stack.hasTagCompound() && stack.getTagCompound().hasKey("zssHitCount")) {
				list.add(StatCollector.translateToLocalFormatted("tooltip.zss.sword_tempered.desc.1",stack.getTagCompound().getInteger("zssHitCount")));
			}
		} else if (stack.getItem() == ZSSItems.swordGolden) {
			if (stack.hasTagCompound() && stack.getTagCompound().hasKey("SacredFlames")) {
				int level = stack.getTagCompound().getInteger("SacredFlames");
				for (int i = 1; i < 5; ++i) { // bits 1, 2, and 4
					if (i != 3 && (level & i) != 0) {
						BlockSacredFlame.EnumType flame = BlockSacredFlame.EnumType.byMetadata((level & i));
						list.add(StatCollector.translateToLocalFormatted("tooltip.zss.sword_golden.desc.1", StatCollector.translateToLocal("tile.zss.sacred_flame." + flame.getName() + ".name")));
					}
				}
			}
		}
	}

	@Override
	public void handleFairyUpgrade(EntityItem item, EntityPlayer player, TileEntityDungeonCore core) {
		BlockPos pos = core.getPos();
		ItemStack stack = item.getEntityItem();
		if (stack.hasTagCompound() && stack.getTagCompound().hasKey("zssHitCount") && stack.getTagCompound().getInteger("zssHitCount") > Config.getRequiredKills()) {
			item.setDead();
			WorldUtils.spawnItemWithRandom(core.getWorld(), new ItemStack(ZSSItems.swordGolden), pos.getX(), pos.getY() + 2, pos.getZ());
			core.getWorld().playSoundEffect(pos.getX() + 0.5D, pos.getY() + 1, pos.getZ() + 0.5D, Sounds.FAIRY_BLESSING, 1.0F, 1.0F);
			PlayerUtils.sendTranslatedChat(player, "chat.zss.sword.blessing");
			player.triggerAchievement(ZSSAchievements.swordGolden);
		} else {
			core.getWorld().playSoundEffect(pos.getX() + 0.5D, pos.getY() + 1, pos.getZ() + 0.5D, Sounds.FAIRY_LAUGH, 1.0F, 1.0F);
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
	public boolean onActivatedSacredFlame(ItemStack stack, World world, EntityPlayer player, BlockSacredFlame.EnumType flame, boolean isActive) {
		return false;
	}

	@Override
	public boolean onClickedSacredFlame(ItemStack stack, World world, EntityPlayer player, BlockSacredFlame.EnumType flame, boolean isActive) {
		if (world.isRemote) {
			return false;
		} else if (this == ZSSItems.swordGolden && isActive) {
			NBTTagCompound tag = stack.getTagCompound();
			if (tag == null) { tag = new NBTTagCompound(); }
			if ((tag.getInteger("SacredFlames") & flame.getBit()) == 0) {
				tag.setInteger("SacredFlames", tag.getInteger("SacredFlames") | flame.getBit());
				stack.setTagCompound(tag);
				world.playSoundAtEntity(player, Sounds.FLAME_ABSORB, 1.0F, 1.0F);
				PlayerUtils.sendTranslatedChat(player, "chat.zss.sacred_flame.new",
						new ChatComponentTranslation(stack.getUnlocalizedName() + ".name"),
						new ChatComponentTranslation("tile.zss.sacred_flame." + flame.getName() + ".name"));
				player.triggerAchievement(ZSSAchievements.swordFlame);
				addSacredFlameEnchantments(stack, flame);
				return true;
			} else {
				PlayerUtils.sendTranslatedChat(player, "chat.zss.sacred_flame.old.same", new ChatComponentTranslation(stack.getUnlocalizedName() + ".name"));
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
	private void addSacredFlameEnchantments(ItemStack stack, BlockSacredFlame.EnumType flame) {
		switch(flame) {
		case DIN: stack.addEnchantment(Enchantment.fireAspect, 2); break;
		case FARORE: stack.addEnchantment(Enchantment.knockback, 2); break;
		case NAYRU: stack.addEnchantment(Enchantment.looting, 3); break;
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

	@Override
	public boolean isSword(ItemStack stack) {
		return true;
	}

	@Override
	public boolean isWeapon(ItemStack stack) {
		return true;
	}

	@Method(modid="battlegear2")
	@Override
	public boolean sheatheOnBack(ItemStack stack) {
		return true;
	}

	@Method(modid="battlegear2")
	@Override
	public boolean isOffhandWieldable(ItemStack stack, EntityPlayer player) {
		return !twoHanded && (!isMaster || Config.allowOffhandMaster());
	}

	@Method(modid="battlegear2")
	@Override
	public boolean allowOffhand(ItemStack main, ItemStack offhand, EntityPlayer player) {
		return !twoHanded;
	}
}
