package org.spout.bridge.bukkit.inventory;

import org.bukkit.block.DoubleChest;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.Inventory;

public class BridgeInventoryDoubleChest extends BridgeInventory implements DoubleChestInventory {
	@Override
	public Inventory getLeftSide() {
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public Inventory getRightSide() {
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public DoubleChest getHolder() {
		return null;
	}
}