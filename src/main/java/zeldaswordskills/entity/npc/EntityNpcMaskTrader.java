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

package zeldaswordskills.entity.npc;

import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.IChatComponent;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import zeldaswordskills.ZSSMain;
import zeldaswordskills.api.entity.INpcVillager;
import zeldaswordskills.entity.player.ZSSPlayerInfo;
import zeldaswordskills.entity.player.quests.IQuest;
import zeldaswordskills.entity.player.quests.QuestBase;
import zeldaswordskills.entity.player.quests.QuestMaskSales;
import zeldaswordskills.entity.player.quests.QuestMaskShop;
import zeldaswordskills.entity.player.quests.ZSSQuests;
import zeldaswordskills.handler.GuiHandler;
import zeldaswordskills.network.PacketDispatcher;
import zeldaswordskills.network.client.SyncQuestPacket;
import zeldaswordskills.ref.Sounds;
import zeldaswordskills.util.PlayerUtils;
import zeldaswordskills.util.TimedChatDialogue;

public class EntityNpcMaskTrader extends EntityNpcBase implements INpcVillager
{
	private EntityPlayer customer;

	public EntityNpcMaskTrader(World world) {
		super(world);
	}

	public EntityPlayer getCustomer() {
		return customer;
	}

	public void setCustomer(EntityPlayer player) {
		customer = player;
	}

	@Override
	protected String getNameTagOnSpawn() {
		return "Happy Mask Salesman";
	}

	@Override
	protected void randomUpdateTick() {
		if (customer != null) {
			if (customer.openContainer instanceof Container && this.getDistanceSqToEntity(customer) > 16.0D) {
				getNavigator().clearPathEntity();
			} else {
				customer = null;
			}
		}
	}

	@Override
	protected String getLivingSound() {
		return Sounds.VILLAGER_HAGGLE;
	}

	@Override
	protected String getHurtSound() {
		return Sounds.VILLAGER_HIT;
	}

	@Override
	protected String getDeathSound() {
		return Sounds.VILLAGER_DEATH;
	}

	@Override
	public boolean interact(EntityPlayer player) {
		if (!isEntityAlive() || player.isSneaking()) { 
			return false;
		} else if (getCustomer() != null) {
			if (!worldObj.isRemote) {
				PlayerUtils.sendTranslatedChat(player, "chat.zss.npc.merchant.busy");
			}
			return true;
		} else if (worldObj.isRemote) {
			return false;
		}
		// Mask Shop must be open for player to interact
		if (checkShopStatus(player, false, false)) {
			return true;
		}
		ZSSQuests quests = ZSSQuests.get(player);
		IQuest quest = quests.get(QuestMaskSales.class);
		if (quest == null) { // might be null for old saves
			quest = new QuestMaskSales();
			quests.add(quest);
		}
		if (QuestBase.checkQuestProgress(player, quest, QuestBase.DEFAULT_QUEST_HANDLER, this)) {
			return true;
		} else if (quest.isComplete(player)) {
			ItemStack stack = player.getHeldItem();
			Item mask = quests.getBorrowedMask();
			if (mask != null && stack != null && stack.getItem() == mask) {
				PlayerUtils.sendTranslatedChat(player, "chat.zss.npc.mask_salesman.returning");
			} else if (mask != null) {
				new TimedChatDialogue(player,
						new ChatComponentTranslation("chat.zss.npc.mask_salesman.borrowed.0", mask.getItemStackDisplayName(new ItemStack(mask))),
						new ChatComponentTranslation("chat.zss.npc.mask_salesman.borrowed.1"));
			} else {
				playLivingSound();
				setCustomer(player);
				player.openGui(ZSSMain.instance, GuiHandler.GUI_MASK_TRADER, worldObj, getEntityId(), 0, 0);
			}
		}
		return true;
	}

	@Override
	public Result canInteractConvert(EntityPlayer player, EntityVillager villager) {
		if (villager.getClass() != EntityVillager.class || villager.isChild()) {
			return Result.DEFAULT;
		} else if (!villager.worldObj.isRemote) {
			checkShopStatus(player, true, false);
		}
		return Result.DENY;
	}

	/**
	 * Call when player left- or right-clicks on Mask Salesman to check initial quest status.
	 * @param isVillager true if the interaction is with a villager and not the mask salesman
	 * @param leftClick true if this is a left-click interaction
	 * @return true if the quest status changed (i.e. interaction should be cancelled)
	 */
	public boolean checkShopStatus(EntityPlayer player, boolean isVillager, boolean leftClick) {
		IQuest quest = getMaskQuest(player);
		if (!isVillager && quest.isComplete(player)) { // not a villager, so don't need to convert
			return false;
		}
		// for compatibility with old saves - player already started mask trading sequence
		else if (!isVillager && ZSSPlayerInfo.get(player).getCurrentMaskStage() > 0) {
			quest.forceComplete(player); // completes the first quest and begins or possibly completes QuestMaskSales
			// both quests require updating the client
			if (player instanceof EntityPlayerMP) {
				PacketDispatcher.sendTo(new SyncQuestPacket(quest), (EntityPlayerMP) player);
				quest = ZSSQuests.get(player).get(QuestMaskSales.class);
				if (quest != null) {
					PacketDispatcher.sendTo(new SyncQuestPacket(quest), (EntityPlayerMP) player);
				}
			}
			return false;
		} else if (leftClick) { // try to complete the quest
			if (quest.complete(player)) {
				if (player instanceof EntityPlayerMP) {
					PacketDispatcher.sendTo(new SyncQuestPacket(quest), (EntityPlayerMP) player);
				}
				return true;
			}
			return false;
		}
		// Right-click gives a hint, but can't complete quest
		IChatComponent hint = quest.getHint(player);
		if (hint != null) {
			player.addChatMessage(hint);
		}
		return true;
	}

	@Override
	public Result canLeftClickConvert(EntityPlayer player, EntityVillager villager) {
		if (!villager.worldObj.isRemote && villager.getClass() == EntityVillager.class && !villager.isChild()) {
			if (getMaskQuest(player).complete(player)) {
				return Result.ALLOW;
			}
		}
		return Result.DEFAULT;
	}

	/**
	 * Adds or retrieves the player's Mask Shop quest instance
	 */
	private IQuest getMaskQuest(EntityPlayer player) {
		IQuest quest = ZSSQuests.get(player).get(QuestMaskShop.class);
		if (quest == null) {
			quest = new QuestMaskShop();
			ZSSQuests.get(player).add(quest);
		}
		return quest;
	}

	@Override
	public void onConverted(EntityPlayer player) {
		// nothing to do - handled by quests
	}
}
