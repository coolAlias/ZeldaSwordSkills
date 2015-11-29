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

import java.util.Iterator;

import net.minecraft.entity.IMerchant;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import zeldaswordskills.item.ItemCustomEgg;

/**
 * 
 * Base merchant class for non-villager Npcs with a default trading implementation
 * similar to vanilla villagers.
 *
 */
public abstract class EntityNpcMerchantBase extends EntityNpcBase implements IMerchant
{
	/** MerchantRecipeList of all currently available trades */
	protected MerchantRecipeList trades;

	/** The merchant's current customer */
	protected EntityPlayer customer;

	/** The merchant's last customer, used for adding to that player's reputation */
	protected String lastCustomer;

	/** Set to true when trades will gain more uses, e.g. when a trade is used for the first time */
	protected boolean refreshTrades;

	/** Time remaining until trades are refreshed */
	protected int refreshTimer;

	public EntityNpcMerchantBase(World world) {
		super(world);
	}

	/**
	 * Called when the merchant's trade list is null or empty to add trades
	 */
	protected abstract void populateTradingList();

	/**
	 * Called each time the trading list is refreshed. The default implementation
	 * increases the max trade uses of any disabled recipes, just as vanilla does.
	 */
	protected void refreshTradingList() {
		for (Iterator<MerchantRecipe> iterator = trades.iterator(); iterator.hasNext();) {
			MerchantRecipe trade = iterator.next();
			if (trade.isRecipeDisabled()) {
				// func_82783_a is increaseMaxTradeUses
				trade.func_82783_a(rand.nextInt(6) + rand.nextInt(6) + 2);
			}
		}
	}

	/**
	 * Called after the trading list is refreshed to add new trades. Vanilla
	 * villagers would also call this while populating the list for the first time.
	 */
	protected abstract void updateTradingList();

	/**
	 * Displays the vanilla trading interface for the given player if trades are available
	 */
	protected void displayTradingGuiFor(EntityPlayer player) {
		// allowing null trades results in #getRecipes being called and thus lazy population of the list
		if (!worldObj.isRemote && (trades == null || trades.size() > 0)) {
			setCustomer(player);
			player.displayGUIMerchant(this, this.getCustomNameTag());
		}
	}

	@Override
	public EntityPlayer getCustomer() {
		return customer;
	}

	@Override
	public void setCustomer(EntityPlayer player) {
		customer = player;
	}

	@Override
	public MerchantRecipeList getRecipes(EntityPlayer player) {
		if (trades == null || trades.isEmpty()) {
			populateTradingList();
		}
		return trades;
	}

	@Override
	public void setRecipes(MerchantRecipeList trades) {
		this.trades = trades;
	}

	@Override
	public void useRecipe(MerchantRecipe recipe) {
		recipe.incrementToolUses();
		livingSoundTime = -getTalkInterval();
		playSound("mob.villager.yes", getSoundVolume(), getSoundPitch());
		if (recipe.hasSameIDsAs((MerchantRecipe) trades.get(trades.size() - 1))) {
			refreshTimer = 40;
			refreshTrades = true;
			lastCustomer = (customer == null ? null : customer.getCommandSenderName());
		}
	}

	@Override
	public void func_110297_a_(ItemStack stack) {
		if (!worldObj.isRemote && livingSoundTime > -getTalkInterval() + 20) {
			livingSoundTime = -getTalkInterval();
			playSound((stack == null ? "mob.villager.no" : "mob.villager.yes"), getSoundVolume(), getSoundPitch());
		}
	}

	@Override
	public boolean interact(EntityPlayer player) {
		ItemStack stack = player.inventory.getCurrentItem();
		boolean flag = stack != null && (stack.getItem() == Items.spawn_egg || stack.getItem() instanceof ItemCustomEgg);
		if (!flag && isEntityAlive() && getCustomer() == null && !isChild() && !player.isSneaking()) {
			displayTradingGuiFor(player);
			return true;
		}
		return super.interact(player);
	}

	@Override
	protected void updateAITasks() {
		if (getCustomer() == null && refreshTimer > 0) {
			--refreshTimer;
			if (refreshTimer <= 0) {
				if (refreshTrades) {
					refreshTradingList();
					updateTradingList();
					refreshTrades = false;
					if (villageObj != null && lastCustomer != null) {
						worldObj.setEntityState(this, (byte) 14);
						villageObj.setReputationForPlayer(lastCustomer, 1);
					}
				}
				addPotionEffect(new PotionEffect(Potion.regeneration.id, 200, 0));
			}
		}
		super.updateAITasks();
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound compound) {
		super.writeEntityToNBT(compound);
		if (trades != null) {
			compound.setTag("Offers", trades.getRecipiesAsTags());
		}
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound compound) {
		super.readEntityFromNBT(compound);
		if (compound.hasKey("Offers", Constants.NBT.TAG_COMPOUND)) {
			NBTTagCompound tradeTag = compound.getCompoundTag("Offers");
			trades = new MerchantRecipeList(tradeTag);
		}
	}
}
