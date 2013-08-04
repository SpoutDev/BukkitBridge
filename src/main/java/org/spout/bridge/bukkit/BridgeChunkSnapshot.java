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
package org.spout.bridge.bukkit;

import org.bukkit.ChunkSnapshot;
import org.bukkit.block.Biome;

/**
 * Represents a static, thread-safe snapshot of chunk of blocks. Purpose is to allow clean, efficient copy of a chunk data to be made, and then handed off for processing in another thread (e.g. map
 * rendering).
 */
public class BridgeChunkSnapshot implements ChunkSnapshot {
	private final int x, z;
	private final String worldname;
	private final short[][] blockids; // Block IDs, by section
	private final byte[][] blockdata;
	private final byte[][] skylight;
	private final byte[][] emitlight;
	private final boolean[] empty;
	private final int[] hmap; // Height map
	private final long captureFulltime;
	private final Biome[] biome;
	private final double[] biomeTemp;
	private final double[] biomeRain;

	public BridgeChunkSnapshot(int x, int z, String wname, long wtime, short[][] sectionBlockIDs, byte[][] sectionBlockData, byte[][] sectionSkyLights, byte[][] sectionEmitLights, boolean[] sectionEmpty, int[] hmap, Biome[] biome, double[] biomeTemp, double[] biomeRain) {
		this.x = x;
		this.z = z;
		this.worldname = wname;
		this.captureFulltime = wtime;
		this.blockids = sectionBlockIDs;
		this.blockdata = sectionBlockData;
		this.skylight = sectionSkyLights;
		this.emitlight = sectionEmitLights;
		this.empty = sectionEmpty;
		this.hmap = hmap;
		this.biome = biome;
		this.biomeTemp = biomeTemp;
		this.biomeRain = biomeRain;
	}

	public int getX() {
		return x;
	}

	public int getZ() {
		return z;
	}

	public String getWorldName() {
		return worldname;
	}

	public final int getBlockTypeId(int x, int y, int z) {
		return blockids[y >> 4][((y & 0xF) << 8) | (z << 4) | x];
	}

	public final int getBlockData(int x, int y, int z) {
		int off = ((y & 0xF) << 7) | (z << 3) | (x >> 1);
		return (blockdata[y >> 4][off] >> ((x & 1) << 2)) & 0xF;
	}

	public final int getBlockSkyLight(int x, int y, int z) {
		return skylight[y >> 4][((y & 0xF) << 8) | (z << 4) | x];
	}

	public final int getBlockEmittedLight(int x, int y, int z) {
		return emitlight[y >> 4][((y & 0xF) << 8) | (z << 4) | x];
	}

	public final int getHighestBlockYAt(int x, int z) {
		return hmap[z << 4 | x];
	}

	public final Biome getBiome(int x, int z) {
		return biome[z << 4 | x];
	}

	public final double getRawBiomeTemperature(int x, int z) {
		return biomeTemp[z << 4 | x];
	}

	public final double getRawBiomeRainfall(int x, int z) {
		return biomeRain[z << 4 | x];
	}

	public final long getCaptureFullTime() {
		return captureFulltime;
	}

	public final boolean isSectionEmpty(int sy) {
		return empty[sy];
	}
}
