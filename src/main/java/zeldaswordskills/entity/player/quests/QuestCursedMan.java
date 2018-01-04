/**
    Copyright (C) <2018> <coolAlias>

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

package zeldaswordskills.entity.player.quests;

import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.IChatComponent;
import zeldaswordskills.api.entity.BombType;
import zeldaswordskills.entity.ZSSVillagerInfo;
import zeldaswordskills.entity.player.ZSSPlayerSkills;
import zeldaswordskills.entity.player.ZSSPlayerWallet;
import zeldaswordskills.item.ItemBombBag;
import zeldaswordskills.item.ItemRupee.Rupee;
import zeldaswordskills.item.ZSSItems;
import zeldaswordskills.ref.Config;
import zeldaswordskills.ref.Sounds;
import zeldaswordskills.skills.SkillBase;
import zeldaswordskills.util.BossType;
import zeldaswordskills.util.PlayerUtils;
import zeldaswordskills.util.TimedAddItem;
import zeldaswordskills.util.TimedChatDialogue;

public final class QuestCursedMan extends QuestBase
{
	/** Maximum number of skulltula tokens which can be turned in */
	public static final int MAX_SKULLTULA_TOKENS = 100;

	/** Number of tokens player has turned in */
	private int tokens;

	public QuestCursedMan() {}

	/**
	 * Special constructor for backwards compatibility: starts the quest with the given number of tokens
	 */
	public QuestCursedMan(int tokens) {
		this.set(FLAG_BEGIN);
		this.tokens = tokens;
		if (this.tokens >= MAX_SKULLTULA_TOKENS) {
			this.set(FLAG_COMPLETE);
		}
	}

	@Override
	public boolean canBegin(EntityPlayer player) {
		return super.canBegin(player) && PlayerUtils.consumeHeldItem(player, ZSSItems.skulltulaToken, 1);
	}

	@Override
	protected boolean onBegin(EntityPlayer player, Object... data) {
		this.tokens = 1;
		PlayerUtils.sendTranslatedChat(player, "chat.zss.npc.cursed_man.token.1");
		return true;
	}

	/**
	 * @param data Expects data[0] to be an EntityVillager
	 */
	@Override
	public boolean canComplete(EntityPlayer player, Object... data) {
		if (this.tokens < MAX_SKULLTULA_TOKENS) {
			return false;
		} else if (this.isComplete(player) && Config.getSkulltulaRewardRate() > 0 && data != null && data[0] instanceof EntityVillager) {
			return player.worldObj.getTotalWorldTime() > ZSSVillagerInfo.get((EntityVillager) data[0]).getNextSkulltulaReward();
		}
		return super.canComplete(player);
	}

	/**
	 * @param data Expects data[0] to be an EntityVillager
	 */
	@Override
	protected boolean onComplete(EntityPlayer player, Object... data) {
		if (this.tokens < MAX_SKULLTULA_TOKENS || data == null || !(data[0] instanceof EntityVillager)) {
			return false;
		}
		ItemStack reward = new ItemStack(ZSSItems.rupee, 1, Rupee.GOLD_RUPEE.ordinal());
		new TimedChatDialogue(player, new ChatComponentTranslation("chat.zss.npc.cursed_man.token." + this.tokens), new ChatComponentTranslation("chat.zss.npc.cursed_man.reward." + this.tokens, new ChatComponentTranslation(reward.getUnlocalizedName() + ".name")));
		new TimedAddItem(player, reward, 2500, Sounds.SUCCESS);
		if (Config.getSkulltulaRewardRate() > 0) {
			ZSSVillagerInfo.get((EntityVillager) data[0]).setNextSkulltulaReward(player.worldObj.getTotalWorldTime() + (24000 * Config.getSkulltulaRewardRate()));
		}
		return true;
	}

	@Override
	public void forceComplete(EntityPlayer player, Object... data) {
		this.tokens = MAX_SKULLTULA_TOKENS;
		this.set(FLAG_COMPLETE);
	}

	/**
	 * @param data Expects data[0] to be an EntityVillager
	 */
	@Override
	public boolean update(EntityPlayer player, Object... data) {
		if (data == null || !(data[0] instanceof EntityVillager)) {
			return false;
		} else if (!PlayerUtils.consumeHeldItem(player, ZSSItems.skulltulaToken, 1)) {
			return false;
		}
		this.tokens++;
		ItemStack reward = null;
		switch (this.tokens) {
		case 10:
			reward = new ItemStack(ZSSItems.whip);
			break;
		case 20:
			reward = new ItemStack(ZSSItems.tunicZoraChest);
			break;
		case 30:
			if (ZSSPlayerWallet.get(player).getWallet().canUpgrade()) {
				reward = new ItemStack(ZSSItems.walletUpgrade);
			} else {
				reward = new ItemStack(ZSSItems.bombBag);
				((ItemBombBag) reward.getItem()).addBombs(reward, new ItemStack(ZSSItems.bomb, 10));
			}
			break;
		case 40:
			reward = new ItemStack(ZSSItems.keyBig, 1, player.worldObj.rand.nextInt(BossType.values().length));
			break;
		case 50:
			ZSSPlayerSkills skills = ZSSPlayerSkills.get(player);
			for (SkillBase skill : SkillBase.getSkills()) {
				if (skill.getId() != SkillBase.bonusHeart.getId() && skills.getSkillLevel(skill) < skill.getMaxLevel() && (reward == null || player.worldObj.rand.nextInt(4) == 0)) {
					reward = new ItemStack(ZSSItems.skillOrb, 1, skill.getId());
				}
			}
			if (reward == null) {
				reward = new ItemStack(ZSSItems.arrowLight, 20);
			}
			break;
		case 75:
			if (ZSSPlayerWallet.get(player).getWallet().canUpgrade()) {
				reward = new ItemStack(ZSSItems.walletUpgrade);
			} else {
				reward = new ItemStack(ZSSItems.bombBag);
				((ItemBombBag) reward.getItem()).addBombs(reward, new ItemStack(ZSSItems.bomb, 10, BombType.BOMB_FIRE.ordinal()));
			}
			break;
		case MAX_SKULLTULA_TOKENS:
			// Completes the quest for the first time
			return this.complete(player, data);
		default: PlayerUtils.sendTranslatedChat(player, "chat.zss.npc.cursed_man.amount", this.tokens);
		}
		if (reward != null) {
			new TimedChatDialogue(player, new ChatComponentTranslation("chat.zss.npc.cursed_man.token." + this.tokens), new ChatComponentTranslation("chat.zss.npc.cursed_man.reward." + this.tokens, new ChatComponentTranslation(reward.getUnlocalizedName() + ".name")));
			new TimedAddItem(player, reward, 2500, Sounds.SUCCESS);
			return true;
		}
		return false;
	}

	@Override
	public IChatComponent getHint(EntityPlayer player, Object... data) {
		return null;
	}

	@Override
	public boolean requiresSync() {
		return false; // TODO required on client at all for interactions? don't think so
	}

	@Override
	public void writeToNBT(NBTTagCompound compound) {
		super.writeToNBT(compound);
		compound.setInteger("tokens", this.tokens);
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		this.tokens = compound.getInteger("tokens");
	}
}
