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

package zeldaswordskills.handler;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.block.BlockBreakable;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.IBossDisplayData;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityBlaze;
import net.minecraft.entity.monster.EntityCaveSpider;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.EntityGhast;
import net.minecraft.entity.monster.EntityIronGolem;
import net.minecraft.entity.monster.EntityMagmaCube;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.entity.monster.EntitySilverfish;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.entity.monster.EntityWitch;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.passive.EntityOcelot;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.event.Event.Result;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.player.BonemealEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent.HarvestDropsEvent;
import zeldaswordskills.ZSSAchievements;
import zeldaswordskills.api.block.BlockWeight;
import zeldaswordskills.api.block.ILiftable;
import zeldaswordskills.api.block.ISmashable;
import zeldaswordskills.api.item.ArmorIndex;
import zeldaswordskills.api.item.IFairyUpgrade;
import zeldaswordskills.api.item.IHandlePickup;
import zeldaswordskills.api.item.IHandleToss;
import zeldaswordskills.api.item.ILiftBlock;
import zeldaswordskills.api.item.ISmashBlock;
import zeldaswordskills.block.IGrowable;
import zeldaswordskills.block.tileentity.TileEntityDungeonCore;
import zeldaswordskills.entity.ZSSPlayerSkills;
import zeldaswordskills.entity.mobs.EntityDarknut;
import zeldaswordskills.entity.mobs.EntityKeese;
import zeldaswordskills.entity.mobs.EntityOctorok;
import zeldaswordskills.entity.mobs.EntityWizzrobe;
import zeldaswordskills.item.ItemHeldBlock;
import zeldaswordskills.item.ZSSItems;
import zeldaswordskills.lib.Config;
import zeldaswordskills.lib.Sounds;
import zeldaswordskills.network.client.UnpressKeyPacket;
import zeldaswordskills.skills.SkillBase;
import zeldaswordskills.util.PlayerUtils;
import zeldaswordskills.util.WorldUtils;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;
import cpw.mods.fml.common.registry.GameRegistry;

/**
 * 
 * Event Handler for various item- and block-related events
 *
 */
public class ZSSItemEvents
{
	/** Mapping of mobs to skill orb drops */
	private static final Map<Class<? extends EntityLivingBase>, ItemStack> dropsList = new HashMap<Class<? extends EntityLivingBase>, ItemStack>();

	public ZSSItemEvents() {
		if (dropsList.isEmpty()) {
			ZSSItemEvents.init();
		}
	}

	/** Adds a mob-class to skill orb mapping */
	private static void addDrop(Class<? extends EntityLivingBase> mobClass, SkillBase skill) {
		ItemStack stack = new ItemStack(ZSSItems.skillOrb, 1, skill.getId());
		dropsList.put(mobClass, stack);
	}

	/**
	 * Returns the type of skill orb that the mob will drop this time;
	 * this is not always the same as the stack stored in dropsList
	 */
	private static ItemStack getOrbDrop(EntityLivingBase mob, boolean isBoss) {
		if (dropsList.get(mob.getClass()) != null && mob.worldObj.rand.nextFloat() > Config.getChanceForRandomDrop()) {
			return dropsList.get(mob.getClass());
		} else {
			ItemStack orb = null;
			int id = mob.worldObj.rand.nextInt(SkillBase.getNumSkills());
			if (SkillBase.doesSkillExist(id) && SkillBase.getSkill(id).canDrop()) {
				if (dropsList.get(mob.getClass()) != null || isBoss || mob.worldObj.rand.nextFloat() < Config.getRandomMobDropChance()) {
					orb = (id == SkillBase.bonusHeart.getId() ? new ItemStack(ZSSItems.heartPiece) : new ItemStack(ZSSItems.skillOrb, 1, id));
				}
			}
			return orb;
		}
	}

