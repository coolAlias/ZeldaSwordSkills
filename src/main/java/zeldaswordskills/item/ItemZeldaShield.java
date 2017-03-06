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

package zeldaswordskills.item;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.common.collect.Lists;

import mods.battlegear2.api.ISheathed;
import mods.battlegear2.api.core.BattlegearUtils;
import mods.battlegear2.api.core.InventoryPlayerBattle;
import mods.battlegear2.api.shield.IArrowCatcher;
import mods.battlegear2.api.shield.IArrowDisplay;
import mods.battlegear2.api.shield.IShield;
import net.minecraft.client.renderer.ItemModelMesher;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StatCollector;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.Optional.Method;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import zeldaswordskills.ZSSAchievements;
import zeldaswordskills.ZSSMain;
import zeldaswordskills.api.damage.DamageUtils.DamageSourceArmorBreak;
import zeldaswordskills.api.damage.IDamageAoE;
import zeldaswordskills.api.entity.IReflectable;
import zeldaswordskills.api.item.IDashItem;
import zeldaswordskills.api.item.IFairyUpgrade;
import zeldaswordskills.api.item.ISwingSpeed;
import zeldaswordskills.api.item.IUnenchantable;
import zeldaswordskills.block.tileentity.TileEntityDungeonCore;
import zeldaswordskills.client.ISwapModel;
import zeldaswordskills.client.render.item.ModelItemShield;
import zeldaswordskills.creativetab.ZSSCreativeTabs;
import zeldaswordskills.entity.player.ZSSPlayerInfo;
import zeldaswordskills.ref.ModInfo;
import zeldaswordskills.ref.Sounds;
import zeldaswordskills.util.PlayerUtils;
import zeldaswordskills.util.TargetUtils;
import zeldaswordskills.util.WorldUtils;

