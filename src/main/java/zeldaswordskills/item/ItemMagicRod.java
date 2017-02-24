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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.util.StatCollector;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import zeldaswordskills.api.damage.DamageUtils.DamageSourceFire;
import zeldaswordskills.api.damage.DamageUtils.DamageSourceIce;
import zeldaswordskills.api.entity.MagicType;
import zeldaswordskills.api.item.IFairyUpgrade;
import zeldaswordskills.api.item.ISacredFlame;
import zeldaswordskills.api.item.IUnenchantable;
import zeldaswordskills.block.BlockSacredFlame;
import zeldaswordskills.block.ZSSBlocks;
import zeldaswordskills.block.tileentity.TileEntityDungeonCore;
import zeldaswordskills.creativetab.ZSSCreativeTabs;
import zeldaswordskills.entity.player.ZSSPlayerInfo;
import zeldaswordskills.entity.projectile.EntityCyclone;
import zeldaswordskills.entity.projectile.EntityMagicSpell;
import zeldaswordskills.entity.projectile.EntityMobThrowable;
import zeldaswordskills.network.PacketDispatcher;
import zeldaswordskills.network.client.PacketISpawnParticles;
import zeldaswordskills.ref.Config;
import zeldaswordskills.ref.Sounds;
import zeldaswordskills.util.PlayerUtils;
import zeldaswordskills.util.TargetUtils;
import zeldaswordskills.util.WorldUtils;

/**
 * 
 * Magic rods, such as the Fire and Ice Rod
 * 
 * A variety of magical rods are available throughout Link's adventures.
 * Each rod has two abilities: the first is a continuous effect activated while the rod is in
 * use - note that magic is consumed each tick; the second is activated by using the
 * item while sneaking, shooting a single projectile per use.
 * 
 * All magic rods can be upgraded by first bathing in the appropriate Sacred Flame, and then
 * tossing it and enough emeralds into an active fairy pool.
 *
 */
public class ItemMagicRod extends BaseModItem implements IFairyUpgrade, ISacredFlame, ISpawnParticles, IUnenchantable
{
	/** The type of magic this rod uses (e.g. FIRE, ICE, etc.) */
	private final MagicType magicType;

	/** The amount of damage inflicted by the rod's projectile spell */
	private final float damage;

	/** Amount of magic consumed when used; magic / 50 is added per 4 ticks of use */
	private final float magic_cost;

	/**
	 * @param magicType	The type of magic this rod uses (e.g. FIRE, ICE, etc.)
	 * @param damage	The amount of damage inflicted by the rod's projectile spell
	 * @param magic_cost	Amount of magic consumed when used; magic / 50 is added per 4 ticks of use
	 */
	public ItemMagicRod(MagicType magicType, float damage, float magic_cost) {
		super();
		this.magicType = magicType;
		this.damage = damage;
		this.magic_cost = magic_cost;
		setFull3D();
		setMaxDamage(0);
		setMaxStackSize(1);
		setCreativeTab(ZSSCreativeTabs.tabTools);
	}

	/**
	 * Returns the next time this stack may be used
	 */
	private long getNextUseTime(ItemStack stack) {
		return (stack.hasTagCompound() ? stack.getTagCompound().getLong("next_use") : 0);
	}

	/**
	 * Sets the next time this stack may be used to the current world time plus a number of ticks
	 */
	private void setNextUseTime(ItemStack stack, World world, int ticks) {
		if (!stack.hasTagCompound()) { stack.setTagCompound(new NBTTagCompound()); }
		stack.getTagCompound().setLong("next_use", (world.getTotalWorldTime() + ticks));
	}

	/**
	 * Returns whether the rod has absorbed its associated sacred flame
	 */
	private boolean hasAbsorbedFlame(ItemStack stack) {
		return (stack.hasTagCompound() && stack.getTagCompound().getBoolean("absorbedFlame"));
	}

	/**
	 * Returns true if the rod is upgraded to the 'Nice' version
	 */
	private boolean isUpgraded(ItemStack stack) {
		return (stack.hasTagCompound() && stack.getTagCompound().getBoolean("isUpgraded"));
	}

	@Override
	public String getItemStackDisplayName(ItemStack stack) {
		String s = (isUpgraded(stack) ? StatCollector.translateToLocal("item.rodmagic.nice") + " " : "");
		return s + StatCollector.translateToLocal(getUnlocalizedName() + ".name");
	}