	@ForgeSubscribe
	public void onLivingDrops(LivingDropsEvent event) {
		if (event.source.getEntity() instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer) event.source.getEntity();
			EntityLivingBase mob = event.entityLiving;
			boolean isBoss = mob instanceof IBossDisplayData;
			boolean flag = ZSSPlayerSkills.get(player).getSkillLevel(SkillBase.mortalDraw) == SkillBase.mortalDraw.getMaxLevel();
			ItemStack orb = (isBoss && !flag ? new ItemStack(ZSSItems.skillOrb,1,SkillBase.mortalDraw.getId()) : getOrbDrop(mob, isBoss));
			if (orb != null && Config.areOrbDropsEnabled()) {
				ItemStack helm = (player).getCurrentArmor(ArmorIndex.WORN_HELM);
				float f = (helm != null && helm.getItem() == ZSSItems.maskTruth ? 0.01F : 0.0F);
				float baseChance = Config.getDropChance(orb.getItem() == ZSSItems.heartPiece ? SkillBase.bonusHeart.getId() : orb.getItemDamage());
				if (baseChance > 0.0F && (isBoss || mob.worldObj.rand.nextFloat() < (Config.getDropChance(orb.getItemDamage()) + f + (0.005F * event.lootingLevel)))) {
					event.drops.add(new EntityItem(mob.worldObj, mob.posX, mob.posY, mob.posZ, orb.copy()));
					mob.worldObj.playSoundEffect(mob.posX, mob.posY, mob.posZ, Sounds.SPECIAL_DROP, 1.0F, 1.0F);
					player.triggerAchievement(ZSSAchievements.skillGain);
					if (isBoss) {
						player.triggerAchievement(ZSSAchievements.skillMortal);
					}
				}
			}
			if (mob instanceof EntityCreeper && mob.worldObj.rand.nextFloat() < Config.getCreeperDropChance()) {
				event.drops.add(new EntityItem(mob.worldObj, mob.posX, mob.posY, mob.posZ, new ItemStack(ZSSItems.bomb)));
			}
			if (mob instanceof IMob && mob.worldObj.rand.nextInt(Config.getPowerDropRate()) == 0) {
				event.drops.add(new EntityItem(mob.worldObj, mob.posX, mob.posY, mob.posZ, new ItemStack(ZSSItems.powerPiece)));
			}
		}
	}

	@ForgeSubscribe
	public void onBlockHarvest(HarvestDropsEvent event) {
		if (event.harvester != null) {
			if (event.block == Block.dirt || event.block == Block.grass) {
				if (event.harvester.getCurrentArmor(ArmorIndex.WORN_HELM) != null && event.harvester.getCurrentArmor(ArmorIndex.WORN_HELM).getItem() == ZSSItems.maskScents && event.world.rand.nextInt(32) == 0) {
					event.drops.add(event.world.rand.nextInt(4) == 0 ? new ItemStack(Block.mushroomRed) : new ItemStack(Block.mushroomBrown));
				}
			} else if (event.block == Block.tallGrass) {
				if (PlayerUtils.isHoldingSword(event.harvester) && event.world.rand.nextFloat() < Config.getGrassDropChance()) {
					event.drops.add(ZSSItems.getRandomGrassDrop(event.world.rand));
				}
			} else if (event.block == Block.oreIron) {
				if (event.world.rand.nextFloat() < (0.005F * event.fortuneLevel)) {
					event.drops.add(new ItemStack(ZSSItems.masterOre));
					event.harvester.worldObj.playSoundEffect(event.harvester.posX, event.harvester.posY, event.harvester.posZ, Sounds.SPECIAL_DROP, 1.0F, 1.0F);
				}
			}
		}
	}

	@ForgeSubscribe
	public void onApplyBonemeal(BonemealEvent event) {
		Block block = (event.ID > 0 ? Block.blocksList[event.ID]: null);
		if (block instanceof IGrowable) {
			event.setResult(((IGrowable) block).applyBonemeal(event.world, event.X, event.Y, event.Z));
			if (event.getResult() == Result.DENY) {
				event.setCanceled(true);
			}
		}
	}

	@ForgeSubscribe
	public void onItemToss(ItemTossEvent event) {
		EntityItem item = event.entityItem;
		ItemStack stack = item.getEntityItem();
		if (stack != null) {
			if (stack.getItem() instanceof IHandleToss) {
				((IHandleToss) stack.getItem()).onItemTossed(item, event.player);
			}
			if (!item.isDead && (stack.getItem() == Item.emerald || (stack.getItem() instanceof IFairyUpgrade)
					&& ((IFairyUpgrade) stack.getItem()).hasFairyUpgrade(stack))) {
				TileEntityDungeonCore core = WorldUtils.getNearbyFairySpawner(item.worldObj, item.posX, item.posY, item.posZ, true);
				if (core != null) {
					core.scheduleItemUpdate(event.player);
				}
			}
		}
		event.setCanceled(item.isDead);
	}

	@ForgeSubscribe
	public void onItemPickup(EntityItemPickupEvent event) {
		ItemStack stack = event.item.getEntityItem();
		EntityPlayer player = event.entityPlayer;
		if (stack != null && stack.getItem() instanceof IHandlePickup) {
			int size = stack.stackSize;
			if (((IHandlePickup) stack.getItem()).onPickupItem(stack, player)) {
				if (stack.stackSize < size) {
					GameRegistry.onPickupNotification(player, event.item);
					event.item.playSound(Sounds.POP, 0.2F, ((event.item.worldObj.rand.nextFloat()
							- event.item.worldObj.rand.nextFloat()) * 0.7F + 1.0F) * 2.0F);
					player.onItemPickup(event.item, size - stack.stackSize);
				}
				if (stack.stackSize <= 0) {
					event.item.setDead();
					event.setCanceled(true);
				}
			} else {
				event.setCanceled(true);
			}
		}
	}

	/**
	 * LEFT_CLICK_BLOCK is only called on the server
	 * RIGHT_CLICK_BLOCK is called on both sides... weird.
	 */
	@ForgeSubscribe
	public void onInteract(PlayerInteractEvent event) {
		ItemStack stack = event.entityPlayer.getHeldItem();
		switch(event.action) {
		case LEFT_CLICK_BLOCK:
			if (stack != null && stack.getItem() instanceof ISmashBlock && event.entityPlayer.attackTime == 0) {
				if (blockWasSmashed(event.entityPlayer.worldObj, event.entityPlayer, stack, event.x, event.y, event.z, event.face)) {
					if (event.entityPlayer instanceof Player) {
						ZSSCombatEvents.setPlayerAttackTime(event.entityPlayer);
						PacketDispatcher.sendPacketToPlayer(new UnpressKeyPacket(UnpressKeyPacket.LMB).makePacket(), (Player) event.entityPlayer);
					}
					event.useBlock = Result.DENY;
				}
			}
			break;
		case RIGHT_CLICK_BLOCK:
			if (stack != null && stack.getItem() instanceof ILiftBlock) {
				if (blockWasLifted(event.entityPlayer.worldObj, event.entityPlayer, stack, event.x, event.y, event.z, event.face)) {
					event.useBlock = Result.DENY;
				}
			}
			break;
		default:
		}
	}

	/**
	 * Returns true if the ILiftBlock itemstack was able to pick up the block clicked
	 * and the useBlock result should be denied
	 */
	private boolean blockWasLifted(World world, EntityPlayer player, ItemStack stack, int x, int y, int z, int side) {
		int id = world.getBlockId(x, y, z);
		if (id > 0 && (player.canPlayerEdit(x, y, z, side, stack) || Block.blocksList[id] instanceof ILiftable)) {
			Block block = Block.blocksList[id];
			int meta = world.getBlockMetadata(x, y, z);
			boolean isLiftable = block instanceof ILiftable;
			boolean isValidBlock = block.isOpaqueCube() || block instanceof BlockBreakable;
			BlockWeight weight = (isLiftable ? ((ILiftable) block).getLiftWeight(player, stack, meta, side)
					: (Config.canLiftVanilla() ? null : BlockWeight.IMPOSSIBLE));
			float strength = ((ILiftBlock) stack.getItem()).getLiftStrength(player, stack, block, meta).weight;
			float resistance = (weight != null ? weight.weight : (block.getExplosionResistance(null, world, x, y, z, x, y, z) * 5.0F/3.0F));
			if (isValidBlock && weight != BlockWeight.IMPOSSIBLE && strength >= resistance && (isLiftable || !block.hasTileEntity(meta))) {
				if (!world.isRemote) {
					ItemStack returnStack = ((ILiftBlock) stack.getItem()).onLiftBlock(player, stack, block, meta);
					if (returnStack != null && returnStack.stackSize <= 0) {
						returnStack = null;
					}
					ItemStack heldBlock = ItemHeldBlock.getBlockStack(block, meta, returnStack);
					if (isLiftable) {
						((ILiftable) block).onLifted(world, player, heldBlock, x, y, z, meta);
					}
					player.setCurrentItemOrArmor(0, heldBlock);
					world.playSoundEffect((double)(x + 0.5D), (double)(y + 0.5D), (double)(z + 0.5D),
							block.stepSound.getPlaceSound(), (block.stepSound.getVolume() + 1.0F) / 2.0F, block.stepSound.getPitch() * 0.8F);
					world.setBlockToAir(x, y, z);
				}
				return true;
			} else {
				WorldUtils.playSoundAtEntity(player, Sounds.GRUNT, 0.3F, 0.8F);
			}
		}
		return false;
	}

	/**
	 * Returns true if the ISmashBlock itemstack was able to smash up the block clicked
	 * and the useBlock result should be denied
	 */
	private boolean blockWasSmashed(World world, EntityPlayer player, ItemStack stack, int x, int y, int z, int side) {
		int id = world.getBlockId(x, y, z);
		boolean isSmashable = id > 0 ? Block.blocksList[id] instanceof ISmashable : false;
		Result smashResult = Result.DEFAULT;
		boolean wasDestroyed = false;
		if (id > 0 && (player.canPlayerEdit(x, y, z, side, stack) || isSmashable)) {
			Block block = Block.blocksList[id];
			int meta = world.getBlockMetadata(x, y, z);
			BlockWeight weight = (isSmashable ? ((ISmashable) block).getSmashWeight(player, stack, meta, side)
					: (Config.canSmashVanilla() || isVanillaBlockSmashable(block) ? null : BlockWeight.IMPOSSIBLE));
			float strength = ((ISmashBlock) stack.getItem()).getSmashStrength(player, stack, block, meta).weight;
			float resistance = (weight != null ? weight.weight : (block.getExplosionResistance(null, world, x, y, z, x, y, z) * 5.0F/3.0F));
			smashResult = (isSmashable ? ((ISmashable) block).onSmashed(world, player, stack, x, y, z, side) : smashResult);
			if (smashResult == Result.DEFAULT) {
				boolean isValidBlock = block.isOpaqueCube() || block instanceof BlockBreakable;
				if (isValidBlock && weight != BlockWeight.IMPOSSIBLE && strength >= resistance && (!block.hasTileEntity(meta) || isSmashable)) {
					if (!(block instanceof BlockBreakable)) {
						world.playSoundAtEntity(player, Sounds.ROCK_FALL, 1.0F, 1.0F);
					}
					if (!world.isRemote) {
						world.destroyBlock(x, y, z, false);
					}
					wasDestroyed = true;
				}
			}
			((ISmashBlock) stack.getItem()).onBlockSmashed(player, stack, block, meta, (smashResult == Result.ALLOW || wasDestroyed));
		}
		return (smashResult == Result.ALLOW || wasDestroyed);
	}

	private boolean isVanillaBlockSmashable(Block block) {
		return block.blockMaterial == Material.glass || block.blockMaterial == Material.ice;
	}

	private static void init() {
		addDrop(EntityCreeper.class, SkillBase.armorBreak);
		addDrop(EntityIronGolem.class, SkillBase.armorBreak);
		addDrop(EntitySilverfish.class, SkillBase.dash);
		addDrop(EntityHorse.class, SkillBase.dash);
		addDrop(EntityEnderman.class, SkillBase.dodge);
		addDrop(EntityKeese.class, SkillBase.dodge);
		addDrop(EntitySpider.class, SkillBase.endingBlow);
		addDrop(EntityCaveSpider.class, SkillBase.leapingBlow);
		addDrop(EntityMagmaCube.class, SkillBase.leapingBlow);
		addDrop(EntityPigZombie.class, SkillBase.parry);
		addDrop(EntityOcelot.class, SkillBase.parry);
		addDrop(EntityOctorok.class, SkillBase.risingCut);
		addDrop(EntityBlaze.class, SkillBase.spinAttack);
		addDrop(EntityDarknut.class, SkillBase.spinAttack);
		addDrop(EntityZombie.class, SkillBase.swordBasic);
		addDrop(EntitySkeleton.class, SkillBase.swordBasic);
		addDrop(EntityGhast.class, SkillBase.swordBeam);
		addDrop(EntityWitch.class, SkillBase.swordBeam);
		addDrop(EntityWizzrobe.class, SkillBase.swordBreak);
	}
}
