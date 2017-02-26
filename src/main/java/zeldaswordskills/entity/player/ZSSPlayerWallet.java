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

package zeldaswordskills.entity.player;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import zeldaswordskills.item.ItemRupee;
import zeldaswordskills.item.ZSSItems;
import zeldaswordskills.network.PacketDispatcher;
import zeldaswordskills.network.client.SyncPlayerWalletPacket;
import zeldaswordskills.ref.Config;

public class ZSSPlayerWallet
{
	public static enum EnumWallet {
		NONE("none", 0, 0),
		CHILD("child", 99, 0),
		ADULT("adult", 200, 1),
		SMALL("small", 300, 2),
		MEDIUM("medium", 500, 3),
		BIG("big", 1000, 4),
		GIANT("giant", 5000, 5),
		TYCOON("tycoon", 9999, 6);
		public final String unlocalized_name;
		public final int capacity;
		public final int icon_index;
		private EnumWallet(String name, int capacity, int icon_index) {
			this.unlocalized_name = name;
			this.capacity = capacity;
			this.icon_index = icon_index;
		}
		/** Returns the full unlocalized name */
		public String getUnlocalizedName() {
			return "item.zss.wallet_" + this.unlocalized_name + ".name";
		}
		/** True if the wallet can be further upgraded */
		public boolean canUpgrade() {
			return this != EnumWallet.TYCOON;
		}
		/** Returns the next wallet in the upgrade path */
		public EnumWallet next() {
			return (canUpgrade() ? EnumWallet.byOrdinal(this.ordinal() + 1) : this);
		}
		public static EnumWallet byOrdinal(int n) {
			return EnumWallet.values()[n % EnumWallet.values().length];
		}
	}

	private final EntityPlayer player;

	private EnumWallet wallet;

	private int rupees = 0;

	public ZSSPlayerWallet(EntityPlayer player) {
		this.wallet = (Config.enableStartingWallet ? EnumWallet.CHILD : EnumWallet.NONE);
		this.player = player;
	}

	public static ZSSPlayerWallet get(EntityPlayer player) {
		return ZSSPlayerInfo.get(player).getPlayerWallet();
	}

	public EnumWallet getWallet() {
		return this.wallet;
	}

	public int getCapacity() {
		return this.wallet.capacity;
	}

	public int getRupees() {
		return this.rupees;
	}

	/**
	 * Resets the player's wallet to initial size, losing any rupees over the new capacity
	 */
	public void reset() {
		this.wallet = (Config.enableStartingWallet ? EnumWallet.CHILD : EnumWallet.NONE);
		this.rupees = Math.min(this.rupees, this.wallet.capacity);
		this.sync();
	}

	/**
	 * Upgrades the wallet to the next larger size, returning true if successful
	 */
	public boolean upgrade() {
		if (this.wallet.canUpgrade()) {
			this.wallet = this.wallet.next();
			this.sync();
			return true;
		}
		return false;
	}

	/**
	 * Adds as many rupees as possible from the provided stack
	 * @return Input stack with appropriately decremented stack size (may be 0)
	 */
	public ItemStack addRupees(ItemStack stack) {
		if (stack == null || stack.getItem() != ZSSItems.rupee) {
			return stack;
		}
		ItemRupee.Rupee rupee = ItemRupee.Rupee.byDamage(stack.getItemDamage());
		while (stack.stackSize > 0 && this.addRupees(rupee.value, false)) {
			--stack.stackSize;
		}
		this.sync();
		return stack;
	}

	/**
	 * Adds the entire amount to the wallet, or none if it would exceed the wallet's capacity.
	 * @param n Number of rupees to add, at least 1
	 * @return True if all n rupees were added.
	 */
	public boolean addRupees(int n) {
		return this.addRupees(n, true);
	}

	/**
	 * See {@link #addRupees(int)}
	 * @param sync Pass false to avoid sending an update packet
	 */
	public boolean addRupees(int n, boolean sync) {
		if (n > 0 && this.rupees + n <= this.wallet.capacity) {
			this.rupees = Math.max(this.rupees + n, 0);
			if (sync) {
				this.sync();
			}
			return true;
		}
		return false;
	}

	/**
	 * Sets the current number of rupees to 0 <= n <= getCapacity()
	 */
	public void setRupees(int n) {
		this.rupees = Math.min(this.getCapacity(), Math.max(n, 0));
		this.sync();
	}

	/**
	 * Removes the entire amount from the wallet if possible, or none.
	 * @param n Number of rupees to spend, at least 1
	 * @return True if all n rupees were removed.
	 */
	public boolean spendRupees(int n) {
		return this.spendRupees(n, true);
	}

	/**
	 * See {@link #spendRupees(int)}
	 * @param sync Pass false to avoid sending an update packet
	 */
	public boolean spendRupees(int n, boolean sync) {
		if (n > 0 && this.rupees >= n) {
			this.rupees -= n;
			if (sync) {
				this.sync();
			}
			return true;
		}
		return false;
	}

	/**
	 * Sends the wallet type and rupee count to the client
	 */
	public void sync() {
		if (this.player instanceof EntityPlayerMP) {
			PacketDispatcher.sendTo(new SyncPlayerWalletPacket(this), (EntityPlayerMP) this.player);
		}
	}

	public void saveNBTData(NBTTagCompound compound) {
		compound.setInteger("walletIndex", this.wallet.ordinal());
		compound.setInteger("walletRupees", this.rupees);
	}

	public void loadNBTData(NBTTagCompound compound) {
		this.wallet = EnumWallet.byOrdinal(compound.getInteger("walletIndex"));
		this.rupees = compound.getInteger("walletRupees");
	}
}
