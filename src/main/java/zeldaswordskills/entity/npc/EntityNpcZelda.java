/**
    Copyright (C) <2016> <coolAlias>

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

import java.util.UUID;

import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.util.IChatComponent;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import zeldaswordskills.ZSSMain;
import zeldaswordskills.api.entity.INpcVillager;
import zeldaswordskills.api.entity.ISongTeacher;
import zeldaswordskills.entity.player.quests.IQuest;
import zeldaswordskills.entity.player.quests.IQuestHandler;
import zeldaswordskills.entity.player.quests.QuestBase;
import zeldaswordskills.entity.player.quests.QuestZeldaTalk;
import zeldaswordskills.entity.player.quests.ZSSQuests;
import zeldaswordskills.item.ItemInstrument;
import zeldaswordskills.network.PacketDispatcher;
import zeldaswordskills.network.bidirectional.PlayRecordPacket;
import zeldaswordskills.network.client.SyncQuestPacket;
import zeldaswordskills.songs.ZeldaSongs;
import zeldaswordskills.util.PlayerUtils;

import com.google.common.collect.ImmutableSet;

public class EntityNpcZelda extends EntityNpcMerchantBase implements INpcVillager, IQuestHandler, ISongTeacher
{
	/** List of quests, in order, which involve interacting with Zelda */
	private static final ImmutableSet<Class<? extends IQuest>> QUEST_LIST = new ImmutableSet.Builder<Class<? extends IQuest>>()
			.add(QuestZeldaTalk.class)
			.build();

	/** Set when Zelda first converting from a villager and used to end the song */
	private int conversionTime;

	/** Ocarina-owner's UUID, used only for returning the ocarina immediately after converting */
	private UUID ocarinaOwnerId;

	/** Position at which the song is playing, if any */
	private BlockPos songPos;

	public EntityNpcZelda(World world) {
		super(world);
	}

	@Override
	protected String getNameTagOnSpawn() {
		return "Princess Zelda";
	}

	@Override
	protected String getLivingSound() {
		return null; // TODO Sounds.PRINCESS_HAGGLE;
	}

	@Override
	protected String getHurtSound() {
		return null; // TODO Sounds.PRINCESS_HIT;
	}

	@Override
	protected String getDeathSound() {
		return null; // TODO Sounds.PRINCESS_DEATH;
	}

	@Override
	protected void populateTradingList() {
		if (trades == null) {
			trades = new MerchantRecipeList();
		}
		updateTradingList();
	}

	@Override
	protected void updateTradingList() {
		// TODO add new trades from the vanilla lists, if possible
		if (trades != null && trades.isEmpty()) {
			trades.add(new MerchantRecipe(new ItemStack(Items.emerald, 2), new ItemStack(Items.potato, 3)));
		}
	}

	@Override
	public void onQuestBegun(IQuest quest, EntityPlayer player) {
		if (quest instanceof QuestZeldaTalk) {
			onConverted(player); // begins Zelda's 'transformation' process
		}
	}

	@Override
	public void onQuestChanged(IQuest quest, EntityPlayer player) {}

	@Override
	public void onQuestCompleted(IQuest quest, EntityPlayer player) {}

	@Override
	public boolean interact(EntityPlayer player) {
		if (!isEntityAlive() || player.isSneaking()) { 
			return false;
		} else if (getCustomer() != null) {
			if (!worldObj.isRemote) {
				PlayerUtils.sendTranslatedChat(player, "chat.zss.npc.merchant.busy");
			}
			return true;
		} else if (conversionTime > 0) {
			return true; // prevent any further interaction while 'converting'
		}
		ZSSQuests quests = ZSSQuests.get(player);
		ItemStack stack = player.getHeldItem();
		if (stack != null && stack.getItem() instanceof ItemInstrument && (worldObj.isRemote || quests.hasBegun(QuestZeldaTalk.class))) {
			return true; // prevent Item#onItemRightClick so song GUI doesn't open, but still allow quest to begin on server
		} else if (worldObj.isRemote) {
			return false;
		}
		if (quests.get(QuestZeldaTalk.class) == null) {
			quests.add(new QuestZeldaTalk());
		}
		for (Class<? extends IQuest> clazz : EntityNpcZelda.QUEST_LIST) {
			IQuest quest = quests.get(clazz);
			if (quest != null && QuestBase.checkQuestProgress(player, quest, this)) {
				return true;
			}
		}
		updateTradingList();
		displayTradingGuiFor(player);
		return true;
	}

	@Override
	public Result canInteractConvert(EntityPlayer player, EntityVillager villager) {
		if (player.worldObj.isRemote || villager.getClass() != EntityVillager.class || villager.isChild()) {
			return Result.DEFAULT;
		}
		ZSSQuests quests = ZSSQuests.get(player);
		QuestZeldaTalk quest = (QuestZeldaTalk) quests.get(QuestZeldaTalk.class);
		if (quest == null) { // can be null if someone else converted the villager
			quest = new QuestZeldaTalk();
			quests.add(quest);
		}
		// Even though the held item is checked in #begin, it needs to be checked here as well
		ItemStack stack = player.getHeldItem();
		if (stack != null && stack.getItem() instanceof ItemInstrument && ((ItemInstrument) stack.getItem()).getInstrument(stack) == ItemInstrument.Instrument.OCARINA_FAIRY) {
			// allow if player already gave ocarina to Zelda, whether quest completed yet or not
			if (quest.hasBegun(player)) {
				conversionTime = -1; // skips quest intro, since player already went through it
				return Result.ALLOW;
			} else if (quest.begin(player)) {
				return Result.ALLOW;
			}
		}
		// If quest hasn't been completed, try to give a hint to the player
		if (!quest.isComplete(player)) {
			IChatComponent hint = quest.getHint(player);
			if (hint != null) {
				player.addChatMessage(hint);
				return Result.DENY;
			}
		}
		return Result.DEFAULT;
	}

	@Override
	public Result canLeftClickConvert(EntityPlayer player, EntityVillager villager) {
		return Result.DEFAULT;
	}

	@Override
	public void onConverted(EntityPlayer player) {
		if (conversionTime < 0) {
			conversionTime = 0; // auto-conversion of villager by player that has already done so
		} else {
			setCustomer(player);
			this.ocarinaOwnerId = player.getUniqueID();
			this.conversionTime = ZeldaSongs.songTime.getMinDuration() / 2;
			this.songPos = new BlockPos(this);
			PacketDispatcher.sendToAllAround(new PlayRecordPacket(ZeldaSongs.songTime.getSoundString(), songPos), this, 64.0D);
		}
	}

	@Override
	public void onLivingUpdate() {
		super.onLivingUpdate();
		if (conversionTime > 0) {
			--conversionTime;
			if (conversionTime == 0) {
				if (songPos != null) {
					PacketDispatcher.sendToAllAround(new PlayRecordPacket(null, songPos), this, 64.0D);
					songPos = null;
				}
				EntityPlayer player = getCustomer();
				if (player == null) {
					player = worldObj.getClosestPlayerToEntity(this, 16);
				}
				if (!worldObj.isRemote && player != null) {
					boolean isOwner = (ocarinaOwnerId != null && player.getUniqueID().compareTo(ocarinaOwnerId) == 0);
					if (player.getDistanceSqToEntity(this) < 64) {
						if (isOwner) {
							IQuest quest = ZSSQuests.get(player).get(QuestZeldaTalk.class);
							if (quest != null && quest.canComplete(player)) {
								quest.complete(player, true); // flag for completion message
								PacketDispatcher.sendTo(new SyncQuestPacket(quest), (EntityPlayerMP) player);
								ocarinaOwnerId = null;
							}
						} else {
							PlayerUtils.sendTranslatedChat(player, "chat.zss.npc.zelda.ocarina.wrong_player");
						}
					} else if (isOwner) {
						PlayerUtils.sendTranslatedChat(player, "chat.zss.npc.zelda.ocarina.too_far");
					} // else don't bother saying anything
				}
				setCustomer(null);
			}
		}
	}

	@Override
	public TeachingResult getTeachingResult(ItemStack stack, EntityPlayer player) {
		if (!isEntityAlive() || player.isSneaking()) { 
			return null;
		}
		String deny = null;
		ZSSQuests quests = ZSSQuests.get(player);
		if (!quests.hasCompleted(QuestZeldaTalk.class)) {
			return null; // continue to Entity#interact
		} else if (((ItemInstrument) stack.getItem()).getInstrument(stack).getPower() < 5) {
			deny = "chat.zss.npc.zelda.song.weak";
		}
		if (deny != null) {
			if (!player.worldObj.isRemote) {
				PlayerUtils.sendTranslatedChat(player, deny);
			}
			// To prevent song GUI from being opened after displaying chat message, return null
			// or don't cancel so Entity#interact can be called and return true from there
			return null; // new ISongTeacher.TeachingResult(null, false, true);
		}
		return new ISongTeacher.TeachingResult(ZeldaSongs.songTime, true, true); // skip Entity#interact
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound compound) {
		super.writeEntityToNBT(compound);
		compound.setInteger("conversionTime", conversionTime);
		if (ocarinaOwnerId != null) {
			compound.setString("OcarinaOwnerId", ocarinaOwnerId.toString());
		}
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound compound) {
		super.readEntityFromNBT(compound);
		conversionTime = compound.getInteger("conversionTime");
		if (compound.hasKey("OcarinaOwnerId", Constants.NBT.TAG_STRING)) {
			ocarinaOwnerId = UUID.fromString(compound.getString("OcarinaOwnerId"));
		}
	}
}