@Optional.InterfaceList(value={
		@Optional.Interface(iface="mods.battlegear2.api.ISheathed", modid="battlegear2", striprefs=true),
		@Optional.Interface(iface="mods.battlegear2.api.shield.IArrowCatcher", modid="battlegear2", striprefs=true),
		@Optional.Interface(iface="mods.battlegear2.api.shield.IArrowDisplay", modid="battlegear2", striprefs=true),
		@Optional.Interface(iface="mods.battlegear2.api.shield.IShield", modid="battlegear2", striprefs=true)
})
public class ItemZeldaShield extends BaseModItem implements IDashItem, IFairyUpgrade,
/*ISwapModel,*/ ISwingSpeed, IUnenchantable, IShield, ISheathed, IArrowCatcher, IArrowDisplay
{
	@SideOnly(Side.CLIENT)
	private List<ModelResourceLocation> models;

	/**
	 * Material used in construction determines various properties:
	 * WOOD: Can catch arrows, vulnerable to fire; EMERALD: Reflects projectiles
	 */
	protected final ToolMaterial toolMaterial;

	/** Percent of damage from magical AoE attacks that is blocked, if any */
	private final float magicReduction;

	/** Time for which blocking will be disabled after a successful block */
	private final int recoveryTime;

	/** Rate at which BG2 stamina bar will decay per tick */
	private final float bg2DecayRate;

	/** Rate at which BG2 stamina bar will recover per tick; 0.012F takes 5 seconds */
	private final float bg2RecoveryRate;

	/**
	 * @param material Affects shield's qualities, such as weakness to fire or ability to reflect projectiles
	 * @param magicReduction Percent of damage from magical AoE attacks that is blocked, if any
	 * @param recoveryTime time in ticks it takes to recover from a block when held normally
	 * @param decayRate number of seconds it will take the BG2 stamina bar to deplete
	 * @param recoveryRate number of seconds until BG2 stamina bar will completely replenish
	 */
	public ItemZeldaShield(ToolMaterial material, float magicReduction, int recoveryTime, float decayRate, float recoveryRate) {
		super();
		this.toolMaterial = material;
		this.magicReduction = magicReduction;
		this.recoveryTime = recoveryTime;
		this.bg2DecayRate = 1F / decayRate / 20F;
		this.bg2RecoveryRate = 1F / recoveryRate / 20F;
		setFull3D();
		setMaxDamage(64);
		setMaxStackSize(1);
		setCreativeTab(ZSSCreativeTabs.tabCombat);
	}

	/** Time for which blocking will be disabled after a successful block */
	public int getRecoveryTime() {
		return recoveryTime;
	}

	/**
	 * Returns true if the shield can block this kind of damage
	 */
	public boolean canBlockDamage(ItemStack shield, DamageSource source) {
		boolean flag = source.isUnblockable() && !(source instanceof DamageSourceArmorBreak);
		if (toolMaterial == ToolMaterial.WOOD) {
			return !flag;
		}
		return !flag || source.isMagicDamage() || source.isFireDamage() || (source.isProjectile() && toolMaterial == ToolMaterial.EMERALD);
	}

	/**
	 * Called when the shield blocks an attack when held in the normal fashion (i.e. non-BG2)
	 * used by Deku Shield to damage / destroy the stack and by Mirror Shield to reflect projectiles
	 * @return	Return the amount of damage remaining, if any; 0 cancels the hurt event
	 */
	public float onBlock(EntityPlayer player, ItemStack shield, DamageSource source, float damage) {
		ZSSPlayerInfo.get(player).onAttackBlocked(shield, damage);
		WorldUtils.playSoundAtEntity(player, Sounds.HAMMER, 0.4F, 0.5F);
		float damageBlocked = damage;
		if (toolMaterial == ToolMaterial.WOOD) {
			if (source.isProjectile() && !source.isExplosion() && source.getSourceOfDamage() instanceof IProjectile) {
				if (ZSSMain.isBG2Enabled && player.getHeldItem() == shield && shield.getItem() instanceof IArrowCatcher){
					if (((IArrowCatcher) shield.getItem()).catchArrow(shield, player, (IProjectile) source.getSourceOfDamage())) {
						((InventoryPlayerBattle) player.inventory).hasChanged = true;
					}
				}
			} else if (source instanceof IDamageAoE && ((IDamageAoE) source).isAoEDamage()) {
				damageBlocked *= magicReduction;
			}
			int dmg = Math.round(source.isFireDamage() ? damage + 10.0F : damage - 2.0F);
			if (dmg > 0) {
				shield.damageItem(dmg, player);
				if (shield.stackSize <= 0) {
					ForgeEventFactory.onPlayerDestroyItem(player, shield);
					if (ZSSMain.isBG2Enabled && BattlegearUtils.isPlayerInBattlemode(player)) {
						BattlegearUtils.setPlayerOffhandItem(player, null);
					} else {
						player.destroyCurrentEquippedItem();
					}
				}
			}
		} else if (toolMaterial == ToolMaterial.EMERALD) {
			if (source.isProjectile() && !source.isExplosion() && source.getSourceOfDamage() != null) {
				float chance = (source.isMagicDamage() ? (1F / 3F) : 1.0F);
				if (source.getSourceOfDamage() instanceof IReflectable) {
					((IReflectable) source.getSourceOfDamage()).getReflectChance(shield, player, source.getEntity());
				}
				if (player.worldObj.rand.nextFloat() < chance) {
					Entity projectile = null;
					try {
						projectile = source.getSourceOfDamage().getClass().getConstructor(World.class).newInstance(player.worldObj); 
					} catch (Exception e) {
						;
					}
					if (projectile != null) {
						NBTTagCompound data = new NBTTagCompound();
						source.getSourceOfDamage().writeToNBT(data);
						projectile.readFromNBT(data);
						projectile.getEntityData().setBoolean("isReflected", true);
						projectile.posX -= projectile.motionX;
						projectile.posY -= projectile.motionY;
						projectile.posZ -= projectile.motionZ;
						double motionX = (double)(-MathHelper.sin(player.rotationYaw / 180.0F * (float) Math.PI) * MathHelper.cos(player.rotationPitch / 180.0F * (float) Math.PI));
						double motionZ = (double)(MathHelper.cos(player.rotationYaw / 180.0F * (float) Math.PI) * MathHelper.cos(player.rotationPitch / 180.0F * (float) Math.PI));
						double motionY = (double)(-MathHelper.sin(player.rotationPitch / 180.0F * (float) Math.PI));
						TargetUtils.setEntityHeading(projectile, motionX, motionY, motionZ, 1.0F, 2.0F + (20.0F * player.worldObj.rand.nextFloat()), false);
						if (projectile instanceof IReflectable) {
							((IReflectable) projectile).onReflected(shield, player, source.getEntity(), source.getSourceOfDamage());
						}
						player.worldObj.spawnEntityInWorld(projectile);
					}
				} else if (source.isUnblockable() || (source instanceof IDamageAoE && ((IDamageAoE) source).isAoEDamage())) { // failed to reflect projectile
					damageBlocked *= magicReduction;
				}
			}
		} else if (source.isUnblockable() || (source instanceof IDamageAoE && ((IDamageAoE) source).isAoEDamage())) {
			damageBlocked *= magicReduction; // default shield behavior blocks half damage from AoE magic attacks
		}
		return (damage - damageBlocked);
	}

	@Override
	public float getExhaustion() {
		return 0.3F;
	}

	@Override
	public int getSwingSpeed() {
		return 10; // same as BG2 bash speed
	}

	@Override
	public EnumAction getItemUseAction(ItemStack stack) {
		return EnumAction.BLOCK;
	}

	@Override
	public int getMaxItemUseDuration(ItemStack stack) {
		return 72000;
	}

	@Override
	public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity target) {
		if (target instanceof EntityLivingBase) {
			WorldUtils.playSoundAtEntity(player, Sounds.HAMMER, 0.4F, 0.5F);
			TargetUtils.knockTargetBack((EntityLivingBase) target, player);
		}
		return true;
	}

	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
		if (ZSSPlayerInfo.get(player).canBlock()) {
			player.setItemInUse(stack, getMaxItemUseDuration(stack));
		}
		return stack;
	}

	@Override
	public void onUsingTick(ItemStack stack, EntityPlayer player, int count) {
		if (toolMaterial == ToolMaterial.EMERALD) {
			if (player.getHeldItem() != null && ZSSPlayerInfo.get(player).canBlock()) {
				Vec3 vec3 = player.getLookVec();
				double dx = player.posX + vec3.xCoord * 2.0D;
				double dy = player.posY + player.getEyeHeight() + vec3.yCoord * 2.0D;
				double dz = player.posZ + vec3.zCoord * 2.0D;
				List<EntityFireball> list = player.worldObj.getEntitiesWithinAABB(EntityFireball.class, new AxisAlignedBB(dx - 1, dy - 1, dz - 1, dx + 1, dy + 1, dz + 1));
				for (EntityFireball fireball : list) {
					DamageSource source = DamageSource.causeFireballDamage(fireball, fireball.shootingEntity);
					if (canBlockDamage(stack, source) && fireball.attackEntityFrom(DamageSource.causePlayerDamage(player), 1.0F)) {
						fireball.getEntityData().setBoolean("isReflected", true);
						ZSSPlayerInfo.get(player).onAttackBlocked(stack, 1.0F);
						WorldUtils.playSoundAtEntity(player, Sounds.HAMMER, 0.4F, 0.5F);
						break;
					}
				}
			}
		}
	}

	@Override
	public int getItemEnchantability() {
		return 0;
	}

	@Override
	public boolean getIsRepairable(ItemStack toRepair, ItemStack stack) {
		return toRepair.isItemStackDamageable() && stack.isItemEqual(toolMaterial.getRepairItemStack());
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, EntityPlayer player, List<String> list, boolean advanced) {
		list.add(EnumChatFormatting.ITALIC + StatCollector.translateToLocal("tooltip." + getUnlocalizedName().substring(5) + ".desc.0"));
	}

	@Override
	public void handleFairyUpgrade(EntityItem item, EntityPlayer player, TileEntityDungeonCore core) {
		BlockPos pos = core.getPos();
		if (PlayerUtils.hasItem(player, ZSSItems.swordMasterTrue)) {
			item.setDead();
			player.triggerAchievement(ZSSAchievements.shieldMirror);
			WorldUtils.spawnItemWithRandom(core.getWorld(), new ItemStack(ZSSItems.shieldMirror), pos.getX(), pos.getY() + 2, pos.getZ());
			core.getWorld().playSoundEffect(pos.getX() + 0.5D, pos.getY() + 1, pos.getZ() + 0.5D, Sounds.SECRET_MEDLEY, 1.0F, 1.0F);
		} else {
			core.getWorld().playSoundEffect(pos.getX() + 0.5D, pos.getY() + 1, pos.getZ() + 0.5D, Sounds.FAIRY_LAUGH, 1.0F, 1.0F);
			PlayerUtils.sendTranslatedChat(player, "chat.zss.fairy.laugh.sword");
		}
	}

	@Override
	public boolean hasFairyUpgrade(ItemStack stack) {
		return this == ZSSItems.shieldHylian;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public ModelResourceLocation getModel(ItemStack stack, EntityPlayer player, int ticksRemaining) {
		return (player.isUsingItem() ? models.get(1) : models.get(0));
	}

	@Override
	public String[] getVariants() {
		String name = getUnlocalizedName();
		name = ModInfo.ID + ":" + name.substring(name.lastIndexOf(".") + 1);
		return new String[]{name, name + "_using", name + "_back"};
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerRenderers(ItemModelMesher mesher) {
		String[] resources = getVariants();
		models = new ArrayList<ModelResourceLocation>(resources.length);
		for (int i = 0; i < resources.length; ++i) {
			models.add(new ModelResourceLocation(resources[i], "inventory"));
		}
		// Register only the first model as the base resource:
		mesher.register(this, 0, models.get(0));
	}

	//@Override
	@SideOnly(Side.CLIENT)
	public Collection<ModelResourceLocation> getDefaultResources() {
		String[] variants = getVariants();
		// Swap both regular and 'using' models
		return Lists.newArrayList(new ModelResourceLocation(variants[0], "inventory"), new ModelResourceLocation(variants[1], "inventory"));
	}

	//@Override
	@SideOnly(Side.CLIENT)
	public Class<? extends IBakedModel> getNewModel() {
		return ModelItemShield.class;
	}

	@Method(modid="battlegear2")
	@Override
	public void setArrowCount(ItemStack stack, int count) {
		if (!stack.hasTagCompound()) { stack.setTagCompound(new NBTTagCompound()); }
		stack.getTagCompound().setShort("arrows", (short) Math.min(count, Short.MAX_VALUE));
	}

	@Method(modid="battlegear2")
	@Override
	public int getArrowCount(ItemStack stack) {
		return (stack.hasTagCompound() ? stack.getTagCompound().getShort("arrows") : 0);
	}

	@Method(modid="battlegear2")
	@Override
	public boolean catchArrow(ItemStack shield, EntityPlayer player, IProjectile projectile) {
		if (toolMaterial == ToolMaterial.WOOD && projectile instanceof EntityArrow){
			setArrowCount(shield, getArrowCount(shield) + 1);
			player.setArrowCountInEntity(player.getArrowCountInEntity() - 1);
			((EntityArrow) projectile).setDead();
			return true;
		}
		return false;
	}

	@Method(modid="battlegear2")
	@Override
	public boolean sheatheOnBack(ItemStack item) {
		return true;
	}

	@Method(modid="battlegear2")
	@Override
	public float getDecayRate(ItemStack shield) {
		return bg2DecayRate;
	}

	@Method(modid="battlegear2")
	@Override
	public float getRecoveryRate(ItemStack shield) {
		return bg2RecoveryRate;
	}

	@Method(modid="battlegear2")
	@Override
	public boolean canBlock(ItemStack shield, DamageSource source) {
		return canBlockDamage(shield, source);
	}

	@Method(modid="battlegear2")
	@Override
	public float getDamageDecayRate(ItemStack shield, float amount) {
		return 0.0F; // 1F/20F is the default BG2 value
	}

	@Method(modid="battlegear2")
	@Override
	public float getBlockAngle(ItemStack shield) {
		return 60; // this is the default BG2 value
	}

	@Method(modid="battlegear2")
	@Override
	public int getBashTimer(ItemStack shield) {
		return 10; // this is the default BG2 value
	}

	@Method(modid="battlegear2")
	@Override
	public void blockAnimation(EntityPlayer player, float amount) {}

	@Method(modid="battlegear2")
	@Override
	public float getDamageReduction(ItemStack shield, DamageSource source) {
		return 0.0F;
	}
}
