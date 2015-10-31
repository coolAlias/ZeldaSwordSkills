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

import java.util.ListIterator;

import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import zeldaswordskills.api.entity.BombType;
import zeldaswordskills.api.entity.INpcVillager;
import zeldaswordskills.item.ItemBomb;
import zeldaswordskills.item.ItemBombBag;
import zeldaswordskills.item.ZSSItems;
import zeldaswordskills.ref.Config;
import zeldaswordskills.ref.Sounds;
import zeldaswordskills.util.MerchantRecipeHelper;
import zeldaswordskills.util.PlayerUtils;

public class EntityNpcBarnes extends EntityNpcMerchantBase implements INpcVillager
{
	private static final MerchantRecipe standardBomb = new MerchantRecipe(new ItemStack(Items.emerald, 8), null, new ItemStack(ZSSItems.bomb, 1, BombType.BOMB_STANDARD.ordinal()), 0, 99);
	private static final MerchantRecipe waterBomb = new MerchantRecipe(new ItemStack(Items.emerald, 12), null, new ItemStack(ZSSItems.bomb, 1, BombType.BOMB_WATER.ordinal()), 0, 99);
	private static final MerchantRecipe fireBomb = new MerchantRecipe(new ItemStack(Items.emerald, 16), null, new ItemStack(ZSSItems.bomb, 1, BombType.BOMB_FIRE.ordinal()), 0, 99);
	private static final MerchantRecipe bombSeeds = new MerchantRecipe(new ItemStack(ZSSItems.bombFlowerSeed), null, new ItemStack(Items.emerald, 4), 0, 99);

	public EntityNpcBarnes(World world) {
		super(world);
	}

	@Override
	protected String getNameTagOnSpawn() {
		return "Barnes";
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
	protected void populateTradingList() {
		MerchantRecipeList newTrades = new MerchantRecipeList();
		newTrades.add(standardBomb);
		if (trades == null) {
			trades = newTrades;
		} else {
			// Add any previous trades after bomb-related trades
			for (int i = 0; i < trades.size(); ++i) {
				MerchantRecipeHelper.addToListWithCheck(newTrades, (MerchantRecipe) trades.get(i));
			}
			trades = newTrades;
		}
	}

	@Override
	protected void refreshTradingList() {
		for (ListIterator<MerchantRecipe> iterator = trades.listIterator(); iterator.hasNext();) {
			MerchantRecipe trade = iterator.next();
			// replace bomb trades so he can't ever become sold out
			if (isBombTrade(trade) && trade.getToolUses() > 49) {
				iterator.set(getRefreshedBombTrade(trade));
			} else if (trade.isRecipeDisabled()) {
				trade.increaseMaxTradeUses(rand.nextInt(6) + rand.nextInt(6) + 2);
			}
		}
	}

	@Override
	protected void updateTradingList() {}

	private boolean isBombTrade(MerchantRecipe trade) {
		return isBombTrade(trade.getItemToSell()) || isBombTrade(trade.getItemToBuy());
	}

	private boolean isBombTrade(ItemStack stack) {
		return stack != null && (stack.getItem() instanceof ItemBomb || stack.getItem() instanceof ItemBombBag || stack.getItem() == ZSSItems.bombFlowerSeed);
	}

	private MerchantRecipe getRefreshedBombTrade(MerchantRecipe trade) {
		int uses = trade.getToolUses();
		return new MerchantRecipe(trade.getItemToBuy(), trade.getSecondItemToBuy(), trade.getItemToSell(), (uses > 0 ? 1 : 0), 99);
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
		}
		ItemStack stack = player.getHeldItem();
		String chat = "chat.zss.npc.barnes.greeting";
		boolean openGui = true;
		if (stack != null && trades != null) {
			if (stack.getItem() == ZSSItems.bombFlowerSeed) {
				if (insertBombTrade(bombSeeds)) {
					chat = "chat.zss.npc.barnes.trade.bombseeds.new";
				} else {
					chat = "chat.zss.npc.barnes.trade.bombseeds.old";
				}
			} else if (stack.getItem() == Items.fish) {
				if (insertBombTrade(waterBomb)) {
					--stack.stackSize;
					chat = "chat.zss.npc.barnes.trade.water";
					openGui = false;
				}
			} else if (stack.getItem() == Items.magma_cream) {
				if (insertBombTrade(fireBomb)) {
					--stack.stackSize;
					chat = "chat.zss.npc.barnes.trade.fire";
					openGui = false;
				}
			} else if (!MerchantRecipeHelper.hasSimilarTrade(trades, waterBomb)) {
				chat = "chat.zss.npc.barnes.material.water";
			} else if (!MerchantRecipeHelper.hasSimilarTrade(trades, fireBomb)) {
				chat = "chat.zss.npc.barnes.material.fire";
			}
		}
		if (!worldObj.isRemote) {
			PlayerUtils.sendTranslatedChat(player, chat);
			if (openGui) {
				displayTradingGuiFor(player);
			}
		}
		return true;
	}

	/**
	 * Returns true if a Bomb Bag trade was added (must be enabled in Config)
	 */
	public boolean addBombBagTrade() {
		return (Config.enableTradeBombBag() && insertBombTrade(new MerchantRecipe(new ItemStack(Items.emerald, Config.getBombBagPrice()), null, new ItemStack(ZSSItems.bombBag), 0, 99)));
	}

	/**
	 * Inserts the trade at the first available slot after any bombs already in stock
	 * @return true if the trade was inserted, or false if a similar trade already existed
	 */
	private boolean insertBombTrade(MerchantRecipe trade) {
		if (trades == null || MerchantRecipeHelper.hasSimilarTrade(trades, trade)) {
			return false;
		}
		int i = 0;
		for (i = 0; i < trades.size(); ++i) {
			MerchantRecipe r = (MerchantRecipe) trades.get(i);
			if (!(r.getItemToSell().getItem() instanceof ItemBomb)) {
				break;
			}
		}
		trades.add(i, trade);
		return true;
	}

	@Override
	public Result canInteractConvert(EntityPlayer player, EntityVillager villager) {
		if (player.worldObj.isRemote || villager.getClass() != EntityVillager.class || villager.isChild()) {
			return Result.DEFAULT;
		} else if (PlayerUtils.consumeHeldItem(player, Items.gunpowder, 1)) {
			return Result.ALLOW;
		}
		PlayerUtils.sendTranslatedChat(player, "chat.zss.npc.barnes.hmph");
		return Result.DENY;
	}

	@Override
	public Result canLeftClickConvert(EntityPlayer player, EntityVillager villager) {
		return Result.DEFAULT;
	}

	@Override
	public void onConverted(EntityPlayer player) {
		PlayerUtils.sendTranslatedChat(player, "chat.zss.npc.barnes.open");
		populateTradingList();
	}
}
