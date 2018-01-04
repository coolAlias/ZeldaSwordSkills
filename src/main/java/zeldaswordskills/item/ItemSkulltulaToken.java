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

package zeldaswordskills.item;

import java.util.List;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;
import zeldaswordskills.api.item.IRightClickEntity;
import zeldaswordskills.api.item.IUnenchantable;
import zeldaswordskills.creativetab.ZSSCreativeTabs;
import zeldaswordskills.entity.ZSSVillagerInfo;
import zeldaswordskills.entity.player.ZSSPlayerInfo;
import zeldaswordskills.entity.player.quests.IQuest;
import zeldaswordskills.entity.player.quests.QuestBase;
import zeldaswordskills.entity.player.quests.QuestCursedMan;
import zeldaswordskills.entity.player.quests.ZSSQuests;
import zeldaswordskills.ref.ModInfo;
import zeldaswordskills.util.PlayerUtils;

public class ItemSkulltulaToken extends Item implements IRightClickEntity, IUnenchantable {

	public ItemSkulltulaToken() {
		super();
		this.setMaxDamage(0);
		this.canRepair = false;
		this.setCreativeTab(ZSSCreativeTabs.tabMisc);
	}

	@Override
	public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity entity) {
		if (!player.worldObj.isRemote && entity instanceof EntityVillager) {
			this.handleClick(stack, player, (EntityVillager) entity, true);
		}
		return true;
	}

	@Override
	public boolean onRightClickEntity(ItemStack stack, EntityPlayer player, Entity entity) {
		if (entity instanceof EntityVillager) {
			if (!player.worldObj.isRemote) {
				this.handleClick(stack, player, (EntityVillager) entity, false);
			}
			return true;
		}
		return false;
	}

	private void handleClick(ItemStack stack, EntityPlayer player, EntityVillager villager, boolean isLeftClick) {
		ZSSVillagerInfo info = ZSSVillagerInfo.get(villager);
		if (villager.getClass() == EntityVillager.class && ("Cursed Man").equals(villager.getCustomNameTag())) {
			// Use existing skulltula tokens for backwards compatibility
			IQuest quest = ZSSQuests.get(player).add(new QuestCursedMan(ZSSPlayerInfo.get(player).getSkulltulaTokens()));
			if (villager.isChild()) {
				PlayerUtils.sendTranslatedChat(player, "chat.zss.npc.cursed_man.child");
			} else if (quest.isComplete(player)) {
				// This quest can potentially be "completed" many times
				if (quest.canComplete(player, villager)) {
					quest.complete(player, villager);
				} else {
					PlayerUtils.sendTranslatedChat(player, "chat.zss.npc.cursed_man.complete");
				}
			} else {
				// Handles beginning, updating, and completing the quest for the first time
				QuestBase.checkQuestProgress(player, quest, QuestBase.DEFAULT_QUEST_HANDLER, villager);
			}
		} else if (villager.isChild()) {
			PlayerUtils.sendTranslatedChat(player, "chat.zss.trade.generic.child");
		} else if (info.isHunter()) {
			info.addHunterTrade(player, new ItemStack(this), 20);
		} else {
			int i = villager.getProfession();
			PlayerUtils.sendTranslatedChat(player, "chat.zss.skulltula_token.villager." + (i > 4 ? "custom" : i));
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister register) {
		itemIcon = register.registerIcon(ModInfo.ID + ":" + getUnlocalizedName().substring(9));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack,	EntityPlayer player, List list, boolean isHeld) {
		list.add(StatCollector.translateToLocal("tooltip." + getUnlocalizedName().substring(5) + ".desc.0"));
	}
}
