/**
    Copyright (C) <2014> <coolAlias>

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

package zeldaswordskills.entity;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.INpc;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAvoidEntity;
import net.minecraft.entity.ai.EntityAIMoveIndoors;
import net.minecraft.entity.ai.EntityAIMoveTowardsRestriction;
import net.minecraft.entity.ai.EntityAIOpenDoor;
import net.minecraft.entity.ai.EntityAIRestrictOpenDoor;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.ai.EntityAIWatchClosest2;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StatCollector;
import net.minecraft.village.Village;
import net.minecraft.world.World;
import zeldaswordskills.ZSSAchievements;
import zeldaswordskills.ZSSMain;
import zeldaswordskills.handler.GuiHandler;
import zeldaswordskills.item.ItemMask;
import zeldaswordskills.item.ZSSItems;
import zeldaswordskills.lib.ModInfo;
import zeldaswordskills.util.PlayerUtils;
import zeldaswordskills.util.TimedAddItem;
import zeldaswordskills.util.TimedChatDialogue;

public class EntityMaskTrader extends EntityCreature implements INpc
{
	private int randomTickDivider;
	public Village villageObj;
	private EntityPlayer customer;

	/** Mapping of masks to give for each quest stage */
	private static final Map<Integer, Item> maskMap = new HashMap<Integer, Item>();
	/** Number of stages per mask */
	private static final int NUM_STAGES = 3;

	public EntityMaskTrader(World world) {
		super(world);
		this.setSize(0.6F, 1.8F);
		this.getNavigator().setBreakDoors(true);
		this.getNavigator().setAvoidsWater(true);
		this.tasks.addTask(0, new EntityAISwimming(this));
		this.tasks.addTask(1, new EntityAIAvoidEntity(this, EntityZombie.class, 8.0F, 0.6D, 0.6D));
		this.tasks.addTask(2, new EntityAIMoveIndoors(this));
		this.tasks.addTask(3, new EntityAIRestrictOpenDoor(this));
		this.tasks.addTask(4, new EntityAIOpenDoor(this, true));
		this.tasks.addTask(5, new EntityAIMoveTowardsRestriction(this, 0.6D));
		this.tasks.addTask(6, new EntityAIWatchClosest2(this, EntityPlayer.class, 3.0F, 1.0F));
		this.tasks.addTask(6, new EntityAIWander(this, 0.6D));
		this.tasks.addTask(7, new EntityAIWatchClosest(this, EntityLiving.class, 8.0F));
	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.movementSpeed).setAttribute(0.5D);
	}

	@Override
	public boolean isEntityInvulnerable() {
		// TODO return Config setting
		return true;
	}

	@Override
	public boolean isAIEnabled() {
		return true;
	}

	@Override
	protected void updateAITick() {
		if (--randomTickDivider <= 0) {
			worldObj.villageCollectionObj.addVillagerPosition(MathHelper.floor_double(posX), MathHelper.floor_double(posY), MathHelper.floor_double(posZ));
			randomTickDivider = 70 + rand.nextInt(50);
			villageObj = worldObj.villageCollectionObj.findNearestVillage(MathHelper.floor_double(posX), MathHelper.floor_double(posY), MathHelper.floor_double(posZ), 32);
			if (villageObj == null) {
				detachHome();
			} else {
				ChunkCoordinates chunkcoordinates = villageObj.getCenter();
				setHomeArea(chunkcoordinates.posX, chunkcoordinates.posY, chunkcoordinates.posZ, (int)(villageObj.getVillageRadius() * 0.6F));
			}
			if (customer != null) {
				if (customer.openContainer instanceof Container && this.getDistanceSqToEntity(customer) > 16.0D) {
					this.getNavigator().clearPathEntity();
				} else {
					customer = null;
				}
			}
		}
		super.updateAITick();
	}

	@Override
	protected boolean canDespawn() {
		return false;
	}

	@Override
	protected String getLivingSound() {
		return "mob.villager.haggle";
	}

	@Override
	protected String getHurtSound() {
		return "mob.villager.hit";
	}

	@Override
	protected String getDeathSound() {
		return "mob.villager.death";
	}

	@Override
	public boolean allowLeashing() {
		return false;
	}

	@Override
	public boolean interact(EntityPlayer player) {
		if (!player.worldObj.isRemote) {
			playLivingSound();
			ZSSPlayerInfo info = ZSSPlayerInfo.get(player);
			int maskStage = info.getCurrentMaskStage();
			if (maskStage >= (maskMap.size() * NUM_STAGES)) {
				ItemStack stack = player.getHeldItem();
				Item mask = info.getBorrowedMask();
				if (stack != null && stack.getItem() == mask) {
					player.setCurrentItemOrArmor(0, null);
					info.setBorrowedMask(null);
					PlayerUtils.playSound(player, "random.pop", 1.0F, ((rand.nextFloat() - rand.nextFloat()) * 0.7F + 1.0F) * 2.0F);
					player.addChatMessage(StatCollector.translateToLocal("chat.zss.npc.mask_trader.returned"));
				} else if (mask != null) {
					new TimedChatDialogue(player, Arrays.asList(
							StatCollector.translateToLocalFormatted("chat.zss.npc.mask_trader.borrowed.0", mask.getItemDisplayName(new ItemStack(mask))),
							StatCollector.translateToLocalFormatted("chat.zss.npc.mask_trader.borrowed.1")));
				} else {
					int x = MathHelper.floor_double(posX);
					int y = MathHelper.floor_double(posY);
					int z = MathHelper.floor_double(posZ);
					this.customer = player;
					player.openGui(ZSSMain.instance, GuiHandler.GUI_MASK_TRADER, worldObj, x, y, z);
				}
			} else {
				Item mask = maskMap.get(maskStage / NUM_STAGES);
				switch(maskStage % NUM_STAGES) {
				case 0: // new mask
					String[] chat;
					if (maskStage == 0) {
						chat = new String[] {
								StatCollector.translateToLocal("chat.zss.npc.mask_trader.intro.0"),
								StatCollector.translateToLocal("chat.zss.npc.mask_trader.intro.1"),
								StatCollector.translateToLocal("chat.zss.npc.mask_trader.intro.2"),
								StatCollector.translateToLocal("chat.zss.npc.mask_trader.intro.3")
						};
					} else {
						chat = new String[] {
								StatCollector.translateToLocal("chat.zss.npc.mask_trader.next_mask.0"),
								StatCollector.translateToLocal("chat.zss.npc.mask_trader.next_mask.1")
						};
					}
					new TimedChatDialogue(player, Arrays.asList(chat));
					if (mask != null) {
						new TimedAddItem(player, new ItemStack(mask), maskStage == 0 ? 4000 : 2000);
					}
					info.completeCurrentMaskStage();
					break;
				case 1: // still need to sell mask
					player.addChatMessage(StatCollector.translateToLocal("chat.zss.npc.mask_trader.selling." + rand.nextInt(4)));
					break;
				case 2: // need to pay for mask
					int price = (mask instanceof ItemMask ? ((ItemMask) mask).getBuyPrice() : 16);
					if (PlayerUtils.consumeInventoryItems(player, new ItemStack(Item.emerald, price))) {
						PlayerUtils.playSound(player, ModInfo.SOUND_CASH_SALE, 1.0F, 1.0F);
						info.completeCurrentMaskStage();
						if (info.getCurrentMaskStage() == (maskMap.size() * NUM_STAGES)) {
							new TimedChatDialogue(player, Arrays.asList(StatCollector.translateToLocal("chat.zss.npc.mask_trader.reward.0"),
									StatCollector.translateToLocal("chat.zss.npc.mask_trader.reward.1"),
									StatCollector.translateToLocal("chat.zss.npc.mask_trader.reward.2"),
									StatCollector.translateToLocal("chat.zss.npc.mask_trader.reward.3")));
							new TimedAddItem(player, new ItemStack(ZSSItems.maskTruth), 4000);
							info.setBorrowedMask(ZSSItems.maskTruth);
							player.triggerAchievement(ZSSAchievements.maskShop);
						} else {
							player.addChatMessage(StatCollector.translateToLocal("chat.zss.npc.mask_trader.sold"));
						}
					} else {
						new TimedChatDialogue(player, Arrays.asList(StatCollector.translateToLocal("chat.zss.npc.mask_trader.penniless.0"),
								StatCollector.translateToLocalFormatted("chat.zss.npc.mask_trader.penniless.1", price)));
					}
					break;
				}
			}
		}
		return true;
	}

	/** Returns the size of the mask map */
	public static int getMaskMapSize() {
		return maskMap.size();
	}
	/** Returns the ItemMask stored at i, or null */
	public static Item getMask(int i) {
		return maskMap.get(i);
	}

	static {
		int i = 0;
		maskMap.put(i++, ZSSItems.maskKeaton);
		maskMap.put(i++, ZSSItems.maskSkull);
		maskMap.put(i++, ZSSItems.maskSpooky);
		maskMap.put(i++, ZSSItems.maskScents);
		maskMap.put(i++, ZSSItems.maskCouples);
		maskMap.put(i++, ZSSItems.maskBunny);
	}
}