	@Override
	public int getItemEnchantability() {
		return 0;
	}

	@Override
	public int getMaxItemUseDuration(ItemStack stack) {
		return 72000;
	}

	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
		float mp = (player.isSneaking() ? magic_cost : magic_cost / 20.0F);
		boolean isUpgraded = isUpgraded(stack);
		if (isUpgraded) {
			mp *= 1.5F;
		}
		if (player.capabilities.isCreativeMode || (world.getTotalWorldTime() > getNextUseTime(stack) && ZSSPlayerInfo.get(player).useMagic(mp))) {
			if (player.isSneaking()) {
				EntityMobThrowable magic;
				if (magicType == MagicType.WIND) {
					magic = new EntityCyclone(world, player).setArea(isUpgraded(stack) ? 3.0F : 2.0F);
				} else {
					magic = new EntityMagicSpell(world, player).setType(magicType).setArea(isUpgraded ? 3.0F : 2.0F);
					if (magicType == MagicType.FIRE && !Config.getRodFireGriefing()) {
						((EntityMagicSpell) magic).disableGriefing();
					}
				}
				magic.setDamage(isUpgraded ? damage * 1.5F : damage);
				if (!world.isRemote) {
					WorldUtils.playSoundAtEntity(player, Sounds.WHOOSH, 0.4F, 0.5F);
					world.spawnEntityInWorld(magic);
				}
				setNextUseTime(stack, world, 10); // prevents use during swing animation
				player.swingItem();
			} else {
				player.setItemInUse(stack, getMaxItemUseDuration(stack));
				if (this == ZSSItems.rodTornado) {
					ZSSPlayerInfo.get(player).reduceFallAmount += (isUpgraded(stack) ? 8.0F : 4.0F);
				}
			}
		} else {
			// TODO need better magic fail sound...
			player.playSound(Sounds.MAGIC_FAIL, 1.0F, 1.0F);
		}
		return stack;
	}

	@Override
	public void onUsingTick(ItemStack stack, EntityPlayer player, int count) {
		if (this == ZSSItems.rodTornado) {
			player.fallDistance = 0.0F;
		}
		if (!player.worldObj.isRemote) {
			int ticksInUse = getMaxItemUseDuration(stack) - count;
			float mp = (magic_cost / 50.0F) * (isUpgraded(stack) ? 1.5F : 1.0F);
			if (ticksInUse % 4 == 0 && !ZSSPlayerInfo.get(player).consumeMagic(mp)) {
				player.clearItemInUse();
			} else if (this == ZSSItems.rodTornado) {
				if (ticksInUse % 10 == 0) {
					player.worldObj.spawnEntityInWorld(new EntityCyclone(player.worldObj, player.posX, player.posY, player.posZ).disableGriefing());
				}
			} else {
				handleUpdateTick(stack, player.worldObj, player, ticksInUse);
			}
		}
	}

	/**
	 * Handles fire and ice rod update tick
	 */
	private void handleUpdateTick(ItemStack stack, World world, EntityPlayer player, int ticksInUse) {
		float r = 0.5F + Math.min(5.5F, (ticksInUse / 10F));
		if (isUpgraded(stack)) {
			r *= 1.5F;
		}
		PacketDispatcher.sendToAllAround(new PacketISpawnParticles(player, r), player, 64.0D);
		if (ticksInUse % 4 == 3) {
			if (magicType != MagicType.FIRE || Config.getRodFireGriefing()) {
				affectBlocks(world, player, r);
			}
			List<EntityLivingBase> targets = TargetUtils.acquireAllLookTargets(player, Math.round(r), 1.0F);
			for (EntityLivingBase target : targets) {
				target.attackEntityFrom(getDamageSource(player), r);
				if (magicType == MagicType.FIRE && !target.isImmuneToFire()) {
					target.setFire(5);
				}
			}
		}
		if (ticksInUse % magicType.getSoundFrequency() == 0) {
			world.playSoundAtEntity(player, magicType.getMovingSound(), magicType.getSoundVolume(world.rand), magicType.getSoundPitch(world.rand));
		}
	}

	/**
	 * Affects blocks within the area of effect provided there is line of sight to the player
	 */
	private void affectBlocks(World world, EntityPlayer player, float radius) {
		Set<BlockPos> affectedBlocks = new HashSet<BlockPos>();
		Vec3 vec3 = player.getLookVec();
		double x = player.posX + vec3.xCoord;
		double y = player.posY + player.getEyeHeight() + vec3.yCoord;
		double z = player.posZ + vec3.zCoord;
		int r = MathHelper.ceiling_float_int(radius);
		for (int n = 0; n < r; ++n) {
			int i = MathHelper.floor_double(x);
			int j = MathHelper.floor_double(y);
			int k = MathHelper.floor_double(z);
			if (canAddBlockPosition(world, player, i, j, k)) {
				affectedBlocks.add(new BlockPos(i, j, k));
			}
			x += vec3.xCoord;
			y += vec3.yCoord;
			z += vec3.zCoord;
		}

		affectAllBlocks(world, affectedBlocks, magicType);
	}

	private boolean canAddBlockPosition(World world, EntityPlayer player, int i, int j, int k) {
		Vec3 vec31 = new Vec3(player.posX, player.posY + player.getEyeHeight(), player.posZ);
		Vec3 vec32 = new Vec3(i, j, k);
		MovingObjectPosition mop = world.rayTraceBlocks(vec31, vec32);
		return (mop == null || (mop.typeOfHit == MovingObjectType.BLOCK && (mop.getBlockPos().getX() == i && mop.getBlockPos().getY() == j && mop.getBlockPos().getZ() == k)
				|| !world.getBlockState(mop.getBlockPos()).getBlock().getMaterial().blocksLight()));
	}

	/**
	 * Affects all blocks in the set of chunk positions with the magic type's effect (freeze, thaw, etc.)
	 */
	public static void affectAllBlocks(World world, Set<BlockPos> blocks, MagicType type) {
		for (BlockPos pos : blocks) {
			Block block = world.getBlockState(pos).getBlock();
			switch(type) {
			case FIRE:
				if (WorldUtils.canMeltBlock(world, block, pos) && world.rand.nextInt(4) == 0) {
					world.setBlockToAir(pos);
					world.playSoundEffect(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
							Sounds.FIRE_FIZZ, 1.0F, world.rand.nextFloat() * 0.4F + 0.8F);
				} else if (block.getMaterial() == Material.air && world.getBlockState(pos.down()).getBlock().isFullBlock() && world.rand.nextInt(8) == 0) {
					world.setBlockState(pos, Blocks.fire.getDefaultState());
					world.playSoundEffect(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
							Sounds.FIRE_IGNITE, 1.0F, world.rand.nextFloat() * 0.4F + 0.8F);
				} else if (block == ZSSBlocks.bombFlower) {
					block.onBlockExploded(world, pos, null);
				}
				break;
			case ICE:
				if (block.getMaterial() == Material.water && world.rand.nextInt(2) == 0) {
					world.setBlockState(pos, Blocks.ice.getDefaultState());
					world.playSoundEffect(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
							Sounds.GLASS_BREAK, 1.0F, world.rand.nextFloat() * 0.4F + 0.8F);
				} else if (block.getMaterial() == Material.lava && world.rand.nextInt(4) == 0) {
					Block solid = (block == Blocks.lava ? Blocks.obsidian : Blocks.cobblestone);
					world.setBlockState(pos, solid.getDefaultState());
					world.playSoundEffect(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
							Sounds.FIRE_FIZZ, 1.0F, world.rand.nextFloat() * 0.4F + 0.8F);
				} else if (block.getMaterial() == Material.fire) {
					world.setBlockToAir(pos);
					world.playSoundEffect(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
							Sounds.FIRE_FIZZ, 1.0F, world.rand.nextFloat() * 0.4F + 0.8F);
				}
				break;
			default:
			}
		}
	}

	/** Only used for Fire and Ice Rods */
	private DamageSource getDamageSource(EntityPlayer player) {
		switch(magicType) { // causing AoE damage
		case ICE: return new DamageSourceIce("blast.ice", player, 60, 1, true);
		default: return new DamageSourceFire("blast.fire", player, true);
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void spawnParticles(World world, EntityPlayer player, ItemStack stack, double x, double y, double z, float r) {
		y += player.getEyeHeight();
		Vec3 look = player.getLookVec();
		for (float f = 0; f < r; f += 0.5F) {
			x += look.xCoord;
			y += look.yCoord;
			z += look.zCoord;
			for (int i = 0; i < 4; ++i) {
				world.spawnParticle(magicType.getTrailingParticle(),
						x + 0.5F - world.rand.nextFloat(),
						y + 0.5F - world.rand.nextFloat(),
						z + 0.5F - world.rand.nextFloat(),
						look.xCoord * (0.5F * world.rand.nextFloat()),
						look.yCoord * 0.15D,
						look.zCoord * (0.5F * world.rand.nextFloat()));
			}
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public EnumRarity getRarity(ItemStack stack) {
		return isUpgraded(stack) ? EnumRarity.RARE : EnumRarity.COMMON;
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
		list.add(EnumChatFormatting.ITALIC + StatCollector.translateToLocal("tooltip." + getUnlocalizedName().substring(5) + ".desc.1"));
		list.add("");
		list.add(EnumChatFormatting.RED + StatCollector.translateToLocalFormatted("tooltip.zss.damage", "", String.format("%.1f", isUpgraded(stack) ? damage * 1.5F : damage)));
		if (this != ZSSItems.rodTornado) {
			list.add(EnumChatFormatting.YELLOW + StatCollector.translateToLocalFormatted("tooltip.zss.area", String.format("%.1f", isUpgraded(stack) ? 3.0F : 2.0F)));
		}
	}

	@Override
	public boolean onActivatedSacredFlame(ItemStack stack, World world, EntityPlayer player, BlockSacredFlame.EnumType flame, boolean isActive) {
		return false;
	}

	@Override
	public boolean onClickedSacredFlame(ItemStack stack, World world, EntityPlayer player, BlockSacredFlame.EnumType flame, boolean isActive) {
		if (!world.isRemote) {
			if (hasAbsorbedFlame(stack)) {
				PlayerUtils.sendTranslatedChat(player, "chat.zss.sacred_flame.old.any", new ChatComponentTranslation(stack.getUnlocalizedName() + ".name"));
			} else if (isActive) {
				boolean canAbsorb = false;
				switch(magicType) {
				case FIRE: canAbsorb = (flame == BlockSacredFlame.EnumType.DIN); break;
				case ICE: canAbsorb = (flame == BlockSacredFlame.EnumType.NAYRU); break;
				case WIND: canAbsorb = (flame == BlockSacredFlame.EnumType.FARORE); break;
				default: break;
				}
				if (canAbsorb) {
					if (!stack.hasTagCompound()) {
						stack.setTagCompound(new NBTTagCompound());
					}
					stack.getTagCompound().setBoolean("absorbedFlame", true);
					world.playSoundAtEntity(player, Sounds.FLAME_ABSORB, 1.0F, 1.0F);
					PlayerUtils.sendTranslatedChat(player, "chat.zss.sacred_flame.new",
							new ChatComponentTranslation(stack.getUnlocalizedName() + ".name"),
							new ChatComponentTranslation("tile.zss.sacred_flame." + flame.getName() + ".name"));
					return true;
				} else {
					PlayerUtils.sendTranslatedChat(player, "chat.zss.sacred_flame.random");
				}
			} else {
				PlayerUtils.sendTranslatedChat(player, "chat.zss.sacred_flame.inactive");
			}
			WorldUtils.playSoundAtEntity(player, Sounds.SWORD_MISS, 0.4F, 0.5F);
		}
		return false;
	}

	@Override
	public void handleFairyUpgrade(EntityItem item, EntityPlayer player, TileEntityDungeonCore core) {
		int cost = Math.round(Config.getRodUpgradeCost() * (magicType == MagicType.WIND ? 0.75F : 1.0F));
		if (core.consumeRupees(cost)) {
			if (!item.getEntityItem().hasTagCompound()) {
				item.getEntityItem().setTagCompound(new NBTTagCompound());
			}
			item.getEntityItem().getTagCompound().setBoolean("isUpgraded", true);
			core.getWorld().playSoundEffect(core.getPos().getX() + 0.5D, core.getPos().getY() + 1, core.getPos().getZ() + 0.5D, Sounds.SECRET_MEDLEY, 1.0F, 1.0F);
		} else {
			core.getWorld().playSoundEffect(core.getPos().getX() + 0.5D, core.getPos().getY() + 1, core.getPos().getZ() + 0.5D, Sounds.FAIRY_LAUGH, 1.0F, 1.0F);
			PlayerUtils.sendTranslatedChat(player, "chat.zss.fairy.laugh.unworthy");
		}
	}

	@Override
	public boolean hasFairyUpgrade(ItemStack stack) {
		return !isUpgraded(stack) && hasAbsorbedFlame(stack);
	}
}
