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

package zeldaswordskills.entity.npc;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import zeldaswordskills.ZSSAchievements;
import zeldaswordskills.ZSSMain;
import zeldaswordskills.entity.ZSSPlayerInfo;
import zeldaswordskills.entity.ZSSPlayerSongs;
import zeldaswordskills.handler.GuiHandler;
import zeldaswordskills.item.ItemInstrument;
import zeldaswordskills.item.ItemMask;
import zeldaswordskills.item.ZSSItems;
import zeldaswordskills.ref.Sounds;
import zeldaswordskills.ref.ZeldaSong;
import zeldaswordskills.util.PlayerUtils;
import zeldaswordskills.util.TimedAddItem;
import zeldaswordskills.util.TimedChatDialogue;

public class EntityNpcMaskTrader extends EntityNpcBase
{
	private EntityPlayer customer;

	/** Mapping of masks to give for each quest stage */
	private static final Map<Integer, Item> maskMap = new HashMap<Integer, Item>();
	/** Number of stages per mask */
	private static final int NUM_STAGES = 3;

	public EntityNpcMaskTrader(World world) {
		super(world);
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
		ItemStack stack = player.getHeldItem();
		if (stack != null && stack.getItem() instanceof ItemInstrument) {
			if (player.worldObj.isRemote) {
				ZSSPlayerSongs songs = ZSSPlayerSongs.get(player);
				if (songs.isSongKnown(ZeldaSong.HEALING_SONG)) {
					// instrument doesn't matter when reviewing a known song
					songs.songToLearn = ZeldaSong.HEALING_SONG;
					player.openGui(ZSSMain.instance, GuiHandler.GUI_LEARN_SONG, player.worldObj, 0, 0, 0);
				} else if (stack.getItemDamage() == ItemInstrument.Instrument.OCARINA_TIME.ordinal()) {
					new TimedChatDialogue(player, Arrays.asList(
							StatCollector.translateToLocal("chat.zss.npc.mask_trader.ocarina.found.0"),
							StatCollector.translateToLocal("chat.zss.npc.mask_trader.ocarina.found.1")));
					songs.songToLearn = ZeldaSong.HEALING_SONG;
					player.openGui(ZSSMain.instance, GuiHandler.GUI_LEARN_SONG, player.worldObj, 0, 0, 0);
				} else {
					new TimedChatDialogue(player, Arrays.asList(
							StatCollector.translateToLocal("chat.zss.npc.mask_trader.ocarina.lost.0"),
							StatCollector.translateToLocal("chat.zss.npc.mask_trader.ocarina.lost.1")));
				}
			}
		} else if (!player.worldObj.isRemote) {
			playLivingSound();
			ZSSPlayerInfo info = ZSSPlayerInfo.get(player);
			int maskStage = info.getCurrentMaskStage();
			if (maskStage >= (maskMap.size() * NUM_STAGES)) {
				Item mask = info.getBorrowedMask();
				if (stack != null && stack.getItem() == mask) {
					player.setCurrentItemOrArmor(0, null);
					info.setBorrowedMask(null);
					PlayerUtils.playSound(player, Sounds.POP, 1.0F, ((rand.nextFloat() - rand.nextFloat()) * 0.7F + 1.0F) * 2.0F);
					PlayerUtils.sendChat(player, StatCollector.translateToLocal("chat.zss.npc.mask_trader.returned"));
				} else if (mask != null) {
					new TimedChatDialogue(player, Arrays.asList(
							StatCollector.translateToLocalFormatted("chat.zss.npc.mask_trader.borrowed.0", mask.getItemStackDisplayName(new ItemStack(mask))),
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
					PlayerUtils.sendChat(player, StatCollector.translateToLocal("chat.zss.npc.mask_trader.selling." + rand.nextInt(4)));
					break;
				case 2: // need to pay for mask
					int price = (mask instanceof ItemMask ? ((ItemMask) mask).getBuyPrice() : 16);
					if (PlayerUtils.consumeInventoryItem(player, Items.emerald, price)) {
						PlayerUtils.playSound(player, Sounds.CASH_SALE, 1.0F, 1.0F);
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
							PlayerUtils.sendChat(player, StatCollector.translateToLocal("chat.zss.npc.mask_trader.sold"));
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
