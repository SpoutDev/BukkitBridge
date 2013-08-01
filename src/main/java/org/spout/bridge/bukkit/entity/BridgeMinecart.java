/*
 * This file is part of BukkitBridge.
 *
 * Copyright (c) 2012 Spout LLC <http://www.spout.org/>
 * BukkitBridge is licensed under the GNU General Public License.
 *
 * BukkitBridge is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * BukkitBridge is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
/*
 * This file is part of Vanilla.
 *
 * Copyright (c) 2011-2012, VanillaDev <http://www.spout.org/>
 * Vanilla is licensed under the SpoutDev License Version 1.
 *
 * Vanilla is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * In addition, 180 days after any changes are published, you can use the
 * software, incorporating those changes, under the terms of the MIT license,
 * as described in the SpoutDev License Version 1.
 *
 * Vanilla is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License,
 * the MIT license and the SpoutDev License Version 1 along with this program.
 * If not, see <http://www.gnu.org/licenses/> for the GNU Lesser General Public
 * License and see <http://www.spout.org/SpoutDevLicenseV1.txt> for the full license,
 * including the MIT license.
 */
package org.spout.bridge.bukkit.entity;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Minecart;
import org.bukkit.util.Vector;

import org.spout.api.entity.Entity;

public class BridgeMinecart extends BridgeVehicle implements Minecart {
	protected BridgeMinecart(Entity handle) {
		super(handle);
	}

    @Override
    public void _INVALID_setDamage(int i) {
        setDamage(i);
    }

    @Override
    public void setDamage(double v) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int _INVALID_getDamage() {
        return (int) getDamage();
    }

    @Override
	public double getDamage() {
		throw new UnsupportedOperationException();
	}

	@Override
	public double getMaxSpeed() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setMaxSpeed(double v) {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public boolean isSlowWhenEmpty() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setSlowWhenEmpty(boolean b) {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public Vector getFlyingVelocityMod() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setFlyingVelocityMod(Vector vector) {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public Vector getDerailedVelocityMod() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setDerailedVelocityMod(Vector vector) {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public EntityType getType() {
		return EntityType.MINECART;
	}
}
